package main.java.com.tankbattle.core;

import main.java.com.tankbattle.entity.*;
import main.java.com.tankbattle.world.GameWorld;
import main.java.com.tankbattle.world.MapTile;

import java.util.List;
import java.util.Random;

public class GameLogic {
    // 游戏状态枚举
    public enum GameState {
        START,      // 开始界面
        PLAYING,    // 游戏中
        VICTORY,    // 游戏胜利
        GAME_OVER   // 游戏结束
    }

    private GameState gameState = GameState.START;
    private Player player;
    private List<Enemy> enemies;
    private List<Bullet> bullets;
    private List<Explosion> explosions;
    private GameWorld world;
    private InputHandler input;
    private Random random = new Random();

    // 音效触发控制
    private boolean wasPlaying = false; // 上次游戏状态
    private int moveSoundCooldown = 0;

    public GameLogic(Player player, List<Enemy> enemies, List<Bullet> bullets,
                     List<Explosion> explosions, GameWorld world, InputHandler input) {
        this.player = player;
        this.enemies = enemies;
        this.bullets = bullets;
        this.explosions = explosions;
        this.world = world;
        this.input = input;
    }

    public void update() {
        // 处理游戏状态变化时的音效
        handleGameStateSounds();

        switch (gameState) {
            case START:
                updateStartState();
                break;
            case PLAYING:
                updatePlayingState();
                break;
            case VICTORY:
            case GAME_OVER:
                updateEndState();
                break;
        }
    }

    private void handleGameStateSounds() {
        ResourceManager resourceManager = ResourceManager.getInstance();

        // 当游戏状态变化时播放相应音效
        if (!wasPlaying && gameState == GameState.PLAYING) {
            // 从开始界面进入游戏
            resourceManager.playSound("start");
            resourceManager.playBackgroundMusic();
        } else if (gameState == GameState.VICTORY && wasPlaying) {
            // 游戏胜利
            resourceManager.stopBackgroundMusic();
            resourceManager.playSound("victory");
        } else if (gameState == GameState.GAME_OVER && wasPlaying) {
            // 游戏失败
            resourceManager.stopBackgroundMusic();
            resourceManager.playSound("gameover");
        } else if (gameState == GameState.START && wasPlaying) {
            // 返回开始界面
            resourceManager.stopBackgroundMusic();
        }

        wasPlaying = (gameState == GameState.PLAYING);
    }

    private void updateStartState() {
        if (input.spacePressed || input.rPressed) {
            gameState = GameState.PLAYING;
            input.spacePressed = false;
            input.rPressed = false;
        }
    }

    private void updatePlayingState() {
        ResourceManager resourceManager = ResourceManager.getInstance();
        ConfigManager config = ConfigManager.getInstance();

        // 播放移动音效
        if (moveSoundCooldown <= 0 && (input.upPressed || input.downPressed || input.leftPressed || input.rightPressed)) {
            resourceManager.playSound("move");
            moveSoundCooldown = 10; // 冷却时间
        }
        if (moveSoundCooldown > 0) moveSoundCooldown--;

        // 玩家更新
        player.update(input.upPressed, input.downPressed, input.leftPressed, input.rightPressed,
                input.spacePressed, input.mouseX, input.mouseY, world, bullets);

        // 播放射击音效
        if (input.spacePressed) {
            resourceManager.playSound("shoot");
        }

        for (Bullet b : bullets) b.update();

        // 子弹与地图碰撞检测
        for (Bullet bullet : bullets) {
            if (!bullet.isAlive()) continue;
            int bx = bullet.getX();
            int by = bullet.getY();
            int tileSize = config.getInt(ConfigManager.KEY_TILE_SIZE);
            int tileX = bx / tileSize;
            int tileY = by / tileSize;
            int mapWidth = config.getInt(ConfigManager.KEY_MAP_WIDTH);
            int mapHeight = config.getInt(ConfigManager.KEY_MAP_HEIGHT);

            if (tileX >= 0 && tileX < mapWidth && tileY >= 0 && tileY < mapHeight) {
                MapTile tile = world.map[tileX][tileY];
                if (!tile.isPassable()) {
                    int type = tile.getType();
                    if (type == ConfigManager.TILE_BRICK) {
                        world.destroyTile(tileX, tileY);
                        explosions.add(new Explosion(
                                tileX * tileSize + tileSize / 2,
                                tileY * tileSize + tileSize / 2
                        ));
                        resourceManager.playSound("explosion");
                    }
                    bullet.setAlive(false);
                }
            }
        }

        bullets.removeIf(b -> !b.isAlive());
        for (Enemy e : enemies) e.update(world, bullets);
        for (Explosion exp : explosions) exp.update();
        explosions.removeIf(exp -> !exp.alive);

        // 处理碰撞检测
        handleCollisions();

        // 检查游戏状态变化
        if (player.dead) {
            gameState = GameState.GAME_OVER;
        } else if (enemies.isEmpty()) {
            gameState = GameState.VICTORY;
        }
    }

    private void handleCollisions() {
        ResourceManager resourceManager = ResourceManager.getInstance();

        // 子弹与敌人碰撞
        for (Bullet bullet : bullets) {
            if (!bullet.isAlive()) continue;
            if (!bullet.isFromPlayer()) continue;
            for (Enemy enemy : enemies) {
                if (enemy.alive && bullet.collidesWith(enemy.x, enemy.y,
                        ConfigManager.getInstance().getInt(ConfigManager.KEY_TANK_WIDTH))) {
                    bullet.setAlive(false);
                    enemy.alive = false;
                    ConfigManager config = ConfigManager.getInstance();
                    int tankWidth = config.getInt(ConfigManager.KEY_TANK_WIDTH);
                    explosions.add(new Explosion(enemy.x + tankWidth/2, enemy.y + tankWidth/2));
                    resourceManager.playSound("explosion");
                    break;
                }
            }
        }

        // 子弹与玩家碰撞
        for (Bullet bullet : bullets) {
            if (!bullet.isAlive()) continue;
            if (bullet.isFromPlayer()) continue;
            if (bullet.collidesWith(player.x, player.y,
                    ConfigManager.getInstance().getInt(ConfigManager.KEY_TANK_WIDTH))) {
                bullet.setAlive(false);
                player.lives--;
                resourceManager.playSound("hit");
                if (player.lives <= 0) {
                    player.lives = 0;
                    player.dead = true;
                }
            }
        }

        // 敌人与玩家碰撞
        for (Enemy enemy : enemies) {
            if (enemy.alive && enemy.collidesWith(player.x, player.y)) {
                player.lives--;
                enemy.alive = false;
                ConfigManager config = ConfigManager.getInstance();
                int tankWidth = config.getInt(ConfigManager.KEY_TANK_WIDTH);
                explosions.add(new Explosion(enemy.x + tankWidth/2, enemy.y + tankWidth/2));
                resourceManager.playSound("explosion");
                if (player.lives <= 0) {
                    player.lives = 0;
                    player.dead = true;
                }
            }
        }

        enemies.removeIf(e -> !e.alive);
    }

    private void updateEndState() {
        if (input.rPressed) {
            restartGame();
            input.rPressed = false;
        }
    }

    public void restartGame() {
        // 重置玩家
        player.reset();

        // 清空所有列表
        bullets.clear();
        explosions.clear();

        // 重新生成敌人
        initializeEnemies(ConfigManager.getInstance().getInt(ConfigManager.KEY_ENEMY_COUNT));

        // 重置游戏状态
        gameState = GameState.START;
    }

    public void initializeEnemies(int enemyCount) {
        enemies.clear();
        for (int i = 0; i < enemyCount; i++) {
            int x, y;
            ConfigManager config = ConfigManager.getInstance();
            int windowWidth = config.getInt(ConfigManager.KEY_WINDOW_WIDTH);
            int windowHeight = config.getInt(ConfigManager.KEY_WINDOW_HEIGHT);
            int tankWidth = config.getInt(ConfigManager.KEY_TANK_WIDTH);
            int tankHeight = config.getInt(ConfigManager.KEY_TANK_HEIGHT);

            do {
                x = random.nextInt(windowWidth - tankWidth);
                y = random.nextInt(windowHeight - tankHeight);
            } while (!world.isPositionPassable(x, y, tankWidth, tankHeight));
            enemies.add(new Enemy(x, y));
        }
    }

    public GameState getGameState() {
        return gameState;
    }

    public Player getPlayer() {
        return player;
    }

    public int getEnemyCount() {
        return enemies.size();
    }
}