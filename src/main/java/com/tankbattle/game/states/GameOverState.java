package main.java.com.tankbattle.game.states;

import main.java.com.tankbattle.core.*;
import main.java.com.tankbattle.game.factory.GameObjectFactory;
import main.java.com.tankbattle.world.GameWorld;
import java.awt.*;

public class GameOverState extends BaseGameState {
    private GameWorld backgroundWorld;

    public GameOverState(GameManager gameManager) {
        super(gameManager);
    }

    @Override
    public void enter() {
        backgroundWorld = GameObjectFactory.createGameWorld();
        ResourceManager.getInstance().playSound("gameover");
    }

    @Override
    public void render(Graphics g) {
        backgroundWorld.draw(g);
        ConfigManager config = ConfigManager.getInstance();
        int width = config.getInt(ConfigManager.KEY_WINDOW_WIDTH);
        int height = config.getInt(ConfigManager.KEY_WINDOW_HEIGHT);

        g.setColor(new Color(255, 50, 50, 150));
        g.fillRect(width/2 - 180, height/2 - 100, 360, 200);

        g.setColor(Color.WHITE);
        g.setFont(ResourceManager.getInstance().getSafeFont(Font.BOLD, 48));
        String gameOverText = "游戏结束!";
        int textWidth = g.getFontMetrics().stringWidth(gameOverText);
        g.drawString(gameOverText, width/2 - textWidth/2, height/2 - 30);

        g.setFont(ResourceManager.getInstance().getSafeFont(Font.PLAIN, 24));
        String restartHint = "按 R 键返回主菜单";
        int hintWidth = g.getFontMetrics().stringWidth(restartHint);
        g.drawString(restartHint, width/2 - hintWidth/2, height/2 + 40);
    }

    @Override
    public void keyPressed(int keyCode) {
        if (keyCode == 82) {
            gameManager.changeState(GameManager.GameStateType.MENU);
        }
    }
}