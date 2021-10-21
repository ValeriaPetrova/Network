package ru.nsu.client;

import java.io.IOException;
import java.net.InetAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger("CLIENT");

    private final static int CORRECT_COUNT_OF_ARGUMENTS = 3;

    public static void main(String[] args) throws IOException {
        if (args.length < CORRECT_COUNT_OF_ARGUMENTS) {
            logger.error("Error: incorrect input. You should enter file path, ip address and server port number");
            System.exit(1);
        }

        String filePath = args[0];
        InetAddress serverIP = InetAddress.getByName(args[1]);
        int serverPort = Integer.parseInt(args[2]);

        Client client = new Client(filePath, serverIP, serverPort);
        client.sendFileName();
        client.sendFileSize();
        client.sendData();
        logger.info(client.receiveLastMessage());
    }
}
