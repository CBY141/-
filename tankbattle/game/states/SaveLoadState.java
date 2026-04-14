package main.java.com.tankbattle.game.states;

import main.java.com.tankbattle.core.*;
import main.java.com.tankbattle.game.factory.GameObjectFactory;
import main.java.com.tankbattle.managers.SaveManager;
import main.java.com.tankbattle.system.SaveData;
import main.java.com.tankbattle.world.GameWorld;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

public class SaveLoadState extends BaseGameState {
    private GameWorld backgroundWorld;
    private Rectangle[] slotRects = new Rectangle[5];
    private Rectangle backRect = new Rectangle(300, 480, 200, 50);
    private String[] slotInfos = new String[5];
    private boolean[] slotExists = new boolean[5];

    public SaveLoadState(GameManager gameManager) {
        super(gameManager);
        for (int i = 0; i < 5; i++) {
            slotRects[i] = new Rectangle(200, 120 + i * 60, 400, 50);
        }
    }

    @Override
    public void enter() {
        backgroundWorld = GameObjectFactory.createGameWorld();
        refreshSlotInfo();
    }

    private void refreshSlotInfo() {
        SaveManager sm = SaveManager.getInstance();
        for (int i = 0; i < 5; i++) {
            slotExists[i] = sm.isSlotExist(i + 1);
            slotInfos[i] = sm.getSlotInfo(i + 1);
        }
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
        g.setFont(ResourceManager.getInstance().getSafeFont(Font.BOLD, 36));
        String title = "存档管理";
        g.drawString(title, width / 2 - 80, 70);

        for (int i = 0; i < 5; i++) {
            Rectangle rect = slotRects[i];
            Color bgColor = slotExists[i] ? new Color(50, 100, 150) : Color.DARK_GRAY;
            g.setColor(bgColor);
            g.fillRoundRect(rect.x, rect.y, rect.width, rect.height, 10, 10);
            g.setColor(Color.WHITE);
            g.drawRoundRect(rect.x, rect.y, rect.width, rect.height, 10, 10);

            g.setFont(ResourceManager.getInstance().getSafeFont(Font.PLAIN, 18));
            String text = "槽位 " + (i + 1) + " : " + slotInfos[i];
            g.drawString(text, rect.x + 20, rect.y + 30);
        }

        drawButton(g, backRect, "返回", Color.GRAY);

        g.setColor(Color.LIGHT_GRAY);
        g.setFont(ResourceManager.getInstance().getSafeFont(Font.PLAIN, 14));
        g.drawString("左键保存  右键加载（如果存档存在）", 200, 440);
    }

    private void drawButton(Graphics g, Rectangle rect, String text, Color color) {
        g.setColor(color.darker());
        g.fillRoundRect(rect.x, rect.y, rect.width, rect.height, 10, 10);
        g.setColor(color);
        g.fillRoundRect(rect.x + 2, rect.y + 2, rect.width - 4, rect.height - 4, 8, 8);
        g.setColor(Color.WHITE);
        g.setFont(ResourceManager.getInstance().getSafeFont(Font.BOLD, 20));
        FontMetrics fm = g.getFontMetrics();
        int tx = rect.x + (rect.width - fm.stringWidth(text)) / 2;
        int ty = rect.y + (rect.height - fm.getHeight()) / 2 + fm.getAscent();
        g.drawString(text, tx, ty);
    }

    @Override
    public void mousePressed(int x, int y, int button) {
        if (backRect.contains(x, y)) {
            gameManager.changeState(GameManager.GameStateType.MENU);
            return;
        }
        for (int i = 0; i < 5; i++) {
            if (slotRects[i].contains(x, y)) {
                if (button == MouseEvent.BUTTON1) {
                    // 左键保存
                    GameState state = gameManager.getState(GameManager.GameStateType.PLAYING);
                    if (state instanceof PlayingState) {
                        SaveData data = ((PlayingState) state).createSaveData();
                        SaveManager.getInstance().saveToSlot(i + 1, data);
                        refreshSlotInfo();
                    }
                } else if (button == MouseEvent.BUTTON3) {
                    // 右键加载
                    SaveData data = SaveManager.getInstance().loadFromSlot(i + 1);
                    if (data != null) {
                        PlayingState.setPendingSaveData(data);
                        gameManager.changeState(GameManager.GameStateType.PLAYING);
                    }
                }
                break;
            }
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