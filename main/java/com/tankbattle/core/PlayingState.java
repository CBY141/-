package main.java.com.tankbattle.core;

import main.java.com.tankbattle.entity.*;
import main.java.com.tankbattle.world.GameWorld;
import main.java.com.tankbattle.world.MapTile;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PlayingState extends BaseGameState {
    private Player player;
    private List<Enemy> enemies = new ArrayList<>();
    private List<Bullet> bullets = new ArrayList<>();
    private List<Explosion> explosions = new ArrayList<>();
    private GameWorld gameWorld;
    private Random random = new Random();
    private int moveSoundCooldown = 0;
    private boolean wasShooting = false;

    // 调试信息
    private int updateCount = 0;
    private String lastInputStatus = "";

    // 事件监听器
    private EventListener gameEventListener = new EventListener() {
        @Override
        public void onEvent(GameEvent event) {
            handleGameEvent(event);
        }
    };

    // 游戏状态
    private int totalEnemiesKilled = 0;
    private int totalShotsFired = 0;
    private int totalDamageTaken = 0;
    private long gameStartTime = 0;

    public PlayingState(GameManager gameManager) {
        super(gameManager);
    }

    @Override
    public void enter() {
        System.out.println("进入游戏状态");
        gameStartTime = System.currentTimeMillis();

        // 初始化游戏世界
        gameWorld = GameObjectFactory.createGameWorld();
        player = GameObjectFactory.createPlayerAtSafePosition(gameWorld);
        initializeEnemies(ConfigManager.getInstance().getInt(ConfigManager.KEY_ENEMY_COUNT));
        bullets.clear();
        explosions.clear();
        moveSoundCooldown = 0;
        wasShooting = false;
        updateCount = 0;

        // 重置统计
        totalEnemiesKilled = 0;
        totalShotsFired = 0;
        totalDamageTaken = 0;

        // 注册事件监听器
        EventManager eventManager = EventManager.getInstance();
        eventManager.registerListener(gameEventListener,
                Event.EventType.GAME_STARTED,
                Event.EventType.PLAYER_SPAWNED,
                Event.EventType.PLAYER_DIED,
                Event.EventType.PLAYER_HIT,
                Event.EventType.ENEMY_SPAWNED,
                Event.EventType.ENEMY_DIED,
                Event.EventType.BULLET_FIRED,
                Event.EventType.EXPLOSION_CREATED,
                Event.EventType.GAME_OVER
        );

        // 触发游戏开始事件
        eventManager.triggerEvent(GameEvent.gameStarted());

        // 播放音效
        ResourceManager.getInstance().playSound("start");
        ResourceManager.getInstance().playBackgroundMusic();

        System.out.println("游戏状态初始化完成");
        System.out.println("玩家位置: (" + player.x + ", " + player.y + ")");
        System.out.println("敌人数量: " + enemies.size());
    }

    @Override
    public void exit() {
        System.out.println("退出游戏状态");

        // 注销事件监听器
        EventManager.getInstance().unregisterListener(gameEventListener);

        // 停止背景音乐
        ResourceManager.getInstance().stopBackgroundMusic();

        // 打印游戏统计
        printGameStats();
    }

    @Override
    public void update(float deltaTime) {
        updateCount++;

        // 获取输入处理器
        InputHandler input = gameManager.getInputHandler();
        if (input == null) {
            if (updateCount % 60 == 0) { // 每秒输出一次错误
                System.out.println("错误: InputHandler为null!");
            }
            return;
        }

        // 构建输入状态字符串
        String inputStatus = String.format("W:%s A:%s S:%s D:%s 射击:%s 鼠标:(%d,%d)",
                input.upPressed, input.leftPressed, input.downPressed, input.rightPressed,
                input.spacePressed, input.mouseX, input.mouseY);

        // 只有输入状态变化时才输出
        if (!inputStatus.equals(lastInputStatus)) {
            System.out.println("输入状态: " + inputStatus);
            lastInputStatus = inputStatus;
        }

        // 处理移动音效
        if (moveSoundCooldown <= 0 && (input.upPressed || input.downPressed ||
                input.leftPressed || input.rightPressed)) {
            ResourceManager.getInstance().playSound("move");
            moveSoundCooldown = 20;
        }
        if (moveSoundCooldown > 0) moveSoundCooldown--;

        // 处理射击音效
        boolean isShootingNow = input.spacePressed;
        if (isShootingNow && !wasShooting) {
            ResourceManager.getInstance().playSound("shoot");
            System.out.println("播放射击音效");
            totalShotsFired++;
        }
        wasShooting = isShootingNow;

        // 更新玩家
        player.update(input.upPressed, input.downPressed, input.leftPressed, input.rightPressed,
                input.spacePressed, input.mouseX, input.mouseY, gameWorld, bullets);

        // 更新子弹
        for (Bullet b : bullets) b.update();

        // 子弹与地图碰撞检测
        for (Bullet bullet : bullets) {
            if (!bullet.isAlive()) continue;
            int bx = bullet.getX();
            int by = bullet.getY();
            ConfigManager config = ConfigManager.getInstance();
            int tileSize = config.getInt(ConfigManager.KEY_TILE_SIZE);
            int tileX = bx / tileSize;
            int tileY = by / tileSize;
            int mapWidth = config.getInt(ConfigManager.KEY_MAP_WIDTH);
            int mapHeight = config.getInt(ConfigManager.KEY_MAP_HEIGHT);

            if (tileX >= 0 && tileX < mapWidth && tileY >= 0 && tileY < mapHeight) {
                MapTile tile = gameWorld.map[tileX][tileY];
                if (!tile.isPassable()) {
                    int type = tile.getType();
                    if (type == ConfigManager.TILE_BRICK) {
                        gameWorld.destroyTile(tileX, tileY);
                        explosions.add(new Explosion(
                                tileX * tileSize + tileSize / 2,
                                tileY * tileSize + tileSize / 2
                        ));
                        ResourceManager.getInstance().playSound("explosion");

                        // 触发爆炸事件
                        EventManager.getInstance().triggerEvent(
                                GameEvent.explosionCreated(
                                        new Point(tileX * tileSize + tileSize / 2,
                                                tileY * tileSize + tileSize / 2)
                                )
                        );
                    }
                    bullet.setAlive(false);
                }
            }
        }
        bullets.removeIf(b -> !b.isAlive());

        // 更新敌人
        for (Enemy e : enemies) e.update(gameWorld, bullets);

        // 更新爆炸效果
        for (Explosion exp : explosions) exp.update();
        explosions.removeIf(exp -> !exp.alive);

        // 碰撞检测
        checkCollisions();

        // 检查游戏结束条件
        checkGameEndConditions();

        // 每秒输出一次游戏状态
        if (updateCount % 60 == 0) {
            System.out.println("游戏状态: 生命" + player.lives +
                    " 敌人:" + enemies.size() +
                    " 子弹:" + bullets.size() +
                    " 爆炸:" + explosions.size());
        }
    }

    @Override
    public void render(Graphics g) {
        // 绘制游戏世界
        gameWorld.draw(g);

        // 绘制玩家
        InputHandler input = gameManager.getInputHandler();
        int mouseX = 400; // 默认值
        int mouseY = 300; // 默认值
        if (input != null) {
            mouseX = input.mouseX;
            mouseY = input.mouseY;
        }
        player.draw(g, mouseX, mouseY);

        // 绘制敌人
        for (Enemy e : enemies) e.draw(g);

        // 绘制子弹
        for (Bullet b : bullets) b.draw(g);

        // 绘制爆炸效果
        for (Explosion exp : explosions) exp.draw(g);

        // 绘制UI
        drawGameUI(g);

        // 绘制准星
        drawCrosshair(g, mouseX, mouseY);
    }

    @Override
    public void keyPressed(int keyCode) {
        if (keyCode == 82) { // R键返回主菜单
            System.out.println("R键按下，返回主菜单");
            gameManager.changeState(GameManager.GameStateType.MENU);
        } else if (keyCode == 116) { // F5快速保存
            System.out.println("F5键按下，快速保存");
            SaveManager.getInstance().quickSave();
        } else if (keyCode == 117) { // F6快速加载
            System.out.println("F6键按下，快速加载");
            SaveManager.getInstance().quickLoad();
        }
    }

    @Override
    public void mousePressed(int x, int y, int button) {
        System.out.println("鼠标按下: (" + x + "," + y + ") 按钮: " + button);
    }

    @Override
    public void mouseMoved(int x, int y) {
        // 鼠标移动事件
    }

    // 处理游戏事件
    private void handleGameEvent(GameEvent event) {
        System.out.println("收到事件: " + event.getType() +
                " 来源: " + event.getSource() +
                " 数据: " + event.getData());

        switch (event.getType()) {
            case PLAYER_SPAWNED:
                System.out.println("事件: 玩家生成");
                break;

            case PLAYER_DIED:
                System.out.println("事件: 玩家死亡");
                ResourceManager.getInstance().playSound("gameover");
                break;

            case PLAYER_HIT:
                System.out.println("事件: 玩家受伤，伤害: " + event.getData());
                totalDamageTaken += (int)event.getData();
                ResourceManager.getInstance().playSound("hit");
                break;

            case ENEMY_DIED:
                System.out.println("事件: 敌人死亡");
                totalEnemiesKilled++;
                // 给玩家加分
                if (player != null) {
                    player.addScore(100);
                }
                // 触发成就检查
                AchievementManager.getInstance().onEvent(event);
                break;

            case ENEMY_SPAWNED:
                System.out.println("事件: 敌人生成");
                break;

            case BULLET_FIRED:
                System.out.println("事件: 子弹发射");
                break;

            case EXPLOSION_CREATED:
                System.out.println("事件: 爆炸创建");
                break;

            case GAME_STARTED:
                System.out.println("事件: 游戏开始");
                break;

            default:
                // 忽略其他事件
                break;
        }
    }

    private void initializeEnemies(int count) {
        enemies.clear();
        ConfigManager config = ConfigManager.getInstance();
        int windowWidth = config.getInt(ConfigManager.KEY_WINDOW_WIDTH);
        int windowHeight = config.getInt(ConfigManager.KEY_WINDOW_HEIGHT);
        int tankWidth = config.getInt(ConfigManager.KEY_TANK_WIDTH);
        int tankHeight = config.getInt(ConfigManager.KEY_TANK_HEIGHT);

        for (int i = 0; i < count; i++) {
            int x, y;
            int attempts = 0;
            do {
                x = random.nextInt(windowWidth - tankWidth);
                y = random.nextInt(windowHeight - tankHeight);
                attempts++;
            } while (attempts < 50 && !gameWorld.isPositionPassable(x, y, tankWidth, tankHeight));

            if (attempts < 50) {
                Enemy enemy = new Enemy(x, y);
                enemies.add(enemy);

                // 触发敌人生成事件
                EventManager.getInstance().triggerEvent(
                        GameEvent.enemySpawned(enemy)
                );
            }
        }
    }

    private void checkCollisions() {
        ConfigManager config = ConfigManager.getInstance();
        int tankWidth = config.getInt(ConfigManager.KEY_TANK_WIDTH);

        // 子弹与敌人碰撞
        for (Bullet bullet : bullets) {
            if (!bullet.isAlive() || !bullet.isFromPlayer()) continue;
            for (Enemy enemy : enemies) {
                if (enemy.alive && bullet.collidesWith(enemy.x, enemy.y, tankWidth)) {
                    bullet.setAlive(false);
                    enemy.alive = false;
                    explosions.add(new Explosion(enemy.x + tankWidth/2, enemy.y + tankWidth/2));
                    ResourceManager.getInstance().playSound("explosion");

                    // 触发敌人死亡事件
                    EventManager.getInstance().triggerEvent(
                            GameEvent.enemyDied(enemy)
                    );
                    break;
                }
            }
        }

        // 子弹与玩家碰撞
        for (Bullet bullet : bullets) {
            if (!bullet.isAlive() || bullet.isFromPlayer()) continue;
            if (bullet.collidesWith(player.x, player.y, tankWidth)) {
                bullet.setAlive(false);
                // 使用新的受伤方法
                player.takeDamage(1);
            }
        }

        // 敌人与玩家碰撞
        for (Enemy enemy : enemies) {
            if (enemy.alive && enemy.collidesWith(player.x, player.y)) {
                enemy.alive = false;
                explosions.add(new Explosion(enemy.x + tankWidth/2, enemy.y + tankWidth/2));
                ResourceManager.getInstance().playSound("explosion");

                // 触发敌人死亡事件
                EventManager.getInstance().triggerEvent(
                        GameEvent.enemyDied(enemy)
                );

                // 玩家受伤
                player.takeDamage(1);
            }
        }
        enemies.removeIf(e -> !e.alive);
    }

    private void checkGameEndConditions() {
        if (player.dead) {
            System.out.println("玩家死亡，游戏结束");

            // 触发游戏结束事件
            EventManager.getInstance().triggerEvent(
                    GameEvent.gameOver(false)
            );

            gameManager.changeState(GameManager.GameStateType.GAME_OVER);
        } else if (enemies.isEmpty()) {
            System.out.println("所有敌人被消灭，游戏胜利");

            // 触发游戏胜利事件
            EventManager.getInstance().triggerEvent(
                    new GameEvent(Event.EventType.GAME_VICTORY, null, true)
            );

            gameManager.changeState(GameManager.GameStateType.VICTORY);
        }
    }

    private void drawGameUI(Graphics g) {
        ResourceManager rm = ResourceManager.getInstance();

        // 基本信息
        g.setColor(Color.WHITE);
        g.setFont(rm.getSafeFont(Font.BOLD, 16));
        g.drawString("生命值: " + player.lives, 10, 20);
        g.drawString("敌人剩余: " + enemies.size(), 10, 40);
        g.drawString("分数: " + player.getScore(), 10, 60);

        // 控制提示
        g.setColor(Color.CYAN);
        g.setFont(rm.getSafeFont(Font.PLAIN, 14));
        g.drawString("按 R 键返回主菜单", 10, 80);
        g.drawString("按 F5 快速保存", 10, 100);
        g.drawString("按 F6 快速加载", 10, 120);

        // 游戏统计
        g.setColor(Color.ORANGE);
        g.drawString("消灭敌人: " + totalEnemiesKilled, 10, 140);
        g.drawString("发射子弹: " + totalShotsFired, 10, 160);
        g.drawString("承受伤害: " + totalDamageTaken, 10, 180);

        // 成就信息
        AchievementManager achievementManager = AchievementManager.getInstance();
        g.setColor(Color.MAGENTA);
        g.drawString("成就: " + achievementManager.getUnlockedCount() +
                "/" + achievementManager.getTotalCount(), 10, 200);
    }

    private void drawCrosshair(Graphics g, int mouseX, int mouseY) {
        g.setColor(Color.RED);
        g.drawLine(mouseX - 10, mouseY, mouseX + 10, mouseY);
        g.drawLine(mouseX, mouseY - 10, mouseX, mouseY + 10);
        g.drawOval(mouseX - 5, mouseY - 5, 10, 10);
    }

    // 打印游戏统计
    private void printGameStats() {
        long gameTime = (System.currentTimeMillis() - gameStartTime) / 1000;
        System.out.println("========== 游戏统计 ==========");
        System.out.println("游戏时间: " + gameTime + "秒");
        System.out.println("消灭敌人: " + totalEnemiesKilled);
        System.out.println("发射子弹: " + totalShotsFired);
        System.out.println("承受伤害: " + totalDamageTaken);
        System.out.println("最终分数: " + player.getScore());
        System.out.println("============================");
    }
}