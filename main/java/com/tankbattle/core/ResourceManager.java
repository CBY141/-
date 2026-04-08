package main.java.com.tankbattle.core;

import javax.sound.sampled.*;
import java.awt.*;
import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

/**
 * 资源管理器 - 单例模式
 * 统一管理所有游戏资源（音效、图片、字体等）
 */
public class ResourceManager {
    private static ResourceManager instance;

    // 音频资源
    private Map<String, Clip> soundClips = new ConcurrentHashMap<>();
    private Clip backgroundMusicClip = null;
    private ExecutorService soundExecutor = Executors.newFixedThreadPool(2);

    // 图片资源（预留）
    private Map<String, Image> imageCache = new HashMap<>();

    // 字体资源
    private Map<String, Font> fontCache = new HashMap<>();

    // 资源配置
    private boolean audioEnabled = true;
    private float masterVolume = 0.7f;
    private float musicVolume = 0.5f;
    private float sfxVolume = 0.8f;

    // 私有构造器
    private ResourceManager() {
        loadFonts();
    }

    // 获取单例实例
    public static ResourceManager getInstance() {
        if (instance == null) {
            synchronized (ResourceManager.class) {
                if (instance == null) {
                    instance = new ResourceManager();
                }
            }
        }
        return instance;
    }

    // ================= 音频管理 =================

    /**
     * 预加载音效
     */
    public void preloadSounds() {
        String[] soundFiles = {
                "background", "shoot", "explosion", "hit",
                "victory", "gameover", "start", "move"
        };

        for (String sound : soundFiles) {
            loadSound(sound);
        }
    }

    /**
     * 加载单个音效
     */
    private void loadSound(String soundName) {
        String resourcePath = "/sounds/" + soundName + ".wav";
        try {
            URL soundURL = getClass().getResource(resourcePath);
            if (soundURL == null) {
                // 尝试从文件系统加载
                File soundFile = new File("src/sounds/" + soundName + ".wav");
                if (soundFile.exists()) {
                    soundURL = soundFile.toURI().toURL();
                }
            }

            if (soundURL != null) {
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundURL);
                Clip clip = AudioSystem.getClip();
                clip.open(audioStream);
                soundClips.put(soundName, clip);
            } else {
                System.out.println("音效文件未找到: " + soundName);
            }
        } catch (Exception e) {
            System.out.println("无法加载音效 " + soundName + ": " + e.getMessage());
        }
    }

    /**
     * 播放音效（线程安全）
     */
    public void playSound(String soundName) {
        if (!audioEnabled) return;

        soundExecutor.submit(() -> {
            try {
                Clip clip = soundClips.get(soundName);
                if (clip != null) {
                    // 创建新的Clip实例避免冲突
                    AudioInputStream audioStream = AudioSystem.getAudioInputStream(
                            getClass().getResource("/sounds/" + soundName + ".wav"));
                    Clip newClip = AudioSystem.getClip();
                    newClip.open(audioStream);

                    // 设置音量
                    setClipVolume(newClip, soundName.equals("background") ? musicVolume : sfxVolume);

                    newClip.setFramePosition(0);
                    newClip.start();

                    // 监听播放结束，释放资源
                    newClip.addLineListener(event -> {
                        if (event.getType() == LineEvent.Type.STOP) {
                            newClip.close();
                        }
                    });
                }
            } catch (Exception e) {
                // 静默处理音频播放错误
            }
        });
    }

    /**
     * 播放背景音乐
     */
    public void playBackgroundMusic() {
        if (!audioEnabled || backgroundMusicClip != null) return;

        soundExecutor.submit(() -> {
            try {
                URL musicURL = getClass().getResource("/sounds/background.wav");
                if (musicURL == null) {
                    File musicFile = new File("src/sounds/background.wav");
                    if (musicFile.exists()) {
                        musicURL = musicFile.toURI().toURL();
                    }
                }

                if (musicURL != null) {
                    AudioInputStream audioStream = AudioSystem.getAudioInputStream(musicURL);
                    backgroundMusicClip = AudioSystem.getClip();
                    backgroundMusicClip.open(audioStream);

                    setClipVolume(backgroundMusicClip, musicVolume);
                    backgroundMusicClip.loop(Clip.LOOP_CONTINUOUSLY);
                    backgroundMusicClip.start();
                }
            } catch (Exception e) {
                System.out.println("无法播放背景音乐: " + e.getMessage());
            }
        });
    }

    /**
     * 停止背景音乐
     */
    public void stopBackgroundMusic() {
        if (backgroundMusicClip != null && backgroundMusicClip.isRunning()) {
            backgroundMusicClip.stop();
            backgroundMusicClip.close();
            backgroundMusicClip = null;
        }
    }

    /**
     * 设置Clip音量
     */
    private void setClipVolume(Clip clip, float volume) {
        if (clip == null) return;

        try {
            FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            float dB = (float) (Math.log(volume) / Math.log(10.0) * 20.0);
            gainControl.setValue(dB);
        } catch (IllegalArgumentException e) {
            // 某些音频可能不支持音量控制
        }
    }

    // ================= 字体管理 =================

    /**
     * 加载字体
     */
    private void loadFonts() {
        // 系统字体列表
        String[] fontNames = {"Microsoft YaHei", "SimHei", "KaiTi", "SimSun", "SansSerif", "Arial"};

        for (String fontName : fontNames) {
            try {
                Font font = new Font(fontName, Font.PLAIN, 12);
                if (font.getFamily().equals(fontName)) {
                    fontCache.put(fontName, font);
                }
            } catch (Exception e) {
                // 忽略加载失败的字体
            }
        }
    }

    /**
     * 获取安全字体
     */
    public Font getSafeFont(int style, int size) {
        // 优先尝试中文字体
        String[] preferredFonts = {"Microsoft YaHei", "SimHei", "KaiTi", "SimSun", "SansSerif"};

        for (String fontName : preferredFonts) {
            Font font = fontCache.get(fontName);
            if (font != null) {
                return font.deriveFont(style, size);
            }
        }

        // 默认字体
        return new Font("SansSerif", style, size);
    }

    // ================= 配置管理 =================

    public void setAudioEnabled(boolean enabled) {
        this.audioEnabled = enabled;
        if (!enabled) {
            stopBackgroundMusic();
        }
    }

    public void setMasterVolume(float volume) {
        this.masterVolume = Math.max(0, Math.min(1, volume));
    }

    public void setMusicVolume(float volume) {
        this.musicVolume = Math.max(0, Math.min(1, volume));
        if (backgroundMusicClip != null) {
            setClipVolume(backgroundMusicClip, musicVolume);
        }
    }

    public void setSfxVolume(float volume) {
        this.sfxVolume = Math.max(0, Math.min(1, volume));
    }

    // ================= 资源清理 =================

    /**
     * 释放所有资源
     */
    public void dispose() {
        // 停止背景音乐
        stopBackgroundMusic();

        // 关闭音效线程池
        soundExecutor.shutdown();
        try {
            if (!soundExecutor.awaitTermination(1, TimeUnit.SECONDS)) {
                soundExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            soundExecutor.shutdownNow();
        }

        // 关闭所有音效Clip
        for (Clip clip : soundClips.values()) {
            if (clip != null && clip.isOpen()) {
                clip.close();
            }
        }
        soundClips.clear();

        // 清空图片缓存
        imageCache.clear();

        System.out.println("资源管理器已释放");
    }
}