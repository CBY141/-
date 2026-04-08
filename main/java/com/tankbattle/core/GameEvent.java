package main.java.com.tankbattle.core;

import main.java.com.tankbattle.entity.Player;
import main.java.com.tankbattle.entity.Enemy;
import java.awt.Point;

/**
 * 具体游戏事件实现
 */
public class GameEvent extends Event {
    private Object data;
    private Object source;

    public GameEvent(EventType type, Object source, Object data) {
        super(type);
        this.source = source;
        this.data = data;
    }

    public GameEvent(EventType type, Object source) {
        this(type, source, null);
    }

    public Object getData() {
        return data;
    }

    public Object getSource() {
        return source;
    }

    // 静态工厂方法，创建常见事件

    public static GameEvent playerSpawned(Player player) {
        return new GameEvent(EventType.PLAYER_SPAWNED, player,
                new Point(player.x, player.y));
    }

    public static GameEvent playerDied(Player player) {
        return new GameEvent(EventType.PLAYER_DIED, player, player.lives);
    }

    public static GameEvent playerHit(Player player, int damage) {
        return new GameEvent(EventType.PLAYER_HIT, player, damage);
    }

    public static GameEvent enemySpawned(Enemy enemy) {
        return new GameEvent(EventType.ENEMY_SPAWNED, enemy,
                new Point(enemy.x, enemy.y));
    }

    public static GameEvent enemyDied(Enemy enemy) {
        return new GameEvent(EventType.ENEMY_DIED, enemy, null);
    }

    public static GameEvent gameStarted() {
        return new GameEvent(EventType.GAME_STARTED, null, null);
    }

    public static GameEvent gameOver(boolean victory) {
        return new GameEvent(EventType.GAME_OVER, null, victory);
    }

    public static GameEvent gameVictory() {
        return new GameEvent(EventType.GAME_VICTORY, null, null);
    }

    public static GameEvent bulletFired(Point position, boolean fromPlayer) {
        return new GameEvent(EventType.BULLET_FIRED, null,
                new Object[]{position, fromPlayer});
    }

    public static GameEvent explosionCreated(Point position) {
        return new GameEvent(EventType.EXPLOSION_CREATED, null, position);
    }

    // 通用实体事件工厂方法
    public static GameEvent entitySpawned(Entity entity) {
        return new GameEvent(EventType.ENTITY_SPAWNED, entity, entity.getName());
    }

    public static GameEvent entityHit(Entity entity, int damage) {
        return new GameEvent(EventType.ENTITY_HIT, entity, damage);
    }

    public static GameEvent entityDied(Entity entity) {
        return new GameEvent(EventType.ENTITY_DIED, entity, null);
    }

    public static GameEvent entityHealed(Entity entity, int healAmount) {
        return new GameEvent(EventType.ENTITY_HEALED, entity, healAmount);
    }
}