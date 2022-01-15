package ru.nsu.snakes.network;

import ru.nsu.snakes.Model;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.time.LocalTime;
import java.util.Arrays;

public class Network {

    private static final int SIZE = 64000;

    public static me.ippolitov.fit.snakes.SnakesProto.GameMessage receive(Sender sender) {
        try {
            byte[] recvBuf = new byte[SIZE];
            DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
            Model.socket.receive(packet);
            sender.ip = packet.getAddress().toString().split("/")[1];
            sender.port = packet.getPort();
            me.ippolitov.fit.snakes.SnakesProto.GameMessage msg = me.ippolitov.fit.snakes.SnakesProto.GameMessage.parseFrom(Arrays.copyOf(packet.getData(), packet.getLength()));
            System.out.println("received msg with id = " + msg.getMsgSeq() + " from ip = " + sender.ip + " , port = " + sender.port
                    + " of type = " + msg.getTypeCase());
            return msg;
        } catch (IOException e) {
            System.err.println("Exception:  " + e);
            e.printStackTrace();
        }
        return null;
    }

    public static void send(me.ippolitov.fit.snakes.SnakesProto.GameMessage msg,
                            me.ippolitov.fit.snakes.SnakesProto.GamePlayer receiver) {
        try {
            byte[] sendBuf = msg.toByteArray();
            DatagramPacket packet = new DatagramPacket(sendBuf, sendBuf.length, InetAddress.getByName(receiver.getIpAddress()), receiver.getPort());
            System.out.println("sends msg with id = " + msg.getMsgSeq() + " to addr = " + receiver.getIpAddress() +
                    " to port = " + receiver.getPort() + "of type = " + msg.getTypeCase());
            Model.socket.send(packet);
            NetworkWriter.lastSent.put(receiver, LocalTime.now());
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void send(me.ippolitov.fit.snakes.SnakesProto.GameMessage msg, Sender receiver) {
        try {
            byte[] sendBuf = msg.toByteArray();
            DatagramPacket packet = new DatagramPacket(sendBuf, sendBuf.length, InetAddress.getByName(receiver.ip), receiver.port);
            System.out.println("sends msg with id = " + msg.getMsgSeq() + " to addr = " + receiver.ip +
                    " to port = " + receiver.port + "of type = " + msg.getTypeCase());
            Model.socket.send(packet);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}

