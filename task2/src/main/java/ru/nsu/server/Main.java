package ru.nsu.server;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger("SERVER");

    private final static int CORRECT_COUNT_OF_ARGUMENTS = 1;

    public static void main(String[] args) throws IOException {
        if(args.length < CORRECT_COUNT_OF_ARGUMENTS) {
            logger.error("Error: empty input. You should enter port");
            System.exit(1);
        }

        int port = Integer.parseInt(args[0]);
        Server server = new Server(port);
    }
}
