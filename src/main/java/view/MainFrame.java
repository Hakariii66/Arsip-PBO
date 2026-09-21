package view;

import database.DatabaseConnection;
import model.Cart;

import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.GridLayout;

/**
 * Main menu aplikasi (PRD bagian 5.1).
 *
 * Tersedia dua jalur penggunaan:
 * - "Masuk Admin"  -> login untuk mengelola data toko.
 * - "Pembelian"    -> customer langsung berbelanja tanpa login.
 */
public class MainFrame extends BaseFrame {

    private static final long serialVersionUID = 1L;

    public MainFrame() {
        super("Halaman Login", null);
        setSize(620, 520);
        add(buildHeader(), BorderLayout.NORTH);
        add(buildMenu(), BorderLayout.CENTER);
        center();
    }

    private JPanel buildHeader() {
        return Theme.headerPanel("Selamat Datang di SITAG",
                "Sistem Inventaris Toko Aksesoris Gadget",
                clockLabel(true));
    }

    private JPanel buildMenu() {
        JPanel wrapper = Theme.lightPanel();
        wrapper.setLayout(new BorderLayout(0, 16));
        wrapper.setBorder(Theme.padding(22));

        JPanel info = Theme.lightPanel();
        info.setLayout(new GridLayout(4, 1, 0, 4));
        info.add(Theme.subtitle("Menu Utama :"));
        info.add(Theme.text("Menu \"Administrator\" untuk mengelola produk, stok, dan histori pembelian."));
        info.add(Theme.text("Menu \"Customer\" untuk customer berbelanja tanpa perlu membuat akun."));
        info.add(Theme.text("Menu \"Exit\" untuk keluar dari aplikasi."));
        wrapper.add(info, BorderLayout.NORTH);

        JButton adminButton = Theme.primaryButton("Administrator");
        adminButton.addActionListener(e -> bukaLoginAdmin());
        JButton buyerButton = Theme.primaryButton("Customer");
        buyerButton.addActionListener(e -> openNext(new BuyerFrame(this, new Cart())));
        JButton exitButton = Theme.secondaryButton("Exit");
        exitButton.addActionListener(e -> keluar());

        JPanel menu = Theme.lightPanel();
        menu.setLayout(new GridLayout(3, 1, 12, 12));
        menu.add(adminButton);
        menu.add(buyerButton);
        menu.add(exitButton);
        wrapper.add(menu, BorderLayout.CENTER);
        return wrapper;
    }

    /**
     * Membuka halaman login admin.
     *
     * Menu utama hanya disembunyikan (tidak di-dispose) agar tetap hidup, sehingga
     * setelah admin logout aplikasi kembali ke halaman login awal ini dan program
     * tidak ikut tertutup.
     */
    private void bukaLoginAdmin() {
        setVisible(false);
        new LoginFrame(this).setVisible(true);
    }

    private void keluar() {
        if (!Theme.confirm(this, "Tutup aplikasi SITAG sekarang?")) {
            return;
        }
        DatabaseConnection.getInstance().closeConnection();
        dispose();
        System.exit(0);
    }
}