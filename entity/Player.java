package main.java.com.tankbattle.entity;

import main.java.com.tankbattle.core.*;
import main.java.com.tankbattle.managers.EventManager;
import main.java.com.tankbattle.system.*;
import main.java.com.tankbattle.world.GameWorld;
import main.java.com.tankbattle.game.factory.GameObjectFactory; // 引入工厂
import java.awt.*;
import java.util.List;

public class Player {
    public int x, y;
    public int lives;
    public boolean inGrass = false;
    public boolean dead = false;

    private int shootCooldown = 0;
    private static final int SHOOT_COOLDOWN_TIME = 20;
    private int startX, startY;
    private int moveCount = 0;
    private int shootCount = 0;
    private String lastMoveDirection = "";
    private int score = 0;

    private TransformComponent transform = null;
    private HealthComponent health = null;
    private RenderComponent render = null;
    private boolean useComponents = false;

    public Player(int startX, int startY) {
        ConfigManager config = ConfigManager.getInstance();
        this.lives = config.getInt(ConfigManager.KEY_PLAYER_LIVES);
        this.x = startX;
        this.y = startY;
        this.startX = startX;
        this.startY = startY;
        EventManager.getInstance().triggerEvent(GameEvent.playerSpawned(this));
    }

    public void enableComponents() {
        useComponents = true;
        transform = new TransformComponent();
        health = new HealthComponent();
        render = new RenderComponent();
        ConfigManager config = ConfigManager.getInstance();
        transform.setPosition(x, y);
        transform.setSize(config.getInt(ConfigManager.KEY_TANK_WIDTH), config.getInt(ConfigManager.KEY_TANK_HEIGHT));
        health.setEntity(new Entity("Player_Entity"));
        health.setMaxHealth(lives * 100);
        health.setCurrentHealth(lives * 100);
        render.setColor(ConfigManager.PLAYER_TANK_COLOR);
        render.setVisible(true);
    }

    public void reset() {
        ConfigManager config = ConfigManager.getInstance();
        this.x = startX;
        this.y = startY;
        this.lives = config.getInt(ConfigManager.KEY_PLAYER_LIVES);
        this.inGrass = false;
        this.dead = false;
        this.shootCooldown = 0;
        this.moveCount = 0;
        this.shootCount = 0;
        this.score = 0;
        if (useComponents && transform != null && health != null) {
            transform.setPosition(startX, startY);
            health.setCurrentHealth(lives * 100);
        }
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

        boolean isMoving = (up || down || left || right);
        moveCount++;

        int originalX = x;
        int originalY = y;
        x = newX;
        y = newY;

        boolean positionValid = world.isPositionPassable(x, y, tankWidth, tankHeight);
        boolean inBounds = (x >= 0 && x <= windowWidth - tankWidth && y >= 0 && y <= windowHeight - tankHeight);

        if (!positionValid || !inBounds) {
            x = originalX;
            y = originalY;
            if (world.isPositionPassable(newX, originalY, tankWidth, tankHeight) && newX >= 0 && newX <= windowWidth - tankWidth) {
                x = newX;
            }
            if (world.isPositionPassable(originalX, newY, tankWidth, tankHeight) && newY >= 0 && newY <= windowHeight - tankHeight) {
                y = newY;
            }
        }

        inGrass = world.isInGrass(x, y);

        if (shoot && shootCooldown == 0) {
            shootCount++;
            int bulletX = x + tankWidth / 2;
            int bulletY = y + tankHeight / 2;
            double angle = Math.atan2(mouseY - bulletY, mouseX - bulletX);
            int direction;
            if (Math.abs(angle) < Math.PI/4) direction = ConfigManager.DIR_RIGHT;
            else if (Math.abs(angle) > 3*Math.PI/4) direction = ConfigManager.DIR_LEFT;
            else if (angle > 0) direction = ConfigManager.DIR_DOWN;
            else direction = ConfigManager.DIR_UP;

            // 【核心修复：改用工厂模式生成玩家子弹】
            Bullet bullet = GameObjectFactory.createBullet(bulletX, bulletY, direction, true);
            if (bullet != null) {
                bullets.add(bullet);
            }

            shootCooldown = SHOOT_COOLDOWN_TIME;
            EventManager.getInstance().triggerEvent(GameEvent.bulletFired(new Point(bulletX, bulletY), true));
        }

        if (shootCooldown > 0) shootCooldown--;

        if (useComponents && health != null) {
            health.update(0.016f);
            this.lives = (health.getCurrentHealth() + 99) / 100;
            if (!health.isAlive() && !dead) {
                dead = true;
                EventManager.getInstance().triggerEvent(GameEvent.playerDied(this));
            }
        }
    }

    public void takeDamage(int damage) {
        if (dead || damage <= 0) return;
        if (useComponents && health != null) {
            health.damage(damage);
            lives = (health.getCurrentHealth() + 99) / 100;
        } else {
            lives -= damage;
        }
        EventManager.getInstance().triggerEvent(GameEvent.playerHit(this, damage));
        if (lives <= 0) {
            lives = 0;
            dead = true;
            EventManager.getInstance().triggerEvent(GameEvent.playerDied(this));
        }
    }

    public void addScore(int points) {
        score += points;
        EventManager.getInstance().triggerEvent(GameEvent.playerScored(this, points));
    }

    public int getScore() { return score; }

    public void draw(Graphics g, int mouseX, int mouseY) {
        if (dead) return;
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
        g.fillPolygon(new int[]{centerX, centerX - 3, centerX + 3}, new int[]{y + 2, y + 8, y + 8}, 3);

        if (useComponents && health != null) drawHealthBar(g, x, y, tankWidth);
    }

    private void drawHealthBar(Graphics g, int x, int y, int tankWidth) {
        if (health == null) return;
        float healthPercent = health.getHealthPercentage();
        int barY = y - 8;
        g.setColor(Color.DARK_GRAY);
        g.fillRect(x, barY, tankWidth, 4);
        int fillWidth = (int)(tankWidth * healthPercent);
        if (healthPercent > 0.6) g.setColor(Color.GREEN);
        else if (healthPercent > 0.3) g.setColor(Color.YELLOW);
        else g.setColor(Color.RED);
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

    public int getCurrentHealth() { return useComponents && health != null ? health.getCurrentHealth() : lives * 100; }
    public int getMaxHealth() { return useComponents && health != null ? health.getMaxHealth() : ConfigManager.getInstance().getInt(ConfigManager.KEY_PLAYER_LIVES) * 100; }
    public boolean isUsingComponents() { return useComponents; }
    public TransformComponent getTransformComponent() { return transform; }
    public HealthComponent getHealthComponent() { return health; }
    public RenderComponent getRenderComponent() { return render; }
}