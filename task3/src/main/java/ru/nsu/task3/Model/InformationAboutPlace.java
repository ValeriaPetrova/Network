package ru.nsu.task3.Model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class InformationAboutPlace {
    private final String name;
    private final String xid;

    @Override
    public String toString() {
        return String.format("Place: %s", name);
    }
}
