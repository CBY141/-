package main.java.com.tankbattle.core;

import javax.sound.sampled.*;
import java.awt.*;
import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

public class ResourceManager {
    private static ResourceManager instance;
    private Map<String, Clip> soundClips = new ConcurrentHashMap<>();
    private Clip backgroundMusicClip = null;
    private ExecutorService soundExecutor = Executors.newFixedThreadPool(4);
    private Map<String, Image> imageCache = new HashMap<>();
    private Map<String, Font> fontCache = new HashMap<>();
    private boolean audioEnabled = true;
    private float masterVolume = 0.7f;
    private float musicVolume = 0.5f;
    private float sfxVolume = 0.8f;

    private ResourceManager() { loadFonts(); }

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

    public void preloadSounds() {
        String[] soundFiles = {"background", "shoot", "explosion", "hit", "victory", "gameover", "start", "move"};
        for (String sound : soundFiles) {
            loadSound(sound);
        }
    }

    private void loadSound(String soundName) {
        try {
            // 优先从资源路径加载
            URL soundURL = getClass().getResource("/sounds/" + soundName + ".wav");
            if (soundURL == null) {
                // 尝试从文件系统加载
                File soundFile = new File("sounds/" + soundName + ".wav");
                if (!soundFile.exists()) {
                    soundFile = new File("src/sounds/" + soundName + ".wav");
                }
                if (soundFile.exists()) {
                    soundURL = soundFile.toURI().toURL();
                }
            }

            if (soundURL != null) {
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundURL);
                Clip clip = AudioSystem.getClip();
                clip.open(audioStream);
                soundClips.put(soundName, clip);
                System.out.println("音效加载成功: " + soundName);
            } else {
                System.out.println("警告: 音效文件未找到: " + soundName);
            }
        } catch (Exception e) {
            System.out.println("无法加载音效 " + soundName + ": " + e.getMessage());
        }
    }

    public void playSound(String soundName) {
        if (!audioEnabled) return;

        soundExecutor.submit(() -> {
            try {
                Clip clip = soundClips.get(soundName);
                if (clip != null) {
                    // 重置音频到开始位置
                    clip.setFramePosition(0);
                    setClipVolume(clip, soundName.equals("background") ? musicVolume : sfxVolume);
                    clip.start();
                }
            } catch (Exception e) {
                // 静默处理音频播放错误
            }
        });
    }

    public void playBackgroundMusic() {
        if (!audioEnabled) return;

        soundExecutor.submit(() -> {
            try {
                if (backgroundMusicClip == null || !backgroundMusicClip.isOpen()) {
                    loadSound("background");
                    backgroundMusicClip = soundClips.get("background");
                }
                if (backgroundMusicClip != null) {
                    setClipVolume(backgroundMusicClip, musicVolume);
                    backgroundMusicClip.loop(Clip.LOOP_CONTINUOUSLY);
                    backgroundMusicClip.start();
                }
            } catch (Exception e) {
                System.out.println("无法播放背景音乐: " + e.getMessage());
            }
        });
    }

    public void stopBackgroundMusic() {
        if (backgroundMusicClip != null && backgroundMusicClip.isRunning()) {
            backgroundMusicClip.stop();
        }
    }

    private void setClipVolume(Clip clip, float volume) {
        if (clip == null) return;
        try {
            FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            float minGain = gainControl.getMinimum();
            float maxGain = gainControl.getMaximum();
            float gain = minGain + (maxGain - minGain) * volume;
            gainControl.setValue(gain);
        } catch (IllegalArgumentException e) {
            // 某些音频可能不支持音量控制
        }
    }

    private void loadFonts() {
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

    public Font getSafeFont(int style, int size) {
        String[] preferredFonts = {"Microsoft YaHei", "SimHei", "KaiTi", "SimSun", "SansSerif"};
        for (String fontName : preferredFonts) {
            Font font = fontCache.get(fontName);
            if (font != null) {
                return font.deriveFont(style, size);
            }
        }
        return new Font("SansSerif", style, size);
    }

    public void setAudioEnabled(boolean enabled) {
        this.audioEnabled = enabled;
        if (!enabled) stopBackgroundMusic();
    }

    public void setMusicVolume(float volume) {
        this.musicVolume = Math.max(0, Math.min(1, volume));
        if (backgroundMusicClip != null) {
            setClipVolume(backgroundMusicClip, musicVolume);
        }
    }

    public void dispose() {
        stopBackgroundMusic();
        soundExecutor.shutdown();
        try {
            if (!soundExecutor.awaitTermination(1, TimeUnit.SECONDS)) {
                soundExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            soundExecutor.shutdownNow();
        }
        for (Clip clip : soundClips.values()) {
            if (clip != null && clip.isOpen()) {
                clip.close();
            }
        }
        soundClips.clear();
        imageCache.clear();
        System.out.println("资源管理器已释放");
    }
}