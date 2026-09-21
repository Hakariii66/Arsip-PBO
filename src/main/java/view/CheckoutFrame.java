package view;

import database.DatabaseException;
import model.Cart;
import model.CartItem;
import model.Transaction;
import service.CurrencyUtil;
import service.TransactionService;
import strategy.CashPayment;
import strategy.DiscountPricing;
import strategy.NormalPricing;
import strategy.PaymentStrategy;
import strategy.TransferPayment;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Iterator;

/**
 * Halaman checkout customer (PRD bagian 5.8).
 *
 * Customer mengisi nama pembeli dan memilih metode pembayaran (Cash / Transfer).
 * Metode pembayaran memakai Strategy Pattern ({@link PaymentStrategy}) dan
 * pilihan promo memakai Strategy Pattern harga (PricingStrategy), sehingga
 * keduanya dapat diganti tanpa mengubah proses checkout.
 */
public class CheckoutFrame extends BaseFrame {

    private static final long serialVersionUID = 1L;

    private final Cart cart;
    private final TransactionService transactionService = new TransactionService();

    private final JTextField namaField = Theme.textField(20);
    private final JRadioButton cashRadio = new JRadioButton("Cash (Tunai)");
    private final JRadioButton transferRadio = new JRadioButton("Transfer Bank");
    private final JTextField uangField = Theme.textField(20);
    private final JCheckBox promoCheck = new JCheckBox("Aktifkan promo diskon 5% (opsional)");
    private final JLabel metodeInfoLabel = Theme.text(" ");
    private final JLabel kembalianLabel = Theme.textBold(" ");

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[] { "Produk", "Fitur Tambahan", "Qty", "Harga", "Subtotal" }, 0);
    private final JTable tabel = new JTable(model);
    private final JLabel totalLabel = Theme.textBold(" ");

    public CheckoutFrame(JFrame cartFrame, Cart cart) {
        super("Checkout - SITAG", cartFrame);
        this.cart = cart;

        Theme.styleTable(tabel);
        promoCheck.setBackground(Theme.LIGHT_BLUE_SOFT);
        promoCheck.setForeground(Theme.DARK_BLUE);
        cashRadio.setBackground(Theme.LIGHT_BLUE_SOFT);
        cashRadio.setForeground(Theme.DARK_BLUE);
        transferRadio.setBackground(Theme.LIGHT_BLUE_SOFT);
        transferRadio.setForeground(Theme.DARK_BLUE);
        cashRadio.setSelected(true);
        ButtonGroup grup = new ButtonGroup();
        grup.add(cashRadio);
        grup.add(transferRadio);

        setSize(880, 660);
        add(Theme.headerPanel("Checkout Pembelian", "Isi nama pembeli dan pilih metode pembayaran",
                clockLabel(false)), BorderLayout.NORTH);

        JPanel isi = Theme.lightPanel();
        isi.setLayout(new BorderLayout(0, 12));
        isi.setBorder(Theme.padding(14));
        isi.add(buildForm(), BorderLayout.NORTH);
        isi.add(buildRingkasan(), BorderLayout.CENTER);
        isi.add(buildTombol(), BorderLayout.SOUTH);
        add(isi, BorderLayout.CENTER);
        center();

        cashRadio.addActionListener(e -> updateMetode());
        transferRadio.addActionListener(e -> updateMetode());
        uangField.getDocument().addDocumentListener(new UangListener());
        promoCheck.addActionListener(e -> updateStrategiHarga());
        muatRingkasan();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                // Strategy harga dikembalikan ke normal ketika halaman checkout ditutup,
                // agar diskon promo hanya berlaku untuk transaksi yang benar-benar dibayar.
                cart.setPricingStrategy(new NormalPricing());
            }
        });
    }

    private JPanel buildForm() {
        JPanel panel = Theme.lightPanel();
        panel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        panel.add(Theme.text("Nama Pembeli"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(namaField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        panel.add(Theme.text("Metode Bayar"), gbc);
        JPanel metode = Theme.lightPanel();
        metode.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 0));
        metode.add(cashRadio);
        metode.add(transferRadio);
        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(metode, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        panel.add(Theme.text("Uang Diterima"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(uangField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        panel.add(promoCheck, gbc);
        gbc.gridy = 4;
        panel.add(metodeInfoLabel, gbc);
        gbc.gridy = 5;
        panel.add(kembalianLabel, gbc);
        return panel;
    }

    private JPanel buildRingkasan() {
        JPanel panel = Theme.lightPanel();
        panel.setLayout(new BorderLayout(0, 6));
        panel.add(Theme.subtitle("Ringkasan Item yang Dibeli"), BorderLayout.NORTH);
        panel.add(Theme.scrollPane(tabel), BorderLayout.CENTER);
        panel.add(totalLabel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildTombol() {
        JButton bayarButton = Theme.primaryButton("Bayar & Simpan Transaksi");
        bayarButton.addActionListener(e -> bayar());
        JButton batalButton = Theme.secondaryButton("Batal");
        batalButton.addActionListener(e -> goBack());

        JPanel panel = Theme.lightPanel();
        panel.setLayout(new GridLayout(1, 2, 10, 10));
        panel.add(bayarButton);
        panel.add(batalButton);
        getRootPane().setDefaultButton(bayarButton);
        return panel;
    }

    /** Menampilkan ringkasan item keranjang memakai strategy harga yang aktif. */
    private void muatRingkasan() {
        model.setRowCount(0);
        // Iterator Pattern: menelusuri item keranjang
        Iterator<CartItem> iterator = cart.iterator();
        while (iterator.hasNext()) {
            CartItem item = iterator.next();
            model.addRow(new Object[] {
                item.getProduct().getNamaProduk(),
                item.getAddOnLabel(),
                item.getQuantity(),
                Theme.rupiah(item.getUnitPrice(cart.getPricingStrategy())),
                Theme.rupiah(item.getSubtotal(cart.getPricingStrategy()))
            });
        }
        totalLabel.setText("Total yang harus dibayar: " + Theme.rupiah(cart.getTotal())
                + "  |  Strategi harga: " + cart.getPricingStrategy().getName());
        uangField.setText(String.valueOf((long) Math.ceil(cart.getTotal())));
        updateMetode();
    }

    /** Mengganti Strategy harga saat promo diaktifkan/dimatikan. */
    private void updateStrategiHarga() {
        cart.setPricingStrategy(promoCheck.isSelected() ? new DiscountPricing(5) : new NormalPricing());
        muatRingkasan();
    }

    private void updateMetode() {
        boolean tunai = cashRadio.isSelected();
        uangField.setEnabled(tunai);
        if (tunai) {
            metodeInfoLabel.setText("Tunai: masukkan uang yang diterima dari pembeli untuk menghitung kembalian.");
            updateKembalian();
        } else {
            TransferPayment transfer = new TransferPayment();
            metodeInfoLabel.setText("Transfer ke " + transfer.getTujuanTransfer()
                    + " (status pembayaran: BERHASIL - simulasi tanpa payment gateway).");
            kembalianLabel.setText("Nominal transfer: " + Theme.rupiah(cart.getTotal()));
        }
    }

    private void updateKembalian() {
        if (!cashRadio.isSelected()) {
            return;
        }
        double total = cart.getTotal();
        try {
            double diterima = CurrencyUtil.parse(uangField.getText());
            if (diterima < total) {
                kembalianLabel.setText("Uang tunai kurang " + Theme.rupiah(total - diterima)
                        + " dari total pembayaran.");
            } else {
                kembalianLabel.setText("Kembalian: " + Theme.rupiah(new CashPayment().getChange(total, diterima)));
            }
        } catch (RuntimeException e) {
            kembalianLabel.setText("Kembalian: -");
        }
    }

    /** Proses checkout: strategy pembayaran -> simpan transaksi + kurangi stok -> nota .txt. */
    private void bayar() {
        PaymentStrategy paymentStrategy = cashRadio.isSelected() ? new CashPayment() : new TransferPayment();
        try {
            double diterima = paymentStrategy.isCashInputRequired()
                    ? CurrencyUtil.parse(uangField.getText())
                    : cart.getTotal();
            Transaction transaksi = transactionService.checkout(cart, namaField.getText(), paymentStrategy, diterima);
            Theme.info(this, "TRANSAKSI BERHASIL\n\n"
                    + "ID Transaksi     : " + transaksi.getIdTransaksi() + "\n"
                    + "Nama Pembeli     : " + transaksi.getNamaPembeli() + "\n"
                    + "Tanggal / Waktu  : " + transaksi.getTanggal() + " " + transaksi.getWaktu().withNano(0) + "\n"
                    + "Metode Bayar     : " + transaksi.getMetodePembayaran() + "\n"
                    + "Total            : " + Theme.rupiah(transaksi.getTotal()) + "\n"
                    + paymentStrategy.getPaymentNote(transaksi.getTotal(), diterima) + "\n\n"
                    + "Nota tersimpan pada file:\n" + transaksi.getNotaPath() + "\n\n"
                    + "Stok produk otomatis berkurang dan transaksi tersimpan di database.");
            cart.clear();
            closeTo(getBackTarget());
        } catch (IllegalArgumentException e) {
            Theme.error(this, e.getMessage());
        } catch (DatabaseException e) {
            Theme.error(this, "Transaksi gagal disimpan: " + e.getMessage());
        } catch (IllegalStateException e) {
            Theme.error(this, "Transaksi sudah tersimpan di database, tetapi nota gagal dibuat:\n"
                    + e.getMessage());
        }
    }

    /** Listener untuk menghitung kembalian saat uang diterima diketik. */
    private final class UangListener implements DocumentListener {

        @Override
        public void insertUpdate(DocumentEvent e) {
            updateKembalian();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            updateKembalian();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            updateKembalian();
        }
    }
}