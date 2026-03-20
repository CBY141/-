import java.awt.*;

public class Bullet {
    private int x, y;
    private int direction;
    private boolean alive = true;

    public Bullet(int x, int y, int direction) {
        this.x = x;
        this.y = y;
        this.direction = direction;
    }


    
    public void update() {
        switch (direction) {
            case GameConfig.DIR_UP: y -= GameConfig.BULLET_SPEED; break;
            case GameConfig.DIR_DOWN: y += GameConfig.BULLET_SPEED; break;
            case GameConfig.DIR_LEFT: x -= GameConfig.BULLET_SPEED; break;
            case GameConfig.DIR_RIGHT: x += GameConfig.BULLET_SPEED; break;
        }

        if (x < 0 || x > GameConfig.WINDOW_WIDTH || y < 0 || y > GameConfig.WINDOW_HEIGHT) {
            alive = false;
        }
    }
    public void draw(Graphics g) {
        if (!alive) return;
        g.setColor(Color.YELLOW);
        g.fillRect(x - 2, y - 2, 4, 4);
    }
    public boolean isAlive() { return alive; }
    public void setAlive(boolean alive) { this.alive = alive; }

    public boolean collidesWith(int targetX, int targetY, int targetSize) {
        return x >= targetX && x <= targetX + targetSize &&
                y >= targetY && y <= targetY + targetSize;
    }
}
