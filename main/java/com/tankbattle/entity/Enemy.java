package main.java.com.tankbattle.entity;

import main.java.com.tankbattle.core.ConfigManager;
import main.java.com.tankbattle.world.GameWorld;
import java.awt.*;
import java.util.List;
import java.util.Random;

public class Enemy {
    public int x, y;
    public int direction;
    public boolean alive = true;
    private int moveTimer = 0;
    private int shootTimer = 0;
    private Random random = new Random();

    public Enemy(int x, int y) {
        this.x = x;
        this.y = y;
        this.direction = random.nextInt(4);
    }

    public void update(GameWorld gameWorld, List<Bullet> bullets) {
        if (!alive) return;

        ConfigManager config = ConfigManager.getInstance();
        int enemySpeed = config.getInt(ConfigManager.KEY_ENEMY_SPEED);
        int tankWidth = config.getInt(ConfigManager.KEY_TANK_WIDTH);
        int tankHeight = config.getInt(ConfigManager.KEY_TANK_HEIGHT);

        moveTimer++;
        shootTimer++;
        if (moveTimer > 60) {
            direction = random.nextInt(4);
            moveTimer = 0;
        }
        int newX = x;
        int newY = y;
        switch (direction) {
            case ConfigManager.DIR_UP: newY -= enemySpeed; break;
            case ConfigManager.DIR_DOWN: newY += enemySpeed; break;
            case ConfigManager.DIR_LEFT: newX -= enemySpeed; break;
            case ConfigManager.DIR_RIGHT: newX += enemySpeed; break;
        }
        if (gameWorld.isPositionPassable(newX, newY, tankWidth, tankHeight)) {
            x = newX;
            y = newY;
        } else {
            direction = random.nextInt(4);
        }
        if (shootTimer > 120 && random.nextInt(100) < 5) {
            int bulletX = x + tankWidth / 2;
            int bulletY = y + tankHeight / 2;
            bullets.add(new Bullet(bulletX, bulletY, direction, false));
            shootTimer = 0;
        }
    }

    public void draw(Graphics g) {
        if (!alive) return;

        ConfigManager config = ConfigManager.getInstance();
        int tankWidth = config.getInt(ConfigManager.KEY_TANK_WIDTH);
        int tankHeight = config.getInt(ConfigManager.KEY_TANK_HEIGHT);
        int turretLength = config.getInt(ConfigManager.KEY_TANK_TURRET_LENGTH);

        int centerX = x + tankWidth / 2;
        int centerY = y + tankHeight / 2;
        drawTankBody(g, x, y, ConfigManager.ENEMY_TANK_COLOR, ConfigManager.TANK_DETAIL_COLOR);
        Color turretColor = ConfigManager.TANK_TURRET_COLOR;
        int turretEndX = centerX, turretEndY = centerY;
        switch (direction) {
            case ConfigManager.DIR_UP: turretEndY = centerY - turretLength; break;
            case ConfigManager.DIR_DOWN: turretEndY = centerY + turretLength; break;
            case ConfigManager.DIR_LEFT: turretEndX = centerX - turretLength; break;
            case ConfigManager.DIR_RIGHT: turretEndX = centerX + turretLength; break;
        }
        g.setColor(turretColor);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setStroke(new BasicStroke(3));
        g2d.drawLine(centerX, centerY, turretEndX, turretEndY);
        g2d.setStroke(new BasicStroke(1));
        g.setColor(turretColor.darker());
        g.fillOval(centerX - 4, centerY - 4, 8, 8);
        g.setColor(Color.RED);
        g.fillRect(centerX - 2, y + 2, 4, 4);
    }

    private void drawTankBody(Graphics g, int x, int y, Color mainColor, Color detailColor) {
        ConfigManager config = ConfigManager.getInstance();
        int tankWidth = config.getInt(ConfigManager.KEY_TANK_WIDTH);
        int tankHeight = config.getInt(ConfigManager.KEY_TANK_HEIGHT);

        g.setColor(mainColor);
        g.fillRect(x + 2, y + 2, tankWidth - 4, tankHeight - 4);
        g.setColor(detailColor);
        g.fillRect(x, y, tankWidth, 3);
        g.fillRect(x, y + tankHeight - 3, tankWidth, 3);
        g.fillRect(x, y, 3, tankHeight);
        g.fillRect(x + tankWidth - 3, y, 3, tankHeight);
        g.setColor(mainColor.darker());
        g.fillRect(x + 6, y + 6, tankWidth - 12, tankHeight - 12);
        g.setColor(Color.BLACK);
        g.drawRect(x + 6, y + 6, tankWidth - 12, tankHeight - 12);
    }

    public boolean collidesWith(int targetX, int targetY) {
        ConfigManager config = ConfigManager.getInstance();
        int tankWidth = config.getInt(ConfigManager.KEY_TANK_WIDTH);

        int thisCenterX = x + tankWidth / 2;
        int thisCenterY = y + tankWidth / 2;  // 注意：这里使用tankWidth，因为坦克是正方形
        int targetCenterX = targetX + tankWidth / 2;
        int targetCenterY = targetY + tankWidth / 2;
        int dx = thisCenterX - targetCenterX;
        int dy = thisCenterY - targetCenterY;
        int distance = (int)Math.sqrt(dx*dx + dy*dy);
        int collisionDistance = tankWidth - 2;
        return distance < collisionDistance;
    }
}