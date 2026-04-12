package main.java.com.tankbattle.core;

import main.java.com.tankbattle.entity.*;
import main.java.com.tankbattle.world.GameWorld;
import java.awt.Point;

/**
 * 游戏对象工厂
 */
public class GameObjectFactory {
    private static ConfigManager config = ConfigManager.getInstance();

    private GameObjectFactory() {}

    /**
     * 创建玩家坦克
     */
    public static Player createPlayer(int x, int y) {
        return new Player(x, y);
    }

    /**
     * 创建玩家坦克（在安全位置）
     */
    public static Player createPlayerAtSafePosition(GameWorld world) {
        Point safePoint = findSafeSpawnPoint(world);
        return createPlayer(safePoint.x, safePoint.y);
    }

    /**
     * 创建敌人坦克
     */
    public static Enemy createEnemy(int x, int y) {
        return new Enemy(x, y);
    }

    /**
     * 创建子弹
     */
    public static Bullet createBullet(int x, int y, int direction, boolean fromPlayer) {
        return new Bullet(x, y, direction, fromPlayer);
    }

    /**
     * 创建爆炸效果
     */
    public static Explosion createExplosion(int x, int y) {
        return new Explosion(x, y);
    }

    /**
     * 创建游戏世界
     */
    public static GameWorld createGameWorld() {
        return new GameWorld();
    }

    private static Point findSafeSpawnPoint(GameWorld world) {
        int centerX = config.getInt(ConfigManager.KEY_MAP_WIDTH) / 2;
        int centerY = config.getInt(ConfigManager.KEY_MAP_HEIGHT) / 2;

        Point[] possibleSpawns = {
                new Point(centerX + 5, centerY + 5),
                new Point(centerX - 5, centerY - 5),
                new Point(centerX + 8, centerY - 8),
                new Point(centerX - 8, centerY + 8),
                new Point(centerX, centerY + 12),
                new Point(centerX, centerY - 12),
                new Point(centerX + 12, centerY),
                new Point(centerX - 12, centerY)
        };

        for (Point tilePos : possibleSpawns) {
            int pixelX = tilePos.x * config.getInt(ConfigManager.KEY_TILE_SIZE);
            int pixelY = tilePos.y * config.getInt(ConfigManager.KEY_TILE_SIZE);

            if (world.isPositionPassable(pixelX, pixelY,
                    config.getInt(ConfigManager.KEY_TANK_WIDTH),
                    config.getInt(ConfigManager.KEY_TANK_HEIGHT))) {
                return new Point(pixelX, pixelY);
            }
        }

        return new Point(
                centerX * config.getInt(ConfigManager.KEY_TILE_SIZE),
                centerY * config.getInt(ConfigManager.KEY_TILE_SIZE)
        );
    }
}