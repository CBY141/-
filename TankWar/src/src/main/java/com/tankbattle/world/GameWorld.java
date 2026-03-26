package main.java.com.tankbattle.world;

import main.java.com.tankbattle.core.GameConfig;
import java.awt.*;
import java.util.Random;

public class GameWorld {
    public MapTile[][] map;
    private Random random = new Random();

    public GameWorld() {
        map = new MapTile[GameConfig.MAP_WIDTH][GameConfig.MAP_HEIGHT];
        generateMap();
    }

    private void generateMap() {
        // 1. 初始化所有格子为空地
        for (int x = 0; x < GameConfig.MAP_WIDTH; x++) {
            for (int y = 0; y < GameConfig.MAP_HEIGHT; y++) {
                map[x][y] = new MapTile(x, y, GameConfig.TILE_EMPTY);
            }
        }

        // 2. 创建四周的钢铁和砖墙边界
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

        // 3. 生成随机障碍物
        generateObstacles(GameConfig.TILE_BRICK, 60);
        generateObstacles(GameConfig.TILE_STEEL, 15);
        generateObstacles(GameConfig.TILE_GRASS, 40);
        generateObstacles(GameConfig.TILE_WATER, 12);

        // 4. 创建对称结构
        createSymmetricStructures();

        // 5. 确保中心区域是空的（玩家出生区域）
        clearCenterArea();
    }

    // 新增：专门清理中心区域的方法
    private void clearCenterArea() {
        int centerX = GameConfig.MAP_WIDTH / 2;
        int centerY = GameConfig.MAP_HEIGHT / 2;

        // 清理一个9x9的区域，确保玩家出生点安全
        for (int dx = -4; dx <= 4; dx++) {
            for (int dy = -4; dy <= 4; dy++) {
                int x = centerX + dx;
                int y = centerY + dy;
                if (x >= 0 && x < GameConfig.MAP_WIDTH && y >= 0 && y < GameConfig.MAP_HEIGHT) {
                    // 移除中心的钢铁十字
                    map[x][y] = new MapTile(x, y, GameConfig.TILE_EMPTY);
                }
            }
        }
    }

    private void generateObstacles(int type, int count) {
        for (int i = 0; i < count; i++) {
            int x, y;
            int attempts = 0;
            do {
                x = random.nextInt(GameConfig.MAP_WIDTH-4) + 2;
                y = random.nextInt(GameConfig.MAP_HEIGHT-4) + 2;
                attempts++;
                // 避免在中心区域生成障碍物
                int centerX = GameConfig.MAP_WIDTH / 2;
                int centerY = GameConfig.MAP_HEIGHT / 2;
                if (Math.abs(x - centerX) <= 6 && Math.abs(y - centerY) <= 6) {
                    continue; // 跳过中心区域
                }
            } while (attempts < 100 && map[x][y].getType() != GameConfig.TILE_EMPTY);

            if (attempts < 100) {
                map[x][y] = new MapTile(x, y, type);
            }
        }
    }

    private void createSymmetricStructures() {
        int centerX = GameConfig.MAP_WIDTH / 2;
        int centerY = GameConfig.MAP_HEIGHT / 2;

        // 不再在中心创建钢铁十字，而是创建远离中心的装饰
        int[][] steelPatterns = {
                {centerX - 10, centerY},
                {centerX + 10, centerY},
                {centerX, centerY - 10},
                {centerX, centerY + 10}
        };

        for (int[] pos : steelPatterns) {
            int x = pos[0];
            int y = pos[1];
            if (x >= 0 && x < GameConfig.MAP_WIDTH && y >= 0 && y < GameConfig.MAP_HEIGHT) {
                map[x][y] = new MapTile(x, y, GameConfig.TILE_STEEL);
            }
        }

        int[][] corners = {
                {5,5},
                {GameConfig.MAP_WIDTH-6,5},
                {5,GameConfig.MAP_HEIGHT-6},
                {GameConfig.MAP_WIDTH-6,GameConfig.MAP_HEIGHT-6}
        };

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

    public void destroyTile(int tileX, int tileY) {
        if (tileX >= 0 && tileX < GameConfig.MAP_WIDTH && tileY >= 0 && tileY < GameConfig.MAP_HEIGHT) {
            if (map[tileX][tileY].getType() == GameConfig.TILE_BRICK) {
                map[tileX][tileY] = new MapTile(tileX, tileY, GameConfig.TILE_EMPTY);
            }
        }
    }
}