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

        // 玩家子弹 vs 敌人
        for (Bullet bullet : bullets) {
            if (bullet == null || !bullet.isAlive() || !bullet.isFromPlayer()) continue;
            RECT_A.setBounds(bullet.getX() - bulletSize/2, bullet.getY() - bulletSize/2, bulletSize, bulletSize);
            for (Enemy enemy : enemies) {
                if (enemy != null && enemy.alive) {
                    RECT_B.setBounds(enemy.x, enemy.y, tankWidth, tankWidth);
                    if (RECT_A.intersects(RECT_B)) {
                        handleBulletHitEnemy(bullet, enemy, explosions);
                        break; // 穿透子弹可能继续，所以用break跳出内层循环
                    }
                }
            }
        }

        // 敌人子弹 vs 玩家
        for (Bullet bullet : bullets) {
            if (bullet == null || !bullet.isAlive() || bullet.isFromPlayer()) continue;
            RECT_A.setBounds(bullet.getX() - bulletSize/2, bullet.getY() - bulletSize/2, bulletSize, bulletSize);
            RECT_B.setBounds(player.x, player.y, tankWidth, tankWidth);
            if (RECT_A.intersects(RECT_B)) {
                handleBulletHitPlayer(bullet, player, explosions);
            }
        }

        // 敌人 vs 玩家（碰撞）
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
        // 使用子弹的伤害值
        enemy.takeDamage(bullet.getDamage());

        // 生成爆炸效果
        ConfigManager config = ConfigManager.getInstance();
        int tankWidth = config.getInt(ConfigManager.KEY_TANK_WIDTH);
        Explosion exp = GameObjectFactory.createExplosion(enemy.x + tankWidth/2, enemy.y + tankWidth/2);
        if (exp != null) explosions.add(exp);

        ResourceManager.getInstance().playSound("explosion");
        EventManager.getInstance().triggerEvent(GameEvent.enemyHit(enemy, bullet.getDamage()));

        // 如果不是穿透子弹，子弹消失
        if (!bullet.isPenetrating()) {
            bullet.setAlive(false);
        }
        // 如果敌人死亡，触发事件
        if (!enemy.alive) {
            EventManager.getInstance().triggerEvent(GameEvent.enemyDied(enemy));
        }
    }

    private static void handleBulletHitPlayer(Bullet bullet, Player player, List<Explosion> explosions) {
        bullet.setAlive(false);
        player.takeDamage(1);
        // 玩家受伤音效由事件触发，不在此处重复
    }

    private static void handleEnemyHitPlayer(Enemy enemy, Player player, List<Explosion> explosions) {
        enemy.alive = false;

        ConfigManager config = ConfigManager.getInstance();
        int tankWidth = config.getInt(ConfigManager.KEY_TANK_WIDTH);

        Explosion exp = GameObjectFactory.createExplosion(enemy.x + tankWidth/2, enemy.y + tankWidth/2);
        if (exp != null) explosions.add(exp);

        ResourceManager.getInstance().playSound("explosion");
        player.takeDamage(1);
        EventManager.getInstance().triggerEvent(GameEvent.enemyDied(enemy));
    }
}