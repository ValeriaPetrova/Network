package ru.nsu;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

public class Checker {
    private String group;

    private static final int PORT = 8000;
    private static final int LENGTH = 16;
    private static final int MAX_ITER = 10000;
    private static final int TIMEOUT = 1000;

    private HashMap<UUID, Long> copies = new HashMap<>();

    public Checker(String group) {
        this.group = group;
    }

    public void checkPackets(UUID uuidMulticastMessageSend, UUID uuidMulticastMessageReceive) {
        if (copies.containsKey(uuidMulticastMessageReceive)) {
            copies.replace(
                    uuidMulticastMessageReceive,
                    copies.get(uuidMulticastMessageReceive),
                    System.currentTimeMillis()
            );
        }

        if (uuidMulticastMessageSend.equals(uuidMulticastMessageReceive)) {
            System.out.println("Found myself");
        } else {
            copies.put(uuidMulticastMessageReceive, System.currentTimeMillis());
            System.out.println("Copy found: " + uuidMulticastMessageReceive.toString());
        }
    }

    public void run() throws IOException {
        UUID uuidMulticastMessageSend = UUID.randomUUID();
        byte[] sendBuf = UuidHelper.getBytesFromUUID(uuidMulticastMessageSend);

        MulticastSocket multicastSocket = new MulticastSocket(PORT);
        multicastSocket.setSoTimeout(TIMEOUT);
        multicastSocket.joinGroup(InetAddress.getByName(group));

        DatagramPacket datagramPacketSend = new DatagramPacket(
                sendBuf,
                sendBuf.length,
                InetAddress.getByName(group),
                PORT
        );

        byte[] receiveBuf = new byte[LENGTH];
        DatagramPacket datagramPacketReceive = new DatagramPacket(
                receiveBuf,
                receiveBuf.length
        );

        for (int iter = 0; iter < MAX_ITER; iter++) {
            multicastSocket.send(datagramPacketSend);

            try {
                multicastSocket.receive(datagramPacketReceive);
            } catch (SocketTimeoutException e) {
                continue;
            }

            receiveBuf = datagramPacketReceive.getData();
            UUID uuidMulticastMessageReceive = UuidHelper.getUUIDFromBytes(receiveBuf);

            checkPackets(uuidMulticastMessageSend, uuidMulticastMessageReceive);
        }
        multicastSocket.leaveGroup(InetAddress.getByName(group));
        multicastSocket.close();
        System.out.println("Number of clones found: " + copies.size());
    }
}


