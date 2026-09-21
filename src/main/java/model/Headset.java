package model;

/**
 * INHERITANCE + POLYMORPHISM: produk kategori Headset (audio).
 *
 * Contoh polymorphic reference: {@code Product product = new Headset();}
 */
public class Headset extends Product {

    public static final String CATEGORY = "Headset";

    private static final String SPESIFIKASI = "Bluetooth 5.0, baterai 6 jam";

    public Headset() {
        super();
    }

    public Headset(String namaProduk, double hargaBeli, double hargaJual, int jumlahStok, int stokMinimum) {
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