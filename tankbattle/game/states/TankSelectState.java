package main.java.com.tankbattle.game.states;

import main.java.com.tankbattle.core.*;
import main.java.com.tankbattle.entity.Player;
import main.java.com.tankbattle.entity.TankType;
import main.java.com.tankbattle.game.factory.GameObjectFactory;
import main.java.com.tankbattle.world.GameWorld;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

public class TankSelectState extends BaseGameState {
    private GameWorld backgroundWorld;
    private TankType[] tankTypes = TankType.values();
    private int selectedIndex = 0;

    private Rectangle[] tankRects = new Rectangle[5];
    private Rectangle confirmRect = new Rectangle(300, 520, 200, 50);
    private Rectangle backRect = new Rectangle(50, 520, 150, 50);

    public TankSelectState(GameManager gameManager) {
        super(gameManager);
        // 调整卡片位置，留出空间显示属性和技能
        for (int i = 0; i < 5; i++) {
            tankRects[i] = new Rectangle(60 + i * 140, 150, 120, 100);
        }
    }

    @Override
    public void enter() {
        backgroundWorld = GameObjectFactory.createGameWorld();
    }

    @Override
    public void exit() {
    }

    @Override
    public void update(float deltaTime) {
    }

    @Override
    public void render(Graphics g) {
        backgroundWorld.draw(g);

        ConfigManager config = ConfigManager.getInstance();
        int width = config.getInt(ConfigManager.KEY_WINDOW_WIDTH);
        int height = config.getInt(ConfigManager.KEY_WINDOW_HEIGHT);

        // 半透明遮罩
        g.setColor(new Color(0, 0, 0, 170));
        g.fillRect(0, 0, width, height);

        // 标题
        g.setColor(Color.YELLOW);
        g.setFont(ResourceManager.getInstance().getSafeFont(Font.BOLD, 42));
        String title = "选择你的坦克";
        int titleWidth = g.getFontMetrics().stringWidth(title);
        g.drawString(title, width / 2 - titleWidth / 2, 70);

        // 绘制每种坦克
        for (int i = 0; i < tankTypes.length; i++) {
            TankType type = tankTypes[i];
            Rectangle rect = tankRects[i];

            // 坦克预览（色块 + 炮管示意）
            g.setColor(type.getColor());
            g.fillRect(rect.x, rect.y, rect.width, rect.height);
            g.setColor(Color.BLACK);
            g.drawRect(rect.x, rect.y, rect.width, rect.height);
            // 简单的炮塔示意
            g.setColor(type.getColor().darker());
            g.fillOval(rect.x + rect.width/2 - 8, rect.y + rect.height/2 - 8, 16, 16);

            // 选中边框
            if (i == selectedIndex) {
                g.setColor(Color.YELLOW);
                g.drawRect(rect.x - 4, rect.y - 4, rect.width + 8, rect.height + 8);
                g.drawRect(rect.x - 5, rect.y - 5, rect.width + 10, rect.height + 10);
            }

            // 坦克名称
            g.setColor(Color.WHITE);
            g.setFont(ResourceManager.getInstance().getSafeFont(Font.BOLD, 16));
            String name = type.getName();
            int nameWidth = g.getFontMetrics().stringWidth(name);
            g.drawString(name, rect.x + rect.width/2 - nameWidth/2, rect.y + rect.height + 20);

            // 属性数值（生命、速度、特殊弹药）
            g.setFont(ResourceManager.getInstance().getSafeFont(Font.PLAIN, 11));
            g.setColor(Color.ORANGE);
            String attr = "❤️" + type.getBaseLives() + "  ⚡" + type.getBaseSpeed() + "  🔮" + type.getInitialSpecialBullets();
            int attrWidth = g.getFontMetrics().stringWidth(attr);
            g.drawString(attr, rect.x + rect.width/2 - attrWidth/2, rect.y + rect.height + 35);

            // 技能描述（分行显示，每行一个技能）
            g.setFont(ResourceManager.getInstance().getSafeFont(Font.PLAIN, 10));
            String[] skills = type.getSkillDesc().split("/");
            int lineY = rect.y + rect.height + 50;
            for (String skill : skills) {
                g.setColor(Color.LIGHT_GRAY);
                int skillWidth = g.getFontMetrics().stringWidth(skill);
                g.drawString(skill, rect.x + rect.width/2 - skillWidth/2, lineY);
                lineY += 14;
            }
        }

        // 绘制底部按钮
        drawButton(g, confirmRect, "开始游戏", new Color(0, 180, 0));
        drawButton(g, backRect, "返回", new Color(100, 100, 100));

        // 底部操作提示
        g.setColor(Color.LIGHT_GRAY);
        g.setFont(ResourceManager.getInstance().getSafeFont(Font.PLAIN, 14));
        String hint = "← → 选择坦克  |  按 ENTER 开始  |  ESC 返回";
        int hintWidth = g.getFontMetrics().stringWidth(hint);
        g.drawString(hint, width / 2 - hintWidth / 2, height - 20);
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
        if (button == MouseEvent.BUTTON1) {
            for (int i = 0; i < tankRects.length; i++) {
                if (tankRects[i].contains(x, y)) {
                    selectedIndex = i;
                    break;
                }
            }
            if (confirmRect.contains(x, y)) {
                Player.setSelectedTankType(tankTypes[selectedIndex]);
                gameManager.changeState(GameManager.GameStateType.PLAYING);
            } else if (backRect.contains(x, y)) {
                gameManager.changeState(GameManager.GameStateType.MENU);
            }
        }
    }

    @Override
    public void keyPressed(int keyCode) {
        if (keyCode == KeyEvent.VK_ENTER) {
            Player.setSelectedTankType(tankTypes[selectedIndex]);
            gameManager.changeState(GameManager.GameStateType.PLAYING);
        } else if (keyCode == KeyEvent.VK_ESCAPE) {
            gameManager.changeState(GameManager.GameStateType.MENU);
        } else if (keyCode == KeyEvent.VK_LEFT) {
            selectedIndex = (selectedIndex - 1 + tankTypes.length) % tankTypes.length;
        } else if (keyCode == KeyEvent.VK_RIGHT) {
            selectedIndex = (selectedIndex + 1) % tankTypes.length;
        }
    }

    @Override public void mouseReleased(int x, int y, int button) {}
    @Override public void mouseMoved(int x, int y) {}
    @Override public void keyReleased(int keyCode) {}
}