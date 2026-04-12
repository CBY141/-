package main.java.com.tankbattle.entity;

import main.java.com.tankbattle.core.*;
import main.java.com.tankbattle.world.GameWorld;
import java.awt.*;
import java.util.List;

public class Player {
    // 公共字段 - GameEvent.java 会直接访问这些字段
    public int x, y;
    public int lives;
    public boolean inGrass = false;
    public boolean dead = false;

    // 私有字段
    private int shootCooldown = 0;
    private static final int SHOOT_COOLDOWN_TIME = 20;

    // 记录初始位置，用于重置
    private int startX, startY;

    // 调试信息
    private int moveCount = 0;
    private int shootCount = 0;
    private String lastMoveDirection = "";
    private int score = 0;

    // 组件引用（如果使用组件系统）
    private TransformComponent transform = null;
    private HealthComponent health = null;
    private RenderComponent render = null;

    // 是否使用组件系统
    private boolean useComponents = false;

    public Player(int startX, int startY) {
        ConfigManager config = ConfigManager.getInstance();
        this.lives = config.getInt(ConfigManager.KEY_PLAYER_LIVES);
        this.x = startX;
        this.y = startY;
        this.startX = startX;
        this.startY = startY;

        System.out.println("玩家创建: 位置(" + x + "," + y + "), 生命:" + lives);

        // 触发玩家生成事件
        EventManager.getInstance().triggerEvent(
                GameEvent.playerSpawned(this)
        );
    }

    /**
     * 启用组件系统
     */
    public void enableComponents() {
        useComponents = true;

        // 初始化组件
        transform = new TransformComponent();
        health = new HealthComponent();
        render = new RenderComponent();

        ConfigManager config = ConfigManager.getInstance();

        // 设置组件初始状态
        transform.setPosition(x, y);
        transform.setSize(
                config.getInt(ConfigManager.KEY_TANK_WIDTH),
                config.getInt(ConfigManager.KEY_TANK_HEIGHT)
        );

        health.setEntity(new Entity("Player_Entity"));
        health.setMaxHealth(lives * 100);
        health.setCurrentHealth(lives * 100);

        render.setColor(ConfigManager.PLAYER_TANK_COLOR);
        render.setVisible(true);

        System.out.println("玩家组件系统已启用");
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
        this.moveCount = 0;
        this.shootCount = 0;
        this.score = 0;

        // 重置组件（如果启用）
        if (useComponents && transform != null && health != null) {
            transform.setPosition(startX, startY);
            health.setCurrentHealth(lives * 100);
        }

        System.out.println("玩家重置: 位置(" + x + "," + y + "), 生命:" + lives);
    }

    public void update(boolean up, boolean down, boolean left, boolean right, boolean shoot,
                       int mouseX, int mouseY, GameWorld world, List<Bullet> bullets) {
        if (dead) {
            if (moveCount % 30 == 0) { // 每秒输出一次
                System.out.println("玩家已死亡，不处理输入");
            }
            return;
        }

        ConfigManager config = ConfigManager.getInstance();
        int playerSpeed = config.getInt(ConfigManager.KEY_PLAYER_SPEED);
        int tankWidth = config.getInt(ConfigManager.KEY_TANK_WIDTH);
        int tankHeight = config.getInt(ConfigManager.KEY_TANK_HEIGHT);
        int windowWidth = config.getInt(ConfigManager.KEY_WINDOW_WIDTH);
        int windowHeight = config.getInt(ConfigManager.KEY_WINDOW_HEIGHT);

        int newX = x;
        int newY = y;

        // 计算新位置
        if (up) newY -= playerSpeed;
        if (down) newY += playerSpeed;
        if (left) newX -= playerSpeed;
        if (right) newX += playerSpeed;

        // 检查是否有移动
        boolean isMoving = (up || down || left || right);

        // 构建移动方向字符串
        String moveDirection = "";
        if (up) moveDirection += "U";
        if (down) moveDirection += "D";
        if (left) moveDirection += "L";
        if (right) moveDirection += "R";
        if (moveDirection.isEmpty()) moveDirection = "无";

        // 只有移动方向变化时才输出
        if (isMoving && !moveDirection.equals(lastMoveDirection)) {
            System.out.println("玩家移动方向: " + moveDirection + " 速度: " + playerSpeed);
            lastMoveDirection = moveDirection;
        }

        moveCount++;

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

            if (isMoving && moveCount % 20 == 0) { // 减少输出频率
                System.out.println("移动被阻挡，返回原位置 (" + originalX + "," + originalY + ")");
            }

            // 尝试X方向移动
            if (world.isPositionPassable(newX, originalY, tankWidth, tankHeight) &&
                    newX >= 0 && newX <= windowWidth - tankWidth) {
                x = newX;
                if (isMoving) {
                    System.out.println("X方向移动成功: " + originalX + " -> " + newX);
                }
            }

            // 尝试Y方向移动
            if (world.isPositionPassable(originalX, newY, tankWidth, tankHeight) &&
                    newY >= 0 && newY <= windowHeight - tankHeight) {
                y = newY;
                if (isMoving) {
                    System.out.println("Y方向移动成功: " + originalY + " -> " + newY);
                }
            }
        } else if (isMoving && moveCount % 30 == 0) { // 减少输出频率
            System.out.println("移动成功: (" + originalX + "," + originalY + ") -> (" + x + "," + y + ")");
        }

        inGrass = world.isInGrass(x, y);
        if (inGrass && moveCount % 60 == 0) { // 每秒输出一次
            System.out.println("玩家在草丛中，隐身状态");
        }

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

            // 创建子弹
            Bullet bullet = new Bullet(bulletX, bulletY, direction, true);
            bullets.add(bullet);
            shootCooldown = SHOOT_COOLDOWN_TIME;

            // 触发子弹发射事件
            EventManager.getInstance().triggerEvent(
                    GameEvent.bulletFired(new Point(bulletX, bulletY), true)
            );

            System.out.println("玩家发射子弹 #" + shootCount +
                    ": 位置(" + bulletX + "," + bulletY +
                    ") 方向:" + getDirectionName(direction) +
                    " 角度:" + String.format("%.1f", Math.toDegrees(angle)));
        } else if (shoot && moveCount % 20 == 0) { // 减少输出频率
            System.out.println("射击冷却中: " + shootCooldown + "/" + SHOOT_COOLDOWN_TIME);
        }

        if (shootCooldown > 0) shootCooldown--;

        // 更新组件（如果启用）
        if (useComponents && health != null) {
            health.update(0.016f);

            // 同步生命值
            this.lives = (health.getCurrentHealth() + 99) / 100; // 向上取整

            // 检查死亡状态
            if (!health.isAlive() && !dead) {
                dead = true;
                EventManager.getInstance().triggerEvent(
                        GameEvent.playerDied(this)
                );
            }
        }
    }

    // 新增：玩家受伤方法
    public void takeDamage(int damage) {
        if (dead || damage <= 0) return;

        int oldHealth = lives;

        if (useComponents && health != null) {
            // 使用组件系统
            health.damage(damage);
            lives = (health.getCurrentHealth() + 99) / 100; // 向上取整
        } else {
            // 使用传统系统
            lives -= damage;
        }

        // 触发玩家受伤事件
        EventManager.getInstance().triggerEvent(
                GameEvent.playerHit(this, damage)
        );

        if (lives <= 0) {
            lives = 0;
            dead = true;

            // 触发玩家死亡事件
            EventManager.getInstance().triggerEvent(
                    GameEvent.playerDied(this)
            );
        } else if (oldHealth > lives) {
            System.out.println("玩家受伤: 生命 " + oldHealth + " -> " + lives);
        }
    }

    // 新增：增加分数
    public void addScore(int points) {
        score += points;
        System.out.println("玩家获得分数: " + points + " 总分: " + score);

        // 触发得分事件
        EventManager.getInstance().triggerEvent(
                GameEvent.playerScored(this, points)
        );
    }

    public int getScore() {
        return score;
    }

    private String getDirectionName(int direction) {
        switch (direction) {
            case ConfigManager.DIR_UP: return "上";
            case ConfigManager.DIR_DOWN: return "下";
            case ConfigManager.DIR_LEFT: return "左";
            case ConfigManager.DIR_RIGHT: return "右";
            default: return "未知";
        }
    }

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

        // 绘制玩家标识
        g.setColor(Color.WHITE);
        g.fillPolygon(new int[]{centerX, centerX - 3, centerX + 3},
                new int[]{y + 2, y + 8, y + 8}, 3);

        // 绘制生命条（如果启用组件）
        if (useComponents && health != null) {
            drawHealthBar(g, x, y, tankWidth);
        }
    }

    private void drawHealthBar(Graphics g, int x, int y, int tankWidth) {
        if (health == null) return;

        float healthPercent = health.getHealthPercentage();
        int barWidth = tankWidth;
        int barHeight = 4;
        int barY = y - 8;

        // 背景
        g.setColor(Color.DARK_GRAY);
        g.fillRect(x, barY, barWidth, barHeight);

        // 生命条
        int fillWidth = (int)(barWidth * healthPercent);
        if (healthPercent > 0.6) {
            g.setColor(Color.GREEN);
        } else if (healthPercent > 0.3) {
            g.setColor(Color.YELLOW);
        } else {
            g.setColor(Color.RED);
        }
        g.fillRect(x, barY, fillWidth, barHeight);

        // 边框
        g.setColor(Color.BLACK);
        g.drawRect(x, barY, barWidth, barHeight);
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

    // 获取当前生命值（兼容组件系统）
    public int getCurrentHealth() {
        if (useComponents && health != null) {
            return health.getCurrentHealth();
        }
        return lives * 100; // 转换为组件系统的生命值单位
    }

    // 获取最大生命值（兼容组件系统）
    public int getMaxHealth() {
        if (useComponents && health != null) {
            return health.getMaxHealth();
        }
        return ConfigManager.getInstance().getInt(ConfigManager.KEY_PLAYER_LIVES) * 100;
    }

    // 组件系统相关方法
    public boolean isUsingComponents() {
        return useComponents;
    }

    public TransformComponent getTransformComponent() {
        return transform;
    }

    public HealthComponent getHealthComponent() {
        return health;
    }

    public RenderComponent getRenderComponent() {
        return render;
    }
}