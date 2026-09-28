package core;

import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.Out;
import edu.princeton.cs.algs4.StdDraw;

import java.awt.*;
import java.io.File;

import static core.Main.drawMenu;


public class SaveFile {
        private static final int WIDTH = 80;
        private static final int HEIGHT = 40;
        public static void drawLoadingFrame(int i){
                StdDraw.clear(Color.BLACK);
                StdDraw.setPenColor(Color.WHITE);
                Font titleFont = new Font("Monospaced", Font.BOLD, 30);
                StdDraw.setFont(titleFont);
                StdDraw.text(WIDTH / 2.0, HEIGHT * 0.8, "PAC-MAN TILEWORLD");
                Font promptFont = new Font("Monospaced", Font.BOLD, 20);
                StdDraw.setFont(promptFont);
                if(i != -1){
                    StdDraw.text(WIDTH / 2.0, HEIGHT * 0.45, "Loading");
                    StdDraw.show();
                }
                else {
                    StdDraw.text(WIDTH / 2.0, HEIGHT * 0.35, "You have no saved game,please create a new one");
                    StdDraw.show();
                    StdDraw.pause(1000);
                    drawMenu();
                }
        }
    public static void readF() {
        String filename = "lastGameMemory.txt";
        File file = new File(filename);

        // 文件不存在
        if (!file.exists()) {
            drawLoadingFrame(-1);
            return;
        }

        In in = new In(file);
        // 文件存在但是为空
        if (!in.hasNextLine()) {
            drawLoadingFrame(-1);
            in.close();
            return;
        }

        String saved = in.readLine();
        String[] parts = saved.split(",", 6);

        long seed = Long.parseLong(parts[0]);
        int px = Integer.parseInt(parts[1]);
        int py = Integer.parseInt(parts[2]);
        int collected = Integer.parseInt(parts[3]);
        boolean isGameWin = Boolean.parseBoolean(parts[4]);
        String coinStr = parts[5];

        //存档已经通关
        if (isGameWin) {
            showMessageAndBackMenu("This save is already completed!");
            in.close();
            return;
        }

        //正常加载游戏
        drawLoadingFrame(1);
        World w = new World(seed);
        w.restoreCoinsState(collected, coinStr);
        new Player(w, px, py);
        in.close();
    }


    public static void putInFile(Player player) {
        World w = player.playerWorld;
        StringBuilder coinSb = new StringBuilder();
        int ww = World.getW();
        int hh = World.getH();
        for (int x = 0; x < ww; x++) {
            for (int y = 0; y < hh; y++) {
                coinSb.append(w.hasCoinAt(x, y) ? '1' : '0');
            }
        }
        String outLine = w.getSeed() + ","
                + player.getX() + ","
                + player.getY() + ","
                + w.getCollectedCoins() + ","
                + player.gameWin + ","
                + coinSb;

        Out out = new Out("lastGameMemory.txt");
        out.println(outLine);
        out.close();
    }

    private static void showMessageAndBackMenu(String msg) {
        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(Color.WHITE);
        Font font = new Font("Monospaced", Font.BOLD, 22);
        StdDraw.setFont(font);
        StdDraw.text(WIDTH / 2.0, HEIGHT * 0.45, msg);
        StdDraw.show();
        StdDraw.pause(1500);
        drawMenu();
    }
}
