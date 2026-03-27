import javax.sound.sampled.*;
import java.io.File;
import java.io.ByteArrayInputStream;

public class GenerateSounds {
    public static void main(String[] args) {
        System.out.println("开始生成坦克大战游戏音效...");

        // 创建sounds目录
        File soundsDir = new File("sounds");
        if (!soundsDir.exists()) {
            soundsDir.mkdir();
            System.out.println("创建sounds目录");
        }

        // 生成所有音效
        generateShootSound();
        generateExplosionSound();
        generateHitSound();
        generateVictorySound();
        generateGameOverSound();
        generateStartSound();
        generateMoveSound();
        generateBackgroundMusic();

        System.out.println("音效生成完成！文件保存在 sounds/ 目录中");
        System.out.println("请将这些文件复制到: src/sounds/ 目录中");
    }

    // 1. 射击音效 - 清脆的"砰"声
    private static void generateShootSound() {
        int duration = 150; // 150毫秒
        int sampleRate = 8000;
        int numSamples = duration * sampleRate / 1000;
        byte[] buffer = new byte[numSamples];

        for (int i = 0; i < numSamples; i++) {
            // 高频短促音
            double frequency = 800 + 200 * Math.sin(i * 0.01);
            double wave = Math.sin(2 * Math.PI * i * frequency / sampleRate);
            // 快速衰减包络
            double envelope = Math.exp(-i / 100.0);
            buffer[i] = (byte) (wave * 100 * envelope);
        }

        saveToFile(buffer, "sounds/shoot.wav", sampleRate);
    }

    // 2. 爆炸音效 - 厚重的爆炸声
    private static void generateExplosionSound() {
        int duration = 800; // 800毫秒
        int sampleRate = 8000;
        int numSamples = duration * sampleRate / 1000;
        byte[] buffer = new byte[numSamples];

        for (int i = 0; i < numSamples; i++) {
            // 低频爆炸声
            double progress = (double) i / numSamples;
            double frequency = 60 + progress * 100; // 从低频到中频
            double wave = Math.sin(2 * Math.PI * i * frequency / sampleRate);

            // 爆炸包络：快速起音，慢速衰减
            double envelope;
            if (progress < 0.1) {
                envelope = progress * 10; // 快速起音
            } else {
                envelope = Math.exp(-(progress - 0.1) * 4); // 慢速衰减
            }

            // 添加一些噪音
            double noise = Math.random() * 0.3 - 0.15;
            buffer[i] = (byte) (wave * 120 * envelope + noise * 50);
        }

        saveToFile(buffer, "sounds/explosion.wav", sampleRate);
    }

    // 3. 击中音效 - 金属撞击声
    private static void generateHitSound() {
        int duration = 300; // 300毫秒
        int sampleRate = 8000;
        int numSamples = duration * sampleRate / 1000;
        byte[] buffer = new byte[numSamples];

        for (int i = 0; i < numSamples; i++) {
            // 金属撞击的谐波
            double freq1 = 300;
            double freq2 = 600;
            double freq3 = 900;

            double wave1 = Math.sin(2 * Math.PI * i * freq1 / sampleRate) * 0.5;
            double wave2 = Math.sin(2 * Math.PI * i * freq2 / sampleRate) * 0.3;
            double wave3 = Math.sin(2 * Math.PI * i * freq3 / sampleRate) * 0.2;

            double envelope = Math.exp(-i / 200.0);
            buffer[i] = (byte) ((wave1 + wave2 + wave3) * 100 * envelope);
        }

        saveToFile(buffer, "sounds/hit.wav", sampleRate);
    }

    // 4. 胜利音效 - 欢快的旋律
    private static void generateVictorySound() {
        int duration = 3000; // 3秒
        int sampleRate = 8000;
        int numSamples = duration * sampleRate / 1000;
        byte[] buffer = new byte[numSamples];

        // 简单的胜利旋律: C E G C
        double[] notes = {261.63, 329.63, 392.00, 523.25}; // C4, E4, G4, C5
        int noteDuration = numSamples / notes.length;

        for (int i = 0; i < numSamples; i++) {
            int noteIndex = i / noteDuration;
            if (noteIndex >= notes.length) noteIndex = notes.length - 1;

            double frequency = notes[noteIndex];
            double wave = Math.sin(2 * Math.PI * i * frequency / sampleRate);

            // 每个音符的包络
            double notePos = (i % noteDuration) / (double) noteDuration;
            double envelope = Math.sin(notePos * Math.PI) * 0.8; // 正弦包络

            buffer[i] = (byte) (wave * 80 * envelope);
        }

        saveToFile(buffer, "sounds/victory.wav", sampleRate);
    }

    // 5. 失败音效 - 低沉的音调
    private static void generateGameOverSound() {
        int duration = 2000; // 2秒
        int sampleRate = 8000;
        int numSamples = duration * sampleRate / 1000;
        byte[] buffer = new byte[numSamples];

        for (int i = 0; i < numSamples; i++) {
            double progress = (double) i / numSamples;
            // 从高到低的滑音
            double frequency = 200 * (1.0 - progress * 0.5);
            double wave = Math.sin(2 * Math.PI * i * frequency / sampleRate);

            // 缓慢衰减
            double envelope = Math.exp(-progress * 2);
            buffer[i] = (byte) (wave * 100 * envelope);
        }

        saveToFile(buffer, "sounds/gameover.wav", sampleRate);
    }

    // 6. 开始音效 - 上升音
    private static void generateStartSound() {
        int duration = 1000; // 1秒
        int sampleRate = 8000;
        int numSamples = duration * sampleRate / 1000;
        byte[] buffer = new byte[numSamples];

        for (int i = 0; i < numSamples; i++) {
            double progress = (double) i / numSamples;
            // 上升音
            double frequency = 100 + progress * 400;
            double wave = Math.sin(2 * Math.PI * i * frequency / sampleRate);

            // 快速起音，慢速衰减
            double envelope = Math.exp(-progress * 3);
            buffer[i] = (byte) (wave * 100 * envelope);
        }

        saveToFile(buffer, "sounds/start.wav", sampleRate);
    }

    // 7. 移动音效 - 引擎声
    private static void generateMoveSound() {
        int duration = 1000; // 1秒（可循环）
        int sampleRate = 8000;
        int numSamples = duration * sampleRate / 1000;
        byte[] buffer = new byte[numSamples];

        for (int i = 0; i < numSamples; i++) {
            // 引擎的震动声
            double time = i / (double) sampleRate;
            double freq1 = 80;  // 基础频率
            double freq2 = 160; // 谐波

            // 添加一些随机波动模拟引擎
            double vibrato = Math.sin(time * 5) * 2;

            double wave1 = Math.sin(2 * Math.PI * i * (freq1 + vibrato) / sampleRate);
            double wave2 = Math.sin(2 * Math.PI * i * (freq2 + vibrato) / sampleRate) * 0.3;

            // 持续的音量
            double envelope = 0.7;
            buffer[i] = (byte) ((wave1 + wave2) * 60 * envelope);
        }

        saveToFile(buffer, "sounds/move.wav", sampleRate);
    }

    // 8. 背景音乐 - 简单的循环音乐
    private static void generateBackgroundMusic() {
        int duration = 10000; // 10秒（可循环）
        int sampleRate = 8000;
        int numSamples = duration * sampleRate / 1000;
        byte[] buffer = new byte[numSamples];

        // 简单的和弦进行
        double[] chords = {130.81, 164.81, 196.00, 261.63}; // C3, E3, G3, C4
        int chordDuration = numSamples / chords.length;

        for (int i = 0; i < numSamples; i++) {
            int chordIndex = i / chordDuration;
            if (chordIndex >= chords.length) chordIndex = chords.length - 1;

            double baseFreq = chords[chordIndex];

            // 三个音组成的和弦
            double wave1 = Math.sin(2 * Math.PI * i * baseFreq / sampleRate);
            double wave2 = Math.sin(2 * Math.PI * i * baseFreq * 1.25 / sampleRate) * 0.5;
            double wave3 = Math.sin(2 * Math.PI * i * baseFreq * 1.5 / sampleRate) * 0.3;

            // 缓慢起伏的音量
            double time = i / (double) sampleRate;
            double envelope = 0.5 + 0.2 * Math.sin(time * 0.5);

            buffer[i] = (byte) ((wave1 + wave2 + wave3) * 40 * envelope);
        }

        saveToFile(buffer, "sounds/background.wav", sampleRate);
    }

    // 保存为WAV文件
    private static void saveToFile(byte[] audioData, String filename, int sampleRate) {
        try {
            AudioFormat format = new AudioFormat(sampleRate, 8, 1, true, false);
            ByteArrayInputStream bais = new ByteArrayInputStream(audioData);
            AudioInputStream ais = new AudioInputStream(bais, format, audioData.length);

            File file = new File(filename);
            AudioSystem.write(ais, AudioFileFormat.Type.WAVE, file);
            System.out.println("生成: " + filename);
        } catch (Exception e) {
            System.out.println("错误生成 " + filename + ": " + e.getMessage());
        }
    }
}