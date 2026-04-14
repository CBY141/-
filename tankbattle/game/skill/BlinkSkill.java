package main.java.com.tankbattle.game.skill;

import main.java.com.tankbattle.core.ConfigManager;
import main.java.com.tankbattle.entity.Player;
import main.java.com.tankbattle.world.GameWorld;

public class BlinkSkill extends Skill {
    private static final int BLINK_DISTANCE = 100;

    public BlinkSkill(String displayName, String iconText, float cooldown) {
        super("Blink", displayName, iconText, cooldown, 0);
    }

    @Override
    protected void activate(Player player) {
        GameWorld world = player.getCurrentWorld();
        if (world == null) return;

        ConfigManager config = ConfigManager.getInstance();
        int tankWidth = config.getInt(ConfigManager.KEY_TANK_WIDTH);
        int tankHeight = config.getInt(ConfigManager.KEY_TANK_HEIGHT);
        int windowWidth = config.getInt(ConfigManager.KEY_WINDOW_WIDTH);
        int windowHeight = config.getInt(ConfigManager.KEY_WINDOW_HEIGHT);

        int direction = player.getCurrentDirection();
        int newX = player.x;
        int newY = player.y;

        switch (direction) {
            case ConfigManager.DIR_UP:    newY -= BLINK_DISTANCE; break;
            case ConfigManager.DIR_DOWN:  newY += BLINK_DISTANCE; break;
            case ConfigManager.DIR_LEFT:  newX -= BLINK_DISTANCE; break;
            case ConfigManager.DIR_RIGHT: newX += BLINK_DISTANCE; break;
        }

        if (newX >= 0 && newX <= windowWidth - tankWidth &&
                newY >= 0 && newY <= windowHeight - tankHeight &&
                world.isPositionPassable(newX, newY, tankWidth, tankHeight)) {
            player.x = newX;
            player.y = newY;
        }
        deactivate();
    }
}