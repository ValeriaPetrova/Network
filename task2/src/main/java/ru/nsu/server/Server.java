package ru.nsu.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Server {
    private static final Logger logger = LoggerFactory.getLogger("SERVER");

    private static int PORT;
    private final ExecutorService threadPool = Executors.newCachedThreadPool();

    public Server(int port) throws IOException {
        PORT = port;
        ServerSocket serverSocket = new ServerSocket(PORT);

        logger.info("Successfully connected to " + InetAddress.getLocalHost().getHostAddress() + " " + serverSocket.getLocalPort());

        while (!serverSocket.isClosed()) {
            Socket newConnection = serverSocket.accept();
            DownloadFile downloadFile = new DownloadFile(newConnection);
            threadPool.submit(downloadFile);
        }
    }
}
