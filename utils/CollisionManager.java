package main.java.com.tankbattle.utils;

import main.java.com.tankbattle.core.ConfigManager;
import main.java.com.tankbattle.system.GameEvent;
import main.java.com.tankbattle.core.ResourceManager;
import main.java.com.tankbattle.entity.*;
import main.java.com.tankbattle.managers.EventManager;
import main.java.com.tankbattle.game.factory.GameObjectFactory;

import java.util.List;
import java.awt.Rectangle;

public class CollisionManager {

    private static final Rectangle RECT_A = new Rectangle();
    private static final Rectangle RECT_B = new Rectangle();

    public static void checkAllCollisions(List<Bullet> bullets, List<Enemy> enemies,
                                          Player player, List<Explosion> explosions) {
        if (player == null || player.dead) return;

        ConfigManager config = ConfigManager.getInstance();
        int tankWidth = config.getInt(ConfigManager.KEY_TANK_WIDTH);
        int bulletSize = config.getInt(ConfigManager.KEY_BULLET_SIZE);

        for (Bullet bullet : bullets) {
            if (bullet == null || !bullet.isAlive() || !bullet.isFromPlayer()) continue;
            RECT_A.setBounds(bullet.getX() - bulletSize/2, bullet.getY() - bulletSize/2, bulletSize, bulletSize);
            for (Enemy enemy : enemies) {
                if (enemy != null && enemy.alive) {
                    RECT_B.setBounds(enemy.x, enemy.y, tankWidth, tankWidth);
                    if (RECT_A.intersects(RECT_B)) {
                        handleBulletHitEnemy(bullet, enemy, explosions);
                        break;
                    }
                }
            }
        }

        for (Bullet bullet : bullets) {
            if (bullet == null || !bullet.isAlive() || bullet.isFromPlayer()) continue;
            RECT_A.setBounds(bullet.getX() - bulletSize/2, bullet.getY() - bulletSize/2, bulletSize, bulletSize);
            RECT_B.setBounds(player.x, player.y, tankWidth, tankWidth);
            if (RECT_A.intersects(RECT_B)) {
                handleBulletHitPlayer(bullet, player, explosions);
            }
        }

        for (Enemy enemy : enemies) {
            if (enemy != null && enemy.alive) {
                RECT_A.setBounds(enemy.x, enemy.y, tankWidth, tankWidth);
                RECT_B.setBounds(player.x, player.y, tankWidth, tankWidth);
                if (RECT_A.intersects(RECT_B)) {
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

        Explosion exp = GameObjectFactory.createExplosion(enemy.x + tankWidth/2, enemy.y + tankWidth/2);
        if (exp != null) explosions.add(exp); // 防空判断

        ResourceManager.getInstance().playSound("explosion");
        EventManager.getInstance().triggerEvent(GameEvent.enemyDied(enemy));
    }

    private static void handleBulletHitPlayer(Bullet bullet, Player player, List<Explosion> explosions) {
        bullet.setAlive(false);
        player.takeDamage(1);
    }

    private static void handleEnemyHitPlayer(Enemy enemy, Player player, List<Explosion> explosions) {
        enemy.alive = false;

        ConfigManager config = ConfigManager.getInstance();
        int tankWidth = config.getInt(ConfigManager.KEY_TANK_WIDTH);

        Explosion exp = GameObjectFactory.createExplosion(enemy.x + tankWidth/2, enemy.y + tankWidth/2);
        if (exp != null) explosions.add(exp); // 防空判断

        ResourceManager.getInstance().playSound("explosion");
        player.takeDamage(1);
        EventManager.getInstance().triggerEvent(GameEvent.enemyDied(enemy));
    }
}