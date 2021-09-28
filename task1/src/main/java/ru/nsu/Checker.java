package ru.nsu;

import java.util.HashMap;
import java.util.UUID;

public class Checker {
    public HashMap<UUID, Long> copies = new HashMap<>();

    public void addElemToMap(UUID uuid, long value) {
        if (copies.containsKey(uuid)) {
            copies.replace(
                    uuid,
                    copies.get(uuid),
                    value
            );
            return;
        }
        copies.put(uuid, value);
    }

    public void checkPackets(UUID uuidMulticastMessageSend, UUID uuidMulticastMessageReceive) {
        if (uuidMulticastMessageSend.equals(uuidMulticastMessageReceive)) {
            System.out.println("Found myself");
        } else {
            copies.put(uuidMulticastMessageReceive, System.currentTimeMillis());
            System.out.println("Copy found: " + uuidMulticastMessageReceive.toString());
        }
    }

}


