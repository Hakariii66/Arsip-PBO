package decorator;

/**
 * Komponen produk (bagian dari Decorator Pattern).
 *
 * Interface ini menjadi kontrak harga &amp; deskripsi sebuah produk, sehingga
 * fitur tambahan (garansi, gift wrapping) dapat ditambahkan sebagai pembungkus
 * tanpa mengubah class produk utama.
 */
public interface ProductComponent {

    /** Nama produk yang ditampilkan ke pengguna. */
    String getName();

    /** Harga satuan produk termasuk seluruh fitur tambahan yang dibungkus. */
    double getPrice();

    /** Deskripsi produk + fitur tambahan yang menempel. */
    String getDescription();
}