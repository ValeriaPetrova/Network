package ru.nsu.snakes.network;

import me.ippolitov.fit.snakes.SnakesProto;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GameListReceiver implements Runnable {
    private static final int SIZE = 64000;
    private static final int PORT = 9192;
    private static final String IP = "239.192.0.4";
    public static MulticastSocket socket;
    public static ConcurrentHashMap<Sender, SnakesProto.GameMessage.AnnouncementMsg> table = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<Sender, LocalTime> clients = new ConcurrentHashMap<>();

    public static void start() {
        Thread thread = new Thread(new GameListReceiver());
        thread.start();
    }

    GameListReceiver() {}

    public void UpdateTable(SnakesProto.GameMessage.AnnouncementMsg msg, Sender sender) {
        if (clients.containsKey(sender)) {
            clients.replace(sender, LocalTime.now());
            table.replace(sender, msg);
        } else {
            clients.put(sender, LocalTime.now());
            table.put(sender, msg);
        }
        Iterator<Map.Entry<Sender, LocalTime>> it = clients.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry<Sender, LocalTime> pair = it.next();
            if (pair.getValue().plusSeconds(2).isBefore(LocalTime.now())) {
                Sender key = pair.getKey();
                table.remove(key);
                it.remove();
            }
        }
    }

    public void receiveUDPMessage(String ip, int port) throws IOException {
        InetAddress group = InetAddress.getByName(ip);
        socket.joinGroup(group);
        while (true) {
            Sender sender = new Sender();
            byte[] recvBuf = new byte[SIZE];
            DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
            socket.receive(packet);
            SnakesProto.GameMessage msg = SnakesProto.GameMessage.parseFrom(Arrays.copyOf(packet.getData(), packet.getLength()));
            List<SnakesProto.GamePlayer> players = msg.getAnnouncement().getPlayers().getPlayersList();
            Iterator<SnakesProto.GamePlayer> iter = players.iterator();
            int j = 0;
            while (iter.hasNext()){
                if (iter.next().getRole().equals(SnakesProto.NodeRole.MASTER)) {
                    break;
                } else {
                    j++;
                }
            }
            if (j < msg.getAnnouncement().getPlayers().getPlayersList().size()) {
                sender.ip = packet.getAddress().toString().split("/")[1];
                sender.port = msg.getAnnouncement().getPlayers().getPlayers(j).getPort();
                UpdateTable(msg.getAnnouncement(), sender);
            }
        }
    }

    @Override
    public void run() {
        try (MulticastSocket sock = new MulticastSocket(PORT);){
            socket = sock;
            receiveUDPMessage(IP, PORT);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
