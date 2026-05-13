package GUI;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;

/**
 * SoundManager v2 — Manajemen audio game.
 *
 * PERBAIKAN v2:
 *  [FIX-SOUND-1] currentBgmName disimpan sehingga BGM bisa di-resume saat unmute.
 *  [FIX-SOUND-2] toggleMute() sekarang restart BGM ketika unmuting.
 *  [FIX-SOUND-3] Refactor playBGM → startBgmPlayback agar tidak mengubah currentBgmName.
 */
public class SoundManager {

    // ============================================================
    // KONSTANTA NAMA SUARA
    // ============================================================
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

    // BGM
    public static final String BGM_MAIN    = "bgm_main";
    public static final String BGM_BOSS    = "bgm_boss";
    public static final String BGM_FOREST  = "bgm_forest";
    public static final String BGM_UNESA   = "bgm_city";
    public static final String BGM_FROZEN  = "bgm_FROZEN";
    public static final String BGM_VOLCANO = "bgm_volcano";

    // ============================================================
    // PETA PATH
    // ============================================================
    private static final java.util.Map<String, String> SOUND_PATHS = new java.util.HashMap<>();
    static {
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

        SOUND_PATHS.put(BGM_MAIN,    "/ImageAssets/sounds/bgm_main.wav");
        SOUND_PATHS.put(BGM_BOSS,    "/ImageAssets/sounds/bgm_boss.wav");
        SOUND_PATHS.put(BGM_FOREST,  "/ImageAssets/sounds/bgm_forest.wav");
        SOUND_PATHS.put(BGM_UNESA,   "/ImageAssets/sounds/bgm_unesa.wav");
        SOUND_PATHS.put(BGM_FROZEN,  "/ImageAssets/sounds/bgm_FROZEN.wav");
        SOUND_PATHS.put(BGM_VOLCANO, "/ImageAssets/sounds/bgm_mountain.wav");
    }

    private static Clip   bgmClip        = null;
    private static boolean muted         = false;
    private static float   volume        = 0.8f;
    // FIX-SOUND-1: simpan nama BGM yang sedang/terakhir dimainkan
    private static String  currentBgmName = null;

    // ============================================================
    // API PUBLIK
    // ============================================================

    /** Putar efek suara sekali (one-shot). */
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
            setClipVolume(clip, volume * 0.85f);
            clip.start();
            clip.addLineListener(e -> {
                if (e.getType() == LineEvent.Type.STOP) clip.close();
            });
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException ignored) {}
    }

    /**
     * Putar BGM (looping). Hentikan BGM sebelumnya.
     * Menyimpan nama BGM untuk keperluan resume saat unmute.
     */
    public static void playBGM(String soundName) {
        // FIX-SOUND-3: simpan nama SEBELUM stop, lalu panggil startBgmPlayback
        currentBgmName = soundName;
        stopBGM();
        startBgmPlayback(soundName);
    }

    /** Hentikan BGM (clip ditutup, tidak mengubah currentBgmName). */
    public static void stopBGM() {
        if (bgmClip != null) {
            if (bgmClip.isRunning()) bgmClip.stop();
            bgmClip.close();
        }
        bgmClip = null;
    }

    /**
     * Toggle mute.
     * FIX-SOUND-2: Saat unmuting, BGM yang terakhir diputar akan di-resume.
     */
    public static void toggleMute() {
        muted = !muted;
        if (muted) {
            stopBGM();
        } else {
            // Resume BGM yang terakhir diputar
            if (currentBgmName != null) {
                startBgmPlayback(currentBgmName);
            }
        }
    }

    /** Set volume global (0.0–1.0). Update BGM yang sedang berjalan. */
    public static void setVolume(float vol) {
        volume = Math.max(0f, Math.min(1f, vol));
        if (bgmClip != null && bgmClip.isOpen()) {
            setClipVolume(bgmClip, volume);
        }
    }

    public static boolean isMuted()         { return muted;  }
    public static float   getVolume()       { return volume; }
    public static String  getCurrentBgm()   { return currentBgmName; }

    // ============================================================
    // PRIVATE HELPERS
    // ============================================================

    /**
     * Memulai pemutaran BGM secara internal tanpa mengubah currentBgmName.
     * Dipanggil oleh playBGM() dan toggleMute().
     */
    private static void startBgmPlayback(String soundName) {
        if (muted) return;
        String path = SOUND_PATHS.get(soundName);
        if (path == null) {
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

    private static void setClipVolume(Clip clip, float vol) {
        try {
            FloatControl fc = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            float min  = fc.getMinimum();
            float max  = fc.getMaximum();
            float gain = (vol <= 0f) ? min : (float)(20.0 * Math.log10(vol));
            fc.setValue(Math.max(min, Math.min(max, gain)));
        } catch (IllegalArgumentException | UnsupportedOperationException ignored) {}
    }
}