package main.java.com.tankbattle.core;

/**
 * 生命值组件 - 管理实体的生命值
 */
public class HealthComponent implements Component {
    private Entity entity;

    private int maxHealth = 100;
    private int currentHealth = 100;
    private boolean invulnerable = false;
    private int invulnerableTimer = 0;

    // ========== 必须实现的 Component 接口方法 ==========

    @Override
    public void initialize(Entity entity) {
        this.entity = entity;
    }

    @Override
    public void update(float deltaTime) {
        if (invulnerableTimer > 0) {
            invulnerableTimer--;
            if (invulnerableTimer == 0) {
                invulnerable = false;
            }
        }
    }

    @Override
    public void dispose() {
        // 清理资源
        this.entity = null;
    }

    // ========== 公共方法 ==========

    public int getMaxHealth() {
        return maxHealth;
    }

    public void setMaxHealth(int maxHealth) {
        this.maxHealth = maxHealth;
        if (currentHealth > maxHealth) {
            currentHealth = maxHealth;
        }
    }

    public int getCurrentHealth() {
        return currentHealth;
    }

    public void setCurrentHealth(int health) {
        this.currentHealth = Math.max(0, Math.min(maxHealth, health));
    }

    public void heal(int amount) {
        int oldHealth = currentHealth;
        setCurrentHealth(currentHealth + amount);

        if (currentHealth > oldHealth && entity != null) {
            // 触发治疗事件
            EventManager.getInstance().triggerEvent(
                    new GameEvent(Event.EventType.ENTITY_HEALED, entity,
                            new HealData(amount, currentHealth, maxHealth))
            );
        }
    }

    public void damage(int amount) {
        if (invulnerable || amount <= 0) return;

        int oldHealth = currentHealth;
        setCurrentHealth(currentHealth - amount);

        if (entity != null) {
            // 触发实体受伤事件
            EventManager.getInstance().triggerEvent(
                    new GameEvent(Event.EventType.ENTITY_HIT, entity,
                            new DamageData(amount, currentHealth, maxHealth))
            );
        }

        if (currentHealth <= 0 && oldHealth > 0) {
            // 触发实体死亡事件
            EventManager.getInstance().triggerEvent(
                    new GameEvent(Event.EventType.ENTITY_DIED, entity, null)
            );
        }
    }

    public float getHealthPercentage() {
        return (float) currentHealth / maxHealth;
    }

    public boolean isAlive() {
        return currentHealth > 0;
    }

    public boolean isInvulnerable() {
        return invulnerable;
    }

    public void setInvulnerable(boolean invulnerable, int duration) {
        this.invulnerable = invulnerable;
        this.invulnerableTimer = duration;
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
    }

    public Entity getEntity() {
        return entity;
    }

    // 内部类：伤害数据
    public static class DamageData {
        public final int damage;
        public final int currentHealth;
        public final int maxHealth;

        public DamageData(int damage, int currentHealth, int maxHealth) {
            this.damage = damage;
            this.currentHealth = currentHealth;
            this.maxHealth = maxHealth;
        }

        @Override
        public String toString() {
            return String.format("Damage: %d, Health: %d/%d",
                    damage, currentHealth, maxHealth);
        }
    }

    // 内部类：治疗数据
    public static class HealData {
        public final int healAmount;
        public final int currentHealth;
        public final int maxHealth;

        public HealData(int healAmount, int currentHealth, int maxHealth) {
            this.healAmount = healAmount;
            this.currentHealth = currentHealth;
            this.maxHealth = maxHealth;
        }

        @Override
        public String toString() {
            return String.format("Heal: %d, Health: %d/%d",
                    healAmount, currentHealth, maxHealth);
        }
    }
}