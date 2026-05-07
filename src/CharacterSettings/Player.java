package CharacterSettings;

/**
 * Player v3 — Karakter player.
 *
 * PERUBAHAN v3:
 *  [+] Invisible/Dodge cap diperketat ke 50% (sesuai spesifikasi Invisible buff)
 *  [+] Semua logika lama tetap ada
 */
public class Player extends Character {
    private int  limitHeal       = 3;
    private int  invisibleChance = 0;
    private boolean hasLastChance = false;
    private boolean hasLifesteal  = false;

    public Player(String name, int hp, int damage) {
        super(name, hp, damage);
    }

    // ---- Setter heal limit ----
    public void setHealLimit(int limit) { this.limitHeal = limit; }

    // ---- Last Chance ----
    public void setLastChance(boolean status) { this.hasLastChance = status; }
    public boolean hasLastChance()            { return hasLastChance; }

    // ---- Lifesteal ----
    public void setLifesteal(boolean status) { this.hasLifesteal = status; }
    public boolean isLifesteal()             { return hasLifesteal; }

    // ---- Invisible / Dodge ----
    /**
     * Tambah dodge chance. Maksimum 75% sesuai spesifikasi Invisible buff.
     * (Raymond mendapat 30% dari ability, bisa meningkat ke maks 75% via buff)
     */
    public void addInvisible(int amount) {
        this.invisibleChance += amount;
        if (this.invisibleChance > 75) this.invisibleChance = 75; 
    }

    public int getInvisibleChance() { return invisibleChance; }

    // ---- Heal ----
    public void heal() {
        if (limitHeal <= 0) {
            System.out.println(" [!] Gagal: Kesempatan Heal kamu sudah habis!");
            return;
        }
        if (getHp() >= getMaxHp()) {
            System.out.println(" [!] Gagal: HP kamu masih penuh (" + getHp() + "/" + getMaxHp() + ")!");
            return;
        }
        // Heal 90% maxHP
        int healAmount = Math.max(10, (int)(getMaxHp() * 0.90));
        int hpBaru = Math.min(getHp() + healAmount, getMaxHp());
        setHp(hpBaru);
        limitHeal--;
        System.out.println(" <<< Healing Berhasil! HP: " + getHp() + "/" + getMaxHp());
        System.out.println("     Sisa limit heal: " + limitHeal);
    }

    public int getHealLimit() { return limitHeal; }

    // ---- Attack (override untuk lifesteal — lifesteal sekarang 10% maxHP, dihandle di GamePanel) ----
    @Override
    public void attack(Character target) {
        super.attack(target);
        // Lifesteal visual dihandle di GamePanel (10% maxHP per serangan)
        // Baris ini hanya untuk kompatibilitas console output
    }

    public boolean tryDodge() {
        return Math.random() * 100 < invisibleChance;
    }
}