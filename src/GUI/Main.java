package GUI;

import javax.swing.*;

/**
 * Main — Entry point game.
 * Alur: AuthScreen (Login/Register) → PreGameScreen → GamePanel
 */
public class Main {
    public static void main(String[] args) {
        // Pastikan folder data/ siap (DatabaseManager akan membuatnya otomatis)
        SwingUtilities.invokeLater(() -> {
            try {
                // Inisialisasi DatabaseManager saat startup
                Database.DatabaseManager.getInstance();
                System.out.println("[Main] Database siap.");
            } catch (Exception e) {
                System.err.println("[Main] DB init error: " + e.getMessage());
            }

            // Buka layar login/register
            new AuthScreen().setVisible(true);
        });
    }
}