package model;

import decorator.GiftWrapDecorator;
import decorator.ProductComponent;
import decorator.WarrantyDecorator;
import strategy.PricingStrategy;

import java.util.ArrayList;
import java.util.List;

/**
 * Item keranjang belanja customer.
 *
 * Setiap item menyimpan produk, jumlah beli, dan fitur tambahan opsional.
 * Fitur tambahan direalisasikan dengan Decorator Pattern melalui method
 * {@link #toComponent()}, sehingga harga satuan item otomatis mengikuti
 * fitur yang dipilih tanpa mengubah class produk.
 */
public class CartItem {

    private final Product product;
    private int quantity;
    private final boolean giftWrap;
    private final boolean extendedWarranty;

    public CartItem(Product product, int quantity) {
        this(product, quantity, false, false);
    }

    public CartItem(Product product, int quantity, boolean giftWrap, boolean extendedWarranty) {
        if (product == null) {
            throw new IllegalArgumentException("Produk tidak boleh null.");
        }
        if (quantity < 1) {
            throw new IllegalArgumentException("Jumlah pembelian harus lebih dari 0.");
        }
        this.quantity = quantity;
        this.product = product;
        this.giftWrap = giftWrap;
        this.extendedWarranty = extendedWarranty;
    }

    /**
     * Membungkus produk dengan decorator sesuai fitur tambahan yang dipilih
     * (Decorator Pattern: WarrantyDecorator / GiftWrapDecorator).
     */
    public ProductComponent toComponent() {
        ProductComponent component = product;
        if (extendedWarranty) {
            component = new WarrantyDecorator(component);
        }
        if (giftWrap) {
            component = new GiftWrapDecorator(component);
        }
        return component;
    }

    /** Harga satuan sebelum strategy pricing (sudah termasuk fitur tambahan). */
    public double getUnitPrice() {
        return toComponent().getPrice();
    }

    /** Harga satuan setelah strategy pricing diterapkan. */
    public double getUnitPrice(PricingStrategy pricingStrategy) {
        return pricingStrategy.calculatePrice(getUnitPrice());
    }

    /** Subtotal item = harga satuan (setelah pricing) x jumlah beli. */
    public double getSubtotal(PricingStrategy pricingStrategy) {
        return getUnitPrice(pricingStrategy) * quantity;
    }

    /** Label fitur tambahan untuk GUI / nota. */
    public String getAddOnLabel() {
        List<String> labels = new ArrayList<>();
        if (extendedWarranty) {
            labels.add(WarrantyDecorator.LABEL);
        }
        if (giftWrap) {
            labels.add(GiftWrapDecorator.LABEL);
        }
        return labels.isEmpty() ? "-" : String.join(", ", labels);
    }

    /** Keterangan produk lengkap dengan fitur tambahan (untuk nota). */
    public String getDisplayName() {
        String name = product.getNamaProduk();
        if (extendedWarranty) {
            name += " + Garansi 1 Tahun";
        }
        if (giftWrap) {
            name += " + Gift Wrap";
        }
        return name;
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    /** Mengubah jumlah beli; minimal 1 sesuai business rule PRD. */
    public void setQuantity(int quantity) {
        if (quantity < 1) {
            throw new IllegalArgumentException("Jumlah pembelian harus lebih dari 0.");
        }
        this.quantity = quantity;
    }

    public boolean isGiftWrap() {
        return giftWrap;
    }

    public boolean isExtendedWarranty() {
        return extendedWarranty;
    }

    @Override
    public String toString() {
        return product.getNamaProduk() + " x" + quantity;
    }
}