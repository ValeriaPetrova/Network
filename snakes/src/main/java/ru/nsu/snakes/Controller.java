package ru.nsu.snakes;

import me.ippolitov.fit.snakes.SnakesProto;
import ru.nsu.snakes.network.NetworkReader;
import ru.nsu.snakes.network.NetworkWriter;
import ru.nsu.snakes.network.Sender;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static me.ippolitov.fit.snakes.SnakesProto.NodeRole.MASTER;
import static me.ippolitov.fit.snakes.SnakesProto.NodeRole.VIEWER;

public class Controller {
    private final static int ERROR = -1;
    public static int playerId = 0;
    public static int masterId = 1;
    public static Sender masterSender;
    public static SnakesProto.NodeRole role = VIEWER;
    public static List<Sender> senderList = new ArrayList<>();

    public static String name;
    public static int port;

    public static void main(String[] args) {
        name = args[0];
        port = Integer.parseInt(args[1]);
        Model.Init();
        NetworkReader.start();
        NetworkWriter.start();
    }

    public static void setState(me.ippolitov.fit.snakes.SnakesProto.GameMessage gameMessage, Sender sender){
        Model.config = gameMessage.getState().getState().getConfig();
        if (senderList.size() > 0) {
            Controller.senderList.remove(0);
        }
        if (Model.state == null || Model.state.getStateOrder() <= gameMessage.getState().getState().getStateOrder()) {
            Model.setState(gameMessage.getState().getState(), sender);
        }
        Model.sendAck(gameMessage, getId(sender));
    }

    public static void connect(Sender sender) {
        exit();
        Model.state = null;
        if (Model.state == null) {
            System.out.println("setted state to null");
        }
        me.ippolitov.fit.snakes.SnakesProto.GameMessage.Builder gameMessage = me.ippolitov.fit.snakes.SnakesProto.GameMessage.newBuilder();
        me.ippolitov.fit.snakes.SnakesProto.GameMessage.JoinMsg.Builder joinMessage = me.ippolitov.fit.snakes.SnakesProto.GameMessage.JoinMsg.newBuilder();
        joinMessage.setName(Controller.name);
        gameMessage.setJoin(joinMessage.build());
        gameMessage.setMsgSeq(Model.getMsgId());
        if (senderList.size() >= 1) {
            senderList.remove(0);
        }
        if (senderList.add(sender)) {
            System.out.println("wait ack from ip = " + sender.ip + ":" + sender.port);
        }
        Model.sendJoin(gameMessage.build(), sender);
    }

    public static void steer(me.ippolitov.fit.snakes.SnakesProto.Direction direction) {
        System.out.println("steer " + direction + "role = " + role);
        if (role.equals(MASTER)) {
            System.out.println("steer " + direction);
            Model.steer(direction, playerId);
        }
        else Model.sendSteer(direction);
    }

    public static void steer(me.ippolitov.fit.snakes.SnakesProto.GameMessage gameMessage, Sender sender) {
        me.ippolitov.fit.snakes.SnakesProto.Direction dir = gameMessage.getSteer().getDirection();
        System.out.println("steer " + dir);
        int id = getId(sender);
        if (id != ERROR) {
            Model.steer(dir, id);
            Model.sendAck(gameMessage, id);
        }
    }

    public static void error(me.ippolitov.fit.snakes.SnakesProto.GameMessage gameMessage, Sender sender) {
        Model.error(gameMessage, getPlayer(sender));
        Model.sendAck(gameMessage, getId(sender));
    }

    public static void ack(me.ippolitov.fit.snakes.SnakesProto.GameMessage gameMessage, Sender sender) {
        if (senderList.size() == 0) {
            Model.getAck(gameMessage, getPlayer(sender));
        }
        else {
            Sender sender1 = senderList.get(0);
            if (sender.equals(sender1)) {
                System.out.println("got ack from neededsender");
                Controller.masterSender = sender;
                Controller.playerId = gameMessage.getReceiverId();
                me.ippolitov.fit.snakes.SnakesProto.GamePlayer.Builder p = me.ippolitov.fit.snakes.SnakesProto.GamePlayer.newBuilder();
                p.setId(0);
                p.setName("");
                p.setIpAddress(sender.ip);
                p.setPort(sender.port);
                p.setRole(me.ippolitov.fit.snakes.SnakesProto.NodeRole.MASTER);
                p.setScore(0);
                Model.getAck(gameMessage, p.build());
            }
        }
    }

    public static void join(me.ippolitov.fit.snakes.SnakesProto.GameMessage gameMessage, Sender sender) {
        Model.join(gameMessage, sender);
    }

    public static void roleChange(me.ippolitov.fit.snakes.SnakesProto.GameMessage gameMessage, Sender sender) {
        if (gameMessage.getRoleChange().hasSenderRole()) {
            if (gameMessage.getRoleChange().getSenderRole().equals(me.ippolitov.fit.snakes.SnakesProto.NodeRole.VIEWER)) {
                if (gameMessage.getRoleChange().hasReceiverRole()) {
                    if (gameMessage.getRoleChange().getReceiverRole().equals(MASTER)) {
                        System.out.println("become master");
                        becomeMaster();
                    }
                } else {
                    System.out.println("make smn viewer");
                    Model.makeViewer(gameMessage.getSenderId());
                }
            }
            if (gameMessage.getRoleChange().getSenderRole().equals(MASTER)) {
                if (gameMessage.getRoleChange().hasReceiverRole()){
                    if (gameMessage.getRoleChange().getReceiverRole().equals(me.ippolitov.fit.snakes.SnakesProto.NodeRole.DEPUTY)) {
                        System.out.println("become deputy");
                        Controller.role = me.ippolitov.fit.snakes.SnakesProto.NodeRole.DEPUTY;
                    } else if (gameMessage.getRoleChange().getReceiverRole().equals(me.ippolitov.fit.snakes.SnakesProto.NodeRole.VIEWER)) {
                        System.out.println("become viewer");
                        Model.becomeViewer(Controller.playerId);
                    }
                } else {
                    System.out.println("new master with id =" + gameMessage.getSenderId());
                    masterId = gameMessage.getSenderId();
                }
            }
        } else {
            if (gameMessage.getRoleChange().hasReceiverRole()) {
                if (gameMessage.getRoleChange().getReceiverRole().equals(me.ippolitov.fit.snakes.SnakesProto.NodeRole.DEPUTY)) {
                    Controller.role = me.ippolitov.fit.snakes.SnakesProto.NodeRole.DEPUTY;
                } else if (gameMessage.getRoleChange().getReceiverRole().equals(me.ippolitov.fit.snakes.SnakesProto.NodeRole.VIEWER)) {
                    Model.becomeViewer(Controller.playerId);
                }
            }
        }
        Model.sendAck(gameMessage, getId(sender));
    }

    public static void pingAnswer(me.ippolitov.fit.snakes.SnakesProto.GameMessage gameMessage, Sender sender) {
        if (senderList.size() == 0) {
            Model.sendAck(gameMessage, getId(sender));
        } else {
            Model.sendAck(gameMessage, 0);
        }
    }

    public static void exit() {
        Model.exit();
    }

    public static void newgame() {
        Model.StartNew();
    }

    public static me.ippolitov.fit.snakes.SnakesProto.NodeRole getRole(int searchId) {
        Iterator<SnakesProto.GamePlayer> iter = Model.state.getPlayers().getPlayersList().iterator();
        while (iter.hasNext()) {
            SnakesProto.GamePlayer player = iter.next();
            if (player.getId() == searchId) {
                return player.getRole();
            }
        }
        return null;
    }

    public static int getId(Sender sender) {
        Iterator<SnakesProto.GamePlayer> iter = Model.state.getPlayers().getPlayersList().iterator();
        while (iter.hasNext()) {
            SnakesProto.GamePlayer player = iter.next();
            if ((player.getIpAddress().equals(sender.ip)) && (player.getPort() == sender.port)) {
                return player.getId();
            }
        }
        return ERROR;
    }

    public static SnakesProto.GamePlayer getPlayer(Sender sender) {
        Iterator<SnakesProto.GamePlayer> iter = Model.state.getPlayers().getPlayersList().iterator();
        while (iter.hasNext()) {
            SnakesProto.GamePlayer player = iter.next();
            if ((player.getIpAddress().equals(sender.ip)) && (player.getPort() == sender.port)) {
                return player;
            }
        }
        return null;
    }

    public static SnakesProto.GamePlayer getPlayer(int searchId) {
        Iterator<SnakesProto.GamePlayer> iter = Model.state.getPlayers().getPlayersList().iterator();
        while (iter.hasNext()) {
            SnakesProto.GamePlayer player = iter.next();
            if (player.getId() == searchId) {
                return player;
            }
        }
        return null;
    }

    public static void changeMaster() {
        Iterator<SnakesProto.GamePlayer> iter = Model.state.getPlayers().getPlayersList().iterator();
        while (iter.hasNext()) {
            SnakesProto.GamePlayer player = iter.next();
            if (player.getRole().equals(SnakesProto.NodeRole.DEPUTY)) {
                masterId = player.getId();
            }
        }
    }

    public static void findDeputy() {
        Iterator<SnakesProto.GamePlayer> iter = Model.state.getPlayers().getPlayersList().iterator();
        while (iter.hasNext()) {
            SnakesProto.GamePlayer player = iter.next();
            if (player.getRole().equals(SnakesProto.NodeRole.NORMAL)) {
                Model.setDeputy(player.getId());
                System.out.println("find deputy");
                break;
            }
        }
    }

    public static int findRole(SnakesProto.NodeRole role) {
        Iterator<SnakesProto.GamePlayer> iter = Model.state.getPlayers().getPlayersList().iterator();
        while (iter.hasNext()) {
            SnakesProto.GamePlayer player = iter.next();
            if (player.getRole().equals(role)) {
                return player.getId();
            }
        }
        return ERROR;
    }

    public static int findRoleIndex(SnakesProto.NodeRole role) {
        int ind = 0;
        Iterator<SnakesProto.GamePlayer> iter = Model.state.getPlayers().getPlayersList().iterator();
        while (iter.hasNext()) {
            SnakesProto.GamePlayer player = iter.next();
            if (player.getRole().equals(role)) {
                return ind;
            }
            ind++;
        }
        return ERROR;
    }

    public static int findIdIndex(int id) {
        int ind = 0;
        Iterator<SnakesProto.GamePlayer> iter = Model.state.getPlayers().getPlayersList().iterator();
        while (iter.hasNext()) {
            SnakesProto.GamePlayer player = iter.next();
            if (player.getId() == id) {
                return ind;
            }
            ind++;
        }
        return ERROR;
    }

    public static void becomeMaster() {
        System.out.println("become master called");
        Controller.role = MASTER;
        Controller.masterId = Controller.playerId;
        Model.continueGame();
        GameProcess.changeState(masterId, MASTER);
        findDeputy();
        SnakesProto.GameMessage.RoleChangeMsg.Builder msg = SnakesProto.GameMessage.RoleChangeMsg.newBuilder();
        msg.setSenderRole(MASTER);
        Model.sendRoleChange(msg.build(), -1);
    }
}
