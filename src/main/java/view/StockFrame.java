package view;

import database.DatabaseException;
import iterator.ProductCollection;
import model.Product;
import service.ProductService;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Halaman manajemen stok (PRD bagian 5.5).
 *
 * Admin dapat melihat stok saat ini, melakukan restock, menentukan stok
 * minimum, dan melihat peringatan stok menipis
 * ({@code jumlah_stok <= stok_minimum}).
 */
public class StockFrame extends BaseFrame {

    private static final long serialVersionUID = 1L;

    private static final int KOLOM_STATUS = 5;

    private final ProductService productService = new ProductService();
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[] { "ID", "Nama Produk", "Kategori", "Stok", "Stok Min", "Status" }, 0);
    private final JTable tabel = new JTable(model);
    private final JCheckBox hanyaMenipis = new JCheckBox("Tampilkan hanya stok menipis");
    private final JLabel infoLabel = Theme.textBold(" ");
    private final List<Product> produkTampil = new ArrayList<>();

    public StockFrame(JFrame dashboard) {
        super("Manajemen Stok - SITAG", dashboard);
        Theme.styleTable(tabel);
        Theme.highlightLowStock(tabel, KOLOM_STATUS);
        tabel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        hanyaMenipis.setBackground(Theme.LIGHT_BLUE_SOFT);
        hanyaMenipis.setForeground(Theme.DARK_BLUE);
        hanyaMenipis.setFont(Theme.FONT_BODY_BOLD);
        hanyaMenipis.addActionListener(e -> muatData());

        setSize(900, 600);
        add(Theme.headerPanel("Manajemen Stok", "Restock dan pantau peringatan stok minimum",
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

    private JPanel buildToolbar() {
        JPanel atas = Theme.lightPanel();
        atas.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 8));
        atas.add(hanyaMenipis);

        JButton refreshButton = Theme.primaryButton("Refresh");
        refreshButton.addActionListener(e -> muatData());
        JButton restockButton = Theme.primaryButton("Restock");
        restockButton.addActionListener(e -> restock());
        JButton minimumButton = Theme.secondaryButton("Ubah Stok Minimum");
        minimumButton.addActionListener(e -> ubahStokMinimum());
        JButton kembaliButton = Theme.secondaryButton("Kembali");
        kembaliButton.addActionListener(e -> goBack());

        JPanel bawah = Theme.lightPanel();
        bawah.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 4));
        bawah.add(refreshButton);
        bawah.add(restockButton);
        bawah.add(minimumButton);
        bawah.add(kembaliButton);

        JPanel toolbar = Theme.lightPanel();
        toolbar.setLayout(new GridLayout(2, 1));
        toolbar.add(atas);
        toolbar.add(bawah);
        return toolbar;
    }

    private void muatData() {
        try {
            List<Product> hasil = hanyaMenipis.isSelected()
                    ? productService.findLowStock()
                    : productService.findAll();
            produkTampil.clear();
            model.setRowCount(0);
            // Iterator Pattern: penelusuran koleksi produk stok
            ProductCollection collection = productService.toCollection(hasil);
            Iterator<Product> iterator = collection.iterator();
            while (iterator.hasNext()) {
                Product produk = iterator.next();
                produkTampil.add(produk);
                model.addRow(new Object[] {
                    produk.getIdProduk(),
                    produk.getNamaProduk(),
                    produk.getCategory(),
                    produk.getJumlahStok(),
                    produk.getStokMinimum(),
                    produk.getStatusStok()
                });
            }
            infoLabel.setText("Menampilkan " + produkTampil.size() + " produk | "
                    + productService.getLowStockWarning());
        } catch (DatabaseException e) {
            Theme.error(this, "Gagal memuat data stok: " + e.getMessage());
        }
    }

    /** Menambah stok produk terpilih (restock). */
    private void restock() {
        Product produk = produkTerpilih();
        if (produk == null) {
            Theme.info(this, "Pilih satu produk pada tabel terlebih dahulu.");
            return;
        }
        String input = Theme.input(this, "Jumlah stok yang ditambahkan untuk \""
                + produk.getNamaProduk() + "\" (stok sekarang " + produk.getJumlahStok() + "):", "10");
        if (input == null) {
            return;
        }
        try {
            int tambahan = Integer.parseInt(input.trim());
            productService.restock(produk.getIdProduk(), tambahan);
            muatData();
            Theme.info(this, "Restock berhasil. Stok " + produk.getNamaProduk() + " kini "
                    + (produk.getJumlahStok() + tambahan) + " pcs.");
        } catch (NumberFormatException e) {
            Theme.error(this, "Jumlah restock harus berupa angka bulat.");
        } catch (IllegalArgumentException | DatabaseException e) {
            Theme.error(this, e.getMessage());
        }
    }

    /** Menentukan kembali batas stok minimum produk terpilih. */
    private void ubahStokMinimum() {
        Product produk = produkTerpilih();
        if (produk == null) {
            Theme.info(this, "Pilih satu produk pada tabel terlebih dahulu.");
            return;
        }
        String input = Theme.input(this, "Stok minimum baru untuk \"" + produk.getNamaProduk()
                + "\" (sekarang " + produk.getStokMinimum() + "):", String.valueOf(produk.getStokMinimum()));
        if (input == null) {
            return;
        }
        try {
            int minimum = Integer.parseInt(input.trim());
            productService.updateStokMinimum(produk.getIdProduk(), minimum);
            muatData();
            Theme.info(this, "Stok minimum " + produk.getNamaProduk() + " berhasil diubah menjadi "
                    + minimum + ".");
        } catch (NumberFormatException e) {
            Theme.error(this, "Stok minimum harus berupa angka bulat.");
        } catch (IllegalArgumentException | DatabaseException e) {
            Theme.error(this, e.getMessage());
        }
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