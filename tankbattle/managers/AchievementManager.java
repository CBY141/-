package main.java.com.tankbattle.managers;

import main.java.com.tankbattle.system.Achievement;
import main.java.com.tankbattle.system.Event;
import main.java.com.tankbattle.system.EventListener;
import main.java.com.tankbattle.system.GameEvent;
import main.java.com.tankbattle.utils.LogUtil;

import java.util.*;

/**
 * 成就管理器
 */
public class AchievementManager implements EventListener {
    private static volatile AchievementManager instance;
    private Map<String, Achievement> achievements = new HashMap<>();

    private int enemiesKilled = 0;
    private int shotsFired = 0;
    private int damageTaken = 0;
    private int playerDeaths = 0;
    private int gamesPlayed = 0;
    private int gamesWon = 0;

    private AchievementManager() {
        initializeAchievements();
        EventManager.getInstance().registerListener(this,
                Event.EventType.ENEMY_DIED,
                Event.EventType.PLAYER_DIED,
                Event.EventType.PLAYER_HIT,
                Event.EventType.BULLET_FIRED,
                Event.EventType.GAME_STARTED,
                Event.EventType.GAME_VICTORY,
                Event.EventType.GAME_OVER
        );
    }

    public static synchronized AchievementManager getInstance() {
        if (instance == null) {
            instance = new AchievementManager();
        }
        return instance;
    }

    private void initializeAchievements() {
        addAchievement(new Achievement("first_blood", "第一滴血", "消灭第一个敌人"));
        addAchievement(new Achievement("tank_destroyer", "坦克杀手", "消灭10个敌人"));
        addAchievement(new Achievement("tank_exterminator", "坦克灭绝者", "消灭50个敌人"));
        addAchievement(new Achievement("sharpshooter", "神射手", "连续命中5个敌人"));
        addAchievement(new Achievement("explosive_expert", "爆炸专家", "一次爆炸消灭3个敌人"));
        addAchievement(new Achievement("survivor", "幸存者", "一场游戏中没有死亡"));
        addAchievement(new Achievement("iron_will", "钢铁意志", "承受1000点伤害"));
        addAchievement(new Achievement("ghost", "幽灵", "一场游戏中没有被敌人击中"));
        addAchievement(new Achievement("pacifist", "和平主义者", "一场游戏中没有消灭敌人"));
        addAchievement(new Achievement("quick_draw", "快枪手", "一秒内发射5发子弹"));
        addAchievement(new Achievement("trick_shot", "技巧射击", "在最大距离消灭敌人"));
        addAchievement(new Achievement("ninja", "忍者", "消灭敌人而不被发现"));
        addAchievement(new Achievement("first_win", "首胜", "赢得第一场游戏"));
        addAchievement(new Achievement("veteran", "老兵", "完成10场游戏"));
        addAchievement(new Achievement("master", "大师", "赢得10场游戏"));
        addAchievement(new Achievement("perfectionist", "完美主义者", "以满生命值赢得游戏"));
        addAchievement(new Achievement("speedrunner", "速通玩家", "在60秒内赢得游戏"));
        addAchievement(new Achievement("collector", "收藏家", "解锁所有成就"));
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
        resetStats();
    }

    public void resetStats() {
        enemiesKilled = 0;
        shotsFired = 0;
        damageTaken = 0;
        playerDeaths = 0;
        gamesPlayed = 0;
        gamesWon = 0;
    }

    @Override
    public void onEvent(GameEvent event) {
        switch (event.getType()) {
            case ENEMY_DIED:
                handleEnemyDied();
                break;
            case PLAYER_DIED:
                handlePlayerDied();
                break;
            case PLAYER_HIT:
                handlePlayerHit(event);
                break;
            case BULLET_FIRED:
                handleBulletFired();
                break;
            case GAME_STARTED:
                handleGameStarted();
                break;
            case GAME_VICTORY:
                handleGameVictory();
                break;
            case GAME_OVER:
                handleGameOver(event);
                break;
        }

        if (getUnlockedCount() == getTotalCount() - 1) {
            Achievement collector = achievements.get("collector");
            if (collector != null && !collector.isUnlocked()) {
                collector.unlock();
            }
        }
    }

    private void handleEnemyDied() {
        enemiesKilled++;
        if (enemiesKilled == 1) unlockAchievement("first_blood");
        if (enemiesKilled == 10) unlockAchievement("tank_destroyer");
        if (enemiesKilled == 50) unlockAchievement("tank_exterminator");
    }

    private void handlePlayerDied() {
        playerDeaths++;
    }

    private void handlePlayerHit(GameEvent event) {
        Object data = event.getData();
        if (data instanceof Integer) {
            damageTaken += (Integer) data;
            if (damageTaken >= 1000) {
                unlockAchievement("iron_will");
            }
        }
    }

    private void handleBulletFired() {
        shotsFired++;
        if (shotsFired >= 100) {
            unlockAchievement("quick_draw");
        }
    }

    private void handleGameStarted() {
        gamesPlayed++;
        if (gamesPlayed == 10) unlockAchievement("veteran");
    }

    private void handleGameVictory() {
        gamesWon++;
        if (gamesWon == 1) unlockAchievement("first_win");
        if (gamesWon == 10) unlockAchievement("master");
        if (playerDeaths == 0) unlockAchievement("survivor");
    }

    private void handleGameOver(GameEvent event) {
        Object data = event.getData();
        if (data instanceof Boolean && !(Boolean) data) {
            // 游戏失败
        }
    }

    private void unlockAchievement(String achievementId) {
        Achievement achievement = achievements.get(achievementId);
        if (achievement != null && !achievement.isUnlocked()) {
            achievement.unlock();
            EventManager.getInstance().triggerEvent(
                    new GameEvent(Event.EventType.ACHIEVEMENT_UNLOCKED, achievement, null)
            );
        }
    }

    public void printStats() {
        LogUtil.info("========== 成就统计 ==========");
        LogUtil.info("总成就数: " + getTotalCount());
        LogUtil.info("已解锁: " + getUnlockedCount());
        LogUtil.info("进度: " + getUnlockedCount() + "/" + getTotalCount());
        LogUtil.info("\n游戏统计:");
        LogUtil.info("游戏场次: " + gamesPlayed);
        LogUtil.info("获胜场次: " + gamesWon);
        LogUtil.info("消灭敌人: " + enemiesKilled);
        LogUtil.info("发射子弹: " + shotsFired);
        LogUtil.info("承受伤害: " + damageTaken);
        LogUtil.info("玩家死亡: " + playerDeaths);
        LogUtil.info("\n已解锁成就:");
        for (Achievement achievement : getUnlockedAchievements()) {
            LogUtil.info("  ✓ " + achievement.getName() + " - " + achievement.getDescription());
        }
        LogUtil.info("\n未解锁成就:");
        for (Achievement achievement : achievements.values()) {
            if (!achievement.isUnlocked()) {
                LogUtil.info("  ○ " + achievement.getName() + " - " + achievement.getDescription());
            }
        }
        LogUtil.info("============================");
    }
}