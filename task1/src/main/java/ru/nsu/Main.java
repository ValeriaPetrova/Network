package ru.nsu;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        String group = args[0];
        Checker checker= new Checker(group);
        checker.run();
    }
}
