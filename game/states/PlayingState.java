package main.java.com.tankbattle.game.states;

import main.java.com.tankbattle.core.*;
import main.java.com.tankbattle.system.Event;
import main.java.com.tankbattle.entity.*;
import main.java.com.tankbattle.game.factory.GameObjectFactory;
import main.java.com.tankbattle.managers.AchievementManager;
import main.java.com.tankbattle.managers.EventManager;
import main.java.com.tankbattle.managers.GameObjectPool; // 引入对象池
import main.java.com.tankbattle.managers.SaveManager;
import main.java.com.tankbattle.system.EventListener;
import main.java.com.tankbattle.system.GameEvent;
import main.java.com.tankbattle.utils.CollisionManager;
import main.java.com.tankbattle.utils.FrameRateManager;
import main.java.com.tankbattle.world.GameWorld;
import main.java.com.tankbattle.world.MapTile;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Random;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class PlayingState extends BaseGameState {
    private Player player;
    private List<Enemy> enemies = new CopyOnWriteArrayList<>();
    private List<Bullet> bullets = new CopyOnWriteArrayList<>();
    private List<Explosion> explosions = new CopyOnWriteArrayList<>();
    private GameWorld gameWorld;
    private Random random = new Random();
    private int moveSoundCooldown = 0;
    private boolean wasShooting = false;

    private EventListener gameEventListener = new EventListener() {
        @Override
        public void onEvent(GameEvent event) {
            handleGameEvent(event);
        }
    };

    private int totalEnemiesKilled = 0;
    private int totalShotsFired = 0;
    private int totalDamageTaken = 0;
    private long gameStartTime = 0;
    private int updateCount = 0;

    private FrameRateManager frameRateManager = FrameRateManager.getInstance();

    public PlayingState(GameManager gameManager) {
        super(gameManager);
    }

    @Override
    public void enter() {
        gameStartTime = System.currentTimeMillis();
        gameWorld = GameObjectFactory.createGameWorld();
        player = GameObjectFactory.createPlayerAtSafePosition(gameWorld);
        initializeEnemies(ConfigManager.getInstance().getInt(ConfigManager.KEY_ENEMY_COUNT));

        // 进游戏前清空并归还可能遗留的对象
        GameObjectPool.getInstance().clearAll();
        bullets.clear();
        explosions.clear();

        moveSoundCooldown = 0;
        wasShooting = false;
        updateCount = 0;
        totalEnemiesKilled = 0;
        totalShotsFired = 0;
        totalDamageTaken = 0;

        EventManager eventManager = EventManager.getInstance();
        eventManager.registerListener(gameEventListener,
                Event.EventType.GAME_STARTED, Event.EventType.PLAYER_SPAWNED,
                Event.EventType.PLAYER_DIED, Event.EventType.PLAYER_HIT,
                Event.EventType.ENEMY_SPAWNED, Event.EventType.ENEMY_DIED,
                Event.EventType.BULLET_FIRED, Event.EventType.EXPLOSION_CREATED,
                Event.EventType.GAME_OVER
        );

        eventManager.triggerEvent(GameEvent.gameStarted());
        ResourceManager.getInstance().playSound("start");
        ResourceManager.getInstance().playBackgroundMusic();
    }

    @Override
    public void exit() {
        EventManager.getInstance().unregisterListener(gameEventListener);
        ResourceManager.getInstance().stopBackgroundMusic();
        printGameStats();
    }

    @Override
    public void update(float deltaTime) {
        updateCount++;

        InputHandler input = gameManager.getInputHandler();
        if (input == null) return;

        if (moveSoundCooldown <= 0 && (input.upPressed || input.downPressed ||
                input.leftPressed || input.rightPressed)) {
            ResourceManager.getInstance().playSound("move");
            moveSoundCooldown = 20;
        }
        if (moveSoundCooldown > 0) moveSoundCooldown--;

        boolean isShootingNow = input.spacePressed;
        if (isShootingNow && !wasShooting) {
            totalShotsFired++;
        }
        wasShooting = isShootingNow;

        player.update(input.upPressed, input.downPressed, input.leftPressed, input.rightPressed,
                input.spacePressed, input.mouseX, input.mouseY, gameWorld, bullets);

        for (Bullet b : bullets) {
            if (b != null) b.update();
        }

        for (Bullet bullet : bullets) {
            if (bullet == null || !bullet.isAlive()) continue;
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
                        Explosion exp = GameObjectFactory.createExplosion(
                                tileX * tileSize + tileSize / 2,
                                tileY * tileSize + tileSize / 2
                        );
                        if (exp != null) explosions.add(exp); // 安全判断

                        ResourceManager.getInstance().playSound("explosion");
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

        // 【核心修复：回收子弹】
        List<Bullet> deadBullets = new ArrayList<>();
        for (Bullet b : bullets) {
            if (b == null || !b.isAlive()) {
                if (b != null) GameObjectPool.getInstance().returnBullet(b);
                deadBullets.add(b);
            }
        }
        bullets.removeAll(deadBullets);

        for (Enemy e : enemies) {
            if (e != null) e.update(gameWorld, bullets);
        }

        for (Explosion exp : explosions) {
            if (exp != null) exp.update();
        }

        // 【核心修复：回收爆炸特效】
        List<Explosion> deadExps = new ArrayList<>();
        for (Explosion exp : explosions) {
            if (exp == null || !exp.alive) {
                if (exp != null) GameObjectPool.getInstance().returnExplosion(exp);
                deadExps.add(exp);
            }
        }
        explosions.removeAll(deadExps);

        CollisionManager.checkAllCollisions(bullets, enemies, player, explosions);

        // 【核心修复：回收敌人】
        List<Enemy> deadEnemies = new ArrayList<>();
        for (Enemy e : enemies) {
            if (e == null || !e.alive) {
                if (e != null) GameObjectPool.getInstance().returnEnemy(e);
                deadEnemies.add(e);
            }
        }
        enemies.removeAll(deadEnemies);

        checkGameEndConditions();
    }

    @Override
    public void render(Graphics g) {
        gameWorld.draw(g);

        InputHandler input = gameManager.getInputHandler();
        int mouseX = 400;
        int mouseY = 300;
        if (input != null) {
            mouseX = input.mouseX;
            mouseY = input.mouseY;
        }
        player.draw(g, mouseX, mouseY);

        for (Enemy e : enemies) if (e != null) e.draw(g);
        for (Bullet b : bullets) if (b != null) b.draw(g);
        for (Explosion exp : explosions) if (exp != null) exp.draw(g);

        drawGameUI(g);
        drawCrosshair(g, mouseX, mouseY);
    }

    @Override
    public void keyPressed(int keyCode) {
        if (keyCode == KeyEvent.VK_R) {
            gameManager.changeState(GameManager.GameStateType.MENU);
        } else if (keyCode == KeyEvent.VK_F5) {
            SaveManager.getInstance().quickSave();
        } else if (keyCode == KeyEvent.VK_F6) {
            SaveManager.getInstance().quickLoad();
        }
    }

    private void handleGameEvent(GameEvent event) {
        switch (event.getType()) {
            case PLAYER_DIED:
                ResourceManager.getInstance().playSound("gameover");
                break;
            case PLAYER_HIT:
                totalDamageTaken += (int)event.getData();
                break;
            case ENEMY_DIED:
                totalEnemiesKilled++;
                if (player != null) {
                    player.addScore(100);
                }
                AchievementManager.getInstance().onEvent(event);
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
                Enemy enemy = GameObjectFactory.createEnemy(x, y);
                if (enemy != null) { // 安全判断
                    enemies.add(enemy);
                    EventManager.getInstance().triggerEvent(GameEvent.enemySpawned(enemy));
                }
            }
        }
    }

    private void checkGameEndConditions() {
        if (player.dead) {
            EventManager.getInstance().triggerEvent(GameEvent.gameOver(false));
            gameManager.changeState(GameManager.GameStateType.GAME_OVER);
        } else if (enemies.isEmpty()) {
            EventManager.getInstance().triggerEvent(new GameEvent(Event.EventType.GAME_VICTORY, null, true));
            gameManager.changeState(GameManager.GameStateType.VICTORY);
        }
    }

    private void drawGameUI(Graphics g) {
        ResourceManager rm = ResourceManager.getInstance();
        g.setColor(Color.WHITE);
        g.setFont(rm.getSafeFont(Font.BOLD, 16));
        g.drawString("生命值: " + player.lives, 10, 20);
        g.drawString("敌人剩余: " + enemies.size(), 10, 40);
        g.drawString("分数: " + player.getScore(), 10, 60);

        g.setColor(Color.CYAN);
        g.setFont(rm.getSafeFont(Font.PLAIN, 14));
        g.drawString("按 R 键返回主菜单", 10, 80);
        g.drawString("按 F5 快速保存", 10, 100);
        g.drawString("按 F6 快速加载", 10, 120);

        g.setColor(Color.ORANGE);
        g.drawString("消灭敌人: " + totalEnemiesKilled, 10, 140);
        g.drawString("发射子弹: " + totalShotsFired, 10, 160);
        g.drawString("承受伤害: " + totalDamageTaken, 10, 180);

        AchievementManager am = AchievementManager.getInstance();
        g.setColor(Color.MAGENTA);
        g.drawString("成就: " + am.getUnlockedCount() + "/" + am.getTotalCount(), 10, 200);

        g.setColor(Color.GREEN);
        g.drawString("FPS: " + frameRateManager.getCurrentFPS(), 10, 220);
    }

    private void drawCrosshair(Graphics g, int mouseX, int mouseY) {
        g.setColor(Color.RED);
        g.drawLine(mouseX - 10, mouseY, mouseX + 10, mouseY);
        g.drawLine(mouseX, mouseY - 10, mouseX, mouseY + 10);
        g.drawOval(mouseX - 5, mouseY - 5, 10, 10);
    }

    private void printGameStats() {
        long gameTime = (System.currentTimeMillis() - gameStartTime) / 1000;
        System.out.println("========== 本局结束 ==========");
    }
}