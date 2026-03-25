package main.java.com.tankbattle.core;

import main.java.com.tankbattle.entity.*;
import java.util.List;

public class CollisionManager {
    public static void checkBulletEnemyCollisions(List<Bullet> bullets, List<Enemy> enemies, List<Explosion> explosions) {
        for (Bullet bullet : bullets) {
            if (!bullet.isAlive()) continue;
            if (!bullet.isFromPlayer()) {
                continue;
            }
            for (Enemy enemy : enemies) {
                if (enemy.alive && bullet.collidesWith(enemy.x, enemy.y, GameConfig.TANK_WIDTH)) {
                    bullet.setAlive(false);
                    enemy.alive = false;
                    explosions.add(new Explosion(enemy.x + GameConfig.TANK_WIDTH/2, enemy.y + GameConfig.TANK_HEIGHT/2));
                    break;
                }
            }
        }
    }

    public static void checkBulletPlayerCollisions(List<Bullet> bullets, Player player, List<Explosion> explosions) {
        for (Bullet bullet : bullets) {
            if (!bullet.isAlive()) continue;
            if (bullet.isFromPlayer()) {
                continue;
            }
            if (bullet.collidesWith(player.x, player.y, GameConfig.TANK_WIDTH)) {
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
        for (Enemy enemy : enemies) {
            if (enemy.alive && enemy.collidesWith(player.x, player.y)) {
                player.lives--;
                enemy.alive = false;
                explosions.add(new Explosion(enemy.x + GameConfig.TANK_WIDTH/2, enemy.y + GameConfig.TANK_HEIGHT/2));
                if (player.lives <= 0) {
                    player.lives = 0;
                    player.dead = true;
                }
            }
        }
    }
}