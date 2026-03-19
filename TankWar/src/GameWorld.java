import java.awt.*;
import java.util.Random;

public class GameWorld {
    private MapTile[][] map;
    private Random random = new Random();

    public GameWorld() {
        map = new MapTile[GameConfig.MAP_WIDTH][GameConfig.MAP_HEIGHT];
        generateMap();
    }

    private void generateMap() {
        for (int x = 0; x < GameConfig.MAP_WIDTH; x++) {
            for (int y = 0; y < GameConfig.MAP_HEIGHT; y++) {
                map[x][y] = new MapTile(x, y, GameConfig.TILE_EMPTY);
            }
        }

        for (int x = 0; x < GameConfig.MAP_WIDTH; x++) {
            map[x][0] = new MapTile(x, 0, GameConfig.TILE_STEEL);
            map[x][GameConfig.MAP_HEIGHT-1] = new MapTile(x, GameConfig.MAP_HEIGHT-1, GameConfig.TILE_STEEL);
        }
        for (int y = 0; y < GameConfig.MAP_HEIGHT; y++) {
            map[0][y] = new MapTile(0, y, GameConfig.TILE_STEEL);
            map[GameConfig.MAP_WIDTH-1][y] = new MapTile(GameConfig.MAP_WIDTH-1, y, GameConfig.TILE_STEEL);
        }

        generateObstacles(GameConfig.TILE_BRICK, 30);
        generateObstacles(GameConfig.TILE_STEEL, 10);
        generateObstacles(GameConfig.TILE_GRASS, 20);
        generateObstacles(GameConfig.TILE_WATER, 8);
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

        return map[tileX1][tileY1].isPassable() &&
                map[tileX1][tileY2].isPassable() &&
                map[tileX2][tileY1].isPassable() &&
                map[tileX2][tileY2].isPassable();
    }

    public boolean isInGrass(int pixelX, int pixelY) {
        int tileX = (pixelX + 15) / GameConfig.TILE_SIZE;
        int tileY = (pixelY + 15) / GameConfig.TILE_SIZE;

        if (tileX < 0 || tileX >= GameConfig.MAP_WIDTH || tileY < 0 || tileY >= GameConfig.MAP_HEIGHT) {
            return false;
        }
        return map[tileX][tileY].getType() == GameConfig.TILE_GRASS;
    }
}