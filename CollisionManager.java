package main.java.com.tankbattle.core;

import main.java.com.tankbattle.entity.*;
import java.util.List;
import java.awt.Rectangle;

/**
 * 碰撞管理器
 */
public class CollisionManager {

    /**
     * 检测所有碰撞
     */
    public static void checkAllCollisions(List<Bullet> bullets, List<Enemy> enemies,
                                          Player player, List<Explosion> explosions) {
        if (player == null || player.dead) return;

        ConfigManager config = ConfigManager.getInstance();
        int tankWidth = config.getInt(ConfigManager.KEY_TANK_WIDTH);
        int bulletSize = config.getInt(ConfigManager.KEY_BULLET_SIZE);

        // 1. 玩家子弹与敌人碰撞
        for (Bullet bullet : bullets) {
            if (!bullet.isAlive() || !bullet.isFromPlayer()) continue;

            Rectangle bulletRect = new Rectangle(
                    bullet.getX() - bulletSize/2,
                    bullet.getY() - bulletSize/2,
                    bulletSize, bulletSize
            );

            for (Enemy enemy : enemies) {
                if (enemy.alive) {
                    Rectangle enemyRect = new Rectangle(
                            enemy.x, enemy.y,
                            tankWidth, tankWidth
                    );

                    if (bulletRect.intersects(enemyRect)) {
                        handleBulletHitEnemy(bullet, enemy, explosions);
                        break;
                    }
                }
            }
        }

        // 2. 敌人子弹与玩家碰撞
        for (Bullet bullet : bullets) {
            if (!bullet.isAlive() || bullet.isFromPlayer()) continue;

            Rectangle bulletRect = new Rectangle(
                    bullet.getX() - bulletSize/2,
                    bullet.getY() - bulletSize/2,
                    bulletSize, bulletSize
            );

            Rectangle playerRect = new Rectangle(
                    player.x, player.y,
                    tankWidth, tankWidth
            );

            if (bulletRect.intersects(playerRect)) {
                handleBulletHitPlayer(bullet, player, explosions);
            }
        }

        // 3. 敌人与玩家碰撞
        for (Enemy enemy : enemies) {
            if (enemy.alive) {
                Rectangle enemyRect = new Rectangle(
                        enemy.x, enemy.y,
                        tankWidth, tankWidth
                );

                Rectangle playerRect = new Rectangle(
                        player.x, player.y,
                        tankWidth, tankWidth
                );

                if (enemyRect.intersects(playerRect)) {
                    handleEnemyHitPlayer(enemy, player, explosions);
                }
            }
        }
    }

    private static void handleBulletHitEnemy(Bullet bullet, Enemy enemy, List<Explosion> explosions) {
        bullet.setAlive(false);
        enemy.alive = false;

        ConfigManager config = ConfigManager.getInstance();
        int tankWidth = config.getInt(ConfigManager.KEY_TANK_WIDTH);

        explosions.add(new Explosion(
                enemy.x + tankWidth/2,
                enemy.y + tankWidth/2
        ));

        ResourceManager.getInstance().playSound("explosion");
        EventManager.getInstance().triggerEvent(GameEvent.enemyDied(enemy));
    }

    private static void handleBulletHitPlayer(Bullet bullet, Player player, List<Explosion> explosions) {
        bullet.setAlive(false);
        player.takeDamage(1);
        ResourceManager.getInstance().playSound("hit");
    }

    private static void handleEnemyHitPlayer(Enemy enemy, Player player, List<Explosion> explosions) {
        enemy.alive = false;

        ConfigManager config = ConfigManager.getInstance();
        int tankWidth = config.getInt(ConfigManager.KEY_TANK_WIDTH);

        explosions.add(new Explosion(
                enemy.x + tankWidth/2,
                enemy.y + tankWidth/2
        ));

        ResourceManager.getInstance().playSound("explosion");
        player.takeDamage(1);
        EventManager.getInstance().triggerEvent(GameEvent.enemyDied(enemy));
    }
}