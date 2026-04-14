package main.java.com.tankbattle.system;

/**
 * 游戏事件基类
 */
public abstract class Event {
    protected long timestamp;
    protected EventType type;

    public Event(EventType type) {
        this.type = type;
        this.timestamp = System.currentTimeMillis();
    }

    public EventType getType() {
        return type;
    }

    public long getTimestamp() {
        return timestamp;
    }

    // 事件类型枚举
    public enum EventType {
        // 游戏状态事件
        GAME_STARTED,
        GAME_PAUSED,
        GAME_RESUMED,
        GAME_OVER,
        GAME_VICTORY,

        // 玩家事件
        PLAYER_SPAWNED,
        PLAYER_DIED,
        PLAYER_HIT,
        PLAYER_SCORED,

        // 敌人事件
        ENEMY_SPAWNED,
        ENEMY_DIED,
        ENEMY_HIT,

        // 游戏对象事件
        BULLET_FIRED,
        BULLET_HIT,
        EXPLOSION_CREATED,

        // 输入事件
        INPUT_MOVED,
        INPUT_SHOOT,

        // 系统事件
        RESOURCE_LOADED,
        CONFIG_CHANGED,

        // 通用实体事件
        ENTITY_SPAWNED,
        ENTITY_HIT,
        ENTITY_DIED,
        ENTITY_HEALED,

        // 存档事件
        GAME_SAVED,
        GAME_LOADED,

        // 成就事件
        ACHIEVEMENT_UNLOCKED,

        // 技能事件
        TIME_SLOW_START,
        TIME_SLOW_END
    }
}