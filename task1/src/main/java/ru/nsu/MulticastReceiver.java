package ru.nsu;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.UUID;

public class MulticastReceiver extends Thread{
    private String group;

    private static final int PORT = 8000;
    private static final int LENGTH = 16;
    private static final int MAX_ITER = 10000;
    private static final int TIMEOUT = 100;

    public MulticastSocket multicastSocket;

    public Checker checker;

    public MulticastReceiver(String group, Checker checker) throws IOException {
        this.group = group;
        this.checker = checker;

        multicastSocket = new MulticastSocket(PORT);
        multicastSocket.setSoTimeout(TIMEOUT);
        multicastSocket.joinGroup(InetAddress.getByName(group));
    }

    @Override
    public void run() {
        while (true) {
            byte[] receiveBuf = new byte[LENGTH];
            DatagramPacket datagramPacketReceive = new DatagramPacket(
                    receiveBuf,
                    receiveBuf.length
            );
            try {
                multicastSocket.receive(datagramPacketReceive);
            } catch (IOException e) {
                continue;
            }
            receiveBuf = datagramPacketReceive.getData();
            UUID uuidMulticastMessageReceive = UuidHelper.getUUIDFromBytes(receiveBuf);
            checker.addElemToMap(uuidMulticastMessageReceive, System.currentTimeMillis());
            checker.checkPackets(MulticastSender.uuidMulticastMessageSend, uuidMulticastMessageReceive);
        }

    }
}
