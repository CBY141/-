package main.java.com.tankbattle.core;

/**
 * 音效监听器 - 处理所有游戏音效
 */
public class SoundEffectListener implements EventListener {

    public SoundEffectListener() {
        EventManager.getInstance().registerListener(this,
                Event.EventType.PLAYER_HIT,
                Event.EventType.EXPLOSION_CREATED,
                Event.EventType.BULLET_FIRED,
                Event.EventType.GAME_STARTED,
                Event.EventType.GAME_VICTORY,
                Event.EventType.GAME_OVER,
                Event.EventType.ACHIEVEMENT_UNLOCKED
        );
    }

    @Override
    public void onEvent(GameEvent event) {
        switch (event.getType()) {
            case PLAYER_HIT:
                ResourceManager.getInstance().playSound("hit");
                break;

            case EXPLOSION_CREATED:
                ResourceManager.getInstance().playSound("explosion");
                break;

            case BULLET_FIRED:
                ResourceManager.getInstance().playSound("shoot");
                break;

            case GAME_STARTED:
                ResourceManager.getInstance().playSound("start");
                break;

            case GAME_VICTORY:
                ResourceManager.getInstance().playSound("victory");
                break;

            case GAME_OVER:
                ResourceManager.getInstance().playSound("gameover");
                break;

            case ACHIEVEMENT_UNLOCKED:
                // 成就解锁音效
                ResourceManager.getInstance().playSound("achievement");
                break;
        }
    }
}