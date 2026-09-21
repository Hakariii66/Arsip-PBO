package decorator;

/**
 * Decorator Pattern: abstract decorator.
 *
 * Menyimpan referensi ke komponen yang dibungkus dan mendelegasikan seluruh
 * pemanggilan secara default, sehingga subclass hanya perlu meng-override
 * bagian yang ingin ditambahkan (harga/deskripsi).
 */
public abstract class ProductDecorator implements ProductComponent {

    /** Komponen yang dibungkus (produk asli atau decorator lain). */
    protected final ProductComponent component;

    protected ProductDecorator(ProductComponent component) {
        if (component == null) {
            throw new IllegalArgumentException("Komponen produk yang dibungkus tidak boleh null.");
        }
        this.component = component;
    }

    @Override
    public String getName() {
        return component.getName();
    }

    @Override
    public double getPrice() {
        return component.getPrice();
    }

    @Override
    public String getDescription() {
        return component.getDescription();
    }

    /** Mengembalikan komponen yang dibungkus (produk asli / decorator di bawahnya). */
    public ProductComponent getComponent() {
        return component;
    }
}