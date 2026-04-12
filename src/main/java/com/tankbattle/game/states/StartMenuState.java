package main.java.com.tankbattle.game.states;

import main.java.com.tankbattle.core.*;
import main.java.com.tankbattle.game.factory.GameObjectFactory;
import main.java.com.tankbattle.world.GameWorld;
import java.awt.*;

public class StartMenuState extends BaseGameState {
    private GameWorld backgroundWorld;
    private int blinkTimer = 0;
    private boolean showStartText = true;

    public StartMenuState(GameManager gameManager) {
        super(gameManager);
    }

    @Override
    public void enter() {
        backgroundWorld = GameObjectFactory.createGameWorld();
        ResourceManager.getInstance().playBackgroundMusic();
        System.out.println("进入开始菜单状态");
    }

    @Override
    public void exit() {
        ResourceManager.getInstance().stopBackgroundMusic();
    }

    @Override
    public void update(float deltaTime) {
        // 闪烁效果
        blinkTimer++;
        if (blinkTimer > 30) {
            showStartText = !showStartText;
            blinkTimer = 0;
        }
    }

    @Override
    public void render(Graphics g) {
        backgroundWorld.draw(g);

        ConfigManager config = ConfigManager.getInstance();
        int width = config.getInt(ConfigManager.KEY_WINDOW_WIDTH);
        int height = config.getInt(ConfigManager.KEY_WINDOW_HEIGHT);

        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, width, height);

        g.setColor(Color.YELLOW);
        g.setFont(ResourceManager.getInstance().getSafeFont(Font.BOLD, 64));
        String title = "坦克大战";
        int titleWidth = g.getFontMetrics().stringWidth(title);
        g.drawString(title, width/2 - titleWidth/2, height/2 - 100);

        // 闪烁的开始提示
        if (showStartText) {
            g.setColor(Color.GREEN);
            g.setFont(ResourceManager.getInstance().getSafeFont(Font.BOLD, 36));
            String startText = "按 空格键 开始游戏";
            int startWidth = g.getFontMetrics().stringWidth(startText);
            g.drawString(startText, width/2 - startWidth/2, height/2);
        }

        g.setColor(Color.WHITE);
        g.setFont(ResourceManager.getInstance().getSafeFont(Font.PLAIN, 20));
        String[] controls = {
                "控制说明:",
                "移动: W(上) A(左) S(下) D(右)",
                "射击: 鼠标左键 或 空格键",
                "瞄准: 鼠标移动",
                "返回菜单: R键"
        };

        int y = height/2 + 80;
        for (String line : controls) {
            int lineWidth = g.getFontMetrics().stringWidth(line);
            g.drawString(line, width/2 - lineWidth/2, y);
            y += 30;
        }

        // 显示当前焦点状态
        g.setColor(Color.CYAN);
        g.setFont(ResourceManager.getInstance().getSafeFont(Font.PLAIN, 16));
        g.drawString("提示: 如果无法控制，请点击游戏画面获取焦点", 20, height - 40);
    }

    @Override
    public void keyPressed(int keyCode) {
        System.out.println("按键: " + keyCode);

        if (keyCode == 32 || keyCode == 82) { // 空格键(32) 或 R键(82)
            System.out.println("开始游戏...");
            ResourceManager.getInstance().playSound("start");
            gameManager.changeState(GameManager.GameStateType.PLAYING);
        }
    }
}