package strategy;

/** Strategy Pattern (tambahan): harga normal tanpa diskon. */
public class NormalPricing implements PricingStrategy {

    @Override
    public String getName() {
        return "Normal (tanpa diskon)";
    }

    @Override
    public double calculatePrice(double basePrice) {
        return Math.round(basePrice);
    }
}