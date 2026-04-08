package main.java.com.tankbattle.core;

import main.java.com.tankbattle.entity.*;
import main.java.com.tankbattle.world.GameWorld;
import main.java.com.tankbattle.world.MapTile;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PlayingState extends BaseGameState {
    private Player player;
    private List<Enemy> enemies = new ArrayList<>();
    private List<Bullet> bullets = new ArrayList<>();
    private List<Explosion> explosions = new ArrayList<>();
    private GameWorld gameWorld;
    private Random random = new Random();
    private int moveSoundCooldown = 0;
    private boolean wasShooting = false;

    // 调试信息
    private int updateCount = 0;
    private String lastInputStatus = "";

    public PlayingState(GameManager gameManager) {
        super(gameManager);
    }

    @Override
    public void enter() {
        System.out.println("进入游戏状态");
        gameWorld = GameObjectFactory.createGameWorld();
        player = GameObjectFactory.createPlayerAtSafePosition(gameWorld);
        initializeEnemies(ConfigManager.getInstance().getInt(ConfigManager.KEY_ENEMY_COUNT));
        bullets.clear();
        explosions.clear();
        moveSoundCooldown = 0;
        wasShooting = false;
        updateCount = 0;

        ResourceManager.getInstance().playSound("start");
        ResourceManager.getInstance().playBackgroundMusic();

        System.out.println("游戏状态初始化完成");
        System.out.println("玩家位置: (" + player.x + ", " + player.y + ")");
        System.out.println("敌人数量: " + enemies.size());
    }

    @Override
    public void exit() {
        System.out.println("退出游戏状态");
        ResourceManager.getInstance().stopBackgroundMusic();
    }

    @Override
    public void update(float deltaTime) {
        updateCount++;

        // 获取输入处理器
        InputHandler input = gameManager.getInputHandler();
        if (input == null) {
            if (updateCount % 60 == 0) { // 每秒输出一次错误
                System.out.println("错误: InputHandler为null!");
            }
            return;
        }

        // 构建输入状态字符串
        String inputStatus = String.format("W:%s A:%s S:%s D:%s 射击:%s 鼠标:(%d,%d)",
                input.upPressed, input.leftPressed, input.downPressed, input.rightPressed,
                input.spacePressed, input.mouseX, input.mouseY);

        // 只有输入状态变化时才输出
        if (!inputStatus.equals(lastInputStatus)) {
            System.out.println("输入状态: " + inputStatus);
            lastInputStatus = inputStatus;
        }

        // 处理移动音效
        if (moveSoundCooldown <= 0 && (input.upPressed || input.downPressed ||
                input.leftPressed || input.rightPressed)) {
            ResourceManager.getInstance().playSound("move");
            moveSoundCooldown = 20;
        }
        if (moveSoundCooldown > 0) moveSoundCooldown--;

        // 处理射击音效
        boolean isShootingNow = input.spacePressed;
        if (isShootingNow && !wasShooting) {
            ResourceManager.getInstance().playSound("shoot");
            System.out.println("播放射击音效");
        }
        wasShooting = isShootingNow;

        // 更新玩家
        player.update(input.upPressed, input.downPressed, input.leftPressed, input.rightPressed,
                input.spacePressed, input.mouseX, input.mouseY, gameWorld, bullets);

        // 更新子弹
        for (Bullet b : bullets) b.update();

        // 子弹与地图碰撞检测
        for (Bullet bullet : bullets) {
            if (!bullet.isAlive()) continue;
            int bx = bullet.getX();
            int by = bullet.getY();
            ConfigManager config = ConfigManager.getInstance();
            int tileSize = config.getInt(ConfigManager.KEY_TILE_SIZE);
            int tileX = bx / tileSize;
            int tileY = by / tileSize;
            int mapWidth = config.getInt(ConfigManager.KEY_MAP_WIDTH);
            int mapHeight = config.getInt(ConfigManager.KEY_MAP_HEIGHT);

            if (tileX >= 0 && tileX < mapWidth && tileY >= 0 && tileY < mapHeight) {
                MapTile tile = gameWorld.map[tileX][tileY];
                if (!tile.isPassable()) {
                    int type = tile.getType();
                    if (type == ConfigManager.TILE_BRICK) {
                        gameWorld.destroyTile(tileX, tileY);
                        explosions.add(new Explosion(
                                tileX * tileSize + tileSize / 2,
                                tileY * tileSize + tileSize / 2
                        ));
                        ResourceManager.getInstance().playSound("explosion");
                    }
                    bullet.setAlive(false);
                }
            }
        }
        bullets.removeIf(b -> !b.isAlive());

        // 更新敌人
        for (Enemy e : enemies) e.update(gameWorld, bullets);

        // 更新爆炸效果
        for (Explosion exp : explosions) exp.update();
        explosions.removeIf(exp -> !exp.alive);

        // 碰撞检测
        checkCollisions();

        // 检查游戏结束条件
        checkGameEndConditions();

        // 每秒输出一次游戏状态
        if (updateCount % 60 == 0) {
            System.out.println("游戏状态: 生命" + player.lives +
                    " 敌人:" + enemies.size() +
                    " 子弹:" + bullets.size() +
                    " 爆炸:" + explosions.size());
        }
    }

    @Override
    public void render(Graphics g) {
        // 绘制游戏世界
        gameWorld.draw(g);

        // 绘制玩家
        InputHandler input = gameManager.getInputHandler();
        int mouseX = 400; // 默认值
        int mouseY = 300; // 默认值
        if (input != null) {
            mouseX = input.mouseX;
            mouseY = input.mouseY;
        }
        player.draw(g, mouseX, mouseY);

        // 绘制敌人
        for (Enemy e : enemies) e.draw(g);

        // 绘制子弹
        for (Bullet b : bullets) b.draw(g);

        // 绘制爆炸效果
        for (Explosion exp : explosions) exp.draw(g);

        // 绘制UI
        drawGameUI(g);

        // 绘制准星
        drawCrosshair(g, mouseX, mouseY);
    }

    @Override
    public void keyPressed(int keyCode) {
        if (keyCode == 82) { // R键返回主菜单
            System.out.println("R键按下，返回主菜单");
            gameManager.changeState(GameManager.GameStateType.MENU);
        }
    }

    @Override
    public void mousePressed(int x, int y, int button) {
        System.out.println("鼠标按下: (" + x + "," + y + ") 按钮: " + button);
    }

    @Override
    public void mouseMoved(int x, int y) {
        // 鼠标移动事件
    }

    private void initializeEnemies(int count) {
        enemies.clear();
        ConfigManager config = ConfigManager.getInstance();
        int windowWidth = config.getInt(ConfigManager.KEY_WINDOW_WIDTH);
        int windowHeight = config.getInt(ConfigManager.KEY_WINDOW_HEIGHT);
        int tankWidth = config.getInt(ConfigManager.KEY_TANK_WIDTH);
        int tankHeight = config.getInt(ConfigManager.KEY_TANK_HEIGHT);

        for (int i = 0; i < count; i++) {
            int x, y;
            int attempts = 0;
            do {
                x = random.nextInt(windowWidth - tankWidth);
                y = random.nextInt(windowHeight - tankHeight);
                attempts++;
            } while (attempts < 50 && !gameWorld.isPositionPassable(x, y, tankWidth, tankHeight));

            if (attempts < 50) {
                enemies.add(new Enemy(x, y));
            }
        }
    }

    private void checkCollisions() {
        ConfigManager config = ConfigManager.getInstance();
        int tankWidth = config.getInt(ConfigManager.KEY_TANK_WIDTH);

        // 子弹与敌人碰撞
        for (Bullet bullet : bullets) {
            if (!bullet.isAlive() || !bullet.isFromPlayer()) continue;
            for (Enemy enemy : enemies) {
                if (enemy.alive && bullet.collidesWith(enemy.x, enemy.y, tankWidth)) {
                    bullet.setAlive(false);
                    enemy.alive = false;
                    explosions.add(new Explosion(enemy.x + tankWidth/2, enemy.y + tankWidth/2));
                    ResourceManager.getInstance().playSound("explosion");
                    break;
                }
            }
        }

        // 子弹与玩家碰撞
        for (Bullet bullet : bullets) {
            if (!bullet.isAlive() || bullet.isFromPlayer()) continue;
            if (bullet.collidesWith(player.x, player.y, tankWidth)) {
                bullet.setAlive(false);
                player.lives--;
                ResourceManager.getInstance().playSound("hit");
                if (player.lives <= 0) player.dead = true;
            }
        }

        // 敌人与玩家碰撞
        for (Enemy enemy : enemies) {
            if (enemy.alive && enemy.collidesWith(player.x, player.y)) {
                player.lives--;
                enemy.alive = false;
                explosions.add(new Explosion(enemy.x + tankWidth/2, enemy.y + tankWidth/2));
                ResourceManager.getInstance().playSound("explosion");
                if (player.lives <= 0) player.dead = true;
            }
        }
        enemies.removeIf(e -> !e.alive);
    }

    private void checkGameEndConditions() {
        if (player.dead) {
            System.out.println("玩家死亡，游戏结束");
            gameManager.changeState(GameManager.GameStateType.GAME_OVER);
        } else if (enemies.isEmpty()) {
            System.out.println("所有敌人被消灭，游戏胜利");
            gameManager.changeState(GameManager.GameStateType.VICTORY);
        }
    }

    private void drawGameUI(Graphics g) {
        ResourceManager rm = ResourceManager.getInstance();
        g.setColor(Color.WHITE);
        g.setFont(rm.getSafeFont(Font.BOLD, 16));
        g.drawString("生命值: " + player.lives, 10, 20);
        g.drawString("敌人剩余: " + enemies.size(), 10, 40);
        g.setColor(Color.CYAN);
        g.drawString("按 R 键返回主菜单", 10, 60);

        // 显示输入状态
        InputHandler input = gameManager.getInputHandler();
        if (input != null) {
            g.setColor(Color.YELLOW);
            g.drawString("输入状态: W:" + input.upPressed + " A:" + input.leftPressed +
                    " S:" + input.downPressed + " D:" + input.rightPressed, 10, 80);
            g.drawString("射击: " + input.spacePressed, 10, 100);
        }
    }

    private void drawCrosshair(Graphics g, int mouseX, int mouseY) {
        g.setColor(Color.RED);
        g.drawLine(mouseX - 10, mouseY, mouseX + 10, mouseY);
        g.drawLine(mouseX, mouseY - 10, mouseX, mouseY + 10);
        g.drawOval(mouseX - 5, mouseY - 5, 10, 10);
    }
}