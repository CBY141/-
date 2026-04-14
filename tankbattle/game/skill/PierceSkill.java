package main.java.com.tankbattle.game.skill;

import main.java.com.tankbattle.entity.Player;

public class PierceSkill extends Skill {
    public PierceSkill(String displayName, String iconText, float cooldown, float duration) {
        super("Pierce", displayName, iconText, cooldown, duration);
    }

    @Override
    protected void activate(Player player) {
        player.setPiercingBullet(true);
    }
}