package CharacterSettings;

public class Character {
    // atribut
    private String name;
    private int hp;
    private int damage;
    private int maxHp; // Tambahan untuk membatasi heal
    
    // constructor
    public Character(String name, int hp, int damage) {
        this.name = name;
        this.hp = hp;
        this.maxHp = hp;
        this.damage = damage;
    }

    // Getter & Setter
    public String getName() { return name; }
    public int getHp() { return hp; }
    public void setHp(int hp) { this.hp = hp; }
    public int getMaxHp() { return maxHp; }
    public void setMaxHp(int maxHp) { this.maxHp = maxHp; }
    public int getDamage() { return damage; }
    public void setDamage(int damage) { this.damage = damage; }
    
    // method
    public void attack(Character target) {
    if (target instanceof Player) {
        Player p = (Player) target;
        
        // Menggunakan angka acak 1-100
        int roll = (int) (Math.random() * 100) + 1;
        
        if (roll <= p.getInvisibleChance()) {
            System.out.println(" >> [MISS!] " + this.getName() + " gagal menyerang! (Evasion: " + p.getInvisibleChance()+ "%)");
            return; 
        }
    }
    
    target.setHp(target.getHp() - this.getDamage());
    System.out.println(" >> " + getName() + " menyerang " + target.getName() + " sebesar " + getDamage() + " DMG!");
    }
    
}
