package GUI;

import CharacterSettings.Player;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.Collections;

/**
 * BuffDialog v3 — Pop-up pemilihan buff.
 *
 * PERUBAHAN v3 (buff baru sesuai spesifikasi):
 *  - God Slayer      : ATK +65
 *  - Punch Strike    : ATK +20
 *  - Steel Heart     : MaxHP +40
 *  - Strong Defense  : MaxHP +65
 *  - Lifesteal       : ATK +15 + serap 10% maxHP setiap serangan
 *  - Invisible       : Dodge +10%, bertahap maks 50%
 *  - Last Chance     : Bangkit sekali dengan 25% maxHP
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

            // Nama buff
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
                // Double shield
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

        // ---- Shape helpers ----
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
            // Kepalan tangan sederhana
            g2.setColor(new Color(255,160,60));
            g2.fillRoundRect(cx-R/2, cy-R/3, R, (int)(R*0.7), 8, 8);
            // 4 jari
            int fw=R/5;
            for(int i=0;i<4;i++){
                g2.fillRoundRect(cx-R/2+i*fw+2, cy-R/3-(int)(R*0.35), fw-2, (int)(R*0.4), 6,6);
            }
            // Ibu jari
            g2.fillRoundRect(cx+R/2-2, cy-R/10, (int)(R*0.3), (int)(R*0.35), 6,6);
            // Outline
            g2.setColor(new Color(200,100,30));
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(cx-R/2, cy-R/3, R, (int)(R*0.7), 8, 8);
            // Efek speed lines
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

        // ---- Warna per buff ----
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
    // Logika terapkan buff — DIPERBARUI sesuai spesifikasi
    // =========================================================
    private void terapkanBuff(Player p, String buffDiambil, ArrayList<String> listBuff) {
        String pesan;

        if (buffDiambil.contains("Steel Heart")) {
            // MaxHP +40
            int bonus = 40;
            p.setMaxHp(p.getMaxHp() + bonus);
            p.setHp(Math.min(p.getHp() + bonus, p.getMaxHp()));
            pesan = "Max HP +" + bonus + "\nHP: " + p.getHp() + "/" + p.getMaxHp();

        } else if (buffDiambil.contains("Strong Defense")) {
            // MaxHP +65
            int bonus = 65;
            p.setMaxHp(p.getMaxHp() + bonus);
            p.setHp(Math.min(p.getHp() + bonus, p.getMaxHp()));
            pesan = "Max HP +" + bonus + "\nHP: " + p.getHp() + "/" + p.getMaxHp();

        } else if (buffDiambil.contains("Punch Strike")) {
            // ATK +20
            int bonus = 20;
            p.setDamage(p.getDamage() + bonus);
            pesan = "ATK +" + bonus + "\nATK: " + p.getDamage();

        } else if (buffDiambil.contains("God Slayer")) {
            // ATK +65
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
            // ATK +15, aktifkan lifesteal (10% maxHP per serangan — dihandle di GamePanel)
            p.setDamage(p.getDamage() + 15);
            p.setLifesteal(true);
            listBuff.remove(buffDiambil);
            pesan = "ATK +15 + Lifesteal aktif!\nSerap 10% MaxHP setiap serang.\nATK: " + p.getDamage();

        } else if (buffDiambil.contains("Invisible")) {
            // Dodge +15%, maks 75%
            int before = p.getInvisibleChance();
            p.addInvisible(15); // addInvisible sudah cap di 75
            // MAX 75%
            if (p.getInvisibleChance() > 75) {
                // Paksa set ke 75 via workaround
                p.addInvisible(75 - p.getInvisibleChance()); // akan 0 atau negatif → tidak berubah
            }
            int gained = p.getInvisibleChance() - before;
            pesan = "Dodge Chance +" + gained + "%\nTotal Dodge: " + p.getInvisibleChance() + "%";

        } else {
            pesan = "Buff tidak dikenal.";
        }

        SoundManager.play(SoundManager.BUFF_SELECT);
        JOptionPane.showMessageDialog(this,
            "✅ Buff Diterapkan!\n\n" + buffDiambil + "\n\n" + pesan,
            "Buff Aktif!", JOptionPane.INFORMATION_MESSAGE);
    }
}