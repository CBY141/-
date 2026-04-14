package main.java.com.tankbattle.game.states;

import main.java.com.tankbattle.core.*;
import main.java.com.tankbattle.game.factory.GameObjectFactory;
import main.java.com.tankbattle.world.GameWorld;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

public class StartMenuState extends BaseGameState {
    private GameWorld backgroundWorld;

    // 按钮区域
    private Rectangle singlePlayerRect = new Rectangle(250, 220, 300, 50);
    private Rectangle multiPlayerRect = new Rectangle(250, 290, 300, 50);
    private Rectangle achievementRect = new Rectangle(250, 360, 300, 50);
    private Rectangle exitRect = new Rectangle(250, 430, 300, 50);

    public StartMenuState(GameManager gameManager) {
        super(gameManager);
    }

    @Override
    public void enter() {
        backgroundWorld = GameObjectFactory.createGameWorld();
        ResourceManager.getInstance().playBackgroundMusic();
    }

    @Override
    public void exit() {
        ResourceManager.getInstance().stopBackgroundMusic();
    }

    @Override
    public void update(float deltaTime) {
        // 不需要闪烁动画
    }

    @Override
    public void render(Graphics g) {
        // 绘制背景地图
        backgroundWorld.draw(g);

        ConfigManager config = ConfigManager.getInstance();
        int width = config.getInt(ConfigManager.KEY_WINDOW_WIDTH);
        int height = config.getInt(ConfigManager.KEY_WINDOW_HEIGHT);

        // 半透明黑色遮罩
        g.setColor(new Color(0, 0, 0, 160));
        g.fillRect(0, 0, width, height);

        // 标题
        g.setColor(Color.YELLOW);
        g.setFont(ResourceManager.getInstance().getSafeFont(Font.BOLD, 72));
        String title = "坦克大战";
        FontMetrics fm = g.getFontMetrics();
        int titleWidth = fm.stringWidth(title);
        g.drawString(title, width / 2 - titleWidth / 2, 120);

        // 绘制按钮
        drawButton(g, singlePlayerRect, "单人模式", new Color(0, 180, 0));
        drawButton(g, multiPlayerRect, "联机模式 (开发中)", new Color(100, 100, 100));
        drawButton(g, achievementRect, "成就", new Color(0, 200, 200));
        drawButton(g, exitRect, "退出游戏", new Color(200, 50, 50));

        // 底部提示
        g.setColor(Color.LIGHT_GRAY);
        g.setFont(ResourceManager.getInstance().getSafeFont(Font.PLAIN, 14));
        String hint = "按 ESC 退出游戏  |  鼠标点击按钮选择";
        int hintWidth = g.getFontMetrics().stringWidth(hint);
        g.drawString(hint, width / 2 - hintWidth / 2, height - 30);
    }

    private void drawButton(Graphics g, Rectangle rect, String text, Color baseColor) {
        // 按钮阴影
        g.setColor(baseColor.darker().darker());
        g.fillRoundRect(rect.x + 2, rect.y + 2, rect.width, rect.height, 15, 15);
        // 按钮主体
        g.setColor(baseColor);
        g.fillRoundRect(rect.x, rect.y, rect.width, rect.height, 15, 15);
        // 按钮边框
        g.setColor(baseColor.brighter());
        g.drawRoundRect(rect.x, rect.y, rect.width, rect.height, 15, 15);
        // 文字
        g.setColor(Color.WHITE);
        g.setFont(ResourceManager.getInstance().getSafeFont(Font.BOLD, 22));
        FontMetrics fm = g.getFontMetrics();
        int textX = rect.x + (rect.width - fm.stringWidth(text)) / 2;
        int textY = rect.y + (rect.height - fm.getHeight()) / 2 + fm.getAscent();
        g.drawString(text, textX, textY);
    }

    @Override
    public void mousePressed(int x, int y, int button) {
        if (button == MouseEvent.BUTTON1) {
            if (singlePlayerRect.contains(x, y)) {
                ResourceManager.getInstance().playSound("start");
                gameManager.changeState(GameManager.GameStateType.TANK_SELECT);
            } else if (achievementRect.contains(x, y)) {
                ResourceManager.getInstance().playSound("start");
                gameManager.changeState(GameManager.GameStateType.ACHIEVEMENT);
            } else if (exitRect.contains(x, y)) {
                System.exit(0);
            }
            // 联机模式暂不处理
        }
    }

    @Override
    public void keyPressed(int keyCode) {
        if (keyCode == KeyEvent.VK_ENTER || keyCode == KeyEvent.VK_SPACE) {
            ResourceManager.getInstance().playSound("start");
            gameManager.changeState(GameManager.GameStateType.TANK_SELECT);
        } else if (keyCode == KeyEvent.VK_ESCAPE) {
            System.exit(0);
        }
    }

    // 以下方法必须实现，但无需额外逻辑
    @Override public void mouseReleased(int x, int y, int button) {}
    @Override public void mouseMoved(int x, int y) {}
    @Override public void keyReleased(int keyCode) {}
}