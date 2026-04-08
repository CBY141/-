package main.java.com.tankbattle.core;

import main.java.com.tankbattle.entity.*;
import java.util.List;

public class CollisionManager {
    public static void checkBulletEnemyCollisions(List<Bullet> bullets, List<Enemy> enemies, List<Explosion> explosions) {
        ConfigManager config = ConfigManager.getInstance();
        int tankWidth = config.getInt(ConfigManager.KEY_TANK_WIDTH);

        for (Bullet bullet : bullets) {
            if (!bullet.isAlive()) continue;
            if (!bullet.isFromPlayer()) {
                continue;
            }
            for (Enemy enemy : enemies) {
                if (enemy.alive && bullet.collidesWith(enemy.x, enemy.y, tankWidth)) {
                    bullet.setAlive(false);
                    enemy.alive = false;
                    explosions.add(new Explosion(enemy.x + tankWidth/2, enemy.y + tankWidth/2));
                    break;
                }
            }
        }
    }

    public static void checkBulletPlayerCollisions(List<Bullet> bullets, Player player, List<Explosion> explosions) {
        ConfigManager config = ConfigManager.getInstance();
        int tankWidth = config.getInt(ConfigManager.KEY_TANK_WIDTH);

        for (Bullet bullet : bullets) {
            if (!bullet.isAlive()) continue;
            if (bullet.isFromPlayer()) {
                continue;
            }
            if (bullet.collidesWith(player.x, player.y, tankWidth)) {
                bullet.setAlive(false);
                player.lives--;
                if (player.lives <= 0) {
                    player.lives = 0;
                    player.dead = true;
                }
            }
        }
    }

    public static void checkEnemyPlayerCollisions(List<Enemy> enemies, Player player, List<Explosion> explosions) {
        ConfigManager config = ConfigManager.getInstance();
        int tankWidth = config.getInt(ConfigManager.KEY_TANK_WIDTH);

        for (Enemy enemy : enemies) {
            if (enemy.alive && enemy.collidesWith(player.x, player.y)) {
                player.lives--;
                enemy.alive = false;
                explosions.add(new Explosion(enemy.x + tankWidth/2, enemy.y + tankWidth/2));
                if (player.lives <= 0) {
                    player.lives = 0;
                    player.dead = true;
                }
            }
        }
    }
}