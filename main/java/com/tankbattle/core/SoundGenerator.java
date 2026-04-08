package main.java.com.tankbattle.core;

import javax.sound.sampled.*;
import java.io.*;

/**
 * 音效生成器 - 在缺少音效文件时生成基本音效
 */
public class SoundGenerator {

    public static byte[] generateShootSound() {
        int duration = 100; // 100毫秒
        int sampleRate = 8000;
        int numSamples = duration * sampleRate / 1000;
        byte[] buffer = new byte[numSamples];

        // 生成射击音效（短促的高频音）
        for (int i = 0; i < numSamples; i++) {
            double frequency = 1000; // 1000Hz
            double wave = Math.sin(2 * Math.PI * i * frequency / sampleRate);
            buffer[i] = (byte) (wave * 127);
        }

        return buffer;
    }

    public static byte[] generateExplosionSound() {
        int duration = 500; // 500毫秒
        int sampleRate = 8000;
        int numSamples = duration * sampleRate / 1000;
        byte[] buffer = new byte[numSamples];

        // 生成爆炸音效（低频到高频的扫频）
        for (int i = 0; i < numSamples; i++) {
            double progress = (double) i / numSamples;
            double frequency = 50 + progress * 200; // 从50Hz到250Hz
            double wave = Math.sin(2 * Math.PI * i * frequency / sampleRate);

            // 添加包络
            double envelope = 1.0;
            if (progress < 0.1) envelope = progress * 10; // 起音
            else if (progress > 0.7) envelope = 1.0 - (progress - 0.7) * 3.33; // 释音

            buffer[i] = (byte) (wave * 127 * envelope);
        }

        return buffer;
    }

    public static byte[] generateHitSound() {
        int duration = 200; // 200毫秒
        int sampleRate = 8000;
        int numSamples = duration * sampleRate / 1000;
        byte[] buffer = new byte[numSamples];

        // 生成击中音效（中频脉冲）
        for (int i = 0; i < numSamples; i++) {
            double progress = (double) i / numSamples;
            double frequency = 400; // 400Hz
            double wave = Math.sin(2 * Math.PI * i * frequency / sampleRate);

            // 快速衰减的包络
            double envelope = Math.exp(-progress * 5);
            buffer[i] = (byte) (wave * 127 * envelope);
        }

        return buffer;
    }

    public static byte[] generateVictorySound() {
        int duration = 2000; // 2秒
        int sampleRate = 8000;
        int numSamples = duration * sampleRate / 1000;
        byte[] buffer = new byte[numSamples];

        // 生成胜利音效（简单的旋律）
        double[] frequencies = {523.25, 659.25, 783.99, 1046.50}; // C5, E5, G5, C6
        int noteDuration = numSamples / frequencies.length;

        for (int i = 0; i < numSamples; i++) {
            int noteIndex = i / noteDuration;
            if (noteIndex >= frequencies.length) noteIndex = frequencies.length - 1;

            double frequency = frequencies[noteIndex];
            double wave = Math.sin(2 * Math.PI * i * frequency / sampleRate);

            // 每个音符有起音和释音
            double noteProgress = (i % noteDuration) / (double) noteDuration;
            double envelope = 1.0;
            if (noteProgress < 0.1) envelope = noteProgress * 10;
            else if (noteProgress > 0.8) envelope = 1.0 - (noteProgress - 0.8) * 5;

            buffer[i] = (byte) (wave * 127 * envelope);
        }

        return buffer;
    }

    public static void saveSoundToFile(byte[] audioData, String filename) {
        try {
            AudioFormat format = new AudioFormat(8000, 8, 1, true, false);
            ByteArrayInputStream bais = new ByteArrayInputStream(audioData);
            AudioInputStream ais = new AudioInputStream(bais, format, audioData.length);

            File file = new File(filename);
            AudioSystem.write(ais, AudioFileFormat.Type.WAVE, file);
            System.out.println("已生成音效文件: " + filename);
        } catch (Exception e) {
            System.out.println("无法保存音效文件: " + e.getMessage());
        }
    }

    public static void generateAllSounds() {
        System.out.println("开始生成音效文件...");
        saveSoundToFile(generateShootSound(), "shoot.wav");
        saveSoundToFile(generateExplosionSound(), "explosion.wav");
        saveSoundToFile(generateHitSound(), "hit.wav");
        saveSoundToFile(generateVictorySound(), "victory.wav");
        System.out.println("音效文件生成完成");
    }

    public static void main(String[] args) {
        generateAllSounds();
    }
}