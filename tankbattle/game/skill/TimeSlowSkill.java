package main.java.com.tankbattle.game.skill;

import main.java.com.tankbattle.entity.Player;
import main.java.com.tankbattle.managers.EventManager;
import main.java.com.tankbattle.system.Event;
import main.java.com.tankbattle.system.GameEvent;

public class TimeSlowSkill extends Skill {
    private float slowFactor;

    public TimeSlowSkill(String displayName, String iconText, float cooldown, float duration, float slowFactor) {
        super("TimeSlow", displayName, iconText, cooldown, duration);
        this.slowFactor = slowFactor;
    }

    @Override
    protected void activate(Player player) {
        EventManager.getInstance().triggerEvent(
                new GameEvent(Event.EventType.TIME_SLOW_START, player, slowFactor)
        );
    }

    @Override
    protected void deactivate() {
        super.deactivate();
        EventManager.getInstance().triggerEvent(
                new GameEvent(Event.EventType.TIME_SLOW_END, null, null)
        );
    }
}