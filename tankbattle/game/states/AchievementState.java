package main.java.com.tankbattle.game.states;

import main.java.com.tankbattle.core.*;
import main.java.com.tankbattle.game.factory.GameObjectFactory;
import main.java.com.tankbattle.managers.AchievementManager;
import main.java.com.tankbattle.system.Achievement;
import main.java.com.tankbattle.world.GameWorld;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

public class AchievementState extends BaseGameState {
    private GameWorld backgroundWorld;
    private Rectangle backRect = new Rectangle(300, 500, 200, 50);

    public AchievementState(GameManager gameManager) {
        super(gameManager);
    }

    @Override
    public void enter() {
        backgroundWorld = GameObjectFactory.createGameWorld();
    }

    @Override
    public void exit() {}

    @Override
    public void update(float deltaTime) {}

    @Override
    public void render(Graphics g) {
        backgroundWorld.draw(g);

        ConfigManager config = ConfigManager.getInstance();
        int width = config.getInt(ConfigManager.KEY_WINDOW_WIDTH);
        int height = config.getInt(ConfigManager.KEY_WINDOW_HEIGHT);

        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, width, height);

        g.setColor(Color.YELLOW);
        g.setFont(ResourceManager.getInstance().getSafeFont(Font.BOLD, 48));
        String title = "成就";
        int titleWidth = g.getFontMetrics().stringWidth(title);
        g.drawString(title, width / 2 - titleWidth / 2, 80);

        AchievementManager am = AchievementManager.getInstance();
        Achievement[] achievements = am.getAllAchievements();

        g.setFont(ResourceManager.getInstance().getSafeFont(Font.PLAIN, 18));
        int y = 140;
        for (Achievement a : achievements) {
            if (a.isUnlocked()) {
                g.setColor(Color.GREEN);
                g.drawString("✓ " + a.getName() + " - " + a.getDescription(), 150, y);
            } else {
                g.setColor(Color.GRAY);
                g.drawString("○ " + a.getName() + " - " + a.getDescription(), 150, y);
            }
            y += 30;
            if (y > 450) break;
        }

        g.setColor(Color.CYAN);
        g.drawString("已完成: " + am.getUnlockedCount() + "/" + am.getTotalCount(), 150, 480);

        drawButton(g, backRect, "返回", Color.GRAY);
    }

    private void drawButton(Graphics g, Rectangle rect, String text, Color color) {
        g.setColor(color.darker());
        g.fillRoundRect(rect.x, rect.y, rect.width, rect.height, 10, 10);
        g.setColor(color);
        g.fillRoundRect(rect.x + 2, rect.y + 2, rect.width - 4, rect.height - 4, 8, 8);
        g.setColor(Color.BLACK);
        g.setFont(ResourceManager.getInstance().getSafeFont(Font.BOLD, 20));
        FontMetrics fm = g.getFontMetrics();
        int tx = rect.x + (rect.width - fm.stringWidth(text)) / 2;
        int ty = rect.y + (rect.height - fm.getHeight()) / 2 + fm.getAscent();
        g.drawString(text, tx, ty);
    }

    @Override
    public void mousePressed(int x, int y, int button) {
        if (button == MouseEvent.BUTTON1 && backRect.contains(x, y)) {
            gameManager.changeState(GameManager.GameStateType.MENU);
        }
    }

    @Override
    public void keyPressed(int keyCode) {
        if (keyCode == KeyEvent.VK_ESCAPE) {
            gameManager.changeState(GameManager.GameStateType.MENU);
        }
    }

    @Override public void mouseReleased(int x, int y, int button) {}
    @Override public void mouseMoved(int x, int y) {}
    @Override public void keyReleased(int keyCode) {}
}