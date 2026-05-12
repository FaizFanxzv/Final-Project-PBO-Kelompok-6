package GUI;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;

/**
 * SoundManager — Manajemen audio game.
 *
 * PENGEMBANGAN:
 *  [+] BGM berbeda per map (FOREST, CITY,_FROZEN, VOLCANO, BOSS)
 *  [+] Volume control
 *  [+] Graceful fallback jika file tidak ditemukan
 *
 * FORMAT: .wav (PCM 16-bit, 44100Hz, mono/stereo)
 *
 * CARA PAKAI:
 *   SoundManager.play(SoundManager.ATTACK);
 *   SoundManager.playBGM(SoundManager.BGM_FOREST);
 *   SoundManager.stopBGM();
 *   SoundManager.toggleMute();
 */
public class SoundManager {

    // ============================================================
    // DAFTAR NAMA SUARA — efek & BGM
    // ============================================================
    // Efek suara
    public static final String ATTACK      = "attack";
    public static final String HEAL        = "heal";
    public static final String HIT_PLAYER  = "hit_player";
    public static final String HIT_ENEMY   = "hit_enemy";
    public static final String DEATH       = "death";
    public static final String WAVE_CLEAR  = "wave_clear";
    public static final String WAVE_START  = "wave_start";
    public static final String MISS        = "miss";
    public static final String BUFF_SELECT = "buff_select";
    public static final String LAST_CHANCE = "last_chance";
    public static final String GAME_OVER   = "game_over";
    public static final String VICTORY     = "victory";

    // BGM — satu per map [PENGEMBANGAN #4]
    public static final String BGM_MAIN  = "bgm_main";    // fallback generic
    public static final String BGM_BOSS      = "bgm_boss";      // Wave 10 Zomboss
    public static final String BGM_FOREST    = "bgm_forest";    // Map Hutan
    public static final String BGM_UNESA      = "bgm_city";      // Map Kota Mati
    public static final String BGM_FROZEN   = "bgm_FROZEN"; // Map Pemakaman
    public static final String BGM_VOLCANO   = "bgm_volcano";   // Map Gunung Berapi

    // ============================================================
    // PETA PATH SUARA — isi sesuai file .wav yang tersedia
    // ============================================================
    private static final java.util.Map<String, String> SOUND_PATHS = new java.util.HashMap<>();
    static {
        // Efek suara
        SOUND_PATHS.put(ATTACK,      "/ImageAssets/sounds/attack.wav");
        SOUND_PATHS.put(HEAL,        "/ImageAssets/sounds/heal.wav");
        SOUND_PATHS.put(HIT_PLAYER,  "/ImageAssets/sounds/hit_player.wav");
        SOUND_PATHS.put(HIT_ENEMY,   "/ImageAssets/sounds/hit_enemy.wav");
        SOUND_PATHS.put(DEATH,       "/ImageAssets/sounds/death.wav");
        SOUND_PATHS.put(WAVE_CLEAR,  "/ImageAssets/sounds/wave_clear.wav");
        SOUND_PATHS.put(WAVE_START,  "/ImageAssets/sounds/wave_start.wav");
        SOUND_PATHS.put(MISS,        "/ImageAssets/sounds/miss.wav");
        SOUND_PATHS.put(BUFF_SELECT, "/ImageAssets/sounds/buff_select.wav");
        SOUND_PATHS.put(LAST_CHANCE, "/ImageAssets/sounds/last_chance.wav");
        SOUND_PATHS.put(GAME_OVER,   "/ImageAssets/sounds/game_over.wav");
        SOUND_PATHS.put(VICTORY,     "/ImageAssets/sounds/victory.wav");

        // BGM per map [PENGEMBANGAN #4]
        SOUND_PATHS.put(BGM_MAIN,    "/ImageAssets/sounds/bgm_main.wav");
        SOUND_PATHS.put(BGM_BOSS,      "/ImageAssets/sounds/bgm_boss.wav");
        SOUND_PATHS.put(BGM_FOREST,    "/ImageAssets/sounds/bgm_forest.wav");
        SOUND_PATHS.put(BGM_UNESA,      "/ImageAssets/sounds/bgm_unesa.wav");
        SOUND_PATHS.put(BGM_FROZEN, "/ImageAssets/sounds/bgm_FROZEN.wav");
        SOUND_PATHS.put(BGM_VOLCANO,   "/ImageAssets/sounds/bgm_mountain.wav");
    }

    private static Clip   bgmClip  = null;
    private static boolean muted   = false;
    private static float   volume  = 0.8f; // 0.0 – 1.0

    // ============================================================
    // API PUBLIK
    // ============================================================

    /** Putar efek suara sekali (one-shot, tidak interrupt BGM). */
    public static void play(String soundName) {
        if (muted) return;
        String path = SOUND_PATHS.get(soundName);
        if (path == null) return;
        try {
            URL url = SoundManager.class.getResource(path);
            if (url == null) return;
            AudioInputStream ais = AudioSystem.getAudioInputStream(url);
            Clip clip = AudioSystem.getClip();
            clip.open(ais);
            setClipVolume(clip, volume * 0.85f); // efek sedikit lebih pelan dari BGM
            clip.start();
            clip.addLineListener(e -> {
                if (e.getType() == LineEvent.Type.STOP) clip.close();
            });
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException ignored) {}
    }

    /**
     * Putar music latar (looping). Hentikan BGM sebelumnya.
     * Jika BGM yang diminta sama dengan yang sedang berjalan, tidak restart.
     */
    public static void playBGM(String soundName) {
        // Jangan restart jika sudah playing track yang sama
        if (bgmClip != null && bgmClip.isRunning()) {
            // Tidak ada cara mudah cek nama track di Java Clip,
            // jadi kita stop & restart — bisa dioptimasi dengan menyimpan currentBgmName
        }
        stopBGM();
        if (muted) return;
        String path = SOUND_PATHS.get(soundName);
        if (path == null) {
            // Fallback ke generic battle BGM
            path = SOUND_PATHS.get(BGM_MAIN);
            if (path == null) return;
        }
        try {
            URL url = SoundManager.class.getResource(path);
            if (url == null) return;
            AudioInputStream ais = AudioSystem.getAudioInputStream(url);
            bgmClip = AudioSystem.getClip();
            bgmClip.open(ais);
            setClipVolume(bgmClip, volume);
            bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
            bgmClip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException ignored) {}
    }

    /** Hentikan music latar. */
    public static void stopBGM() {
        if (bgmClip != null) {
            if (bgmClip.isRunning()) bgmClip.stop();
            bgmClip.close();
        }
        bgmClip = null;
    }

    /** Toggle mute semua suara. */
    public static void toggleMute() {
        muted = !muted;
        if (muted) {
            stopBGM();
        }
    }

    /**
     * Set volume global (0.0 – 1.0).
     * Berlaku untuk BGM yang diputar setelah ini.
     */
    public static void setVolume(float vol) {
        volume = Math.max(0f, Math.min(1f, vol));
        // Update volume BGM yang sedang berjalan
        if (bgmClip != null && bgmClip.isOpen()) {
            setClipVolume(bgmClip, volume);
        }
    }

    public static boolean isMuted()  { return muted; }
    public static float   getVolume(){ return volume; }

    // ============================================================
    // PRIVATE HELPERS
    // ============================================================
    private static void setClipVolume(Clip clip, float vol) {
        try {
            FloatControl fc = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            float min = fc.getMinimum(); // biasanya -80 dB
            float max = fc.getMaximum(); // biasanya 6 dB
            float gain = (vol <= 0f) ? min : (float)(20.0 * Math.log10(vol));
            fc.setValue(Math.max(min, Math.min(max, gain)));
        } catch (IllegalArgumentException | UnsupportedOperationException ignored) {
            // Kontrol volume tidak didukung perangkat — skip saja
        }
    }
}