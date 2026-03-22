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
        player.update(input.upPressed, input.downPressed, input.leftPressed, input.rightPressed,
                input.spacePressed, input.mouseX, input.mouseY, world, bullets);
        for (Bullet b : bullets) b.update();

        // 子弹与地图障碍物碰撞检测（修复穿墙）
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
                        // 砖墙可被破坏
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