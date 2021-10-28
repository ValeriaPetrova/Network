package ru.nsu.client;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Client {
    private static final Logger logger = LoggerFactory.getLogger("CLIENT");

    private final static int BUZ_SIZE = 1024;

    private String filePath;
    private InetAddress serverIP;
    private int serverPort;

    private Socket socket;
    private File file;
    private FileInputStream fileInputStream;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;

    public Client(String filePath, InetAddress serverIP, int serverPort) throws IOException {
        this.filePath = filePath;
        this.serverIP = serverIP;
        this.serverPort = serverPort;

        file = new File(filePath);
        socket = new Socket(serverIP, serverPort);
        fileInputStream = new FileInputStream(file);
        logger.info("Successfully connected to " + socket.getInetAddress() + " " + socket.getPort());
        inputStream = new DataInputStream(socket.getInputStream());
        outputStream = new DataOutputStream(socket.getOutputStream());
    }

    public void sendFileName() throws IOException {
        String fileName = file.getName();
        byte[] fileNameBytes = fileName.getBytes(StandardCharsets.UTF_8);
        outputStream.writeInt(fileName.length());
        outputStream.write(fileNameBytes);
    }

    public void sendFileSize() throws IOException {
        outputStream.writeLong(file.length());
    }

    public void sendData() throws IOException {
        byte[] buf = new byte[BUZ_SIZE];
        int length;
        while ((length = fileInputStream.read(buf, 0, BUZ_SIZE)) > 0) {
            outputStream.write(buf, 0, length);
        }
    }

    public String receiveLastMessage() throws IOException {
        int length = inputStream.readInt();
        byte[] message = inputStream.readNBytes(length);
        String text = new String(message, StandardCharsets.UTF_8);
        return text;
    }

}
