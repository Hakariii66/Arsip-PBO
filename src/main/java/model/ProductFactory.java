package model;

/**
 * Factory sederhana untuk membuat subclass {@link Product} dari nilai kategori.
 *
 * Dipakai oleh DAO saat membaca tabel {@code produk} sehingga data dari
 * database tetap dipetakan ke subclass yang tepat (mendukung polymorphism).
 */
public final class ProductFactory {

    private ProductFactory() {
        // Kelas utilitas tidak untuk di-instansiasi.
    }

    /** Daftar kategori yang didukung aplikasi (dipakai combo box pada GUI). */
    public static String[] categories() {
        return new String[] { Cable.CATEGORY, Charger.CATEGORY, Case.CATEGORY, Headset.CATEGORY };
    }

    public static boolean isValidCategory(String kategori) {
        if (kategori == null) {
            return false;
        }
        for (String category : categories()) {
            if (category.equalsIgnoreCase(kategori.trim())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Membuat object produk sesuai kategori.
     *
     * @throws IllegalArgumentException jika kategori tidak dikenal.
     */
    public static Product create(String kategori) {
        if (kategori == null) {
            throw new IllegalArgumentException("Kategori produk tidak boleh kosong.");
        }
        String normalized = kategori.trim();
        if (Cable.CATEGORY.equalsIgnoreCase(normalized)) {
            return new Cable();
        }
        if (Charger.CATEGORY.equalsIgnoreCase(normalized)) {
            return new Charger();
        }
        if (Case.CATEGORY.equalsIgnoreCase(normalized)) {
            return new Case();
        }
        if (Headset.CATEGORY.equalsIgnoreCase(normalized)) {
            return new Headset();
        }
        throw new IllegalArgumentException("Kategori produk tidak dikenal: " + kategori
                + ". Kategori yang tersedia: Cable, Charger, Case, Headset.");
    }
}