package ru.nsu.task3.Model;

import lombok.Getter;

public class Description {
    @Getter
    String title;
    @Getter
    String text;

    public Description(String title, String text) {
        this.title = title;
        this.text = text;
    }

    @Override
    public String toString() {
        return String.format("""
                %s
                %s
                """, title, text);
    }
}

