package ru.nsu.messeges;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class Request extends ToolsMessage {
    private final static int CORRECT_SIZE = 5;
    private final static int CORRECT_LENGTH_IPv4 = 10;
    private final static int CORRECT_LENGTH_IPv6 = 22;
    private final static int PART_OF_DOMAIN_NAME_LEN = 7;
    private final static int ERROR = -1;

    public Request(ByteBuffer buffer) {
        super(new byte[buffer.limit()]);
        buffer.get(this.data);
        if (!Request.isCorrect(this.data)) {
            throw new IllegalArgumentException();
        }
    }

    private static boolean isCorrect(byte[] data) {
        if (data.length < CORRECT_SIZE) {
            return false;
        }
        if (data[2] != 0x00) {
            return false;
        }
        switch (data[3]) {
            case IPv4:
                if (data.length != CORRECT_LENGTH_IPv4) {
                    return false;
                }
                break;
            case IPv6:
                if (data.length != CORRECT_LENGTH_IPv6) {
                    return false;
                }
                break;
            case DOMAIN_NAME:
                if (data.length != PART_OF_DOMAIN_NAME_LEN + data[4]) {
                    return false;
                }
                break;
        }
        return true;
    }

    public static boolean isCorrectSizeOfMessage(ByteBuffer data) {
        if (data.position() < CORRECT_SIZE) {
            return false;
        }
        switch (data.get(3)) {
            case IPv4:
                if (data.position() != CORRECT_LENGTH_IPv4) {
                    return false;
                }
                break;
            case IPv6:
                if (data.position() != CORRECT_LENGTH_IPv6) {
                    return false;
                }
                break;
            case DOMAIN_NAME:
                if (data.position() != PART_OF_DOMAIN_NAME_LEN + data.get(4)) {
                    return false;
                }
                break;
        }
        return true;
    }

    public boolean isCommand(byte command) {
        return command == data[1];
    }

    public byte getAddressType() {
        return data[3];
    }

    public byte[] getDestAddress() {
        switch (this.getAddressType()) {
            case IPv4:
                return Arrays.copyOfRange(data, 4, 8);
            case DOMAIN_NAME:
                int length = data[4];
                return Arrays.copyOfRange(data, 5, 5 + length);
            case IPv6:
                return Arrays.copyOfRange(data, 4, 20);
        }
        return null;
    }

    public short getDestPort() {
        switch (data[3]) {
            case IPv4:
                return ByteBuffer.wrap(data, 8, 2).getShort();
            case DOMAIN_NAME:
                int length = data[4];
                return ByteBuffer.wrap(data, 5 + length, 2).getShort();
            case IPv6:
                return ByteBuffer.wrap(data, 20, 2).getShort();
        }

        return ERROR;
    }

    public byte[] getBytes() {
        return data;
    }
}
