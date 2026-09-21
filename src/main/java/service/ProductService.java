package service;

import dao.ProductDAO;
import iterator.ProductCollection;
import model.Product;
import model.ProductFactory;

import java.util.List;

/**
 * Service business logic produk &amp; stok.
 *
 * GUI hanya berbicara dengan service ini (bukan langsung ke DAO), sehingga
 * aturan bisnis PRD (validasi input, stok minimum, restock) terkumpul di satu
 * tempat dan dapat dipakai ulang oleh admin maupun customer.
 */
public class ProductService {

    private final ProductDAO productDAO = new ProductDAO();

    public List<Product> findAll() {
        return productDAO.findAll();
    }

    public List<Product> search(String keyword, String kategori) {
        return productDAO.search(keyword, kategori);
    }

    public Product findById(int idProduk) {
        return productDAO.findById(idProduk);
    }

    /** Produk dengan stok menipis: jumlah_stok <= stok_minimum (business rule no. 13). */
    public List<Product> findLowStock() {
        return productDAO.findLowStock();
    }

    /**
     * Mengubah daftar produk menjadi {@link ProductCollection} agar dapat
     * ditelusuri dengan iterator (Iterator Pattern).
     */
    public ProductCollection toCollection(List<Product> products) {
        return new ProductCollection(products);
    }

    public void saveNew(Product product) {
        validate(product);
        productDAO.insert(product);
    }

    public void update(Product product) {
        if (product.getIdProduk() <= 0) {
            throw new IllegalArgumentException("Pilih produk yang akan diubah terlebih dahulu.");
        }
        validate(product);
        productDAO.update(product);
    }

    public void delete(int idProduk) {
        productDAO.delete(idProduk);
    }

    /** Menambah stok produk (restock) dengan validasi jumlah. */
    public void restock(int idProduk, int tambahanStok) {
        if (tambahanStok <= 0) {
            throw new IllegalArgumentException("Jumlah restock harus lebih dari 0.");
        }
        productDAO.restock(idProduk, tambahanStok);
    }

    /** Menentukan ulang batas stok minimum produk. */
    public void updateStokMinimum(int idProduk, int stokMinimum) {
        if (stokMinimum < 0) {
            throw new IllegalArgumentException("Stok minimum tidak boleh negatif.");
        }
        productDAO.updateStokMinimum(idProduk, stokMinimum);
    }

    public int countAll() {
        return productDAO.countAll();
    }

    public int countLowStock() {
        return productDAO.countLowStock();
    }

    /** Kalimat peringatan stok minimum untuk dashboard admin. */
    public String getLowStockWarning() {
        int jumlah = countLowStock();
        if (jumlah == 0) {
            return "Semua produk memiliki stok di atas batas minimum.";
        }
        return "Peringatan: " + jumlah + " produk memiliki stok <= stok minimum dan perlu restock.";
    }

    /** Validasi data produk sebelum disimpan ke database. */
    public void validate(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Data produk belum diisi.");
        }
        if (product.getNamaProduk() == null || product.getNamaProduk().trim().isEmpty()) {
            throw new IllegalArgumentException("Nama produk wajib diisi.");
        }
        if (!ProductFactory.isValidCategory(product.getCategory())) {
            throw new IllegalArgumentException("Kategori produk tidak valid.");
        }
        if (product.getHargaBeli() < 0 || product.getHargaJual() < 0) {
            throw new IllegalArgumentException("Harga beli dan harga jual tidak boleh negatif.");
        }
        if (product.getHargaJual() < product.getHargaBeli()) {
            throw new IllegalArgumentException("Harga jual tidak boleh lebih rendah dari harga beli.");
        }
        if (product.getJumlahStok() < 0) {
            throw new IllegalArgumentException("Jumlah stok tidak boleh negatif.");
        }
        if (product.getStokMinimum() < 0) {
            throw new IllegalArgumentException("Stok minimum tidak boleh negatif.");
        }
    }
}