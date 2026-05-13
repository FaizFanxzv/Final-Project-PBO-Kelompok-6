package GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * StoryScreen — Dialog narasi cerita game.
 *
 * Menampilkan teks cerita dengan:
 *  - Efek typewriter (huruf muncul satu per satu)
 *  - Auto-scroll mengikuti baris aktif
 *  - Background animasi bintang
 *  - Klik untuk skip ke akhir teks
 *
 * Ditampilkan di:
 *  - STORY_NEW_GAME     : pertama kali register akun baru
 *  - STORY_FOREST_WIN   : setelah menang di Map Forest
 *  - STORY_UNESA_WIN    : setelah menang di Map Unesa
 *  - STORY_FROZEN_WIN   : setelah menang di Map Frozen
 *  - STORY_MOUNTAIN_WIN : setelah menang di Map Mountain (+ credits)
 */
public class StoryScreen extends JDialog {

    // ── ID cerita ─────────────────────────────────────────────────────────
    public static final int STORY_NEW_GAME      = 0;
    public static final int STORY_FOREST_WIN    = 1;
    public static final int STORY_UNESA_WIN     = 2;
    public static final int STORY_FROZEN_WIN    = 3;
    public static final int STORY_MOUNTAIN_WIN  = 4;

    private static final int LINE_H = 23;

    // ── Konten cerita ─────────────────────────────────────────────────────
    private static final String[][] STORIES = {

        // ── 0: NEW GAME INTRO ─────────────────────────────────────────────
        {
            "◈  LAST CHANCE FOR LIFE  ◈",
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
            "",
            "Tahun 2047. Dunia tidak lagi mengenal kedamaian.",
            "",
            "Sebuah virus misterius bocor dari laboratorium rahasia",
            "dan menyebar ke seluruh dunia hanya dalam 72 jam.",
            "Jutaan manusia berubah menjadi zombie haus darah.",
            "Kota-kota runtuh. Pemerintahan hancur. Peradaban musnah.",
            "",
            "Di tengah kehancuran ini, sekelompok pejuang muda bangkit.",
            "Mereka berasal dari berbagai penjuru, berbeda latar belakang.",
            "Namun satu tekad menyatukan mereka:",
            "",
            "  Menghentikan wabah ini. Sebelum semua berakhir.",
            "",
            "Mereka menyebut diri mereka — LAST CHANCE.",
            "Harapan terakhir umat manusia.",
            "",
            "Kamu adalah salah satu dari mereka.",
            "Petualanganmu dimulai dari kegelapan Hutan Terlarang...",
            "",
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
            "    ⚔  Bersiaplah. Pertarungan dimulai sekarang.  ⚔",
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
        },

        // ── 1: SETELAH MENANG FOREST ──────────────────────────────────────
        {
            "🌲  HUTAN TERLARANG — DIBERSIHKAN  🌲",
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
            "",
            "Zombie-zombie di Hutan Terlarang akhirnya tumbang.",
            "Keheningan kembali menyelimuti pepohonan yang hangus.",
            "",
            "Di antara puing dan abu, kamu menemukan sebuah radio rusak.",
            "Dengan susah payah, radio itu menyala —",
            "dan memutar satu pesan terakhir yang tersimpan di memorinya:",
            "",
            "  \"Kepada siapapun yang mendengar ini...\"",
            "  \"Kami berlindung di Kampus Unesa, Surabaya.\"",
            "  \"Para ilmuwan kami sedang mengembangkan antidot.\"",
            "  \"Tapi zombie semakin banyak dan semakin kuat.\"",
            "  \"Tolong... datangi kami sebelum terlambat.\"",
            "               — Dr. Putri, Peneliti Virologi Unesa",
            "",
            "Antidot! Inilah harapan yang selama ini dicari.",
            "Kamu harus segera menuju Kampus Unesa.",
            "Tapi para zombie telah mengepung seluruh area kampus...",
            "",
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
            "       Tujuan berikutnya: Kampus Unesa, Surabaya.",
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
        },

        // ── 2: SETELAH MENANG UNESA ───────────────────────────────────────
        {
            "🏫  KAMPUS UNESA — DIBEBASKAN  🏫",
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
            "",
            "Kampus Unesa akhirnya bebas dari invasi zombie!",
            "Para ilmuwan selamat setelah berminggu-minggu terkurung.",
            "",
            "Dr. Putri menyambutmu dengan air mata bercucuran.",
            "  \"Tanpamu, kami sudah tidak bisa bertahan malam ini...\"",
            "",
            "Ia membuka peta tua di atas meja dan menunjukkan sebuah titik.",
            "  \"Antidot hampir selesai. Tapi kami butuh satu bahan terakhir —\"",
            "  \"Kristal Beku dari Tundra Utara.\"",
            "",
            "  \"Suhu di sana bisa membekukan tulangmu dalam hitungan menit.\"",
            "  \"Zombie di sana pun berbeda — mereka bermutasi karena hawa beku.\"",
            "  \"Tidak ada yang pernah berhasil kembali dari sana.\"",
            "",
            "Kamu mengambil peta itu tanpa berkata apa-apa.",
            "Tidak ada pilihan lain jika ingin menyelamatkan dunia.",
            "Demi antidot yang akan mengakhiri semuanya.",
            "",
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
            "         Tujuan berikutnya: Tundra Beku, Utara.",
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
        },

        // ── 3: SETELAH MENANG FROZEN ──────────────────────────────────────
        {
            "❄  TUNDRA BEKU — DITAKLUKKAN  ❄",
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
            "",
            "Kristal Beku berhasil kamu raih dari jantung tundra!",
            "Tanganmu gemetar kedinginan, tapi matamu menyala penuh tekad.",
            "",
            "Tiba-tiba, langit gelap dibelah oleh tawa yang mengerikan.",
            "",
            "  \"HAHAHA! Kamu pikir antidot bisa menghentikanku?!\"",
            "",
            "Dari balik awan hitam, sosok raksasa melayang turun.",
            "ZOMBOSS — sang pencipta virus, arsitek kehancuran dunia.",
            "",
            "  \"Aku telah membangun pasukan terakhirku di Gunung Berapi!\"",
            "  \"Ketika gunung itu meletus, abunya akan menyebar ke atmosfer —\"",
            "  \"Dan setiap manusia yang menghirupnya akan BERUBAH selamanya!\"",
            "",
            "Zomboss menghilang ke balik awan dengan tawa membahana.",
            "",
            "Gunung Berapi harus dihentikan. SEKARANG.",
            "Sebelum manusia terakhir di bumi ini pun musnah.",
            "",
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
            "     Tujuan berikutnya: Gunung Berapi, Selatan.",
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
        },

        // ── 4: SETELAH MENANG MOUNTAIN + CREDITS ─────────────────────────
        {
            "🌋  GUNUNG BERAPI — DISELAMATKAN  🌋",
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
            "",
            "Pasukan terakhir Zomboss jatuh satu per satu!",
            "Letusan gunung berhasil dicegah pada detik terakhir.",
            "Dunia... masih bisa diselamatkan.",
            "",
            "Tapi dari kejauhan, sebuah portal gelap terbuka.",
            "Zomboss melangkah masuk, matanya merah membara.",
            "",
            "  \"Kamu memang kuat, manusia kecil...\"",
            "  \"Tapi ini hanyalah PEMBUKA dari rencanaku yang sesungguhnya!\"",
            "  \"Temui aku di Nerakaku — jika kamu berani!\"",
            "",
            "Portal itu menutup. Zomboss lenyap ke dimensi lain.",
            "Neraka Zomboss — domain abadi sang Penguasa Zombie.",
            "",
            "Ini adalah pertarungan terakhir.",
            "Ini adalah kesempatan terakhir.",
            "Ini adalah... LAST CHANCE sejatinya.",
            "",
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
            "",
            "               ✨  HALL OF FAME  ✨",
            "          Kamu telah mencapai babak final!",
            "       Hanya pemberani sejati yang sampai di sini.",
            "",
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
            "",
            "                  🏆  CREDITS  🏆",
            "",
            "     Game Concept & Story Design",
            "         Tim Pengembang LCFL",
            "",
            "     Character Design & Illustration",
            "         Divisi Kreatif Visual",
            "",
            "     Programming & Game Engine",
            "         Tim Backend & Logic",
            "",
            "     Music & Sound Effects",
            "         Tim Produksi Audio",
            "",
            "     Quality Assurance & Testing",
            "         Para Tester Setia",
            "",
            "     Special Thanks",
            "         Universitas Negeri Surabaya (UNESA)",
            "         Seluruh pemain yang mendukung game ini",
            "         Keluarga dan sahabat para developer",
            "",
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
            "           © 2024  LAST CHANCE FOR LIFE",
            "       Dibuat dengan ❤ di Surabaya, Indonesia",
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
            "",
            "         Satu pertempuran lagi menanti...",
            "        Siapkah kamu menghadapi ZOMBOSS?"
        }
    };

    // ── State ─────────────────────────────────────────────────────────────
    private final String[]  lines;
    private float displayedChars = 0;
    private float scrollOffset   = 0;
    private int   animFrame      = 0;
    private boolean done         = false;

    private final javax.swing.Timer typeTimer;
    private final javax.swing.Timer animTimer;

    private JButton btnContinue;
    private JPanel  canvas;

    // ════════════════════════════════════════════════════════════════════
    public StoryScreen(Window parent, int storyId) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);
        setUndecorated(true);

        int id = Math.max(0, Math.min(storyId, STORIES.length - 1));
        lines  = STORIES[id];

        buildUI();
        pack();
        setLocationRelativeTo(parent);
        getRootPane().setBorder(BorderFactory.createLineBorder(new Color(180, 130, 30), 2));

        typeTimer = new javax.swing.Timer(16, e -> tickType());
        typeTimer.start();

        animTimer = new javax.swing.Timer(16, e -> { animFrame++; canvas.repaint(); });
        animTimer.start();
    }

    // ── Build UI ──────────────────────────────────────────────────────────
    private void buildUI() {
        canvas = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawAll((Graphics2D) g);
            }
        };
        canvas.setPreferredSize(new Dimension(700, 580));
        canvas.setBackground(new Color(5, 3, 14));

        // ── Tombol Lanjutkan ─────────────────────────────────────────────
        btnContinue = new JButton("Lanjutkan  ▶") {
            private boolean hov = false;
            { addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { hov = true;  repaint(); }
                public void mouseExited (MouseEvent e) { hov = false; repaint(); }
            }); }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c1 = hov ? new Color(160, 120, 20) : new Color(120, 85, 15);
                Color c2 = hov ? new Color(200, 160, 30) : new Color(160, 110, 20);
                g2.setPaint(new GradientPaint(0, 0, c1, 0, getHeight(), c2));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(new Color(255, 215, 0, 200));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 10, 10);
                g2.setFont(new Font("Serif", Font.BOLD, 16));
                FontMetrics fm = g2.getFontMetrics();
                String t = getText();
                g2.setColor(Color.WHITE);
                g2.drawString(t, (getWidth()-fm.stringWidth(t))/2, getHeight()/2+fm.getAscent()/2-2);
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(200, 40); }
        };
        btnContinue.setBounds(250, 524, 200, 40);
        btnContinue.setContentAreaFilled(false);
        btnContinue.setBorderPainted(false);
        btnContinue.setFocusPainted(false);
        btnContinue.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnContinue.setVisible(false);
        btnContinue.addActionListener(e -> closeScreen());
        canvas.add(btnContinue);

        // Klik kanvas = skip ke akhir teks
        canvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!done) {
                    displayedChars = totalChars();
                    scrollOffset   = totalScrollTarget();
                    done = true;
                    btnContinue.setVisible(true);
                    canvas.repaint();
                }
            }
        });

        setContentPane(canvas);
    }

    // ── Tick typewriter ───────────────────────────────────────────────────
    private void tickType() {
        if (done) return;
        displayedChars += 2f;
        if (displayedChars >= totalChars()) {
            displayedChars = totalChars();
            done = true;
            btnContinue.setVisible(true);
        }
        // Hitung baris aktif untuk auto-scroll
        float counted = 0;
        int lineIdx   = 0;
        for (int i = 0; i < lines.length; i++) {
            counted += lines[i].length() + 1;
            if (counted >= displayedChars) { lineIdx = i; break; }
        }
        float target = Math.max(0f, lineIdx * LINE_H - 300f);
        scrollOffset += (target - scrollOffset) * 0.09f;

        canvas.repaint();
    }

    private float totalChars() {
        float t = 0;
        for (String l : lines) t += l.length() + 1;
        return t;
    }

    private float totalScrollTarget() {
        return Math.max(0, lines.length * LINE_H - 300f);
    }

    // ── Draw ──────────────────────────────────────────────────────────────
    private void drawAll(Graphics2D g2) {
        int W = canvas.getWidth(), H = canvas.getHeight();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,   RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Background
        g2.setPaint(new GradientPaint(0, 0, new Color(5,3,14), W, H, new Color(14,7,24)));
        g2.fillRect(0, 0, W, H);

        // Bintang animasi
        for (int i = 0; i < 80; i++) {
            int sx = (i*137 + animFrame/5) % W;
            int sy = (i*79  + animFrame/7) % H;
            float a = 0.12f + (float)(Math.sin(animFrame*0.04 + i) * 0.2f);
            g2.setColor(new Color(1f, 1f, 1f, Math.max(0.04f, a)));
            g2.fillOval(sx, sy, (i%6==0)?2:1, (i%6==0)?2:1);
        }

        // Garis emas atas
        g2.setPaint(new GradientPaint(0,0,new Color(100,70,0,0),W/2,0,new Color(255,200,50,180)));
        g2.fillRect(0,0,W/2,2);
        g2.setPaint(new GradientPaint(W/2,0,new Color(255,200,50,180),W,0,new Color(100,70,0,0)));
        g2.fillRect(W/2,0,W/2,2);
        // Garis emas bawah
        g2.setPaint(new GradientPaint(0,H-2,new Color(100,70,0,0),W/2,H-2,new Color(255,200,50,100)));
        g2.fillRect(0,H-2,W/2,2);
        g2.setPaint(new GradientPaint(W/2,H-2,new Color(255,200,50,100),W,H-2,new Color(100,70,0,0)));
        g2.fillRect(W/2,H-2,W/2,2);

        // ── Area konten teks (clipped) ────────────────────────────────────
        int contentTop = 18;
        int contentH   = H - 80;
        g2.setClip(0, contentTop, W, contentH);

        float charsRem = displayedChars;
        int   y        = contentTop + 18 - (int) scrollOffset;

        for (String line : lines) {
            if (charsRem <= 0) break;

            int   len     = line.length();
            String toShow = line.substring(0, (int) Math.min(charsRem, len));
            charsRem -= (len + 1);

            if (y >= contentTop - LINE_H && y <= contentTop + contentH + LINE_H) {
                if (!line.isEmpty()) drawStoryLine(g2, toShow, line, W, y);
            }
            y += LINE_H;
        }

        // Kursor berkedip
        if (!done && y <= contentTop + contentH + LINE_H) {
            if (animFrame % 28 < 14) {
                g2.setFont(new Font("Arial", Font.BOLD, 13));
                g2.setColor(new Color(255, 215, 0, 220));
                g2.fillRect(44, y - LINE_H - 8, 7, 13);
            }
        }
        g2.setClip(null);

        // Fade top/bottom untuk efek scroll
        g2.setPaint(new GradientPaint(0, contentTop, new Color(5,3,14,200), 0, contentTop+24, new Color(5,3,14,0)));
        g2.fillRect(0, contentTop, W, 24);
        g2.setPaint(new GradientPaint(0, contentTop+contentH-20, new Color(5,3,14,0), 0, contentTop+contentH, new Color(5,3,14,220)));
        g2.fillRect(0, contentTop+contentH-20, W, 20);

        // Hint klik
        if (!done) {
            g2.setFont(new Font("Arial", Font.ITALIC, 11));
            g2.setColor(new Color(150, 130, 80, 150));
            String hint = "[ Klik di mana saja untuk langsung ke akhir ]";
            FontMetrics fmH = g2.getFontMetrics();
            g2.drawString(hint, (W-fmH.stringWidth(hint))/2, H - 9);
        }
    }

    private void drawStoryLine(Graphics2D g2, String toShow, String fullLine, int W, int y) {
        Font  font;
        Color color;
        int   tx;

        if (fullLine.contains("━") || fullLine.contains("─")) {
            font  = new Font("Monospaced", Font.PLAIN, 12);
            color = new Color(180, 140, 40, 180);
            FontMetrics fm = g2.getFontMetrics(font);
            tx    = (W - fm.stringWidth(fullLine)) / 2;

        } else if (fullLine.contains("◈")  || fullLine.contains("🌲") ||
                   fullLine.contains("🏫") || fullLine.contains("❄")  ||
                   fullLine.contains("🌋") || fullLine.contains("🏆")) {
            font  = new Font("Serif", Font.BOLD, 18);
            color = new Color(255, 210, 50);
            FontMetrics fm = g2.getFontMetrics(font);
            tx    = (W - fm.stringWidth(fullLine)) / 2;

        } else if (fullLine.contains("✨") || fullLine.contains("©") ||
                   fullLine.contains("❤") || fullLine.contains("CREDITS") ||
                   fullLine.contains("HALL OF FAME")) {
            font  = new Font("Serif", Font.BOLD, 14);
            color = new Color(255, 200, 50);
            FontMetrics fm = g2.getFontMetrics(font);
            tx    = (W - fm.stringWidth(fullLine)) / 2;

        } else if (fullLine.startsWith("  \"") || fullLine.startsWith("               ")) {
            font  = new Font("Serif", Font.ITALIC, 13);
            color = new Color(200, 180, 140);
            tx    = 60;

        } else if (fullLine.startsWith("     ") && !fullLine.isBlank()) {
            font  = new Font("Arial", Font.PLAIN, 12);
            color = new Color(160, 200, 255);
            FontMetrics fm = g2.getFontMetrics(font);
            tx    = (W - fm.stringWidth(fullLine)) / 2;

        } else {
            font  = new Font("Serif", Font.PLAIN, 14);
            color = new Color(210, 200, 180);
            tx    = 40;
        }

        g2.setFont(font);
        // Bayangan
        g2.setColor(new Color(0, 0, 0, 120));
        g2.drawString(toShow, tx+1, y+1);
        // Teks
        g2.setColor(color);
        g2.drawString(toShow, tx, y);
    }

    // ── Tutup dialog ──────────────────────────────────────────────────────
    private void closeScreen() {
        typeTimer.stop();
        animTimer.stop();
        dispose();
    }

    // ── Static helper ─────────────────────────────────────────────────────
    /**
     * Tampilkan story dialog secara modal.
     * Memanggil blokir hingga user menutup dialog.
     */
    public static void show(Window parent, int storyId) {
        new StoryScreen(parent, storyId).setVisible(true);
    }
}