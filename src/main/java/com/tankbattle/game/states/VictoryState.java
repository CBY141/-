package main.java.com.tankbattle.game.states;

import main.java.com.tankbattle.core.*;
import main.java.com.tankbattle.game.factory.GameObjectFactory;
import main.java.com.tankbattle.world.GameWorld;
import java.awt.*;

public class VictoryState extends BaseGameState {
    private GameWorld backgroundWorld;

    public VictoryState(GameManager gameManager) {
        super(gameManager);
    }

    @Override
    public void enter() {
        backgroundWorld = GameObjectFactory.createGameWorld();
        ResourceManager.getInstance().playSound("victory");
    }

    @Override
    public void render(Graphics g) {
        backgroundWorld.draw(g);
        ConfigManager config = ConfigManager.getInstance();
        int width = config.getInt(ConfigManager.KEY_WINDOW_WIDTH);
        int height = config.getInt(ConfigManager.KEY_WINDOW_HEIGHT);

        g.setColor(new Color(0, 200, 0, 120));
        g.fillRect(0, 0, width, height);

        g.setColor(new Color(255, 215, 0));
        g.setFont(ResourceManager.getInstance().getSafeFont(Font.BOLD, 72));
        String victoryText = "胜利!";
        int victoryWidth = g.getFontMetrics().stringWidth(victoryText);
        g.drawString(victoryText, width/2 - victoryWidth/2, height/2 - 50);

        g.setColor(Color.WHITE);
        g.setFont(ResourceManager.getInstance().getSafeFont(Font.BOLD, 36));
        String congratsText = "恭喜你消灭了所有敌人!";
        int congratsWidth = g.getFontMetrics().stringWidth(congratsText);
        g.drawString(congratsText, width/2 - congratsWidth/2, height/2 + 30);

        g.setColor(Color.CYAN);
        g.setFont(ResourceManager.getInstance().getSafeFont(Font.PLAIN, 28));
        String restartText = "按 R 键返回主菜单";
        int restartWidth = g.getFontMetrics().stringWidth(restartText);
        g.drawString(restartText, width/2 - restartWidth/2, height/2 + 100);
    }

    @Override
    public void keyPressed(int keyCode) {
        if (keyCode == 82) {
            gameManager.changeState(GameManager.GameStateType.MENU);
        }
    }
}