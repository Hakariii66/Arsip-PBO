package decorator;

/**
 * Decorator Pattern: menambahkan fitur Gift Wrapping (bunga/kotak hadiah)
 * dengan biaya tetap.
 */
public class GiftWrapDecorator extends ProductDecorator {

    /** Label fitur untuk ditampilkan di GUI / nota. */
    public static final String LABEL = "Gift Wrapping (+Rp 5.000)";

    /** Biaya tetap jasa gift wrapping. */
    public static final double FEE = 5000;

    public GiftWrapDecorator(ProductComponent component) {
        super(component);
    }

    @Override
    public double getPrice() {
        return component.getPrice() + FEE;
    }

    @Override
    public String getDescription() {
        return component.getDescription() + " + Gift Wrapping";
    }

    @Override
    public String toString() {
        return LABEL;
    }
}