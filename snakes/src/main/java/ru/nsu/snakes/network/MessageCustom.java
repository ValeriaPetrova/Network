package ru.nsu.snakes.network;

import me.ippolitov.fit.snakes.SnakesProto;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class MessageCustom {
    public me.ippolitov.fit.snakes.SnakesProto.GameMessage gm;
    public LocalTime sendtime;
    public LocalTime origtime;
    public List<SnakesProto.GamePlayer> branches = new ArrayList<>();
}

