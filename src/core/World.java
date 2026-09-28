package core;

import org.checkerframework.checker.units.qual.C;
import tileengine.TETile;
import tileengine.Tileset;
import utils.RandomUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class World {
    private final static int worldWidth = 80;
    private static int worldHeight = 40;
    private TETile[][] world;
    private Random rand;
    ArrayList<Room> rooms;
    private ArrayList<Zone> zones;
    private long seed;

    //金币
    public static final int total = 20;
    public static final int target = 15;
    private boolean[][] coins;
    private int collected;

    public static final TETile COIN_TILE = new TETile(
            '$',
            Color.YELLOW,
            Color.BLACK,
            "gold coin",
            0
    );

    //视野
    private boolean[][] visible;
    private int viewRadius = 5;

    public World(long seed) {
        this.seed = seed;
        rand = new Random(seed);
        rooms = new ArrayList<>();
        zones = new ArrayList<>();
        world = new TETile[worldWidth][worldHeight];
        coins = new boolean[worldWidth][worldHeight];
        visible = new boolean[worldWidth][worldHeight];
        for (int x = 0; x < worldWidth; x++) {
            for (int y = 0; y < worldHeight; y++) {
                world[x][y] = Tileset.NOTHING;
            }
        }
        //完成初始化

        createZones();//先建立分区
        createRooms();
        buildMST();
        createWalls();

        spawnCoins();
    }


    // 金币方法
    private void spawnCoins() {
        int placed = 0;
        while (placed < total) {
            int rx = RandomUtils.uniform(rand, 0, worldWidth);
            int ry = RandomUtils.uniform(rand, 0, worldHeight);
            //条件：必须是地板，该位置还没有金币
            if (world[rx][ry].equals(Tileset.FLOOR) && !coins[rx][ry]) {
                coins[rx][ry] = true;
                placed++;
            }
        }
    }

    public boolean tryCollectCoin(int x, int y) {
        if (x < 0 || x >= worldWidth || y < 0 || y >= worldHeight) {
            return false;
        }
        if (coins[x][y]) {
            coins[x][y] = false;
            collected += 1;
        }
        return collected >= target;
    }

    public boolean hasCoinAt(int x, int y) {
        if(x <0 || x >= getW() || y <0 || y >= getH()) {
            return false;
        }
        return coins[x][y];
    }

    public int getCollectedCoins() {
        return collected;
    }

    //视野方法
    public void setVisible(int ox, int oy) {
        // 全部置为不可见
        for (int x = 0; x < worldWidth; x++) {
            for (int y = 0; y < worldHeight; y++) {
                visible[x][y] = false;
            }
        }
        visible[ox][oy] = true;

        scan(ox, oy, 1, 0, 0, 1);
        scan(ox, oy, 1, 0, 0, -1);
        scan(ox, oy, -1, 0, 0, 1);
        scan(ox, oy, -1, 0, 0, -1);
        scan(ox, oy, 0, 1, 1, 0);
        scan(ox, oy, 0, 1, -1, 0);
        scan(ox, oy, 0, -1, 1, 0);
        scan(ox, oy, 0, -1, -1, 0);
    }

    private void scan(int ox, int oy, int xx, int xy, int yx, int yy) {
        double lowSlope = 0.0;
        double highSlope = 1.0;

        for (int d = 1; d <= viewRadius; d++) {
            boolean isBlocked = true;
            for (int i = d; i >= 0; i--) {
                int x = ox + i * xx + (d - i) * xy;
                int y = oy + i * yx + (d - i) * yy;


                if (x < 0 || x >= worldWidth || y < 0 || y >= worldHeight) {
                    continue;
                }

                int dx = Math.abs(x - ox);
                int dy = Math.abs(y - oy);
                if (dx + dy > viewRadius) {
                    continue;
                }

                double slope = (i - 0.5) / (d + 0.5);
                if (slope < lowSlope || slope > highSlope) {
                    continue;
                }

                visible[x][y] = true;

                if (world[x][y].equals(Tileset.WALL)) {
                    if (isBlocked) {
                        highSlope = (i - 0.5) / (d + 0.5);
                    } else {
                        return;
                    }
                } else {
                    isBlocked = false;
                }
            }
            if (isBlocked) {
                return;
            }
        }
    }


    public boolean isVisible(int x, int y) {
        if (x < 0 || x >= worldWidth || y < 0 || y >= worldHeight) {
            return false;
        }
        return visible[x][y];
    }

    public int getViewRadius() {
        return viewRadius;
    }

    //IO
    public void restoreCoinsState(int savedCollected, String coinStr) {
        this.collected = savedCollected;
        int idx = 0;
        for (int x = 0; x < worldWidth; x++) {
            for (int y = 0; y < worldHeight; y++) {
                char c = coinStr.charAt(idx++);
                coins[x][y] = (c == '1');
            }
        }
    }





    // build your own world!

    public int getWidth() {
        return worldWidth;
    }

    public int getHeight() {
        return  worldHeight;
    }
    public long getSeed(){
        return this.seed;
    }
    class Room{
        int startX; //左下角坐标
        int startY;
        int width; //长和宽
        int height;
        int centerX; //计算中心点
        int centerY;
        public Room(int startX, int startY, int width, int hight) {
            this.startX = startX;
            this.startY = startY;
            this.width = width;
            this.height = hight;
            this.centerX = startX+width/2;
            this.centerY = startY+hight/2;
        }

        public int getCenterX() {
            return centerX;
        }

        public int getCenterY() {
            return centerY;
        }
    }


    class Zone {
        int zoneX;    // 分区左下角x
        int zoneY;    // 分区左下角y
        int zoneW;    // 分区宽度
        int zoneH;    // 分区高度

        public Zone(int x, int y, int w, int h) {
            zoneX = x;
            zoneY = y;
            zoneW = w;
            zoneH = h;
        }
    }

    private void createZones() {
        int zoneCol = 8;
        int zoneRow = 4;
        // 左右上下各留2格
        int width = worldWidth - 4;
        int height = worldHeight - 4;
        int singleW = width / zoneCol;
        int singleH = height / zoneRow;

        //生成所有分区
        for (int col = 0; col < zoneCol; col++) {
            for (int row = 0; row < zoneRow; row++) {
                int zX = 2 + col * singleW;
                int zY = 2 + row * singleH;
                Zone z = new Zone(zX, zY, singleW, singleH);
                zones.add(z);
            }
        }
    }

    private void createRooms() {
        int roomNum = RandomUtils.uniform(rand, 20, 30);
        ArrayList<Zone> copy = new ArrayList<>(zones);//复制一份防修改
        for (int i = 0; i < roomNum; i++) {
            int useIdx = RandomUtils.uniform(rand, 0, copy.size());
            Zone useZone = copy.remove(useIdx);


            int minRoomW = 3;
            int maxRoomW = 7;
            int minRoomH = 3;
            int maxRoomH = 7;
            int rW = RandomUtils.uniform(rand, minRoomW, maxRoomW + 1);
            int rH = RandomUtils.uniform(rand, minRoomH, maxRoomH + 1);

            int roomXMin = useZone.zoneX + 1; //边缘+1防止粘连不好看
            int roomYMin = useZone.zoneY + 1;
            int roomXMax = useZone.zoneX + useZone.zoneW - rW - 1;
            int roomYMax = useZone.zoneY + useZone.zoneH - rH - 1;

            int rX = RandomUtils.uniform(rand, roomXMin, roomXMax + 1);
            int rY = RandomUtils.uniform(rand, roomYMin, roomYMax + 1);

            Room newRoom = new Room(rX, rY, rW, rH);
            rooms.add(newRoom);
            drawRoomFloor(newRoom);
        }
    }

    // 绘制房间地板
    private void drawRoomFloor(Room room) {
        for (int x = room.startX; x < room.startX + room.width; x++) {
            for (int y = room.startY; y < room.startY + room.height; y++) {
                world[x][y] = Tileset.FLOOR;
            }
        }
    }

    //连接房间
    private void connectRooms() {
        for (int i = 0; i < rooms.size() - 1; i++) {
            Room a = rooms.get(i);
            Room b = rooms.get(i + 1);
            int ax = a.getCenterX();
            int ay = a.getCenterY();
            int bx = b.getCenterX();
            int by = b.getCenterY();

            connectHorizontally(ax, bx, ay);
            connectVertically(ay, by, bx);
        }
    }

    // 水平连接
    private void connectHorizontally(int x1, int x2, int y) {
        int start = Math.min(x1, x2);
        int end = Math.max(x1, x2);
        for (int x = start; x <= end; x++) {
            world[x][y] = Tileset.FLOOR;
        }
    }

    // 垂直连接
    private void connectVertically(int y1, int y2, int x) {
        int start = Math.min(y1, y2);
        int end = Math.max(y1, y2);
        for (int y = start; y <= end; y++) {
            world[x][y] = Tileset.FLOOR;
        }
    }

    //生成墙体围绕地板
    private void createWalls() {
        for (int x = 0; x < worldWidth; x++) {
            for (int y = 0; y < worldHeight; y++) {
                if (world[x][y] == Tileset.NOTHING && hasFloor(x, y)) {
                    world[x][y] = Tileset.WALL;
                }
            }
        }
    }

    // 判断上下左右是否有地板
    private boolean hasFloor(int x, int y) {
        if (x - 1 >= 0 && world[x-1][y] == Tileset.FLOOR) return true;
        if (x + 1 < worldWidth && world[x+1][y] == Tileset.FLOOR) return true;
        if (y - 1 >= 0 && world[x][y-1] == Tileset.FLOOR) return true;
        if (y + 1 < worldHeight && world[x][y+1] == Tileset.FLOOR) return true;
        return false;
    }

    public TETile[][] getMap() {
        return world;
    }

    public static int getW() { return worldWidth; }
    public static int getH() { return worldHeight; }

    private static class Edge {
        int a, b;
        int dist;
        Edge(int a, int b, int dist) {
            this.a = a;
            this.b = b;
            this.dist = dist;
        }
    }

    private static class DSU {
        int[] parent;
        DSU(int n) {
            parent = new int[n];
            for (int i = 0; i < n; i++) parent[i] = i;
        }
        int find(int x) {
            if (parent[x] != x) parent[x] = find(parent[x]);
            return parent[x];
        }
        boolean union(int x, int y) {
            int rx = find(x), ry = find(y);
            if (rx == ry) return false;
            parent[ry] = rx;
            return true;
        }
    }

    private int[] getRoomCenter(Room r) {
        int cx = r.centerX;
        int cy = r.centerY;
        return new int[]{cx, cy};
    }

    private void buildMST() {
        int roomCount = rooms.size();
        if (roomCount <= 1) return;

        // 收集所有中心点
        int[][] centers = new int[roomCount][2];
        for (int i = 0; i < roomCount; i++) {
            centers[i] = getRoomCenter(rooms.get(i));
        }

        // 生成所有边
        List<Edge> edges = new ArrayList<>();
        for (int i = 0; i < roomCount; i++) {
            for (int j = i + 1; j < roomCount; j++) {
                int d = Math.abs(centers[i][0] - centers[j][0]) + Math.abs(centers[i][1] - centers[j][1]);
                edges.add(new Edge(i, j, d));
            }
        }

        // 按距离从小到大排列
        edges.sort((e1, e2) -> Integer.compare(e1.dist, e2.dist));

        // KruskalMST边
        DSU dsu = new DSU(roomCount);
        java.util.List<Edge> mstEdges = new java.util.ArrayList<>();
        for (Edge e : edges) {
            if (dsu.union(e.a, e.b)) {
                mstEdges.add(e);
            }
        }

        for (Edge e : mstEdges) {
            int x1 = centers[e.a][0], y1 = centers[e.a][1];
            int x2 = centers[e.b][0], y2 = centers[e.b][1];

            connectHorizontally(x1, x2, y1);
            connectVertically(y1, y2, x2);
        }
    }

}
