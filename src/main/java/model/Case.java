package model;

/**
 * INHERITANCE + POLYMORPHISM: produk kategori Case (casing pelindung gadget).
 *
 * Contoh polymorphic reference: {@code Product product = new Case();}
 */
public class Case extends Product {

    public static final String CATEGORY = "Case";

    private static final String SPESIFIKASI = "Casing silikon, ukuran universal";

    public Case() {
        super();
    }

    public Case(String namaProduk, double hargaBeli, double hargaJual, int jumlahStok, int stokMinimum) {
        super(namaProduk, hargaBeli, hargaJual, jumlahStok, stokMinimum);
    }

    @Override
    public String getCategory() {
        return CATEGORY;
    }

    /** Harga casing dibulatkan ke atas per Rp 500. */
    @Override
    public double calculatePrice() {
        return roundUpTo(getHargaJual(), 500);
    }

    @Override
    public String getDescription() {
        return super.getDescription() + " | " + SPESIFIKASI;
    }
}