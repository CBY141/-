package main.java.com.tankbattle.entity;

import main.java.com.tankbattle.core.ConfigManager;
import java.awt.*;

public class Bullet {
    private int x, y;
    private int direction;
    private boolean alive = true;
    private Color color;
    private boolean fromPlayer;

    public Bullet(int x, int y, int direction, boolean fromPlayer) {
        this.x = x;
        this.y = y;
        this.direction = direction;
        this.fromPlayer = fromPlayer;
        this.color = fromPlayer ? new Color(255, 255, 0) : new Color(255, 100, 0);
    }

    public void update() {
        ConfigManager config = ConfigManager.getInstance();
        int bulletSpeed = config.getInt(ConfigManager.KEY_BULLET_SPEED);
        int windowWidth = config.getInt(ConfigManager.KEY_WINDOW_WIDTH);
        int windowHeight = config.getInt(ConfigManager.KEY_WINDOW_HEIGHT);

        switch (direction) {
            case ConfigManager.DIR_UP: y -= bulletSpeed; break;
            case ConfigManager.DIR_DOWN: y += bulletSpeed; break;
            case ConfigManager.DIR_LEFT: x -= bulletSpeed; break;
            case ConfigManager.DIR_RIGHT: x += bulletSpeed; break;
        }
        if (x < 0 || x > windowWidth || y < 0 || y > windowHeight) {
            alive = false;
        }
    }

    public void draw(Graphics g) {
        if (!alive) return;

        ConfigManager config = ConfigManager.getInstance();
        int bulletSize = config.getInt(ConfigManager.KEY_BULLET_SIZE);

        g.setColor(color);
        g.fillOval(x - bulletSize/2, y - bulletSize/2,
                bulletSize, bulletSize);
        g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 100));
        g.fillOval(x - bulletSize, y - bulletSize,
                bulletSize * 2, bulletSize * 2);
        g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 150));
        switch (direction) {
            case ConfigManager.DIR_UP: g.fillOval(x - 1, y + 3, 2, 4); break;
            case ConfigManager.DIR_DOWN: g.fillOval(x - 1, y - 7, 2, 4); break;
            case ConfigManager.DIR_LEFT: g.fillOval(x + 3, y - 1, 4, 2); break;
            case ConfigManager.DIR_RIGHT: g.fillOval(x - 7, y - 1, 4, 2); break;
        }
    }

    public boolean isAlive() { return alive; }
    public void setAlive(boolean alive) { this.alive = alive; }
    public boolean isFromPlayer() { return fromPlayer; }
    public int getX() { return x; }
    public int getY() { return y; }

    public boolean collidesWith(int targetX, int targetY, int targetSize) {
        int bulletCenterX = x;
        int bulletCenterY = y;
        int targetCenterX = targetX + targetSize / 2;
        int targetCenterY = targetY + targetSize / 2;
        int dx = bulletCenterX - targetCenterX;
        int dy = bulletCenterY - targetCenterY;
        int distance = (int)Math.sqrt(dx*dx + dy*dy);

        ConfigManager config = ConfigManager.getInstance();
        int bulletSize = config.getInt(ConfigManager.KEY_BULLET_SIZE);

        return distance < (bulletSize/2 + targetSize/2);
    }
}