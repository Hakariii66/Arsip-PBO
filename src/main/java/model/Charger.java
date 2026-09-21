package model;

/**
 * INHERITANCE + POLYMORPHISM: produk kategori Charger (adaptor / fast charging).
 *
 * Contoh polymorphic reference: {@code Product product = new Charger();}
 */
public class Charger extends Product {

    public static final String CATEGORY = "Charger";

    private static final String SPESIFIKASI = "Fast charging 20W, output Type-C";

    public Charger() {
        super();
    }

    public Charger(String namaProduk, double hargaBeli, double hargaJual, int jumlahStok, int stokMinimum) {
        super(namaProduk, hargaBeli, hargaJual, jumlahStok, stokMinimum);
    }

    @Override
    public String getCategory() {
        return CATEGORY;
    }

    /** Produk bernilai tinggi dibulatkan ke atas per Rp 1.000. */
    @Override
    public double calculatePrice() {
        return roundUpTo(getHargaJual(), 1_000);
    }

    @Override
    public String getDescription() {
        return super.getDescription() + " | " + SPESIFIKASI;
    }
}