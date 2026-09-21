package model;

/**
 * INHERITANCE + POLYMORPHISM: produk kategori Cable (kabel data / charging).
 *
 * Contoh polymorphic reference: {@code Product product = new Cable();}
 */
public class Cable extends Product {

    public static final String CATEGORY = "Cable";

    private static final String SPESIFIKASI = "Kabel data 1 m, konektor Type-C";

    public Cable() {
        super();
    }

    public Cable(String namaProduk, double hargaBeli, double hargaJual, int jumlahStok, int stokMinimum) {
        super(namaProduk, hargaBeli, hargaJual, jumlahStok, stokMinimum);
    }

    @Override
    public String getCategory() {
        return CATEGORY;
    }

    /** Harga kabel dibulatkan ke atas per Rp 100 agar mudah dibayar tunai. */
    @Override
    public double calculatePrice() {
        return roundUpTo(getHargaJual(), 100);
    }

    @Override
    public String getDescription() {
        return super.getDescription() + " | " + SPESIFIKASI;
    }
}