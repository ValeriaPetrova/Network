package ru.nsu.snakes.view;

import me.ippolitov.fit.snakes.SnakesProto;
import ru.nsu.snakes.Controller;
import ru.nsu.snakes.Model;
import ru.nsu.snakes.network.Sender;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GUI {
    public static JFrame window = new JFrame("snake");
    public static JFrame error = new JFrame("error");
    private static JPanel game;
    private static JPanel rightSide;
    private static JPanel tables;
    private static JTable scores;
    private static JTable gameSpecs;
    private static JTable gamelistinfo;
    private static JPanel buttons;
    private static JPanel gameField;
    private static JButton newGame;
    private static JButton exit;

    public static int ox;
    public static int oy;
    public static int checksize = 20;

    private static HashMap<Integer, JButton> buttonList;
    private static HashMap<Integer, JButton> connectButtons;

    private static void createUIComponents() {
        game = new JPanel();
        CreateField();
        CreateScores();
        CreateGameList();
        CreateSpecs(Model.config);
        CreateButtons();
    }

    public static void init(me.ippolitov.fit.snakes.SnakesProto.GameConfig config) {
        ox = config.getWidth();
        oy = config.getHeight();
        rightSide = new JPanel();
        rightSide.setLayout(new BorderLayout());
        tables= new JPanel();
        createUIComponents();
        rightSide.add(tables, BorderLayout.NORTH);
        game.add(rightSide);
        window.setLayout(new BorderLayout());
        window.setContentPane(game);
        setKeyBindings();
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.pack();
        window.setVisible(true);
    }

    public static void repaint(me.ippolitov.fit.snakes.SnakesProto.GameState state,
                               ConcurrentHashMap<Sender, SnakesProto.GameMessage.AnnouncementMsg> table) {
        System.out.println(state);
        ox = state.getConfig().getWidth();
        oy = state.getConfig().getHeight();
        RepaintField(state.getSnakesList(), state.getFoodsList());
        repaintGameList(table);
        RepaintScores(state.getPlayers().getPlayersList());
        RepaintSpecs(state.getConfig());
        RepaintButtons();
    }

    public static void error(String message) {
        JPanel panel = new JPanel();
        JTextField textField = new JTextField();
        textField.setBackground(Color.WHITE);
        textField.setColumns(14);
        textField.setText(message);
        panel.add(textField);
        error.getContentPane().add(panel);
        error.setSize(400,400);
        error.pack();
        error.setLocationRelativeTo(null);
        error.setVisible(true);
    }

    private static void CreateField() {
        gameField = new JPanel();
        gameField.setLayout(new GridLayout(oy, oy));
        buttonList = new HashMap<>();
        for (int i = 0; i < ox*oy; i++){
            buttonList.put(i, new JButton());
            JButton b = buttonList.get(i);
            b.setBackground(Color.BLACK);
            b.setPreferredSize(new Dimension(checksize,checksize));
            gameField.add(b);
        }
        gameField.setSize(ox, oy);
        game.add(gameField);
    }

    private static void CreateGameList() {
        JPanel gameList = new JPanel();
        connectButtons = new HashMap<>();
        String[] columnNames = {"Name", "Players", "Size", "Food"};
        String[][] data = {};
        int size = 14;
        JToolBar toolbar = new JToolBar(SwingConstants.VERTICAL);
        for (int i = 0; i < size; i++){
            JButton b = new JButton("name" + " : connect");
            b.setPreferredSize(new Dimension(checksize, 10));
            toolbar.add(b);
            connectButtons.put(i, b);
        }
        gamelistinfo = new JTable(data, columnNames);
        gamelistinfo.setRowHeight(20);
        JScrollPane scrollPane = new JScrollPane(gamelistinfo);
        scrollPane.setPreferredSize(new Dimension((int)(30*checksize*0.4), (int)(30*checksize*0.5)));
        toolbar.setPreferredSize(new Dimension((int)(30*checksize*0.4), (int)(30*checksize*0.5)));
        gameList.add(scrollPane, BorderLayout.CENTER);
        gameList.add(toolbar);
        rightSide.add(gameList, BorderLayout.SOUTH);
    }

    private static void CreateSpecs(SnakesProto.GameConfig config) {
        String[] columnNames = {"Type", "Value"};
        String[][] data = {
                {"Width", "", ""},
                {"Height", "", ""},
                {"food_static", "", ""},
                {"food_per_player", "", ""},
                {"state_delay_ms", "", ""},
                {"dead_food_prob", "", ""},
                {"ping_delay_ms", "", ""},
                {"node_timeout_ms", "", ""},
        };
        String s = "";

        data[0][1] = Integer.toString(config.getWidth());
        data[1][1] = Integer.toString(config.getHeight());
        data[2][1] = Integer.toString(config.getFoodStatic());
        data[3][1] = Float.toString(config.getFoodPerPlayer());
        data[4][1] = Integer.toString(config.getStateDelayMs());
        data[5][1] = Float.toString(config.getDeadFoodProb());
        data[6][1] = Integer.toString(config.getPingDelayMs());
        data[7][1] = Integer.toString(config.getNodeTimeoutMs());

        gameSpecs = new JTable(data, columnNames);
        JScrollPane scrollPane = new JScrollPane(gameSpecs);
        scrollPane.setPreferredSize(new Dimension((int)(30*checksize*0.4), (int)(30*checksize*0.4)));
        tables.add(scrollPane, BorderLayout.LINE_END);
    }

    private static void CreateScores() {
        String[] columnNames = {"Position", "Name", "Score", "IsMe"};
        String[][] data = {};
        scores = new JTable(data, columnNames);
        JScrollPane scrollPane = new JScrollPane(scores);
        scrollPane.setPreferredSize(new Dimension((int)(30*checksize*0.5), (int)(30*checksize*0.4)));
        tables.add(scrollPane, BorderLayout.BEFORE_LINE_BEGINS);
    }

    private static void CreateButtons() {
        buttons = new JPanel();

        exit = new JButton("exit");
        exit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Controller.exit();
            }
        });

        newGame = new JButton("new game");
        newGame.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Controller.newgame();
                System.out.println("new");
            }
        });

        buttons.add(exit, BorderLayout.AFTER_LINE_ENDS);
        buttons.add(newGame, BorderLayout.BEFORE_LINE_BEGINS);
        buttons.setPreferredSize(new Dimension((int)(30*checksize), (int)(30*checksize*0.1)));
        rightSide.add(buttons, BorderLayout.CENTER);
    }

    private static void RepaintField(java.util.List<SnakesProto.GameState.Snake> snakesList, java.util.List<SnakesProto.GameState.Coord> foodList) { //repaints field to window
        for (int i = 0; i < ox * oy; i++) {
            JButton b = buttonList.get(i);
            b.setBackground(Color.BLACK);
        }
        Iterator<SnakesProto.GameState.Snake> iter = snakesList.iterator();
        while (iter.hasNext()) {
            SnakesProto.GameState.Snake snake = iter.next();
            List<SnakesProto.GameState.Coord> snakeBody = snake.getPointsList();
            int i = 0;
            Iterator<SnakesProto.GameState.Coord> it = snakeBody.iterator();
            SnakesProto.GameState.Coord prev = snakeBody.get(0);
            SnakesProto.GameState.Coord.Builder tmp = SnakesProto.GameState.Coord.newBuilder();
            while (it.hasNext()){
                SnakesProto.GameState.Coord coord = it.next();
                if (i != 0) {
                    if (coord.getX() != 0){
                        int step;
                        if (coord.getX() < 0) {
                            step = -1;
                        } else step = 1;
                        for (int j = prev.getX(); j != (Model.config.getWidth() + coord.getX() + prev.getX()) % Model.config.getWidth(); j = (Model.config.getWidth() + j + step) % Model.config.getWidth()){
                            tmp.setY(prev.getY());
                            tmp.setX(j);
                            repaintCheck(tmp.build(), snake.getPlayerId());
                        }
                        repaintCheck(tmp.setX((Model.config.getWidth() + coord.getX() + prev.getX()) % Model.config.getWidth()).build(), snake.getPlayerId());
                    } else {
                        int step;
                        if (coord.getY() < 0) {
                            step = -1;
                        }
                        else step = 1;
                        for (int j = prev.getY(); j != (Model.config.getHeight() + coord.getY() + prev.getY()) % Model.config.getHeight(); j = (Model.config.getHeight() + j + step) % Model.config.getHeight()){
                            tmp.setX(prev.getX());
                            tmp.setY(j);
                            repaintCheck(tmp.build(), snake.getPlayerId()); //?
                        }
                        repaintCheck(tmp.setY((Model.config.getHeight() + coord.getY() + prev.getY()) % Model.config.getHeight()).setX(prev.getX()).build(), snake.getPlayerId());
                    }
                    prev = tmp.build();
                }
                i++;
            }
        }
        Iterator<SnakesProto.GameState.Coord> it = foodList.iterator();
        while (it.hasNext()){
            SnakesProto.GameState.Coord coord = it.next();
            int butNum = coord.getY() * Model.config.getWidth() + coord.getX();
            buttonList.get(butNum).setBackground(Color.GREEN);
        }
    }
    private static void repaintCheck(SnakesProto.GameState.Coord coord, int playerId) {
        int butNum = coord.getY() * Model.config.getWidth() + coord.getX();
        if (playerId == Controller.playerId) buttonList.get(butNum).setBackground(Color.YELLOW);
        else buttonList.get(butNum).setBackground(Color.RED);
    }

    public static void repaintGameList(ConcurrentHashMap<Sender, SnakesProto.GameMessage.AnnouncementMsg> table) {
        String[] columnNames = {"Name", "Players", "Size", "Food"};
        String[][] empty = {};
        DefaultTableModel model = new DefaultTableModel(empty,columnNames);
        gamelistinfo.setModel(model);
        model.fireTableDataChanged();
        String[][] data = new String[table.size() + 1][5];
        int i = 0;
        Iterator<Map.Entry<Sender, SnakesProto.GameMessage.AnnouncementMsg>> it = table.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry<Sender, SnakesProto.GameMessage.AnnouncementMsg> pair = it.next();
            data[i][0] = Integer.toString(pair.getKey().port);
            data[i][1] = Integer.toString(pair.getValue().getPlayers().getPlayersList().size());
            data[i][2] = pair.getValue().getConfig().getWidth() + "x" +
                    pair.getValue().getConfig().getHeight();
            data[i][3] = pair.getValue().getConfig().getFoodStatic() + "+" +
                    pair.getValue().getConfig().getFoodPerPlayer() + "x";
            JButton b = connectButtons.get(i);
            ActionListener[] list = b.getActionListeners();
            if (list.length > 0) b.removeActionListener(list[0]);
            b.setName(Integer.toString(i));
            b.addActionListener(new ConnectListener(pair.getKey()));
            connectButtons.put(i, b);
            i++;
        }
        DefaultTableModel model1 = new DefaultTableModel(data,columnNames); // for example
        gamelistinfo.setModel(model1);
        model1.fireTableDataChanged();
    }

    private static void RepaintSpecs(SnakesProto.GameConfig config) {
        String[] columnNames = {"Type", "Value"};
        String[][] data = {
                {"Width", "", ""},
                {"Height", "", ""},
                {"food_static", "", ""},
                {"food_per_player", "", ""},
                {"state_delay_ms", "", ""},
                {"dead_food_prob", "", ""},
                {"ping_delay_ms", "", ""},
                {"node_timeout_ms", "", ""},
        };
        String s = "";
        data[0][1] = Integer.toString(config.getWidth());
        data[1][1] = Integer.toString(config.getHeight());
        data[2][1] = Integer.toString(config.getFoodStatic());
        data[3][1] = Float.toString(config.getFoodPerPlayer());
        data[4][1] = Integer.toString(config.getStateDelayMs());
        data[5][1] = Float.toString(config.getDeadFoodProb());
        data[6][1] = Integer.toString(config.getPingDelayMs());
        data[7][1] = Integer.toString(config.getNodeTimeoutMs());
        DefaultTableModel model = new DefaultTableModel(data,columnNames);
        gameSpecs.setModel(model);
        model.fireTableDataChanged();
    }

    private static void RepaintScores(java.util.List<SnakesProto.GamePlayer> players) {
        String[] columnNames = {"Position", "Name", "Score", "IsMe"};
        String[][] empty = {};
        DefaultTableModel model = new DefaultTableModel(empty,columnNames); // for example
        scores.setModel(model);
        model.fireTableDataChanged();
        String[][] data = new String[players.size() + 1][5];
        Iterator<SnakesProto.GamePlayer> playerIterator = players.iterator();
        int i = 0;
        while (playerIterator.hasNext()){
            SnakesProto.GamePlayer player = playerIterator.next();
            data[i][0] = Integer.toString(i+1);
            data[i][1] = player.getName();
            data[i][2] = Integer.toString(player.getScore());
            if (player.getId() == Controller.playerId) data[i][3] = "yes";
            else data[i][3] = "no";
            i++;
        }
        DefaultTableModel model1 = new DefaultTableModel(data,columnNames); // for example
        scores.setModel(model1);
        model1.fireTableDataChanged();
    }

    private static void RepaintButtons() {

    }

    private static void setKeyBindings() {
        InputMap inputMap =
                game.getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW);
        inputMap.put(KeyStroke.getKeyStroke("W"), "up arrow");
        inputMap.put(KeyStroke.getKeyStroke("S"), "down arrow");
        inputMap.put(KeyStroke.getKeyStroke("A"), "left arrow");
        inputMap.put(KeyStroke.getKeyStroke("D"), "right arrow");

        inputMap.put(KeyStroke.getKeyStroke("UP"), "up arrow");
        inputMap.put(KeyStroke.getKeyStroke("DOWN"), "down arrow");
        inputMap.put(KeyStroke.getKeyStroke("LEFT"), "left arrow");
        inputMap.put(KeyStroke.getKeyStroke("RIGHT"), "right arrow");

        inputMap = game.getInputMap(JPanel.WHEN_FOCUSED);
        inputMap.put(KeyStroke.getKeyStroke("UP"), "up arrow");
        inputMap.put(KeyStroke.getKeyStroke("DOWN"), "down arrow");
        inputMap.put(KeyStroke.getKeyStroke("LEFT"), "left arrow");
        inputMap.put(KeyStroke.getKeyStroke("RIGHT"), "right arrow");


        game.getActionMap().put("up arrow",
                new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Controller.steer(SnakesProto.Direction.UP);
                    }
                });
        game.getActionMap().put("down arrow",
                new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Controller.steer(SnakesProto.Direction.DOWN);
                    }
                });
        game.getActionMap().put("left arrow",
                new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Controller.steer(SnakesProto.Direction.LEFT);
                    }
                });
        game.getActionMap().put("right arrow",
                new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Controller.steer(SnakesProto.Direction.RIGHT);
                    }
                });
    }
}

