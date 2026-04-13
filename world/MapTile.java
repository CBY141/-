package main.java.com.tankbattle.world;

import main.java.com.tankbattle.core.ConfigManager;
import java.awt.*;

public class MapTile {
    private int x, y;
    private int type;
    private boolean passable;

    public MapTile(int x, int y, int type) {
        this.x = x;
        this.y = y;
        this.type = type;
        switch (type) {
            case ConfigManager.TILE_BRICK:
            case ConfigManager.TILE_STEEL:
            case ConfigManager.TILE_WATER:
                passable = false;
                break;
            default:
                passable = true;
        }
    }

    public void draw(Graphics g) {
        ConfigManager config = ConfigManager.getInstance();
        int tileSize = config.getInt(ConfigManager.KEY_TILE_SIZE);

        int pixelX = x * tileSize;
        int pixelY = y * tileSize;

        switch (type) {
            case ConfigManager.TILE_BRICK:
                g.setColor(new Color(160, 80, 40));
                g.fillRect(pixelX, pixelY, tileSize, tileSize);
                g.setColor(new Color(120, 60, 20));
                g.drawRect(pixelX, pixelY, tileSize, tileSize);
                g.drawLine(pixelX + tileSize/2, pixelY, pixelX + tileSize/2, pixelY + tileSize);
                g.drawLine(pixelX, pixelY + tileSize/2, pixelX + tileSize, pixelY + tileSize/2);
                break;
            case ConfigManager.TILE_STEEL:
                g.setColor(new Color(120, 120, 120));
                g.fillRect(pixelX, pixelY, tileSize, tileSize);
                g.setColor(Color.DARK_GRAY);
                for (int i = 2; i < tileSize; i += 4) {
                    for (int j = 2; j < tileSize; j += 4) {
                        g.fillRect(pixelX + i, pixelY + j, 2, 2);
                    }
                }
                break;
            case ConfigManager.TILE_GRASS:
                g.setColor(new Color(40, 160, 40, 180));
                g.fillRect(pixelX, pixelY, tileSize, tileSize);
                g.setColor(new Color(20, 120, 20));
                for (int i = 0; i < tileSize; i += 3) {
                    // 【核心修复】利用瓦片坐标和线条索引生成固定的伪随机数，代替 random.nextInt()
                    int pseudoRandom = (this.x * 73 + this.y * 31 + i * 17) % 4;
                    int height = 2 + Math.abs(pseudoRandom);
                    g.drawLine(pixelX + i, pixelY + tileSize,
                            pixelX + i, pixelY + tileSize - height);
                }
                break;
            case ConfigManager.TILE_WATER:
                g.setColor(new Color(30, 120, 220, 120));
                g.fillRect(pixelX, pixelY, tileSize, tileSize);
                g.setColor(new Color(60, 150, 255, 180));
                for (int i = 0; i < tileSize; i += 8) {
                    g.drawArc(pixelX + i, pixelY + 3, 6, 3, 0, 180);
                }
                break;
        }
    }

    public int getType() { return type; }
    public boolean isPassable() { return passable; }
}