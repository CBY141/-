package main.java.com.tankbattle.game.skill;

import main.java.com.tankbattle.entity.Player;

public abstract class Skill {
    protected String name;           // 技能内部标识名（如"护盾"）
    protected String displayName;    // 显示在界面上的技能名称（如"钢铁护盾"）
    protected String iconText;       // 技能图标上显示的文字（如"盾"）
    protected float cooldown;
    protected float currentCooldown;
    protected float duration;
    protected float activeTime;
    protected boolean active;

    public Skill(String name, String displayName, String iconText, float cooldown, float duration) {
        this.name = name;
        this.displayName = displayName;
        this.iconText = iconText;
        this.cooldown = cooldown;
        this.duration = duration;
        this.currentCooldown = 0;
        this.activeTime = 0;
        this.active = false;
    }

    public void update(float deltaTime) {
        if (currentCooldown > 0) {
            currentCooldown -= deltaTime;
            if (currentCooldown < 0) currentCooldown = 0;
        }
        if (active) {
            activeTime += deltaTime;
            if (duration > 0 && activeTime >= duration) {
                deactivate();
            }
        }
    }

    public boolean tryActivate(Player player) {
        if (currentCooldown > 0 || active) {
            return false;
        }
        activate(player);
        currentCooldown = cooldown;
        activeTime = 0;
        active = true;
        return true;
    }

    protected abstract void activate(Player player);

    protected void deactivate() {
        active = false;
        activeTime = 0;
    }

    public void forceDeactivate() {
        if (active) {
            deactivate();
        }
    }

    public boolean isActive() { return active; }
    public boolean isReady() { return currentCooldown <= 0 && !active; }
    public float getCooldownPercent() { return cooldown > 0 ? currentCooldown / cooldown : 0; }
    public String getName() { return name; }
    public String getDisplayName() { return displayName; }
    public String getIconText() { return iconText; }
    public float getRemainingCooldown() { return currentCooldown; }
    public float getRemainingActiveTime() { return active ? duration - activeTime : 0; }
}