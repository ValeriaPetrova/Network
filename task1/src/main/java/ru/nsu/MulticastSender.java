package ru.nsu;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.UUID;

public class MulticastSender extends Thread{
    private String group;

    private static final int PORT = 8000;
    private static final int LENGTH = 16;
    private static final int MAX_ITER = 100;
    private static final int TIMEOUT = 100;

    public MulticastSocket multicastSocket;
    public DatagramPacket datagramPacketSend;
    public byte[] sendBuf;

    public static  final UUID uuidMulticastMessageSend = UUID.randomUUID();

    public MulticastSender(String group) throws IOException {
        this.group = group;
        multicastSocket = new MulticastSocket(PORT);

        sendBuf = UuidHelper.getBytesFromUUID(uuidMulticastMessageSend);
        datagramPacketSend = new DatagramPacket(
                sendBuf,
                sendBuf.length,
                InetAddress.getByName(group),
                PORT
        );
    }

    @Override
    public void run() {
        for (int iter = 0; iter < MAX_ITER; iter++) {
            try {
                multicastSocket.send(datagramPacketSend);
            } catch (IOException e) {
               continue;
            }
        }
    }
}

