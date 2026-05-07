package GUI;

/**
 * AssetConfig v4 — Konfigurasi aset & konstanta game terpusat.
 *
 * PERUBAHAN v4:
 *  [1] Konstanta ukuran render karakter & enemy (PERBAIKAN #4: custom image size)
 *  [2] Konstanta ukuran kartu karakter di PreGameScreen
 *  [3] Semua perubahan v3 tetap ada
 */
public class AssetConfig {

    // ============================================================
    // PRE-GAME SCREEN
    // ============================================================
    public static final String BG_PREGAME = "/ImageAssets/bg_pregame.jpg";

    // ============================================================
    // UKURAN RENDER KARAKTER (PERBAIKAN #4 — Custom Image Size)
    // ============================================================

    /**
     * Ukuran RENDER player di game panel (bisa diubah bebas).
     * Default sebelumnya mengikuti PLAYER_W/H di AnimationEngine.
     * Sekarang dipisah agar bisa dikustomisasi tanpa ubah hitbox.
     */
    public static int PLAYER_RENDER_W = 200;  // lebar render gambar player
    public static int PLAYER_RENDER_H = 200;  // tinggi render gambar player

    /**
     * Ukuran RENDER enemy di game panel.
     */
    public static int ENEMY_RENDER_W  = 180;  // lebar render gambar enemy
    public static int ENEMY_RENDER_H  = 180;  // tinggi render gambar enemy

    /**
     * Offset Y render enemy dari posisi base.
     * Berguna jika gambar zombie terlalu tinggi/rendah.
     */
    public static int ENEMY_RENDER_OFFSET_Y = 0;

    /**
     * Ukuran gambar karakter di kartu PreGameScreen.
     * CHAR_CARD_IMG_W: lebar gambar dalam kartu.
     * CHAR_CARD_IMG_RATIO: rasio tinggi/lebar (misal 1.0 = kotak, 1.3 = portrait).
     */
    public static final int   CHAR_CARD_IMG_W     = 100; // px lebar gambar di kartu
    public static final float CHAR_CARD_IMG_RATIO  = 1.2f; // tinggi = W * ratio

    // ============================================================
    // KARAKTER PLAYER — 5 pilihan
    // ============================================================
    public static final String[] CHAR_KEYS = {
        "UNESA_BOYS", "UNESA_GIRLS", "KNIGHT_PRINCE", "RAYMOND", "XAVIER"
    };

    public static final String CHAR_UNESA_BOYS    = "/ImageAssets/Player_Unesa.png";
    public static final String CHAR_UNESA_GIRLS   = "/ImageAssets/Unesa_Girls.png";
    public static final String CHAR_KNIGHT_PRINCE = "/ImageAssets/Knight_Prince.png";
    public static final String CHAR_RAYMOND       = "/ImageAssets/Raymnd.png";
    public static final String CHAR_XAVIER        = "/ImageAssets/Xavier.png";

    public static String getCharacterPath(String key) {
        switch (key) {
            case "UNESA_BOYS":    return CHAR_UNESA_BOYS;
            case "UNESA_GIRLS":   return CHAR_UNESA_GIRLS;
            case "KNIGHT_PRINCE": return CHAR_KNIGHT_PRINCE;
            case "RAYMOND":       return CHAR_RAYMOND;
            case "XAVIER":        return CHAR_XAVIER;
            default:              return CHAR_UNESA_BOYS;
        }
    }

    public static String getCharacterName(String key) {
        switch (key) {
            case "UNESA_BOYS":    return "Unesa Boys";
            case "UNESA_GIRLS":   return "Unesa Girls";
            case "KNIGHT_PRINCE": return "Knight Prince";
            case "RAYMOND":       return "Raymond";
            case "XAVIER":        return "Xavier";
            default:              return "Hero";
        }
    }

    public static int[] getCharacterBaseStats(String key) {
        switch (key) {
            case "UNESA_BOYS":    return new int[]{ 100, 15, 3 };
            case "UNESA_GIRLS":   return new int[]{ 100, 15, 3 };
            case "KNIGHT_PRINCE": return new int[]{ 100, 15, 3 };
            case "RAYMOND":       return new int[]{ 100,  5, 3 };
            case "XAVIER":        return new int[]{ 120, 13, 2 };
            default:              return new int[]{ 100, 15, 3 };
        }
    }

    public static String getCharacterStats(String key) {
        int[] s = getCharacterBaseStats(key);
        return "HP:" + s[0] + " ATK:" + s[1] + " Heal:" + s[2];
    }

    public static String getCharacterAbility(String key) {
        switch (key) {
            case "UNESA_BOYS":    return "Unesa Pride: ATK +10% di Map Unesa";
            case "UNESA_GIRLS":   return "Unesa Spirit: ATK +10% di Map Unesa";
            case "KNIGHT_PRINCE": return "Noble Blade: ATK dasar +5% pasif";
            case "RAYMOND":       return "Shadow Step: Dodge +30% awal, ATK-5";
            case "XAVIER":        return "Vampiric: Serap 10% dari damage ke enemy";
            default:              return "";
        }
    }

    // ============================================================
    // ZOMBOSS
    // ============================================================
    public static final String ZOMBOSS = "/ImageAssets/Zomboss.png";

    // ============================================================
    // ZOMBIE PER WAVE
    // ============================================================
    public static String getZombieForWave(int wave) {
        switch (wave) {
            case 1:  return "/ImageAssets/L1.png";
            case 2:  return "/ImageAssets/L2.png";
            case 3:  return "/ImageAssets/L3.png";
            case 4:  return "/ImageAssets/L4.png";
            case 5:  return "/ImageAssets/L5.png";
            case 6:  return "/ImageAssets/L6.png";
            case 7:  return "/ImageAssets/L7.png";
            case 8:  return "/ImageAssets/L8.png";
            case 9:  return "/ImageAssets/L9.png";
            default: return "/ImageAssets/L1.png";
        }
    }

    // ============================================================
    // ZOMBIE STATS — scaling 17.5% per wave
    // ============================================================
    public static final int ZOMBIE_BASE_HP  = 125;
    public static final int ZOMBIE_BASE_ATK = 10;

    public static int getZombieHp(int wave) {
        double mult = Math.pow(1.175, wave - 1);
        return (int)(ZOMBIE_BASE_HP * mult);
    }

    public static int getZombieAtk(int wave) {
        double mult = Math.pow(1.25, wave - 1);
        return Math.max(10, (int)(ZOMBIE_BASE_ATK * mult));
    }

    // ============================================================
    // MAP / MEDAN PERANG
    // ============================================================
    public static final String[] MAP_KEYS = {
        "FOREST", "UNESA", "FROZEN", "MOUNTAIN", "ZOMBOSS_MAP"
    };

    public static final String BG_FOREST      = "/ImageAssets/forest_3.jpg";
    public static final String BG_UNESA       = "/ImageAssets/unesa_lidah_wetan.png";
    public static final String BG_FROZEN      = "/ImageAssets/FROZEN.png";
    public static final String BG_MOUNTAIN    = "/ImageAssets/MOUNTAIN.png";
    public static final String BG_ZOMBOSS_MAP = "/ImageAssets/Hell.jpg";
    public static final String BG_DEFAULT     = "/ImageAssets/forest_3.jpg";

    public static String getBgPath(String mapKey) {
        switch (mapKey) {
            case "UNESA":       return BG_UNESA;
            case "FROZEN":      return BG_FROZEN;
            case "MOUNTAIN":    return BG_MOUNTAIN;
            case "ZOMBOSS_MAP": return BG_ZOMBOSS_MAP;
            case "FOREST":
            default:            return BG_FOREST;
        }
    }

    public static String getBgForWave10() { return BG_ZOMBOSS_MAP; }

    public static String getMapName(String mapKey) {
        switch (mapKey) {
            case "FOREST":      return "Hutan Terlarang";
            case "UNESA":       return "Kampus Unesa";
            case "FROZEN":      return "Tundra Beku";
            case "MOUNTAIN":    return "Gunung Berapi";
            case "ZOMBOSS_MAP": return "Neraka Zomboss";
            default:            return mapKey;
        }
    }

    public static String[] getMapDesc(String mapKey) {
        switch (mapKey) {
            case "FOREST":
                return new String[]{"Map normal, tidak ada efek khusus.", "Cocok untuk pemula. Selesaikan untuk membuka segalanya!"};
            case "UNESA":
                return new String[]{"Zombie ATK +5% per wave.", "Unesa Boys/Girls dapat bonus ATK +10%."};
            case "FROZEN":
                return new String[]{"Player dibekukan 4 detik, tidak bisa attack/heal.", "Zombie bebas menyerang saat freeze!"};
            case "MOUNTAIN":
                return new String[]{"Zombie menyerang dari jarak lebih jauh.", "Interval serangan zombie 5s → 2s per wave!"};
            case "ZOMBOSS_MAP":
                return new String[]{"-5% HP player tiap 3 detik!", "Zombie ATK +50%. Berlaku dari wave 1."};
            default:
                return new String[]{""};
        }
    }

    // ============================================================
    // BGM PER MAP
    // ============================================================
    public static String getBgmForMap(String mapKey) {
        switch (mapKey) {
            case "FOREST":      return SoundManager.BGM_FOREST;
            case "UNESA":       return SoundManager.BGM_UNESA;
            case "FROZEN":      return SoundManager.BGM_FROZEN;
            case "MOUNTAIN":    return SoundManager.BGM_VOLCANO;
            case "ZOMBOSS_MAP": return SoundManager.BGM_BOSS;
            default:            return SoundManager.BGM_MAIN;
        }
    }

    public static String getMapBgmName(String mapKey) {
        switch (mapKey) {
            case "FOREST":      return "Forest Ambience";
            case "UNESA":       return "Unesa Theme";
            case "FROZEN":      return "Frozen Winds";
            case "MOUNTAIN":    return "Inferno March";
            case "ZOMBOSS_MAP": return "Final Boss Theme";
            default:            return "Battle Theme";
        }
    }

    // ============================================================
    // BUFF ICON PATHS
    // ============================================================
    public static final String BUFF_STEEL_HEART    = "/ImageAssets/buff_steel_heart.png";
    public static final String BUFF_STRONG_DEFENSE = "/ImageAssets/buff_strong_defense.png";
    public static final String BUFF_PUNCH_STRIKE   = "/ImageAssets/buff_punch_attack.png";
    public static final String BUFF_GOD_SLAYER     = "/ImageAssets/buff_god_slayer.png";
    public static final String BUFF_LAST_CHANCE    = "/ImageAssets/buff_last_chance.png";
    public static final String BUFF_LIFESTEAL      = "/ImageAssets/buff_LifesteaL.png";
    public static final String BUFF_INVISIBLE      = "/ImageAssets/buff_invisible.png";

    public static String getBuffIconPath(String buffName) {
        if (buffName.contains("Steel Heart"))      return BUFF_STEEL_HEART;
        if (buffName.contains("Strong Defense"))   return BUFF_STRONG_DEFENSE;
        if (buffName.contains("Punch Strike"))     return BUFF_PUNCH_STRIKE;
        if (buffName.contains("God Slayer"))       return BUFF_GOD_SLAYER;
        if (buffName.contains("Last Chance"))      return BUFF_LAST_CHANCE;
        if (buffName.contains("Lifesteal"))        return BUFF_LIFESTEAL;
        if (buffName.contains("Invisible"))        return BUFF_INVISIBLE;
        return null;
    }

    // ============================================================
    // MAP EFFECT CONSTANTS
    // ============================================================
    public static int getProximityThreshold(String mapKey) {
        if ("MOUNTAIN".equals(mapKey)) return 150;
        return 70;
    }

    public static final int ZOMBOSS_MAP_DOT_INTERVAL = 3000;
    public static final float ZOMBOSS_MAP_DOT_PERCENT = 0.05f;

    public static float getZombieAtkMultiplier(String mapKey, int wave) {
        switch (mapKey) {
            case "UNESA":
                return 1f + Math.min(wave * 0.05f, 0.5f);
            case "ZOMBOSS_MAP":
                return 1.5f;
            default:
                return 1f;
        }
    }

    public static float getPlayerAtkMultiplier(String mapKey, String charKey) {
        if ("UNESA".equals(mapKey) &&
            ("UNESA_BOYS".equals(charKey) || "UNESA_GIRLS".equals(charKey))) {
            return 1.10f;
        }
        return 1f;
    }

    public static int getFreezeInterval(int wave) {
        return Math.max(5000, 12000 - wave * 700);
    }

    public static final int FREEZE_DURATION_MS = 4000;

    public static int getMountainAttackInterval(int wave) {
        int interval = 5000 - (wave - 1) * 333;
        return Math.max(2000, interval);
    }

    public static int getDefaultAttackInterval(int wave) {
        return Math.max(3000, 10000 - (wave - 1) * 700);
    }

    // ============================================================
    // OPACITY BACKGROUND PER MAP (PERBAIKAN #3 — Unesa kontras)
    // ============================================================

    /**
     * Alpha overlay hitam di atas background map dalam game.
     * Nilai 0–255. Semakin tinggi = makin gelap = karakter lebih terlihat.
     * UNESA di-set lebih gelap agar zombie kontras dengan background terang.
     */
    public static int getMapBgDimAlpha(String mapKey) {
        switch (mapKey) {
            case "UNESA":       return 80;   // lebih gelap dari biasanya
            case "ZOMBOSS_MAP": return 40;
            case "FROZEN":      return 25;
            case "MOUNTAIN":    return 20;
            default:            return 0;    // Forest = tidak di-dim
        }
    }

    /**
     * Warna tint overlay (tambahan di atas dim hitam).
     * UNESA: sedikit ungu/biru agar zombie warna cerah kontras.
     * null = tidak ada tint.
     */
    public static java.awt.Color getMapBgTint(String mapKey) {
        switch (mapKey) {
            case "UNESA": return new java.awt.Color(10, 5, 30, 40); // tint gelap ungu
            default:      return null;
        }
    }
}