import java.awt.*;
import java.util.Random;

public class MapTile {
    private int x, y;
    private int type;
    private boolean passable;
    private Random random = new Random();

    public MapTile(int x, int y, int type) {
        this.x = x;
        this.y = y;
        this.type = type;
        switch (type) {
            case GameConfig.TILE_BRICK:
            case GameConfig.TILE_STEEL:
            case GameConfig.TILE_WATER:
                passable = false;
                break;
            default:
                passable = true;
        }
    }

    public void draw(Graphics g) {
        int pixelX = x * GameConfig.TILE_SIZE;
        int pixelY = y * GameConfig.TILE_SIZE;
        switch (type) {
            case GameConfig.TILE_BRICK:
                g.setColor(new Color(160, 80, 40));
                g.fillRect(pixelX, pixelY, GameConfig.TILE_SIZE, GameConfig.TILE_SIZE);
                g.setColor(new Color(120, 60, 20));
                g.drawRect(pixelX, pixelY, GameConfig.TILE_SIZE, GameConfig.TILE_SIZE);
                g.drawLine(pixelX + GameConfig.TILE_SIZE/2, pixelY,
                        pixelX + GameConfig.TILE_SIZE/2, pixelY + GameConfig.TILE_SIZE);
                g.drawLine(pixelX, pixelY + GameConfig.TILE_SIZE/2,
                        pixelX + GameConfig.TILE_SIZE, pixelY + GameConfig.TILE_SIZE/2);
                break;
            case GameConfig.TILE_STEEL:
                g.setColor(new Color(120, 120, 120));
                g.fillRect(pixelX, pixelY, GameConfig.TILE_SIZE, GameConfig.TILE_SIZE);
                g.setColor(Color.DARK_GRAY);
                for (int i = 2; i < GameConfig.TILE_SIZE; i += 4) {
                    for (int j = 2; j < GameConfig.TILE_SIZE; j += 4) {
                        g.fillRect(pixelX + i, pixelY + j, 2, 2);
                    }
                }
                break;
            case GameConfig.TILE_GRASS:
                g.setColor(new Color(40, 160, 40, 180));
                g.fillRect(pixelX, pixelY, GameConfig.TILE_SIZE, GameConfig.TILE_SIZE);
                g.setColor(new Color(20, 120, 20));
                for (int i = 0; i < GameConfig.TILE_SIZE; i += 3) {
                    int height = 2 + random.nextInt(4);
                    g.drawLine(pixelX + i, pixelY + GameConfig.TILE_SIZE,
                            pixelX + i, pixelY + GameConfig.TILE_SIZE - height);
                }
                break;
            case GameConfig.TILE_WATER:
                g.setColor(new Color(30, 120, 220, 120));
                g.fillRect(pixelX, pixelY, GameConfig.TILE_SIZE, GameConfig.TILE_SIZE);
                g.setColor(new Color(60, 150, 255, 180));
                for (int i = 0; i < GameConfig.TILE_SIZE; i += 8) {
                    g.drawArc(pixelX + i, pixelY + 3, 6, 3, 0, 180);
                }
                break;
        }
    }

    public int getType() { return type; }
    public boolean isPassable() { return passable; }
}