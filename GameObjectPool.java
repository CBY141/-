package main.java.com.tankbattle.core;

import main.java.com.tankbattle.entity.*;
import java.awt.Point;
import java.util.function.Supplier;

/**
 * 游戏对象池管理器
 */
public class GameObjectPool {
    private static GameObjectPool instance;

    // 子弹对象池
    private ObjectPool<Bullet> bulletPool;

    // 爆炸效果对象池
    private ObjectPool<Explosion> explosionPool;

    // 敌人对象池
    private ObjectPool<Enemy> enemyPool;

    private GameObjectPool() {
        initializePools();
    }

    public static synchronized GameObjectPool getInstance() {
        if (instance == null) {
            instance = new GameObjectPool();
        }
        return instance;
    }

    private void initializePools() {
        // 子弹对象池
        bulletPool = new ObjectPool<>(() ->
                new Bullet(0, 0, ConfigManager.DIR_UP, true), 20, 100);

        // 爆炸对象池
        explosionPool = new ObjectPool<>(() ->
                new Explosion(0, 0), 10, 50);

        // 敌人对象池
        enemyPool = new ObjectPool<>(() ->
                new Enemy(0, 0), 5, 20);
    }

    // 获取子弹
    public Bullet borrowBullet(int x, int y, int direction, boolean fromPlayer) {
        Bullet bullet = bulletPool.borrow();
        if (bullet != null) {
            // 重置子弹状态
            // 注意：这里需要Bullet类有重置方法
            // 暂时创建新对象
            bullet = new Bullet(x, y, direction, fromPlayer);
        }
        return bullet;
    }

    // 归还子弹
    public void returnBullet(Bullet bullet) {
        if (bullet != null) {
            bulletPool.returnObject(bullet);
        }
    }

    // 获取爆炸
    public Explosion borrowExplosion(int x, int y) {
        Explosion explosion = explosionPool.borrow();
        if (explosion != null) {
            // 重置爆炸状态
            explosion.x = x;
            explosion.y = y;
            explosion.size = 5;
            explosion.alive = true;
        }
        return explosion;
    }

    // 归还爆炸
    public void returnExplosion(Explosion explosion) {
        if (explosion != null) {
            explosionPool.returnObject(explosion);
        }
    }

    // 获取敌人
    public Enemy borrowEnemy(int x, int y) {
        Enemy enemy = enemyPool.borrow();
        if (enemy != null) {
            // 重置敌人状态
            enemy.x = x;
            enemy.y = y;
            enemy.alive = true;
            enemy.direction = (int)(Math.random() * 4);
        }
        return enemy;
    }

    // 归还敌人
    public void returnEnemy(Enemy enemy) {
        if (enemy != null) {
            enemyPool.returnObject(enemy);
        }
    }

    // 清理所有对象池
    public void clearAll() {
        bulletPool.clear();
        explosionPool.clear();
        enemyPool.clear();
    }

    // 获取统计信息
    public void printStats() {
        System.out.println("=== 对象池统计 ===");
        System.out.println("子弹池: 可用=" + bulletPool.availableCount() +
                " 使用中=" + bulletPool.inUseCount() +
                " 总数=" + bulletPool.size());
        System.out.println("爆炸池: 可用=" + explosionPool.availableCount() +
                " 使用中=" + explosionPool.inUseCount() +
                " 总数=" + explosionPool.size());
        System.out.println("敌⼈池: 可用=" + enemyPool.availableCount() +
                " 使用中=" + enemyPool.inUseCount() +
                " 总数=" + enemyPool.size());
    }
    public String getBulletPoolStats() {
        if (bulletPool != null) {
            return bulletPool.inUseCount() + "/" + bulletPool.size();
        }
        return "N/A";
    }
}