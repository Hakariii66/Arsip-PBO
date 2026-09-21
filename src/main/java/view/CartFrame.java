package view;

import model.Cart;
import model.CartItem;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Iterator;

/**
 * Halaman keranjang belanja customer (PRD bagian 5.7).
 *
 * Customer dapat melihat item terpilih, mengubah jumlah, menghapus item,
 * melihat subtotal setiap item dan total transaksi, lalu melanjutkan ke
 * checkout. Penelusuran item memakai {@link java.util.Iterator} (Iterator Pattern).
 */
public class CartFrame extends BaseFrame {

    private static final long serialVersionUID = 1L;

    private final Cart cart;

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[] { "No", "Produk", "Fitur Tambahan", "Harga Satuan", "Qty", "Subtotal" }, 0);
    private final JTable tabel = new JTable(model);
    private final JLabel totalLabel = Theme.textBold("Total Transaksi: ");
    private final JLabel infoLabel = Theme.text(" ");

    public CartFrame(JFrame buyerFrame, Cart cart) {
        super("Keranjang Belanja - SITAG", buyerFrame);
        this.cart = cart;

        Theme.styleTable(tabel);
        tabel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        setSize(960, 600);
        add(Theme.headerPanel("Keranjang Belanja", "Ubah jumlah, hapus item, lalu lanjut ke checkout",
                clockLabel(false)), BorderLayout.NORTH);

        JPanel isi = Theme.lightPanel();
        isi.setLayout(new BorderLayout(0, 10));
        isi.setBorder(Theme.padding(14));
        isi.add(buildToolbar(), BorderLayout.NORTH);
        isi.add(Theme.scrollPane(tabel), BorderLayout.CENTER);
        isi.add(buildRingkasan(), BorderLayout.SOUTH);
        add(isi, BorderLayout.CENTER);
        center();
        muatKeranjang();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowActivated(WindowEvent e) {
                muatKeranjang();
            }
        });
    }

    private JPanel buildToolbar() {
        JPanel panel = Theme.lightPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 8));

        JButton ubahButton = Theme.primaryButton("Ubah Jumlah");
        ubahButton.addActionListener(e -> ubahJumlah());
        JButton hapusButton = Theme.secondaryButton("Hapus Item");
        hapusButton.addActionListener(e -> hapusItem());
        JButton kosongkanButton = Theme.secondaryButton("Kosongkan");
        kosongkanButton.addActionListener(e -> kosongkan());
        JButton checkoutButton = Theme.primaryButton("Checkout");
        checkoutButton.addActionListener(e -> bukaCheckout());
        JButton kembaliButton = Theme.secondaryButton("Kembali ke Katalog");
        kembaliButton.addActionListener(e -> goBack());

        panel.add(ubahButton);
        panel.add(hapusButton);
        panel.add(kosongkanButton);
        panel.add(checkoutButton);
        panel.add(kembaliButton);
        return panel;
    }

    private JPanel buildRingkasan() {
        JPanel panel = Theme.lightPanel();
        panel.setLayout(new GridLayout(2, 1, 0, 4));
        panel.add(totalLabel);
        panel.add(infoLabel);
        return panel;
    }

    private void muatKeranjang() {
        model.setRowCount(0);
        int nomor = 1;
        // Iterator Pattern: menelusuri item keranjang satu per satu
        Iterator<CartItem> iterator = cart.iterator();
        while (iterator.hasNext()) {
            CartItem item = iterator.next();
            model.addRow(new Object[] {
                nomor++,
                item.getProduct().getNamaProduk(),
                item.getAddOnLabel(),
                Theme.rupiah(item.getUnitPrice(cart.getPricingStrategy())),
                item.getQuantity(),
                Theme.rupiah(item.getSubtotal(cart.getPricingStrategy()))
            });
        }
        totalLabel.setText("Total Transaksi: " + Theme.rupiah(cart.getTotal())
                + "  |  Strategi harga: " + cart.getPricingStrategy().getName());
        infoLabel.setText(cart.isEmpty()
                ? "Keranjang masih kosong, silakan pilih produk pada halaman pembelian."
                : cart.getTotalQuantity() + " pcs produk siap di-checkout.");
    }

    /** Mengubah jumlah beli item terpilih (tidak boleh 0 atau negatif). */
    private void ubahJumlah() {
        int index = barisTerpilih();
        if (index < 0) {
            Theme.info(this, "Pilih satu item keranjang terlebih dahulu.");
            return;
        }
        CartItem item = cart.getItem(index);
        String input = Theme.input(this, "Jumlah baru untuk \"" + item.getProduct().getNamaProduk()
                + "\" (stok tersedia " + item.getProduct().getJumlahStok() + "):",
                String.valueOf(item.getQuantity()));
        if (input == null) {
            return;
        }
        try {
            cart.updateQuantity(index, Integer.parseInt(input.trim()));
            muatKeranjang();
        } catch (NumberFormatException e) {
            Theme.error(this, "Jumlah harus berupa angka bulat lebih dari 0.");
        } catch (IllegalArgumentException e) {
            Theme.error(this, e.getMessage());
        }
    }

    private void hapusItem() {
        int index = barisTerpilih();
        if (index < 0) {
            Theme.info(this, "Pilih satu item keranjang terlebih dahulu.");
            return;
        }
        CartItem item = cart.getItem(index);
        if (!Theme.confirm(this, "Hapus \"" + item.getProduct().getNamaProduk() + "\" dari keranjang?")) {
            return;
        }
        cart.removeItem(index);
        muatKeranjang();
    }

    private void kosongkan() {
        if (cart.isEmpty()) {
            Theme.info(this, "Keranjang sudah kosong.");
            return;
        }
        if (!Theme.confirm(this, "Kosongkan seluruh isi keranjang?")) {
            return;
        }
        cart.clear();
        muatKeranjang();
    }

    private void bukaCheckout() {
        if (cart.isEmpty()) {
            Theme.info(this, "Keranjang masih kosong. Pilih produk terlebih dahulu.");
            return;
        }
        setVisible(false);
        new CheckoutFrame(this, cart).setVisible(true);
    }

    /** Index item pada model keranjang (bukan index baris tampilan tabel). */
    private int barisTerpilih() {
        int barisView = tabel.getSelectedRow();
        return barisView < 0 ? -1 : tabel.convertRowIndexToModel(barisView);
    }
}