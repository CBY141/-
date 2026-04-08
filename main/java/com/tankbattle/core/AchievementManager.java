package main.java.com.tankbattle.core;

import java.util.*;

/**
 * 成就管理器
 */
public class AchievementManager implements EventListener {
    private static AchievementManager instance;
    private Map<String, Achievement> achievements = new HashMap<>();

    private AchievementManager() {
        initializeAchievements();
        EventManager.getInstance().registerListener(this,
                Event.EventType.ENEMY_DIED,
                Event.EventType.PLAYER_DIED,
                Event.EventType.BULLET_FIRED);
    }

    public static synchronized AchievementManager getInstance() {
        if (instance == null) {
            instance = new AchievementManager();
        }
        return instance;
    }

    private void initializeAchievements() {
        // 添加成就
        addAchievement(new Achievement("first_blood", "第一滴血", "消灭第一个敌人"));
        addAchievement(new Achievement("tank_destroyer", "坦克杀手", "消灭10个敌人"));
    }

    private void addAchievement(Achievement achievement) {
        achievements.put(achievement.getId(), achievement);
    }

    public Achievement getAchievement(String id) {
        return achievements.get(id);
    }

    public Achievement[] getAllAchievements() {
        return achievements.values().toArray(new Achievement[0]);
    }

    public Achievement[] getUnlockedAchievements() {
        List<Achievement> unlocked = new ArrayList<>();
        for (Achievement achievement : achievements.values()) {
            if (achievement.isUnlocked()) {
                unlocked.add(achievement);
            }
        }
        return unlocked.toArray(new Achievement[0]);
    }

    public int getUnlockedCount() {
        int count = 0;
        for (Achievement achievement : achievements.values()) {
            if (achievement.isUnlocked()) {
                count++;
            }
        }
        return count;
    }

    public int getTotalCount() {
        return achievements.size();
    }

    public void resetAll() {
        for (Achievement achievement : achievements.values()) {
            achievement.reset();
        }
    }

    @Override
    public void onEvent(GameEvent event) {
        switch (event.getType()) {
            case ENEMY_DIED:
                handleEnemyDied();
                break;
        }
    }

    private void handleEnemyDied() {
        // 第一滴血成就
        Achievement firstBlood = achievements.get("first_blood");
        if (firstBlood != null && !firstBlood.isUnlocked()) {
            firstBlood.unlock();
        }
    }
}