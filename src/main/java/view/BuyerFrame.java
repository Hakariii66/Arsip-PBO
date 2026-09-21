package view;

import database.DatabaseException;
import decorator.GiftWrapDecorator;
import decorator.ProductComponent;
import decorator.WarrantyDecorator;
import iterator.ProductCollection;
import model.Cart;
import model.Product;
import model.ProductFactory;
import service.ProductService;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Halaman pembelian customer (PRD bagian 5.6).
 *
 * Customer tidak perlu login: dapat melihat katalog, mencari produk, memfilter
 * kategori, memilih fitur tambahan (Decorator Pattern: gift wrapping / extended
 * warranty), dan menambahkan produk ke keranjang.
 */
public class BuyerFrame extends BaseFrame {

    private static final long serialVersionUID = 1L;

    private static final int KOLOM_STATUS = 6;

    private final Cart cart;
    private final ProductService productService = new ProductService();

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[] { "ID", "Nama Produk", "Kategori", "Deskripsi", "Harga", "Stok", "Status" }, 0);
    private final JTable tabel = new JTable(model);
    private final JTextField cariField = Theme.textField(14);
    private final JComboBox<String> kategoriCombo = Theme.comboBox(kategoriFilter());
    private final JSpinner jumlahSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
    private final JCheckBox giftWrapCheck = new JCheckBox("Gift Wrapping (+Rp 5.000)");
    private final JCheckBox warrantyCheck = new JCheckBox("Extended Warranty (+10%)");
    private final JLabel hargaLabel = Theme.textBold("Harga item terpilih: -");
    private final JButton keranjangButton = Theme.primaryButton("Lihat Keranjang");
    private final JLabel infoLabel = Theme.text(" ");
    private final List<Product> produkTampil = new ArrayList<>();

    public BuyerFrame(JFrame mainFrame, Cart cart) {
        super("Pembelian - SITAG", mainFrame);
        this.cart = cart;

        Theme.styleTable(tabel);
        Theme.highlightLowStock(tabel, KOLOM_STATUS);
        tabel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabel.getSelectionModel().addListSelectionListener(e -> updatePreview());
        jumlahSpinner.addChangeListener(e -> updatePreview());
        giftWrapCheck.addActionListener(e -> updatePreview());
        warrantyCheck.addActionListener(e -> updatePreview());
        giftWrapCheck.setBackground(Theme.LIGHT_BLUE_SOFT);
        giftWrapCheck.setForeground(Theme.DARK_BLUE);
        warrantyCheck.setBackground(Theme.LIGHT_BLUE_SOFT);
        warrantyCheck.setForeground(Theme.DARK_BLUE);

        setSize(1040, 680);
        add(Theme.headerPanel("Pembelian Customer", "Pilih produk lalu tambahkan ke keranjang",
                clockLabel(false)), BorderLayout.NORTH);

        JPanel isi = Theme.lightPanel();
        isi.setLayout(new BorderLayout(0, 10));
        isi.setBorder(Theme.padding(14));
        isi.add(buildToolbar(), BorderLayout.NORTH);
        isi.add(Theme.scrollPane(tabel), BorderLayout.CENTER);
        isi.add(buildPanelBawah(), BorderLayout.SOUTH);
        add(isi, BorderLayout.CENTER);

        center();
        muatData();
        updateKeranjangButton();

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowActivated(java.awt.event.WindowEvent e) {
                muatData();
                updateKeranjangButton();
            }
        });
    }

    private static String[] kategoriFilter() {
        String[] kategori = ProductFactory.categories();
        String[] hasil = new String[kategori.length + 1];
        hasil[0] = "Semua";
        System.arraycopy(kategori, 0, hasil, 1, kategori.length);
        return hasil;
    }

    private JPanel buildToolbar() {
        JPanel panel = Theme.lightPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 8));
        panel.add(Theme.text("Cari Produk"));
        panel.add(cariField);
        panel.add(Theme.text("Kategori"));
        panel.add(kategoriCombo);
        JButton cariButton = Theme.primaryButton("Cari");
        cariButton.addActionListener(e -> muatData());
        JButton resetButton = Theme.secondaryButton("Reset");
        resetButton.addActionListener(e -> {
            cariField.setText("");
            kategoriCombo.setSelectedIndex(0);
            muatData();
        });
        keranjangButton.addActionListener(e -> bukaKeranjang());
        JButton kembaliButton = Theme.secondaryButton("Kembali ke Menu");
        kembaliButton.addActionListener(e -> goBack());
        panel.add(cariButton);
        panel.add(resetButton);
        panel.add(keranjangButton);
        panel.add(kembaliButton);
        return panel;
    }

    private JPanel buildPanelBawah() {
        JPanel kiri = Theme.lightPanel();
        kiri.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 6));
        kiri.add(Theme.text("Jumlah Beli"));
        kiri.add(jumlahSpinner);
        kiri.add(giftWrapCheck);
        kiri.add(warrantyCheck);
        JButton tambahButton = Theme.primaryButton("Tambah ke Keranjang");
        tambahButton.addActionListener(e -> tambahKeKeranjang());
        kiri.add(tambahButton);

        JPanel panel = Theme.lightPanel();
        panel.setLayout(new GridLayout(3, 1, 0, 4));
        panel.add(kiri);
        panel.add(hargaLabel);
        panel.add(infoLabel);
        return panel;
    }

    private void muatData() {
        try {
            List<Product> hasil = productService.search(cariField.getText(), (String) kategoriCombo.getSelectedItem());
            produkTampil.clear();
            model.setRowCount(0);
            // Iterator Pattern: menelusuri katalog produk
            ProductCollection collection = productService.toCollection(hasil);
            Iterator<Product> iterator = collection.iterator();
            while (iterator.hasNext()) {
                Product produk = iterator.next();
                produkTampil.add(produk);
                model.addRow(new Object[] {
                    produk.getIdProduk(),
                    produk.getNamaProduk(),
                    produk.getCategory(),
                    produk.getDescription(),
                    Theme.rupiah(produk.calculatePrice()),
                    produk.getJumlahStok(),
                    produk.getStatusStok()
                });
            }
            infoLabel.setText("Jumlah produk tersedia: " + produkTampil.size()
                    + " | Item di keranjang: " + cart.getTotalQuantity() + " pcs");
        } catch (DatabaseException e) {
            Theme.error(this, "Gagal memuat katalog produk: " + e.getMessage());
        }
    }

    private void tambahKeKeranjang() {
        Product produk = produkTerpilih();
        if (produk == null) {
            Theme.info(this, "Pilih produk yang ingin dibeli terlebih dahulu.");
            return;
        }
        int jumlah = (Integer) jumlahSpinner.getValue();
        try {
            // Decorator Pattern: fitur tambahan membungkus produk
            ProductComponent komponen = produk;
            if (warrantyCheck.isSelected()) {
                komponen = new WarrantyDecorator(komponen);
            }
            if (giftWrapCheck.isSelected()) {
                komponen = new GiftWrapDecorator(komponen);
            }
            cart.addItem(produk, jumlah, giftWrapCheck.isSelected(), warrantyCheck.isSelected());
            updateKeranjangButton();
            muatData();
            Theme.info(this, jumlah + " " + komponen.getName() + " masuk keranjang.\n"
                    + "Harga satuan: " + Theme.rupiah(komponen.getPrice()) + "\n"
                    + "Subtotal: " + Theme.rupiah(komponen.getPrice() * jumlah) + "\n"
                    + "Total keranjang: " + Theme.rupiah(cart.getTotal()));
        } catch (IllegalArgumentException e) {
            Theme.error(this, e.getMessage());
        }
    }

    private void updatePreview() {
        Product produk = produkTerpilih();
        if (produk == null) {
            hargaLabel.setText("Harga item terpilih: -");
            return;
        }
        ProductComponent komponen = produk;
        if (warrantyCheck.isSelected()) {
            komponen = new WarrantyDecorator(komponen);
        }
        if (giftWrapCheck.isSelected()) {
            komponen = new GiftWrapDecorator(komponen);
        }
        int jumlah = (Integer) jumlahSpinner.getValue();
        hargaLabel.setText("Item terpilih: " + produk.getNamaProduk() + " | Harga satuan setelah fitur tambahan: "
                + Theme.rupiah(komponen.getPrice()) + " | Subtotal " + jumlah + " pcs: "
                + Theme.rupiah(komponen.getPrice() * jumlah));
    }

    private void updateKeranjangButton() {
        keranjangButton.setText("Lihat Keranjang (" + cart.getTotalQuantity() + " pcs)");
    }

    private void bukaKeranjang() {
        setVisible(false);
        new CartFrame(this, cart).setVisible(true);
    }

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