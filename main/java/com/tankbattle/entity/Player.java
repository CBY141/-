package main.java.com.tankbattle.entity;

import main.java.com.tankbattle.core.ConfigManager;
import main.java.com.tankbattle.world.GameWorld;
import java.awt.*;
import java.util.List;

public class Player {
    public int x, y;
    public int lives;
    public boolean inGrass = false;
    public boolean dead = false;
    private int shootCooldown = 0;
    private static final int SHOOT_COOLDOWN_TIME = 20;

    // 记录初始位置，用于重置
    private int startX, startY;

    public Player(int startX, int startY) {
        ConfigManager config = ConfigManager.getInstance();
        this.lives = config.getInt(ConfigManager.KEY_PLAYER_LIVES);
        this.x = startX;
        this.y = startY;
        this.startX = startX;
        this.startY = startY;
    }

    // 新增：重置玩家状态
    public void reset() {
        ConfigManager config = ConfigManager.getInstance();
        this.x = startX;
        this.y = startY;
        this.lives = config.getInt(ConfigManager.KEY_PLAYER_LIVES);
        this.inGrass = false;
        this.dead = false;
        this.shootCooldown = 0;
    }

    public void update(boolean up, boolean down, boolean left, boolean right, boolean shoot,
                       int mouseX, int mouseY, GameWorld world, List<Bullet> bullets) {
        if (dead) return;

        ConfigManager config = ConfigManager.getInstance();
        int playerSpeed = config.getInt(ConfigManager.KEY_PLAYER_SPEED);
        int tankWidth = config.getInt(ConfigManager.KEY_TANK_WIDTH);
        int tankHeight = config.getInt(ConfigManager.KEY_TANK_HEIGHT);
        int windowWidth = config.getInt(ConfigManager.KEY_WINDOW_WIDTH);
        int windowHeight = config.getInt(ConfigManager.KEY_WINDOW_HEIGHT);

        int newX = x;
        int newY = y;
        if (up) newY -= playerSpeed;
        if (down) newY += playerSpeed;
        if (left) newX -= playerSpeed;
        if (right) newX += playerSpeed;

        int originalX = x;
        int originalY = y;

        x = newX;
        y = newY;

        boolean positionValid = world.isPositionPassable(x, y, tankWidth, tankHeight);
        boolean inBounds = (x >= 0 && x <= windowWidth - tankWidth &&
                y >= 0 && y <= windowHeight - tankHeight);

        if (!positionValid || !inBounds) {
            x = originalX;
            y = originalY;

            if (world.isPositionPassable(newX, originalY, tankWidth, tankHeight) &&
                    newX >= 0 && newX <= windowWidth - tankWidth) {
                x = newX;
            }
            if (world.isPositionPassable(originalX, newY, tankWidth, tankHeight) &&
                    newY >= 0 && newY <= windowHeight - tankHeight) {
                y = newY;
            }
        }

        inGrass = world.isInGrass(x, y);

        if (shoot && shootCooldown == 0) {
            int bulletX = x + tankWidth / 2;
            int bulletY = y + tankHeight / 2;
            double angle = Math.atan2(mouseY - bulletY, mouseX - bulletX);
            int direction;
            if (Math.abs(angle) < Math.PI/4) direction = ConfigManager.DIR_RIGHT;
            else if (Math.abs(angle) > 3*Math.PI/4) direction = ConfigManager.DIR_LEFT;
            else if (angle > 0) direction = ConfigManager.DIR_DOWN;
            else direction = ConfigManager.DIR_UP;

            bullets.add(new Bullet(bulletX, bulletY, direction, true));
            shootCooldown = SHOOT_COOLDOWN_TIME;
        }

        if (shootCooldown > 0) shootCooldown--;
    }

    public void draw(Graphics g, int mouseX, int mouseY) {
        ConfigManager config = ConfigManager.getInstance();
        int tankWidth = config.getInt(ConfigManager.KEY_TANK_WIDTH);
        int tankHeight = config.getInt(ConfigManager.KEY_TANK_HEIGHT);
        int turretLength = config.getInt(ConfigManager.KEY_TANK_TURRET_LENGTH);

        int centerX = x + tankWidth / 2;
        int centerY = y + tankHeight / 2;
        Color tankColor = ConfigManager.PLAYER_TANK_COLOR;
        Color detailColor = ConfigManager.TANK_DETAIL_COLOR;
        Color turretColor = ConfigManager.TANK_TURRET_COLOR;

        if (inGrass) {
            tankColor = new Color(tankColor.getRed(), tankColor.getGreen(), tankColor.getBlue(), 100);
            detailColor = new Color(detailColor.getRed(), detailColor.getGreen(), detailColor.getBlue(), 100);
            turretColor = new Color(turretColor.getRed(), turretColor.getGreen(), turretColor.getBlue(), 100);
        }
        drawTankBody(g, x, y, tankColor, detailColor);
        drawTankTurret(g, centerX, centerY, mouseX, mouseY, turretColor, turretLength);
        g.setColor(Color.WHITE);
        g.fillPolygon(new int[]{centerX, centerX - 3, centerX + 3},
                new int[]{y + 2, y + 8, y + 8}, 3);
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

    private void drawTankTurret(Graphics g, int centerX, int centerY, int targetX, int targetY, Color turretColor, int turretLength) {
        double angle = Math.atan2(targetY - centerY, targetX - centerX);
        int turretEndX = centerX + (int)(turretLength * Math.cos(angle));
        int turretEndY = centerY + (int)(turretLength * Math.sin(angle));
        g.setColor(turretColor);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setStroke(new BasicStroke(3));
        g2d.drawLine(centerX, centerY, turretEndX, turretEndY);
        g2d.setStroke(new BasicStroke(1));
        g.setColor(turretColor.darker());
        g.fillOval(centerX - 4, centerY - 4, 8, 8);
    }
}