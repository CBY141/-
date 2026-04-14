package main.java.com.tankbattle.game.states;

import main.java.com.tankbattle.core.*;
import main.java.com.tankbattle.system.Event;
import main.java.com.tankbattle.entity.*;
import main.java.com.tankbattle.game.factory.GameObjectFactory;
import main.java.com.tankbattle.game.skill.Skill;
import main.java.com.tankbattle.game.ui.GameWindow;
import main.java.com.tankbattle.managers.AchievementManager;
import main.java.com.tankbattle.managers.EventManager;
import main.java.com.tankbattle.managers.GameObjectPool;
import main.java.com.tankbattle.managers.SaveManager;
import main.java.com.tankbattle.system.EventListener;
import main.java.com.tankbattle.system.GameEvent;
import main.java.com.tankbattle.system.SaveData;
import main.java.com.tankbattle.utils.CollisionManager;
import main.java.com.tankbattle.utils.FrameRateManager;
import main.java.com.tankbattle.utils.LogUtil;
import main.java.com.tankbattle.world.GameWorld;
import main.java.com.tankbattle.world.MapTile;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public class PlayingState extends BaseGameState {
    private Player player;
    private List<Enemy> enemies = new CopyOnWriteArrayList<>();
    private List<Bullet> bullets = new CopyOnWriteArrayList<>();
    private List<Explosion> explosions = new CopyOnWriteArrayList<>();
    private GameWorld gameWorld;
    private Random random = new Random();

    private float moveSoundTimer = 0f;
    private static final float MOVE_SOUND_INTERVAL = 0.3f;
    private boolean wasShooting = false;

    private float globalTimeScale = 1.0f;
    private float timeSlowTimer = 0f;

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

    private FrameRateManager frameRateManager = FrameRateManager.getInstance();

    // 底部按钮区域（扩大以提升点击成功率）
    private Rectangle restartButtonRect = new Rectangle(10, 545, 90, 35);
    private Rectangle pauseButtonRect = new Rectangle(110, 545, 90, 35);
    private Rectangle fullscreenButtonRect = new Rectangle(210, 545, 90, 35);
    private boolean mouseWasPressed = false;

    // ESC 菜单区域（扩大并居中）
    private boolean escMenuActive = false;
    private Rectangle saveProgressRect = new Rectangle(230, 200, 340, 45);
    private Rectangle saveManageRect = new Rectangle(230, 265, 340, 45);
    private Rectangle returnMenuRect = new Rectangle(230, 330, 340, 45);
    private Rectangle closeMenuRect = new Rectangle(230, 395, 340, 45);

    private static SaveData pendingSaveData = null;

    public static void setPendingSaveData(SaveData data) {
        pendingSaveData = data;
    }

    public PlayingState(GameManager gameManager) {
        super(gameManager);
    }

    @Override
    public void enter() {
        gameStartTime = System.currentTimeMillis();
        gameWorld = GameObjectFactory.createGameWorld();
        player = GameObjectFactory.createPlayerAtSafePosition(gameWorld);
        initializeEnemies(ConfigManager.getInstance().getInt(ConfigManager.KEY_ENEMY_COUNT));

        GameObjectPool.getInstance().clearAll();
        bullets.clear();
        explosions.clear();

        moveSoundTimer = 0f;
        wasShooting = false;
        totalEnemiesKilled = 0;
        totalShotsFired = 0;
        totalDamageTaken = 0;
        globalTimeScale = 1.0f;
        timeSlowTimer = 0f;
        escMenuActive = false;

        EventManager eventManager = EventManager.getInstance();
        eventManager.registerListener(gameEventListener,
                Event.EventType.GAME_STARTED, Event.EventType.PLAYER_SPAWNED,
                Event.EventType.PLAYER_DIED, Event.EventType.PLAYER_HIT,
                Event.EventType.ENEMY_SPAWNED, Event.EventType.ENEMY_DIED,
                Event.EventType.BULLET_FIRED, Event.EventType.EXPLOSION_CREATED,
                Event.EventType.GAME_OVER, Event.EventType.TIME_SLOW_START,
                Event.EventType.TIME_SLOW_END
        );

        eventManager.triggerEvent(GameEvent.gameStarted());
        ResourceManager.getInstance().playSound("start");
        ResourceManager.getInstance().playBackgroundMusic();

        if (pendingSaveData != null) {
            applySaveData(pendingSaveData);
            pendingSaveData = null;
        }
    }

    @Override
    public void exit() {
        EventManager.getInstance().unregisterListener(gameEventListener);
        ResourceManager.getInstance().stopBackgroundMusic();
        printGameStats();
    }

    @Override
    public void update(float deltaTime) {
        InputHandler input = gameManager.getInputHandler();
        if (input == null) return;

        // 全局按键处理（必须在暂停判断之前）
        if (input.pauseTyped) {
            gameManager.togglePause();
            input.pauseTyped = false;
        }

        if (input.escapeTyped) {
            if (gameManager.isPaused()) {
                gameManager.togglePause();
            } else {
                escMenuActive = !escMenuActive;
            }
            input.escapeTyped = false;
        }

        // 暂停或菜单激活时，不更新游戏逻辑
        if (escMenuActive || gameManager.isPaused()) {
            return;
        }

        // 正常游戏逻辑
        if (input.mouseLeftPressed && !mouseWasPressed) {
            handleMouseClick(input.mouseX, input.mouseY);
        }
        mouseWasPressed = input.mouseLeftPressed;

        float scaledDelta = deltaTime * globalTimeScale;
        if (globalTimeScale < 1.0f) {
            timeSlowTimer += deltaTime;
            if (timeSlowTimer >= 5.0f) {
                globalTimeScale = 1.0f;
                timeSlowTimer = 0f;
            }
        }

        if (input.isMoving()) {
            moveSoundTimer += deltaTime;
            if (moveSoundTimer >= MOVE_SOUND_INTERVAL) {
                ResourceManager.getInstance().playSound("move");
                moveSoundTimer -= MOVE_SOUND_INTERVAL;
            }
        } else {
            moveSoundTimer = 0f;
        }

        boolean isShootingNow = input.mouseLeftPressed;
        if (isShootingNow && !wasShooting) {
            totalShotsFired++;
        }
        wasShooting = isShootingNow;

        if (player != null) {
            player.update(scaledDelta, input, gameWorld, bullets);
        }

        for (Bullet b : bullets) {
            if (b != null) b.update(scaledDelta);
        }

        List<Bullet> bulletsToRemove = new ArrayList<>();
        for (Bullet bullet : bullets) {
            if (bullet == null || !bullet.isAlive()) {
                if (bullet != null) GameObjectPool.getInstance().returnBullet(bullet);
                bulletsToRemove.add(bullet);
                continue;
            }

            boolean shouldDestroy = checkBulletMapCollision(bullet);
            if (shouldDestroy) {
                bullet.setAlive(false);
                GameObjectPool.getInstance().returnBullet(bullet);
                bulletsToRemove.add(bullet);
            }
        }
        bullets.removeAll(bulletsToRemove);

        for (Enemy e : enemies) {
            if (e != null) e.update(scaledDelta, gameWorld, bullets);
        }

        List<Explosion> deadExps = new ArrayList<>();
        for (Explosion exp : explosions) {
            if (exp != null) {
                exp.update();
                if (!exp.alive) {
                    GameObjectPool.getInstance().returnExplosion(exp);
                    deadExps.add(exp);
                }
            }
        }
        explosions.removeAll(deadExps);

        CollisionManager.checkAllCollisions(bullets, enemies, player, explosions);

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

    private void handleMouseClick(int x, int y) {
        LogUtil.debug("handleMouseClick: (" + x + "," + y + ")");
        if (restartButtonRect.contains(x, y)) {
            LogUtil.debug("点击重新开始");
            restartGame();
        } else if (pauseButtonRect.contains(x, y)) {
            LogUtil.debug("点击暂停/继续");
            gameManager.togglePause();
        } else if (fullscreenButtonRect.contains(x, y)) {
            LogUtil.debug("点击全屏");
            SwingUtilities.invokeLater(() -> {
                for (java.awt.Window w : java.awt.Window.getWindows()) {
                    if (w instanceof GameWindow) {
                        ((GameWindow) w).toggleFullscreen();
                        break;
                    }
                }
            });
        }
    }

    private void restartGame() {
        gameManager.changeState(GameManager.GameStateType.PLAYING);
    }

    private boolean checkBulletMapCollision(Bullet bullet) {
        ConfigManager config = ConfigManager.getInstance();
        int tileSize = config.getInt(ConfigManager.KEY_TILE_SIZE);
        int mapWidth = config.getInt(ConfigManager.KEY_MAP_WIDTH);
        int mapHeight = config.getInt(ConfigManager.KEY_MAP_HEIGHT);
        int startX = bullet.getX(), startY = bullet.getY();
        int direction = bullet.getDirection();
        int speed = config.getInt(ConfigManager.KEY_BULLET_SPEED);
        int endX = startX, endY = startY;
        switch (direction) {
            case ConfigManager.DIR_UP: endY -= speed; break;
            case ConfigManager.DIR_DOWN: endY += speed; break;
            case ConfigManager.DIR_LEFT: endX -= speed; break;
            case ConfigManager.DIR_RIGHT: endX += speed; break;
        }
        if (endX < 0 || endX >= config.getInt(ConfigManager.KEY_WINDOW_WIDTH) ||
                endY < 0 || endY >= config.getInt(ConfigManager.KEY_WINDOW_HEIGHT)) {
            return true;
        }

        int x0 = startX / tileSize, y0 = startY / tileSize;
        int x1 = endX / tileSize, y1 = endY / tileSize;
        int dx = Math.abs(x1 - x0), dy = Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1, sy = y0 < y1 ? 1 : -1;
        int err = dx - dy;
        int x = x0, y = y0;
        while (true) {
            if (x >= 0 && x < mapWidth && y >= 0 && y < mapHeight) {
                MapTile tile = gameWorld.map[x][y];
                if (!tile.isPassable()) {
                    int type = tile.getType();
                    if (type == ConfigManager.TILE_BRICK) {
                        gameWorld.destroyTile(x, y);
                        Explosion exp = GameObjectFactory.createExplosion(
                                x * tileSize + tileSize / 2,
                                y * tileSize + tileSize / 2
                        );
                        if (exp != null) explosions.add(exp);
                        ResourceManager.getInstance().playSound("explosion");
                        EventManager.getInstance().triggerEvent(
                                GameEvent.explosionCreated(
                                        new Point(x * tileSize + tileSize / 2,
                                                y * tileSize + tileSize / 2)
                                )
                        );
                        if (!bullet.isPenetrating()) {
                            return true;
                        }
                    } else {
                        return true;
                    }
                }
            }
            if (x == x1 && y == y1) break;
            int e2 = 2 * err;
            if (e2 > -dy) { err -= dy; x += sx; }
            if (e2 < dx) { err += dx; y += sy; }
        }
        return false;
    }

    @Override
    public void render(Graphics g) {
        gameWorld.draw(g);
        InputHandler input = gameManager.getInputHandler();
        int mouseX = 400, mouseY = 300;
        if (input != null) {
            mouseX = input.mouseX;
            mouseY = input.mouseY;
        }
        if (player != null) player.draw(g, mouseX, mouseY);
        for (Enemy e : enemies) if (e != null) e.draw(g);
        for (Bullet b : bullets) if (b != null) b.draw(g);
        for (Explosion exp : explosions) if (exp != null) exp.draw(g);

        drawGameUI(g);
        drawCrosshair(g, mouseX, mouseY);

        if (gameManager.isPaused()) {
            drawPauseOverlay(g);
        }

        if (escMenuActive) {
            drawEscMenu(g);
        }
    }

    @Override
    public void mousePressed(int x, int y, int button) {
        LogUtil.debug("PlayingState mousePressed: (" + x + "," + y + ") button=" + button);
        if (escMenuActive) {
            LogUtil.debug("ESC菜单激活，检测按钮: saveProgress=" + saveProgressRect +
                    " saveManage=" + saveManageRect +
                    " returnMenu=" + returnMenuRect +
                    " closeMenu=" + closeMenuRect);
            if (saveProgressRect.contains(x, y)) {
                LogUtil.debug("点击保存进度");
                quickSaveWithData();
                escMenuActive = false;
            } else if (saveManageRect.contains(x, y)) {
                LogUtil.debug("点击存档管理");
                gameManager.changeState(GameManager.GameStateType.SAVE_LOAD);
                escMenuActive = false;
            } else if (returnMenuRect.contains(x, y)) {
                LogUtil.debug("点击返回主菜单");
                gameManager.changeState(GameManager.GameStateType.MENU);
                escMenuActive = false;
            } else if (closeMenuRect.contains(x, y)) {
                LogUtil.debug("点击关闭菜单");
                escMenuActive = false;
            } else {
                LogUtil.debug("未点击任何按钮");
            }
            return;
        }
        if (gameManager.isPaused()) return;
    }

    @Override
    public void keyPressed(int keyCode) {
        if (escMenuActive) {
            if (keyCode == KeyEvent.VK_ESCAPE) {
                escMenuActive = false;
            }
            return;
        }
        if (keyCode == KeyEvent.VK_F5) {
            quickSaveWithData();
        } else if (keyCode == KeyEvent.VK_F6) {
            loadAndApplySave();
        }
    }

    public SaveData createSaveData() {
        SaveData data = new SaveData("temp");
        populateSaveData(data);
        return data;
    }

    private void quickSaveWithData() {
        SaveData data = createSaveData();
        data.setSaveName("quicksave_" + System.currentTimeMillis());
        boolean success = SaveManager.getInstance().getSaveSystem().saveGame(data);
        if (success) {
            LogUtil.info("游戏已快速保存");
        } else {
            LogUtil.warning("保存失败");
        }
    }

    private void populateSaveData(SaveData data) {
        ConfigManager config = ConfigManager.getInstance();
        data.put("playerX", player.x);
        data.put("playerY", player.y);
        data.put("playerLives", player.lives);
        data.put("playerScore", player.getScore());
        data.put("totalEnemyCount", config.getInt(ConfigManager.KEY_ENEMY_COUNT));
        data.put("enemyCount", enemies.size());
        int idx = 0;
        for (Enemy e : enemies) {
            if (e != null && e.alive) {
                data.put("enemy" + idx + "X", e.x);
                data.put("enemy" + idx + "Y", e.y);
                data.put("enemy" + idx + "Direction", e.direction);
                idx++;
            }
        }
        data.put("enemyCount", idx);
        data.put("totalEnemiesKilled", totalEnemiesKilled);
        data.put("totalShotsFired", totalShotsFired);
        data.put("totalDamageTaken", totalDamageTaken);
        data.put("specialBulletCount", player.getSpecialBulletCount());

        StringBuilder destroyedTiles = new StringBuilder();
        int mapWidth = config.getInt(ConfigManager.KEY_MAP_WIDTH);
        int mapHeight = config.getInt(ConfigManager.KEY_MAP_HEIGHT);
        for (int x = 0; x < mapWidth; x++) {
            for (int y = 0; y < mapHeight; y++) {
                MapTile tile = gameWorld.map[x][y];
                if (tile.getType() == ConfigManager.TILE_EMPTY) {
                    destroyedTiles.append(x).append(",").append(y).append(";");
                }
            }
        }
        data.put("destroyedTiles", destroyedTiles.toString());
        data.put("gameVersion", "1.0.0");
    }

    private void loadAndApplySave() {
        SaveData data = SaveManager.getInstance().quickLoad();
        if (data != null) {
            applySaveData(data);
            LogUtil.info("存档已加载");
        } else {
            LogUtil.warning("没有可用的存档");
        }
    }

    private void applySaveData(SaveData data) {
        ConfigManager config = ConfigManager.getInstance();
        if (player != null) {
            player.x = data.getInt("playerX", player.x);
            player.y = data.getInt("playerY", player.y);
            player.lives = data.getInt("playerLives", player.lives);
            int savedScore = data.getInt("playerScore", player.getScore());
            player.addScore(savedScore - player.getScore());
            player.dead = false;
            int savedSpecial = data.getInt("specialBulletCount", player.getSpecialBulletCount());
            while (player.getSpecialBulletCount() < savedSpecial) player.addSpecialBullet(1);
        }

        enemies.clear();
        int enemyCount = data.getInt("enemyCount", 0);
        for (int i = 0; i < enemyCount; i++) {
            String keyX = "enemy" + i + "X";
            String keyY = "enemy" + i + "Y";
            String keyDir = "enemy" + i + "Direction";
            if (data.containsKey(keyX) && data.containsKey(keyY)) {
                int ex = data.getInt(keyX, 100);
                int ey = data.getInt(keyY, 100);
                Enemy enemy = GameObjectFactory.createEnemy(ex, ey);
                if (enemy != null) {
                    if (data.containsKey(keyDir)) {
                        enemy.direction = data.getInt(keyDir, 0);
                    }
                    enemies.add(enemy);
                }
            }
        }

        String destroyedStr = data.getString("destroyedTiles", "");
        if (!destroyedStr.isEmpty()) {
            String[] tiles = destroyedStr.split(";");
            for (String tileStr : tiles) {
                if (tileStr.isEmpty()) continue;
                String[] parts = tileStr.split(",");
                if (parts.length == 2) {
                    try {
                        int tx = Integer.parseInt(parts[0]);
                        int ty = Integer.parseInt(parts[1]);
                        gameWorld.destroyTile(tx, ty);
                    } catch (NumberFormatException ignored) {}
                }
            }
        }

        totalEnemiesKilled = data.getInt("totalEnemiesKilled", totalEnemiesKilled);
        totalShotsFired = data.getInt("totalShotsFired", totalShotsFired);
        totalDamageTaken = data.getInt("totalDamageTaken", totalDamageTaken);
        bullets.clear();
        explosions.clear();
    }

    private void handleGameEvent(GameEvent event) {
        switch (event.getType()) {
            case PLAYER_DIED:
                ResourceManager.getInstance().playSound("gameover");
                break;
            case PLAYER_HIT:
                totalDamageTaken += (int) event.getData();
                break;
            case ENEMY_DIED:
                totalEnemiesKilled++;
                if (player != null) player.addScore(100);
                break;
            case TIME_SLOW_START:
                globalTimeScale = (float) event.getData();
                timeSlowTimer = 0f;
                break;
            case TIME_SLOW_END:
                globalTimeScale = 1.0f;
                timeSlowTimer = 0f;
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
                if (enemy != null) {
                    enemies.add(enemy);
                    EventManager.getInstance().triggerEvent(GameEvent.enemySpawned(enemy));
                }
            }
        }
    }

    private void checkGameEndConditions() {
        if (player == null) return;
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
        ConfigManager config = ConfigManager.getInstance();
        int width = config.getInt(ConfigManager.KEY_WINDOW_WIDTH);
        int height = config.getInt(ConfigManager.KEY_WINDOW_HEIGHT);

        g.setColor(new Color(0, 0, 0, 150));
        g.fillRoundRect(width - 180, 5, 170, 130, 10, 10);
        g.setColor(Color.WHITE);
        g.setFont(rm.getSafeFont(Font.BOLD, 14));
        g.drawString("生命: " + (player != null ? player.lives : 0), width - 165, 25);
        g.drawString("敌人: " + enemies.size(), width - 165, 45);
        g.drawString("分数: " + (player != null ? player.getScore() : 0), width - 165, 65);
        g.drawString("特殊弹: " + (player != null ? player.getSpecialBulletCount() : 0), width - 165, 85);
        g.drawString("FPS: " + frameRateManager.getCurrentFPS(), width - 165, 105);
        if (globalTimeScale < 1.0f) {
            g.setColor(Color.CYAN);
            g.drawString("减速中...", width - 165, 125);
        }

        // 底部按钮
        g.setColor(new Color(50, 50, 50, 180));
        g.fillRoundRect(restartButtonRect.x, restartButtonRect.y, restartButtonRect.width, restartButtonRect.height, 8, 8);
        g.fillRoundRect(pauseButtonRect.x, pauseButtonRect.y, pauseButtonRect.width, pauseButtonRect.height, 8, 8);
        g.fillRoundRect(fullscreenButtonRect.x, fullscreenButtonRect.y, fullscreenButtonRect.width, fullscreenButtonRect.height, 8, 8);
        g.setColor(Color.WHITE);
        g.setFont(rm.getSafeFont(Font.BOLD, 12));
        drawCenteredString(g, "重新开始", restartButtonRect);
        drawCenteredString(g, gameManager.isPaused() ? "继续" : "暂停", pauseButtonRect);
        drawCenteredString(g, "全屏", fullscreenButtonRect);

        if (player != null) {
            int barWidth = 320;
            int barX = width / 2 - barWidth / 2;
            int barY = height - 50;
            g.setColor(new Color(0, 0, 0, 150));
            g.fillRoundRect(barX, barY, barWidth, 40, 10, 10);

            int iconSize = 50;
            int iconY = barY + 5;
            drawSkillIcon(g, player.getSkillQ(), barX + 20, iconY, iconSize);
            drawSkillIcon(g, player.getSkillE(), barX + 90, iconY, iconSize);
            drawSkillIcon(g, player.getSkillR(), barX + 160, iconY, iconSize);
            drawSkillIcon(g, player.getSkillF(), barX + 230, iconY, iconSize);
        }

        g.setColor(Color.LIGHT_GRAY);
        g.setFont(rm.getSafeFont(Font.PLAIN, 11));
        g.drawString("P:暂停  ESC:菜单", 10, height - 10);
    }

    private void drawSkillIcon(Graphics g, Skill skill, int x, int y, int size) {
        g.setColor(Color.DARK_GRAY);
        g.fillRoundRect(x, y, size, size, 8, 8);
        if (skill.isReady()) {
            g.setColor(Color.GREEN);
        } else if (skill.isActive()) {
            g.setColor(Color.CYAN);
        } else {
            g.setColor(Color.GRAY);
        }
        g.drawRoundRect(x, y, size, size, 8, 8);

        if (!skill.isReady() && !skill.isActive()) {
            float percent = skill.getCooldownPercent();
            int maskHeight = (int) (size * percent);
            g.setColor(new Color(0, 0, 0, 150));
            g.fillRoundRect(x, y, size, maskHeight, 8, 8);
        }

        g.setColor(Color.WHITE);
        g.setFont(ResourceManager.getInstance().getSafeFont(Font.BOLD, 16));
        FontMetrics fm = g.getFontMetrics();
        String icon = skill.getIconText();
        int iconX = x + (size - fm.stringWidth(icon)) / 2;
        int iconY = y + size / 2 + fm.getAscent() / 2 - 2;
        g.drawString(icon, iconX, iconY);

        if (!skill.isReady()) {
            g.setFont(ResourceManager.getInstance().getSafeFont(Font.PLAIN, 9));
            String timeText;
            if (skill.isActive()) {
                timeText = String.format("%.1f", skill.getRemainingActiveTime());
            } else {
                timeText = String.format("%.1f", skill.getRemainingCooldown());
            }
            fm = g.getFontMetrics();
            int timeX = x + (size - fm.stringWidth(timeText)) / 2;
            int timeY = y + size - 5;
            g.drawString(timeText, timeX, timeY);
        }
    }

    private void drawCenteredString(Graphics g, String text, Rectangle rect) {
        FontMetrics fm = g.getFontMetrics();
        int x = rect.x + (rect.width - fm.stringWidth(text)) / 2;
        int y = rect.y + ((rect.height - fm.getHeight()) / 2) + fm.getAscent();
        g.drawString(text, x, y);
    }

    private void drawPauseOverlay(Graphics g) {
        ConfigManager config = ConfigManager.getInstance();
        int w = config.getInt(ConfigManager.KEY_WINDOW_WIDTH);
        int h = config.getInt(ConfigManager.KEY_WINDOW_HEIGHT);
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, w, h);
        g.setColor(Color.WHITE);
        g.setFont(ResourceManager.getInstance().getSafeFont(Font.BOLD, 48));
        String text = "暂停";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(text, (w - fm.stringWidth(text)) / 2, h / 2);
        g.setFont(ResourceManager.getInstance().getSafeFont(Font.PLAIN, 18));
        String hint = "按 P 继续  |  ESC 菜单";
        fm = g.getFontMetrics();
        g.drawString(hint, (w - fm.stringWidth(hint)) / 2, h / 2 + 50);
    }

    private void drawEscMenu(Graphics g) {
        ConfigManager config = ConfigManager.getInstance();
        int w = config.getInt(ConfigManager.KEY_WINDOW_WIDTH);
        int h = config.getInt(ConfigManager.KEY_WINDOW_HEIGHT);

        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, w, h);

        g.setColor(new Color(30, 30, 30, 220));
        g.fillRoundRect(200, 150, 400, 320, 20, 20);
        g.setColor(Color.WHITE);
        g.drawRoundRect(200, 150, 400, 320, 20, 20);

        g.setFont(ResourceManager.getInstance().getSafeFont(Font.BOLD, 28));
        String title = "游戏菜单";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(title, w / 2 - fm.stringWidth(title) / 2, 195);

        drawMenuButton(g, saveProgressRect, "保存当前进度");
        drawMenuButton(g, saveManageRect, "存档管理");
        drawMenuButton(g, returnMenuRect, "返回主菜单");
        drawMenuButton(g, closeMenuRect, "关闭菜单");

        g.setColor(Color.LIGHT_GRAY);
        g.setFont(ResourceManager.getInstance().getSafeFont(Font.PLAIN, 14));
        String hint = "按 ESC 关闭菜单";
        g.drawString(hint, w / 2 - fm.stringWidth(hint) / 2, 455);
    }

    private void drawMenuButton(Graphics g, Rectangle rect, String text) {
        g.setColor(Color.GRAY);
        g.fillRoundRect(rect.x, rect.y, rect.width, rect.height, 10, 10);
        g.setColor(Color.WHITE);
        g.drawRoundRect(rect.x, rect.y, rect.width, rect.height, 10, 10);
        g.setFont(ResourceManager.getInstance().getSafeFont(Font.PLAIN, 18));
        FontMetrics fm = g.getFontMetrics();
        int tx = rect.x + (rect.width - fm.stringWidth(text)) / 2;
        int ty = rect.y + (rect.height - fm.getHeight()) / 2 + fm.getAscent();
        g.drawString(text, tx, ty);
    }

    private void drawCrosshair(Graphics g, int mouseX, int mouseY) {
        g.setColor(Color.RED);
        g.drawLine(mouseX - 10, mouseY, mouseX + 10, mouseY);
        g.drawLine(mouseX, mouseY - 10, mouseX, mouseY + 10);
        g.drawOval(mouseX - 5, mouseY - 5, 10, 10);
    }

    private void printGameStats() {
        long gameTime = (System.currentTimeMillis() - gameStartTime) / 1000;
        LogUtil.info("========== 本局结束 ==========");
    }
}