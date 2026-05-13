package Database;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;

/**
 * DatabaseManager v4 — Pure Java file-based database.
 *
 * PERUBAHAN v4:
 *  [1] Tambah field storyXShown di GameProgress untuk tracking cerita yang sudah ditampilkan.
 *  [2] Tambah recentAccounts di file terpisah (recent_accounts.properties)
 *      untuk fitur "akun yang pernah login" di AuthScreen.
 *  [3] Unlock logic v3 tetap tidak berubah.
 */
public class DatabaseManager {

    private static final String DATA_DIR       = "data";
    private static final String USERS_FILE     = DATA_DIR + "/users.properties";
    private static final String COUNTER_FILE   = DATA_DIR + "/counter.properties";
    private static final String RECENT_FILE    = DATA_DIR + "/recent_accounts.properties";
    private static final int    MAX_RECENT     = 5;

    private static DatabaseManager instance;

    private DatabaseManager() { ensureDataDir(); }

    public static DatabaseManager getInstance() {
        if (instance == null) instance = new DatabaseManager();
        return instance;
    }

    // ─────────────────────────────────────────────────────────────────────
    // SETUP
    // ─────────────────────────────────────────────────────────────────────

    private void ensureDataDir() {
        try { Files.createDirectories(Paths.get(DATA_DIR)); }
        catch (IOException e) { System.err.println("[DB] Gagal buat folder: " + e.getMessage()); }
    }

    // ─────────────────────────────────────────────────────────────────────
    // USERS
    // ─────────────────────────────────────────────────────────────────────

    private Properties loadUsers() {
        Properties p = new Properties();
        File f = new File(USERS_FILE);
        if (!f.exists()) return p;
        try (FileInputStream in = new FileInputStream(f)) { p.load(in); }
        catch (IOException e) { System.err.println("[DB] loadUsers: " + e.getMessage()); }
        return p;
    }

    private synchronized void saveUsers(Properties p) {
        try (FileOutputStream out = new FileOutputStream(USERS_FILE)) {
            p.store(out, "Last Chance For Life — Users");
        } catch (IOException e) { System.err.println("[DB] saveUsers: " + e.getMessage()); }
    }

    private synchronized int nextUserId() {
        Properties p = new Properties();
        File f = new File(COUNTER_FILE);
        if (f.exists()) {
            try (FileInputStream in = new FileInputStream(f)) { p.load(in); }
            catch (IOException ignored) {}
        }
        int id = Integer.parseInt(p.getProperty("next_id", "1"));
        p.setProperty("next_id", String.valueOf(id + 1));
        try (FileOutputStream out = new FileOutputStream(COUNTER_FILE)) {
            p.store(out, "counter");
        } catch (IOException ignored) {}
        return id;
    }

    // ─────────────────────────────────────────────────────────────────────
    // AUTH — REGISTER & LOGIN
    // ─────────────────────────────────────────────────────────────────────

    public synchronized boolean register(String username, String password) {
        if (username == null || username.trim().isEmpty()) return false;
        if (password == null || password.length() < 4)    return false;

        String key = username.trim().toLowerCase();
        Properties users = loadUsers();
        if (users.containsKey(key)) return false;

        int    id   = nextUserId();
        String hash = sha256(password);
        users.setProperty(key, hash + "|" + id + "|" + username.trim());
        saveUsers(users);
        saveProgress(id, new GameProgress());
        System.out.println("[DB] Akun dibuat: " + username + " (id=" + id + ")");
        return true;
    }

    public int login(String username, String password) {
        if (username == null || password == null) return -1;
        String key = username.trim().toLowerCase();
        Properties users = loadUsers();
        String entry = users.getProperty(key);
        if (entry == null) return -1;

        String[] parts = entry.split("\\|", 3);
        if (parts.length < 2) return -1;
        if (!sha256(password).equals(parts[0])) return -1;

        int id = Integer.parseInt(parts[1]);
        // Simpan ke daftar akun terakhir
        recordRecentAccount(username.trim(), id);
        System.out.println("[DB] Login berhasil: " + username + " (id=" + id + ")");
        return id;
    }

    public boolean usernameExists(String username) {
        if (username == null) return false;
        return loadUsers().containsKey(username.trim().toLowerCase());
    }

    public String getDisplayName(int userId) {
        Properties users = loadUsers();
        for (String key : users.stringPropertyNames()) {
            String val = users.getProperty(key);
            String[] parts = val.split("\\|", 3);
            if (parts.length >= 2 && Integer.parseInt(parts[1]) == userId)
                return parts.length >= 3 ? parts[2] : capitalize(key);
        }
        return "Hero";
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    // ─────────────────────────────────────────────────────────────────────
    // RECENT ACCOUNTS  [v4 NEW]
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Catat akun yang baru login ke daftar "terakhir digunakan".
     * Maksimal MAX_RECENT akun, diurutkan dari yang terbaru.
     */
    private synchronized void recordRecentAccount(String displayName, int userId) {
        Properties p = loadRecentAccounts();

        // Hapus entri lama untuk user ini jika ada
        String toRemove = null;
        for (String k : p.stringPropertyNames()) {
            String v = p.getProperty(k);
            String[] parts = v.split("\\|", 3);
            if (parts.length >= 2 && parts[1].equals(String.valueOf(userId))) {
                toRemove = k;
                break;
            }
        }
        if (toRemove != null) p.remove(toRemove);

        // Geser semua entri ke bawah (slot 0 = paling baru)
        List<String> existing = new ArrayList<>();
        for (int i = 0; i < MAX_RECENT; i++) {
            String v = p.getProperty("recent_" + i);
            if (v != null) existing.add(v);
        }
        // Tambahkan yang baru di depan
        existing.add(0, displayName + "|" + userId + "|" + System.currentTimeMillis());

        // Simpan kembali (maks MAX_RECENT)
        p.clear();
        for (int i = 0; i < Math.min(existing.size(), MAX_RECENT); i++) {
            p.setProperty("recent_" + i, existing.get(i));
        }
        saveRecentAccounts(p);
    }

    /**
     * Ambil daftar akun terakhir login.
     * @return List string "DisplayName|userId|timestamp", paling baru di indeks 0.
     */
    public List<String[]> getRecentAccounts() {
        Properties p = loadRecentAccounts();
        List<String[]> result = new ArrayList<>();
        for (int i = 0; i < MAX_RECENT; i++) {
            String v = p.getProperty("recent_" + i);
            if (v == null) break;
            String[] parts = v.split("\\|", 3);
            if (parts.length >= 2) result.add(parts);
        }
        return result;
    }

    private Properties loadRecentAccounts() {
        Properties p = new Properties();
        File f = new File(RECENT_FILE);
        if (!f.exists()) return p;
        try (FileInputStream in = new FileInputStream(f)) { p.load(in); }
        catch (IOException ignored) {}
        return p;
    }

    private synchronized void saveRecentAccounts(Properties p) {
        try (FileOutputStream out = new FileOutputStream(RECENT_FILE)) {
            p.store(out, "Recent Accounts");
        } catch (IOException e) { System.err.println("[DB] saveRecentAccounts: " + e.getMessage()); }
    }

    // ─────────────────────────────────────────────────────────────────────
    // PROGRESS
    // ─────────────────────────────────────────────────────────────────────

    private String progressFile(int userId) {
        return DATA_DIR + "/progress_" + userId + ".properties";
    }

    public GameProgress getProgress(int userId) {
        Properties p = new Properties();
        File f = new File(progressFile(userId));
        if (!f.exists()) return new GameProgress();
        try (FileInputStream in = new FileInputStream(f)) { p.load(in); }
        catch (IOException e) { System.err.println("[DB] getProgress: " + e.getMessage()); }
        return GameProgress.fromProperties(p);
    }

    public synchronized void saveProgress(int userId, GameProgress gp) {
        try (FileOutputStream out = new FileOutputStream(progressFile(userId))) {
            gp.toProperties().store(out, "Progress user " + userId);
        } catch (IOException e) { System.err.println("[DB] saveProgress: " + e.getMessage()); }
    }

    public void saveGameResult(int userId, String mapKey, int wavesReached, boolean won) {
        GameProgress gp = getProgress(userId);
        gp.gamesCompleted++;
        gp.highestWave = Math.max(gp.highestWave, wavesReached);

        if ("UNESA".equals(mapKey) && wavesReached >= 5) {
            if (!gp.unlockUnesaGirls) {
                gp.unlockUnesaGirls = true;
                System.out.println("[DB] Unlock: Unesa Girls (wave " + wavesReached + ")");
            }
        }

        if (won) {
            gp.totalWins++;
            applyUnlocks(gp, mapKey, wavesReached);
        }

        saveProgress(userId, gp);
        System.out.println("[DB] Hasil disimpan → map=" + mapKey + " wave=" + wavesReached + " won=" + won);
    }

    private void applyUnlocks(GameProgress gp, String mapKey, int wave) {
        switch (mapKey) {
            case "FOREST":
                gp.unlockUnesa     = true;
                gp.unlockUnesaBoys = true;
                System.out.println("[DB] Unlock: Map Unesa + Unesa Boys");
                break;
            case "UNESA":
                gp.unlockFrozen  = true;
                gp.unlockRaymond = true;
                System.out.println("[DB] Unlock: Map Frozen + Raymond");
                break;
            case "FROZEN":
                gp.unlockMountain = true;
                gp.unlockXavier   = true;
                System.out.println("[DB] Unlock: Map Mountain + Xavier");
                break;
            case "MOUNTAIN":
                gp.unlockZombossMap = true;
                System.out.println("[DB] Unlock: Map Zomboss");
                break;
            case "ZOMBOSS_MAP":
                System.out.println("[DB] Menang di Zomboss! Semua konten terbuka.");
                break;
            default:
                System.out.println("[DB] applyUnlocks: map tidak dikenal = " + mapKey);
                break;
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // SHA-256
    // ─────────────────────────────────────────────────────────────────────

    private static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 tidak tersedia", e);
        }
    }

    // ═════════════════════════════════════════════════════════════════════
    // Inner DTO: GameProgress
    // ═════════════════════════════════════════════════════════════════════

    public static class GameProgress {
        public int     gamesCompleted   = 0;
        public int     highestWave      = 0;
        public int     totalWins        = 0;

        // Karakter
        public boolean unlockUnesaBoys    = false;
        public boolean unlockUnesaGirls   = false;
        public boolean unlockKnightPrince = true;
        public boolean unlockRaymond      = false;
        public boolean unlockXavier       = false;

        // Map
        public boolean unlockForest     = true;
        public boolean unlockUnesa      = false;
        public boolean unlockFrozen     = false;
        public boolean unlockMountain   = false;
        public boolean unlockZombossMap = false;

        // Story flags [v4 NEW] — mana story yang sudah ditampilkan
        public boolean storyNewGameShown     = false;
        public boolean storyForestWinShown   = false;
        public boolean storyUnesaWinShown    = false;
        public boolean storyFrozenWinShown   = false;
        public boolean storyMountainWinShown = false;

        // ── Query ────────────────────────────────────────────────────────

        public boolean isCharUnlocked(String key) {
            switch (key) {
                case "UNESA_BOYS":    return unlockUnesaBoys;
                case "UNESA_GIRLS":   return unlockUnesaGirls;
                case "KNIGHT_PRINCE": return unlockKnightPrince;
                case "RAYMOND":       return unlockRaymond;
                case "XAVIER":        return unlockXavier;
                default:              return false;
            }
        }

        public boolean isMapUnlocked(String key) {
            switch (key) {
                case "FOREST":      return unlockForest;
                case "UNESA":       return unlockUnesa;
                case "FROZEN":      return unlockFrozen;
                case "MOUNTAIN":    return unlockMountain;
                case "ZOMBOSS_MAP": return unlockZombossMap;
                default:            return false;
            }
        }

        public String getUnlockHint(String key) {
            switch (key) {
                case "UNESA_BOYS":  return "Menangkan Map Forest";
                case "UNESA_GIRLS": return "Bertahan hingga Wave 5 di Map Unesa";
                case "RAYMOND":     return "Menangkan Map Unesa";
                case "XAVIER":      return "Menangkan Map Frozen";
                case "UNESA":       return "Menangkan Map Forest";
                case "FROZEN":      return "Menangkan Map Unesa";
                case "MOUNTAIN":    return "Menangkan Map Frozen";
                case "ZOMBOSS_MAP": return "Menangkan Map Mountain";
                default:            return "Selesaikan Forest terlebih dahulu";
            }
        }

        // ── Serialisasi ──────────────────────────────────────────────────

        Properties toProperties() {
            Properties p = new Properties();
            p.setProperty("gamesCompleted",        String.valueOf(gamesCompleted));
            p.setProperty("highestWave",           String.valueOf(highestWave));
            p.setProperty("totalWins",             String.valueOf(totalWins));
            p.setProperty("unlockUnesaBoys",       b(unlockUnesaBoys));
            p.setProperty("unlockUnesaGirls",      b(unlockUnesaGirls));
            p.setProperty("unlockKnightPrince",    b(unlockKnightPrince));
            p.setProperty("unlockRaymond",         b(unlockRaymond));
            p.setProperty("unlockXavier",          b(unlockXavier));
            p.setProperty("unlockForest",          b(unlockForest));
            p.setProperty("unlockUnesa",           b(unlockUnesa));
            p.setProperty("unlockFrozen",          b(unlockFrozen));
            p.setProperty("unlockMountain",        b(unlockMountain));
            p.setProperty("unlockZombossMap",      b(unlockZombossMap));
            // Story flags [v4]
            p.setProperty("storyNewGameShown",     b(storyNewGameShown));
            p.setProperty("storyForestWinShown",   b(storyForestWinShown));
            p.setProperty("storyUnesaWinShown",    b(storyUnesaWinShown));
            p.setProperty("storyFrozenWinShown",   b(storyFrozenWinShown));
            p.setProperty("storyMountainWinShown", b(storyMountainWinShown));
            return p;
        }

        static GameProgress fromProperties(Properties p) {
            GameProgress g = new GameProgress();
            g.gamesCompleted        = i(p, "gamesCompleted",   0);
            g.highestWave           = i(p, "highestWave",      0);
            g.totalWins             = i(p, "totalWins",        0);
            g.unlockUnesaBoys       = b(p, "unlockUnesaBoys",    false);
            g.unlockUnesaGirls      = b(p, "unlockUnesaGirls",   false);
            g.unlockKnightPrince    = b(p, "unlockKnightPrince", true);
            g.unlockRaymond         = b(p, "unlockRaymond",      false);
            g.unlockXavier          = b(p, "unlockXavier",       false);
            g.unlockForest          = b(p, "unlockForest",       true);
            g.unlockUnesa           = b(p, "unlockUnesa",        false);
            g.unlockFrozen          = b(p, "unlockFrozen",       false);
            g.unlockMountain        = b(p, "unlockMountain",     false);
            g.unlockZombossMap      = b(p, "unlockZombossMap",   false);
            // Story flags [v4]
            g.storyNewGameShown     = b(p, "storyNewGameShown",     false);
            g.storyForestWinShown   = b(p, "storyForestWinShown",   false);
            g.storyUnesaWinShown    = b(p, "storyUnesaWinShown",    false);
            g.storyFrozenWinShown   = b(p, "storyFrozenWinShown",   false);
            g.storyMountainWinShown = b(p, "storyMountainWinShown", false);
            return g;
        }

        private static String b(boolean v) { return String.valueOf(v); }
        private static boolean b(Properties p, String k, boolean def) {
            String v = p.getProperty(k); return v == null ? def : "true".equalsIgnoreCase(v);
        }
        private static int i(Properties p, String k, int def) {
            try { return Integer.parseInt(p.getProperty(k, String.valueOf(def))); }
            catch (NumberFormatException e) { return def; }
        }
    }
}