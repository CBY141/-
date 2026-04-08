package main.java.com.tankbattle.core;

import javax.sound.sampled.*;
import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * 音频管理器 - 负责加载和播放所有游戏音效和音乐
 */
public class AudioManager {
    // 音频类型枚举
    public enum SoundType {
        BACKGROUND_MUSIC,  // 背景音乐
        SHOOT,            // 射击音效
        EXPLOSION,        // 爆炸音效
        HIT,              // 击中音效
        VICTORY,          // 胜利音效
        GAME_OVER,        // 失败音效
        START_GAME,       // 开始游戏音效
        MOVE              // 移动音效
    }

    // 存储音频剪辑的映射
    private Map<SoundType, Clip> soundClips = new HashMap<>();
    private Clip backgroundMusicClip = null;
    private boolean audioEnabled = true;
    private float volume = 0.8f; // 音量 0.0-1.0

    public AudioManager() {
        // 尝试加载所有音频资源
        loadAllSounds();
    }

    /**
     * 加载所有音频文件
     */
    private void loadAllSounds() {
        try {
            // 尝试加载背景音乐
            loadSound(SoundType.BACKGROUND_MUSIC, "/sounds/background.wav");

            // 加载音效
            loadSound(SoundType.SHOOT, "/sounds/shoot.wav");
            loadSound(SoundType.EXPLOSION, "/sounds/explosion.wav");
            loadSound(SoundType.HIT, "/sounds/hit.wav");
            loadSound(SoundType.VICTORY, "/sounds/victory.wav");
            loadSound(SoundType.GAME_OVER, "/sounds/gameover.wav");
            loadSound(SoundType.START_GAME, "/sounds/start.wav");
            loadSound(SoundType.MOVE, "/sounds/move.wav");

            System.out.println("音频管理器初始化完成");
        } catch (Exception e) {
            System.out.println("警告: 部分音频文件未找到，游戏将继续静音运行");
            audioEnabled = false;
        }
    }

    /**
     * 加载单个音频文件
     */
    private void loadSound(SoundType type, String resourcePath) {
        try {
            URL soundURL = getClass().getResource(resourcePath);
            if (soundURL != null) {
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundURL);
                Clip clip = AudioSystem.getClip();
                clip.open(audioStream);
                soundClips.put(type, clip);
                System.out.println("已加载音频: " + type);
            } else {
                System.out.println("音频文件未找到: " + resourcePath);
            }
        } catch (Exception e) {
            System.out.println("无法加载音频 " + type + ": " + e.getMessage());
        }
    }

    /**
     * 播放指定音效
     */
    public void playSound(SoundType type) {
        if (!audioEnabled) return;

        Clip clip = soundClips.get(type);
        if (clip != null) {
            // 重置音频到开始位置
            clip.setFramePosition(0);

            // 设置音量
            setClipVolume(clip, volume);

            // 在新线程中播放，避免阻塞
            new Thread(() -> {
                try {
                    clip.start();
                } catch (Exception e) {
                    // 静默处理播放错误
                }
            }).start();
        }
    }

    /**
     * 播放背景音乐（循环播放）
     */
    public void playBackgroundMusic() {
        if (!audioEnabled) return;

        Clip clip = soundClips.get(SoundType.BACKGROUND_MUSIC);
        if (clip != null) {
            backgroundMusicClip = clip;
            clip.setFramePosition(0);
            setClipVolume(clip, volume * 0.5f); // 背景音乐音量减半

            // 设置循环播放
            clip.loop(Clip.LOOP_CONTINUOUSLY);

            new Thread(() -> {
                try {
                    clip.start();
                } catch (Exception e) {
                    // 静默处理播放错误
                }
            }).start();
        }
    }

    /**
     * 停止背景音乐
     */
    public void stopBackgroundMusic() {
        if (backgroundMusicClip != null && backgroundMusicClip.isRunning()) {
            backgroundMusicClip.stop();
        }
    }

    /**
     * 暂停背景音乐
     */
    public void pauseBackgroundMusic() {
        if (backgroundMusicClip != null && backgroundMusicClip.isRunning()) {
            backgroundMusicClip.stop();
        }
    }

    /**
     * 恢复背景音乐
     */
    public void resumeBackgroundMusic() {
        if (backgroundMusicClip != null && !backgroundMusicClip.isRunning()) {
            backgroundMusicClip.start();
            backgroundMusicClip.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    /**
     * 设置音量
     */
    public void setVolume(float volume) {
        this.volume = Math.max(0.0f, Math.min(1.0f, volume));

        // 更新所有已加载音频的音量
        for (Clip clip : soundClips.values()) {
            if (clip != null && clip.isOpen()) {
                setClipVolume(clip, this.volume);
            }
        }
    }

    /**
     * 设置单个音频剪辑的音量
     */
    private void setClipVolume(Clip clip, float volume) {
        if (clip == null || !clip.isOpen()) return;

        try {
            FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            float dB = (float) (Math.log(volume) / Math.log(10.0) * 20.0);
            gainControl.setValue(dB);
        } catch (IllegalArgumentException e) {
            // 某些音频可能不支持音量控制
        }
    }

    /**
     * 启用/禁用音频
     */
    public void setAudioEnabled(boolean enabled) {
        this.audioEnabled = enabled;
        if (!enabled) {
            stopAllSounds();
        }
    }

    /**
     * 停止所有音频
     */
    public void stopAllSounds() {
        for (Clip clip : soundClips.values()) {
            if (clip != null && clip.isRunning()) {
                clip.stop();
            }
        }
    }

    /**
     * 释放所有音频资源
     */
    public void dispose() {
        stopAllSounds();
        for (Clip clip : soundClips.values()) {
            if (clip != null) {
                clip.close();
            }
        }
        soundClips.clear();
    }
}