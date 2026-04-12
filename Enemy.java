package main.java.com.tankbattle.entity;

import main.java.com.tankbattle.core.*;
import main.java.com.tankbattle.world.GameWorld;
import java.awt.*;
import java.util.List;
import java.util.Random;

public class Enemy {
    // 公共字段 - GameEvent.java 会直接访问这些字段
    public int x, y;
    public int direction;
    public boolean alive = true;

    // 私有字段
    private int moveTimer = 0;
    private int shootTimer = 0;
    private Random random = new Random();

    // 卡住检测
    private int lastX = 0, lastY = 0;
    private int stuckFrames = 0;

    // 简单AI状态
    private int aiState = 0; // 0=随机移动, 1=尝试追击玩家
    private int aiTimer = 0;
    private int lastPlayerX = 0, lastPlayerY = 0;
    private int aggression = 0; // 攻击性，0-100

    // 组件引用（如果使用组件系统）
    private TransformComponent transform = null;
    private HealthComponent health = null;
    private RenderComponent render = null;

    // 是否使用组件系统
    private boolean useComponents = false;

    public Enemy(int x, int y) {
        this.x = x;
        this.y = y;
        this.direction = random.nextInt(4);
        this.lastX = x;
        this.lastY = y;
        this.aggression = random.nextInt(100); // 随机攻击性

        // 触发敌人生成事件
        EventManager.getInstance().triggerEvent(
                GameEvent.enemySpawned(this)
        );

        System.out.println("敌人创建: 位置(" + x + "," + y + "), 攻击性:" + aggression);
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

        health.setEntity(new Entity("Enemy_Entity"));
        health.setMaxHealth(100); // 敌人固定100生命
        health.setCurrentHealth(100);

        render.setColor(ConfigManager.ENEMY_TANK_COLOR);
        render.setVisible(true);

        System.out.println("敌人组件系统已启用");
    }

    // 获取位置（兼容组件系统）
    public int getX() {
        if (useComponents && transform != null) {
            return transform.getX();
        }
        return x;
    }

    public int getY() {
        if (useComponents && transform != null) {
            return transform.getY();
        }
        return y;
    }

    // 获取生命值（兼容组件系统）
    public int getHealth() {
        if (useComponents && health != null) {
            return health.getCurrentHealth();
        }
        return alive ? 100 : 0; // 传统系统：活着就是100生命
    }

    public void update(GameWorld gameWorld, List<Bullet> bullets) {
        if (!alive) return;

        ConfigManager config = ConfigManager.getInstance();
        int enemySpeed = config.getInt(ConfigManager.KEY_ENEMY_SPEED);
        int tankWidth = config.getInt(ConfigManager.KEY_TANK_WIDTH);
        int tankHeight = config.getInt(ConfigManager.KEY_TANK_HEIGHT);

        moveTimer++;
        shootTimer++;
        aiTimer++;

        // 卡住检测
        int currentX = getX();
        int currentY = getY();

        if (currentX == lastX && currentY == lastY) {
            stuckFrames++;
            if (stuckFrames > 20) { // 卡住超过20帧
                direction = random.nextInt(4);
                stuckFrames = 0;
            }
        } else {
            stuckFrames = 0;
        }

        // 保存当前位置用于下一帧检测
        lastX = currentX;
        lastY = currentY;

        // 简单AI：每60-120帧重新评估状态
        if (aiTimer > 60 + random.nextInt(60)) {
            aiState = random.nextInt(100) < aggression ? 1 : 0; // 根据攻击性决定是否追击
            aiTimer = 0;
        }

        // 随机移动
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

        // 检查新位置是否可通过
        if (gameWorld.isPositionPassable(newX, newY, tankWidth, tankHeight)) {
            // 更新位置
            if (useComponents && transform != null) {
                transform.setPosition(newX, newY);
            } else {
                x = newX;
                y = newY;
            }
        } else {
            direction = random.nextInt(4);
        }

        // 射击逻辑
        if (shootTimer > 120 + random.nextInt(60) && random.nextInt(100) < 5) {
            int bulletX = getX() + tankWidth / 2;
            int bulletY = getY() + tankHeight / 2;
            bullets.add(new Bullet(bulletX, bulletY, direction, false));
            shootTimer = 0;
        }

        // 更新组件（如果启用）
        if (useComponents && health != null) {
            health.update(0.016f);

            // 检查死亡状态
            if (!health.isAlive() && alive) {
                alive = false;
                EventManager.getInstance().triggerEvent(
                        GameEvent.enemyDied(this)
                );
            }
        }
    }

    /**
     * 增强更新方法：可以传入玩家位置进行简单追击
     */
    public void updateEnhanced(GameWorld gameWorld, List<Bullet> bullets, Player player) {
        if (!alive) return;

        ConfigManager config = ConfigManager.getInstance();
        int enemySpeed = config.getInt(ConfigManager.KEY_ENEMY_SPEED);
        int tankWidth = config.getInt(ConfigManager.KEY_TANK_WIDTH);
        int tankHeight = config.getInt(ConfigManager.KEY_TANK_HEIGHT);

        moveTimer++;
        shootTimer++;
        aiTimer++;

        // 卡住检测
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

        // 如果有玩家，记录位置
        if (player != null && !player.dead) {
            lastPlayerX = player.x;
            lastPlayerY = player.y;
        }

        // 决定行为
        if (aiTimer > 30 + random.nextInt(60)) {
            if (player != null && !player.dead) {
                // 计算与玩家的距离
                int dx = player.x - currentX;
                int dy = player.y - currentY;
                float distance = (float)Math.sqrt(dx*dx + dy*dy);

                if (distance < 300) { // 玩家在300像素内
                    // 根据攻击性决定行为
                    if (random.nextInt(100) < aggression) {
                        aiState = 1; // 追击
                    } else {
                        aiState = 0; // 随机移动
                    }
                } else {
                    aiState = 0; // 随机移动
                }
            } else {
                aiState = 0; // 没有玩家，随机移动
            }
            aiTimer = 0;
        }

        // 根据AI状态决定移动方向
        if (aiState == 1 && player != null && !player.dead) {
            // 尝试追击玩家
            int dx = lastPlayerX - currentX;
            int dy = lastPlayerY - currentY;

            if (Math.abs(dx) > Math.abs(dy)) {
                direction = dx > 0 ? ConfigManager.DIR_RIGHT : ConfigManager.DIR_LEFT;
            } else {
                direction = dy > 0 ? ConfigManager.DIR_DOWN : ConfigManager.DIR_UP;
            }
        } else {
            // 随机移动
            if (moveTimer > 40 + random.nextInt(80)) {
                direction = random.nextInt(4);
                moveTimer = 0;
            }
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
            if (useComponents && transform != null) {
                transform.setPosition(newX, newY);
            } else {
                x = newX;
                y = newY;
            }
        } else {
            direction = random.nextInt(4);
        }

        // 增强射击逻辑
        if (player != null && !player.dead && aiState == 1) {
            // 追击状态下射击频率更高
            if (shootTimer > 60 + random.nextInt(60) && random.nextInt(100) < 8) {
                int bulletX = getX() + tankWidth / 2;
                int bulletY = getY() + tankHeight / 2;

                // 朝玩家方向射击
                int dx = lastPlayerX - getX();
                int dy = lastPlayerY - getY();
                int shootDir = direction; // 默认朝移动方向

                if (Math.abs(dx) > Math.abs(dy)) {
                    shootDir = dx > 0 ? ConfigManager.DIR_RIGHT : ConfigManager.DIR_LEFT;
                } else {
                    shootDir = dy > 0 ? ConfigManager.DIR_DOWN : ConfigManager.DIR_UP;
                }

                bullets.add(new Bullet(bulletX, bulletY, shootDir, false));
                shootTimer = 0;
            }
        } else {
            // 随机射击
            if (shootTimer > 100 + random.nextInt(80) && random.nextInt(100) < 3) {
                int bulletX = getX() + tankWidth / 2;
                int bulletY = getY() + tankHeight / 2;
                bullets.add(new Bullet(bulletX, bulletY, direction, false));
                shootTimer = 0;
            }
        }

        // 更新组件（如果启用）
        if (useComponents && health != null) {
            health.update(0.016f);

            // 检查死亡状态
            if (!health.isAlive() && alive) {
                alive = false;
                EventManager.getInstance().triggerEvent(
                        GameEvent.enemyDied(this)
                );
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

        // 根据AI状态绘制不同标识
        if (aiState == 1) {
            g.setColor(Color.RED); // 追击状态：红色
        } else {
            g.setColor(Color.ORANGE); // 随机状态：橙色
        }
        g.fillRect(centerX - 2, getY() + 2, 4, 4);

        // 绘制生命条（如果启用组件）
        if (useComponents && health != null) {
            drawHealthBar(g, getX(), getY(), tankWidth);
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
            g.setColor(Color.RED.brighter());
        } else if (healthPercent > 0.3) {
            g.setColor(Color.ORANGE);
        } else {
            g.setColor(Color.RED.darker());
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

    public boolean collidesWith(int targetX, int targetY) {
        ConfigManager config = ConfigManager.getInstance();
        int tankWidth = config.getInt(ConfigManager.KEY_TANK_WIDTH);
        int tankHeight = config.getInt(ConfigManager.KEY_TANK_HEIGHT);

        // 修复：使用正确的宽高计算中心点
        int thisCenterX = getX() + tankWidth / 2;
        int thisCenterY = getY() + tankHeight / 2;

        int targetCenterX = targetX + tankWidth / 2;
        int targetCenterY = targetY + tankHeight / 2;

        int dx = thisCenterX - targetCenterX;
        int dy = thisCenterY - targetCenterY;
        int distance = (int)Math.sqrt(dx*dx + dy*dy);

        // 使用坦克宽度作为碰撞距离（假设坦克是正方形）
        int collisionDistance = tankWidth - 5;  // 留出一些边距
        return distance < collisionDistance;
    }

    /**
     * 获取AI状态描述
     */
    public String getAIStatus() {
        switch (aiState) {
            case 0: return "随机移动";
            case 1: return "追击玩家";
            default: return "未知";
        }
    }

    /**
     * 获取攻击性等级
     */
    public int getAggression() {
        return aggression;
    }

    // 受到伤害
    public void takeDamage(int damage) {
        if (!alive || damage <= 0) return;

        if (useComponents && health != null) {
            // 使用组件系统
            health.damage(damage);

            // 触发敌人受伤事件
            EventManager.getInstance().triggerEvent(
                    GameEvent.enemyHit(this, damage)
            );

            if (!health.isAlive() && alive) {
                alive = false;
                EventManager.getInstance().triggerEvent(
                        GameEvent.enemyDied(this)
                );
            }
        } else {
            // 传统系统：敌人直接死亡
            alive = false;
            EventManager.getInstance().triggerEvent(
                    GameEvent.enemyDied(this)
            );
        }
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