package iterator;

import model.Product;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Iterator Pattern: aggregate (koleksi) produk.
 *
 * Class ini membungkus daftar produk dan menyediakan {@link ProductIterator},
 * sehingga penelusuran data tidak bergantung pada tipe koleksi di dalamnya
 * (encapsulation koleksi).
 */
public class ProductCollection implements Iterable<Product> {

    private final List<Product> products = new ArrayList<>();

    public ProductCollection() {
        // koleksi kosong
    }

    public ProductCollection(List<Product> products) {
        if (products != null) {
            this.products.addAll(products);
        }
    }

    public void add(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Produk tidak boleh null.");
        }
        products.add(product);
    }

    public int size() {
        return products.size();
    }

    public boolean isEmpty() {
        return products.isEmpty();
    }

    /** Salinan isi koleksi dalam bentuk list read-only. */
    public List<Product> toList() {
        return Collections.unmodifiableList(products);
    }

    /** Mengembalikan iterator khusus produk (implementasi Iterator Pattern). */
    @Override
    public Iterator<Product> iterator() {
        return new ProductIterator(products);
    }
}