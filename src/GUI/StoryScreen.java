package GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * StoryScreen — Dialog narasi cerita game.
 *
 * PERUBAHAN:
 *  [FIX-2] Tambah onClose callback → setelah "Lanjutkan" ditekan, callback dijalankan
 *           (digunakan GamePanel untuk navigasi ke PreGameScreen setelah win-story).
 *  [FIX-5] Tambah scrolling manual (mouse wheel) + scrollbar visual di sisi kanan.
 *          - Auto-scroll typewriter berhenti saat user scroll manual.
 *          - Scrollbar muncul setelah teks selesai ditampilkan.
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
            "Kesempatan Terakhir untuk HIDUP",
            "",
            "Tahun 2047. Dunia tidak lagi mengenal kedamaian.",
            "",
            "Awal mula dari Zomboss yang ingin mencoba menguasai bumi",
            "dan mulai menyebarkan pasukan zombienya ke seluruh dunia dengan cepat.",
            "Jutaan manusia berubah menjadi zombie haus darah.",
            "Kota-kota runtuh. Pemerintahan hancur. Peradaban musnah.",
            "",
            "Di tengah kehancuran ini, sekelompok pejuang muda bangkit.",
            "Mereka berasal dari berbagai penjuru, berbeda latar belakang.",
            "Namun satu tekad menyatukan mereka:",
            "",
            "  Menghentikan wabah ini. Sebelum semuanya berakhir.",
            "",
            "Mereka menyebut diri mereka — LAST CHANCE.",
            "Harapan terakhir umat manusia.",
            "",
            "Kamu adalah salah satu dari mereka.",
            "Petualanganmu dimulai dari kegelapan di Hutan Terlarang...",
            "Knight Prince adalah Pangeran kerajaan yang telah gugur sedari lama efek zombie",
            "Kini , Ia ingin membalaskan dendamnya dan siap berjuang mempertahankan Hutan Tercintanya itu.",
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
            "Knight Prince merasa lega dan mulai menjadi pengembara",
            "",
            "Di antara puing dan abu, ditemukan sebuah radio rusak.",
            "Dengan susah payah, radio itu tiba tiba menyala —",
            "dan memutar satu pesan terakhir yang tersimpan di memorinya:",
            "",
            "  \"Kepada siapapun yang mendengar ini...\"",
            "  \"Kami berlindung di Kampus Unesa, Surabaya.\"",
            "  \"Zombie zombie sudah mengepung wilayah ini.\"",
            "  \"Para peneliti yang mengkaji virus ini satu persatu berhasil ditumbangkan.\"",
            "  \"Sungguh, Zombie disini semakin brutal.\"",
            "  \"Tolong kami... datanglah secepatnya sebelum tempat ini musnah.\"",
            "               — Dr. Sonjibar, Peneliti sekaligus Dosen PTI Unesa",
            "",
            "penelitain itu! ialah harapan yang selama ini dicari.",
            "jawaban untuk memberantas para zombie zombie di muka bumi",
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
            "Para peneliti yang tersisa selamat setelah berminggu-minggu terisolasi.",
            "",
            "Dr. Sonjibar menyambutmu dengan air mata yang bercucuran.",
            "  \"Tanpa kehadiranmu, kami sudah tidak bisa bertahan lebih lama lagi...\"",
            "",
            "Ia membuka hasil penelitian di dalam dokumen dan menunjukkan sebuah titik.",
            "  \"jawaban atas segalanya sudah ditemukan. Tapi kami butuh rekan —\"",
            "  \"Temuilah Raymond dari Tundra Utara.\"",
            "",
            "  \"Suhu di sana bisa membekukan tulangmu dalam hitungan menit.\"",
            "  \"Tapi dengan bantuan Raymond, semua akan baik baik saja.\"",
            "  \"Zombie di sana pun berbeda — mereka bermutasi karena hawa beku.\"",
            "  \"Tidak ada yang pernah berhasil kembali dari sana.\"",
            "",
            "Kamu mengambil peta itu tanpa berkata apa-apa.",
            "Tidak ada pilihan lain jika ingin menyelamatkan dunia.",
            "Demi sebuah Jawaban yang akan mengakhiri semuanya.",
            "",
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
            "         Tujuan berikutnya: Tundra Beku, Puncak Salju.",
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
        },

        // ── 3: SETELAH MENANG FROZEN ──────────────────────────────────────
        {
            "❄  TUNDRA BEKU — DITAKLUKKAN  ❄",
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
            "",
            "Kamu berhasil memberantas Zombie di Wilaya Tundra !",
            "Tanganmu gemetar kedinginan, tapi matamu menyala penuh tekad.",
            "",
            "Tiba-tiba, langit gelap dibelah oleh tawa yang mengerikan.",
            "",
            "  \"HAHAHA! Kamu pikir hanya dengan memberantas zombie disini kau bisa menghentikanku?!\"",
            "",
            "Dari balik awan hitam, sosok raksasa melayang turun.",
            "ialah Raja terakhir Boss para Zombie",
            "ZOMBOSS — sang pencipta virus, arsitek kehancuran dunia.",
            "",
            "  \"HEBAT JUGA KAMU ! Jangan harap bisa selamat !\"",
            "  \"Lihatlah di Pegunungan sana , Ketika gunung itu meletus, abunya akan menyebar ke atmosfer —\"",
            "  \"Dan setiap manusia yang menghirupnya akan BERUBAH selamanya! HAHAHAHA \"",
            "",
            "Zomboss menghilang ke balik awan dengan tawa membahana.",
            "Zomboss itu abadi , butuh sebuah trik mengalahkannya. Ini adalah kesempatan terakhir !",
            "",
            "Sebentar ! Mountain disaa berbahaya ! Jangkauan Zombie lebih luas dan lebih brutal",
            "Raymond : Xavier penduduk asli sana , tapi keangkuhannya berbeda dengan diriku",
            "Xavier tidak peduli apapun yang terjadi dengan bumi dan siapapun. pasti disaat dirinya dalam bahaya",
            "Disitulah dia akan berpihak pada kita",
            "",
            "CEPAT !",
            "Pegunungan itu shearusnya pasif jadi harus dihentikan. SEKARANG.",
            "Sebelum manusia yang tersisa di bumi ini pun ikut musnah.",
            "",
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
            "     Tujuan berikutnya: Mountain, Pegunungan pasif.",
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
        },

        // ── 4: SETELAH MENANG MOUNTAIN + CREDITS ─────────────────────────
        {
            "🌋  MOUNTAIN — DISELAMATKAN  🌋",
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
            "",
            "Pasukan terakhir Zomboss jatuh satu per satu!",
            "Letusan gunung berhasil dicegah pada detik terakhir.",
            "Dunia... masih bisa diselamatkan.",
            "",
            "Tapi dari kejauhan, sebuah portal gelap terbuka.",
            "Zomboss melangkah masuk, matanya merah membara.",
            "",
            "  \"Kamu memang kuat, tapi masih LEMAH...\"",
            "  \"Tapi, ini hanyalah PEMBUKA dari rencanaku yang sesungguhnya!\"",
            "  \"Temui aku di Nerakaku — jika kamu berani! HAHAHAHAHAHA \"",
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
            "         Kelompok 6 PTI 2025D Comelzz",
            "",
            "     Character Design & Illustration Animation",
            "         Arafina Aazahra",
            "",
            "     Programming & Game Logic",
            "         Muhammad Faiz Risqullah Ramadhan",
            "",
            "     Music & Sound Effects , Combat System",
            "         Varsaretha Najmi R",
            "",
            "     UI/UX Scene Management & Testing",
            "         Ahmad Khadik Mustawan'alwi",
            "",
            "     Logic Program Flow",
            "         Bunga Aulia Maharani",
            "",
            "     Special Thanks",
            "         Dosen Matkul PBO PTI Unesa , Bpk. Mohammad Sonhaji Akbar, S.Pd.,M.Kom.",
            "         Universitas Negeri Surabaya (UNESA)",
            "         Seluruh pemain yang mendukung game ini",
            "         Keluarga dan sahabat para developer",
            "",
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
            "           © 2026  LAST CHANCE FOR LIFE",
            "       Dikembangkan di Surabaya, Indonesia",
            "       Dibuat dengan ❤️ — Semoga dunia tidak benar-benar diserang zombie ",
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
            "",
            "         Satu pertempuran lagi menanti...",
            "        Siapkah kamu menghadapi ZOMBOSS di Markasnya ?"
        }
    };

    // ── State ─────────────────────────────────────────────────────────────
    private final String[]  lines;
    private float displayedChars = 0;
    private float scrollOffset   = 0;
    private int   animFrame      = 0;
    private boolean done         = false;

    // [FIX-5] Flag: apakah user sudah scroll manual (matikan auto-scroll)
    private boolean userScrolled = false;

    // [FIX-2] Callback setelah dialog ditutup
    private Runnable onClose;

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
                    userScrolled = false; // reset so we land at bottom
                    btnContinue.setVisible(true);
                    canvas.repaint();
                }
            }
        });

        // [FIX-5] Mouse wheel scrolling
        canvas.addMouseWheelListener(e -> {
            userScrolled = true;
            float delta = e.getWheelRotation() * LINE_H * 1.8f;
            scrollOffset += delta;
            float maxScroll = computeMaxScroll();
            scrollOffset = Math.max(0f, Math.min(scrollOffset, maxScroll));
            canvas.repaint();
        });

        // Keyboard scrolling (Arrow keys) when canvas is focused
        canvas.setFocusable(true);
        canvas.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                float maxScroll = computeMaxScroll();
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_DOWN:
                    case KeyEvent.VK_PAGE_DOWN:
                        userScrolled = true;
                        scrollOffset = Math.min(scrollOffset + LINE_H * 3, maxScroll);
                        canvas.repaint(); break;
                    case KeyEvent.VK_UP:
                    case KeyEvent.VK_PAGE_UP:
                        userScrolled = true;
                        scrollOffset = Math.max(0, scrollOffset - LINE_H * 3);
                        canvas.repaint(); break;
                    case KeyEvent.VK_END:
                        userScrolled = false;
                        scrollOffset = maxScroll;
                        if (!done) {
                            displayedChars = totalChars();
                            done = true;
                            btnContinue.setVisible(true);
                        }
                        canvas.repaint(); break;
                }
            }
        });

        setContentPane(canvas);
        SwingUtilities.invokeLater(() -> canvas.requestFocusInWindow());
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

        // [FIX-5] Auto-scroll hanya jika user belum scroll manual
        if (!userScrolled) {
            float counted = 0;
            int lineIdx   = 0;
            for (int i = 0; i < lines.length; i++) {
                counted += lines[i].length() + 1;
                if (counted >= displayedChars) { lineIdx = i; break; }
            }
            float target = Math.max(0f, lineIdx * LINE_H - 300f);
            scrollOffset += (target - scrollOffset) * 0.09f;
        }

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

    private float computeMaxScroll() {
        int contentH = canvas.getHeight() - 80;
        return Math.max(0f, lines.length * LINE_H - contentH + 36f);
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
        g2.setClip(0, contentTop, W - 16, contentH); // leave space for scrollbar

        float charsRem = displayedChars;
        int   y        = contentTop + 18 - (int) scrollOffset;

        for (String line : lines) {
            if (charsRem <= 0) break;

            int   len     = line.length();
            String toShow = line.substring(0, (int) Math.min(charsRem, len));
            charsRem -= (len + 1);

            if (y >= contentTop - LINE_H && y <= contentTop + contentH + LINE_H) {
                if (!line.isEmpty()) drawStoryLine(g2, toShow, line, W - 16, y);
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

        // Fade top/bottom
        g2.setPaint(new GradientPaint(0, contentTop, new Color(5,3,14,200), 0, contentTop+24, new Color(5,3,14,0)));
        g2.fillRect(0, contentTop, W, 24);
        g2.setPaint(new GradientPaint(0, contentTop+contentH-20, new Color(5,3,14,0), 0, contentTop+contentH, new Color(5,3,14,220)));
        g2.fillRect(0, contentTop+contentH-20, W, 20);

        // [FIX-5] Scrollbar visual di sisi kanan
        drawScrollbar(g2, contentTop, contentH, W);

        // Hint bawah
        g2.setFont(new Font("Arial", Font.ITALIC, 11));
        g2.setColor(new Color(150, 130, 80, 150));
        String hint;
        if (!done) {
            hint = "[ Klik di mana saja untuk langsung ke akhir ]";
        } else {
            hint = "[ ↑↓ / Scroll untuk membaca  |  Lanjutkan untuk melanjutkan ]";
        }
        FontMetrics fmH = g2.getFontMetrics();
        g2.drawString(hint, (W-fmH.stringWidth(hint))/2, H - 9);
    }

    // [FIX-5] Gambar scrollbar
    private void drawScrollbar(Graphics2D g2, int contentTop, int contentH, int W) {
        float maxScroll = computeMaxScroll();
        if (maxScroll <= 0) return;

        int sbX     = W - 10;
        int sbTrackH = contentH;

        // Track (latar scrollbar)
        g2.setColor(new Color(60, 50, 20, 80));
        g2.fillRoundRect(sbX, contentTop, 6, sbTrackH, 3, 3);

        // Thumb (indicator posisi)
        float visibleRatio = Math.min(1f, (float)(contentH) / (lines.length * LINE_H));
        int   thumbH       = Math.max(28, (int)(sbTrackH * visibleRatio));
        float scrollPct    = (maxScroll > 0) ? Math.min(1f, scrollOffset / maxScroll) : 0f;
        int   thumbY       = contentTop + (int)((sbTrackH - thumbH) * scrollPct);

        Color thumbColor = done ? new Color(255, 200, 50, 200) : new Color(180, 140, 40, 140);
        g2.setColor(thumbColor);
        g2.fillRoundRect(sbX, thumbY, 6, thumbH, 3, 3);

        // Arrow atas
        g2.setColor(new Color(200, 160, 50, 150));
        g2.setFont(new Font("Arial", Font.PLAIN, 9));
        g2.drawString("▲", sbX, contentTop + 10);
        g2.drawString("▼", sbX, contentTop + sbTrackH + 2);
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
        g2.setColor(new Color(0, 0, 0, 120));
        g2.drawString(toShow, tx+1, y+1);
        g2.setColor(color);
        g2.drawString(toShow, tx, y);
    }

    // ── Tutup dialog ──────────────────────────────────────────────────────
    private void closeScreen() {
        typeTimer.stop();
        animTimer.stop();
        dispose();
        // [FIX-2] Jalankan callback setelah dialog tertutup
        if (onClose != null) {
            SwingUtilities.invokeLater(onClose);
        }
    }

    // ── Static helpers ────────────────────────────────────────────────────

    /**
     * Tampilkan story dialog secara modal (tanpa callback).
     */
    public static void show(Window parent, int storyId) {
        new StoryScreen(parent, storyId).setVisible(true);
    }

    /**
     * [FIX-2] Tampilkan story dialog secara modal dengan callback setelah tutup.
     * Berguna saat GamePanel ingin navigasi ke PreGameScreen setelah win-story.
     */
    public static void show(Window parent, int storyId, Runnable onClose) {
        StoryScreen s = new StoryScreen(parent, storyId);
        s.onClose = onClose;
        s.setVisible(true);
    }
}