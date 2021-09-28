package ru.nsu;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        if (args.length <= 0) {
            System.out.println("Error: empty input. You should enter the group ID");
            System.exit(1);
        }
        Checker checker = new Checker();
        MulticastSender multicastSender = new MulticastSender(args[0], checker);
        multicastSender.start();
        MulticastReceiver multicastReceiver = new MulticastReceiver(args[0], checker);
        multicastReceiver.start();
    }
}
