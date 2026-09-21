package main;

import database.DatabaseConnection;
import view.MainFrame;
import view.Theme;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.sql.SQLException;

/**
 * Entry point aplikasi SITAG
 * (Sistem Manajemen Inventaris Toko Aksesoris Gadget).
 *
 * Urutan proses saat aplikasi dijalankan:
 * 1. Menyiapkan Look &amp; Feel + tema warna biru tua / biru muda.
 * 2. Menyiapkan database (Singleton DatabaseConnection) - bila
 *    {@code db.auto.init=true} database, tabel, dan data awal dibuat otomatis.
 * 3. Menampilkan main menu (Masuk Admin / Pembelian).
 */
public class Main {

    public static void main(String[] args) {
        siapkanTampilan();
        if (!siapkanDatabase()) {
            return;
        }
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }

    /** Mengatur Look &amp; Feel dan warna dasar seluruh komponen Swing. */
    private static void siapkanTampilan() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {
            // Look & Feel default tetap dipakai bila gagal
        }
        UIManager.put("Panel.background", Theme.LIGHT_BLUE_SOFT);
        UIManager.put("OptionPane.background", Theme.LIGHT_BLUE_SOFT);
        UIManager.put("OptionPane.messageForeground", Theme.DARK_BLUE);
        UIManager.put("Label.foreground", Theme.DARK_BLUE);
        UIManager.put("Label.font", Theme.FONT_BODY);
        UIManager.put("Button.font", Theme.FONT_BODY_BOLD);
        UIManager.put("TextField.font", Theme.FONT_BODY);
        UIManager.put("PasswordField.font", Theme.FONT_BODY);
        UIManager.put("ComboBox.font", Theme.FONT_BODY);
        UIManager.put("Table.font", Theme.FONT_BODY);
        UIManager.put("TableHeader.font", Theme.FONT_BODY_BOLD);
        UIManager.put("CheckBox.background", Theme.LIGHT_BLUE_SOFT);
        UIManager.put("CheckBox.foreground", Theme.DARK_BLUE);
        UIManager.put("RadioButton.background", Theme.LIGHT_BLUE_SOFT);
        UIManager.put("RadioButton.foreground", Theme.DARK_BLUE);
        UIManager.put("TitledBorder.titleColor", Theme.DARK_BLUE);
    }

    /**
     * Menyiapkan koneksi &amp; skema database.
     *
     * @return true bila database siap dipakai; false bila gagal (aplikasi ditutup)
     */
    private static boolean siapkanDatabase() {
        try {
            DatabaseConnection.getInstance().initialize();
            Runtime.getRuntime().addShutdownHook(
                    new Thread(() -> DatabaseConnection.getInstance().closeConnection(), "shutdown-db"));
            return true;
        } catch (SQLException e) {
            Theme.error(null, "Aplikasi tidak dapat terhubung ke database MySQL.\n\n"
                    + e.getMessage() + "\n\nLangkah perbaikan:\n"
                    + "1. Pastikan server MySQL (Laragon/XAMPP/MySQL Server) sudah berjalan.\n"
                    + "2. Periksa db.host, db.port, db.user, dan db.password pada config.properties.\n"
                    + "3. Import database.sql melalui phpMyAdmin, atau biarkan\n"
                    + "   db.auto.init=true agar aplikasi membuat database otomatis.\n\n"
                    + "Aplikasi akan ditutup.");
            return false;
        }
    }
}