package view;

import database.DatabaseException;
import model.Transaction;
import model.TransactionDetail;
import service.TransactionService;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Halaman histori pembelian - hanya dapat diakses admin (PRD bagian 5.9).
 *
 * Menampilkan ID transaksi, nama pembeli, tanggal, waktu, total, dan metode
 * pembayaran, lengkap dengan detail produk setiap transaksi serta tombol untuk
 * melihat isi nota .txt hasil transaksi.
 */
public class TransactionFrame extends BaseFrame {

    private static final long serialVersionUID = 1L;

    private static final DateTimeFormatter FORMAT_TANGGAL = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter FORMAT_WAKTU = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final TransactionService transactionService = new TransactionService();

    private final DefaultTableModel modelHistori = new DefaultTableModel(
            new Object[] { "ID", "Nama Pembeli", "Tanggal", "Waktu", "Total", "Metode" }, 0);
    private final JTable tabelHistori = new JTable(modelHistori);

    private final DefaultTableModel modelDetail = new DefaultTableModel(
            new Object[] { "Produk", "Jumlah", "Harga", "Subtotal" }, 0);
    private final JTable tabelDetail = new JTable(modelDetail);

    private final JLabel infoLabel = Theme.textBold(" ");
    private final List<Transaction> daftarTransaksi = new ArrayList<>();

    public TransactionFrame(JFrame dashboard) {
        super("Histori Pembelian - SITAG", dashboard);
        Theme.styleTable(tabelHistori);
        Theme.styleTable(tabelDetail);
        tabelHistori.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelHistori.getSelectionModel().addListSelectionListener(this::pilihTransaksi);
        tabelDetail.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        setSize(1000, 680);
        add(Theme.headerPanel("Histori Pembelian", "Khusus admin: seluruh transaksi customer",
                clockLabel(false)), BorderLayout.NORTH);

        JPanel isi = Theme.lightPanel();
        isi.setLayout(new BorderLayout(0, 10));
        isi.setBorder(Theme.padding(14));
        isi.add(buildToolbar(), BorderLayout.NORTH);
        isi.add(buildTabel(), BorderLayout.CENTER);
        isi.add(infoLabel, BorderLayout.SOUTH);
        add(isi, BorderLayout.CENTER);
        center();
        muatHistori();
    }

    private JPanel buildToolbar() {
        JPanel panel = Theme.lightPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 8));

        JButton refreshButton = Theme.primaryButton("Refresh");
        refreshButton.addActionListener(e -> muatHistori());
        JButton notaButton = Theme.primaryButton("Lihat Nota");
        notaButton.addActionListener(e -> lihatNota());
        JButton kembaliButton = Theme.secondaryButton("Kembali");
        kembaliButton.addActionListener(e -> goBack());

        panel.add(refreshButton);
        panel.add(notaButton);
        panel.add(kembaliButton);
        return panel;
    }

    private JPanel buildTabel() {
        JPanel panelHistori = Theme.lightPanel();
        panelHistori.setLayout(new BorderLayout(0, 6));
        panelHistori.add(Theme.subtitle("Daftar Transaksi"), BorderLayout.NORTH);
        panelHistori.add(Theme.scrollPane(tabelHistori), BorderLayout.CENTER);

        JPanel panelDetail = Theme.lightPanel();
        panelDetail.setLayout(new BorderLayout(0, 6));
        panelDetail.add(Theme.subtitle("Detail Produk Transaksi Terpilih"), BorderLayout.NORTH);
        panelDetail.add(Theme.scrollPane(tabelDetail), BorderLayout.CENTER);

        JPanel panel = Theme.lightPanel();
        panel.setLayout(new GridLayout(2, 1, 0, 12));
        panel.add(panelHistori);
        panel.add(panelDetail);
        return panel;
    }

    private void muatHistori() {
        try {
            List<Transaction> hasil = transactionService.getHistory();
            daftarTransaksi.clear();
            modelHistori.setRowCount(0);
            modelDetail.setRowCount(0);
            for (Transaction transaksi : hasil) {
                daftarTransaksi.add(transaksi);
                modelHistori.addRow(new Object[] {
                    transaksi.getIdTransaksi(),
                    transaksi.getNamaPembeli(),
                    transaksi.getTanggal().format(FORMAT_TANGGAL),
                    transaksi.getWaktu().format(FORMAT_WAKTU),
                    Theme.rupiah(transaksi.getTotal()),
                    transaksi.getMetodePembayaran()
                });
            }
            infoLabel.setText("Total " + daftarTransaksi.size() + " transaksi | Transaksi hari ini: "
                    + transactionService.countToday() + " | Penjualan hari ini: "
                    + Theme.rupiah(transactionService.sumToday()));
        } catch (DatabaseException e) {
            Theme.error(this, "Gagal memuat histori pembelian: " + e.getMessage());
        }
    }

    /** Menampilkan detail produk ketika baris transaksi dipilih. */
    private void pilihTransaksi(ListSelectionEvent event) {
        if (event.getValueIsAdjusting()) {
            return;
        }
        modelDetail.setRowCount(0);
        Transaction transaksi = historiTerpilih();
        if (transaksi == null) {
            return;
        }
        try {
            for (TransactionDetail detail : transactionService.getDetails(transaksi.getIdTransaksi())) {
                modelDetail.addRow(new Object[] {
                    detail.getNamaProduk(),
                    detail.getJumlah(),
                    Theme.rupiah(detail.getHarga()),
                    Theme.rupiah(detail.getSubtotal())
                });
            }
        } catch (DatabaseException e) {
            Theme.error(this, "Gagal memuat detail transaksi: " + e.getMessage());
        }
    }

    private Transaction historiTerpilih() {
        int barisView = tabelHistori.getSelectedRow();
        if (barisView < 0) {
            return null;
        }
        int barisModel = tabelHistori.convertRowIndexToModel(barisView);
        if (barisModel < 0 || barisModel >= daftarTransaksi.size()) {
            return null;
        }
        return daftarTransaksi.get(barisModel);
    }

    /** Menampilkan isi nota .txt transaksi terpilih. */
    private void lihatNota() {
        Transaction transaksi = historiTerpilih();
        if (transaksi == null) {
            Theme.info(this, "Pilih satu transaksi pada tabel terlebih dahulu.");
            return;
        }
        try {
            String isiNota = transactionService.readReceipt(transaksi);
            String namaFile = transactionService.getReceiptFileName(transaksi);
            if (isiNota == null) {
                Theme.info(this, "File nota tidak ditemukan pada folder nota/.\nNama file yang diharapkan: "
                        + namaFile);
                return;
            }
            tampilkanNota(namaFile, isiNota);
        } catch (IllegalStateException e) {
            Theme.error(this, e.getMessage());
        }
    }

    /** Dialog read-only berisi isi nota. */
    private void tampilkanNota(String namaFile, String isiNota) {
        javax.swing.JDialog dialog = new javax.swing.JDialog(this, "Nota Pembelian", true);
        javax.swing.JTextArea area = new javax.swing.JTextArea(isiNota, 26, 76);
        area.setEditable(false);
        area.setFont(new java.awt.Font("Consolas", java.awt.Font.PLAIN, 12));
        area.setBackground(Theme.LIGHT_BLUE_SOFT);
        area.setForeground(Theme.DARK_BLUE);
        area.setCaretPosition(0);

        JButton tutupButton = Theme.secondaryButton("Tutup");
        tutupButton.addActionListener(e -> dialog.dispose());
        JPanel bawah = Theme.lightPanel();
        bawah.setLayout(new FlowLayout(FlowLayout.CENTER));
        bawah.add(tutupButton);

        dialog.setLayout(new BorderLayout());
        dialog.add(Theme.headerPanel("Isi Nota Pembelian", "Folder nota/ | file: " + namaFile, null),
                BorderLayout.NORTH);
        dialog.add(Theme.scrollPane(area), BorderLayout.CENTER);
        dialog.add(bawah, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
        dialog.dispose();
    }
}