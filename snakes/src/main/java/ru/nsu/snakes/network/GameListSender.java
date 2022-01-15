package ru.nsu.snakes.network;

import ru.nsu.snakes.Model;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import static java.lang.Thread.sleep;

public class GameListSender implements Runnable {
    private static final int PORT = 9192;
    private static final String IP = "239.192.0.4";
    public static MulticastSocket socket;
    public static boolean running;

    public static void start() {
        Thread t = new Thread(new GameListSender());
        running = true;
        t.start();
    }

    public static void sendUDPMessage(me.ippolitov.fit.snakes.SnakesProto.GameMessage msg,
                                      String ipAddress, int port, MulticastSocket socket) throws IOException {
        try {
            InetAddress group = InetAddress.getByName(ipAddress);
            byte[] sendBuf = msg.toByteArray();
            DatagramPacket packet = new DatagramPacket(sendBuf, sendBuf.length, group, port);
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            socket = GameListReceiver.socket;
            me.ippolitov.fit.snakes.SnakesProto.GameMessage.Builder msg = me.ippolitov.fit.snakes.SnakesProto.GameMessage.newBuilder();
            me.ippolitov.fit.snakes.SnakesProto.GameMessage.AnnouncementMsg.Builder ann = me.ippolitov.fit.snakes.SnakesProto.GameMessage.AnnouncementMsg.newBuilder();
            ann.setConfig(Model.config);
            while (true) {
                ann.setPlayers(Model.state.getPlayers());
                msg.setAnnouncement(ann.build());
                msg.setMsgSeq(Model.getMsgId());
                if (running) {
                    sendUDPMessage(msg.build(), IP, PORT, socket);
                }
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

