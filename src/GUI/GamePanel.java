package GUI;

import CharacterSettings.*;
import CharacterSettings.Character;
import CharacterSettings.Zombie;
import CharacterSettings.Zomboss;
import Database.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.ArrayList;

/**
 * GamePanel v4 — Panel utama game.
 *
 * PERBAIKAN v4:
 *  [1] BUG FIX #5: Focus keyboard (A/D/P) — requestFocusInWindow() konsisten
 *  [2] Custom image size: gambar player/enemy pakai AssetConfig.PLAYER_RENDER_W/H
 *  [3] Unesa map opacity: drawMapOverlay menggunakan AssetConfig.getMapBgDimAlpha
 *  [4] Database: simpan hasil game saat win/game over
 *  [5] Wave progress bar di HUD
 *
 * PERBAIKAN BUG (Revisi):
 *  [FIX-1] Import CharacterSettings.Character, Zombie, Zomboss ditambahkan
 *  [FIX-2] addOverlayMouseListener() — listener disimpan sebagai field, hapus lama sebelum tambah baru
 *  [FIX-3] stopAllEffectTimers() — ditambahkan proximityTimer agar tidak bocor saat game over
 *  [FIX-4] drawScene() — guard dimensi (W==0||H==0) agar tidak crash saat panel belum siap
 *  [FIX-5] enemyTurn() — tambah guard anim.isFrozen() agar tidak double damage saat freeze
 */
public class GamePanel extends JPanel {

    // ---- Data game ----
    private Player player;
    private Character currentEnemy;          // FIX-1: pakai import eksplisit
    private int wave = 1;
    private ArrayList<String> listBuff;

    // ---- Pilihan dari PreGame ----
    private String selectedMap;
    private String selectedChar;

    // ---- Komponen GUI ----
    private JTextArea battleLog;
    private JButton   btnAttack, btnHeal;
    private JPanel    actionPanel, visualPanel;

    // ---- Assets ----
    private Image imgPlayer, imgBackground;
    private final Image[] imgZombies = new Image[10];
    private Image imgZomboss;

    // ---- Mesin animasi ----
    private AnimationEngine anim;

    // ---- Timer efek map ----
    private Timer zombosMapDotTimer;
    private Timer frozenTimer;
    private Timer zombieAdvanceTimer;

    // ---- State layar ----
    private boolean gameOver     = false;
    private boolean victory      = false;
    private int     overlayFrame = 0;
    private Timer   overlayTimer;

    // FIX-2: simpan overlay listener agar bisa dihapus sebelum tambah baru
    private MouseAdapter overlayListener;

    // ---- Proximity ----
    private int   proximityThreshold = 70;
    private Timer proximityTimer;

    // ---- Stat zombie ----
    private int baseEnemyDamage = 0;
    private int enemyMaxHpCache = 0;

    // ====================================================================
    // CONSTRUCTOR
    // ====================================================================
    public GamePanel(String playerName, String selectedMap, String selectedChar) {
        this.selectedMap  = selectedMap;
        this.selectedChar = selectedChar;
        this.setLayout(new BorderLayout());

        // 1. BattleLog
        battleLog = new JTextArea();
        battleLog.setEditable(false);
        battleLog.setFont(new Font("Monospaced", Font.PLAIN, 13));
        battleLog.setBackground(new Color(15, 15, 20));
        battleLog.setForeground(new Color(200, 220, 200));
        battleLog.setCaretColor(Color.WHITE);
        JScrollPane scrollLog = new JScrollPane(battleLog);
        scrollLog.setPreferredSize(new Dimension(800, 120));
        scrollLog.setBorder(BorderFactory.createLineBorder(new Color(60, 80, 60), 1));

        // 2. Init player
        initPlayer(playerName);
        initBuffs();
        spawnEnemyForWave(1);
        loadAssets();

        // 3. Visual canvas
        visualPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawScene((Graphics2D) g);
            }
        };
        visualPanel.setPreferredSize(new Dimension(800, 420));
        visualPanel.setBackground(Color.BLACK);

        // Focus policy
        visualPanel.setFocusable(true);
        visualPanel.setRequestFocusEnabled(true);

        // 4. AnimationEngine
        anim = new AnimationEngine(visualPanel);
        anim.setMovementEnabled(true);
        anim.setIsZomboss(false);

        // 5. Tombol
        btnAttack = createVisualButton("⚔ SERANG", "ATK: " + player.getDamage(),
                                       new Color(180,40,40), new Color(220,80,60));
        btnHeal   = createVisualButton("💚 HEAL", "Sisa: " + player.getHealLimit(),
                                       new Color(30,120,50), new Color(60,180,80));

        anim.registerButtons(btnAttack, btnHeal);

        btnAttack.addActionListener(e -> {
            playerAction(1);
            SwingUtilities.invokeLater(() -> visualPanel.requestFocusInWindow());
        });
        btnHeal.addActionListener(e -> {
            playerAction(2);
            SwingUtilities.invokeLater(() -> visualPanel.requestFocusInWindow());
        });

        // 6. Panel tombol
        JButton btnMute = createMuteButton();
        actionPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(new Color(18,18,28));
                g.fillRect(0,0,getWidth(),getHeight());
            }
        };
        actionPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 16, 8));
        actionPanel.add(btnAttack);
        actionPanel.add(btnHeal);
        actionPanel.add(btnMute);
        actionPanel.setPreferredSize(new Dimension(800, 62));

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(scrollLog, BorderLayout.CENTER);
        bottomPanel.add(actionPanel, BorderLayout.SOUTH);

        this.add(visualPanel, BorderLayout.CENTER);
        this.add(bottomPanel, BorderLayout.SOUTH);

        // Klik mana saja → kembalikan focus ke visualPanel
        MouseAdapter focusGrabber = new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                visualPanel.requestFocusInWindow();
            }
        };
        this.addMouseListener(focusGrabber);
        visualPanel.addMouseListener(focusGrabber);

        // 7. Proximity
        proximityThreshold = AssetConfig.getProximityThreshold(selectedMap);
        proximityTimer = new Timer(800, e -> checkProximityDanger());
        proximityTimer.start();

        // 8. Mulai
        log("=== LAST CHANCE FOR LIFE ===");
        log("Karakter: " + AssetConfig.getCharacterName(selectedChar)
            + " | Map: " + AssetConfig.getMapName(selectedMap));
        logCharacterAbility();
        logMapEffect();
        startWave(1, true);

        SwingUtilities.invokeLater(() -> {
            visualPanel.requestFocusInWindow();
            log("[INFO] Tekan A/D untuk bergerak, P/ESC untuk pause.");
        });
    }

    // ====================================================================
    // INISIALISASI PLAYER
    // ====================================================================
    private void initPlayer(String playerName) {
        int[] stats = AssetConfig.getCharacterBaseStats(selectedChar);
        int maxHp = stats[0];
        int atk   = stats[1];
        int heal  = stats[2];

        if ("KNIGHT_PRINCE".equals(selectedChar)) atk = (int)(atk * 1.05f);
        float atkMult = AssetConfig.getPlayerAtkMultiplier(selectedMap, selectedChar);
        if (atkMult > 1f) atk = (int)(atk * atkMult);

        player = new Player(playerName, maxHp, atk);
        player.setHealLimit(heal);

        if ("RAYMOND".equals(selectedChar)) player.addInvisible(30);
    }

    private void logCharacterAbility() {
        String ability = AssetConfig.getCharacterAbility(selectedChar);
        if (!ability.isEmpty()) log("[✨ Ability] " + AssetConfig.getCharacterName(selectedChar) + ": " + ability);
    }

    private void logMapEffect() {
        String[] desc = AssetConfig.getMapDesc(selectedMap);
        log("[🗺 Map Effect] " + AssetConfig.getMapName(selectedMap) + ": " + desc[0]);
    }

    // ====================================================================
    // TOMBOL
    // ====================================================================
    private JButton createVisualButton(String mainText, String subText,
                                       Color colorFrom, Color colorTo) {
        JButton btn = new JButton() {
            private boolean hov = false;
            { addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e){ hov=true; repaint(); }
                public void mouseExited(MouseEvent e) { hov=false; repaint(); }
            }); }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                int w=getWidth(),h=getHeight();
                Color c1=isEnabled()?(hov?colorFrom.brighter():colorFrom):new Color(55,55,55);
                Color c2=isEnabled()?(hov?colorTo.brighter():colorTo):new Color(75,75,75);
                g2.setPaint(new GradientPaint(0,0,c1,0,h,c2));
                g2.fillRoundRect(0,0,w,h,14,14);
                if(hov&&isEnabled()){
                    g2.setColor(new Color(255,255,255,160));
                    g2.setStroke(new BasicStroke(2f));
                    g2.drawRoundRect(1,1,w-3,h-3,14,14);
                }
                g2.setFont(new Font("Serif",Font.BOLD,16));
                g2.setColor(isEnabled()?Color.WHITE:new Color(130,130,130));
                FontMetrics fm1=g2.getFontMetrics();
                g2.drawString(mainText,(w-fm1.stringWidth(mainText))/2,h/2-2);
                g2.setFont(new Font("Arial",Font.PLAIN,11));
                g2.setColor(isEnabled()?new Color(215,215,215):new Color(110,110,110));
                String sub=(String)getClientProperty("subText");
                if(sub==null) sub=subText;
                FontMetrics fm2=g2.getFontMetrics();
                g2.drawString(sub,(w-fm2.stringWidth(sub))/2,h/2+14);
                g2.dispose();
            }
            @Override public Dimension getPreferredSize(){return new Dimension(200,50);}
        };
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setFocusable(false);
        btn.putClientProperty("subText", subText);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton createMuteButton() {
        JButton btn = new JButton() {
            private boolean hov=false;
            { addMouseListener(new MouseAdapter(){
                public void mouseEntered(MouseEvent e){hov=true; repaint();}
                public void mouseExited(MouseEvent e){hov=false; repaint();}
            }); }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                int w=getWidth(),h=getHeight();
                g2.setColor(hov?new Color(70,70,90):new Color(45,45,65));
                g2.fillRoundRect(0,0,w,h,10,10);
                g2.setFont(new Font("Segoe UI Emoji",Font.PLAIN,18));
                String icon=SoundManager.isMuted()?"🔇":"🔊";
                FontMetrics fm=g2.getFontMetrics();
                g2.setColor(Color.WHITE);
                g2.drawString(icon,(w-fm.stringWidth(icon))/2,h/2+fm.getAscent()/2-3);
                g2.dispose();
            }
            @Override public Dimension getPreferredSize(){return new Dimension(50,50);}
        };
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setFocusable(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> {
            SoundManager.toggleMute();
            btn.repaint();
            log(SoundManager.isMuted()?"[🔇 Suara dimatikan]":"[🔊 Suara dinyalakan]");
            SwingUtilities.invokeLater(() -> visualPanel.requestFocusInWindow());
        });
        return btn;
    }

    private void updateButtonSub(JButton btn, String sub) {
        btn.putClientProperty("subText", sub);
        btn.repaint();
    }

    // ====================================================================
    // ASSETS
    // ====================================================================
    private void loadAssets() {
        imgPlayer  = loadImage(AssetConfig.getCharacterPath(selectedChar));
        imgZomboss = loadImage(AssetConfig.ZOMBOSS);
        for (int i=0; i<9; i++) imgZombies[i] = loadImage(AssetConfig.getZombieForWave(i+1));
        loadBackground(AssetConfig.getBgPath(selectedMap));
    }

    private Image loadImage(String path) {
        try {
            java.net.URL url = getClass().getResource(path);
            if (url != null) return new ImageIcon(url).getImage();
        } catch (Exception ignored) {}
        return null;
    }

    private void loadBackground(String path) { imgBackground = loadImage(path); }

    private Image getEnemyImage() {
        if (wave == 10) return imgZomboss;
        return imgZombies[Math.min(wave-1, 8)];
    }

    // ====================================================================
    // DRAW SCENE
    // ====================================================================
    private void drawScene(Graphics2D g2) {
        // FIX-4: Guard dimensi — keluar jika panel belum siap
        int W = visualPanel.getWidth();
        int H = visualPanel.getHeight();
        if (W == 0 || H == 0) return;

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int sx = anim.getShakeOffsetX(), sy = anim.getShakeOffsetY();
        g2.translate(sx, sy);

        // Background
        if (imgBackground != null) g2.drawImage(imgBackground, 0, 0, W, H, this);
        else drawFallbackBg(g2, W, H);

        // Map overlay dengan opacity per-map
        drawMapOverlay(g2, W, H);

        // Player
        int px  = anim.getPlayerRenderX();
        int py  = anim.getPlayerY();
        int prW = AssetConfig.PLAYER_RENDER_W;
        int prH = AssetConfig.PLAYER_RENDER_H;
        if (imgPlayer != null)
            g2.drawImage(imgPlayer, px, py, prW, prH, this);
        else {
            g2.setColor(new Color(60,100,200));
            g2.fillRoundRect(px+30, py, 140, prH, 20, 20);
        }

        // Enemy
        int ex  = anim.getEnemyRenderX();
        int ey  = anim.getEnemyRenderY() + AssetConfig.ENEMY_RENDER_OFFSET_Y;
        int erW = AssetConfig.ENEMY_RENDER_W;
        int erH = AssetConfig.ENEMY_RENDER_H;
        Image enemyImg = getEnemyImage();
        if (enemyImg != null) {
            if ("UNESA".equals(selectedMap)) {
                drawEnemyWithContrast(g2, enemyImg, ex, ey, erW, erH);
            } else {
                g2.drawImage(enemyImg, ex, ey, erW, erH, this);
            }
        } else {
            Color ec = (wave==10)?new Color(180,0,180):new Color(180,40,40);
            g2.setColor(ec);
            g2.fillRoundRect(ex+10, ey, erW-20, erH, 20, 20);
        }

        // Efek animasi
        anim.drawEffects(g2);

        // HP bar — posisi logis (tidak bergerak saat animasi)
        int logicalPx = anim.getPlayerX();
        int logicalEx = anim.getEnemyCurrentX();

        drawHpBar(g2, logicalPx, anim.getPlayerY()-28,
                  prW, player.getHp(), player.getMaxHp(), player.getName());
        drawHpBar(g2, logicalEx, AnimationEngine.ENEMY_BASE_Y-28,
                  erW, currentEnemy.getHp(), enemyMaxHpCache, currentEnemy.getName());

        // Proximity warning
        drawProximityWarning(g2, logicalPx, logicalEx);

        // HUD
        drawHUD(g2, W, H);

        if (gameOver) drawGameOverOverlay(g2, W, H);
        if (victory)  drawVictoryOverlay(g2, W, H);

        g2.translate(-sx, -sy);
    }

    private void drawEnemyWithContrast(Graphics2D g2, Image img, int x, int y, int w, int h) {
        g2.setColor(new Color(0, 0, 0, 90));
        g2.fillRoundRect(x + 4, y + 6, w, h, 12, 12);
        g2.drawImage(img, x, y, w, h, this);
    }

    private void drawMapOverlay(Graphics2D g2, int W, int H) {
        int dimAlpha = AssetConfig.getMapBgDimAlpha(selectedMap);
        if (dimAlpha > 0) {
            g2.setColor(new Color(0, 0, 0, dimAlpha));
            g2.fillRect(0, 0, W, H);
        }

        java.awt.Color tint = AssetConfig.getMapBgTint(selectedMap);
        if (tint != null) {
            g2.setColor(tint);
            g2.fillRect(0, 0, W, H);
        }

        if ("ZOMBOSS_MAP".equals(selectedMap)) {
            g2.setColor(new Color(80,0,0,40));
            g2.fillRect(0,0,W,H);
        }
        if ("FROZEN".equals(selectedMap)) {
            g2.setColor(new Color(0,50,120,25));
            g2.fillRect(0,0,W,H);
        }
        if ("MOUNTAIN".equals(selectedMap)) {
            g2.setColor(new Color(100,40,0,20));
            g2.fillRect(0,0,W,H);
        }
        if (wave==10 && !"ZOMBOSS_MAP".equals(selectedMap)) {
            g2.setColor(new Color(80,0,0,35));
            g2.fillRect(0,0,W,H);
        }
    }

    private void drawFallbackBg(Graphics2D g2, int W, int H) {
        g2.setPaint(new GradientPaint(0,0,new Color(20,20,35),0,H,new Color(10,30,10)));
        g2.fillRect(0,0,W,H);
    }

    private void drawProximityWarning(Graphics2D g2, int px, int ex) {
        int dist = ex - (px + AssetConfig.PLAYER_RENDER_W);
        if (dist < proximityThreshold && dist > -10 && !gameOver && !victory) {
            float alpha=0.4f+(float)Math.sin(System.currentTimeMillis()/200.0)*0.3f;
            alpha=Math.max(0.1f,Math.min(alpha,0.7f));
            g2.setColor(new Color(1f,0.1f,0.1f,alpha));
            g2.setStroke(new BasicStroke(3f));
            g2.drawLine(px+AssetConfig.PLAYER_RENDER_W,0,px+AssetConfig.PLAYER_RENDER_W,visualPanel.getHeight());
            g2.setFont(new Font("Arial",Font.BOLD,11));
            g2.setColor(new Color(255,80,80,(int)(alpha*255)));
            g2.drawString("⚠ BAHAYA!",px+AssetConfig.PLAYER_RENDER_W/2,AnimationEngine.PLAYER_BASE_Y-40);
        }
    }

    private void drawHpBar(Graphics2D g2, int x, int y, int bw, int cur, int max, String name) {
        int safe=Math.max(cur,0);
        float pct=(float)safe/Math.max(max,1);
        int filled=(int)(pct*bw);

        g2.setColor(new Color(30,30,30,210));
        g2.fillRoundRect(x,y,bw,15,8,8);

        Color barColor=pct>0.6f?new Color(60,200,60):pct>0.3f?new Color(230,200,40):new Color(220,50,50);
        if (filled>0) {
            g2.setPaint(new GradientPaint(x,y,barColor.brighter(),x,y+15,barColor.darker()));
            g2.fillRoundRect(x,y,filled,15,8,8);
        }
        g2.setColor(new Color(180,180,180,100));
        g2.setStroke(new BasicStroke(1.2f));
        g2.drawRoundRect(x,y,bw,15,8,8);

        String hpText=name+"  "+safe+"/"+max;
        g2.setFont(new Font("Arial",Font.BOLD,11));
        g2.setColor(new Color(0,0,0,160)); g2.drawString(hpText,x+6,y+11);
        g2.setColor(Color.WHITE);          g2.drawString(hpText,x+5,y+10);
    }

    private void drawHUD(Graphics2D g2, int W, int H) {
        String waveText=(wave==10)?"── ⚠ ZOMBOSS ⚠ ──":"── WAVE "+wave+" / 10 ──";
        Color waveColor=(wave==10)?new Color(220,80,220):new Color(255,215,0);
        g2.setFont(new Font("Serif",Font.BOLD,20));
        FontMetrics fm=g2.getFontMetrics();
        int wx=(W-fm.stringWidth(waveText))/2;
        g2.setColor(new Color(0,0,0,160)); g2.drawString(waveText,wx+2,32);
        g2.setColor(waveColor);            g2.drawString(waveText,wx,30);

        drawWaveProgressBar(g2, W);

        g2.setFont(new Font("Arial",Font.PLAIN,12));
        g2.setColor(new Color(120,220,255));
        g2.drawString("ATK: "+player.getDamage(),82,
                      AnimationEngine.PLAYER_BASE_Y+AssetConfig.PLAYER_RENDER_H+17);

        drawMapEffectHUD(g2, W);

        g2.setFont(new Font("Arial",Font.ITALIC,11));
        g2.setColor(new Color(200,200,200,110));
        g2.drawString("[A/D: gerak | P: pause | Dekati zombie = danger!]",W/2-140,H-8);

        anim.drawStatusHUD(g2, player.hasLastChance(), player.isLifesteal(),
            player.getInvisibleChance(), player.getHealLimit(), H, selectedChar, selectedMap);

        if (!anim.hasMoved()) {
            g2.setFont(new Font("Arial",Font.BOLD,12));
            g2.setColor(new Color(255,220,80,200));
            String hint = "⬅ Klik area game lalu tekan A/D untuk bergerak ➡";
            FontMetrics fmH = g2.getFontMetrics();
            g2.drawString(hint, (W-fmH.stringWidth(hint))/2, H-24);
        }
    }

    private void drawWaveProgressBar(Graphics2D g2, int W) {
        int barW = 200, barH = 8;
        int barX = (W - barW) / 2;
        int barY = 38;
        float pct = (float) wave / 10f;

        g2.setColor(new Color(30,30,30,180));
        g2.fillRoundRect(barX, barY, barW, barH, 4, 4);

        Color barColor = (wave == 10) ? new Color(220,80,220) : new Color(255,215,0);
        g2.setPaint(new GradientPaint(barX, barY, barColor, barX, barY+barH, barColor.darker()));
        g2.fillRoundRect(barX, barY, (int)(barW * pct), barH, 4, 4);

        g2.setColor(new Color(180,180,180,80));
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(barX, barY, barW, barH, 4, 4);
    }

    private void drawMapEffectHUD(Graphics2D g2, int W) {
        String effectText=null; Color effectColor=Color.WHITE;
        switch (selectedMap) {
            case "ZOMBOSS_MAP":
                effectText="🔥 -5% HP/3s | Zombie ATK +50%"; effectColor=new Color(255,80,80); break;
            case "UNESA":
                float zm=AssetConfig.getZombieAtkMultiplier("UNESA",wave)-1f;
                effectText=String.format("⚠ Zombie ATK +%.0f%%",zm*100); effectColor=new Color(255,180,50);
                if ("UNESA_BOYS".equals(selectedChar)||"UNESA_GIRLS".equals(selectedChar))
                    effectText+=" | 💪 Unesa ATK +10%";
                break;
            case "FROZEN":
                effectText="❄ Freeze periodik! Attack/Heal diblokir"; effectColor=new Color(100,200,255); break;
            case "MOUNTAIN":
                effectText="⛰ Zombie makin cepat menyerang!"; effectColor=new Color(255,150,80); break;
        }
        if (wave==10 && !"ZOMBOSS_MAP".equals(selectedMap)) {
            effectText="🔥 Lingkungan Zomboss: -5%HP/3s"; effectColor=new Color(255,80,80);
        }
        if (effectText!=null) {
            g2.setFont(new Font("Arial",Font.BOLD,11));
            FontMetrics fm=g2.getFontMetrics();
            int tw=fm.stringWidth(effectText);
            g2.setColor(new Color(0,0,0,130));
            g2.fillRoundRect(W-tw-18,10,tw+14,20,6,6);
            g2.setColor(effectColor);
            g2.drawString(effectText,W-tw-12,25);
        }
    }

    // ====================================================================
    // GAME OVER / VICTORY
    // ====================================================================
    private void drawGameOverOverlay(Graphics2D g2, int W, int H) {
        float fade=Math.min(overlayFrame/40f,1f);
        g2.setColor(new Color(0,0,0,(int)(200*fade)));
        g2.fillRect(0,0,W,H);
        if (overlayFrame<20) return;

        float textFade=Math.min((overlayFrame-20)/30f,1f);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,textFade));
        g2.setFont(new Font("Serif",Font.BOLD,76));
        FontMetrics fm=g2.getFontMetrics();
        String goText="GAME OVER";
        int tx=(W-fm.stringWidth(goText))/2;
        g2.setColor(new Color(0,0,0,200)); g2.drawString(goText,tx+4,H/2-36);
        g2.setColor(new Color(220,40,40));  g2.drawString(goText,tx,H/2-40);

        if (overlayFrame>50) {
            float btnFade=Math.min((overlayFrame-50)/25f,1f);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,btnFade));
            g2.setFont(new Font("Arial",Font.PLAIN,16));
            g2.setColor(new Color(200,180,180));
            String sub="Dikalahkan di Wave "+wave;
            FontMetrics fm2=g2.getFontMetrics();
            g2.drawString(sub,(W-fm2.stringWidth(sub))/2,H/2+10);
            drawOverlayButton(g2,W/2-130,H/2+45,120,38,"🔄 Ulang",new Color(50,130,50),"RETRY");
            drawOverlayButton(g2,W/2+10,H/2+45,120,38,"🚪 Keluar",new Color(130,50,50),"QUIT");
        }
        g2.setComposite(AlphaComposite.SrcOver);
    }

    private void drawVictoryOverlay(Graphics2D g2, int W, int H) {
        float fade=Math.min(overlayFrame/40f,1f);
        g2.setColor(new Color(0,30,0,(int)(200*fade)));
        g2.fillRect(0,0,W,H);
        if (overlayFrame<20) return;

        float textFade=Math.min((overlayFrame-20)/30f,1f);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,textFade));
        g2.setFont(new Font("Serif",Font.BOLD,58));
        FontMetrics fm=g2.getFontMetrics();
        String vText="🏆 VICTORY! 🏆";
        int tx=(W-fm.stringWidth(vText))/2;
        g2.setColor(new Color(0,0,0,180)); g2.drawString(vText,tx+3,H/2-35);
        g2.setColor(new Color(255,215,0)); g2.drawString(vText,tx,H/2-38);

        if (overlayFrame>50) {
            float btnFade=Math.min((overlayFrame-50)/25f,1f);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,btnFade));
            drawOverlayButton(g2,W/2-130,H/2+40,120,38,"🔄 Main Lagi",new Color(50,100,180),"RETRY");
            drawOverlayButton(g2,W/2+10,H/2+40,120,38,"🚪 Keluar",new Color(100,60,30),"QUIT");
        }
        g2.setComposite(AlphaComposite.SrcOver);
    }

    private void drawOverlayButton(Graphics2D g2,int x,int y,int w,int h,String label,Color col,String key){
        g2.setPaint(new GradientPaint(x,y,col.brighter(),x,y+h,col.darker()));
        g2.fillRoundRect(x,y,w,h,10,10);
        g2.setColor(new Color(255,255,255,120)); g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(x,y,w,h,10,10);
        g2.setFont(new Font("Serif",Font.BOLD,14)); g2.setColor(Color.WHITE);
        FontMetrics fm=g2.getFontMetrics();
        g2.drawString(label,(x+(w-fm.stringWidth(label))/2),(y+h/2+fm.getAscent()/2-2));
    }

    // FIX-2: Simpan listener sebagai field, hapus yang lama sebelum tambah baru
    private void addOverlayMouseListener() {
        if (overlayListener != null) {
            visualPanel.removeMouseListener(overlayListener);
        }
        overlayListener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!gameOver && !victory) return;
                int W=visualPanel.getWidth(), H=visualPanel.getHeight();
                Rectangle retryRect=new Rectangle(W/2-130,H/2+40,120,38);
                Rectangle quitRect =new Rectangle(W/2+10, H/2+40,120,38);
                if (retryRect.contains(e.getPoint())) restartGame();
                else if (quitRect.contains(e.getPoint())) System.exit(0);
            }
        };
        visualPanel.addMouseListener(overlayListener);
    }

    private void restartGame() {
        stopAllTimers();
        SoundManager.stopBGM();
        Window win=SwingUtilities.getWindowAncestor(this);
        win.dispose();
        SwingUtilities.invokeLater(() -> {
            SessionManager.getInstance().refreshProgress();
            new PreGameScreen().setVisible(true);
        });
    }

    private void stopAllTimers() {
        if (overlayTimer      != null) overlayTimer.stop();
        if (zombosMapDotTimer != null) zombosMapDotTimer.stop();
        if (frozenTimer       != null) frozenTimer.stop();
        if (zombieAdvanceTimer!= null) zombieAdvanceTimer.stop();
        if (proximityTimer    != null) proximityTimer.stop();
        if (anim              != null) anim.stopTimer();
    }

    // ====================================================================
    // PROXIMITY DANGER
    // ====================================================================
    private void checkProximityDanger() {
        if (gameOver||victory||anim.isPaused()) return;
        if (player.getHp()<=0||currentEnemy.getHp()<=0) return;
        if (anim.isInEntrance()) return;

        int dist=anim.getPlayerEnemyDistance();
        if (dist<proximityThreshold && dist>-20) {
            int dmg=Math.max(1, currentEnemy.getDamage()/3);
            if (Math.random()<0.45) {
                anim.playZombieAttack();
                applyDamageToPlayer(dmg,"[Proximity] Terlalu dekat! "+
                    currentEnemy.getName()+" menyerang! -"+dmg+" HP");
            }
        }
    }

    // ====================================================================
    // LOGIKA GILIRAN
    // ====================================================================
    private void playerAction(int actionType) {
        if (gameOver||victory) return;
        if (player.getHp()<=0||currentEnemy.getHp()<=0) return;
        if (anim.isInEntrance()||anim.isPaused()) return;
        if (anim.isFrozen()) { log("[❄] Kamu sedang beku! Tidak bisa bertindak!"); return; }

        if (actionType==1) {
            if (anim.isAttacking()) return;
            final int dmg=player.getDamage();
            anim.playAttack(() -> {
                player.attack(currentEnemy);
                anim.playEnemyHit(dmg);
                log("Kamu menyerang "+currentEnemy.getName()+" sebesar "+dmg+" damage!");

                if (player.isLifesteal()) {
                    int steal=Math.max(1,(int)(player.getMaxHp()*0.10f));
                    player.setHp(Math.min(player.getHp()+steal, player.getMaxHp()));
                    anim.playHeal(steal);
                    log("[Lifesteal] Menyerap "+steal+" HP (10% MaxHP)!");
                }
                if ("XAVIER".equals(selectedChar)) {
                    int drain=Math.max(1,(int)(dmg*0.10f));
                    player.setHp(Math.min(player.getHp()+drain, player.getMaxHp()));
                    anim.playVampiricDrain(drain);
                    log("[Xavier] Vampiric drain +"+drain+" HP!");
                }

                if (currentEnemy.getHp()<=0) {
                    log(currentEnemy.getName()+" TELAH DIKALAHKAN!");
                    anim.playWaveClear(wave);
                    stopZombieAdvance();
                    anim.playDeath(false,() -> {
                        Timer t=new Timer(400,ev->nextWave());
                        t.setRepeats(false); t.start();
                    });
                    return;
                }
                Timer te=new Timer(320,ev->enemyTurn());
                te.setRepeats(false); te.start();
            });

        } else {
            if (player.getHealLimit()<=0) { log("Heal sudah habis!"); return; }
            int hpBefore=player.getHp();
            player.heal();
            int gained=player.getHp()-hpBefore;
            if (gained>0) {
                anim.playHeal(gained);
                log("Healing! (+"+gained+" HP)  HP: "+player.getHp()+"/"+player.getMaxHp());
            }
            updateButtonSub(btnHeal,"Sisa: "+player.getHealLimit());
            if (player.getHealLimit()<=0) btnHeal.setEnabled(false);
        }
    }

    private void enemyTurn() {
        // FIX-5: Jangan serang jika player sedang freeze
        if (anim.isFrozen()) return;
        if (currentEnemy.getHp()<=0||player.getHp()<=0) return;
        anim.playZombieAttack();
        if (player.tryDodge()) {
            anim.playMiss();
            log(currentEnemy.getName()+" menyerang... tapi MELESET! (Dodge)");
            return;
        }
        int dmg=getEnemyEffectiveDamage();
        player.setHp(Math.max(0,player.getHp()-dmg));
        anim.playPlayerHit(dmg);
        log(currentEnemy.getName()+" menyerang balik! -"+dmg+" HP");
        if (wave==10) anim.playScreenShake(8);
        checkPlayerDeath();
    }

    private int getEnemyEffectiveDamage() {
        float mult=AssetConfig.getZombieAtkMultiplier(selectedMap, wave);
        if (wave==10) mult=Math.max(mult,1.5f);
        return Math.max(1,(int)(baseEnemyDamage*mult));
    }

    private void applyDamageToPlayer(int dmg, String message) {
        player.setHp(Math.max(0,player.getHp()-dmg));
        anim.playPlayerHit(dmg);
        log(message);
        checkPlayerDeath();
    }

    private void checkPlayerDeath() {
        if (player.getHp()<=0) {
            if (player.hasLastChance()) {
                player.setLastChance(false);
                int revive=(int)(player.getMaxHp()*1f);
                player.setHp(revive);
                anim.playHeal(revive);
                log("[!!!] LAST CHANCE AKTIF! Bangkit dengan "+revive+" HP (100% MaxHP)!");
                SoundManager.play(SoundManager.LAST_CHANCE);
            } else {
                btnAttack.setEnabled(false);
                btnHeal.setEnabled(false);
                // FIX-3: stopAllEffectTimers sekarang juga menghentikan proximityTimer
                stopAllEffectTimers();
                anim.playDeath(true,() -> {
                    log("=== GAME OVER === Dikalahkan di Wave "+wave+"!");
                    SoundManager.play(SoundManager.GAME_OVER);
                    SoundManager.stopBGM();
                    saveProgress(false);
                    startGameOverOverlay();
                });
            }
        }
    }

    // FIX-3: Tambahkan proximityTimer ke dalam stopAllEffectTimers()
    private void stopAllEffectTimers() {
        if (zombosMapDotTimer != null) zombosMapDotTimer.stop();
        if (frozenTimer       != null) frozenTimer.stop();
        if (zombieAdvanceTimer!= null) zombieAdvanceTimer.stop();
        if (proximityTimer    != null) proximityTimer.stop(); // ← FIX: tambah ini
    }

    // ====================================================================
    // DATABASE — Simpan progress
    // ====================================================================
    private void saveProgress(boolean won) {
        SessionManager session = SessionManager.getInstance();
        if (!session.isLoggedIn()) return;
        DatabaseManager.getInstance().saveGameResult(
            session.getUserId(), this.selectedMap, this.wave, won
        );
        session.refreshProgress();
        System.out.println("[DB] Progress disimpan: map=" + selectedMap
            + " wave=" + wave + " won=" + won);
    }

    // ====================================================================
    // OVERLAY ANIMASI
    // ====================================================================
    private void startGameOverOverlay() {
        gameOver=true; overlayFrame=0;
        overlayTimer=new Timer(16,e->{overlayFrame++; visualPanel.repaint();});
        overlayTimer.start();
        addOverlayMouseListener();
    }

    private void startVictoryOverlay() {
        victory=true; overlayFrame=0;
        overlayTimer=new Timer(16,e->{overlayFrame++; visualPanel.repaint();});
        overlayTimer.start();
        addOverlayMouseListener();
    }

    // ====================================================================
    // WAVE MANAGEMENT
    // ====================================================================
    private void startWave(int w, boolean firstWave) {
        this.wave=w;
        anim.setIsZomboss(w==10);

        if (w==10) loadBackground(AssetConfig.getBgForWave10());
        else       loadBackground(AssetConfig.getBgPath(selectedMap));

        if (w==10) SoundManager.playBGM(SoundManager.BGM_BOSS);
        else if (firstWave||w==1) SoundManager.playBGM(AssetConfig.getBgmForMap(selectedMap));

        anim.playWaveIntro(w);
        stopAllEffectTimers();
        startMapEffects(w);

        Timer delay=new Timer(800,e->{
            log("─── WAVE "+w+(w==10?" — ⚠ ZOMBOSS!":"")+
                " | Enemy HP:"+enemyMaxHpCache+" ATK:"+baseEnemyDamage+" ───");
            anim.playEnemyEntrance(()->{
                log(currentEnemy.getName()+" muncul! (HP:"+currentEnemy.getHp()+
                    " | ATK efektif:"+getEnemyEffectiveDamage()+")");
                anim.setMovementEnabled(true);
                startZombieAdvance(w);
                SwingUtilities.invokeLater(() -> visualPanel.requestFocusInWindow());
            });
        });
        delay.setRepeats(false); delay.start();
    }

    private void startMapEffects(int w) {
        if ("ZOMBOSS_MAP".equals(selectedMap)||w==10) startZombosMapDot();
        if ("FROZEN".equals(selectedMap)) startFreezeTimer(w);
    }

    private void startZombosMapDot() {
        zombosMapDotTimer=new Timer(AssetConfig.ZOMBOSS_MAP_DOT_INTERVAL,e->{
            if (gameOver||victory||anim.isPaused()) return;
            if (player.getHp()<=0) {zombosMapDotTimer.stop(); return;}
            int dot=Math.max(1,(int)(player.getMaxHp()*AssetConfig.ZOMBOSS_MAP_DOT_PERCENT));
            player.setHp(Math.max(0,player.getHp()-dot));
            anim.playPlayerHit(dot);
            log("[🔥 Lingkungan] Neraka mengikis -"+dot+" HP!");
            checkPlayerDeath();
        });
        zombosMapDotTimer.start();
        log("[⚠] Lingkungan berbahaya! -"+(int)(AssetConfig.ZOMBOSS_MAP_DOT_PERCENT*100)+"% HP tiap 3 detik!");
    }

    private void startFreezeTimer(int w) {
        int interval=AssetConfig.getFreezeInterval(w);
        frozenTimer=new Timer(interval,e->{
            if (gameOver||victory||anim.isPaused()) return;
            if (player.getHp()<=0) {frozenTimer.stop(); return;}
            anim.playFreeze();
            log("[❄ FROZEN] Kamu membeku selama "+(AssetConfig.FREEZE_DURATION_MS/1000)+
                " detik! Tidak bisa attack/heal! Zombie menyerang bebas!");
            int atk=getEnemyEffectiveDamage();
            anim.playZombieAttack();
            applyDamageToPlayer(atk,"[❄] "+currentEnemy.getName()+" menyerang saat kamu beku! -"+atk+" HP");
            Timer unfreezeTimer=new Timer(AssetConfig.FREEZE_DURATION_MS,ev->{
                anim.unfreeze();
                log("[❄] Efek beku berakhir. Kamu bisa bergerak kembali!");
                SwingUtilities.invokeLater(() -> visualPanel.requestFocusInWindow());
                ((Timer)ev.getSource()).stop();
            });
            unfreezeTimer.setRepeats(false);
            unfreezeTimer.start();
        });
        frozenTimer.start();
        log("[❄] Map Tundra: Freeze periodik tiap ~"+interval/1000+" detik aktif!");
    }

    private void startZombieAdvance(int w) {
        stopZombieAdvance();
        int interval="MOUNTAIN".equals(selectedMap)
            ? AssetConfig.getMountainAttackInterval(w)
            : AssetConfig.getDefaultAttackInterval(w);

        zombieAdvanceTimer=new Timer(interval,e->{
            if (gameOver||victory||anim.isPaused()) return;
            if (currentEnemy.getHp()<=0||player.getHp()<=0){zombieAdvanceTimer.stop(); return;}
            if (anim.isFrozen()) return;

            anim.playZombieAttack();

            int dist=anim.getPlayerEnemyDistance();
            if (dist<250) {
                if (!player.tryDodge()) {
                    int dmg=getEnemyEffectiveDamage();
                    applyDamageToPlayer(dmg,"[⏰ Zombie] "+currentEnemy.getName()+
                        " menyerang otomatis! -"+dmg+" HP");
                    if (wave==10) anim.playScreenShake(6);
                } else {
                    anim.playMiss();
                    log("[⏰ Zombie] "+currentEnemy.getName()+" menyerang... MELESET! (Dodge)");
                }
            } else {
                log("[⏰ Zombie] "+currentEnemy.getName()+" bergerak mendekat...");
            }
        });
        zombieAdvanceTimer.start();
        log("[⏰] Zombie menyerang tiap "+interval/1000+"s! (Wave "+w+
            ("MOUNTAIN".equals(selectedMap)?" — Mountain Mode!":"")+ ")");
    }

    private void stopZombieAdvance() {
        if (zombieAdvanceTimer!=null) { zombieAdvanceTimer.stop(); zombieAdvanceTimer=null; }
    }

    private void nextWave() {
        int nextWave=wave+1;
        if (nextWave>10) {
            log(""); log("🏆 === VICTORY! HALAMANMU TELAH TERSELAMATKAN! === 🏆");
            SoundManager.play(SoundManager.VICTORY);
            SoundManager.stopBGM();
            btnAttack.setEnabled(false);
            btnHeal.setEnabled(false);
            saveProgress(true);
            startVictoryOverlay();
            return;
        }
        log("");
        if (nextWave>=2 && nextWave<=9) showBuffSelection();
        spawnEnemyForWave(nextWave);
        startWave(nextWave, false);
        SwingUtilities.invokeLater(() -> visualPanel.requestFocusInWindow());
    }

    private void showBuffSelection() {
        if (listBuff.isEmpty()) { log("(Tidak ada buff tersisa)"); return; }
        Window parent=SwingUtilities.getWindowAncestor(this);
        if (parent==null) return;
        BuffDialog dialog=new BuffDialog(parent, player, listBuff);
        dialog.setVisible(true);
        SwingUtilities.invokeLater(() -> visualPanel.requestFocusInWindow());
        updateButtonSub(btnAttack,"ATK: "+player.getDamage());
        updateButtonSub(btnHeal,"Sisa: "+player.getHealLimit());
        if (player.getHealLimit()>0) btnHeal.setEnabled(true);
        log("Buff aktif! HP="+player.getHp()+"/"+player.getMaxHp()+" | ATK="+player.getDamage());
    }

    private void spawnEnemyForWave(int w) {
        if (w==10) {
            baseEnemyDamage=100;
            enemyMaxHpCache=1000;
            currentEnemy=new Zomboss("ZOMBOSS",750,baseEnemyDamage);
        } else {
            String[] zombieNames={
                "Basic Zombie","Zombie Penjaga","Zombie Berseragam",
                "Zombie Penggali","Zombie Berlapis","Zombie Beracun",
                "Zombie Raksasa","Zombie Berapi","Zombie Elite"
            };
            String name=(w>=1&&w<=9)?zombieNames[w-1]:"Zombie Wave "+w;
            int hp =AssetConfig.getZombieHp(w);
            int atk=AssetConfig.getZombieAtk(w);
            baseEnemyDamage=atk;
            enemyMaxHpCache=hp;
            currentEnemy=new Zombie(name,hp,atk);
        }
    }

    private void initBuffs() {
        listBuff=new ArrayList<>();
        listBuff.add("God Slayer (+65 ATK)");
        listBuff.add("Punch Strike (+20 ATK)");
        listBuff.add("Steel Heart (+40 Max HP)");
        listBuff.add("Strong Defense (+65 Max HP)");
        listBuff.add("Lifesteal (+15 ATK + serap 10% MaxHP setiap serangan)");
        listBuff.add("Invisible (+15 Dodge Chance, maks 75%)");
        listBuff.add("Last Chance (Bangkit sekali saat mati dengan 100% HP)");
    }

    private void log(String msg) {
        SwingUtilities.invokeLater(()->{
            battleLog.append(msg+"\n");
            battleLog.setCaretPosition(battleLog.getDocument().getLength());
        });
    }
}