package main.java.com.tankbattle.entity;

import main.java.com.tankbattle.core.ConfigManager;
import java.awt.*;

public class Bullet {
    private int x, y;
    private int direction;
    private boolean alive = true;
    private Color color;
    private boolean fromPlayer;

    private boolean penetrating = false;
    private boolean special = false;
    private int damage = 1;

    private int bulletSpeed;
    private int bulletSize;
    private int windowWidth;
    private int windowHeight;

    public Bullet(int x, int y, int direction, boolean fromPlayer) {
        reset(x, y, direction, fromPlayer);
    }

    public void reset(int x, int y, int direction, boolean fromPlayer) {
        this.x = x;
        this.y = y;
        this.direction = direction;
        this.fromPlayer = fromPlayer;
        this.alive = true;
        this.penetrating = false;
        this.special = false;
        this.damage = 1;
        this.color = fromPlayer ? new Color(255, 255, 0) : new Color(255, 100, 0);

        ConfigManager config = ConfigManager.getInstance();
        this.bulletSpeed = config.getInt(ConfigManager.KEY_BULLET_SPEED);
        this.bulletSize = config.getInt(ConfigManager.KEY_BULLET_SIZE);
        this.windowWidth = config.getInt(ConfigManager.KEY_WINDOW_WIDTH);
        this.windowHeight = config.getInt(ConfigManager.KEY_WINDOW_HEIGHT);
    }

    // 新版更新方法，接收时间缩放因子
    public void update(float deltaTime) {
        int effectiveSpeed = Math.round(bulletSpeed * deltaTime * 60); // 保持与帧率大致同步
        if (effectiveSpeed < 1) effectiveSpeed = 1;

        switch (direction) {
            case ConfigManager.DIR_UP:    y -= effectiveSpeed; break;
            case ConfigManager.DIR_DOWN:  y += effectiveSpeed; break;
            case ConfigManager.DIR_LEFT:  x -= effectiveSpeed; break;
            case ConfigManager.DIR_RIGHT: x += effectiveSpeed; break;
        }
        if (x < 0 || x > windowWidth || y < 0 || y > windowHeight) {
            alive = false;
        }
    }

    // 兼容旧版调用（无参数）
    public void update() {
        update(1.0f);
    }

    public void draw(Graphics g) {
        if (!alive) return;

        Color drawColor = special ? new Color(255, 0, 255) : color;
        g.setColor(drawColor);
        g.fillOval(x - bulletSize/2, y - bulletSize/2, bulletSize, bulletSize);
        g.setColor(new Color(drawColor.getRed(), drawColor.getGreen(), drawColor.getBlue(), 100));
        g.fillOval(x - bulletSize, y - bulletSize, bulletSize * 2, bulletSize * 2);
        g.setColor(new Color(drawColor.getRed(), drawColor.getGreen(), drawColor.getBlue(), 150));
        switch (direction) {
            case ConfigManager.DIR_UP:    g.fillOval(x - 1, y + 3, 2, 4); break;
            case ConfigManager.DIR_DOWN:  g.fillOval(x - 1, y - 7, 2, 4); break;
            case ConfigManager.DIR_LEFT:  g.fillOval(x + 3, y - 1, 4, 2); break;
            case ConfigManager.DIR_RIGHT: g.fillOval(x - 7, y - 1, 4, 2); break;
        }
    }

    public boolean isAlive() { return alive; }
    public void setAlive(boolean alive) { this.alive = alive; }
    public boolean isFromPlayer() { return fromPlayer; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getDirection() { return direction; }
    public boolean isPenetrating() { return penetrating; }
    public void setPenetrating(boolean p) { this.penetrating = p; }
    public boolean isSpecial() { return special; }
    public void setSpecial(boolean s) { this.special = s; }
    public int getDamage() { return damage; }
    public void setDamage(int d) { this.damage = d; }

    public boolean collidesWith(int targetX, int targetY, int targetSize) {
        int bulletCenterX = x;
        int bulletCenterY = y;
        int targetCenterX = targetX + targetSize / 2;
        int targetCenterY = targetY + targetSize / 2;
        int dx = bulletCenterX - targetCenterX;
        int dy = bulletCenterY - targetCenterY;
        int distance = (int)Math.sqrt(dx*dx + dy*dy);
        return distance < (bulletSize/2 + targetSize/2);
    }
}