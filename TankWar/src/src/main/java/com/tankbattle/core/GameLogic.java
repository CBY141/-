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

    private GameState gameState = GameState.START;  // 初始为开始状态
    private Player player;
    private List<Enemy> enemies;
    private List<Bullet> bullets;
    private List<Explosion> explosions;
    private GameWorld world;
    private InputHandler input;
    private Random random = new Random();

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
        // 根据游戏状态执行不同的逻辑
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

    private void updateStartState() {
        // 在开始界面，按空格键开始游戏
        if (input.spacePressed) {
            gameState = GameState.PLAYING;
            input.spacePressed = false; // 重置按键状态
        }

        // 按R键也可以开始游戏
        if (input.rPressed) {
            gameState = GameState.PLAYING;
            input.rPressed = false;
        }
    }

    private void updatePlayingState() {
        // 原来的游戏逻辑
        player.update(input.upPressed, input.downPressed, input.leftPressed, input.rightPressed,
                input.spacePressed, input.mouseX, input.mouseY, world, bullets);
        for (Bullet b : bullets) b.update();

        for (Bullet bullet : bullets) {
            if (!bullet.isAlive()) continue;
            int bx = bullet.getX();
            int by = bullet.getY();
            int tileX = bx / GameConfig.TILE_SIZE;
            int tileY = by / GameConfig.TILE_SIZE;
            if (tileX >= 0 && tileX < GameConfig.MAP_WIDTH && tileY >= 0 && tileY < GameConfig.MAP_HEIGHT) {
                MapTile tile = world.map[tileX][tileY];
                if (!tile.isPassable()) {
                    int type = tile.getType();
                    if (type == GameConfig.TILE_BRICK) {
                        world.destroyTile(tileX, tileY);
                        explosions.add(new Explosion(
                                tileX * GameConfig.TILE_SIZE + GameConfig.TILE_SIZE / 2,
                                tileY * GameConfig.TILE_SIZE + GameConfig.TILE_SIZE / 2
                        ));
                    }
                    bullet.setAlive(false);
                }
            }
        }

        bullets.removeIf(b -> !b.isAlive());
        for (Enemy e : enemies) e.update(world, bullets);
        for (Explosion exp : explosions) exp.update();
        explosions.removeIf(exp -> !exp.alive);
        CollisionManager.checkBulletEnemyCollisions(bullets, enemies, explosions);
        CollisionManager.checkBulletPlayerCollisions(bullets, player, explosions);
        CollisionManager.checkEnemyPlayerCollisions(enemies, player, explosions);
        enemies.removeIf(e -> !e.alive);

        // 检查游戏状态变化
        if (player.dead) {
            gameState = GameState.GAME_OVER;
        } else if (enemies.isEmpty()) {
            gameState = GameState.VICTORY;
        }
    }

    private void updateEndState() {
        // 在结束状态，按R键重新开始游戏
        if (input.rPressed) {
            restartGame();
            input.rPressed = false; // 重置按键状态
        }
    }

    // 重新开始游戏
    public void restartGame() {
        // 重置玩家
        player.reset();

        // 清空所有列表
        bullets.clear();
        explosions.clear();

        // 重新生成敌人
        initializeEnemies(GameConfig.ENEMY_COUNT);

        // 重置游戏世界（如果需要可以重新生成地图）
        // world = new GameWorld();

        // 返回开始状态
        gameState = GameState.START;
    }

    public void initializeEnemies(int enemyCount) {
        enemies.clear();
        for (int i = 0; i < enemyCount; i++) {
            int x, y;
            do {
                x = random.nextInt(GameConfig.WINDOW_WIDTH - GameConfig.TANK_WIDTH);
                y = random.nextInt(GameConfig.WINDOW_HEIGHT - GameConfig.TANK_HEIGHT);
            } while (!world.isPositionPassable(x, y, GameConfig.TANK_WIDTH, GameConfig.TANK_HEIGHT));
            enemies.add(new Enemy(x, y));
        }
    }

    // 获取当前游戏状态
    public GameState getGameState() {
        return gameState;
    }

    // 获取玩家对象（用于UI显示）
    public Player getPlayer() {
        return player;
    }

    // 获取敌人数量（用于UI显示）
    public int getEnemyCount() {
        return enemies.size();
    }
}