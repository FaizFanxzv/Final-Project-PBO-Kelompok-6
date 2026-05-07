package GUI;

import Database.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * AuthScreen v2 — Layar Login & Register.
 *
 * FITUR:
 *  - Toggle panel Login ↔ Register
 *  - JOptionPane saat register berhasil (selamat datang + info unlock)
 *  - JOptionPane saat login berhasil (sambutan + stats)
 *  - Animasi shake pada error
 *  - Backsound BGM
 *  - Zero external dependency (pakai DatabaseManager berbasis file)
 */
public class AuthScreen extends JFrame {

    // ── Panel swap ────────────────────────────────────────────────────────
    private JPanel  cardContainer;

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

    // ═══════════════════════════════════════════════════════════════════════
    public AuthScreen() {
        setTitle("LAST CHANCE FOR LIFE — Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        setUndecorated(true);

        try {
            java.net.URL url = getClass().getResource(AssetConfig.BG_PREGAME);
            if (url != null) bgImage = new ImageIcon(url).getImage();
        } catch (Exception ignored) {}

        JPanel root = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawBackground((Graphics2D) g);
            }
        };
        root.setPreferredSize(new Dimension(620, 560));
        root.setBackground(new Color(8, 6, 18));

        // Tombol X
        root.add(makeCloseButton()).setBounds(584, 6, 28, 24);

        buildCenterCard(root);

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
        int W = 620, H = 560;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (bgImage != null) {
            g2.drawImage(bgImage, 0, 0, W, H, this);
            g2.setColor(new Color(4, 2, 12, 215));
            g2.fillRect(0, 0, W, H);
        } else {
            g2.setPaint(new GradientPaint(0, 0, new Color(6, 4, 16), W, H, new Color(22, 8, 32)));
            g2.fillRect(0, 0, W, H);
        }

        // Partikel bintang
        for (int i = 0; i < 80; i++) {
            int sx = (i * 137 + animFrame / 4) % W;
            int sy = (i * 79  + animFrame / 6) % H;
            float alpha = Math.max(0.05f, 0.3f + (float)(Math.sin(animFrame * 0.04 + i) * 0.35f));
            g2.setColor(new Color(1f, 1f, 1f, alpha));
            g2.fillOval(sx, sy, (i%5==0)?2:1, (i%5==0)?2:1);
        }

        // Garis emas atas
        g2.setPaint(new GradientPaint(0,0,new Color(100,70,0,0), W/2,0,new Color(255,200,50,190)));
        g2.fillRect(0,0,W/2,2);
        g2.setPaint(new GradientPaint(W/2,0,new Color(255,200,50,190), W,0,new Color(100,70,0,0)));
        g2.fillRect(W/2,0,W/2,2);

        // Garis emas bawah
        g2.setPaint(new GradientPaint(0,H-2,new Color(100,70,0,0), W/2,H-2,new Color(255,200,50,120)));
        g2.fillRect(0,H-2,W/2,2);
        g2.setPaint(new GradientPaint(W/2,H-2,new Color(255,200,50,120), W,H-2,new Color(100,70,0,0)));
        g2.fillRect(W/2,H-2,W/2,2);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // BUILD UI
    // ═══════════════════════════════════════════════════════════════════════

    private void buildCenterCard(JPanel root) {
        // Kartu tengah
        JPanel card = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(10, 7, 22, 235));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 22, 22);
                g2.setColor(new Color(150, 110, 30, 110));
                g2.setStroke(new BasicStroke(1.8f));
                g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 22, 22);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBounds(75, 60, 470, 460);
        root.add(card);

        // ── Logo ──────────────────────────────────────────────────────────
        JLabel logo = new JLabel("⚔  LAST CHANCE FOR LIFE  ⚔", SwingConstants.CENTER) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setFont(new Font("Serif", Font.BOLD, 21));
                String t = getText();
                FontMetrics fm = g2.getFontMetrics();
                int tx = (getWidth() - fm.stringWidth(t)) / 2;
                // Glow
                for (int r = 6; r > 0; r -= 2) {
                    g2.setColor(new Color(200, 150, 0, 35));
                    g2.drawString(t, tx - r/2 + 1, fm.getAscent() + r/2 + 1);
                }
                g2.setColor(new Color(0,0,0,160));
                g2.drawString(t, tx+2, fm.getAscent()+2);
                g2.setColor(new Color(255, 210, 60));
                g2.drawString(t, tx, fm.getAscent());
                g2.dispose();
            }
        };
        logo.setBounds(0, 16, 470, 36);
        card.add(logo);

        JLabel sub = makeLabel("Petualangan Abadi Menantimu", 12, Font.ITALIC,
                new Color(160, 140, 100), SwingConstants.CENTER);
        sub.setBounds(0, 54, 470, 18);
        card.add(sub);

        card.add(makeSep(35, 76, 400, 2));

        // ── Tab ───────────────────────────────────────────────────────────
        buildTabBar(card);

        // ── CardContainer ─────────────────────────────────────────────────
        cardContainer = new JPanel(new CardLayout());
        cardContainer.setOpaque(false);
        cardContainer.setBounds(25, 138, 420, 290);
        card.add(cardContainer);

        cardContainer.add(buildLoginPanel(),    "LOGIN");
        cardContainer.add(buildRegisterPanel(), "REGISTER");

        // ── Footer hint ───────────────────────────────────────────────────
        JLabel hint = makeLabel("Data tersimpan di folder  data/  dalam direktori project",
                10, Font.ITALIC, new Color(120, 110, 80), SwingConstants.CENTER);
        hint.setBounds(0, 435, 470, 16);
        card.add(hint);

        showLogin();
    }

    // ── Tab Bar ──────────────────────────────────────────────────────────

    private void buildTabBar(JPanel card) {
        JPanel bar = new JPanel(null);
        bar.setOpaque(false);
        bar.setBounds(35, 88, 400, 42);
        card.add(bar);

        tabLogin    = makeTabButton("🗡  Login");
        tabRegister = makeTabButton("✍  Daftar Baru");
        tabLogin.setBounds(0, 0, 200, 42);
        tabRegister.setBounds(200, 0, 200, 42);

        tabLogin.addActionListener(e -> showLogin());
        tabRegister.addActionListener(e -> showRegister());

        bar.add(tabLogin);
        bar.add(tabRegister);
    }

    private JButton makeTabButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean active = Boolean.TRUE.equals(getClientProperty("active"));
                g2.setColor(active ? new Color(90,65,12) : new Color(18,13,30));
                g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);
                g2.setFont(new Font("Serif", Font.BOLD, 15));
                g2.setColor(active ? new Color(255,210,60) : new Color(150,130,90));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),(getWidth()-fm.stringWidth(getText()))/2,
                        getHeight()/2+fm.getAscent()/2-2);
                if (active) {
                    g2.setColor(new Color(255,200,50));
                    g2.fillRect(6, getHeight()-3, getWidth()-12, 3);
                }
                g2.dispose();
            }
        };
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setFocusable(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ── Login Panel ──────────────────────────────────────────────────────

    private JPanel buildLoginPanel() {
        JPanel p = new JPanel(null);
        p.setOpaque(false);

        addLabel(p, "👤  Username", 0, 0, 200);
        loginUserField = makeTextField();
        loginUserField.setBounds(0, 22, 420, 40);
        p.add(loginUserField);

        addLabel(p, "🔑  Password", 0, 74, 200);
        loginPassField = makePasswordField();
        loginPassField.setBounds(0, 96, 420, 40);
        p.add(loginPassField);

        loginErrLabel = makeLabel("", 11, Font.ITALIC, new Color(230,70,70), SwingConstants.CENTER);
        loginErrLabel.setBounds(0, 148, 420, 18);
        p.add(loginErrLabel);

        JButton btnLogin = makeActionButton("⚔  MASUK KE GAME", new Color(130,90,15), new Color(190,140,25));
        btnLogin.setBounds(0, 170, 420, 50);
        p.add(btnLogin);

        // Register shortcut
        JLabel switchLabel = makeLabel("Belum punya akun?  → Klik tab 'Daftar Baru' di atas",
                11, Font.ITALIC, new Color(140,160,200), SwingConstants.CENTER);
        switchLabel.setBounds(0, 232, 420, 18);
        p.add(switchLabel);

        loginPassField.addActionListener(e -> doLogin());
        btnLogin.addActionListener(e -> doLogin());
        return p;
    }

    // ── Register Panel ───────────────────────────────────────────────────

    private JPanel buildRegisterPanel() {
        JPanel p = new JPanel(null);
        p.setOpaque(false);

        addLabel(p, "👤  Username  (hanya huruf, angka, _)", 0, 0, 350);
        regUserField = makeTextField();
        regUserField.setBounds(0, 22, 420, 40);
        p.add(regUserField);

        addLabel(p, "🔑  Password  (minimal 4 karakter)", 0, 72, 320);
        regPassField = makePasswordField();
        regPassField.setBounds(0, 94, 420, 40);
        p.add(regPassField);

        addLabel(p, "🔑  Konfirmasi Password", 0, 146, 280);
        regPassConfField = makePasswordField();
        regPassConfField.setBounds(0, 168, 420, 40);
        p.add(regPassConfField);

        regErrLabel = makeLabel("", 11, Font.ITALIC, new Color(230,70,70), SwingConstants.CENTER);
        regErrLabel.setBounds(0, 216, 420, 18);
        p.add(regErrLabel);

        JButton btnReg = makeActionButton("✍  BUAT AKUN SEKARANG", new Color(28,90,45), new Color(45,150,70));
        btnReg.setBounds(0, 238, 420, 50);
        p.add(btnReg);

        regPassConfField.addActionListener(e -> doRegister());
        btnReg.addActionListener(e -> doRegister());
        return p;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // AKSI LOGIN & REGISTER
    // ═══════════════════════════════════════════════════════════════════════

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

    // ── Dokan Login ──────────────────────────────────────────────────────

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

        // ── JOptionPane: Login berhasil ──────────────────────────────────
        String statsMsg = String.format(
            "Game dimainkan : %d\n" +
            "Total kemenangan: %d\n" +
            "Wave tertinggi  : %d\n\n" +
            "Selamat berjuang, %s!",
            gp.gamesCompleted, gp.totalWins, gp.highestWave,
            DatabaseManager.getInstance().getDisplayName(uid)
        );
        showThemedDialog(
            "✅  Login Berhasil!",
            "Selamat datang kembali, " + DatabaseManager.getInstance().getDisplayName(uid) + "!\n\n"
            + "📊 Statistikmu:\n" + statsMsg,
            JOptionPane.INFORMATION_MESSAGE
        );

        openPreGame();
    }

    // ── Dokan Register ───────────────────────────────────────────────────

    private void doRegister() {
        regErrLabel.setText("");
        String user     = regUserField.getText().trim();
        String pass     = new String(regPassField.getPassword());
        String passConf = new String(regPassConfField.getPassword());

        // Validasi
        if (user.isEmpty())          { shake(regErrLabel, "Username tidak boleh kosong."); return; }
        if (user.length() < 3)       { shake(regErrLabel, "Username minimal 3 karakter."); return; }
        if (!user.matches("[a-zA-Z0-9_]+")) {
            shake(regErrLabel, "Username hanya boleh huruf, angka, dan '_'.");
            return;
        }
        if (pass.length() < 4)       { shake(regErrLabel, "Password minimal 4 karakter."); return; }
        if (!pass.equals(passConf))  { shake(regErrLabel, "Konfirmasi password tidak cocok."); return; }

        boolean ok = DatabaseManager.getInstance().register(user, pass);
        if (!ok) {
            shake(regErrLabel, "❌ Username '" + user + "' sudah digunakan.");
            return;
        }

        // Auto-login
        int uid = DatabaseManager.getInstance().login(user, pass);
        SessionManager.getInstance().login(uid, user);

        // ── JOptionPane: Register berhasil ───────────────────────────────
        String displayName = DatabaseManager.getInstance().getDisplayName(uid);
        showThemedDialog(
            "🎉  Akun Berhasil Dibuat!",
            "Selamat datang, " + displayName + "!\n\n" +
            "Perjalananmu dimulai sekarang!\n\n" +
            "🔓  Yang langsung terrsedia:\n" +
            "    • Karakter : Knight Prince\n" +
            "    • Map      : Hutan Terlarang (Forest)\n\n" +
            "🔒  Cara membuka konten lain:\n" +
            "    • Selesaikan Forest Wave 5+  → Buka Unesa Boys/Girls\n" +
            "    • Selesaikan Forest Wave 10  → Buka SEMUA!\n\n" +
            "Semangat berjuang! ⚔",
            JOptionPane.INFORMATION_MESSAGE
        );

        openPreGame();
    }

    // ── Pindah ke PreGame ────────────────────────────────────────────────

    private void openPreGame() {
        animTimer.stop();
        SoundManager.stopBGM();
        this.dispose();
        SwingUtilities.invokeLater(() -> new PreGameScreen().setVisible(true));
    }

    // ── JOptionPane styled ───────────────────────────────────────────────

    private void showThemedDialog(String title, String message, int type) {
        UIManager.put("OptionPane.background",       new Color(14, 10, 28));
        UIManager.put("Panel.background",            new Color(14, 10, 28));
        UIManager.put("OptionPane.messageForeground", new Color(220, 200, 150));
        UIManager.put("OptionPane.messageFont",      new Font("Serif", Font.PLAIN, 14));
        UIManager.put("Button.background",           new Color(90, 65, 12));
        UIManager.put("Button.foreground",           new Color(255, 210, 60));
        UIManager.put("Button.font",                 new Font("Serif", Font.BOLD, 13));

        JOptionPane.showMessageDialog(this, message, title, type);

        // Reset UIManager agar tidak mengganggu komponen lain
        UIManager.put("OptionPane.background",       null);
        UIManager.put("Panel.background",            null);
        UIManager.put("OptionPane.messageForeground", null);
        UIManager.put("Button.background",           null);
        UIManager.put("Button.foreground",           null);
    }

    // ── Shake error ──────────────────────────────────────────────────────

    private void shake(JLabel label, String msg) {
        label.setText(msg);
        final int origX = label.getX();
        final int[] count = {0};
        Timer t = new Timer(40, null);
        t.addActionListener(e -> {
            int off = (count[0] % 2 == 0) ? 6 : -6;
            label.setBounds(origX + off, label.getY(), label.getWidth(), label.getHeight());
            if (++count[0] >= 10) {
                label.setBounds(origX, label.getY(), label.getWidth(), label.getHeight());
                t.stop();
            }
        });
        t.start();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // WIDGET HELPERS
    // ═══════════════════════════════════════════════════════════════════════

    private void addLabel(JPanel p, String text, int x, int y, int w) {
        JLabel l = makeLabel(text, 12, Font.BOLD, new Color(200, 170, 80), SwingConstants.LEFT);
        l.setBounds(x, y, w, 20);
        p.add(l);
    }

    private JTextField makeTextField() {
        JTextField tf = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(16, 11, 30));
                g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10);
                boolean foc = isFocusOwner();
                g2.setColor(foc ? new Color(200,160,40,220) : new Color(90,70,25,140));
                g2.setStroke(new BasicStroke(foc ? 2f : 1.3f));
                g2.drawRoundRect(1,1,getWidth()-3,getHeight()-3,10,10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        tf.setOpaque(false);
        tf.setForeground(new Color(230,210,160));
        tf.setCaretColor(new Color(255,200,50));
        tf.setFont(new Font("Serif", Font.PLAIN, 15));
        tf.setBorder(BorderFactory.createEmptyBorder(6,12,6,12));
        return tf;
    }

    private JPasswordField makePasswordField() {
        JPasswordField pf = new JPasswordField() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(16, 11, 30));
                g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10);
                boolean foc = isFocusOwner();
                g2.setColor(foc ? new Color(200,160,40,220) : new Color(90,70,25,140));
                g2.setStroke(new BasicStroke(foc ? 2f : 1.3f));
                g2.drawRoundRect(1,1,getWidth()-3,getHeight()-3,10,10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        pf.setOpaque(false);
        pf.setForeground(new Color(230,210,160));
        pf.setCaretColor(new Color(255,200,50));
        pf.setFont(new Font("Arial", Font.PLAIN, 15));
        pf.setBorder(BorderFactory.createEmptyBorder(6,12,6,12));
        return pf;
    }

    private JButton makeActionButton(String text, Color c1, Color c2) {
        JButton btn = new JButton(text) {
            private boolean hov = false;
            { addMouseListener(new MouseAdapter(){
                public void mouseEntered(MouseEvent e){ hov=true; repaint(); }
                public void mouseExited(MouseEvent e) { hov=false; repaint(); }
            }); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w=getWidth(), h=getHeight();
                g2.setPaint(new GradientPaint(0,0,hov?c1.brighter():c1, 0,h,hov?c2.brighter():c2));
                g2.fillRoundRect(0,0,w,h,12,12);
                if (hov) {
                    g2.setColor(new Color(255,255,255,55));
                    g2.setStroke(new BasicStroke(2f));
                    g2.drawRoundRect(1,1,w-3,h-3,12,12);
                }
                g2.setFont(new Font("Serif", Font.BOLD, 17));
                g2.setColor(Color.WHITE);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),(w-fm.stringWidth(getText()))/2, h/2+fm.getAscent()/2-3);
                g2.dispose();
            }
        };
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setFocusable(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton makeCloseButton() {
        JButton btn = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setColor(new Color(80,28,28));
                g2.fillRoundRect(0,0,getWidth(),getHeight(),6,6);
                g2.setFont(new Font("Arial",Font.BOLD,13));
                g2.setColor(new Color(210,100,100));
                FontMetrics fm=g2.getFontMetrics();
                g2.drawString("✕",(getWidth()-fm.stringWidth("✕"))/2,getHeight()/2+fm.getAscent()/2-2);
                g2.dispose();
            }
        };
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setFocusable(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
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
        sep.setOpaque(false);
        sep.setBounds(x,y,w,h);
        return sep;
    }

    // ── MAIN ─────────────────────────────────────────────────────────────
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AuthScreen().setVisible(true));
    }
}