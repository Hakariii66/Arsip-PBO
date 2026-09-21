package view;

import database.DatabaseConnection;
import database.DatabaseException;
import iterator.ProductCollection;
import model.Admin;
import model.Product;
import service.ProductService;
import service.TransactionService;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Iterator;

/**
 * Dashboard admin (PRD bagian 5.3).
 *
 * Menampilkan total produk, jumlah produk dengan stok menipis, jumlah transaksi
 * hari ini, jam digital realtime, dan menu Produk / Stok / Histori Pembelian /
 * Logout. Data stok menipis ditelusuri memakai Iterator Pattern.
 */
public class AdminDashboard extends BaseFrame {

    private static final long serialVersionUID = 1L;

    private final Admin admin;
    private final ProductService productService = new ProductService();
    private final TransactionService transactionService = new TransactionService();

    private final JLabel totalProduk = Theme.text("-");
    private final JLabel stokMenipis = Theme.text("-");
    private final JLabel transaksiHariIni = Theme.text("-");
    private final JLabel penjualanHariIni = Theme.text("-");
    private final JLabel peringatan = Theme.textBold(" ");

    private final DefaultTableModel modelStok;
    private final JTable tabelStok;

    public AdminDashboard(JFrame mainFrame, Admin admin) {
        super("Dashboard Admin", mainFrame);
        this.admin = admin;
        this.modelStok = new DefaultTableModel(
                new Object[] { "ID", "Nama Produk", "Kategori", "Stok", "Stok Min", "Status" }, 0);
        this.tabelStok = new JTable(modelStok);
        Theme.styleTable(tabelStok);
        Theme.highlightLowStock(tabelStok, 5);

        setSize(960, 660);
        add(buildHeader(), BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);
        add(Theme.statusBar("Login sebagai " + admin.getDisplayName()
                + " | " + DatabaseConnection.getInstance().getDatabaseInfo()), BorderLayout.SOUTH);
        center();
        muatData();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowActivated(WindowEvent e) {
                muatData();
            }
        });
    }

    private JPanel buildHeader() {
        JPanel kanan = Theme.darkPanel();
        kanan.setLayout(new GridLayout(2, 1, 0, 4));
        kanan.add(clockLabel(false));
        JLabel adminLabel = Theme.lightText("Admin: " + admin.getUsername(), Theme.FONT_BODY);
        adminLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        kanan.add(adminLabel);
        return Theme.headerPanel("Panel Admin", "Kelola produk, stok, dan histori pembelian", kanan);
    }

    private JPanel buildContent() {
        JPanel content = Theme.lightPanel();
        content.setLayout(new BorderLayout(0, 14));
        content.setBorder(Theme.padding(16));
        content.add(buildStatistik(), BorderLayout.NORTH);
        content.add(buildPanelStok(), BorderLayout.CENTER);
        content.add(buildMenu(), BorderLayout.SOUTH);
        return content;
    }

    private JPanel buildStatistik() {
        JPanel panel = Theme.lightPanel();
        panel.setLayout(new GridLayout(1, 4, 12, 12));
        panel.add(Theme.card("Total Produk", totalProduk));
        panel.add(Theme.card("Stok Menipis", stokMenipis));
        panel.add(Theme.card("Transaksi Hari Ini", transaksiHariIni));
        panel.add(Theme.card("Penjualan Hari Ini", penjualanHariIni));
        return panel;
    }

    private JPanel buildPanelStok() {
        JPanel panel = Theme.lightPanel();
        panel.setLayout(new BorderLayout(0, 8));
        panel.add(Theme.subtitle("Peringatan Stok Minimum (stok <= stok minimum)"), BorderLayout.NORTH);
        panel.add(Theme.scrollPane(tabelStok), BorderLayout.CENTER);
        panel.add(peringatan, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildMenu() {
        JButton produkButton = Theme.primaryButton("Produk");
        produkButton.addActionListener(e -> bukaHalaman(new ProductFrame(this)));
        JButton stokButton = Theme.primaryButton("Stok");
        stokButton.addActionListener(e -> bukaHalaman(new StockFrame(this)));
        JButton historiButton = Theme.primaryButton("Histori Pembelian");
        historiButton.addActionListener(e -> bukaHalaman(new TransactionFrame(this)));
        JButton logoutButton = Theme.secondaryButton("Logout");
        logoutButton.addActionListener(e -> logout());

        JPanel menu = Theme.lightPanel();
        menu.setLayout(new GridLayout(1, 4, 10, 10));
        menu.add(produkButton);
        menu.add(stokButton);
        menu.add(historiButton);
        menu.add(logoutButton);
        return menu;
    }

    /** Membuka halaman admin lain; dashboard disembunyikan agar bisa dikembalikan. */
    private void bukaHalaman(JFrame halaman) {
        setVisible(false);
        halaman.setVisible(true);
    }

    private void logout() {
        if (!Theme.confirm(this, "Logout dari dashboard admin?")) {
            return;
        }
        closeTo(getBackTarget());
    }

    /** Memuat ulang seluruh statistik dan daftar stok menipis. */
    private void muatData() {
        try {
            totalProduk.setText(String.valueOf(productService.countAll()));
            stokMenipis.setText(String.valueOf(productService.countLowStock()));
            transaksiHariIni.setText(String.valueOf(transactionService.countToday()));
            penjualanHariIni.setText(Theme.rupiah(transactionService.sumToday()));
            peringatan.setText(productService.getLowStockWarning());

            modelStok.setRowCount(0);
            // Iterator Pattern: menelusuri koleksi produk stok menipis
            ProductCollection collection = productService.toCollection(productService.findLowStock());
            Iterator<Product> iterator = collection.iterator();
            while (iterator.hasNext()) {
                Product produk = iterator.next();
                modelStok.addRow(new Object[] {
                    produk.getIdProduk(),
                    produk.getNamaProduk(),
                    produk.getCategory(),
                    produk.getJumlahStok(),
                    produk.getStokMinimum(),
                    produk.getStatusStok()
                });
            }
            if (modelStok.getRowCount() == 0) {
                peringatan.setText("Semua produk memiliki stok di atas batas minimum.");
            }
        } catch (DatabaseException e) {
            peringatan.setText("Gagal memuat data: " + e.getMessage());
        }
    }
}