package strategy;

/**
 * Strategy Pattern (tambahan): harga setelah potongan diskon persentase.
 *
 * Strategi ini bersifat opsional (promo), sesuai PRD diskon bukan fitur
 * bisnis utama sehingga hanya diaktifkan lewat checkbox pada halaman checkout.
 */
public class DiscountPricing implements PricingStrategy {

    private final double percent;

    public DiscountPricing(double percent) {
        if (percent < 0 || percent > 100) {
            throw new IllegalArgumentException("Persentase diskon harus di antara 0 - 100.");
        }
        this.percent = percent;
    }

    public double getPercent() {
        return percent;
    }

    @Override
    public String getName() {
        return "Diskon " + (percent == Math.rint(percent) ? String.valueOf((long) percent) : String.valueOf(percent)) + "%";
    }

    @Override
    public double calculatePrice(double basePrice) {
        return Math.round(basePrice * (1 - percent / 100.0));
    }
}