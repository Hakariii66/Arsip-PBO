package strategy;

/**
 * Strategy Pattern (tambahan): cara menghitung harga satuan produk.
 *
 * Dipakai saat menghitung harga item keranjang, sehingga pemberian diskon
 * dapat diaktifkan/dimatikan tanpa mengubah proses checkout.
 */
public interface PricingStrategy {

    /** Nama strategy, dicetak pada nota (contoh: "Normal (tanpa diskon)"). */
    String getName();

    /** Menghitung harga satuan akhir dari harga dasar. */
    double calculatePrice(double basePrice);
}