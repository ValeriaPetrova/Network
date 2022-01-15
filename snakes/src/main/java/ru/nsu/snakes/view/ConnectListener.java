package ru.nsu.snakes.view;

import ru.nsu.snakes.Controller;
import ru.nsu.snakes.network.Sender;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class ConnectListener implements ActionListener {
    private final Sender sender;

    ConnectListener(Sender s){
        sender = s;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Controller.connect(sender);
    }
}
