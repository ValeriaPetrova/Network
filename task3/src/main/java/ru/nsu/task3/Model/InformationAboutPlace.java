package ru.nsu.task3.Model;

import lombok.Getter;

public class InformationAboutPlace {
    @Getter
    public String name;
    @Getter
    public String xid;

    public InformationAboutPlace(String name, String xid) {
        this.name = name;
        this.xid = xid;
    }

    @Override
    public String toString() {
        return String.format("Place: %s", name);
    }
}
