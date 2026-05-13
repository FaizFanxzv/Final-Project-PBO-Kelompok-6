package GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * AnimationEngine v6 — Mesin animasi terpusat game.
 *
 * PERUBAHAN v6:
 *  [1] HAPUS heat-box (fillRect) pada zombie & zomboss saat CHARGING.
 *      Diganti efek glow/aura pada border agar sprite tidak tertutupi.
 *  [2] Tambah animasi kebangkitan LAST CHANCE:
 *      - Fase 1 (0–30): player berkedip putih/merah (efek kematian sesaat)
 *      - Fase 2 (31–70): ledakan emas dari tubuh player, partikel naik
 *      - Fase 3 (71–110): player terang kembali dengan glow emas memudar
 *  [3] isReviving() getter untuk guard di GamePanel.
 *  [4] Semua perbaikan v5 tetap ada.
 */
public class AnimationEngine {

    // ── Konstanta posisi ────────────────────────────────────────────────
    public static final int PLAYER_BASE_X = 100;
    public static final int PLAYER_BASE_Y = 150;
    public static final int PLAYER_W      = 200;
    public static final int PLAYER_H      = 200;
    public static final int ENEMY_BASE_X  = 520;
    public static final int ENEMY_BASE_Y  = 150;
    public static final int ENEMY_W       = 180;
    public static final int ENEMY_H       = 180;
    public static final int WALL_LEFT     = 10;

    // ── State posisi ────────────────────────────────────────────────────
    private int playerX      = PLAYER_BASE_X;
    private int playerY      = PLAYER_BASE_Y;
    private int attackOffsetX = 0;

    // ── Flash & Efek ────────────────────────────────────────────────────
    private int playerFlashFrames = 0;
    private int enemyFlashFrames  = 0;
    private static final int FLASH_DURATION = 10;

    private int healGlowFrames     = 0;
    private static final int HEAL_DURATION = 25;

    private int vampiricGlowFrames = 0;
    private static final int VAMPIRIC_DURATION = 20;

    private int shakeFrames    = 0;
    private int shakeIntensity = 0;
    private static final int SHAKE_DURATION = 14;

    private final List<FloatingText> floatingTexts = new ArrayList<>();

    private final JPanel targetPanel;
    private final Timer  animTimer;

    // ── FSM Serangan Player ─────────────────────────────────────────────
    private enum AttackState { IDLE, MOVING_FORWARD, MOVING_BACK }
    private AttackState attackState = AttackState.IDLE;
    private Runnable    onAttackHit;

    // ── FSM Serangan Zombie ─────────────────────────────────────────────
    private enum ZombieAttackState { IDLE, CHARGING, RETREATING }
    private ZombieAttackState zombieAttackState = ZombieAttackState.IDLE;
    private int zombieAttackOffset = 0;
    private static final int ZOMBIE_CHARGE_DIST  = 60;
    private static final int ZOMBOSS_CHARGE_DIST = 100;
    private boolean isZomboss = false;

    // ── Zombie idle bob ─────────────────────────────────────────────────
    private int zombieWalkFrame = 0;
    private static final int WALK_BOB_AMP = 4;

    // ── Gerakan A/D ─────────────────────────────────────────────────────
    private boolean keyLeft  = false;
    private boolean keyRight = false;
    private static final int MOVE_SPEED = 5;
    private boolean movementEnabled = true;
    private boolean playerHasMoved  = false;

    // ── Freeze ──────────────────────────────────────────────────────────
    private boolean frozen       = false;
    private int     freezeFrames = 0;
    private static final int FREEZE_VISUAL_FRAMES = 150;

    // ── Animasi kematian ────────────────────────────────────────────────
    private boolean playerDeathAnim = false;
    private boolean enemyDeathAnim  = false;
    private int     deathFrame      = 0;
    private static final int DEATH_DURATION = 80;
    private Runnable onDeathComplete;

    // ── Animasi masuk enemy ─────────────────────────────────────────────
    private boolean entranceActive = false;
    private int     enemyCurrentX  = ENEMY_BASE_X;
    private static final int ENTRANCE_START_X = 900;
    private Runnable onEntranceComplete;

    // ── Wave intro ──────────────────────────────────────────────────────
    private String waveIntroText   = "";
    private int    waveIntroFrames = 0;
    private static final int WAVE_INTRO_DURATION = 80;

    // ── Pause ───────────────────────────────────────────────────────────
    private boolean paused = false;

    // ── Tombol ──────────────────────────────────────────────────────────
    private JButton btnAttack;
    private JButton btnHeal;

    // ────────────────────────────────────────────────────────────────────
    // [v6] LAST CHANCE REVIVAL ANIMATION
    // ────────────────────────────────────────────────────────────────────
    private boolean  revivalActive   = false;
    private int      revivalFrame    = 0;
    private static final int REVIVAL_DURATION = 110;
    private Runnable onRevivalComplete;
    // Partikel revival
    private final float[] revPtX  = new float[40];
    private final float[] revPtY  = new float[40];
    private final float[] revPtVX = new float[40];
    private final float[] revPtVY = new float[40];
    private final Color[] revPtC  = new Color[40];

    // ════════════════════════════════════════════════════════════════════
    public AnimationEngine(JPanel panel) {
        this.targetPanel = panel;

        animTimer = new Timer(16, e -> {
            if (!paused) tick();
            panel.repaint();
        });
        animTimer.start();

        panel.setFocusable(true);
        panel.setRequestFocusEnabled(true);

        panel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_A:
                    case KeyEvent.VK_LEFT:
                        keyLeft = true; playerHasMoved = true; break;
                    case KeyEvent.VK_D:
                    case KeyEvent.VK_RIGHT:
                        keyRight = true; playerHasMoved = true; break;
                    case KeyEvent.VK_SPACE:
                        if (btnAttack != null && btnAttack.isEnabled()) btnAttack.doClick();
                        break;
                    case KeyEvent.VK_H:
                        if (btnHeal != null && btnHeal.isEnabled()) btnHeal.doClick();
                        break;
                    case KeyEvent.VK_P:
                    case KeyEvent.VK_ESCAPE:
                        togglePause(); break;
                }
            }
            @Override
            public void keyReleased(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_A: case KeyEvent.VK_LEFT:  keyLeft  = false; break;
                    case KeyEvent.VK_D: case KeyEvent.VK_RIGHT: keyRight = false; break;
                }
            }
        });

        panel.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { panel.requestFocusInWindow(); }
        });

        panel.addHierarchyListener(e -> {
            if ((e.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0
                    && panel.isShowing()) {
                SwingUtilities.invokeLater(panel::requestFocusInWindow);
            }
        });
    }

    public void registerButtons(JButton attack, JButton heal) {
        this.btnAttack = attack;
        this.btnHeal   = heal;
    }

    public void setIsZomboss(boolean zb) { this.isZomboss = zb; }

    // ════════════════════════════════════════════════════════════════════
    // API PUBLIK
    // ════════════════════════════════════════════════════════════════════

    public void playAttack(Runnable onHit) {
        if (attackState != AttackState.IDLE) return;
        onAttackHit   = onHit;
        attackState   = AttackState.MOVING_FORWARD;
        attackOffsetX = 0;
        setButtonsEnabled(false);
        SoundManager.play(SoundManager.ATTACK);
    }

    public void playZombieAttack() {
        if (zombieAttackState != ZombieAttackState.IDLE) return;
        zombieAttackState  = ZombieAttackState.CHARGING;
        zombieAttackOffset = 0;
    }

    public void playPlayerHit(int dmg) {
        playerFlashFrames = FLASH_DURATION;
        addFloat("-" + dmg, playerX + PLAYER_W/2, playerY - 10, new Color(255,70,70));
        SoundManager.play(SoundManager.HIT_PLAYER);
    }

    public void playEnemyHit(int dmg) {
        enemyFlashFrames = FLASH_DURATION;
        addFloat("-" + dmg, enemyCurrentX + ENEMY_W/2, ENEMY_BASE_Y - 10, new Color(255,60,60));
        SoundManager.play(SoundManager.HIT_ENEMY);
    }

    public void playHeal(int hpGained) {
        healGlowFrames = HEAL_DURATION;
        addFloat("+" + hpGained + " HP", playerX + PLAYER_W/2, playerY - 20, new Color(80,255,120));
        SoundManager.play(SoundManager.HEAL);
    }

    public void playVampiricDrain(int hpGained) {
        vampiricGlowFrames = VAMPIRIC_DURATION;
        addFloat("+" + hpGained + " 🩸", playerX + PLAYER_W/2, playerY - 20, new Color(0,230,200));
    }

    public void playMiss() {
        addFloat("MISS!", playerX + PLAYER_W/2, playerY - 15, new Color(255,230,50));
        SoundManager.play(SoundManager.MISS);
    }

    public void playScreenShake(int intensity) {
        shakeFrames    = SHAKE_DURATION;
        shakeIntensity = intensity;
    }

    public void playWaveClear(int waveNum) {
        for (int i = 0; i < 10; i++) {
            int x = 150 + (int)(Math.random()*500);
            int y = 80  + (int)(Math.random()*200);
            Color c = new Color(55+(int)(Math.random()*200), 55+(int)(Math.random()*200), 55+(int)(Math.random()*200));
            addFloat("★", x, y, c);
        }
        addFloat("WAVE " + waveNum + " CLEAR!", 400, 70, Color.YELLOW);
        SoundManager.play(SoundManager.WAVE_CLEAR);
    }

    public void playDeath(boolean isPlayer, Runnable onComplete) {
        if (isPlayer) playerDeathAnim = true;
        else          enemyDeathAnim  = true;
        deathFrame      = 0;
        onDeathComplete = onComplete;
        SoundManager.play(SoundManager.DEATH);
    }

    public void playEnemyEntrance(Runnable onComplete) {
        entranceActive     = true;
        enemyCurrentX      = ENTRANCE_START_X;
        onEntranceComplete = onComplete;
        setButtonsEnabled(false);
        SoundManager.play(SoundManager.WAVE_START);
    }

    public void playWaveIntro(int wave) {
        waveIntroText   = (wave == 10) ? "⚠  ZOMBOSS  ⚠" : "WAVE  " + wave;
        waveIntroFrames = WAVE_INTRO_DURATION;
    }

    public void playFreeze() {
        frozen       = true;
        freezeFrames = FREEZE_VISUAL_FRAMES;
        addFloat("❄ FROZEN!", playerX + PLAYER_W/2, playerY - 30, new Color(100,200,255));
        if (btnAttack != null) btnAttack.setEnabled(false);
        if (btnHeal   != null) btnHeal.setEnabled(false);
    }

    public void unfreeze() {
        frozen       = false;
        freezeFrames = 0;
        if (attackState == AttackState.IDLE && !entranceActive && !revivalActive) {
            setButtonsEnabled(true);
        }
    }

    // ── [v6] LAST CHANCE REVIVAL ─────────────────────────────────────────
    /**
     * Memulai animasi kebangkitan Last Chance.
     * Menonaktifkan semua tombol dan gerakan selama animasi.
     * Callback onComplete dipanggil setelah animasi selesai.
     */
    public void playLastChanceRevival(Runnable onComplete) {
        revivalActive     = true;
        revivalFrame      = 0;
        onRevivalComplete = onComplete;
        setButtonsEnabled(false);
        movementEnabled = false;

        // Inisialisasi partikel naik
        int cx = playerX + PLAYER_W / 2;
        int cy = playerY + PLAYER_H / 2;
        for (int i = 0; i < revPtX.length; i++) {
            revPtX[i]  = cx + (float)((Math.random()-0.5) * PLAYER_W * 0.8);
            revPtY[i]  = cy + (float)((Math.random()-0.5) * PLAYER_H * 0.6);
            revPtVX[i] = (float)((Math.random()-0.5) * 3f);
            revPtVY[i] = -(float)(Math.random() * 4 + 1.5f);
            float hue  = 40f/360f + (float)(Math.random()*0.15f);
            revPtC[i]  = Color.getHSBColor(hue, 0.9f, 1f);
        }

        SoundManager.play(SoundManager.LAST_CHANCE);
        addFloat("✦ LAST CHANCE! ✦", playerX + PLAYER_W/2, playerY - 40, new Color(255,215,0));
    }

    // ── Getters & toggles ─────────────────────────────────────────────────

    public void togglePause()               { paused = !paused; }
    public boolean isPaused()               { return paused; }
    public boolean isAttacking()            { return attackState != AttackState.IDLE; }
    public boolean isInEntrance()           { return entranceActive; }
    public boolean isDeathPlaying()         { return playerDeathAnim || enemyDeathAnim; }
    public boolean isFrozen()               { return frozen; }
    public boolean isReviving()             { return revivalActive; }   // [v6]
    public void setMovementEnabled(boolean e) { movementEnabled = e; }
    public void stopTimer()                 { animTimer.stop(); }
    public boolean hasMoved()               { return playerHasMoved; }

    // ════════════════════════════════════════════════════════════════════
    // TICK
    // ════════════════════════════════════════════════════════════════════

    private void tick() {
        tickAttack();
        tickZombieAttack();
        tickMovement();
        tickDeath();
        tickEntrance();
        tickWaveIntro();
        tickFreeze();
        tickZombieWalk();
        tickRevival();   // [v6]

        if (playerFlashFrames  > 0) playerFlashFrames--;
        if (enemyFlashFrames   > 0) enemyFlashFrames--;
        if (healGlowFrames     > 0) healGlowFrames--;
        if (vampiricGlowFrames > 0) vampiricGlowFrames--;
        if (shakeFrames        > 0) shakeFrames--;

        Iterator<FloatingText> it = floatingTexts.iterator();
        while (it.hasNext()) {
            FloatingText ft = it.next();
            ft.tick();
            if (ft.isDead()) it.remove();
        }
    }

    private void tickAttack() {
        int speed = 20;
        switch (attackState) {
            case MOVING_FORWARD:
                attackOffsetX += speed;
                int maxOffset = (enemyCurrentX - PLAYER_W - 10) - playerX;
                if (attackOffsetX >= maxOffset) {
                    attackOffsetX = maxOffset;
                    attackState   = AttackState.MOVING_BACK;
                    if (onAttackHit != null) { onAttackHit.run(); onAttackHit = null; }
                }
                break;
            case MOVING_BACK:
                attackOffsetX -= speed;
                if (attackOffsetX <= 0) {
                    attackOffsetX = 0;
                    attackState   = AttackState.IDLE;
                    if (!frozen && !revivalActive) setButtonsEnabled(true);
                }
                break;
            default:
                attackOffsetX = 0;
                break;
        }
    }

    private void tickZombieAttack() {
        int chargeDist  = isZomboss ? ZOMBOSS_CHARGE_DIST : ZOMBIE_CHARGE_DIST;
        int chargeSpeed = isZomboss ? 10 : 7;
        switch (zombieAttackState) {
            case CHARGING:
                zombieAttackOffset += chargeSpeed;
                if (zombieAttackOffset >= chargeDist) {
                    zombieAttackOffset = chargeDist;
                    zombieAttackState  = ZombieAttackState.RETREATING;
                }
                break;
            case RETREATING:
                zombieAttackOffset -= chargeSpeed;
                if (zombieAttackOffset <= 0) {
                    zombieAttackOffset = 0;
                    zombieAttackState  = ZombieAttackState.IDLE;
                }
                break;
            default:
                zombieAttackOffset = 0;
                break;
        }
    }

    private void tickZombieWalk() { zombieWalkFrame++; }

    private void tickMovement() {
        if (!movementEnabled || frozen || revivalActive) return;
        int wallRight = enemyCurrentX - PLAYER_W - 20;
        if (keyLeft)  playerX = Math.max(WALL_LEFT, playerX - MOVE_SPEED);
        if (keyRight) playerX = Math.min(wallRight, playerX + MOVE_SPEED);
    }

    private void tickFreeze() {
        if (freezeFrames > 0) {
            freezeFrames--;
            if (freezeFrames <= 0) frozen = false;
        }
    }

    private void tickDeath() {
        if (!playerDeathAnim && !enemyDeathAnim) return;
        deathFrame++;
        if (deathFrame >= DEATH_DURATION) {
            playerDeathAnim = false;
            enemyDeathAnim  = false;
            deathFrame      = 0;
            if (onDeathComplete != null) { onDeathComplete.run(); onDeathComplete = null; }
        }
    }

    private void tickEntrance() {
        if (!entranceActive) return;
        enemyCurrentX -= 6;
        if (enemyCurrentX <= ENEMY_BASE_X) {
            enemyCurrentX  = ENEMY_BASE_X;
            entranceActive = false;
            if (!frozen && !revivalActive) setButtonsEnabled(true);
            if (onEntranceComplete != null) { onEntranceComplete.run(); onEntranceComplete = null; }
        }
    }

    private void tickWaveIntro() {
        if (waveIntroFrames > 0) waveIntroFrames--;
    }

    // ── [v6] Tick revival ──────────────────────────────────────────────
    private void tickRevival() {
        if (!revivalActive) return;
        revivalFrame++;

        // Perbarui partikel (fase 2: 31–70)
        if (revivalFrame >= 31 && revivalFrame <= 70) {
            for (int i = 0; i < revPtX.length; i++) {
                revPtX[i] += revPtVX[i];
                revPtY[i] += revPtVY[i];
                revPtVY[i] += 0.05f; // gravity lemah
            }
        }

        if (revivalFrame >= REVIVAL_DURATION) {
            revivalActive = false;
            revivalFrame  = 0;
            movementEnabled = true;
            if (!frozen) setButtonsEnabled(true);
            if (onRevivalComplete != null) { onRevivalComplete.run(); onRevivalComplete = null; }
        }
    }

    private void setButtonsEnabled(boolean enabled) {
        if (btnAttack != null) btnAttack.setEnabled(enabled);
        if (btnHeal   != null) btnHeal.setEnabled(enabled);
    }

    // ════════════════════════════════════════════════════════════════════
    // GETTERS
    // ════════════════════════════════════════════════════════════════════

    public int getPlayerX()       { return playerX; }
    public int getPlayerY()       { return playerY; }
    public int getPlayerRenderX() { return playerX + attackOffsetX; }
    public int getEnemyRenderX()  { return enemyCurrentX - zombieAttackOffset; }

    public int getEnemyRenderY() {
        if (zombieAttackState != ZombieAttackState.IDLE) return ENEMY_BASE_Y;
        int bob = (int)(Math.sin(zombieWalkFrame * 0.08) * WALK_BOB_AMP);
        return ENEMY_BASE_Y + bob;
    }

    public int getEnemyCurrentX()      { return enemyCurrentX; }
    public int getPlayerEnemyDistance(){ return enemyCurrentX - (playerX + PLAYER_W); }

    public int getShakeOffsetX() {
        if (shakeFrames <= 0) return 0;
        return (int)((Math.random()-0.5)*2*shakeIntensity);
    }
    public int getShakeOffsetY() {
        if (shakeFrames <= 0) return 0;
        return (int)((Math.random()-0.5)*2*shakeIntensity);
    }

    // ════════════════════════════════════════════════════════════════════
    // DRAW EFFECTS
    // ════════════════════════════════════════════════════════════════════

    public void drawEffects(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int renderX = getPlayerRenderX();
        int enemyRX = getEnemyRenderX();
        int enemyRY = getEnemyRenderY();

        // ── Flash efek normal ───────────────────────────────────────────
        if (playerFlashFrames > 0 && !revivalActive) {
            float alpha = (float)playerFlashFrames/FLASH_DURATION*0.65f;
            drawHitFlash(g2, renderX, playerY, PLAYER_W, PLAYER_H, alpha);
        }
        if (healGlowFrames > 0) {
            float alpha = (float)healGlowFrames/HEAL_DURATION*0.5f;
            drawHealGlow(g2, renderX, playerY, PLAYER_W, PLAYER_H, alpha);
        }
        if (vampiricGlowFrames > 0) {
            float alpha = (float)vampiricGlowFrames/VAMPIRIC_DURATION*0.5f;
            drawVampiricGlow(g2, renderX, playerY, PLAYER_W, PLAYER_H, alpha);
        }
        if (enemyFlashFrames > 0) {
            float alpha = (float)enemyFlashFrames/FLASH_DURATION*0.68f;
            drawHitFlash(g2, enemyRX, enemyRY, ENEMY_W, ENEMY_H, alpha);
        }

        // ── Freeze overlay ───────────────────────────────────────────────
        if (frozen && freezeFrames > 0) {
            drawFreezeOverlay(g2, renderX, playerY, PLAYER_W, PLAYER_H);
        }

        // ── [v6] HAPUS fillRect heat-box — ganti dengan GLOW AURA ────────
        if (zombieAttackState == ZombieAttackState.CHARGING) {
            float chargeAlpha = (float)zombieAttackOffset /
                    (isZomboss ? ZOMBOSS_CHARGE_DIST : ZOMBIE_CHARGE_DIST);
            Color baseColor = isZomboss ? new Color(180, 0, 200) : new Color(220, 50, 50);

            // Glow ring berlapis (tidak menutupi sprite)
            for (int r = 18; r >= 2; r -= 4) {
                int alpha = (int)(chargeAlpha * (14 - r) * 12);
                alpha = Math.max(0, Math.min(180, alpha));
                g2.setColor(new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), alpha));
                g2.setStroke(new BasicStroke(r));
                g2.drawRect(enemyRX - r/2, enemyRY - r/2, ENEMY_W + r, ENEMY_H + r);
            }
            g2.setStroke(new BasicStroke(1f));
        }

        // ── Animasi kematian ─────────────────────────────────────────────
        if (playerDeathAnim) drawDeathAnimation(g2, renderX, playerY, PLAYER_W, PLAYER_H, deathFrame, false);
        if (enemyDeathAnim)  drawDeathAnimation(g2, enemyRX, enemyRY, ENEMY_W, ENEMY_H, deathFrame, true);

        // ── [v6] REVIVAL ANIMATION ───────────────────────────────────────
        if (revivalActive) drawRevivalAnimation(g2, renderX, playerY);

        // ── Floating text ────────────────────────────────────────────────
        g2.setStroke(new BasicStroke(1f));
        for (FloatingText ft : floatingTexts) ft.draw(g2);

        if (waveIntroFrames > 0) drawWaveIntro(g2);
        if (paused)              drawPauseOverlay(g2);
    }

    // ── [v6] Revival animation ────────────────────────────────────────────
    private void drawRevivalAnimation(Graphics2D g2, int px, int py) {
        int cx = px + PLAYER_W / 2;
        int cy = py + PLAYER_H / 2;
        int W  = targetPanel.getWidth();
        int H  = targetPanel.getHeight();

        // FASE 1 (0–30): player berkedip merah → putih
        if (revivalFrame <= 30) {
            float t = revivalFrame / 30f;
            // Backdrop gelap
            float darkAlpha = t * 0.6f;
            g2.setColor(new Color(0f, 0f, 0f, darkAlpha));
            g2.fillRect(0, 0, W, H);
            // Overlay merah ke putih pada player
            float flashA = (float)Math.abs(Math.sin(revivalFrame * 0.55)) * 0.7f;
            Color flash = (revivalFrame % 8 < 4)
                    ? new Color(1f, 0.1f, 0.1f, flashA)
                    : new Color(1f, 1f, 1f, flashA * 0.6f);
            g2.setColor(flash);
            g2.fillRect(px, py, PLAYER_W, PLAYER_H);
        }

        // FASE 2 (31–70): ledakan emas + partikel naik
        if (revivalFrame >= 31 && revivalFrame <= 70) {
            float t = (revivalFrame - 31f) / 39f;

            // Backdrop
            float da = Math.max(0f, 0.55f - t * 0.4f);
            g2.setColor(new Color(0f, 0f, 0f, da));
            g2.fillRect(0, 0, W, H);

            // Ring ekspansi emas
            int ringR = (int)(t * 200);
            for (int r = 0; r < 5; r++) {
                int rr = ringR - r * 14;
                if (rr > 0) {
                    float ra = Math.max(0f, (0.6f - t * 0.5f) * (1f - r * 0.18f));
                    g2.setColor(new Color(255, 200, 50, (int)(ra * 255)));
                    g2.setStroke(new BasicStroke(4f - r));
                    g2.drawOval(cx - rr, cy - rr, rr*2, rr*2);
                }
            }
            g2.setStroke(new BasicStroke(1f));

            // Silhouette player (emas)
            float sA = 0.5f - t * 0.3f;
            g2.setColor(new Color(255, 210, 60, (int)(sA * 255)));
            g2.fillRect(px, py, PLAYER_W, PLAYER_H);

            // Partikel emas naik
            for (int i = 0; i < revPtX.length; i++) {
                float age = (revivalFrame - 31f) / 39f;
                float pA  = Math.max(0f, 1f - age * 1.2f);
                g2.setColor(new Color(
                        revPtC[i].getRed(), revPtC[i].getGreen(), revPtC[i].getBlue(),
                        (int)(pA * 230)));
                int size = (int)(6 * pA) + 2;
                g2.fillOval((int)revPtX[i] - size/2, (int)revPtY[i] - size/2, size, size);
            }

            // Teks besar "LAST CHANCE!"
            if (revivalFrame < 55) {
                float textA = Math.min(1f, (revivalFrame - 31f) / 15f);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, textA));
                g2.setFont(new Font("Serif", Font.BOLD, 44));
                FontMetrics fm = g2.getFontMetrics();
                String txt = "✦ LAST CHANCE! ✦";
                int tx = (W - fm.stringWidth(txt)) / 2;
                g2.setColor(new Color(0, 0, 0, 180));
                g2.drawString(txt, tx + 3, H/2 - 10 + 3);
                g2.setColor(new Color(255, 215, 0));
                g2.drawString(txt, tx, H/2 - 10);
                g2.setComposite(AlphaComposite.SrcOver);
            }
        }

        // FASE 3 (71–110): player muncul kembali dengan glow emas memudar
        if (revivalFrame >= 71) {
            float t = (revivalFrame - 71f) / (REVIVAL_DURATION - 71f);
            float glowA = Math.max(0f, 0.55f - t * 0.55f);

            // Ring glow emas
            int rBase = (int)(20 + t * 15);
            g2.setColor(new Color(255, 215, 60, (int)(glowA * 180)));
            g2.setStroke(new BasicStroke(12f));
            g2.drawOval(cx - rBase, cy - rBase*2/3, rBase*2, rBase*4/3);
            g2.setStroke(new BasicStroke(1f));

            // Overlay emas memudar pada player
            g2.setColor(new Color(255, 220, 80, (int)(glowA * 180)));
            g2.fillRect(px, py, PLAYER_W, PLAYER_H);
        }
    }

    public void drawStatusHUD(Graphics2D g2, boolean hasLastChance, boolean hasLifesteal,
                               int invisibleChance, int healLimit, int panelH,
                               String charKey, String mapKey) {
        int x = 12, y = panelH - 105;
        g2.setFont(new Font("Arial", Font.PLAIN, 11));
        g2.setColor(new Color(0,0,0,130));
        g2.fillRoundRect(x-4, y-14, 210, 110, 8, 8);

        g2.setFont(new Font("Arial", Font.BOLD, 11));
        g2.setColor(new Color(200,200,200));
        g2.drawString("── Status Aktif ──", x, y);
        y += 16;

        g2.setFont(new Font("Arial", Font.PLAIN, 11));
        if (hasLastChance) {
            g2.setColor(new Color(255,130,50));
            g2.drawString("🔥 Last Chance: AKTIF", x, y); y += 14;
        }
        if (hasLifesteal) {
            g2.setColor(new Color(210,80,210));
            g2.drawString("🩸 Lifesteal: AKTIF", x, y); y += 14;
        }
        if (invisibleChance > 0) {
            g2.setColor(new Color(80,215,255));
            g2.drawString("👁 Dodge: " + invisibleChance + "%", x, y); y += 14;
        }
        if (healLimit > 0) {
            g2.setColor(new Color(80,200,80));
            g2.drawString("💚 Heal sisa: " + healLimit, x, y); y += 14;
        }
        if (frozen) {
            g2.setColor(new Color(100,200,255));
            g2.drawString("❄ FROZEN! Tak bisa bergerak/attack!", x, y); y += 14;
        }
        if (revivalActive) {
            g2.setColor(new Color(255,215,0));
            g2.drawString("✦ BANGKIT... Tunggu sebentar!", x, y);
        } else {
            g2.setColor(new Color(255,215,100));
            String ability = AssetConfig.getCharacterAbility(charKey);
            if (!ability.isEmpty()) {
                g2.setFont(new Font("Arial", Font.ITALIC, 10));
                g2.drawString("✨ " + ability, x, y);
            }
        }
    }

    // ── Private draw helpers ─────────────────────────────────────────────

    private void drawHitFlash(Graphics2D g2, int x, int y, int w, int h, float alpha) {
        g2.setColor(new Color(1f, 0f, 0f, alpha));
        g2.fillRect(x, y, w, h);
        g2.setColor(new Color(1f, 0.2f, 0.2f, Math.min(alpha+0.35f, 1f)));
        g2.setStroke(new BasicStroke(5f));
        g2.drawRect(x, y, w, h);
        g2.setStroke(new BasicStroke(1f));
        for (int i = 0; i < 6; i++) {
            int px = x+(int)(Math.random()*w);
            int py = y+(int)(Math.random()*h);
            g2.setColor(new Color(1f,0.1f,0.1f,Math.min(alpha*1.5f,1f)));
            g2.fillOval(px-3,py-3,8,8);
        }
    }

    private void drawHealGlow(Graphics2D g2, int x, int y, int w, int h, float alpha) {
        g2.setColor(new Color(0f,1f,0.3f,alpha));
        g2.fillRect(x,y,w,h);
        int pulse=(int)(Math.sin(healGlowFrames*0.6)*10);
        g2.setColor(new Color(0.1f,0.9f,0.25f,alpha+0.15f));
        g2.setStroke(new BasicStroke(4.5f));
        g2.drawOval(x-12-pulse,y-12-pulse,w+24+pulse*2,h+24+pulse*2);
        g2.setColor(new Color(0.3f,1f,0.4f,alpha*0.7f));
        g2.setStroke(new BasicStroke(2.5f));
        g2.drawOval(x-4,y-4,w+8,h+8);
        g2.setStroke(new BasicStroke(1f));
        for (int i=0;i<7;i++) {
            int px=x+10+(int)(Math.random()*(w-20));
            int py=y+(int)(Math.random()*h);
            float pA=Math.min((float)(Math.random()*alpha*2),1f);
            g2.setColor(new Color(0f,1f,0.4f,pA));
            g2.fillOval(px,py,5,5);
        }
    }

    private void drawVampiricGlow(Graphics2D g2, int x, int y, int w, int h, float alpha) {
        g2.setColor(new Color(0f,0.9f,0.8f,alpha*0.6f));
        g2.fillRect(x,y,w,h);
        int pulse=(int)(Math.sin(vampiricGlowFrames*0.7)*8);
        g2.setColor(new Color(0f,0.85f,0.85f,alpha+0.2f));
        g2.setStroke(new BasicStroke(4f));
        g2.drawOval(x-10-pulse,y-10-pulse,w+20+pulse*2,h+20+pulse*2);
        g2.setStroke(new BasicStroke(1f));
        for (int i=0;i<5;i++) {
            int px=x+20+(int)(Math.random()*(w-40));
            int py=y+(int)(Math.random()*h*0.7);
            g2.setColor(new Color(0f,1f,0.9f,(float)(Math.random()*alpha*1.5)));
            g2.fillOval(px,py,4,4);
        }
    }

    private void drawFreezeOverlay(Graphics2D g2, int x, int y, int w, int h) {
        float pulse = (float)Math.abs(Math.sin(freezeFrames * 0.15));
        float alpha = 0.3f + pulse * 0.25f;
        g2.setColor(new Color(0.4f,0.7f,1f,alpha));
        g2.fillRect(x,y,w,h);
        g2.setColor(new Color(0.6f,0.9f,1f,0.8f));
        g2.setStroke(new BasicStroke(3f));
        g2.drawRect(x,y,w,h);
        g2.setStroke(new BasicStroke(1.5f));
        g2.setColor(new Color(0.8f,0.95f,1f,0.6f));
        for (int i=0;i<4;i++) {
            int cx2=x+20+i*(w/4), cy2=y+20+(int)(Math.random()*(h-40));
            g2.drawLine(cx2,cy2-8,cx2,cy2+8);
            g2.drawLine(cx2-8,cy2,cx2+8,cy2);
            g2.drawLine(cx2-5,cy2-5,cx2+5,cy2+5);
            g2.drawLine(cx2+5,cy2-5,cx2-5,cy2+5);
        }
        g2.setStroke(new BasicStroke(1f));
    }

    private void drawDeathAnimation(Graphics2D g2, int x, int y, int w, int h,
                                    int frame, boolean isEnemy) {
        float totalF = DEATH_DURATION;
        if (frame < 55) {
            float rollProgress = Math.min(frame/30f, 1f);
            float angle = rollProgress * 90f;
            float alpha = (frame > 45) ? 1f-(frame-45f)/10f : 1f;
            Graphics2D g2c=(Graphics2D)g2.create();
            g2c.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,Math.max(alpha,0f)));
            int pivotX=x+w/2, pivotY=y+h;
            g2c.rotate(Math.toRadians(angle),pivotX,pivotY);
            if (rollProgress>0.5f) {
                float shadowAlpha=(rollProgress-0.5f)*2f*0.4f;
                g2c.setColor(new Color(0f,0f,0f,shadowAlpha));
                g2c.fillOval(pivotX-w/2,pivotY-10,w,20);
            }
            g2c.dispose();
        }
        if (frame>=25&&frame<70) {
            float cp=Math.min((frame-25f)/15f,1f);
            float ca=(frame>58)?1f-(frame-58f)/12f:cp; ca=Math.max(ca,0f);
            int cx2=x+w/2, cy2=y+h/2, sz=(int)(Math.min(w,h)*0.45f*cp);
            Graphics2D g2c=(Graphics2D)g2.create();
            g2c.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,ca));
            g2c.setStroke(new BasicStroke(12f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
            g2c.setColor(new Color(0,0,0,200));
            g2c.drawLine(cx2-sz,cy2-sz,cx2+sz,cy2+sz); g2c.drawLine(cx2+sz,cy2-sz,cx2-sz,cy2+sz);
            g2c.setColor(new Color(220,30,30));
            g2c.setStroke(new BasicStroke(9f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
            g2c.drawLine(cx2-sz,cy2-sz,cx2+sz,cy2+sz); g2c.drawLine(cx2+sz,cy2-sz,cx2-sz,cy2+sz);
            g2c.dispose();
        }
        if (frame>=58) {
            float fa=1f-(frame-58f)/(totalF-58f); fa=Math.max(fa,0f);
            Graphics2D g2c=(Graphics2D)g2.create();
            g2c.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,Math.min(1f-fa,0.6f)));
            g2c.setColor(Color.WHITE); g2c.fillRect(x,y,w,h);
            g2c.dispose();
        }
    }

    private void drawWaveIntro(Graphics2D g2) {
        int W=targetPanel.getWidth(), H=targetPanel.getHeight();
        float progress=(float)waveIntroFrames/WAVE_INTRO_DURATION;
        float alpha;
        if (progress>0.8f)       alpha=(1f-progress)/0.2f;
        else if (progress>0.25f) alpha=1f;
        else                     alpha=progress/0.25f;
        alpha=Math.max(0f,Math.min(alpha,1f));
        float scale=0.85f+(1f-progress)*0.15f;

        Graphics2D g2c=(Graphics2D)g2.create();
        g2c.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,alpha*0.5f));
        g2c.setColor(Color.BLACK); g2c.fillRect(0,0,W,H);
        g2c.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,alpha));

        boolean isBoss=waveIntroText.contains("ZOMBOSS");
        Color textColor=isBoss?new Color(220,80,220):new Color(255,215,0);

        g2c.setFont(new Font("Serif",Font.BOLD,(int)(52*scale)));
        FontMetrics fm=g2c.getFontMetrics();
        int tw=fm.stringWidth(waveIntroText);
        int tx=(W-tw)/2, ty=H/2+fm.getAscent()/2;

        g2c.setColor(new Color(0,0,0,180)); g2c.drawString(waveIntroText,tx+3,ty+3);
        g2c.setColor(textColor);             g2c.drawString(waveIntroText,tx,ty);

        if (isBoss) {
            g2c.setFont(new Font("Arial",Font.BOLD,(int)(20*scale)));
            String sub="Pertarungan terakhir!";
            FontMetrics fm2=g2c.getFontMetrics();
            g2c.setColor(new Color(255,160,80));
            g2c.drawString(sub,(W-fm2.stringWidth(sub))/2,ty+40);
        }
        g2c.dispose();
    }

    private void drawPauseOverlay(Graphics2D g2) {
        int W=targetPanel.getWidth(), H=targetPanel.getHeight();
        Graphics2D g2c=(Graphics2D)g2.create();
        g2c.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.55f));
        g2c.setColor(Color.BLACK); g2c.fillRect(0,0,W,H);
        g2c.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1f));

        g2c.setFont(new Font("Serif",Font.BOLD,48));
        g2c.setColor(Color.WHITE);
        String pauseText="⏸  PAUSED";
        FontMetrics fm=g2c.getFontMetrics();
        g2c.drawString(pauseText,(W-fm.stringWidth(pauseText))/2,H/2);

        g2c.setFont(new Font("Arial",Font.PLAIN,16));
        g2c.setColor(new Color(180,180,180));
        String hint="Tekan P atau ESC untuk lanjut";
        FontMetrics fm2=g2c.getFontMetrics();
        g2c.drawString(hint,(W-fm2.stringWidth(hint))/2,H/2+36);

        g2c.setFont(new Font("Arial",Font.ITALIC,13));
        g2c.setColor(new Color(140,140,140));
        String ctrl="[A/D: gerak | SPACE: serang | H: heal | P: pause]";
        FontMetrics fm3=g2c.getFontMetrics();
        g2c.drawString(ctrl,(W-fm3.stringWidth(ctrl))/2,H/2+60);
        g2c.dispose();
    }

    private void addFloat(String text, int x, int y, Color color) {
        floatingTexts.add(new FloatingText(text, x, y, color));
    }

    // ════════════════════════════════════════════════════════════════════
    // Inner: FloatingText
    // ════════════════════════════════════════════════════════════════════

    private static class FloatingText {
        private final String text;
        private float x, y;
        private final Color color;
        private int life=50, maxLife=50;
        private final float vy=-1.6f, vx;

        FloatingText(String text, int x, int y, Color color) {
            this.text=text; this.x=x-20; this.y=y; this.color=color;
            this.vx=(float)((Math.random()-0.5)*1.4);
        }
        void tick()      { y+=vy; x+=vx; life--; }
        boolean isDead() { return life<=0; }
        void draw(Graphics2D g2) {
            float alpha=(float)life/maxLife;
            boolean isMiss=text.startsWith("MISS");
            boolean isClear=text.contains("CLEAR");
            boolean isRevive=text.contains("LAST CHANCE");
            int size = isRevive ? 22 : isMiss ? 28 : isClear ? 22 : 20;
            g2.setFont(new Font("Arial",Font.BOLD,size));
            g2.setColor(new Color(0,0,0,(int)(alpha*160)));
            g2.drawString(text,(int)x+2,(int)y+2);
            g2.setColor(new Color(color.getRed(),color.getGreen(),color.getBlue(),(int)(alpha*240)));
            g2.drawString(text,(int)x,(int)y);
        }
    }
}