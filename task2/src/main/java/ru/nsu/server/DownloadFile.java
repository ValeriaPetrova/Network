package ru.nsu.server;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DownloadFile implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger("SERVER");

    private Socket socket;
    private String fileName;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    private File file;
    private long readOneIter = 0;
    private long readAll = 0;
    private long time = 0;

    private final static int NOT_FOUND = -1;
    private final static String DIRECTORY = "uploads";
    private final static int BUF_SIZE = 1024;
    private final static long TIMEOUT = 3000;
    private final static int CORE_POOL_SIZE = 1;
    private final static int INITIAL_DELAY = 2;
    private final static int PERIOD = 3;
    private final static int KILOBYTES = 1024;

    public DownloadFile(Socket socket) {
        this.socket = socket;
    }

    public void receiveFileName() throws IOException {
        int length = inputStream.readInt();
        byte[] fileNameBytes = new byte[length];
        inputStream.readFully(fileNameBytes, 0, length);
        fileName = new String(fileNameBytes, StandardCharsets.UTF_8);
    }

    public long receiveFileSize() throws IOException {
        return inputStream.readLong();
    }

    public File createFile() {
        int dotPosition = fileName.lastIndexOf(".");
        String name, extension;
        if (dotPosition != NOT_FOUND) {
            name = fileName.substring(0, dotPosition);
            extension = fileName.substring(dotPosition);
        } else {
            name = fileName;
            extension = "";
        }

        int countOfFiles = 0;
        while (true) {
            String newFileName = name + extension;
            if (0 != countOfFiles) {
                newFileName = name + "(" + countOfFiles + ")" + extension;
            }
            File file = new File(DIRECTORY + "/" + newFileName);
            if (!file.exists()) {
                return file;
            }
            countOfFiles++;
        }
    }

    private String beautifulSpeed(double speed) {
        if (speed <= KILOBYTES) {
            return String.format("%8.2f  B/s ", speed);
        } else if (speed <= KILOBYTES * KILOBYTES) {
            return String.format("%8.2f KB/s ", speed / KILOBYTES);
        } else if (speed <= KILOBYTES * KILOBYTES * KILOBYTES) {
            return String.format("%8.2f MB/s ", speed / KILOBYTES / KILOBYTES);
        } else {
            return String.format("%8.2f GB/s ", speed / KILOBYTES / KILOBYTES / KILOBYTES);
        }
    }

    private void speedCalculation () {
        if (0 == readOneIter) {
            return;
        }
        double speed = (double) readOneIter / PERIOD;
        readAll += readOneIter;
        time += PERIOD;
        double instantSpeed = (double) readAll / time;
        String speedOutput = String.format("%-15s ", socket.getInetAddress()) +
                beautifulSpeed(speed) +
                beautifulSpeed(instantSpeed) +
                String.format("%s", file.getName());
        logger.info(speedOutput);
        readOneIter = 0;
    }

    public void receiveData(long length, FileOutputStream fileOutputStream) throws IOException {
        var scheduledThreadPool = Executors.newScheduledThreadPool(CORE_POOL_SIZE);

        byte[] buf = new byte[BUF_SIZE];
        long read = 0, readThisSession = 0;

        scheduledThreadPool.scheduleAtFixedRate(this::speedCalculation, INITIAL_DELAY, PERIOD, TimeUnit.SECONDS);
        while (read < length) {
            if (length - read >= BUF_SIZE) {
                readThisSession = inputStream.read(buf);
            } else {
                readThisSession = inputStream.read(buf, 0, (int)(length - read));
            }

            fileOutputStream.write(buf, 0, (int)readThisSession);

            read += readThisSession;
            readOneIter += readThisSession;
        }
    }

    public void sendLastMessage(String text) throws IOException {
        byte[] message = text.getBytes(StandardCharsets.UTF_8);
        outputStream.writeInt(message.length);
        outputStream.write(message);
    }

    @SneakyThrows
    @Override
    public void run() {
        inputStream = new DataInputStream(socket.getInputStream());
        outputStream = new DataOutputStream(socket.getOutputStream());

        receiveFileName();
        file = createFile();

        try (FileOutputStream fileOutputStream = new FileOutputStream(file)){
            long size = receiveFileSize();
            receiveData(size, fileOutputStream);
        } catch (IOException e) {
            e.printStackTrace();
            sendLastMessage("Failed");
            return;
        }
        sendLastMessage("Success");
        socket.close();
    }
}