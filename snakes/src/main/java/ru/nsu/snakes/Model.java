package ru.nsu.snakes;

import ru.nsu.snakes.network.GameListReceiver;
import ru.nsu.snakes.network.GameListSender;
import ru.nsu.snakes.network.MessageCustom;
import ru.nsu.snakes.network.NetworkWriter;
import ru.nsu.snakes.network.Sender;
import ru.nsu.snakes.view.GUI;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Iterator;

import static me.ippolitov.fit.snakes.SnakesProto.NodeRole.*;
import static me.ippolitov.fit.snakes.SnakesProto.NodeRole.VIEWER;

public class Model {
    public int ox;
    public int oy;
    public int[] field;
    public int food;
    public static DatagramSocket socket;
    public static me.ippolitov.fit.snakes.SnakesProto.GameState state;
    public static me.ippolitov.fit.snakes.SnakesProto.GameConfig config;
    public static int msgNum = 0;

    public static void Init() {
        try {
            socket = new DatagramSocket(Controller.port);
            parse("conf.txt");
            GameListReceiver.start();
            GUI.init(config);
            GameProcess.start();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public static void continueGame() {
        GameListSender.running = true;
        GameProcess.continueGame(state);
    }

    public static void StartNew() {
        if (GameProcess.finished = true) {
            exit();
            new GameProcess();
            GameListSender.running = true;
            GameProcess.restart();
            Controller.playerId = 0;
            Controller.masterId = 0;
            Controller.role = MASTER;
        }
    }

    public static void parse(String filename) {
        me.ippolitov.fit.snakes.SnakesProto.GameConfig.Builder tmp = me.ippolitov.fit.snakes.SnakesProto.GameConfig.newBuilder();
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(filename));
            String line = reader.readLine();
            while (line != null) {
                System.out.println(line);
                String key = line.split(" ")[0];
                String value = line.split(" ")[1];
                if (key.equals("width")) {
                    tmp.setWidth(Integer.parseInt(value));
                }
                if (key.equals("height")) {
                    tmp.setHeight(Integer.parseInt(value));
                }
                if (key.equals("food_static")) {
                    tmp.setFoodStatic(Integer.parseInt(value));
                }
                if (key.equals("food_per_player")) {
                    tmp.setFoodPerPlayer(Float.parseFloat(value));
                }
                if (key.equals("state_delay_ms")) {
                    tmp.setStateDelayMs(Integer.parseInt(value));
                }
                if (key.equals("dead_food_prob")) {
                    tmp.setDeadFoodProb(Float.parseFloat(value));
                }
                if (key.equals("ping_delay_ms")) {
                    tmp.setPingDelayMs(Integer.parseInt(value));
                }
                if (key.equals("node_timeout_ms")) {
                    tmp.setNodeTimeoutMs(Integer.parseInt(value));
                }
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        config = tmp.build();
    }

    public static void exit() {
        if (Controller.role == MASTER) {
            GameProcess.running = false;
            GameListSender.running = false;
            GameProcess.stop();
        } else {
            if (Controller.role != VIEWER){
                me.ippolitov.fit.snakes.SnakesProto.GameMessage.RoleChangeMsg.Builder msg = me.ippolitov.fit.snakes.SnakesProto.GameMessage.RoleChangeMsg.newBuilder();
                msg.setSenderRole(VIEWER);
                Model.sendRoleChange(msg.build(), Controller.masterId);
            }
        }

    }

    public synchronized static int getMsgId() {
        msgNum++;
        return msgNum;
    }

    public static void join(me.ippolitov.fit.snakes.SnakesProto.GameMessage gameMessage, Sender sender) {
        GameProcess.newPlayer(gameMessage, sender);
    }

    public static void error(me.ippolitov.fit.snakes.SnakesProto.GameMessage gameMessage,
                             me.ippolitov.fit.snakes.SnakesProto.GamePlayer player) {
        GUI.error(gameMessage.getError().getErrorMessage());
        synchronized (NetworkWriter.resend) {
            Iterator<MessageCustom> iter = NetworkWriter.resend.iterator();
            while (iter.hasNext()) {
                if (iter.next().gm.getMsgSeq() == gameMessage.getMsgSeq()) {
                    Controller.playerId = gameMessage.getReceiverId();
                    Controller.masterId = gameMessage.getSenderId();
                    iter.next().branches.remove(player);
                }
            }
        }
    }
    public static void setState(me.ippolitov.fit.snakes.SnakesProto.GameState state1) {
        state = state1;
        GUI.repaint(state, GameListReceiver.table);
    }

    public static void setState(me.ippolitov.fit.snakes.SnakesProto.GameState state1, Sender sender) {
        state = state1;
        me.ippolitov.fit.snakes.SnakesProto.GameState.Builder newState = me.ippolitov.fit.snakes.SnakesProto.GameState.newBuilder(state1);
        me.ippolitov.fit.snakes.SnakesProto.GamePlayers.Builder players = me.ippolitov.fit.snakes.SnakesProto.GamePlayers.newBuilder(state.getPlayers());
        int masterId = Controller.findRole(MASTER);
        int masterIndex = Controller.findRoleIndex(MASTER);
        Controller.masterId = masterId;
        Controller.role = Controller.getRole(Controller.playerId);
        me.ippolitov.fit.snakes.SnakesProto.GamePlayer.Builder master = me.ippolitov.fit.snakes.SnakesProto.GamePlayer.newBuilder(players.getPlayers(masterIndex));
        master.setIpAddress(sender.ip);
        master.setPort(sender.port);
        players.setPlayers(masterIndex, master.build());
        System.out.println("new master player = " + master.build());
        newState.setPlayers(players.build());
        state = newState.build();
        GUI.repaint(state, GameListReceiver.table);
    }

    public static void setDeputy(int deputyId) {
        GameProcess.changeState(deputyId, DEPUTY);
        me.ippolitov.fit.snakes.SnakesProto.GameMessage.RoleChangeMsg.Builder msg = me.ippolitov.fit.snakes.SnakesProto.GameMessage.RoleChangeMsg.newBuilder();
        msg.setSenderRole(MASTER);
        msg.setReceiverRole(DEPUTY);
        sendRoleChange(msg.build(), deputyId);
    }

    public static void sendError(me.ippolitov.fit.snakes.SnakesProto.GameMessage gameMessage, Sender sender, String erMsg) {
        me.ippolitov.fit.snakes.SnakesProto.GameMessage.Builder message = me.ippolitov.fit.snakes.SnakesProto.GameMessage.newBuilder();
        me.ippolitov.fit.snakes.SnakesProto.GameMessage.ErrorMsg.Builder error = me.ippolitov.fit.snakes.SnakesProto.GameMessage.ErrorMsg.newBuilder();
        error.setErrorMessage(erMsg);
        message.setError(error);
        message.setMsgSeq(gameMessage.getMsgSeq());
        me.ippolitov.fit.snakes.SnakesProto.GamePlayer.Builder p = me.ippolitov.fit.snakes.SnakesProto.GamePlayer.newBuilder();
        p.setId(0);
        p.setName("");
        p.setIpAddress(sender.ip);
        p.setPort(sender.port);
        p.setRole(me.ippolitov.fit.snakes.SnakesProto.NodeRole.NORMAL);
        p.setScore(0);
        NetworkWriter.sendError(message.build(), p.build());
    }

    public static void sendJoin(me.ippolitov.fit.snakes.SnakesProto.GameMessage gameMessage, Sender sender){
        me.ippolitov.fit.snakes.SnakesProto.GameMessage.Builder message = me.ippolitov.fit.snakes.SnakesProto.GameMessage.newBuilder();
        me.ippolitov.fit.snakes.SnakesProto.GameMessage.JoinMsg.Builder join = me.ippolitov.fit.snakes.SnakesProto.GameMessage.JoinMsg.newBuilder();
        join.setName(Controller.name);
        message.setJoin(join);
        message.setMsgSeq(getMsgId());
        me.ippolitov.fit.snakes.SnakesProto.GamePlayer.Builder p = me.ippolitov.fit.snakes.SnakesProto.GamePlayer.newBuilder();
        p.setId(0);
        p.setName("");
        p.setIpAddress(sender.ip);
        p.setPort(sender.port);
        p.setRole(me.ippolitov.fit.snakes.SnakesProto.NodeRole.MASTER);
        p.setScore(0);
        NetworkWriter.sendError(message.build(), p.build());
    }

    public static void sendSteer(me.ippolitov.fit.snakes.SnakesProto.Direction direction) {
        me.ippolitov.fit.snakes.SnakesProto.GameMessage.Builder gameMessage = me.ippolitov.fit.snakes.SnakesProto.GameMessage.newBuilder();
        me.ippolitov.fit.snakes.SnakesProto.GameMessage.SteerMsg.Builder steer = me.ippolitov.fit.snakes.SnakesProto.GameMessage.SteerMsg.newBuilder();
        steer.setDirection(direction);
        gameMessage.setSteer(steer.build());
        gameMessage.setReceiverId(Controller.masterId);
        gameMessage.setMsgSeq(getMsgId());
        NetworkWriter.queue.add(gameMessage.build());
        System.out.println(direction);
    }

    public static void sendState(me.ippolitov.fit.snakes.SnakesProto.GameState state) {
        me.ippolitov.fit.snakes.SnakesProto.GameMessage.Builder gm = me.ippolitov.fit.snakes.SnakesProto.GameMessage.newBuilder();
        me.ippolitov.fit.snakes.SnakesProto.GameMessage.StateMsg.Builder gm1 = me.ippolitov.fit.snakes.SnakesProto.GameMessage.StateMsg.newBuilder();
        gm1.setState(state);
        gm.setState(gm1.build());
        gm.setMsgSeq(getMsgId());
        NetworkWriter.queue.add(gm.build());
    }

    public static void showState(me.ippolitov.fit.snakes.SnakesProto.GameState state) {
        GUI.repaint(state, GameListReceiver.table);
    }

    public static void steer(me.ippolitov.fit.snakes.SnakesProto.Direction direction, int playerId) {
        GameProcess.setSteer(direction, playerId);
    }

    public static void sendAck(me.ippolitov.fit.snakes.SnakesProto.GameMessage gameMessage, int receiverId){
        if (Controller.senderList.size() == 0) {
            me.ippolitov.fit.snakes.SnakesProto.GameMessage.Builder gm = me.ippolitov.fit.snakes.SnakesProto.GameMessage.newBuilder();
            me.ippolitov.fit.snakes.SnakesProto.GameMessage.AckMsg.Builder ack = me.ippolitov.fit.snakes.SnakesProto.GameMessage.AckMsg.newBuilder();
            gm.setAck(ack.build());
            gm.setReceiverId(receiverId);
            gm.setMsgSeq(gameMessage.getMsgSeq());
            NetworkWriter.queue.add(gm.build());
        } else {
            me.ippolitov.fit.snakes.SnakesProto.GameMessage.Builder gm = me.ippolitov.fit.snakes.SnakesProto.GameMessage.newBuilder();
            me.ippolitov.fit.snakes.SnakesProto.GameMessage.AckMsg.Builder ack = me.ippolitov.fit.snakes.SnakesProto.GameMessage.AckMsg.newBuilder();
            gm.setAck(ack.build());
            gm.setMsgSeq(gameMessage.getMsgSeq());
            me.ippolitov.fit.snakes.SnakesProto.GamePlayer.Builder p = me.ippolitov.fit.snakes.SnakesProto.GamePlayer.newBuilder();
            p.setId(0);
            p.setName("");
            p.setIpAddress(Controller.senderList.get(receiverId).ip);
            p.setPort(Controller.senderList.get(receiverId).port);
            p.setRole(me.ippolitov.fit.snakes.SnakesProto.NodeRole.MASTER);
            p.setScore(0);
            NetworkWriter.sendError(gm.build(), p.build());
        }
    }

    public static void sendRoleChange(me.ippolitov.fit.snakes.SnakesProto.GameMessage.RoleChangeMsg gameMessage, int id) {
        if (id == -1) {
            me.ippolitov.fit.snakes.SnakesProto.GameMessage.Builder gm = me.ippolitov.fit.snakes.SnakesProto.GameMessage.newBuilder();
            gm.setRoleChange(gameMessage);
            gm.setSenderId(Controller.playerId);
            gm.setMsgSeq(getMsgId());
            NetworkWriter.queue.add(gm.build());
        } else {
            me.ippolitov.fit.snakes.SnakesProto.GameMessage.Builder gm = me.ippolitov.fit.snakes.SnakesProto.GameMessage.newBuilder();
            gm.setRoleChange(gameMessage);
            gm.setMsgSeq(getMsgId());
            gm.setSenderId(Controller.playerId);
            gm.setReceiverId(id);
            NetworkWriter.queue.add(gm.build());
        }
    }

    public static void getAck(me.ippolitov.fit.snakes.SnakesProto.GameMessage gameMessage, me.ippolitov.fit.snakes.SnakesProto.GamePlayer player) {
        synchronized (NetworkWriter.resend) {
            Iterator<MessageCustom> iter = NetworkWriter.resend.iterator();
            while (iter.hasNext()) {
                MessageCustom messageCustom = iter.next();
                if (messageCustom.gm.getMsgSeq() == gameMessage.getMsgSeq()) {
                    if (gameMessage.getTypeCase().equals(me.ippolitov.fit.snakes.SnakesProto.GameMessage.TypeCase.JOIN)) {
                        Controller.playerId = gameMessage.getReceiverId();
                        Controller.masterId = gameMessage.getSenderId();
                    }
                    messageCustom.branches.remove(player);
                }
            }
        }
    }

    public static void makeViewer(int id) {
        if (id != Controller.playerId) {
            GameProcess.changeState(id, VIEWER);
            GameProcess.makeZombie(id);
            me.ippolitov.fit.snakes.SnakesProto.GameMessage.RoleChangeMsg.Builder msg = me.ippolitov.fit.snakes.SnakesProto.GameMessage.RoleChangeMsg.newBuilder();
            msg.setReceiverRole(VIEWER);
            msg.setSenderRole(MASTER);
            sendRoleChange(msg.build(), id);
        }
    }

    public static void becomeViewer(int playerId){
        Controller.role = VIEWER;
    }

    public static void disconnect(int id) {
        if (Controller.role.equals(MASTER)) {
            if (Controller.getPlayer(id).getRole() != VIEWER) {
                GameProcess.makeZombie(id);
            }
            GameProcess.deletePlayer(id);
        } else {
            GUI.error("connection lost");
        }
    }
}
