package main.java.com.tankbattle.world;

import main.java.com.tankbattle.core.ConfigManager;
import java.awt.*;
import java.util.Random;

public class GameWorld {
    public MapTile[][] map;
    private Random random = new Random();

    public GameWorld() {
        ConfigManager config = ConfigManager.getInstance();
        int mapWidth = config.getInt(ConfigManager.KEY_MAP_WIDTH);
        int mapHeight = config.getInt(ConfigManager.KEY_MAP_HEIGHT);

        map = new MapTile[mapWidth][mapHeight];
        generateMap();
    }

    private void generateMap() {
        ConfigManager config = ConfigManager.getInstance();
        int mapWidth = config.getInt(ConfigManager.KEY_MAP_WIDTH);
        int mapHeight = config.getInt(ConfigManager.KEY_MAP_HEIGHT);

        for (int x = 0; x < mapWidth; x++) {
            for (int y = 0; y < mapHeight; y++) {
                map[x][y] = new MapTile(x, y, ConfigManager.TILE_EMPTY);
            }
        }

        for (int x = 0; x < mapWidth; x++) {
            map[x][0] = new MapTile(x, 0, ConfigManager.TILE_STEEL);
            map[x][1] = new MapTile(x, 1, ConfigManager.TILE_BRICK);
            map[x][mapHeight-1] = new MapTile(x, mapHeight-1, ConfigManager.TILE_STEEL);
            map[x][mapHeight-2] = new MapTile(x, mapHeight-2, ConfigManager.TILE_BRICK);
        }

        for (int y = 0; y < mapHeight; y++) {
            map[0][y] = new MapTile(0, y, ConfigManager.TILE_STEEL);
            map[1][y] = new MapTile(1, y, ConfigManager.TILE_BRICK);
            map[mapWidth-1][y] = new MapTile(mapWidth-1, y, ConfigManager.TILE_STEEL);
            map[mapWidth-2][y] = new MapTile(mapWidth-2, y, ConfigManager.TILE_BRICK);
        }

        generateObstacles(ConfigManager.TILE_BRICK, 60);
        generateObstacles(ConfigManager.TILE_STEEL, 15);
        generateObstacles(ConfigManager.TILE_GRASS, 40);
        generateObstacles(ConfigManager.TILE_WATER, 12);
        createSymmetricStructures();
        clearCenterArea();
    }

    private void generateObstacles(int type, int count) {
        ConfigManager config = ConfigManager.getInstance();
        int mapWidth = config.getInt(ConfigManager.KEY_MAP_WIDTH);
        int mapHeight = config.getInt(ConfigManager.KEY_MAP_HEIGHT);

        for (int i = 0; i < count; i++) {
            int x, y;
            int attempts = 0;
            do {
                x = random.nextInt(mapWidth-4) + 2;
                y = random.nextInt(mapHeight-4) + 2;
                attempts++;
                int centerX = mapWidth / 2;
                int centerY = mapHeight / 2;
                if (Math.abs(x - centerX) <= 6 && Math.abs(y - centerY) <= 6) {
                    continue;
                }
            } while (attempts < 100 && map[x][y].getType() != ConfigManager.TILE_EMPTY);

            if (attempts < 100) {
                map[x][y] = new MapTile(x, y, type);
            }
        }
    }

    private void createSymmetricStructures() {
        ConfigManager config = ConfigManager.getInstance();
        int mapWidth = config.getInt(ConfigManager.KEY_MAP_WIDTH);
        int mapHeight = config.getInt(ConfigManager.KEY_MAP_HEIGHT);
        int centerX = mapWidth / 2;
        int centerY = mapHeight / 2;

        int[][] steelPatterns = {
                {centerX - 10, centerY},
                {centerX + 10, centerY},
                {centerX, centerY - 10},
                {centerX, centerY + 10}
        };

        for (int[] pos : steelPatterns) {
            int x = pos[0];
            int y = pos[1];
            if (x >= 0 && x < mapWidth && y >= 0 && y < mapHeight) {
                map[x][y] = new MapTile(x, y, ConfigManager.TILE_STEEL);
            }
        }

        int[][] corners = {
                {5,5},
                {mapWidth-6,5},
                {5,mapHeight-6},
                {mapWidth-6,mapHeight-6}
        };

        for (int[] corner : corners) {
            int cx = corner[0];
            int cy = corner[1];
            for (int dx = -2; dx <= 2; dx++) {
                for (int dy = -2; dy <= 2; dy++) {
                    if (Math.abs(dx) == 2 || Math.abs(dy) == 2) {
                        int tx = cx + dx;
                        int ty = cy + dy;
                        if (tx >= 0 && tx < mapWidth && ty >= 0 && ty < mapHeight) {
                            map[tx][ty] = new MapTile(tx, ty, ConfigManager.TILE_BRICK);
                        }
                    }
                }
            }
        }
    }

    private void clearCenterArea() {
        ConfigManager config = ConfigManager.getInstance();
        int mapWidth = config.getInt(ConfigManager.KEY_MAP_WIDTH);
        int mapHeight = config.getInt(ConfigManager.KEY_MAP_HEIGHT);
        int centerX = mapWidth / 2;
        int centerY = mapHeight / 2;

        for (int dx = -4; dx <= 4; dx++) {
            for (int dy = -4; dy <= 4; dy++) {
                int x = centerX + dx;
                int y = centerY + dy;
                if (x >= 0 && x < mapWidth && y >= 0 && y < mapHeight) {
                    map[x][y] = new MapTile(x, y, ConfigManager.TILE_EMPTY);
                }
            }
        }
    }

    public void draw(Graphics g) {
        ConfigManager config = ConfigManager.getInstance();
        int mapWidth = config.getInt(ConfigManager.KEY_MAP_WIDTH);
        int mapHeight = config.getInt(ConfigManager.KEY_MAP_HEIGHT);

        for (int x = 0; x < mapWidth; x++) {
            for (int y = 0; y < mapHeight; y++) {
                map[x][y].draw(g);
            }
        }
    }

    public boolean isPositionPassable(int pixelX, int pixelY, int width, int height) {
        ConfigManager config = ConfigManager.getInstance();
        int tileSize = config.getInt(ConfigManager.KEY_TILE_SIZE);
        int mapWidth = config.getInt(ConfigManager.KEY_MAP_WIDTH);
        int mapHeight = config.getInt(ConfigManager.KEY_MAP_HEIGHT);

        int tileX1 = pixelX / tileSize;
        int tileY1 = pixelY / tileSize;
        int tileX2 = (pixelX + width - 1) / tileSize;
        int tileY2 = (pixelY + height - 1) / tileSize;

        for (int x = tileX1; x <= tileX2; x++) {
            for (int y = tileY1; y <= tileY2; y++) {
                if (x < 0 || x >= mapWidth || y < 0 || y >= mapHeight) {
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
        ConfigManager config = ConfigManager.getInstance();
        int tileSize = config.getInt(ConfigManager.KEY_TILE_SIZE);
        int mapWidth = config.getInt(ConfigManager.KEY_MAP_WIDTH);
        int mapHeight = config.getInt(ConfigManager.KEY_MAP_HEIGHT);
        int tankWidth = config.getInt(ConfigManager.KEY_TANK_WIDTH);
        int tankHeight = config.getInt(ConfigManager.KEY_TANK_HEIGHT);

        int tileX = (pixelX + tankWidth/2) / tileSize;
        int tileY = (pixelY + tankHeight/2) / tileSize;
        if (tileX < 0 || tileX >= mapWidth || tileY < 0 || tileY >= mapHeight) {
            return false;
        }
        return map[tileX][tileY].getType() == ConfigManager.TILE_GRASS;
    }

    public void destroyTile(int tileX, int tileY) {
        ConfigManager config = ConfigManager.getInstance();
        int mapWidth = config.getInt(ConfigManager.KEY_MAP_WIDTH);
        int mapHeight = config.getInt(ConfigManager.KEY_MAP_HEIGHT);

        if (tileX >= 0 && tileX < mapWidth && tileY >= 0 && tileY < mapHeight) {
            if (map[tileX][tileY].getType() == ConfigManager.TILE_BRICK) {
                map[tileX][tileY] = new MapTile(tileX, tileY, ConfigManager.TILE_EMPTY);
            }
        }
    }
}