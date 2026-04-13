package main.java.com.tankbattle.entity;

import main.java.com.tankbattle.core.*;
import main.java.com.tankbattle.managers.EventManager;
import main.java.com.tankbattle.system.*;
import main.java.com.tankbattle.world.GameWorld;
import main.java.com.tankbattle.game.factory.GameObjectFactory;
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

    private int lastX = 0, lastY = 0;
    private int stuckFrames = 0;
    private int aiState = 0;
    private int aiTimer = 0;
    private int aggression = 0;

    private TransformComponent transform = null;
    private HealthComponent health = null;
    private RenderComponent render = null;
    private boolean useComponents = false;

    public Enemy(int x, int y) {
        reset(x, y);
    }

    public void reset(int x, int y) {
        this.x = x;
        this.y = y;
        this.direction = random.nextInt(4);
        this.lastX = x;
        this.lastY = y;
        this.aggression = random.nextInt(100);
        this.alive = true;
        this.moveTimer = 0;
        this.shootTimer = 0;
        this.aiTimer = 0;
        this.stuckFrames = 0;
        this.aiState = 0;

        if (useComponents && transform != null && health != null) {
            transform.setPosition(x, y);
            health.setCurrentHealth(100);
        }
    }

    public void enableComponents() {
        useComponents = true;
        transform = new TransformComponent();
        health = new HealthComponent();
        render = new RenderComponent();
        ConfigManager config = ConfigManager.getInstance();
        transform.setPosition(x, y);
        transform.setSize(config.getInt(ConfigManager.KEY_TANK_WIDTH), config.getInt(ConfigManager.KEY_TANK_HEIGHT));
        health.setEntity(new Entity("Enemy_Entity"));
        health.setMaxHealth(100);
        health.setCurrentHealth(100);
        render.setColor(ConfigManager.ENEMY_TANK_COLOR);
        render.setVisible(true);
    }

    public int getX() { return (useComponents && transform != null) ? transform.getX() : x; }
    public int getY() { return (useComponents && transform != null) ? transform.getY() : y; }
    public int getHealth() { return (useComponents && health != null) ? health.getCurrentHealth() : (alive ? 100 : 0); }

    public void update(GameWorld gameWorld, List<Bullet> bullets) {
        if (!alive) return;

        ConfigManager config = ConfigManager.getInstance();
        int enemySpeed = config.getInt(ConfigManager.KEY_ENEMY_SPEED);
        int tankWidth = config.getInt(ConfigManager.KEY_TANK_WIDTH);
        int tankHeight = config.getInt(ConfigManager.KEY_TANK_HEIGHT);

        moveTimer++;
        shootTimer++;
        aiTimer++;

        int currentX = getX();
        int currentY = getY();

        if (currentX == lastX && currentY == lastY) {
            stuckFrames++;
            if (stuckFrames > 20) {
                direction = random.nextInt(4);
                stuckFrames = 0;
            }
        } else {
            stuckFrames = 0;
        }

        lastX = currentX;
        lastY = currentY;

        if (aiTimer > 60 + random.nextInt(60)) {
            aiState = random.nextInt(100) < aggression ? 1 : 0;
            aiTimer = 0;
        }

        if (moveTimer > 60 + random.nextInt(60)) {
            direction = random.nextInt(4);
            moveTimer = 0;
        }

        int newX = currentX;
        int newY = currentY;
        switch (direction) {
            case ConfigManager.DIR_UP: newY -= enemySpeed; break;
            case ConfigManager.DIR_DOWN: newY += enemySpeed; break;
            case ConfigManager.DIR_LEFT: newX -= enemySpeed; break;
            case ConfigManager.DIR_RIGHT: newX += enemySpeed; break;
        }

        if (gameWorld.isPositionPassable(newX, newY, tankWidth, tankHeight)) {
            if (useComponents && transform != null) transform.setPosition(newX, newY);
            else { x = newX; y = newY; }
        } else {
            direction = random.nextInt(4);
        }

        if (shootTimer > 120 + random.nextInt(60) && random.nextInt(100) < 5) {
            int bulletX = getX() + tankWidth / 2;
            int bulletY = getY() + tankHeight / 2;

            // 【核心修复：增加防空指针】
            Bullet b = GameObjectFactory.createBullet(bulletX, bulletY, direction, false);
            if (b != null) {
                bullets.add(b);
            }
            shootTimer = 0;
        }

        if (useComponents && health != null) {
            health.update(0.016f);
            if (!health.isAlive() && alive) {
                alive = false;
                EventManager.getInstance().triggerEvent(GameEvent.enemyDied(this));
            }
        }
    }

    public void draw(Graphics g) {
        if (!alive) return;
        ConfigManager config = ConfigManager.getInstance();
        int tankWidth = config.getInt(ConfigManager.KEY_TANK_WIDTH);
        int tankHeight = config.getInt(ConfigManager.KEY_TANK_HEIGHT);
        int turretLength = config.getInt(ConfigManager.KEY_TANK_TURRET_LENGTH);
        int centerX = getX() + tankWidth / 2;
        int centerY = getY() + tankHeight / 2;

        drawTankBody(g, getX(), getY(), ConfigManager.ENEMY_TANK_COLOR, ConfigManager.TANK_DETAIL_COLOR);

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

        if (aiState == 1) g.setColor(Color.RED);
        else g.setColor(Color.ORANGE);
        g.fillRect(centerX - 2, getY() + 2, 4, 4);

        if (useComponents && health != null) drawHealthBar(g, getX(), getY(), tankWidth);
    }

    private void drawHealthBar(Graphics g, int x, int y, int tankWidth) {
        if (health == null) return;
        float healthPercent = health.getHealthPercentage();
        int barY = y - 8;
        g.setColor(Color.DARK_GRAY);
        g.fillRect(x, barY, tankWidth, 4);
        int fillWidth = (int)(tankWidth * healthPercent);
        if (healthPercent > 0.6) g.setColor(Color.RED.brighter());
        else if (healthPercent > 0.3) g.setColor(Color.ORANGE);
        else g.setColor(Color.RED.darker());
        g.fillRect(x, barY, fillWidth, 4);
        g.setColor(Color.BLACK);
        g.drawRect(x, barY, tankWidth, 4);
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
        int tankHeight = config.getInt(ConfigManager.KEY_TANK_HEIGHT);
        int thisCenterX = getX() + tankWidth / 2;
        int thisCenterY = getY() + tankHeight / 2;
        int targetCenterX = targetX + tankWidth / 2;
        int targetCenterY = targetY + tankHeight / 2;
        int dx = thisCenterX - targetCenterX;
        int dy = thisCenterY - targetCenterY;
        int distance = (int)Math.sqrt(dx*dx + dy*dy);
        return distance < (tankWidth - 5);
    }

    public void takeDamage(int damage) {
        if (!alive || damage <= 0) return;
        if (useComponents && health != null) {
            health.damage(damage);
            EventManager.getInstance().triggerEvent(GameEvent.enemyHit(this, damage));
            if (!health.isAlive() && alive) {
                alive = false;
                EventManager.getInstance().triggerEvent(GameEvent.enemyDied(this));
            }
        } else {
            alive = false;
            EventManager.getInstance().triggerEvent(GameEvent.enemyDied(this));
        }
    }
}