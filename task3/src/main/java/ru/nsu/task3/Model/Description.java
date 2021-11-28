package ru.nsu.task3.Model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class Description {
    private final String title;
    private final String text;

    @Override
    public String toString() {
        return String.format("""
                %s
                %s
                """, title, text);
    }
}

