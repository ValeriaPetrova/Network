package ru.nsu;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Main {
    private static final int CORRECT_ARGS_NUM = 1;

    private static final Logger logger = LoggerFactory.getLogger("APP");

    public static void main(String[] args) throws IOException {
        if (args.length != CORRECT_ARGS_NUM) {
            logger.error("Invalid argument.");
        }
        int port = Integer.parseInt(args[0]);
        Proxy proxy = new Proxy(port);
        proxy.start();
    }
}
