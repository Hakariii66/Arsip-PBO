package iterator;

import model.Product;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Iterator Pattern: implementasi {@link java.util.Iterator} untuk menelusuri
 * koleksi produk satu per satu tanpa membuka struktur penyimpanannya.
 *
 * Collection dapat memakai implementasi ini agar penelusuran produk konsisten
 * di seluruh aplikasi (katalog, laporan stok, dsb).
 */
public class ProductIterator implements Iterator<Product> {

    private final List<Product> products;
    private int position;

    public ProductIterator(List<Product> products) {
        this.products = products == null ? List.of() : products;
        this.position = 0;
    }

    @Override
    public boolean hasNext() {
        return position < products.size();
    }

    @Override
    public Product next() {
        if (!hasNext()) {
            throw new NoSuchElementException("Tidak ada produk lagi pada koleksi.");
        }
        return products.get(position++);
    }

    /** Jumlah produk yang sudah ditelusuri (untuk keterangan GUI). */
    public int getPosition() {
        return position;
    }

    /** Ukuran koleksi yang ditelusuri. */
    public int size() {
        return products.size();
    }
}