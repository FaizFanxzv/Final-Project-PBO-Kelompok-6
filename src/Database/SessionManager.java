package Database;

/**
 * SessionManager — Menyimpan data sesi login yang sedang aktif.
 * Singleton. Diakses dari mana saja tanpa passing parameter.
 */
public class SessionManager {

    private static SessionManager instance;

    private int    userId   = -1;
    private String username = "";
    private DatabaseManager.GameProgress progress;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) instance = new SessionManager();
        return instance;
    }

    // ── Login / Logout ────────────────────────────────────────────────────

    public void login(int userId, String username) {
        this.userId   = userId;
        this.username = username;
        this.progress = DatabaseManager.getInstance().getProgress(userId);
    }

    public void logout() {
        userId   = -1;
        username = "";
        progress = null;
    }

    public boolean isLoggedIn() { return userId > 0; }

    // ── Getters ──────────────────────────────────────────────────────────

    public int    getUserId()   { return userId;   }
    public String getUsername() { return username; }

    public DatabaseManager.GameProgress getProgress() {
        if (progress == null && userId > 0) {
            progress = DatabaseManager.getInstance().getProgress(userId);
        }
        return progress != null ? progress : new DatabaseManager.GameProgress();
    }

    /** Refresh progress dari DB (panggil setelah simpan hasil game) */
    public void refreshProgress() {
        if (userId > 0) {
            progress = DatabaseManager.getInstance().getProgress(userId);
        }
    }

    // ── Shortcut unlock checks ────────────────────────────────────────────

    public boolean isCharUnlocked(String charKey) {
        return getProgress().isCharUnlocked(charKey);
    }

    public boolean isMapUnlocked(String mapKey) {
        return getProgress().isMapUnlocked(mapKey);
    }
}