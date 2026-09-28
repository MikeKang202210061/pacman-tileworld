package core;

import edu.princeton.cs.algs4.StdDraw;

import java.awt.*;

import static core.SaveFile.readF;
import edu.princeton.cs.algs4.StdDraw;
import java.awt.*;

import static core.SaveFile.readF;
public class Main {
    private static final int WIDTH = 80;
    private static final int HEIGHT = 40;
    public static void main(String[] args) {
        drawMenu();
    }

        public static long drawMenu() {
            StdDraw.setCanvasSize(WIDTH * 16, HEIGHT * 16);
            StdDraw.setXscale(0, WIDTH);
            StdDraw.setYscale(0, HEIGHT);
            StdDraw.clear(Color.BLACK);
            StdDraw.setPenColor(Color.WHITE);
            Font titleFont = new Font("Monospaced", Font.BOLD, 30);
            StdDraw.setFont(titleFont);
            StdDraw.text(WIDTH / 2.0, HEIGHT * 0.8, "PROCEDURAL TILEWORLD ADVENTURE");
            Font optionFont = new Font("Monospaced", Font.BOLD, 20);
            StdDraw.setFont(optionFont);

            StdDraw.text(WIDTH / 2.0, HEIGHT * 0.55, "(N) New Game");
            StdDraw.text(WIDTH / 2.0, HEIGHT * 0.45, "(L) Load Game");
            StdDraw.text(WIDTH / 2.0, HEIGHT * 0.35, "(Q) Quit Game");
            StdDraw.show();
            return processInput();
        }
        public static long processInput() {
            while (true) {
                if (StdDraw.hasNextKeyTyped()) {
                    char c = Character.toUpperCase(StdDraw.nextKeyTyped());
                    if (c == 'N') {
                        getSeedInput();
                        break;
                    } else if (c == 'L') {
                        readF();
                        break;
                    } else if (c == 'Q') {
                        System.exit(0);
                    }
                }
            }
            return -1;
        }
        public static void drawSeedFrame(String inputSeed) {
            StdDraw.clear(Color.BLACK);
            StdDraw.setPenColor(Color.WHITE);
            Font titleFont = new Font("Monospaced", Font.BOLD, 30);
            StdDraw.setFont(titleFont);
            StdDraw.text(WIDTH / 2.0, HEIGHT * 0.8, "PROCEDURAL TILEWORLD ADVENTURE");
            Font promptFont = new Font("Monospaced", Font.BOLD, 20);
            StdDraw.setFont(promptFont);
            StdDraw.text(WIDTH / 2.0, HEIGHT * 0.45, "Enter seed followed by S");
            StdDraw.text(WIDTH / 2.0, HEIGHT * 0.35, inputSeed);
            StdDraw.show();
        }
        public static void getSeedInput() {
            String inputSeed = "";
            drawSeedFrame(inputSeed);
            while (true) {
                if (StdDraw.hasNextKeyTyped()) {
                    char c = StdDraw.nextKeyTyped();
                    if (c == 'S' || c == 's') {
                        break;
                    }
                    if (Character.isDigit(c)) {
                        inputSeed += c;
                        drawSeedFrame(inputSeed);
                    }
                }
            }
            if (inputSeed.isEmpty()) {
                return ;
            }

            World w = new World(Long.parseLong(inputSeed));
            Player avatar = new Player(w);
        }
    }
