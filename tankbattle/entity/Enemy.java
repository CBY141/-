package main.java.com.tankbattle.entity;

import main.java.com.tankbattle.core.*;
import main.java.com.tankbattle.managers.EventManager;
import main.java.com.tankbattle.system.*;
import main.java.com.tankbattle.world.GameWorld;
import main.java.com.tankbattle.game.factory.GameObjectFactory;
import main.java.com.tankbattle.utils.TankRenderer;

import java.awt.*;
import java.util.List;
import java.util.Random;

public class Enemy {
    public int x, y;
    public int direction;
    public boolean alive = true;

    private float moveTimer = 0f;
    private float shootTimer = 0f;
    private static final float MOVE_INTERVAL_MIN = 1.0f;
    private static final float MOVE_INTERVAL_MAX = 2.0f;
    private static final float SHOOT_INTERVAL_MIN = 1.5f;
    private static final float SHOOT_INTERVAL_MAX = 3.0f;
    private Random random = new Random();

    private int lastX = 0, lastY = 0;
    private int stuckFrames = 0;
    private int aiState = 0;
    private float aiTimer = 0f;
    private int aggression = 0;

    private int enemySpeed;
    private int tankWidth;
    private int tankHeight;
    private int turretLength;

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
        this.moveTimer = random.nextFloat() * MOVE_INTERVAL_MAX;
        this.shootTimer = random.nextFloat() * SHOOT_INTERVAL_MAX;
        this.aiTimer = 0f;
        this.stuckFrames = 0;
        this.aiState = 0;

        ConfigManager config = ConfigManager.getInstance();
        this.enemySpeed = config.getInt(ConfigManager.KEY_ENEMY_SPEED);
        this.tankWidth = config.getInt(ConfigManager.KEY_TANK_WIDTH);
        this.tankHeight = config.getInt(ConfigManager.KEY_TANK_HEIGHT);
        this.turretLength = config.getInt(ConfigManager.KEY_TANK_TURRET_LENGTH);

        if (useComponents && transform != null && health != null) {
            transform.setPosition(x, y);
            health.setCurrentHealth(100);
        }
    }

    public void enableComponents() { /* 省略，与之前相同 */ }

    public int getX() { return (useComponents && transform != null) ? transform.getX() : x; }
    public int getY() { return (useComponents && transform != null) ? transform.getY() : y; }
    public int getHealth() { return (useComponents && health != null) ? health.getCurrentHealth() : (alive ? 100 : 0); }

    public void update(float deltaTime, GameWorld gameWorld, List<Bullet> bullets) {
        if (!alive) return;

        moveTimer -= deltaTime;
        shootTimer -= deltaTime;
        aiTimer += deltaTime;

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

        if (aiTimer > 2.0f + random.nextFloat() * 2.0f) {
            aiState = random.nextInt(100) < aggression ? 1 : 0;
            aiTimer = 0f;
        }

        if (moveTimer <= 0f) {
            direction = random.nextInt(4);
            moveTimer = MOVE_INTERVAL_MIN + random.nextFloat() * (MOVE_INTERVAL_MAX - MOVE_INTERVAL_MIN);
        }

        int newX = currentX;
        int newY = currentY;
        switch (direction) {
            case ConfigManager.DIR_UP:    newY -= enemySpeed; break;
            case ConfigManager.DIR_DOWN:  newY += enemySpeed; break;
            case ConfigManager.DIR_LEFT:  newX -= enemySpeed; break;
            case ConfigManager.DIR_RIGHT: newX += enemySpeed; break;
        }

        if (gameWorld.isPositionPassable(newX, newY, tankWidth, tankHeight)) {
            if (useComponents && transform != null) transform.setPosition(newX, newY);
            else { x = newX; y = newY; }
        } else {
            direction = random.nextInt(4);
        }

        if (shootTimer <= 0f && random.nextInt(100) < 5) {
            int bulletX = getX() + tankWidth / 2;
            int bulletY = getY() + tankHeight / 2;
            Bullet b = GameObjectFactory.createBullet(bulletX, bulletY, direction, false);
            if (b != null) {
                bullets.add(b);
            }
            shootTimer = SHOOT_INTERVAL_MIN + random.nextFloat() * (SHOOT_INTERVAL_MAX - SHOOT_INTERVAL_MIN);
        }

        if (useComponents && health != null) {
            health.update(deltaTime);
            if (!health.isAlive() && alive) {
                alive = false;
                EventManager.getInstance().triggerEvent(GameEvent.enemyDied(this));
            }
        }
    }

    public void draw(Graphics g) {
        if (!alive) return;
        int centerX = getX() + tankWidth / 2;
        int centerY = getY() + tankHeight / 2;
        TankRenderer.drawTankBody(g, getX(), getY(), ConfigManager.ENEMY_TANK_COLOR, ConfigManager.TANK_DETAIL_COLOR);
        TankRenderer.drawTankTurret(g, centerX, centerY, direction, ConfigManager.TANK_TURRET_COLOR, turretLength);
        g.setColor(aiState == 1 ? Color.RED : Color.ORANGE);
        g.fillRect(centerX - 2, getY() + 2, 4, 4);
        if (useComponents && health != null) {
            TankRenderer.drawHealthBar(g, getX(), getY(), tankWidth, health.getHealthPercentage(), false);
        }
    }

    public boolean collidesWith(int targetX, int targetY) {
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