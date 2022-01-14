package ru.nsu;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nsu.connection.Handler;
import ru.nsu.connection.ServerHandler;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Set;

public class Proxy implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger("APP");

    private final static int TIMEOUT = 10000;

    private final Selector selector = Selector.open();
    private final ServerHandler server;

    public Proxy(int port) throws IOException {
        server = new ServerHandler(port, selector);
    }

    public void start() {
        try {
            while (true) {
                int readyChannels = 0;

                try {
                    readyChannels = selector.select(TIMEOUT);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                if (readyChannels == 0) {
                    continue;
                }
                Set<SelectionKey> modified = selector.selectedKeys();
                for (SelectionKey selected : modified) {
                    Handler key = (Handler) selected.attachment();
                    key.handle(selected);
                }
                modified.clear();
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            try {
                close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void close() throws Exception {
        selector.close();
        server.close();
        server.closeDNS();
    }
}
