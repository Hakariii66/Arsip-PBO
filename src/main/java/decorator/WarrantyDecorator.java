package decorator;

/**
 * Decorator Pattern: menambahkan fitur Extended Warranty (garansi tambahan).
 *
 * Harga bertambah 10% dari harga produk + fitur lain yang sudah dibungkus
 * sebelumnya.
 */
public class WarrantyDecorator extends ProductDecorator {

    /** Label fitur untuk ditampilkan di GUI / nota. */
    public static final String LABEL = "Extended Warranty (+10%)";

    /** Persentase biaya garansi tambahan. */
    public static final double RATE = 0.10;

    public WarrantyDecorator(ProductComponent component) {
        super(component);
    }

    @Override
    public double getPrice() {
        return component.getPrice() * (1 + RATE);
    }

    @Override
    public String getDescription() {
        return component.getDescription() + " + Garansi Tambahan 1 Tahun";
    }

    @Override
    public String toString() {
        return LABEL;
    }
}