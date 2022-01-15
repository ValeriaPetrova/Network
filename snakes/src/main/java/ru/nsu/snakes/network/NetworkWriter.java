package ru.nsu.snakes.network;

import me.ippolitov.fit.snakes.SnakesProto;
import ru.nsu.snakes.Controller;
import ru.nsu.snakes.Model;

import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.*;

import static me.ippolitov.fit.snakes.SnakesProto.NodeRole.MASTER;

public class NetworkWriter implements Runnable {
    public static LinkedBlockingDeque<MessageCustom> resend = new LinkedBlockingDeque<>();
    public static BlockingQueue<SnakesProto.GameMessage> queue = new LinkedBlockingQueue<>() ; //queue on output
    public static ConcurrentHashMap<SnakesProto.GamePlayer, LocalTime> lastSent = new ConcurrentHashMap<>();
    public static LocalTime invoketime = null;
    public LocalTime checktime = null;

    public static void start() {
        Thread thread = new Thread(new NetworkWriter());
        thread.start();
    }

    public void run() {
        while (true) {
            SnakesProto.GameMessage message;
            try {
                message = queue.poll(Model.config.getPingDelayMs(), TimeUnit.MILLISECONDS);
                if (message != null) {
                    switch (message.getTypeCase()) {
                        case PING:
                        case ERROR:
                        case STEER:
                        case JOIN: {
                            send_single(message);
                            break;
                        }
                        case ACK:{
                            send_ack(message);
                            break;
                        }
                        case STATE:{
                            send_all(message);
                            break;
                        }
                        case ANNOUNCEMENT:{
                            System.out.println("ERROR, wrong type of message");
                            break;
                        }
                        case ROLE_CHANGE:{
                            if (message.getRoleChange().hasSenderRole() && message.getRoleChange().getSenderRole() == SnakesProto.NodeRole.MASTER) {
                                if (!message.getRoleChange().hasReceiverRole()){
                                    send_all(message);
                                } else {
                                    send_single(message);
                                }
                            } else {
                                send_single(message);
                            }
                            break;
                        }
                    }
                }
            } catch (InterruptedException e) {
                System.out.println("polling was interrupted");
                e.printStackTrace();
            }
            while ((invoketime != null) && invoketime.isBefore(LocalTime.now())) {
                MessageCustom msg = resend.poll();
                if (msg != null) {
                    if (msg.origtime.plusNanos(Model.config.getNodeTimeoutMs() * 1000000).isBefore(LocalTime.now())) {
                        deleteclients(msg.branches);
                        MessageCustom next = resend.peek();
                        if (next != null) {
                            invoketime = next.sendtime.plusNanos(Model.config.getPingDelayMs() * 1000000);
                        }
                        else invoketime = null;
                        continue;
                    }
                    if (msg.branches.size() > 0) {
                        synchronized (msg.branches) {
                            Iterator<SnakesProto.GamePlayer> iter = msg.branches.iterator();
                            while (iter.hasNext()) {
                                SnakesProto.GamePlayer player = iter.next();
                                Sender sender = new Sender();
                                sender.ip = player.getIpAddress();
                                sender.port = player.getPort();
                                if (!Controller.senderList.contains(sender)) {
                                    if (Model.state.getPlayers().getPlayersList().contains(player)) {
                                        if (player.getRole().equals(SnakesProto.NodeRole.MASTER)) {
                                            SnakesProto.GamePlayer master = Controller.getPlayer(Controller.masterId);
                                            System.out.println("sends to new master " + master);
                                            if (master != null && Controller.role != MASTER) {
                                                Network.send(msg.gm, master);
                                            }
                                        } else {
                                            Network.send(msg.gm, player);
                                        }
                                    } else {
                                        iter.remove();
                                    }
                                } else {
                                    Network.send(msg.gm, player);
                                }
                            }
                        }
                        msg.sendtime = LocalTime.now();
                        resend.add(msg);
                    }
                }
                MessageCustom next = resend.peek();
                if (next != null) {
                    invoketime = next.sendtime.plusNanos(Model.config.getPingDelayMs() * 1000000);
                }
                else invoketime = null;
            }

            if (Model.state != null) {
                synchronized (lastSent) {
                    Iterator<Map.Entry<SnakesProto.GamePlayer, LocalTime>> it = lastSent.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry<SnakesProto.GamePlayer, LocalTime> pair = it.next();
                        if (!Model.state.getPlayers().getPlayersList().contains(pair.getKey())) {
                            it.remove();
                            continue;
                        }
                        if (pair.getValue().plusNanos(Model.config.getPingDelayMs() * 1000000).isBefore(LocalTime.now())) {
                            send_ping(pair.getKey());
                        }
                    }
                }
            }
        }
    }

    private void send_ping(SnakesProto.GamePlayer player) {
        SnakesProto.GameMessage.Builder gm = SnakesProto.GameMessage.newBuilder();
        SnakesProto.GameMessage.PingMsg.Builder ping = SnakesProto.GameMessage.PingMsg.newBuilder();
        gm.setPing(ping.build());
        gm.setReceiverId(player.getId());
        gm.setMsgSeq(Model.getMsgId());
        send_single(gm.build());
    }

    public static void sendError(SnakesProto.GameMessage message, SnakesProto.GamePlayer receiver) {
        MessageCustom mst = new MessageCustom();
        mst.gm = message;
        mst.branches = new ArrayList<>();
        mst.branches.add(receiver);
        mst.origtime = LocalTime.now();
        mst.sendtime = mst.origtime;
        Network.send(message, receiver);
        Iterator<MessageCustom> iter = resend.iterator();
        while (iter.hasNext()) {
            MessageCustom msg = iter.next();
            if ((msg.gm.getReceiverId() == message.getReceiverId()) && (msg.gm.getTypeCase() == message.getTypeCase())) {
                iter.remove();
                break;
            }
        }
        boolean res = resend.add(mst);
        if (invoketime == null) {
            invoketime = mst.origtime.plusNanos(Model.config.getPingDelayMs() * 1000000);
        }
    }

    public static void Start() {
        Thread t = new Thread(new NetworkWriter());
        t.start();
    }

    private void send_all(SnakesProto.GameMessage message) {
        MessageCustom mst = new MessageCustom();
        mst.gm = message;
        mst.branches = new ArrayList<>(Model.state.getPlayers().getPlayersList());
        mst.branches.removeIf(pl -> pl.getId() == Controller.playerId);
        mst.origtime = LocalTime.now();
        mst.sendtime = mst.origtime;
        synchronized (resend) {
            Iterator<MessageCustom> iter = resend.iterator();
            while (iter.hasNext()) {
                MessageCustom msg = iter.next();
                if (msg.gm.getTypeCase() == message.getTypeCase()) resend.remove(msg);
            }
        }
        boolean res = resend.add(mst);
        if (invoketime == null) {
            invoketime = mst.origtime.plusNanos(Model.config.getPingDelayMs() * 1000000);
        }
        synchronized (mst.branches) {
            for (SnakesProto.GamePlayer gamePlayer : mst.branches) {
                Network.send(message, gamePlayer);
            }
        }
    }

    private void send_single(SnakesProto.GameMessage message) {
        MessageCustom mst = new MessageCustom();
        mst.gm = message;
        mst.branches = new ArrayList<>();
        SnakesProto.GamePlayer player = Controller.getPlayer(message.getReceiverId());
        if (player == null) return;
        mst.branches.add(player);
        mst.origtime = LocalTime.now();
        mst.sendtime = mst.origtime;
        Network.send(message, player);
        synchronized (resend) {
            Iterator<MessageCustom> iter = resend.iterator();
            while (iter.hasNext()) {
                MessageCustom msg = iter.next();
                if ((msg.gm.getReceiverId() == message.getReceiverId()) && (msg.gm.getTypeCase() == message.getTypeCase())) {
                    resend.remove(msg);
                    break;
                }
            }
        }
        boolean res = resend.add(mst);
        if (invoketime == null) {
            invoketime = mst.origtime.plusNanos(Model.config.getPingDelayMs() * 1000000);
        }
    }

    private static void send_ack(SnakesProto.GameMessage gm) {
        SnakesProto.GamePlayer player = Controller.getPlayer(gm.getReceiverId());
        if ((player == null)) {
            System.out.println("player == null");
            return;
        }

        synchronized (NetworkReader.received) {
            Iterator<Map.Entry<Sender, SnakesProto.GameMessage>> it = NetworkReader.received.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Sender, SnakesProto.GameMessage> pair = it.next();
                if ((gm.getMsgSeq() == pair.getValue().getMsgSeq()) && (player.getIpAddress().equals(pair.getKey().ip)) && (player.getPort() == pair.getKey().port)) {
                    Network.send(gm, Objects.requireNonNull(Controller.getPlayer(gm.getReceiverId())));
                    break;
                }
            }
        }

    }

    public void deleteclients(List<SnakesProto.GamePlayer> clients) {
        System.out.println("delete clients called");
        if (Model.state != null) {
            Iterator<SnakesProto.GamePlayer> it = clients.iterator();
            while (it.hasNext()) {
                SnakesProto.GamePlayer pl = it.next();
                SnakesProto.GamePlayer player = Controller.getPlayer(pl.getId());
                if (player == null) {
                    continue;
                }
                if ((player.getRole().equals(SnakesProto.NodeRole.MASTER)) && (Objects.equals(Controller.getRole(Controller.playerId), SnakesProto.NodeRole.NORMAL))) {
                    Controller.changeMaster();
                }
                if ((player.getRole().equals(SnakesProto.NodeRole.MASTER)) && (Objects.equals(Controller.getRole(Controller.playerId), SnakesProto.NodeRole.DEPUTY))) {
                    Controller.becomeMaster();
                }
                if ((player.getRole().equals(SnakesProto.NodeRole.DEPUTY)) && (Objects.equals(Controller.getRole(Controller.playerId), SnakesProto.NodeRole.MASTER))) {
                    Controller.findDeputy();
                }
                Model.disconnect(player.getId());
            }
        }
    }
}

