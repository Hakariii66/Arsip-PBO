package view;

import database.DatabaseException;
import iterator.ProductCollection;
import model.Product;
import model.ProductFactory;
import service.ProductService;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Halaman manajemen produk untuk admin (PRD bagian 5.4).
 *
 * Fitur: lihat daftar produk, cari produk (nama/kategori), filter kategori,
 * tambah, ubah, dan hapus produk. Kolom "Harga Customer" dihitung memakai
 * {@code calculatePrice()} sehingga aturan harga tiap kategori terlihat
 * (polymorphism).
 */
public class ProductFrame extends BaseFrame {

    private static final long serialVersionUID = 1L;

    private static final int KOLOM_STATUS = 8;

    private final ProductService productService = new ProductService();
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[] { "ID", "Nama Produk", "Kategori", "Harga Beli", "Harga Jual",
                    "Harga Customer", "Stok", "Stok Min", "Status" }, 0);
    private final JTable tabel = new JTable(model);
    private final JTextField cariField = Theme.textField(16);
    private final JComboBox<String> kategoriCombo = Theme.comboBox(kategoriFilter());
    private final JLabel infoLabel = Theme.text(" ");
    private final List<Product> produkTampil = new ArrayList<>();

    public ProductFrame(JFrame dashboard) {
        super("Manajemen Produk - SITAG", dashboard);
        Theme.styleTable(tabel);
        Theme.highlightLowStock(tabel, KOLOM_STATUS);
        tabel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    ubahProduk();
                }
            }
        });

        setSize(1020, 640);
        add(Theme.headerPanel("Manajemen Produk", "Tambah, ubah, hapus, dan cari produk toko",
                clockLabel(false)), BorderLayout.NORTH);

        JPanel isi = Theme.lightPanel();
        isi.setLayout(new BorderLayout(0, 10));
        isi.setBorder(Theme.padding(14));
        isi.add(buildToolbar(), BorderLayout.NORTH);
        isi.add(Theme.scrollPane(tabel), BorderLayout.CENTER);
        isi.add(infoLabel, BorderLayout.SOUTH);
        add(isi, BorderLayout.CENTER);
        center();
        muatData();
    }

    /** Daftar kategori untuk filter, dengan tambahan pilihan "Semua". */
    private static String[] kategoriFilter() {
        String[] kategori = ProductFactory.categories();
        String[] hasil = new String[kategori.length + 1];
        hasil[0] = "Semua";
        System.arraycopy(kategori, 0, hasil, 1, kategori.length);
        return hasil;
    }

    private JPanel buildToolbar() {
        JPanel pencarian = Theme.lightPanel();
        pencarian.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 8));
        pencarian.add(Theme.text("Cari Produk"));
        pencarian.add(cariField);
        pencarian.add(Theme.text("Kategori"));
        pencarian.add(kategoriCombo);
        JButton cariButton = Theme.primaryButton("Cari");
        cariButton.addActionListener(e -> muatData());
        JButton resetButton = Theme.secondaryButton("Reset");
        resetButton.addActionListener(e -> {
            cariField.setText("");
            kategoriCombo.setSelectedIndex(0);
            muatData();
        });
        pencarian.add(cariButton);
        pencarian.add(resetButton);

        JPanel aksi = Theme.lightPanel();
        aksi.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 4));
        JButton tambahButton = Theme.primaryButton("Tambah Produk");
        tambahButton.addActionListener(e -> tambahProduk());
        JButton ubahButton = Theme.secondaryButton("Ubah Produk");
        ubahButton.addActionListener(e -> ubahProduk());
        JButton hapusButton = Theme.secondaryButton("Hapus Produk");
        hapusButton.addActionListener(e -> hapusProduk());
        JButton kembaliButton = Theme.secondaryButton("Kembali");
        kembaliButton.addActionListener(e -> goBack());
        aksi.add(tambahButton);
        aksi.add(ubahButton);
        aksi.add(hapusButton);
        aksi.add(kembaliButton);

        JPanel toolbar = Theme.lightPanel();
        toolbar.setLayout(new GridLayout(2, 1));
        toolbar.add(pencarian);
        toolbar.add(aksi);
        return toolbar;
    }

    private void muatData() {
        try {
            List<Product> hasil = productService.search(cariField.getText(), (String) kategoriCombo.getSelectedItem());
            produkTampil.clear();
            model.setRowCount(0);
            // Iterator Pattern: menelusuri koleksi produk hasil pencarian
            ProductCollection collection = productService.toCollection(hasil);
            Iterator<Product> iterator = collection.iterator();
            while (iterator.hasNext()) {
                Product produk = iterator.next();
                produkTampil.add(produk);
                model.addRow(new Object[] {
                    produk.getIdProduk(),
                    produk.getNamaProduk(),
                    produk.getCategory(),
                    Theme.rupiah(produk.getHargaBeli()),
                    Theme.rupiah(produk.getHargaJual()),
                    Theme.rupiah(produk.calculatePrice()),
                    produk.getJumlahStok(),
                    produk.getStokMinimum(),
                    produk.getStatusStok()
                });
            }
            infoLabel.setText("Menampilkan " + produkTampil.size() + " produk | Produk stok menipis: "
                    + productService.countLowStock());
        } catch (DatabaseException e) {
            Theme.error(this, "Gagal memuat data produk: " + e.getMessage());
        }
    }

    private void tambahProduk() {
        ProductFormDialog dialog = new ProductFormDialog(this, productService, null);
        dialog.setVisible(true);
        if (dialog.isTersimpan()) {
            muatData();
        }
    }

    private void ubahProduk() {
        Product produk = produkTerpilih();
        if (produk == null) {
            Theme.info(this, "Pilih satu produk pada tabel terlebih dahulu.");
            return;
        }
        ProductFormDialog dialog = new ProductFormDialog(this, productService, produk);
        dialog.setVisible(true);
        if (dialog.isTersimpan()) {
            muatData();
        }
    }

    private void hapusProduk() {
        Product produk = produkTerpilih();
        if (produk == null) {
            Theme.info(this, "Pilih satu produk pada tabel terlebih dahulu.");
            return;
        }
        if (!Theme.confirm(this, "Hapus produk \"" + produk.getNamaProduk() + "\"?")) {
            return;
        }
        try {
            productService.delete(produk.getIdProduk());
            muatData();
            Theme.info(this, "Produk berhasil dihapus.");
        } catch (DatabaseException e) {
            Theme.error(this, e.getMessage());
        }
    }

    /** Produk pada baris yang dipilih (mendukung pengurutan kolom tabel). */
    private Product produkTerpilih() {
        int barisView = tabel.getSelectedRow();
        if (barisView < 0) {
            return null;
        }
        int barisModel = tabel.convertRowIndexToModel(barisView);
        if (barisModel < 0 || barisModel >= produkTampil.size()) {
            return null;
        }
        return produkTampil.get(barisModel);
    }
}