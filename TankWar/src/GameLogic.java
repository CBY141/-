import java.util.List;
import java.util.Random;

public class GameLogic {
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
        // 1. 更新玩家
        player.update(input.upPressed, input.downPressed, input.leftPressed, input.rightPressed,
                input.spacePressed, input.mouseX, input.mouseY, world, bullets);

        // 2. 更新所有子弹
        for (Bullet b : bullets) b.update();

        // === 新增代码开始：子弹与地图障碍物碰撞检测 ===
        for (Bullet bullet : bullets) {
            if (!bullet.isAlive()) continue;

            int bx = bullet.getX();
            int by = bullet.getY();

            // 将子弹坐标转换为地图格子坐标
            int tileX = bx / GameConfig.TILE_SIZE;
            int tileY = by / GameConfig.TILE_SIZE;

            // 检查子弹所在格子是否有效且不可通行
            if (tileX >= 0 && tileX < GameConfig.MAP_WIDTH && tileY >= 0 && tileY < GameConfig.MAP_HEIGHT) {
                MapTile tile = world.map[tileX][tileY]; // 因为world.map已改为public
                if (!tile.isPassable()) {
                    bullet.setAlive(false); // 击中障碍物，子弹消失
                }
            }
        }
        // === 新增代码结束 ===

        // 3. 清理死亡的子弹
        bullets.removeIf(b -> !b.isAlive());

        // 4. 更新所有敌人
        for (Enemy e : enemies) e.update(world, bullets);

        // 5. 更新爆炸效果
        for (Explosion exp : explosions) exp.update();
        explosions.removeIf(exp -> !exp.alive);

        // 6. 检测各种碰撞
        CollisionManager.checkBulletEnemyCollisions(bullets, enemies, explosions);
        CollisionManager.checkBulletPlayerCollisions(bullets, player, explosions);
        CollisionManager.checkEnemyPlayerCollisions(enemies, player, explosions);

        // 7. 清理死亡的敌人
        enemies.removeIf(e -> !e.alive);
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
}