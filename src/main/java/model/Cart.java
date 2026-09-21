package model;

import strategy.NormalPricing;
import strategy.PricingStrategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Keranjang belanja customer (ENCAPSULATION).
 *
 * Daftar item disimpan private; penelusuran item dilakukan dengan
 * {@link java.util.Iterator} (Iterator Pattern) melalui {@link #iterator()}
 * sehingga isi koleksi tidak dapat diubah dari luar tanpa method resmi.
 */
public class Cart implements Iterable<CartItem> {

    private final List<CartItem> items = new ArrayList<>();
    private PricingStrategy pricingStrategy = new NormalPricing();

    /**
     * Menambahkan produk ke keranjang. Jika produk dengan fitur tambahan yang
     * sama sudah ada, jumlahnya langsung ditambahkan.
     *
     * @throws IllegalArgumentException jika jumlah <= 0 atau stok tidak cukup.
     */
    public void addItem(Product product, int quantity, boolean giftWrap, boolean extendedWarranty) {
        if (quantity < 1) {
            throw new IllegalArgumentException("Jumlah pembelian harus lebih dari 0.");
        }
        if (quantity > product.getJumlahStok()) {
            throw new IllegalArgumentException("Stok " + product.getNamaProduk() + " hanya "
                    + product.getJumlahStok() + " pcs, tidak cukup untuk " + quantity + " pcs.");
        }
        for (CartItem item : items) {
            boolean sameProduct = item.getProduct().getIdProduk() == product.getIdProduk();
            if (sameProduct && item.isGiftWrap() == giftWrap && item.isExtendedWarranty() == extendedWarranty) {
                int total = item.getQuantity() + quantity;
                if (total > product.getJumlahStok()) {
                    throw new IllegalArgumentException("Total " + total + " pcs melebihi stok "
                            + product.getNamaProduk() + " (" + product.getJumlahStok() + " pcs).");
                }
                item.setQuantity(total);
                return;
            }
        }
        items.add(new CartItem(product, quantity, giftWrap, extendedWarranty));
    }

    public void addItem(Product product, int quantity) {
        addItem(product, quantity, false, false);
    }

    /** Mengubah jumlah beli item pada posisi tertentu. */
    public void updateQuantity(int index, int quantity) {
        CartItem item = getItem(index);
        if (quantity > item.getProduct().getJumlahStok()) {
            throw new IllegalArgumentException("Stok " + item.getProduct().getNamaProduk() + " hanya "
                    + item.getProduct().getJumlahStok() + " pcs.");
        }
        item.setQuantity(quantity);
    }

    /** Menghapus satu item dari keranjang. */
    public void removeItem(int index) {
        items.remove(getItem(index));
    }

    /** Mengosongkan keranjang (dipakai setelah transaksi berhasil). */
    public void clear() {
        items.clear();
    }

    public CartItem getItem(int index) {
        if (index < 0 || index >= items.size()) {
            throw new IllegalArgumentException("Item keranjang tidak ditemukan.");
        }
        return items.get(index);
    }

    /** Daftar item read-only untuk ditampilkan pada GUI. */
    public List<CartItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public int size() {
        return items.size();
    }

    /** Total seluruh item setelah strategy pricing diterapkan. */
    public double getTotal() {
        double total = 0;
        for (CartItem item : items) {
            total += item.getSubtotal(pricingStrategy);
        }
        return total;
    }

    /** Total harga dasar sebelum strategy pricing (untuk menampilkan potongan diskon). */
    public double getTotalSebelumPricing() {
        double total = 0;
        for (CartItem item : items) {
            total += item.getUnitPrice() * item.getQuantity();
        }
        return total;
    }

    /** Total seluruh pcs produk pada keranjang. */
    public int getTotalQuantity() {
        int total = 0;
        for (CartItem item : items) {
            total += item.getQuantity();
        }
        return total;
    }

    public PricingStrategy getPricingStrategy() {
        return pricingStrategy;
    }

    /** Mengganti strategy harga (Strategy Pattern), mis. NormalPricing -> DiscountPricing. */
    public void setPricingStrategy(PricingStrategy pricingStrategy) {
        if (pricingStrategy == null) {
            throw new IllegalArgumentException("Strategy pricing tidak boleh null.");
        }
        this.pricingStrategy = pricingStrategy;
    }

    @Override
    public Iterator<CartItem> iterator() {
        return items.iterator();
    }
}