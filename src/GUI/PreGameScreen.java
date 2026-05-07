package GUI;

import Database.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

/**
 * PreGameScreen v3 — Layar pemilihan karakter & map.
 *
 * PERBAIKAN BUG (Revisi):
 *  [FIX-6] hoverTimer di CharCard dan MapCard dihentikan via HierarchyListener saat panel disembunyikan
 *  [FIX-7] startGame() — fallback akhir "Hero" jika playerName dan session keduanya kosong
 *  [FIX-8] confirmLogout() & confirmExit() — UIManager reset dibungkus try-finally agar selalu terjadi
 */
public class PreGameScreen extends JFrame {

    // ── State ─────────────────────────────────────────────────────────────
    private String selectedChar = "KNIGHT_PRINCE";
    private String selectedMap  = "FOREST";
    private String playerName;

    // ── Progress dari DB ─────────────────────────────────────────────────
    private DatabaseManager.GameProgress progress;

    // ── Komponen ─────────────────────────────────────────────────────────
    private JTextField nameField;
    private CharCard[] charCards;
    private MapCard[]  mapCards;
    private JButton    btnStart;
    private JLabel     lblMapInfoDynamic;

    // ── Animasi ──────────────────────────────────────────────────────────
    private Timer animTimer;
    private int   animFrame = 0;
    private Image bgImage;

    // ═══════════════════════════════════════════════════════════════════════
    public PreGameScreen() {
        setTitle("LAST CHANCE FOR LIFE — Pilih Karakter");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        setUndecorated(true);

        SessionManager session = SessionManager.getInstance();
        playerName = session.isLoggedIn() ? session.getUsername() : "Hero";
        progress   = session.getProgress();

        selectedChar = "KNIGHT_PRINCE";
        selectedMap  = "FOREST";

        try {
            java.net.URL url = getClass().getResource(AssetConfig.BG_PREGAME);
            if (url != null) bgImage = new ImageIcon(url).getImage();
        } catch (Exception ignored) {}

        JPanel root = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawBackground((Graphics2D) g);
            }
        };
        root.setPreferredSize(new Dimension(1020, 700));
        root.setBackground(new Color(8, 6, 18));

        buildUI(root);

        setContentPane(root);
        pack();
        setLocationRelativeTo(null);
        getRootPane().setBorder(BorderFactory.createLineBorder(new Color(180, 130, 30), 2));

        SoundManager.playBGM(SoundManager.BGM_MAIN);

        animTimer = new Timer(16, e -> { animFrame++; root.repaint(); });
        animTimer.start();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // BACKGROUND
    // ═══════════════════════════════════════════════════════════════════════

    private void drawBackground(Graphics2D g2) {
        int W = 1020, H = 700;
        if (bgImage != null) {
            g2.drawImage(bgImage, 0, 0, W, H, this);
            g2.setColor(new Color(4, 2, 14, 205));
            g2.fillRect(0, 0, W, H);
        } else {
            g2.setPaint(new GradientPaint(0,0,new Color(5,3,15),W,H,new Color(25,8,35)));
            g2.fillRect(0,0,W,H);
        }

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        for (int i = 0; i < 100; i++) {
            int sx = (i*137 + animFrame/4) % W;
            int sy = (i*79  + animFrame/6) % H;
            float alpha = 0.25f + (float)(Math.sin(animFrame*0.04+i)*0.3f);
            g2.setColor(new Color(1f,1f,1f,Math.max(0.05f,alpha)));
            g2.fillOval(sx, sy, (i%5==0)?2:1, (i%5==0)?2:1);
        }

        g2.setPaint(new GradientPaint(0,0,new Color(100,70,0,0),W/2,0,new Color(255,200,50,180)));
        g2.fillRect(0,0,W/2,2);
        g2.setPaint(new GradientPaint(W/2,0,new Color(255,200,50,180),W,0,new Color(100,70,0,0)));
        g2.fillRect(W/2,0,W/2,2);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // BUILD UI
    // ═══════════════════════════════════════════════════════════════════════

    private void buildUI(JPanel root) {
        int W = 1020;

        JLabel title = makeGoldTitle("LAST CHANCE FOR LIFE", 36);
        title.setBounds(0, 14, W, 48);
        root.add(title);

        JLabel subtitle = makeLabel("Pilih pahlawanmu dan medan perangmu, " + playerName + "!", 13, Font.ITALIC,
                new Color(180,155,110), SwingConstants.CENTER);
        subtitle.setBounds(0, 62, W, 20);
        root.add(subtitle);

        root.add(makeSep(60, 88, W-120, 2));

        buildAccountInfo(root);
        addHeaderButtons(root, W);

        JLabel lblName = makeLabel("⚔  Nama Karakter", 13, Font.BOLD, new Color(220,180,80), SwingConstants.LEFT);
        lblName.setBounds(60, 108, 200, 22);
        root.add(lblName);

        nameField = makeTextField(playerName);
        nameField.setBounds(270, 104, 200, 30);
        root.add(nameField);

        JLabel lblVol = makeLabel("🔊", 13, Font.PLAIN, new Color(180,160,100), SwingConstants.LEFT);
        lblVol.setBounds(490, 108, 25, 22);
        root.add(lblVol);
        JSlider volSlider = makeVolumeSlider();
        volSlider.setBounds(515, 108, 120, 26);
        root.add(volSlider);

        JLabel lblChar = makeLabel("👤  Pilih Karakter", 13, Font.BOLD, new Color(220,180,80), SwingConstants.LEFT);
        lblChar.setBounds(60, 148, 300, 22);
        root.add(lblChar);

        JLabel lblCharHint = makeLabel("(Knight Prince selalu tersedia — selesaikan Forest untuk membuka lainnya)",
                10, Font.ITALIC, new Color(150,130,80), SwingConstants.LEFT);
        lblCharHint.setBounds(60, 168, 700, 16);
        root.add(lblCharHint);

        charCards = new CharCard[AssetConfig.CHAR_KEYS.length];
        int charX = 60;
        for (int i = 0; i < AssetConfig.CHAR_KEYS.length; i++) {
            final String key = AssetConfig.CHAR_KEYS[i];
            boolean unlocked = progress.isCharUnlocked(key);
            CharCard card = new CharCard(key, unlocked);
            card.setBounds(charX, 188, 136, 178);
            root.add(card);
            charCards[i] = card;
            charX += 152;

            card.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if (unlocked) selectChar(key);
                    else showLockMessage(key, false);
                }
            });
        }

        root.add(makeSep(60, 374, W-120, 1));

        JLabel lblMap = makeLabel("🗺  Pilih Medan Perang", 13, Font.BOLD, new Color(220,180,80), SwingConstants.LEFT);
        lblMap.setBounds(60, 382, 300, 22);
        root.add(lblMap);

        JLabel lblMapHint = makeLabel("(Forest selalu tersedia — selesaikan map secara berurutan untuk membuka lebih lanjut)",
                10, Font.ITALIC, new Color(150,130,80), SwingConstants.LEFT);
        lblMapHint.setBounds(60, 400, 800, 16);
        root.add(lblMapHint);

        mapCards = new MapCard[AssetConfig.MAP_KEYS.length];
        int mapX = 60;
        for (int i = 0; i < AssetConfig.MAP_KEYS.length; i++) {
            final String key = AssetConfig.MAP_KEYS[i];
            boolean unlocked = progress.isMapUnlocked(key);
            MapCard card = new MapCard(key, unlocked);
            card.setBounds(mapX, 420, 166, 114);
            root.add(card);
            mapCards[i] = card;
            mapX += 182;

            card.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if (unlocked) selectMap(key);
                    else showLockMessage(key, true);
                }
            });
        }

        lblMapInfoDynamic = buildMapInfoLabel();
        lblMapInfoDynamic.setBounds(60, 542, W-120, 80);
        root.add(lblMapInfoDynamic);

        btnStart = makeStartButton();
        btnStart.setBounds((W-320)/2, 634, 320, 52);
        root.add(btnStart);

        selectChar(selectedChar);
        selectMap(selectedMap);
    }

    // ── Account Info Box ─────────────────────────────────────────────────

    private void buildAccountInfo(JPanel root) {
        JPanel infoBox = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(12,9,25,200));
                g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10);
                g2.setColor(new Color(120,90,30,100));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(1,1,getWidth()-3,getHeight()-3,10,10);

                SessionManager s = SessionManager.getInstance();
                DatabaseManager.GameProgress p = s.getProgress();
                g2.setFont(new Font("Serif",Font.BOLD,12));
                g2.setColor(new Color(255,200,60));
                g2.drawString("👤 " + s.getUsername(), 8, 18);
                g2.setFont(new Font("Arial",Font.PLAIN,10));
                g2.setColor(new Color(180,160,120));
                g2.drawString("Games: " + p.gamesCompleted + "  |  Menang: " + p.totalWins, 8, 34);
                g2.setColor(new Color(100,200,255));
                g2.drawString("Wave Tertinggi: " + p.highestWave, 8, 48);
                g2.dispose();
            }
        };
        infoBox.setOpaque(false);
        infoBox.setBounds(646, 100, 200, 56);
        root.add(infoBox);
    }

    private void addHeaderButtons(JPanel root, int W) {
        JButton btnClose = new JButton("✕") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setColor(new Color(80,30,30));
                g2.fillRoundRect(0,0,getWidth(),getHeight(),6,6);
                g2.setFont(new Font("Arial",Font.BOLD,13));
                g2.setColor(new Color(200,100,100));
                FontMetrics fm=g2.getFontMetrics();
                g2.drawString("✕",(getWidth()-fm.stringWidth("✕"))/2,getHeight()/2+fm.getAscent()/2-2);
                g2.dispose();
            }
        };
        btnClose.setContentAreaFilled(false);
        btnClose.setBorderPainted(false);
        btnClose.setFocusPainted(false);
        btnClose.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnClose.setBounds(W-36, 6, 28, 24);
        btnClose.addActionListener(e -> confirmExit());
        root.add(btnClose);

        JButton btnLogout = new JButton("🚪 Logout") {
            private boolean hov=false;
            { addMouseListener(new MouseAdapter(){
                public void mouseEntered(MouseEvent e){hov=true;repaint();}
                public void mouseExited(MouseEvent e){hov=false;repaint();}
            }); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setColor(hov?new Color(80,30,30):new Color(55,20,20));
                g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);
                g2.setFont(new Font("Serif",Font.PLAIN,12));
                g2.setColor(new Color(200,120,100));
                FontMetrics fm=g2.getFontMetrics();
                String t=getText();
                g2.drawString(t,(getWidth()-fm.stringWidth(t))/2,getHeight()/2+fm.getAscent()/2-2);
                g2.dispose();
            }
            @Override public Dimension getPreferredSize(){return new Dimension(90,26);}
        };
        btnLogout.setContentAreaFilled(false);
        btnLogout.setBorderPainted(false);
        btnLogout.setFocusPainted(false);
        btnLogout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogout.setBounds(W-130, 6, 90, 26);
        btnLogout.addActionListener(e -> confirmLogout());
        root.add(btnLogout);
    }

    // ── Map Info Label ───────────────────────────────────────────────────

    private JLabel buildMapInfoLabel() {
        JLabel lbl = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(12,9,25,200));
                g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10);
                g2.setColor(new Color(120,90,30,100));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(1,1,getWidth()-3,getHeight()-3,10,10);

                g2.setFont(new Font("Arial",Font.BOLD,12));
                g2.setColor(new Color(255,200,60));
                g2.drawString("🗺 " + AssetConfig.getMapName(selectedMap), 10, 20);

                g2.setFont(new Font("Arial",Font.PLAIN,11));
                g2.setColor(new Color(180,160,120));
                g2.drawString("♪ BGM: " + AssetConfig.getMapBgmName(selectedMap), 10, 38);

                g2.setColor(new Color(160,200,255));
                String[] desc = AssetConfig.getMapDesc(selectedMap);
                for (int i=0; i<desc.length; i++)
                    g2.drawString("  " + desc[i], 10, 54+i*16);

                String sinergi = getSinergiText();
                if (!sinergi.isEmpty()) {
                    g2.setFont(new Font("Arial",Font.BOLD,11));
                    g2.setColor(new Color(100,255,150));
                    g2.drawString("✨ Sinergi: " + sinergi, 10, 72);
                }
                g2.dispose();
            }
        };
        return lbl;
    }

    // ── Volume Slider ────────────────────────────────────────────────────

    private JSlider makeVolumeSlider() {
        JSlider sl = new JSlider(0, 100, (int)(SoundManager.getVolume()*100));
        sl.setOpaque(false);
        sl.setForeground(new Color(200,160,40));
        sl.addChangeListener(e -> {
            float vol = sl.getValue() / 100f;
            SoundManager.setVolume(vol);
        });
        return sl;
    }

    // ── TextField ────────────────────────────────────────────────────────

    private JTextField makeTextField(String defText) {
        JTextField tf = new JTextField(defText) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(18,13,32));
                g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);
                g2.setColor(new Color(150,110,30,120));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,8,8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        tf.setOpaque(false);
        tf.setForeground(new Color(230,210,160));
        tf.setCaretColor(new Color(255,200,50));
        tf.setFont(new Font("Serif",Font.PLAIN,15));
        tf.setBorder(BorderFactory.createEmptyBorder(4,10,4,10));
        return tf;
    }

    // ── Lock Message ─────────────────────────────────────────────────────

    private void showLockMessage(String key, boolean isMap) {
        String hint = progress.getUnlockHint(key);
        String name = isMap ? AssetConfig.getMapName(key) : AssetConfig.getCharacterName(key);
        JOptionPane.showMessageDialog(this,
            "🔒 " + name + " masih terkunci!\n\n" +
            "Cara membuka: " + hint,
            "Terkunci", JOptionPane.WARNING_MESSAGE);
    }

    // FIX-8: try-finally memastikan resetDialogTheme() selalu dipanggil
    private void confirmLogout() {
        applyDialogTheme();
        try {
            int result = JOptionPane.showConfirmDialog(
                this,
                "Apakah kamu yakin ingin logout?\n\n" +
                "Progress game sudah tersimpan otomatis.\n" +
                "Kamu bisa login kembali kapan saja.",
                "🚪  Konfirmasi Logout",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            if (result == JOptionPane.YES_OPTION) {
                animTimer.stop();
                SoundManager.stopBGM();
                SessionManager.getInstance().logout();
                this.dispose();
                SwingUtilities.invokeLater(() -> new AuthScreen().setVisible(true));
            }
        } finally {
            resetDialogTheme(); // ← FIX-8: selalu dipanggil meskipun terjadi exception
        }
    }

    // FIX-8: try-finally memastikan resetDialogTheme() selalu dipanggil
    private void confirmExit() {
        applyDialogTheme();
        try {
            int result = JOptionPane.showConfirmDialog(
                this,
                "Apakah kamu yakin ingin keluar dari game?\n\n" +
                "Progress sudah tersimpan otomatis.",
                "✕  Keluar dari Game",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            if (result == JOptionPane.YES_OPTION) {
                animTimer.stop();
                SoundManager.stopBGM();
                System.exit(0);
            }
        } finally {
            resetDialogTheme(); // ← FIX-8: selalu dipanggil meskipun terjadi exception
        }
    }

    private void applyDialogTheme() {
        UIManager.put("OptionPane.background",        new Color(14, 10, 28));
        UIManager.put("Panel.background",             new Color(14, 10, 28));
        UIManager.put("OptionPane.messageForeground", new Color(220, 200, 150));
        UIManager.put("OptionPane.messageFont",       new Font("Serif", Font.PLAIN, 14));
        UIManager.put("Button.background",            new Color(90, 65, 12));
        UIManager.put("Button.foreground",            new Color(255, 210, 60));
        UIManager.put("Button.font",                  new Font("Serif", Font.BOLD, 13));
    }

    private void resetDialogTheme() {
        // FIX-8: reset SEMUA 7 key yang di-set di applyDialogTheme()
        UIManager.put("OptionPane.background",        null);
        UIManager.put("Panel.background",             null);
        UIManager.put("OptionPane.messageForeground", null);
        UIManager.put("OptionPane.messageFont",       null); // ← sebelumnya tidak di-reset
        UIManager.put("Button.background",            null);
        UIManager.put("Button.foreground",            null);
        UIManager.put("Button.font",                  null); // ← sebelumnya tidak di-reset
    }

    // ── Sinergi ─────────────────────────────────────────────────────────

    private String getSinergiText() {
        if ("UNESA".equals(selectedMap) && ("UNESA_BOYS".equals(selectedChar)||"UNESA_GIRLS".equals(selectedChar)))
            return "Unesa di Kampus = ATK Player +10%!";
        if ("MOUNTAIN".equals(selectedMap) && "RAYMOND".equals(selectedChar))
            return "Raymond di Gunung = Dodge sangat berguna!";
        if ("ZOMBOSS_MAP".equals(selectedMap) && "XAVIER".equals(selectedChar))
            return "Xavier Vampiric vs DoT = Lebih survivable!";
        return "";
    }

    // ── Seleksi ──────────────────────────────────────────────────────────

    private void selectChar(String key) {
        if (!progress.isCharUnlocked(key)) return;
        selectedChar = key;
        for (CharCard c : charCards) { c.setSelected(c.key.equals(key)); c.repaint(); }
        if (lblMapInfoDynamic != null) lblMapInfoDynamic.repaint();
    }

    private void selectMap(String key) {
        if (!progress.isMapUnlocked(key)) return;
        selectedMap = key;
        for (MapCard m : mapCards) { m.setSelected(m.key.equals(key)); m.repaint(); }
        if (lblMapInfoDynamic != null) lblMapInfoDynamic.repaint();
    }

    // ── Start Game ───────────────────────────────────────────────────────

    private void startGame() {
        // FIX-7: Fallback bertingkat agar playerName tidak pernah kosong
        playerName = nameField.getText().trim();
        if (playerName.isEmpty()) playerName = SessionManager.getInstance().getUsername();
        if (playerName.isEmpty()) playerName = "Hero"; // ← FIX-7: fallback akhir

        animTimer.stop();
        SoundManager.stopBGM();

        JFrame gameWindow = new JFrame();
        gameWindow.setTitle("LAST CHANCE FOR LIFE — " + playerName);
        gameWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gameWindow.setResizable(false);

        GamePanel gamePanel = new GamePanel(playerName, selectedMap, selectedChar);
        gameWindow.add(gamePanel);
        gameWindow.pack();
        gameWindow.setLocationRelativeTo(null);
        gameWindow.setVisible(true);
        this.dispose();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // WIDGET FACTORIES
    // ═══════════════════════════════════════════════════════════════════════

    private JLabel makeGoldTitle(String text, int size) {
        JLabel lbl = new JLabel(text, SwingConstants.CENTER) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setFont(new Font("Serif",Font.BOLD,size));
                String t=getText();
                FontMetrics fm=g2.getFontMetrics();
                int tx=(getWidth()-fm.stringWidth(t))/2;
                for (int r=8;r>0;r-=2) {
                    g2.setColor(new Color(200,150,0,(int)(40*(8-r)/8.0)));
                    g2.drawString(t,tx-r/2+2,fm.getAscent()+r/2+2);
                }
                g2.setColor(new Color(0,0,0,160));
                g2.drawString(t,tx+2,fm.getAscent()+2);
                g2.setColor(new Color(255,210,60));
                g2.drawString(t,tx,fm.getAscent());
                g2.dispose();
            }
        };
        return lbl;
    }

    private JLabel makeLabel(String text, int size, int style, Color color, int align) {
        JLabel l=new JLabel(text,align);
        l.setFont(new Font("Serif",style,size));
        l.setForeground(color);
        return l;
    }

    private JPanel makeSep(int x, int y, int w, int h) {
        JPanel sep = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g;
                g2.setPaint(new GradientPaint(0,0,new Color(100,70,20,0),getWidth()/2,0,new Color(220,160,40,160)));
                g2.fillRect(0,0,getWidth()/2,getHeight());
                g2.setPaint(new GradientPaint(getWidth()/2,0,new Color(220,160,40,160),getWidth(),0,new Color(100,70,20,0)));
                g2.fillRect(getWidth()/2,0,getWidth(),getHeight());
            }
        };
        sep.setOpaque(false);
        sep.setBounds(x,y,w,h);
        return sep;
    }

    private JButton makeStartButton() {
        JButton btn = new JButton() {
            private boolean hov=false;
            { addMouseListener(new MouseAdapter(){
                public void mouseEntered(MouseEvent e){hov=true;repaint();}
                public void mouseExited(MouseEvent e){hov=false;repaint();}
            }); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                int w=getWidth(),h=getHeight();
                Color c1=hov?new Color(200,150,30):new Color(155,110,18);
                Color c2=hov?new Color(240,190,50):new Color(200,140,28);
                g2.setPaint(new GradientPaint(0,0,c1,0,h,c2));
                g2.fillRoundRect(0,0,w,h,14,14);
                if (hov) {
                    float sh=(animFrame%40)/40f;
                    g2.setPaint(new GradientPaint((int)(sh*w),0,new Color(255,255,255,0),
                            (int)(sh*w)+80,0,new Color(255,255,255,55)));
                    g2.fillRoundRect(0,0,w,h,14,14);
                }
                g2.setColor(new Color(255,255,255,130));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(1,1,w-3,h-3,14,14);
                g2.setFont(new Font("Serif",Font.BOLD,20));
                g2.setColor(new Color(15,8,3));
                FontMetrics fm=g2.getFontMetrics();
                String txt="⚔  MULAI BERTARUNG  ⚔";
                g2.drawString(txt,(w-fm.stringWidth(txt))/2,h/2+fm.getAscent()/2-3);
                g2.dispose();
            }
            @Override public Dimension getPreferredSize(){return new Dimension(320,52);}
        };
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> startGame());
        return btn;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Inner: Kartu Karakter
    // ═══════════════════════════════════════════════════════════════════════

    private class CharCard extends JPanel {
        final String key;
        final boolean unlocked;
        private boolean selected = false;
        private boolean hovered  = false;
        private float   scale    = 1f;
        private Image   charImg;
        private final Timer hoverTimer;

        CharCard(String key, boolean unlocked) {
            this.key      = key;
            this.unlocked = unlocked;
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(unlocked ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));

            try {
                java.net.URL url = getClass().getResource(AssetConfig.getCharacterPath(key));
                if (url != null) charImg = new ImageIcon(url).getImage();
            } catch (Exception ignored) {}

            hoverTimer = new Timer(16, e -> {
                float target = (hovered && unlocked) || selected ? 1.05f : 1f;
                scale += (target - scale) * 0.15f;
                repaint();
            });
            hoverTimer.start();

            // FIX-6: Hentikan timer saat komponen tidak lagi ditampilkan
            addHierarchyListener(e -> {
                if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0
                        && !isShowing()) {
                    hoverTimer.stop();
                }
            });

            addMouseListener(new MouseAdapter(){
                public void mouseEntered(MouseEvent e){ hovered=true; }
                public void mouseExited(MouseEvent e) { hovered=false; }
            });
        }

        void setSelected(boolean s) { this.selected = s; }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2=(Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            int w=getWidth(), h=getHeight();
            double cx=w/2.0, cy=h/2.0;
            g2.translate(cx,cy); g2.scale(scale,scale); g2.translate(-cx,-cy);

            g2.setColor(new Color(0,0,0,80));
            g2.fillRoundRect(3,4,w-2,h-2,12,12);

            Color bg = selected ? new Color(50,35,5) : new Color(20,15,34);
            g2.setColor(bg);
            g2.fillRoundRect(0,0,w-2,h-2,12,12);

            Color border = selected ? new Color(255,200,50)
                    : (hovered && unlocked) ? new Color(180,140,40)
                    : new Color(70,55,28);
            g2.setColor(border);
            g2.setStroke(new BasicStroke(selected?2.5f:1.5f));
            g2.drawRoundRect(0,0,w-3,h-3,12,12);

            int imgW = AssetConfig.CHAR_CARD_IMG_W;
            int imgH = (int)(imgW * AssetConfig.CHAR_CARD_IMG_RATIO);
            int imgX = (w - imgW) / 2;
            int imgY = 8;

            if (charImg != null) {
                g2.drawImage(charImg, imgX, imgY, imgW, imgH, this);
            } else {
                g2.setColor(new Color(120,160,220));
                g2.fillOval(w/2-14, 14, 28, 28);
                g2.fillRect(w/2-10, 46, 20, 38);
            }

            if (!unlocked) {
                g2.setColor(new Color(0,0,0,160));
                g2.fillRoundRect(0,0,w-2,h-2,12,12);
                g2.setFont(new Font("Segoe UI Emoji",Font.PLAIN,28));
                FontMetrics fm0=g2.getFontMetrics();
                g2.setColor(new Color(200,180,80,220));
                g2.drawString("🔒",(w-fm0.stringWidth("🔒"))/2, h/2+4);
                g2.setFont(new Font("Arial",Font.PLAIN,8));
                g2.setColor(new Color(180,160,100,200));
                String hint = progress.getUnlockHint(key);
                FontMetrics fmH=g2.getFontMetrics();
                wrapDraw(g2, hint, fmH, w, h/2+20, w-6);
            }

            g2.setFont(new Font("Serif",Font.BOLD,11));
            FontMetrics fm=g2.getFontMetrics();
            String name=AssetConfig.getCharacterName(key);
            g2.setColor(new Color(0,0,0,140));
            g2.drawString(name,(w-fm.stringWidth(name))/2+1,h-40+1);
            g2.setColor(unlocked?(selected?new Color(255,210,60):new Color(200,180,130)):new Color(120,110,80));
            g2.drawString(name,(w-fm.stringWidth(name))/2,h-40);

            g2.setFont(new Font("Arial",Font.PLAIN,9));
            g2.setColor(unlocked?new Color(160,200,160):new Color(100,110,90));
            String stats=AssetConfig.getCharacterStats(key);
            FontMetrics fm2=g2.getFontMetrics();
            g2.drawString(stats,(w-fm2.stringWidth(stats))/2,h-26);

            if (unlocked) {
                g2.setFont(new Font("Arial",Font.ITALIC,8));
                g2.setColor(new Color(100,200,255));
                String ability=AssetConfig.getCharacterAbility(key);
                FontMetrics fm3=g2.getFontMetrics();
                if (ability.length()>18) {
                    int splitAt=ability.indexOf(':',10);
                    if (splitAt<0) splitAt=ability.indexOf(' ',ability.length()/2);
                    String l1=ability.substring(0,Math.min(splitAt+1,ability.length()));
                    String l2=(splitAt+1<ability.length())?ability.substring(splitAt+1).trim():"";
                    g2.drawString(l1,(w-fm3.stringWidth(l1))/2,h-14);
                    g2.drawString(l2,(w-fm3.stringWidth(l2))/2,h-4);
                } else {
                    g2.drawString(ability,(w-fm3.stringWidth(ability))/2,h-8);
                }
            }

            if (selected && unlocked) {
                g2.setColor(new Color(255,200,50,200));
                g2.setFont(new Font("Segoe UI Emoji",Font.PLAIN,14));
                g2.drawString("✓",w-20,18);
            }
            g2.dispose();
        }

        private void wrapDraw(Graphics2D g2, String text, FontMetrics fm, int cw, int y, int maxW) {
            String[] words = text.split(" ");
            StringBuilder line = new StringBuilder();
            for (String w : words) {
                String test = line + (line.length()>0?" ":"") + w;
                if (fm.stringWidth(test) > maxW-4) {
                    String s = line.toString();
                    g2.drawString(s, (cw-fm.stringWidth(s))/2, y);
                    y += fm.getHeight();
                    line = new StringBuilder(w);
                } else {
                    if (line.length()>0) line.append(" ");
                    line.append(w);
                }
            }
            if (line.length()>0) {
                String s=line.toString();
                g2.drawString(s,(cw-fm.stringWidth(s))/2,y);
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Inner: Kartu Map
    // ═══════════════════════════════════════════════════════════════════════

    private class MapCard extends JPanel {
        final String key;
        final boolean unlocked;
        private boolean selected = false;
        private boolean hovered  = false;
        private float   scale    = 1f;
        private Image   bgImg;
        private final Timer hoverTimer;

        MapCard(String key, boolean unlocked) {
            this.key      = key;
            this.unlocked = unlocked;
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(unlocked?Cursor.HAND_CURSOR:Cursor.DEFAULT_CURSOR));

            try {
                java.net.URL url = getClass().getResource(AssetConfig.getBgPath(key));
                if (url!=null) bgImg=new ImageIcon(url).getImage();
            } catch (Exception ignored) {}

            hoverTimer=new Timer(16,e->{
                float target=(hovered&&unlocked)||selected?1.04f:1f;
                scale+=(target-scale)*0.15f;
                repaint();
            });
            hoverTimer.start();

            // FIX-6: Hentikan timer saat komponen tidak lagi ditampilkan
            addHierarchyListener(e -> {
                if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0
                        && !isShowing()) {
                    hoverTimer.stop();
                }
            });

            addMouseListener(new MouseAdapter(){
                public void mouseEntered(MouseEvent e){hovered=true;}
                public void mouseExited(MouseEvent e){hovered=false;}
            });
        }

        void setSelected(boolean s){ this.selected=s; }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2=(Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            int w=getWidth(),h=getHeight();
            double cx=w/2.0,cy=h/2.0;
            g2.translate(cx,cy); g2.scale(scale,scale); g2.translate(-cx,-cy);

            g2.setColor(new Color(0,0,0,80));
            g2.fillRoundRect(3,4,w-2,h-2,10,10);

            RoundRectangle2D shape=new RoundRectangle2D.Float(0,0,w-3,h-3,10,10);
            g2.setClip(shape);

            if (bgImg!=null) {
                g2.drawImage(bgImg,0,0,w-3,h-3,this);
                int dimAlpha="UNESA".equals(key)?155:(selected?90:120);
                g2.setColor(new Color(0,0,0,dimAlpha));
                g2.fillRect(0,0,w,h);
            } else {
                g2.setColor(new Color(20,15,35));
                g2.fillRect(0,0,w,h);
            }

            g2.setPaint(new GradientPaint(0,h-30,new Color(0,0,0,0),0,h,new Color(0,0,0,220)));
            g2.fillRect(0,h-35,w,35);
            g2.setClip(null);

            if (!unlocked) {
                g2.setColor(new Color(0,0,0,170));
                g2.fill(shape);
                g2.setFont(new Font("Segoe UI Emoji",Font.PLAIN,22));
                FontMetrics fmL=g2.getFontMetrics();
                g2.setColor(new Color(200,180,80,200));
                g2.drawString("🔒",(w-fmL.stringWidth("🔒"))/2,h/2+4);
            }

            Color border=selected?new Color(255,200,50):(hovered&&unlocked)?new Color(180,140,40):new Color(60,48,24);
            g2.setColor(border);
            g2.setStroke(new BasicStroke(selected?2.5f:1.5f));
            g2.draw(shape);

            g2.setFont(new Font("Serif",Font.BOLD,12));
            FontMetrics fm=g2.getFontMetrics();
            String name=AssetConfig.getMapName(key);
            g2.setColor(new Color(0,0,0,160));
            g2.drawString(name,(w-fm.stringWidth(name))/2+1,h-9+1);
            g2.setColor(unlocked?(selected?new Color(255,215,60):new Color(220,200,150)):new Color(120,110,80));
            g2.drawString(name,(w-fm.stringWidth(name))/2,h-9);

            g2.setFont(new Font("Arial",Font.PLAIN,9));
            g2.setColor(new Color(140,200,255,200));
            String bgm="♪ "+AssetConfig.getMapBgmName(key);
            FontMetrics fm2=g2.getFontMetrics();
            g2.drawString(bgm,(w-fm2.stringWidth(bgm))/2,15);

            g2.setFont(new Font("Arial",Font.BOLD,8));
            String badge=getMapBadge(key);
            if (!badge.isEmpty() && unlocked) {
                g2.setColor(new Color(255,80,80,220));
                g2.drawString(badge,5,h-22);
            }

            if (selected&&unlocked) {
                g2.setFont(new Font("Segoe UI Emoji",Font.PLAIN,14));
                g2.setColor(new Color(255,200,50,220));
                g2.drawString("✓",w-20,20);
            }
            g2.dispose();
        }

        private String getMapBadge(String k){
            switch(k){
                case "ZOMBOSS_MAP": return "⚠ -5%HP/3s";
                case "FROZEN":      return "❄ Freeze!";
                case "MOUNTAIN":    return "⛰ Cepat!";
                case "UNESA":       return "⚔ Zombie+5%";
                default:            return "";
            }
        }
    }

    // ── MAIN ─────────────────────────────────────────────────────────────
    public static void main(String[] args) {
        SwingUtilities.invokeLater(()->new PreGameScreen().setVisible(true));
    }
}