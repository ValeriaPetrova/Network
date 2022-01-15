package ru.nsu.snakes.network;

import ru.nsu.snakes.network.Sender;

public class GameListMessage {
    public me.ippolitov.fit.snakes.SnakesProto.GameMessage.AnnouncementMsg announce;
    public Sender sender;
}
