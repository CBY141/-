import java.awt.*;
import java.util.Random;

public class GameWorld {
    public MapTile[][] map; // 改为public便于访问
    private Random random = new Random();

    public GameWorld() {
        map = new MapTile[GameConfig.MAP_WIDTH][GameConfig.MAP_HEIGHT];
        generateMap(); // 确保此方法存在且完整
    }

    // 完整的地图生成方法
    private void generateMap() {
        for (int x = 0; x < GameConfig.MAP_WIDTH; x++) {
            for (int y = 0; y < GameConfig.MAP_HEIGHT; y++) {
                map[x][y] = new MapTile(x, y, GameConfig.TILE_EMPTY);
            }
        }
        for (int x = 0; x < GameConfig.MAP_WIDTH; x++) {
            map[x][0] = new MapTile(x, 0, GameConfig.TILE_STEEL);
            map[x][1] = new MapTile(x, 1, GameConfig.TILE_BRICK);
            map[x][GameConfig.MAP_HEIGHT-1] = new MapTile(x, GameConfig.MAP_HEIGHT-1, GameConfig.TILE_STEEL);
            map[x][GameConfig.MAP_HEIGHT-2] = new MapTile(x, GameConfig.MAP_HEIGHT-2, GameConfig.TILE_BRICK);
        }
        for (int y = 0; y < GameConfig.MAP_HEIGHT; y++) {
            map[0][y] = new MapTile(0, y, GameConfig.TILE_STEEL);
            map[1][y] = new MapTile(1, y, GameConfig.TILE_BRICK);
            map[GameConfig.MAP_WIDTH-1][y] = new MapTile(GameConfig.MAP_WIDTH-1, y, GameConfig.TILE_STEEL);
            map[GameConfig.MAP_WIDTH-2][y] = new MapTile(GameConfig.MAP_WIDTH-2, y, GameConfig.TILE_BRICK);
        }
        generateObstacles(GameConfig.TILE_BRICK, 60);
        generateObstacles(GameConfig.TILE_STEEL, 15);
        generateObstacles(GameConfig.TILE_GRASS, 40);
        generateObstacles(GameConfig.TILE_WATER, 12);
        createSymmetricStructures();
        clearArea(20, 25, 10, 8);
    }

    private void generateObstacles(int type, int count) {
        for (int i = 0; i < count; i++) {
            int x, y;
            do {
                x = random.nextInt(GameConfig.MAP_WIDTH-4) + 2;
                y = random.nextInt(GameConfig.MAP_HEIGHT-4) + 2;
            } while (map[x][y].getType() != GameConfig.TILE_EMPTY);
            map[x][y] = new MapTile(x, y, type);
        }
    }

    private void createSymmetricStructures() {
        int centerX = GameConfig.MAP_WIDTH / 2;
        int centerY = GameConfig.MAP_HEIGHT / 2;
        for (int i = -2; i <= 2; i++) {
            if (centerX + i >= 0 && centerX + i < GameConfig.MAP_WIDTH) {
                map[centerX + i][centerY] = new MapTile(centerX + i, centerY, GameConfig.TILE_STEEL);
            }
            if (centerY + i >= 0 && centerY + i < GameConfig.MAP_HEIGHT) {
                map[centerX][centerY + i] = new MapTile(centerX, centerY + i, GameConfig.TILE_STEEL);
            }
        }
        int[][] corners = {{5,5}, {GameConfig.MAP_WIDTH-6,5}, {5,GameConfig.MAP_HEIGHT-6}, {GameConfig.MAP_WIDTH-6,GameConfig.MAP_HEIGHT-6}};
        for (int[] corner : corners) {
            int cx = corner[0];
            int cy = corner[1];
            for (int dx = -2; dx <= 2; dx++) {
                for (int dy = -2; dy <= 2; dy++) {
                    if (Math.abs(dx) == 2 || Math.abs(dy) == 2) {
                        int tx = cx + dx;
                        int ty = cy + dy;
                        if (tx >= 0 && tx < GameConfig.MAP_WIDTH && ty >= 0 && ty < GameConfig.MAP_HEIGHT) {
                            map[tx][ty] = new MapTile(tx, ty, GameConfig.TILE_BRICK);
                        }
                    }
                }
            }
        }
    }

    private void clearArea(int centerX, int centerY, int width, int height) {
        int startX = Math.max(2, centerX - width/2);
        int startY = Math.max(2, centerY - height/2);
        int endX = Math.min(GameConfig.MAP_WIDTH-3, centerX + width/2);
        int endY = Math.min(GameConfig.MAP_HEIGHT-3, centerY + height/2);
        for (int x = startX; x <= endX; x++) {
            for (int y = startY; y <= endY; y++) {
                map[x][y] = new MapTile(x, y, GameConfig.TILE_EMPTY);
            }
        }
    }

    public void draw(Graphics g) {
        for (int x = 0; x < GameConfig.MAP_WIDTH; x++) {
            for (int y = 0; y < GameConfig.MAP_HEIGHT; y++) {
                map[x][y].draw(g);
            }
        }
    }

    public boolean isPositionPassable(int pixelX, int pixelY, int width, int height) {
        int tileX1 = pixelX / GameConfig.TILE_SIZE;
        int tileY1 = pixelY / GameConfig.TILE_SIZE;
        int tileX2 = (pixelX + width - 1) / GameConfig.TILE_SIZE;
        int tileY2 = (pixelY + height - 1) / GameConfig.TILE_SIZE;
        for (int x = tileX1; x <= tileX2; x++) {
            for (int y = tileY1; y <= tileY2; y++) {
                if (x < 0 || x >= GameConfig.MAP_WIDTH || y < 0 || y >= GameConfig.MAP_HEIGHT) {
                    return false;
                }
                if (!map[x][y].isPassable()) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean isInGrass(int pixelX, int pixelY) {
        int tileX = (pixelX + GameConfig.TANK_WIDTH/2) / GameConfig.TILE_SIZE;
        int tileY = (pixelY + GameConfig.TANK_HEIGHT/2) / GameConfig.TILE_SIZE;
        if (tileX < 0 || tileX >= GameConfig.MAP_WIDTH || tileY < 0 || tileY >= GameConfig.MAP_HEIGHT) {
            return false;
        }
        return map[tileX][tileY].getType() == GameConfig.TILE_GRASS;
    }

    // 新增：销毁砖墙
    public void destroyTile(int tileX, int tileY) {
        if (tileX >= 0 && tileX < GameConfig.MAP_WIDTH && tileY >= 0 && tileY < GameConfig.MAP_HEIGHT) {
            if (map[tileX][tileY].getType() == GameConfig.TILE_BRICK) {
                map[tileX][tileY] = new MapTile(tileX, tileY, GameConfig.TILE_EMPTY);
            }
        }
    }
}