package core;

import edu.princeton.cs.algs4.StdDraw;
import tileengine.TERenderer;
import tileengine.TETile;
import tileengine.Tileset;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.ArrayList;


public class Player {
    int x;
    int y;
    long seed;
    World playerWorld;
    private List<Point> currentPath = null;
    private Point targetPoint = null;
    private boolean isMouseAlreadyPressed = false;
    boolean gameWin = false;
    private boolean showAllMap = false;

    public Player(World world){
        this.seed = world.getSeed();
        this.playerWorld = world;
        this.x = playerWorld.rooms.getFirst().centerX;
        this.y = playerWorld.rooms.getFirst().centerY;
        drawPosition();
        playerWorld.setVisible(x,y);
        move();
    }
    public Player(World world, int px, int py){
        this.seed = world.getSeed();
        this.playerWorld = world;
        this.x = px;
        this.y = py;
        drawPosition();
        playerWorld.setVisible(this.x,this.y);
        move();
    }
    public int getX(){
        return x;
    }
    public int getY(){
        return y;
    }
    private void drawPosition(){
        this.playerWorld.getMap()[x][y]= Tileset.TREE;
    }
    private void toggle(World w, int px, int py){
            this.playerWorld.getMap()[this.x][this.y] = Tileset.FLOOR;
            this.x = px;
            this.y = py;
            drawPosition();
    }
    private String HudDescription(int x ,int y){
        String base;
        int w = playerWorld.getWidth();
        int h = playerWorld.getHeight();
        if(x < 0 || x >= w || y < 0 || y >= h){
            return "Nothing";
        }
        if(playerWorld.getMap()[x][y] == Tileset.FLOOR){
            base = "floor";
        } else if(playerWorld.getMap()[x][y] == Tileset.WALL){
            base = "wall";
        } else if(playerWorld.getMap()[x][y] == Tileset.TREE){
            base = "avatar";
        } else {
            base = "Out_Of_Area";
        }
        if(playerWorld.hasCoinAt(x, y)){
            base = base + " | Coin";
        }
        base += "    Coins:" + playerWorld.getCollectedCoins()+"/15";

        if (showAllMap) {
            base += "  | Press B to hide full map";
        } else {
            base += "  | Press B to toggle line of sight";
        }
        return base;
    }
    private void move() {
        char c;
        TERenderer render = new TERenderer();
        TETile[][] map = playerWorld.getMap();
        render.initialize(World.getW(), World.getH());
        while (true) {
            while (StdDraw.hasNextKeyTyped()) {
                c = StdDraw.nextKeyTyped();
                c = Character.toLowerCase(c);
                currentPath = null;
                targetPoint = null;
                switch (c) {
                    case 'b':
                        showAllMap = !showAllMap; // 按B切换视野
                        break;
                    case 'w':
                        if (playerWorld.getMap()[x][y + 1] != Tileset.WALL) {
                            playerWorld.getMap()[x][y] = Tileset.FLOOR;
                            toggle(playerWorld, x, y + 1);
                            boolean win = playerWorld.tryCollectCoin(x, y);
                            if (win) {
                                gameWin = true;
                            }
                            playerWorld.setVisible(x, y);
                        }
                        break;
                    case 'd':
                        if (playerWorld.getMap()[x + 1][y] != Tileset.WALL) {
                            playerWorld.getMap()[x][y] = Tileset.FLOOR;
                            toggle(playerWorld, x + 1, y);
                            boolean win = playerWorld.tryCollectCoin(x, y);
                            if (win) {
                                gameWin = true;
                            }
                            playerWorld.setVisible(x, y);
                        }
                        break;
                    case 'a':
                        if (playerWorld.getMap()[x - 1][y] != Tileset.WALL) {
                            playerWorld.getMap()[x][y] = Tileset.FLOOR;
                            toggle(playerWorld, x - 1, y);
                            boolean win = playerWorld.tryCollectCoin(x, y);
                            if (win) {
                                gameWin = true;
                            }
                            playerWorld.setVisible(x, y);
                        }
                        break;
                    case 's':
                        if (playerWorld.getMap()[x][y - 1] != Tileset.WALL) {
                            playerWorld.getMap()[x][y] = Tileset.FLOOR;
                            toggle(playerWorld, x, y - 1);
                            boolean win = playerWorld.tryCollectCoin(x, y);
                            if (win) {
                                gameWin = true;
                            }
                            playerWorld.setVisible(x, y);
                        }
                        break;
                    case 'q':
                        SaveFile.putInFile(this);
                        System.exit(0);
                        break;
                    default:
                        break;
                }
            }
            if(StdDraw.isMousePressed()) {
                if (!isMouseAlreadyPressed) {
                    isMouseAlreadyPressed = true;
                    int clickedX = (int) StdDraw.mouseX();
                    int clickedY = (int) StdDraw.mouseY();
                    Point clickedPoint = new Point(clickedX, clickedY);
                    if (targetPoint != null && targetPoint.equals(clickedPoint) && currentPath != null) {
                        walkPath();
                    } else {
                        currentPath = findPath(x, y, clickedX, clickedY);
                        if (currentPath != null) {
                            targetPoint = clickedPoint;
                        } else {
                            targetPoint = null;
                        }
                    }
                }
            }
                else{
                    isMouseAlreadyPressed = false;
                }
            TETile[][] renderMap = new TETile[World.getW()][World.getH()];
            TETile[][] baseMap = playerWorld.getMap();
            for (int xx = 0; xx < World.getW(); xx++) {
                for (int yy = 0; yy < World.getH(); yy++) {
                    renderMap[xx][yy] = baseMap[xx][yy];
                    // 如果该坐标存在金币，覆盖渲染为金币瓦片
                    if (playerWorld.hasCoinAt(xx, yy)) {
                        renderMap[xx][yy] = World.COIN_TILE;
                    }
                }
            }
            TETile FOG_FULL = new TETile(' ', java.awt.Color.BLACK, java.awt.Color.BLACK, "fog", 0);

            for (int xx = 0; xx < World.getW(); xx++) {
                for (int yy = 0; yy < World.getH(); yy++) {
                    if (showAllMap || playerWorld.isVisible(xx, yy)) {
                        renderMap[xx][yy] = baseMap[xx][yy];
                        if(playerWorld.hasCoinAt(xx, yy)){
                            renderMap[xx][yy] = World.COIN_TILE;
                        }
                    }else{
                        renderMap[xx][yy] = FOG_FULL;
                    }
                }
            }
            render.drawTiles(renderMap);
            if(gameWin) {
                StdDraw.setPenColor(StdDraw.BLACK);
                StdDraw.filledRectangle(World.getW()/2.0, World.getH()/2.0, 22, 8);
                StdDraw.setPenColor(StdDraw.YELLOW);
                StdDraw.setFont(new Font("SansSerif", Font.BOLD, 32));
                StdDraw.text(World.getW()/2.0, World.getH()/2.0 + 1, "YOU WIN!");
                StdDraw.setFont(new Font("SansSerif", Font.PLAIN, 18));
                StdDraw.text(World.getW()/2.0, World.getH()/2.0 -1, "You collected 15 coins!");
                StdDraw.setFont(new Font("SansSerif", Font.PLAIN,14));
                StdDraw.text(World.getW()/2.0, World.getH()/2.0 -3, "Press Q to quit");
            }
            highLightPath();
            int mouseX = (int) StdDraw.mouseX();
            int mouseY = (int) StdDraw.mouseY();
            StdDraw.setPenColor(StdDraw.WHITE);
            StdDraw.textLeft(0, World.getH() - 1, HudDescription(mouseX, mouseY));
            StdDraw.show();
            StdDraw.pause(10);
        }
    }
    private void walkPath(){
        if(currentPath == null)return;
        TERenderer render = new TERenderer();
        int width = World.getW();
        int height = World.getH();
        for(Point p : currentPath){
            toggle(playerWorld, p.x, p.y);
            boolean win = playerWorld.tryCollectCoin(x, y);
            if (win) {
                gameWin = true;
            }

            playerWorld.setVisible(x, y);

            TETile FOG_FULL = new TETile(' ', java.awt.Color.BLACK, java.awt.Color.BLACK, "fog", 0);
            TETile[][] renderMap = new TETile[width][height];
            TETile[][] baseMap = this.playerWorld.getMap();
            for (int xx = 0; xx < width; xx++) {
                for (int yy = 0; yy < height; yy++) {
                    if (showAllMap || playerWorld.isVisible(xx, yy)) {
                        renderMap[xx][yy] = baseMap[xx][yy];
                        if(playerWorld.hasCoinAt(xx, yy)){
                            renderMap[xx][yy] = World.COIN_TILE;
                        }
                    }else{
                        renderMap[xx][yy] = FOG_FULL;
                    }
                }
            }
            render.drawTiles(renderMap);
            render.drawTiles(renderMap);
            StdDraw.setPenColor(StdDraw.WHITE);
            StdDraw.textLeft(0, World.getH() - 2, "Tile: " + HudDescription(p.x, p.y));
            StdDraw.show();
            StdDraw.pause(100);
        }
        currentPath = null;
        targetPoint = null;
    }
    private void highLightPath(){
        if( currentPath == null){
            return;
        }
        StdDraw.setPenColor(StdDraw.RED);
        for(Point p : currentPath){
            StdDraw.filledCircle(p.x + 0.5, p.y + 0.5, 0.2);
        }
    }
    private List<Point> findPath(int startX, int startY, int targetX, int targetY){
        int width = World.getW();
        int height = World.getH();
        if(targetX < 0 || targetX >= width || targetY < 0 || targetY >= height){
            return null;
        }
        if(playerWorld.getMap()[targetX][targetY] == Tileset.WALL){
            return null;
        }
        if(targetX == startX && targetY == startY){
            return null;
        }
        Queue<Point> queue = new LinkedList<>();
        Map<Point , Point> parentMap = new HashMap<>();
        boolean[][] visited = new boolean[width][height];
        Point start = new Point(startX, startY);
        Point target = new Point(targetX, targetY);
        queue.add(start);
        visited[startX][startY] = true;
        int[] dx = {0, 0, -1, 1};
        int[] dy = {1, -1, 0, 0};
        boolean found = false;
        while (! queue.isEmpty()){
            Point curr = queue.poll();
            if(curr.equals(target)){
                found = true;
                break;
            }
            for(int i = 0; i < 4; i++) {
                int nx = curr.x + dx[i];
                int ny = curr.y + dy[i];
                if (nx >= 0 && nx < width && ny >= 0 && ny < height) {
                    if (playerWorld.getMap()[nx][ny] != Tileset.WALL && !visited[nx][ny]) {
                        visited[nx][ny] = true;
                        Point next = new Point(nx, ny);
                        parentMap.put(next, curr);
                        queue.add(next);
                    }
                }
            }
        }
        if(! found) return null;
        List<Point> path = new ArrayList<>();
        Point curr = target;
        while(!curr.equals(start)){
            path.add(0,curr);
            curr = parentMap.get(curr);
        }
        return path;
    }
}

