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
                g.setColor(new Color(139, 69, 19));
                g.fillRect(pixelX, pixelY, GameConfig.TILE_SIZE, GameConfig.TILE_SIZE);
                break;
            case GameConfig.TILE_STEEL:
                g.setColor(Color.GRAY);
                g.fillRect(pixelX, pixelY, GameConfig.TILE_SIZE, GameConfig.TILE_SIZE);
                break;
            case GameConfig.TILE_GRASS:
                g.setColor(new Color(34, 139, 34, 150));
                g.fillRect(pixelX, pixelY, GameConfig.TILE_SIZE, GameConfig.TILE_SIZE);
                break;
            case GameConfig.TILE_WATER:
                g.setColor(new Color(30, 144, 255, 100));
                g.fillRect(pixelX, pixelY, GameConfig.TILE_SIZE, GameConfig.TILE_SIZE);
                break;
        }
    }

    public int getType() { return type; }
    public boolean isPassable() { return passable; }
}