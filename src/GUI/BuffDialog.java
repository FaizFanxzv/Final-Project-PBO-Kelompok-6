package GUI;

import CharacterSettings.Player;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.Collections;

/**
 * BuffDialog v4 — Pop-up pemilihan buff.
 *
 * PERUBAHAN v4:
 *  [FIX-6] Notifikasi setelah memilih buff tidak lagi menggunakan
 *          JOptionPane biasa. Diganti dengan BuffResultDialog — dialog
 *          kustom bergaya dark-fantasy dengan animasi partikel emas,
 *          ikon buff, dan efek glow pada border.
 */
public class BuffDialog extends JDialog {

    public BuffDialog(Window parentWindow, Player p, ArrayList<String> listBuff) {
        super(parentWindow, "Pilih Kartu Keberuntunganmu!", Dialog.ModalityType.APPLICATION_MODAL);

        ArrayList<String> shuffled = new ArrayList<>(listBuff);
        Collections.shuffle(shuffled);
        int opsi = Math.min(3, shuffled.size());

        JPanel mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setPaint(new GradientPaint(0, 0, new Color(15, 15, 35),
                                              0, getHeight(), new Color(35, 10, 55)));
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JLabel title = new JLabel("⚔  Pilih Kekuatanmu  ⚔", SwingConstants.CENTER);
        title.setFont(new Font("Serif", Font.BOLD, 22));
        title.setForeground(new Color(255, 215, 0));
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));
        mainPanel.add(title, BorderLayout.NORTH);

        JPanel cardRow = new JPanel(new GridLayout(1, Math.max(opsi, 1), 14, 0));
        cardRow.setOpaque(false);

        if (opsi == 0) {
            JLabel empty = new JLabel("Semua buff telah diambil!", SwingConstants.CENTER);
            empty.setForeground(Color.LIGHT_GRAY);
            empty.setFont(new Font("Arial", Font.ITALIC, 15));
            cardRow.add(empty);
            this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        } else {
            this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
            for (int i = 0; i < opsi; i++) {
                final String nama = shuffled.get(i);
                BuffCard card = new BuffCard(nama, e -> {
                    terapkanBuff(p, nama, listBuff);
                    dispose();
                });
                cardRow.add(card);
            }
        }

        mainPanel.add(cardRow, BorderLayout.CENTER);

        JLabel hint = new JLabel("Klik kartu untuk memilih", SwingConstants.CENTER);
        hint.setFont(new Font("Arial", Font.ITALIC, 12));
        hint.setForeground(new Color(170, 170, 170));
        hint.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
        mainPanel.add(hint, BorderLayout.SOUTH);

        this.setContentPane(mainPanel);
        this.setSize(Math.max(360, opsi * 205 + 50), 390);
        this.setLocationRelativeTo(parentWindow);
        this.setUndecorated(true);
        getRootPane().setBorder(BorderFactory.createLineBorder(new Color(255, 215, 0), 2));
    }

    // =========================================================
    // Inner class: Kartu buff
    // =========================================================
    private static class BuffCard extends JPanel {
        private final String nama;
        private boolean hovered = false;
        private float   scale   = 1.0f;
        private final Color cardBg;
        private final Color accent;
        private final String label;
        private Image buffImage = null;

        BuffCard(String nama, ActionListener onClick) {
            this.nama   = nama;
            this.cardBg = resolveCardBg(nama);
            this.accent = resolveAccent(nama);
            this.label  = resolveLabel(nama);
            loadBuffImage();

            setOpaque(false);
            setPreferredSize(new Dimension(180, 285));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            Timer t = new Timer(16, e -> {
                float target = hovered ? 1.07f : 1.0f;
                scale += (target - scale) * 0.18f;
                repaint();
            });
            t.start();

            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { hovered = true;  }
                public void mouseExited (MouseEvent e) { hovered = false; }
                public void mouseClicked(MouseEvent e) { onClick.actionPerformed(null); }
            });
        }

        private void loadBuffImage() {
            String path = AssetConfig.getBuffIconPath(nama);
            if (path != null) {
                try {
                    java.net.URL url = getClass().getResource(path);
                    if (url != null) buffImage = new ImageIcon(url).getImage();
                } catch (Exception ignored) { buffImage = null; }
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            double cx = w/2.0, cy = h/2.0;
            g2.translate(cx, cy); g2.scale(scale, scale); g2.translate(-cx, -cy);

            RoundRectangle2D shape = new RoundRectangle2D.Float(5, 5, w-10, h-10, 18, 18);
            g2.setColor(new Color(0,0,0,90));
            g2.fill(new RoundRectangle2D.Float(7, 8, w-10, h-10, 18, 18));
            g2.setPaint(new GradientPaint(0,0,cardBg.darker(),0,h,cardBg));
            g2.fill(shape);

            g2.setClip(shape);
            g2.setPaint(new GradientPaint(0,0,accent.darker(),w,0,accent));
            g2.fillRect(0,0,w,36);
            g2.setClip(null);

            g2.setFont(new Font("Segoe UI Emoji",Font.PLAIN,16));
            FontMetrics fm=g2.getFontMetrics();
            g2.setColor(Color.WHITE);
            g2.drawString(label,(w-fm.stringWidth(label))/2,24);

            if (hovered) {
                g2.setColor(accent); g2.setStroke(new BasicStroke(2.5f)); g2.draw(shape);
                g2.setColor(new Color(accent.getRed(),accent.getGreen(),accent.getBlue(),55));
                g2.setStroke(new BasicStroke(7f)); g2.draw(shape);
            } else {
                g2.setColor(new Color(255,255,255,35)); g2.setStroke(new BasicStroke(1.5f)); g2.draw(shape);
            }

            int iconTop=38, iconBot=(int)(h*0.64), iconH=iconBot-iconTop, margin=10;

            if (buffImage != null) {
                int imgW=buffImage.getWidth(this), imgH=buffImage.getHeight(this);
                if (imgW>0 && imgH>0) {
                    int availW=w-margin*2, availH=iconH-margin*2;
                    float ratio=Math.min((float)availW/imgW,(float)availH/imgH);
                    int drawW=(int)(imgW*ratio), drawH=(int)(imgH*ratio);
                    int drawX=(w-drawW)/2, drawY=iconTop+margin+(availH-drawH)/2;
                    g2.drawImage(buffImage,drawX,drawY,drawW,drawH,this);
                }
            } else {
                drawBuffIconFallback(g2,w,iconTop,iconBot);
            }

            g2.setColor(new Color(255,255,255,45)); g2.setStroke(new BasicStroke(1f));
            g2.drawLine(14,iconBot+4,w-14,iconBot+4);

            String[] parts=nama.split(" \\(",2);
            String judul=parts[0];
            String detail=(parts.length>1)?"("+parts[1]:"";

            g2.setFont(new Font("Serif",Font.BOLD,14)); g2.setColor(Color.WHITE);
            drawCenter(g2,judul,w,iconBot+22);
            g2.setFont(new Font("Arial",Font.PLAIN,11)); g2.setColor(accent.brighter());
            drawCenter(g2,detail,w,iconBot+38);

            g2.dispose();
        }

        private void drawBuffIconFallback(Graphics2D g2, int w, int top, int bot) {
            int cx=w/2, cy=(top+bot)/2, R=(bot-top)/2-6;
            g2.setStroke(new BasicStroke(2f));
            if (nama.contains("Steel Heart")) {
                drawShield(g2,cx,cy,R,new Color(80,140,255,190),new Color(150,200,255));
                drawHeart(g2,cx,cy+4,R/3,new Color(255,70,100));
            } else if (nama.contains("Strong Defense")) {
                drawShield(g2,cx,cy,R,new Color(180,140,30,190),new Color(255,220,80));
                drawShield(g2,cx,cy+6,R*2/3,new Color(255,200,50,120),new Color(255,220,80));
            } else if (nama.contains("Punch Strike")) {
                drawFist(g2,cx,cy,R);
            } else if (nama.contains("God Slayer")) {
                for (int r=R; r>=R/3; r-=6) {
                    g2.setColor(new Color(255,190,0,12)); g2.fillOval(cx-r,cy-r,r*2,r*2);
                }
                drawSword(g2,cx,cy,R,new Color(255,220,60),new Color(200,100,0));
            } else if (nama.contains("Last Chance")) {
                drawPhoenix(g2,cx,cy,R);
            } else if (nama.contains("Lifesteal")) {
                drawFang(g2,cx,cy,R);
                drawBloodDrop(g2,cx,cy+R/2,R/3,new Color(200,20,20));
            } else if (nama.contains("Invisible")) {
                drawEye(g2,cx,cy,R);
            } else {
                g2.setFont(new Font("Arial",Font.BOLD,R));
                g2.setColor(Color.WHITE);
                drawCenter(g2,"?",w,cy+R/3);
            }
        }

        private void drawShield(Graphics2D g2,int cx,int cy,int R,Color fill,Color stroke){
            int[] sx={cx,cx+R,cx+R,cx+R/2,cx,cx-R/2,cx-R,cx-R};
            int[] sy={cy+R,cy-R/3,cy-R,cy-R,cy-R-4,cy-R,cy-R,cy-R/3};
            g2.setColor(fill); g2.fillPolygon(sx,sy,sx.length);
            g2.setColor(stroke); g2.setStroke(new BasicStroke(2.5f)); g2.drawPolygon(sx,sy,sx.length);
        }
        private void drawHeart(Graphics2D g2,int cx,int cy,int size,Color color){
            GeneralPath h=new GeneralPath();
            h.moveTo(cx,cy+size*0.5);
            h.curveTo(cx-size*1.3,cy-size*0.5,cx-size*1.3,cy-size*1.1,cx,cy-size*0.3);
            h.curveTo(cx+size*1.3,cy-size*1.1,cx+size*1.3,cy-size*0.5,cx,cy+size*0.5);
            h.closePath(); g2.setColor(color); g2.fill(h);
        }
        private void drawSword(Graphics2D g2,int cx,int cy,int R,Color blade,Color hilt){
            int[] bx={cx-4,cx+4,cx+3,cx-3},by={cy+R,cy+R,cy-R,cy-R};
            g2.setColor(blade); g2.fillPolygon(bx,by,4);
            g2.setColor(hilt); g2.fillRoundRect(cx-R/2,cy+R/4,R,R/5,4,4);
            g2.fillRoundRect(cx-5,cy+R/4+R/5,10,R/2,4,4);
        }
        private void drawFist(Graphics2D g2,int cx,int cy,int R){
            g2.setColor(new Color(255,160,60));
            g2.fillRoundRect(cx-R/2, cy-R/3, R, (int)(R*0.7), 8, 8);
            int fw=R/5;
            for(int i=0;i<4;i++){
                g2.fillRoundRect(cx-R/2+i*fw+2, cy-R/3-(int)(R*0.35), fw-2, (int)(R*0.4), 6,6);
            }
            g2.fillRoundRect(cx+R/2-2, cy-R/10, (int)(R*0.3), (int)(R*0.35), 6,6);
            g2.setColor(new Color(200,100,30));
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(cx-R/2, cy-R/3, R, (int)(R*0.7), 8, 8);
            g2.setColor(new Color(255,220,100,160));
            g2.setStroke(new BasicStroke(2f));
            for(int i=0;i<3;i++){
                g2.drawLine(cx-R-i*6, cy-R/2+i*8, cx-R/2-4, cy-R/4+i*8);
            }
        }
        private void drawPhoenix(Graphics2D g2,int cx,int cy,int R){
            int[] lx={cx,cx-R/4,cx-R,cx-R/2,cx-R+R/4,cx-R/6};
            int[] ly={cy+R/3,cy,cy-R/2,cy+R/4,cy+R-R/4,cy+R/2};
            g2.setColor(new Color(255,120,30)); g2.fillPolygon(lx,ly,lx.length);
            int[] rx={cx,cx+R/4,cx+R,cx+R/2,cx+R-R/4,cx+R/6};
            g2.fillPolygon(rx,ly,rx.length);
            g2.setColor(new Color(255,220,60)); g2.fillOval(cx-R/4,cy-R/4,R/2,R/2);
            g2.setColor(Color.WHITE); g2.fillOval(cx-R/8,cy-R/8,R/4,R/4);
        }
        private void drawFang(Graphics2D g2,int cx,int cy,int R){
            Color fc=new Color(200,60,200);
            int[] lx={cx-R/2,cx-R/5,cx-R/3},ly={cy-R/2,cy-R/2,cy+R/2};
            g2.setColor(fc); g2.fillPolygon(lx,ly,3);
            int[] rx={cx+R/2,cx+R/5,cx+R/3};
            g2.fillPolygon(rx,ly,3);
        }
        private void drawBloodDrop(Graphics2D g2,int cx,int cy,int size,Color color){
            GeneralPath d=new GeneralPath();
            d.moveTo(cx,cy-size);
            d.curveTo(cx+size,cy-size/2.0,cx+size,cy+size/2.0,cx,cy+size);
            d.curveTo(cx-size,cy+size/2.0,cx-size,cy-size/2.0,cx,cy-size);
            d.closePath(); g2.setColor(color); g2.fill(d);
        }
        private void drawEye(Graphics2D g2,int cx,int cy,int R){
            g2.setColor(new Color(80,210,255,130));
            g2.fillOval(cx-R,cy-R/3,R*2,(int)(R*0.7));
            g2.setColor(new Color(40,180,255)); g2.setStroke(new BasicStroke(2f));
            g2.drawOval(cx-R,cy-R/3,R*2,(int)(R*0.7));
            g2.setColor(new Color(10,10,10,200)); g2.fillOval(cx-R/4,cy-R/5,R/2,R/2);
            g2.setColor(new Color(80,210,255,200)); g2.fillOval(cx-R/6,cy-R/7,R/4,R/4);
            g2.setColor(new Color(255,255,255,100)); g2.setStroke(new BasicStroke(3f));
            g2.drawLine(cx-R+5,cy+R/3-5,cx+R-5,cy-R/3+5);
        }
        private void drawCenter(Graphics2D g2,String s,int w,int y){
            FontMetrics fm=g2.getFontMetrics();
            g2.drawString(s,(w-fm.stringWidth(s))/2,y);
        }

        private Color resolveCardBg(String b){
            if(b.contains("Steel Heart"))    return new Color(25,50,110);
            if(b.contains("Strong Defense")) return new Color(70,50,10);
            if(b.contains("Punch Strike"))   return new Color(40,40,65);
            if(b.contains("God Slayer"))     return new Color(70,40,5);
            if(b.contains("Last Chance"))    return new Color(90,30,10);
            if(b.contains("Lifesteal"))      return new Color(60,10,65);
            if(b.contains("Invisible"))      return new Color(10,55,75);
            return new Color(35,35,35);
        }
        private Color resolveAccent(String b){
            if(b.contains("Steel Heart"))    return new Color(100,175,255);
            if(b.contains("Strong Defense")) return new Color(255,215,60);
            if(b.contains("Punch Strike"))   return new Color(255,150,50);
            if(b.contains("God Slayer"))     return new Color(255,195,40);
            if(b.contains("Last Chance"))    return new Color(255,120,40);
            if(b.contains("Lifesteal"))      return new Color(210,70,210);
            if(b.contains("Invisible"))      return new Color(70,215,255);
            return Color.WHITE;
        }
        private String resolveLabel(String b){
            if(b.contains("Steel Heart"))    return "💙 Steel Heart";
            if(b.contains("Strong Defense")) return "🛡 Strong Defense";
            if(b.contains("Punch Strike"))   return "👊 Punch Strike";
            if(b.contains("God Slayer"))     return "⚡ God Slayer";
            if(b.contains("Last Chance"))    return "🔥 Last Chance";
            if(b.contains("Lifesteal"))      return "🩸 Lifesteal";
            if(b.contains("Invisible"))      return "👁 Invisible";
            return "✨ Buff";
        }
    }

    // =========================================================
    // Logika terapkan buff
    // =========================================================
    private void terapkanBuff(Player p, String buffDiambil, ArrayList<String> listBuff) {
        String pesan;

        if (buffDiambil.contains("Steel Heart")) {
            int bonus = 40;
            p.setMaxHp(p.getMaxHp() + bonus);
            p.setHp(Math.min(p.getHp() + bonus, p.getMaxHp()));
            pesan = "Max HP +" + bonus + "\nHP: " + p.getHp() + "/" + p.getMaxHp();

        } else if (buffDiambil.contains("Strong Defense")) {
            int bonus = 65;
            p.setMaxHp(p.getMaxHp() + bonus);
            p.setHp(Math.min(p.getHp() + bonus, p.getMaxHp()));
            pesan = "Max HP +" + bonus + "\nHP: " + p.getHp() + "/" + p.getMaxHp();

        } else if (buffDiambil.contains("Punch Strike")) {
            int bonus = 20;
            p.setDamage(p.getDamage() + bonus);
            pesan = "ATK +" + bonus + "\nATK: " + p.getDamage();

        } else if (buffDiambil.contains("God Slayer")) {
            int bonus = 65;
            p.setDamage(p.getDamage() + bonus);
            pesan = "ATK +" + bonus + "\nATK: " + p.getDamage();

        } else if (buffDiambil.contains("Last Chance")) {
            if (p.hasLastChance()) {
                pesan = "Last Chance sudah aktif!";
            } else {
                p.setLastChance(true);
                listBuff.remove(buffDiambil);
                pesan = "Bangkit sekali saat mati!\nMendapat 100% dari MaxHP.";
            }

        } else if (buffDiambil.contains("Lifesteal")) {
            p.setDamage(p.getDamage() + 15);
            p.setLifesteal(true);
            listBuff.remove(buffDiambil);
            pesan = "ATK +15 + Lifesteal aktif!\nSerap 10% MaxHP setiap serang.\nATK: " + p.getDamage();

        } else if (buffDiambil.contains("Invisible")) {
            int before = p.getInvisibleChance();
            p.addInvisible(15);
            if (p.getInvisibleChance() > 75) {
                p.addInvisible(75 - p.getInvisibleChance());
            }
            int gained = p.getInvisibleChance() - before;
            pesan = "Dodge Chance +" + gained + "%\nTotal Dodge: " + p.getInvisibleChance() + "%";

        } else {
            pesan = "Buff tidak dikenal.";
        }

        SoundManager.play(SoundManager.BUFF_SELECT);

        // [FIX-6] Tampilkan dialog kustom bergaya dark-fantasy
        showBuffResultDialog(buffDiambil, pesan);
    }

    // ─────────────────────────────────────────────────────────────────────
    // [FIX-6] BuffResultDialog — Notifikasi buff bergaya kece
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Dialog hasil pemilihan buff. Menggantikan JOptionPane biasa dengan:
     *  - Background gradient gelap + partikel emas animasi
     *  - Border glow berwarna sesuai buff
     *  - Ikon buff (gambar / fallback glyph)
     *  - Nama buff besar + detail kecil
     *  - Tombol "Lanjutkan" kustom
     */
    private void showBuffResultDialog(String buffName, String detail) {
        Color accent = resolveAccent(buffName);

        JDialog dlg = new JDialog(this, Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setUndecorated(true);
        dlg.setSize(420, 320);
        dlg.setLocationRelativeTo(this);

        // ── Anim partikel emas ─────────────────────────────────────────
        int[] frame = {0};
        float[] ptX = new float[30], ptY = new float[30];
        float[] ptVX = new float[30], ptVY = new float[30];
        for (int i = 0; i < 30; i++) {
            ptX[i] = (float)(Math.random() * 420);
            ptY[i] = (float)(Math.random() * 320);
            ptVX[i] = (float)((Math.random()-0.5) * 1.2f);
            ptVY[i] = -(float)(Math.random() * 1.5f + 0.5f);
        }

        JPanel panel = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                int W = getWidth(), H = getHeight();

                // Background
                g2.setPaint(new GradientPaint(0, 0, new Color(10, 8, 25),
                                              W, H, new Color(28, 12, 45)));
                g2.fillRoundRect(0, 0, W, H, 16, 16);

                // Partikel emas
                for (int i = 0; i < ptX.length; i++) {
                    float alpha = 0.15f + (float)(Math.sin(frame[0] * 0.06 + i) * 0.2);
                    alpha = Math.max(0.05f, alpha);
                    g2.setColor(new Color(255, 200, 50, (int)(alpha * 255)));
                    int sz = (i % 4 == 0) ? 4 : 2;
                    g2.fillOval((int)ptX[i], (int)ptY[i], sz, sz);
                }

                // Glow border berlapis
                for (int r = 10; r >= 2; r -= 3) {
                    int alpha2 = (int)(35 * (10 - r) / 10.0);
                    g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), alpha2));
                    g2.setStroke(new BasicStroke(r));
                    g2.drawRoundRect(r/2, r/2, W-r, H-r, 16, 16);
                }
                // Border utama
                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 200));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(1, 1, W-3, H-3, 16, 16);

                // Garis emas atas
                g2.setPaint(new GradientPaint(0,0,new Color(100,70,0,0),W/2,0,new Color(255,200,50,160)));
                g2.fillRect(0,0,W/2,2);
                g2.setPaint(new GradientPaint(W/2,0,new Color(255,200,50,160),W,0,new Color(100,70,0,0)));
                g2.fillRect(W/2,0,W/2,2);

                // Header strip
                g2.setPaint(new GradientPaint(0,0,accent.darker().darker(),W,0,accent.darker()));
                g2.fillRoundRect(0, 0, W, 42, 16, 16);
                g2.fillRect(0, 26, W, 16);

                // Judul header
                g2.setFont(new Font("Serif", Font.BOLD, 15));
                g2.setColor(new Color(255, 255, 255, 220));
                String header = "✅  Buff Diterapkan!";
                FontMetrics fmH = g2.getFontMetrics();
                g2.drawString(header, (W - fmH.stringWidth(header)) / 2, 27);

                // Ikon buff (50×50 di kiri)
                Image bImg = loadBuffImageFor(buffName);
                int iconX = 24, iconY = 58, iconSz = 56;
                if (bImg != null) {
                    g2.drawImage(bImg, iconX, iconY, iconSz, iconSz, this);
                } else {
                    // Fallback lingkaran berwarna
                    g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 80));
                    g2.fillOval(iconX, iconY, iconSz, iconSz);
                    g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 26));
                    g2.setColor(accent);
                    FontMetrics fmE = g2.getFontMetrics();
                    String emoji = resolveEmoji(buffName);
                    g2.drawString(emoji, iconX + (iconSz - fmE.stringWidth(emoji))/2,
                                  iconY + iconSz/2 + fmE.getAscent()/2 - 4);
                }

                // Nama buff besar
                g2.setFont(new Font("Serif", Font.BOLD, 20));
                g2.setColor(accent.brighter());
                String[] parts = buffName.split(" \\(", 2);
                String buffTitle = parts[0];
                FontMetrics fmT = g2.getFontMetrics();
                int textX = iconX + iconSz + 16;
                g2.setColor(new Color(0, 0, 0, 130));
                g2.drawString(buffTitle, textX + 1, 79);
                g2.setColor(accent.brighter());
                g2.drawString(buffTitle, textX, 78);

                // Detail multi-baris
                g2.setFont(new Font("Arial", Font.PLAIN, 13));
                g2.setColor(new Color(210, 195, 160));
                String[] lines = detail.split("\n");
                int ly = 100;
                for (String line : lines) {
                    g2.setColor(new Color(0, 0, 0, 100));
                    g2.drawString(line, textX + 1, ly + 1);
                    g2.setColor(new Color(210, 195, 160));
                    g2.drawString(line, textX, ly);
                    ly += 18;
                }

                // Separator tipis
                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 60));
                g2.setStroke(new BasicStroke(1f));
                g2.drawLine(20, 170, W - 20, 170);

                // Tip kecil
                g2.setFont(new Font("Arial", Font.ITALIC, 11));
                g2.setColor(new Color(150, 140, 110, 180));
                String tip = "💡 " + resolveTip(buffName);
                FontMetrics fmTip = g2.getFontMetrics();
                g2.drawString(tip, (W - fmTip.stringWidth(tip)) / 2, 190);

                g2.dispose();
            }
        };
        panel.setOpaque(false);

        // Tombol "Lanjutkan"
        JButton btnOk = new JButton() {
            private boolean hov = false;
            { addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { hov = true;  repaint(); }
                public void mouseExited (MouseEvent e) { hov = false; repaint(); }
            }); }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                Color c1 = hov ? accent.brighter() : accent.darker();
                Color c2 = hov ? accent : accent.darker().darker();
                g2.setPaint(new GradientPaint(0, 0, c1, 0, h, c2));
                g2.fillRoundRect(0, 0, w, h, 10, 10);
                if (hov) {
                    g2.setColor(new Color(255, 255, 255, 80));
                    g2.setStroke(new BasicStroke(2f));
                    g2.drawRoundRect(1, 1, w-3, h-3, 10, 10);
                }
                g2.setFont(new Font("Serif", Font.BOLD, 15));
                g2.setColor(Color.WHITE);
                FontMetrics fm = g2.getFontMetrics();
                String txt = "✔  Lanjutkan";
                g2.drawString(txt, (w - fm.stringWidth(txt))/2, h/2 + fm.getAscent()/2 - 2);
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(160, 40); }
        };
        btnOk.setContentAreaFilled(false);
        btnOk.setBorderPainted(false);
        btnOk.setFocusPainted(false);
        btnOk.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnOk.setBounds(130, 220, 160, 40);
        btnOk.addActionListener(e -> dlg.dispose());
        panel.add(btnOk);

        // Timer animasi partikel
        Timer dlgAnim = new Timer(16, e -> {
            frame[0]++;
            for (int i = 0; i < ptX.length; i++) {
                ptX[i] += ptVX[i];
                ptY[i] += ptVY[i];
                if (ptY[i] < -5) { ptY[i] = 325; ptX[i] = (float)(Math.random() * 420); }
                if (ptX[i] < 0 || ptX[i] > 420) ptVX[i] = -ptVX[i];
            }
            panel.repaint();
        });
        dlgAnim.start();

        dlg.addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { dlgAnim.stop(); }
            @Override public void windowClosed(WindowEvent e)  { dlgAnim.stop(); }
        });

        dlg.setContentPane(panel);
        dlg.setVisible(true);
    }

    // Helper untuk BuffResultDialog
    private Image loadBuffImageFor(String buffName) {
        String path = AssetConfig.getBuffIconPath(buffName);
        if (path == null) return null;
        try {
            java.net.URL url = getClass().getResource(path);
            if (url != null) return new ImageIcon(url).getImage();
        } catch (Exception ignored) {}
        return null;
    }

    private String resolveEmoji(String b) {
        if(b.contains("Steel Heart"))    return "💙";
        if(b.contains("Strong Defense")) return "🛡";
        if(b.contains("Punch Strike"))   return "👊";
        if(b.contains("God Slayer"))     return "⚡";
        if(b.contains("Last Chance"))    return "🔥";
        if(b.contains("Lifesteal"))      return "🩸";
        if(b.contains("Invisible"))      return "👁";
        return "✨";
    }

    private String resolveTip(String b) {
        if(b.contains("Steel Heart"))    return "Tambah HP sekarang, bertahan lebih lama!";
        if(b.contains("Strong Defense")) return "Pertahanan terkuat — cocok untuk map berat.";
        if(b.contains("Punch Strike"))   return "Setiap pukulan kini terasa lebih menyakitkan!";
        if(b.contains("God Slayer"))     return "ATK masif — satu serangan bisa mengubah segalanya.";
        if(b.contains("Last Chance"))    return "Satu nyawa ekstra saat situasi kritis!";
        if(b.contains("Lifesteal"))      return "Serang musuh = sembuhkan dirimu secara otomatis.";
        if(b.contains("Invisible"))      return "Dodge makin tinggi = musuh sering meleset!";
        return "Kekuatan baru menanti!";
    }

    private Color resolveAccent(String b) {
        if(b.contains("Steel Heart"))    return new Color(100,175,255);
        if(b.contains("Strong Defense")) return new Color(255,215,60);
        if(b.contains("Punch Strike"))   return new Color(255,150,50);
        if(b.contains("God Slayer"))     return new Color(255,195,40);
        if(b.contains("Last Chance"))    return new Color(255,120,40);
        if(b.contains("Lifesteal"))      return new Color(210,70,210);
        if(b.contains("Invisible"))      return new Color(70,215,255);
        return Color.WHITE;
    }
}