package main.java.com.tankbattle.game.skill;

import main.java.com.tankbattle.entity.Player;

public class ShieldSkill extends Skill {
    public ShieldSkill(String displayName, String iconText, float cooldown, float duration) {
        super("Shield", displayName, iconText, cooldown, duration);
    }

    @Override
    protected void activate(Player player) {
        player.setInvulnerable(true);
    }

    @Override
    protected void deactivate() {
        super.deactivate();
        // 无敌状态由Player.update根据active状态控制
    }
}