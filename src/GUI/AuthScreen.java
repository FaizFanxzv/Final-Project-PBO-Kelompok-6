package GUI;

import Database.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * AuthScreen v3 — Layar Login & Register.
 *
 * PERUBAHAN v3:
 *  [1] Panel kiri "Akun Terakhir" — menampilkan maks 5 akun yang pernah login.
 *      Terinspirasi dari Plants vs Zombies: nama + stats ringkas + timestamp.
 *      User tetap harus mengisi password sendiri (klik = isi username otomatis).
 *  [2] Register berhasil → tampilkan StoryScreen STORY_NEW_GAME lalu tandai di DB.
 *  [3] SoundManager perbaikan (referensi v2) sudah terintegrasi.
 */
public class AuthScreen extends JFrame {

    private static final int W = 860;
    private static final int H = 560;
    private static final int LEFT_W = 200;

    // ── Panel swap ────────────────────────────────────────────────────────
    private JPanel cardContainer;

    // ── Login fields ──────────────────────────────────────────────────────
    private JTextField     loginUserField;
    private JPasswordField loginPassField;
    private JLabel         loginErrLabel;

    // ── Register fields ───────────────────────────────────────────────────
    private JTextField     regUserField;
    private JPasswordField regPassField;
    private JPasswordField regPassConfField;
    private JLabel         regErrLabel;

    // ── Tab buttons ───────────────────────────────────────────────────────
    private JButton tabLogin, tabRegister;

    // ── Animasi ───────────────────────────────────────────────────────────
    private int   animFrame = 0;
    private Timer animTimer;
    private Image bgImage;

    // ── Recent accounts data ──────────────────────────────────────────────
    private List<String[]> recentAccounts;
    private JPanel         recentPanel;

    // ════════════════════════════════════════════════════════════════════════
    public AuthScreen() {
        setTitle("LAST CHANCE FOR LIFE — Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        setUndecorated(true);

        try {
            java.net.URL url = getClass().getResource(AssetConfig.BG_PREGAME);
            if (url != null) bgImage = new ImageIcon(url).getImage();
        } catch (Exception ignored) {}

        recentAccounts = DatabaseManager.getInstance().getRecentAccounts();

        JPanel root = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawBackground((Graphics2D) g);
            }
        };
        root.setPreferredSize(new Dimension(W, H));
        root.setBackground(new Color(8, 6, 18));

        // Tombol X
        root.add(makeCloseButton()).setBounds(W-34, 6, 28, 24);

        // Panel kiri: akun terakhir
        buildRecentPanel(root);

        // Panel tengah-kanan: kartu login/register
        buildCenterCard(root);

        setContentPane(root);
        pack();
        setLocationRelativeTo(null);
        getRootPane().setBorder(BorderFactory.createLineBorder(new Color(180, 130, 30), 2));

        SoundManager.playBGM(SoundManager.BGM_MAIN);

        animTimer = new Timer(16, e -> { animFrame++; root.repaint(); });
        animTimer.start();
    }

    // ════════════════════════════════════════════════════════════════════════
    // BACKGROUND
    // ════════════════════════════════════════════════════════════════════════

    private void drawBackground(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (bgImage != null) {
            g2.drawImage(bgImage, 0, 0, W, H, this);
            g2.setColor(new Color(4, 2, 12, 215));
            g2.fillRect(0, 0, W, H);
        } else {
            g2.setPaint(new GradientPaint(0, 0, new Color(6,4,16), W, H, new Color(22,8,32)));
            g2.fillRect(0, 0, W, H);
        }

        // Partikel bintang
        for (int i = 0; i < 80; i++) {
            int sx = (i*137 + animFrame/4) % W;
            int sy = (i*79  + animFrame/6) % H;
            float alpha = Math.max(0.05f, 0.3f+(float)(Math.sin(animFrame*0.04+i)*0.35f));
            g2.setColor(new Color(1f,1f,1f,alpha));
            g2.fillOval(sx, sy, (i%5==0)?2:1, (i%5==0)?2:1);
        }

        // Garis pemisah antara panel kiri dan kartu tengah
        g2.setColor(new Color(120, 90, 30, 100));
        g2.setStroke(new BasicStroke(1f));
        g2.drawLine(LEFT_W, 20, LEFT_W, H-20);

        // Garis emas atas
        g2.setPaint(new GradientPaint(0,0,new Color(100,70,0,0),W/2,0,new Color(255,200,50,190)));
        g2.fillRect(0,0,W/2,2);
        g2.setPaint(new GradientPaint(W/2,0,new Color(255,200,50,190),W,0,new Color(100,70,0,0)));
        g2.fillRect(W/2,0,W/2,2);
    }

    // ════════════════════════════════════════════════════════════════════════
    // PANEL KIRI — Akun Terakhir
    // ════════════════════════════════════════════════════════════════════════

    private void buildRecentPanel(JPanel root) {
        recentPanel = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawRecentPanel((Graphics2D) g);
            }
        };
        recentPanel.setOpaque(false);
        recentPanel.setBounds(0, 0, LEFT_W, H);
        root.add(recentPanel);
    }

    private void drawRecentPanel(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Judul panel
        g2.setFont(new Font("Serif", Font.BOLD, 13));
        g2.setColor(new Color(255, 200, 50, 210));
        String title = "📋 Riwayat Akun";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(title, (LEFT_W - fm.stringWidth(title))/2, 28);

        g2.setColor(new Color(180, 140, 40, 100));
        g2.setStroke(new BasicStroke(1f));
        g2.drawLine(14, 36, LEFT_W-14, 36);

        if (recentAccounts.isEmpty()) {
            g2.setFont(new Font("Arial", Font.ITALIC, 10));
            g2.setColor(new Color(140, 120, 80, 180));
            String empty = "Belum ada akun";
            FontMetrics fmE = g2.getFontMetrics();
            g2.drawString(empty, (LEFT_W - fmE.stringWidth(empty))/2, H/2);
            g2.setFont(new Font("Arial", Font.ITALIC, 9));
            g2.setColor(new Color(120, 100, 60, 140));
            String hint = "yang pernah login.";
            FontMetrics fmH = g2.getFontMetrics();
            g2.drawString(hint, (LEFT_W - fmH.stringWidth(hint))/2, H/2+14);
            return;
        }

        // Keterangan
        g2.setFont(new Font("Arial", Font.ITALIC, 9));
        g2.setColor(new Color(160, 140, 90, 160));
        String hint1 = "Klik nama untuk";
        String hint2 = "isi username otomatis";
        FontMetrics fmS = g2.getFontMetrics();
        g2.drawString(hint1, (LEFT_W-fmS.stringWidth(hint1))/2, 50);
        g2.drawString(hint2, (LEFT_W-fmS.stringWidth(hint2))/2, 62);

        // Daftar akun
        int slotH   = 76;
        int startY  = 74;
        int padding = 8;

        for (int i = 0; i < recentAccounts.size(); i++) {
            String[] parts = recentAccounts.get(i);
            // parts: [displayName, userId, timestamp]
            String  displayName = parts.length > 0 ? parts[0] : "?";
            String  userId      = parts.length > 1 ? parts[1] : "?";
            long    timestamp   = 0;
            try { if (parts.length > 2) timestamp = Long.parseLong(parts[2]); } catch (NumberFormatException ignored) {}

            int cardX = padding;
            int cardY = startY + i * (slotH + 6);
            int cardW = LEFT_W - padding*2;

            // Kartu
            boolean isFirst = (i == 0);
            Color cardBg = isFirst
                    ? new Color(60, 42, 8, 190)
                    : new Color(18, 13, 30, 170);
            Color cardBorder = isFirst
                    ? new Color(220, 170, 40, 180)
                    : new Color(90, 70, 25, 130);

            g2.setColor(cardBg);
            g2.fillRoundRect(cardX, cardY, cardW, slotH-4, 10, 10);
            g2.setColor(cardBorder);
            g2.setStroke(new BasicStroke(isFirst ? 1.8f : 1.2f));
            g2.drawRoundRect(cardX, cardY, cardW, slotH-4, 10, 10);

            // Badge "Terakhir" untuk akun pertama
            if (isFirst) {
                g2.setFont(new Font("Arial", Font.BOLD, 8));
                g2.setColor(new Color(255, 200, 50));
                g2.drawString("★ TERAKHIR", cardX+5, cardY+11);
            }

            // Ikon user
            int iconCX = cardX + 20;
            int iconCY = cardY + (slotH-4)/2;
            g2.setColor(new Color(100, 80, 20, 160));
            g2.fillOval(iconCX-11, iconCY-14, 22, 22);
            g2.setColor(new Color(200, 170, 80));
            g2.setStroke(new BasicStroke(1f));
            g2.drawOval(iconCX-11, iconCY-14, 22, 22);
            g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
            FontMetrics fmI = g2.getFontMetrics();
            g2.setColor(new Color(255, 215, 50));
            g2.drawString("👤", iconCX - fmI.stringWidth("👤")/2 - 1, iconCY+2);

            // Nama akun
            int textX = cardX + 36;
            int nameY = isFirst ? cardY+24 : cardY+20;
            g2.setFont(new Font("Serif", Font.BOLD, 12));
            g2.setColor(isFirst ? new Color(255,210,60) : new Color(200,180,130));
            // Potong nama jika terlalu panjang
            String name = truncate(g2, displayName, cardW - 42);
            g2.drawString(name, textX, nameY);

            // Stats (dari DB jika bisa)
            DatabaseManager.GameProgress gp = null;
            try {
                gp = DatabaseManager.getInstance().getProgress(Integer.parseInt(userId));
            } catch (NumberFormatException ignored) {}

            g2.setFont(new Font("Arial", Font.PLAIN, 9));
            if (gp != null) {
                g2.setColor(new Color(140, 200, 140));
                g2.drawString("🏆 " + gp.totalWins + " menang  W" + gp.highestWave, textX, nameY+13);
                g2.setColor(new Color(140, 160, 200));
                g2.drawString("🎮 " + gp.gamesCompleted + " partai", textX, nameY+25);
            }

            // Waktu terakhir login
            if (timestamp > 0) {
                g2.setFont(new Font("Arial", Font.ITALIC, 8));
                g2.setColor(new Color(120, 110, 70, 160));
                String timeStr = formatRelativeTime(timestamp);
                g2.drawString(timeStr, cardX+5, cardY+slotH-8);
            }
        }

        // Garis bawah setelah daftar
        int afterList = startY + recentAccounts.size() * (slotH + 6) + 4;
        if (afterList < H - 30) {
            g2.setColor(new Color(120, 90, 30, 80));
            g2.setStroke(new BasicStroke(1f));
            g2.drawLine(14, afterList, LEFT_W-14, afterList);
            g2.setFont(new Font("Arial", Font.ITALIC, 9));
            g2.setColor(new Color(120, 100, 60, 140));
            String note = "Password tetap";
            String note2 = "harus diisi manual";
            FontMetrics fmN = g2.getFontMetrics();
            g2.drawString(note,  (LEFT_W-fmN.stringWidth(note))/2,  afterList+14);
            g2.drawString(note2, (LEFT_W-fmN.stringWidth(note2))/2, afterList+25);
        }
    }

    /**
     * Tambahkan mouse listener ke recentPanel untuk deteksi klik kartu akun.
     * Dipanggil setelah UI selesai dibangun.
     */
    private void addRecentClickListeners() {
        if (recentAccounts.isEmpty()) return;

        recentPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int slotH  = 76;
                int startY = 74;
                int padding = 8;
                int cardW  = LEFT_W - padding*2;

                for (int i = 0; i < recentAccounts.size(); i++) {
                    Rectangle r = new Rectangle(padding, startY + i*(slotH+6), cardW, slotH-4);
                    if (r.contains(e.getPoint())) {
                        String displayName = recentAccounts.get(i)[0];
                        // Isi username ke field login dan pindah ke tab login
                        showLogin();
                        loginUserField.setText(displayName);
                        loginPassField.requestFocusInWindow();
                        loginErrLabel.setText("");
                        break;
                    }
                }
            }
        });

        // Kursor berubah saat hover kartu
        recentPanel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int slotH  = 76;
                int startY = 74;
                int padding = 8;
                int cardW  = LEFT_W - padding*2;

                boolean onCard = false;
                for (int i = 0; i < recentAccounts.size(); i++) {
                    Rectangle r = new Rectangle(padding, startY + i*(slotH+6), cardW, slotH-4);
                    if (r.contains(e.getPoint())) { onCard = true; break; }
                }
                recentPanel.setCursor(Cursor.getPredefinedCursor(
                        onCard ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
            }
        });
    }

    // ════════════════════════════════════════════════════════════════════════
    // BUILD UI — KARTU LOGIN
    // ════════════════════════════════════════════════════════════════════════

    private void buildCenterCard(JPanel root) {
        int cardX = LEFT_W + 12;
        int cardW = W - LEFT_W - 24;

        // Kartu
        JPanel card = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(10,7,22,235));
                g2.fillRoundRect(0,0,getWidth(),getHeight(),22,22);
                g2.setColor(new Color(150,110,30,110));
                g2.setStroke(new BasicStroke(1.8f));
                g2.drawRoundRect(1,1,getWidth()-3,getHeight()-3,22,22);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBounds(cardX, 50, cardW, 468);
        root.add(card);

        // Logo
        JLabel logo = new JLabel("⚔  LAST CHANCE FOR LIFE  ⚔", SwingConstants.CENTER) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setFont(new Font("Serif",Font.BOLD,20));
                String t=getText();
                FontMetrics fm=g2.getFontMetrics();
                int tx=(getWidth()-fm.stringWidth(t))/2;
                for (int r=6;r>0;r-=2) {
                    g2.setColor(new Color(200,150,0,35));
                    g2.drawString(t,tx-r/2+1,fm.getAscent()+r/2+1);
                }
                g2.setColor(new Color(0,0,0,160)); g2.drawString(t,tx+2,fm.getAscent()+2);
                g2.setColor(new Color(255,210,60)); g2.drawString(t,tx,fm.getAscent());
                g2.dispose();
            }
        };
        logo.setBounds(0, 14, cardW, 34);
        card.add(logo);

        JLabel sub = makeLabel("Petualangan Abadi Menantimu", 11, Font.ITALIC,
                new Color(160,140,100), SwingConstants.CENTER);
        sub.setBounds(0, 50, cardW, 16);
        card.add(sub);

        card.add(makeSep(30, 70, cardW-60, 2));

        buildTabBar(card, cardW);

        cardContainer = new JPanel(new CardLayout());
        cardContainer.setOpaque(false);
        cardContainer.setBounds(20, 134, cardW-40, 300);
        card.add(cardContainer);

        cardContainer.add(buildLoginPanel(cardW-40),    "LOGIN");
        cardContainer.add(buildRegisterPanel(cardW-40), "REGISTER");

        JLabel hint = makeLabel("Data tersimpan di folder  data/  dalam direktori project",
                9, Font.ITALIC, new Color(120,110,80), SwingConstants.CENTER);
        hint.setBounds(0, 440, cardW, 16);
        card.add(hint);

        showLogin();
        addRecentClickListeners();
    }

    // ── Tab Bar ──────────────────────────────────────────────────────────

    private void buildTabBar(JPanel card, int cardW) {
        JPanel bar = new JPanel(null);
        bar.setOpaque(false);
        bar.setBounds(30, 82, cardW-60, 42);
        card.add(bar);

        tabLogin    = makeTabButton("🗡  Login");
        tabRegister = makeTabButton("✍  Daftar Baru");
        int half = (cardW-60)/2;
        tabLogin.setBounds(0, 0, half, 42);
        tabRegister.setBounds(half, 0, half, 42);
        tabLogin.addActionListener(e -> showLogin());
        tabRegister.addActionListener(e -> showRegister());
        bar.add(tabLogin);
        bar.add(tabRegister);
    }

    private JButton makeTabButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                boolean active=Boolean.TRUE.equals(getClientProperty("active"));
                g2.setColor(active?new Color(90,65,12):new Color(18,13,30));
                g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);
                g2.setFont(new Font("Serif",Font.BOLD,14));
                g2.setColor(active?new Color(255,210,60):new Color(150,130,90));
                FontMetrics fm=g2.getFontMetrics();
                g2.drawString(getText(),(getWidth()-fm.stringWidth(getText()))/2,getHeight()/2+fm.getAscent()/2-2);
                if (active) {
                    g2.setColor(new Color(255,200,50));
                    g2.fillRect(6,getHeight()-3,getWidth()-12,3);
                }
                g2.dispose();
            }
        };
        btn.setContentAreaFilled(false); btn.setBorderPainted(false);
        btn.setFocusPainted(false);      btn.setFocusable(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ── Login Panel ───────────────────────────────────────────────────────

    private JPanel buildLoginPanel(int panelW) {
        JPanel p = new JPanel(null);
        p.setOpaque(false);

        addFieldLabel(p, "👤  Username", 0, 0, 220);
        loginUserField = makeTextField();
        loginUserField.setBounds(0, 22, panelW, 40);
        p.add(loginUserField);

        addFieldLabel(p, "🔑  Password", 0, 74, 220);
        loginPassField = makePasswordField();
        loginPassField.setBounds(0, 96, panelW, 40);
        p.add(loginPassField);

        loginErrLabel = makeLabel("", 11, Font.ITALIC, new Color(230,70,70), SwingConstants.CENTER);
        loginErrLabel.setBounds(0, 148, panelW, 18);
        p.add(loginErrLabel);

        JButton btnLogin = makeActionButton("⚔  MASUK KE GAME", new Color(130,90,15), new Color(190,140,25));
        btnLogin.setBounds(0, 170, panelW, 50);
        p.add(btnLogin);

        JLabel switchLabel = makeLabel("Belum punya akun?  → Klik tab 'Daftar Baru' di atas",
                11, Font.ITALIC, new Color(140,160,200), SwingConstants.CENTER);
        switchLabel.setBounds(0, 232, panelW, 18);
        p.add(switchLabel);

        loginPassField.addActionListener(e -> doLogin());
        btnLogin.addActionListener(e -> doLogin());
        return p;
    }

    // ── Register Panel ────────────────────────────────────────────────────

    private JPanel buildRegisterPanel(int panelW) {
        JPanel p = new JPanel(null);
        p.setOpaque(false);

        addFieldLabel(p, "👤  Username  (hanya huruf, angka, _)", 0, 0, 350);
        regUserField = makeTextField();
        regUserField.setBounds(0, 22, panelW, 40);
        p.add(regUserField);

        addFieldLabel(p, "🔑  Password  (minimal 4 karakter)", 0, 72, 320);
        regPassField = makePasswordField();
        regPassField.setBounds(0, 94, panelW, 40);
        p.add(regPassField);

        addFieldLabel(p, "🔑  Konfirmasi Password", 0, 146, 280);
        regPassConfField = makePasswordField();
        regPassConfField.setBounds(0, 168, panelW, 40);
        p.add(regPassConfField);

        regErrLabel = makeLabel("", 11, Font.ITALIC, new Color(230,70,70), SwingConstants.CENTER);
        regErrLabel.setBounds(0, 216, panelW, 18);
        p.add(regErrLabel);

        JButton btnReg = makeActionButton("✍  BUAT AKUN SEKARANG", new Color(28,90,45), new Color(45,150,70));
        btnReg.setBounds(0, 238, panelW, 50);
        p.add(btnReg);

        regPassConfField.addActionListener(e -> doRegister());
        btnReg.addActionListener(e -> doRegister());
        return p;
    }

    // ════════════════════════════════════════════════════════════════════════
    // AKSI LOGIN & REGISTER
    // ════════════════════════════════════════════════════════════════════════

    private void showLogin() {
        tabLogin.putClientProperty("active", true);
        tabRegister.putClientProperty("active", false);
        tabLogin.repaint(); tabRegister.repaint();
        ((CardLayout)cardContainer.getLayout()).show(cardContainer, "LOGIN");
        SwingUtilities.invokeLater(() -> loginUserField.requestFocusInWindow());
    }

    private void showRegister() {
        tabLogin.putClientProperty("active", false);
        tabRegister.putClientProperty("active", true);
        tabLogin.repaint(); tabRegister.repaint();
        ((CardLayout)cardContainer.getLayout()).show(cardContainer, "REGISTER");
        SwingUtilities.invokeLater(() -> regUserField.requestFocusInWindow());
    }

    // ── Dokan Login ───────────────────────────────────────────────────────

    private void doLogin() {
        loginErrLabel.setText("");
        String user = loginUserField.getText().trim();
        String pass = new String(loginPassField.getPassword());

        if (user.isEmpty()) { shake(loginErrLabel, "Username tidak boleh kosong."); return; }
        if (pass.isEmpty()) { shake(loginErrLabel, "Password tidak boleh kosong."); return; }

        int uid = DatabaseManager.getInstance().login(user, pass);
        if (uid < 0) {
            shake(loginErrLabel, "❌ Username atau password salah.");
            loginPassField.setText("");
            return;
        }

        SessionManager.getInstance().login(uid, user);
        DatabaseManager.GameProgress gp = DatabaseManager.getInstance().getProgress(uid);

        String statsMsg = String.format(
                "Game dimainkan : %d\nTotal kemenangan: %d\nWave tertinggi  : %d\n\nSelamat berjuang, %s!",
                gp.gamesCompleted, gp.totalWins, gp.highestWave,
                DatabaseManager.getInstance().getDisplayName(uid));
        showThemedDialog("✅  Login Berhasil!",
                "Selamat datang kembali, "+DatabaseManager.getInstance().getDisplayName(uid)+"!\n\n📊 Statistikmu:\n"+statsMsg,
                JOptionPane.INFORMATION_MESSAGE);

        openPreGame();
    }

    // ── Dokan Register ────────────────────────────────────────────────────

    private void doRegister() {
        regErrLabel.setText("");
        String user     = regUserField.getText().trim();
        String pass     = new String(regPassField.getPassword());
        String passConf = new String(regPassConfField.getPassword());

        if (user.isEmpty())                { shake(regErrLabel,"Username tidak boleh kosong."); return; }
        if (user.length() < 3)             { shake(regErrLabel,"Username minimal 3 karakter."); return; }
        if (!user.matches("[a-zA-Z0-9_]+")){
            shake(regErrLabel,"Username hanya boleh huruf, angka, dan '_'."); return; }
        if (pass.length() < 4)             { shake(regErrLabel,"Password minimal 4 karakter."); return; }
        if (!pass.equals(passConf))        { shake(regErrLabel,"Konfirmasi password tidak cocok."); return; }

        boolean ok = DatabaseManager.getInstance().register(user, pass);
        if (!ok) { shake(regErrLabel,"❌ Username '"+user+"' sudah digunakan."); return; }

        int uid = DatabaseManager.getInstance().login(user, pass);
        SessionManager.getInstance().login(uid, user);
        String displayName = DatabaseManager.getInstance().getDisplayName(uid);

        showThemedDialog("🎉  Akun Berhasil Dibuat!",
                "Selamat datang, "+displayName+"!\n\n"+
                "Perjalananmu dimulai sekarang!\n\n"+
                "🔓  Yang langsung tersedia:\n"+
                "    • Karakter : Knight Prince\n"+
                "    • Map      : Hutan Terlarang (Forest)\n\n"+
                "🔒  Cara membuka konten lain:\n"+
                "    • Menangkan Forest  → Buka Unesa Boys/Girls\n"+
                "    • Dan seterusnya...\n\n"+
                "Tekan Lanjut untuk mendengar prolog ceritamu! ⚔",
                JOptionPane.INFORMATION_MESSAGE);

        // Tampilkan story intro sebelum buka PreGame
        showNewGameStory(uid);
        openPreGame();
    }

    /** Tampilkan StoryScreen STORY_NEW_GAME jika belum pernah ditampilkan. */
    private void showNewGameStory(int userId) {
        DatabaseManager.GameProgress gp = DatabaseManager.getInstance().getProgress(userId);
        if (!gp.storyNewGameShown) {
            gp.storyNewGameShown = true;
            DatabaseManager.getInstance().saveProgress(userId, gp);
            SessionManager.getInstance().refreshProgress();
            StoryScreen.show(this, StoryScreen.STORY_NEW_GAME);
        }
    }

    // ── Pindah ke PreGame ─────────────────────────────────────────────────

    private void openPreGame() {
        animTimer.stop();
        SoundManager.stopBGM();
        this.dispose();
        SwingUtilities.invokeLater(() -> new PreGameScreen().setVisible(true));
    }

    // ── JOptionPane styled ────────────────────────────────────────────────

    private void showThemedDialog(String title, String message, int type) {
        UIManager.put("OptionPane.background",        new Color(14,10,28));
        UIManager.put("Panel.background",             new Color(14,10,28));
        UIManager.put("OptionPane.messageForeground", new Color(220,200,150));
        UIManager.put("OptionPane.messageFont",       new Font("Serif",Font.PLAIN,14));
        UIManager.put("Button.background",            new Color(90,65,12));
        UIManager.put("Button.foreground",            new Color(255,210,60));
        UIManager.put("Button.font",                  new Font("Serif",Font.BOLD,13));
        JOptionPane.showMessageDialog(this, message, title, type);
        UIManager.put("OptionPane.background",        null);
        UIManager.put("Panel.background",             null);
        UIManager.put("OptionPane.messageForeground", null);
        UIManager.put("OptionPane.messageFont",       null);
        UIManager.put("Button.background",            null);
        UIManager.put("Button.foreground",            null);
        UIManager.put("Button.font",                  null);
    }

    // ── Shake error ───────────────────────────────────────────────────────

    private void shake(JLabel label, String msg) {
        label.setText(msg);
        final int origX = label.getX();
        final int[] count = {0};
        Timer t = new Timer(40, null);
        t.addActionListener(e -> {
            int off = (count[0]%2==0)?6:-6;
            label.setBounds(origX+off, label.getY(), label.getWidth(), label.getHeight());
            if (++count[0]>=10) {
                label.setBounds(origX, label.getY(), label.getWidth(), label.getHeight());
                t.stop();
            }
        });
        t.start();
    }

    // ════════════════════════════════════════════════════════════════════════
    // HELPER UTILITIES
    // ════════════════════════════════════════════════════════════════════════

    /** Potong string agar tidak melebihi lebar tertentu. */
    private String truncate(Graphics2D g2, String s, int maxW) {
        FontMetrics fm = g2.getFontMetrics();
        if (fm.stringWidth(s) <= maxW) return s;
        String ellipsis = "...";
        while (s.length() > 1 && fm.stringWidth(s+ellipsis) > maxW)
            s = s.substring(0, s.length()-1);
        return s + ellipsis;
    }

    /** Format waktu relatif dari timestamp millis. */
    private String formatRelativeTime(long ts) {
        long diff = System.currentTimeMillis() - ts;
        if (diff < 60_000)         return "Baru saja";
        if (diff < 3_600_000)      return (diff/60_000)+"m lalu";
        if (diff < 86_400_000)     return (diff/3_600_000)+"j lalu";
        if (diff < 2_592_000_000L) return (diff/86_400_000)+"h lalu";
        return (diff/2_592_000_000L)+"bln lalu";
    }

    // ── Widget helpers ────────────────────────────────────────────────────

    private void addFieldLabel(JPanel p, String text, int x, int y, int w) {
        JLabel l = makeLabel(text, 12, Font.BOLD, new Color(200,170,80), SwingConstants.LEFT);
        l.setBounds(x, y, w, 20);
        p.add(l);
    }

    private JTextField makeTextField() {
        JTextField tf = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(16,11,30));
                g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10);
                boolean foc=isFocusOwner();
                g2.setColor(foc?new Color(200,160,40,220):new Color(90,70,25,140));
                g2.setStroke(new BasicStroke(foc?2f:1.3f));
                g2.drawRoundRect(1,1,getWidth()-3,getHeight()-3,10,10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        tf.setOpaque(false);
        tf.setForeground(new Color(230,210,160));
        tf.setCaretColor(new Color(255,200,50));
        tf.setFont(new Font("Serif",Font.PLAIN,15));
        tf.setBorder(BorderFactory.createEmptyBorder(6,12,6,12));
        return tf;
    }

    private JPasswordField makePasswordField() {
        JPasswordField pf = new JPasswordField() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(16,11,30));
                g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10);
                boolean foc=isFocusOwner();
                g2.setColor(foc?new Color(200,160,40,220):new Color(90,70,25,140));
                g2.setStroke(new BasicStroke(foc?2f:1.3f));
                g2.drawRoundRect(1,1,getWidth()-3,getHeight()-3,10,10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        pf.setOpaque(false);
        pf.setForeground(new Color(230,210,160));
        pf.setCaretColor(new Color(255,200,50));
        pf.setFont(new Font("Arial",Font.PLAIN,15));
        pf.setBorder(BorderFactory.createEmptyBorder(6,12,6,12));
        return pf;
    }

    private JButton makeActionButton(String text, Color c1, Color c2) {
        JButton btn = new JButton(text) {
            private boolean hov=false;
            { addMouseListener(new MouseAdapter(){
                public void mouseEntered(MouseEvent e){hov=true; repaint();}
                public void mouseExited(MouseEvent e){hov=false; repaint();}
            }); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                int w=getWidth(),h=getHeight();
                g2.setPaint(new GradientPaint(0,0,hov?c1.brighter():c1,0,h,hov?c2.brighter():c2));
                g2.fillRoundRect(0,0,w,h,12,12);
                if (hov) {
                    g2.setColor(new Color(255,255,255,55));
                    g2.setStroke(new BasicStroke(2f));
                    g2.drawRoundRect(1,1,w-3,h-3,12,12);
                }
                g2.setFont(new Font("Serif",Font.BOLD,16));
                g2.setColor(Color.WHITE);
                FontMetrics fm=g2.getFontMetrics();
                g2.drawString(getText(),(w-fm.stringWidth(getText()))/2,h/2+fm.getAscent()/2-3);
                g2.dispose();
            }
        };
        btn.setContentAreaFilled(false); btn.setBorderPainted(false);
        btn.setFocusPainted(false);      btn.setFocusable(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton makeCloseButton() {

    // Load icon X dari ImageAssets
    Image iconImg = null;

    try {
        java.net.URL url =
                getClass().getResource("/ImageAssets/icon_close.png");

        if (url != null) {

            System.out.println("ICON FOUND: " + url);

            // Load image TANPA getScaledInstance
            iconImg = new ImageIcon(url).getImage();

        } else {

            System.out.println("ICON NOT FOUND!");

        }

    } catch (Exception e) {
        e.printStackTrace();
    }

    final Image finalIcon = iconImg;

    JButton btn = new JButton() {

        private boolean hov = false;

        {
            addMouseListener(new MouseAdapter() {

                @Override
                public void mouseEntered(MouseEvent e) {
                    hov = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hov = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {

            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            g2.setRenderingHint(
                    RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR
            );

            int w = getWidth();
            int h = getHeight();

            // Background gradient
            g2.setPaint(new GradientPaint(
                    0, 0,
                    hov
                            ? new Color(160, 40, 40)
                            : new Color(100, 25, 25),

                    0, h,

                    hov
                            ? new Color(200, 60, 60)
                            : new Color(130, 30, 30)
            ));

            g2.fillRoundRect(0, 0, w, h, 8, 8);

            // Border
            if (hov) {

                g2.setColor(new Color(255, 100, 100, 120));
                g2.setStroke(new BasicStroke(2f));

            } else {

                g2.setColor(new Color(180, 60, 60, 80));
                g2.setStroke(new BasicStroke(1f));

            }

            g2.drawRoundRect(1, 1, w - 3, h - 3, 8, 8);

            // Draw icon
            if (finalIcon != null) {

                int iw = 14;
                int ih = 14;

                g2.drawImage(
                        finalIcon,
                        (w - iw) / 2,
                        (h - ih) / 2,
                        iw,
                        ih,
                        null
                );

            } else {

                // Fallback text
                g2.setFont(new Font("Arial", Font.BOLD, 13));

                g2.setColor(
                        hov
                                ? Color.WHITE
                                : new Color(220, 120, 120)
                );

                String text = "✕";

                FontMetrics fm = g2.getFontMetrics();

                g2.drawString(
                        text,
                        (w - fm.stringWidth(text)) / 2,
                        h / 2 + fm.getAscent() / 2 - 2
                );
            }

            g2.dispose();
        }
    };

    btn.setContentAreaFilled(false);
    btn.setBorderPainted(false);
    btn.setFocusPainted(false);
    btn.setFocusable(false);

    btn.setToolTipText("Keluar dari game");

    btn.setCursor(
            Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
    );

    btn.addActionListener(e -> System.exit(0));

    return btn;
}

    private JLabel makeLabel(String text, int size, int style, Color color, int align) {
        JLabel l = new JLabel(text, align);
        l.setFont(new Font("Serif", style, size));
        l.setForeground(color);
        return l;
    }

    private JPanel makeSep(int x, int y, int w, int h) {
        JPanel sep = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g;
                g2.setPaint(new GradientPaint(0,0,new Color(100,70,20,0),getWidth()/2,0,new Color(220,160,40,170)));
                g2.fillRect(0,0,getWidth()/2,getHeight());
                g2.setPaint(new GradientPaint(getWidth()/2,0,new Color(220,160,40,170),getWidth(),0,new Color(100,70,20,0)));
                g2.fillRect(getWidth()/2,0,getWidth(),getHeight());
            }
        };
        sep.setOpaque(false); sep.setBounds(x,y,w,h);
        return sep;
    }

    // ── MAIN ──────────────────────────────────────────────────────────────
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AuthScreen().setVisible(true));
    }
}