package ru.nsu;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.util.UUID;

public class Checker {
    private String group;

    private static final int PORT = 8000;
    private static final String MULTICAST_MESSAGE = "Hello world!";
    private static final int LENGTH = 16;
    private static final int BUF_SIZE = 2 * LENGTH;
    private static final int MAX_ITER = 10000;
    private static final int TIMEOUT = 1000;

    public Checker(String group) {
        this.group = group;
    }

    public void run() throws IOException {
        UUID uuidMulticastMessageSend = UUID.nameUUIDFromBytes(MULTICAST_MESSAGE.getBytes());
        byte[] firstSend = UuidHelper.getBytesFromUUID(uuidMulticastMessageSend);
        UUID uuidProgramSend = UUID.randomUUID();
        byte[] secondSend = UuidHelper.getBytesFromUUID(uuidProgramSend);

        byte[] bufSender = new byte[BUF_SIZE];
        System.arraycopy(firstSend, 0, bufSender, 0, firstSend.length);
        System.arraycopy(secondSend, 0, bufSender, firstSend.length, secondSend.length);

        MulticastSocket multicastSocket = new MulticastSocket(PORT);
        multicastSocket.setSoTimeout(TIMEOUT);
        multicastSocket.joinGroup(InetAddress.getByName(group));

        DatagramPacket datagramPacketSend = new DatagramPacket(
                bufSender,
                bufSender.length,
                InetAddress.getByName(group),
                PORT
        );

        byte[] bufReceiver = new byte[BUF_SIZE];
        DatagramPacket datagramPacketReceive = new DatagramPacket(
                bufReceiver,
                bufReceiver.length
        );

        for (int iter = 0; iter < MAX_ITER; iter++) {
            multicastSocket.send(datagramPacketSend);
            multicastSocket.receive(datagramPacketReceive);
            bufReceiver = datagramPacketReceive.getData();
            byte[] firstReceive = new byte[LENGTH];
            byte[] secondReceive = new byte[LENGTH];
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
