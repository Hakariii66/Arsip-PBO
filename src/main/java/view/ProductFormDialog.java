package view;

import database.DatabaseException;
import model.Product;
import model.ProductFactory;
import service.CurrencyUtil;
import service.ProductService;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;

/**
 * Form tambah / ubah produk (dialog modal, PRD bagian 5.4).
 *
 * Kategori menentukan subclass produk yang dibuat melalui
 * {@link ProductFactory}, sehingga polymorphism terlihat langsung ketika harga
 * ke customer dihitung memakai {@code calculatePrice()}.
 */
public class ProductFormDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private final ProductService productService;
    private final Product produkAsli;

    private final JTextField namaField = Theme.textField(20);
    private final JComboBox<String> kategoriCombo = Theme.comboBox(ProductFactory.categories());
    private final JTextField hargaBeliField = Theme.textField(20);
    private final JTextField hargaJualField = Theme.textField(20);
    private final JTextField stokField = Theme.textField(20);
    private final JTextField stokMinimumField = Theme.textField(20);
    private final JLabel previewLabel = Theme.textBold(" ");

    private boolean tersimpan;

    public ProductFormDialog(Frame owner, ProductService productService, Product produk) {
        super(owner, produk == null ? "Tambah Produk" : "Ubah Produk", true);
        this.productService = productService;
        this.produkAsli = produk;

        setSize(520, 480);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Theme.LIGHT_BLUE_SOFT);
        add(Theme.headerPanel(produk == null ? "Tambah Produk" : "Ubah Produk",
                "Kategori menentukan aturan harga produk", null), BorderLayout.NORTH);
        add(buildForm(), BorderLayout.CENTER);
        add(buildTombol(), BorderLayout.SOUTH);
        isiForm();
        setLocationRelativeTo(owner);
    }

    private JPanel buildForm() {
        JPanel panel = Theme.lightPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBorder(Theme.padding(18));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        baris(panel, gbc, 0, "Nama Produk", namaField);
        baris(panel, gbc, 1, "Kategori", kategoriCombo);
        baris(panel, gbc, 2, "Harga Beli", hargaBeliField);
        baris(panel, gbc, 3, "Harga Jual", hargaJualField);
        baris(panel, gbc, 4, "Jumlah Stok", stokField);
        baris(panel, gbc, 5, "Stok Minimum", stokMinimumField);

        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        panel.add(previewLabel, gbc);
        return panel;
    }

    private void baris(JPanel panel, GridBagConstraints gbc, int baris, String label, java.awt.Component field) {
        gbc.gridx = 0;
        gbc.gridy = baris;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        panel.add(Theme.text(label), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(field, gbc);
    }

    private JPanel buildTombol() {
        JButton simpanButton = Theme.primaryButton("Simpan");
        simpanButton.addActionListener(e -> simpan());
        JButton batalButton = Theme.secondaryButton("Batal");
        batalButton.addActionListener(e -> dispose());

        JPanel panel = Theme.lightPanel();
        panel.setLayout(new GridLayout(1, 2, 10, 10));
        panel.setBorder(Theme.padding(14));
        panel.add(simpanButton);
        panel.add(batalButton);
        getRootPane().setDefaultButton(simpanButton);
        return panel;
    }

    private void isiForm() {
        kategoriCombo.addActionListener(e -> updatePreview());
        hargaJualField.getDocument().addDocumentListener(new PreviewListener());
        if (produkAsli != null) {
            namaField.setText(produkAsli.getNamaProduk());
            kategoriCombo.setSelectedItem(produkAsli.getCategory());
            hargaBeliField.setText(String.valueOf((long) produkAsli.getHargaBeli()));
            hargaJualField.setText(String.valueOf((long) produkAsli.getHargaJual()));
            stokField.setText(String.valueOf(produkAsli.getJumlahStok()));
            stokMinimumField.setText(String.valueOf(produkAsli.getStokMinimum()));
        } else {
            hargaBeliField.setText("0");
            hargaJualField.setText("0");
            stokField.setText("0");
            stokMinimumField.setText("0");
        }
        updatePreview();
    }

    private void updatePreview() {
        try {
            Product contoh = ProductFactory.create((String) kategoriCombo.getSelectedItem());
            contoh.setHargaJual(CurrencyUtil.parse(hargaJualField.getText()));
            previewLabel.setText("Harga jual ke customer (" + contoh.getCategory() + "): "
                    + Theme.rupiah(contoh.calculatePrice()));
        } catch (RuntimeException e) {
            previewLabel.setText("Harga jual ke customer: -");
        }
    }

    private void simpan() {
        try {
            String kategori = (String) kategoriCombo.getSelectedItem();
            Product produk = ProductFactory.create(kategori);
            if (produkAsli != null) {
                produk.setIdProduk(produkAsli.getIdProduk());
            }
            produk.setNamaProduk(namaField.getText().trim());
            produk.setHargaBeli(CurrencyUtil.parse(hargaBeliField.getText()));
            produk.setHargaJual(CurrencyUtil.parse(hargaJualField.getText()));
            produk.setJumlahStok(parseAngka(stokField.getText(), "Jumlah stok"));
            produk.setStokMinimum(parseAngka(stokMinimumField.getText(), "Stok minimum"));

            if (produkAsli == null) {
                productService.saveNew(produk);
            } else {
                productService.update(produk);
            }
            tersimpan = true;
            dispose();
        } catch (IllegalArgumentException e) {
            Theme.error(this, e.getMessage());
        } catch (DatabaseException e) {
            Theme.error(this, "Gagal menyimpan produk: " + e.getMessage());
        }
    }

    private int parseAngka(String text, String label) {
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(label + " harus berupa angka bulat.");
        }
    }

    /** True bila data berhasil disimpan (dipakai ProductFrame untuk refresh tabel). */
    public boolean isTersimpan() {
        return tersimpan;
    }

    /** Listener untuk memperbarui pratinjau harga saat harga jual diketik. */
    private final class PreviewListener implements DocumentListener {

        @Override
        public void insertUpdate(DocumentEvent e) {
            updatePreview();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            updatePreview();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            updatePreview();
        }
    }
}