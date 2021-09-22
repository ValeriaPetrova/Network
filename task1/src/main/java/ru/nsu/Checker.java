package ru.nsu;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.UUID;

public class Checker {
    private static String group;

    private static final int port = 8000;
    private static final String multicastMessage = "Hello world!";
    private static final int length = 16;
    private static final int bufSize = 2 * length;
    private static final int maxIter = 10000;

    Checker(String group) {
        Checker.group = group;
    }

    public void run() throws IOException {
        UUID uuidMulticastMessageSend = UUID.nameUUIDFromBytes(multicastMessage.getBytes());
        byte[] firstSend = UuidHelper.getBytesFromUUID(uuidMulticastMessageSend);
        UUID uuidProgramSend = UUID.randomUUID();
        byte[] secondSend = UuidHelper.getBytesFromUUID(uuidProgramSend);

        byte[] bufSender = new byte[bufSize];
        System.arraycopy(firstSend, 0, bufSender, 0, firstSend.length);
        System.arraycopy(secondSend, 0, bufSender, firstSend.length, secondSend.length);

        MulticastSocket multicastSocket = new MulticastSocket(port);
        multicastSocket.joinGroup(InetAddress.getByName(group));

        DatagramPacket datagramPacketSend = new DatagramPacket(
                bufSender,
                bufSender.length,
                InetAddress.getByName(group),
                port
        );

        byte[] bufReceiver = new byte[bufSize];
        DatagramPacket datagramPacketReceive = new DatagramPacket(
                bufReceiver,
                bufReceiver.length
        );

        for (int iter = 0; iter < maxIter; iter++) {
            multicastSocket.send(datagramPacketSend);
            multicastSocket.receive(datagramPacketReceive);

            bufReceiver = datagramPacketReceive.getData();
            byte[] firstReceive = new byte[length];
            byte[] secondReceive = new byte[length];
            System.arraycopy(bufReceiver, 0, firstReceive, 0, firstReceive.length);
            System.arraycopy(bufReceiver, firstReceive.length, secondReceive, 0, secondReceive.length);

            UUID uuidMulticastMessageReceive = UuidHelper.getUUIDFromBytes(firstReceive);
            UUID uuidProgramReceive = UuidHelper.getUUIDFromBytes(secondReceive);

            if (uuidMulticastMessageSend.equals(uuidMulticastMessageReceive) && !uuidProgramSend.equals(uuidProgramReceive)) {
                System.out.println("------Copy found------");
            } else {
                System.out.println("Copy not found");
            }
        }
        multicastSocket.leaveGroup(InetAddress.getByName(group));
        multicastSocket.close();
    }
}
