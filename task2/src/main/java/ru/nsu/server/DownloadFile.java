package ru.nsu.server;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;

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

    private final static int NOT_FOUND = -1;
    private final static String DIRECTORY = "uploads";
    private final static int BUF_SIZE = 1024;
    private final static long TIMEOUT = 3000;

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

    private void speedCalculation (long read, long readJustNow, long startTime, long iterTime) {
        long currentTime = System.currentTimeMillis();
        double speed = (double) read / (currentTime - startTime) * 1000;
        double instantSpeed = (double) readJustNow / (currentTime - iterTime) * 1000;
        String speedOutput = String.format("%-15s", socket.getInetAddress()) +
                String.format("%8.2f  B/s", speed) + String.format("%8.2f  B/s ", instantSpeed) +
                String.format("%s", file.getName());
        logger.info(speedOutput);
    }

    public void receiveData(long length, FileOutputStream fileOutputStream) throws IOException {
        byte[] buf = new byte[BUF_SIZE];
        long read = 0, readThisSession = 0;
        long startTime = System.currentTimeMillis();
        long prevSessionTime = System.currentTimeMillis() - 1000;
        while (read < length) {
            speedCalculation(read, readThisSession, startTime, prevSessionTime);
            prevSessionTime = System.currentTimeMillis();
            readThisSession = 0;

            long currentStartTime = System.currentTimeMillis();
            long currentFinishTime = currentStartTime;
            do{
                socket.setSoTimeout((int) (TIMEOUT - (currentFinishTime - currentStartTime)));
                long readNow;
                if (length - read >= BUF_SIZE) {
                    readNow = inputStream.read(buf);
                } else {
                    readNow = inputStream.read(buf, 0, (int)(length - read));
                }
                fileOutputStream.write(buf, 0, (int)readNow);
                readThisSession += readNow;
                read += readNow;
                currentFinishTime = System.currentTimeMillis();
            } while ((currentFinishTime - currentStartTime < TIMEOUT) && (read < length));
        }
        speedCalculation(length, 0, 0, 0);
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
