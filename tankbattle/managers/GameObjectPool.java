package main.java.com.tankbattle.managers;

import main.java.com.tankbattle.core.ConfigManager;
import main.java.com.tankbattle.utils.ObjectPool;
import main.java.com.tankbattle.entity.*;
import main.java.com.tankbattle.utils.LogUtil;

/**
 * 游戏对象池管理器
 */
public class GameObjectPool {
    private static volatile GameObjectPool instance;

    private ObjectPool<Bullet> bulletPool;
    private ObjectPool<Explosion> explosionPool;
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
        bulletPool = new ObjectPool<>(() ->
                new Bullet(0, 0, ConfigManager.DIR_UP, true), 20, 100);
        explosionPool = new ObjectPool<>(() ->
                new Explosion(0, 0), 10, 50);
        enemyPool = new ObjectPool<>(() ->
                new Enemy(0, 0), 5, 20);
    }

    public Bullet borrowBullet(int x, int y, int direction, boolean fromPlayer) {
        Bullet bullet = bulletPool.borrow();
        if (bullet != null) {
            bullet.reset(x, y, direction, fromPlayer);
            return bullet;
        }
        return new Bullet(x, y, direction, fromPlayer);
    }

    public void returnBullet(Bullet bullet) {
        if (bullet != null) {
            bulletPool.returnObject(bullet);
        }
    }

    public Explosion borrowExplosion(int x, int y) {
        Explosion explosion = explosionPool.borrow();
        if (explosion != null) {
            explosion.reset(x, y);
            return explosion;
        }
        return new Explosion(x, y);
    }

    public void returnExplosion(Explosion explosion) {
        if (explosion != null) {
            explosionPool.returnObject(explosion);
        }
    }

    public Enemy borrowEnemy(int x, int y) {
        Enemy enemy = enemyPool.borrow();
        if (enemy != null) {
            enemy.reset(x, y);
            return enemy;
        }
        return new Enemy(x, y);
    }

    public void returnEnemy(Enemy enemy) {
        if (enemy != null) {
            enemyPool.returnObject(enemy);
        }
    }

    public void clearAll() {
        bulletPool.clear();
        explosionPool.clear();
        enemyPool.clear();
    }

    public void printStats() {
        LogUtil.info("=== 对象池统计 ===");
        LogUtil.info("子弹池: 可用=" + bulletPool.availableCount() +
                " 使用中=" + bulletPool.inUseCount() +
                " 总数=" + bulletPool.size());
        LogUtil.info("爆炸池: 可用=" + explosionPool.availableCount() +
                " 使用中=" + explosionPool.inUseCount() +
                " 总数=" + explosionPool.size());
        LogUtil.info("敌人池: 可用=" + enemyPool.availableCount() +
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