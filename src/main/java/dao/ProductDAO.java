package dao;

import database.DatabaseConnection;
import database.DatabaseException;
import model.Product;
import model.ProductFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO (Data Access Object) untuk tabel {@code produk}.
 *
 * Semua akses database melewati Singleton
 * {@code DatabaseConnection.getInstance().getConnection()}, dan hasil query
 * dipetakan ke subclass produk yang sesuai melalui {@link ProductFactory}
 * (mendukung polymorphism: Product -> Cable/Charger/Case/Headset).
 */
public class ProductDAO {

    private static final String BASE_SELECT =
            "SELECT id_produk, nama_produk, kategori, harga_beli, harga_jual, jumlah_stok, stok_minimum FROM produk ";

    private Connection conn() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    private Product mapRow(ResultSet rs) throws SQLException {
        Product product = ProductFactory.create(rs.getString("kategori"));
        product.setIdProduk(rs.getInt("id_produk"));
        product.setNamaProduk(rs.getString("nama_produk"));
        product.setHargaBeli(rs.getDouble("harga_beli"));
        product.setHargaJual(rs.getDouble("harga_jual"));
        product.setJumlahStok(rs.getInt("jumlah_stok"));
        product.setStokMinimum(rs.getInt("stok_minimum"));
        return product;
    }

    private List<Product> query(String sql, Object... params) {
        List<Product> list = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Gagal membaca data produk dari database. " + e.getMessage(), e);
        }
        return list;
    }

    /** Seluruh produk, diurutkan berdasarkan id. */
    public List<Product> findAll() {
        return query(BASE_SELECT + "ORDER BY id_produk ASC");
    }

    public Product findById(int idProduk) {
        List<Product> list = query(BASE_SELECT + "WHERE id_produk = ?", idProduk);
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * Pencarian produk berdasarkan nama/kategori.
     *
     * @param keyword  kata kunci nama produk (boleh kosong)
     * @param kategori filter kategori (null / "Semua" berarti seluruh kategori)
     */
    public List<Product> search(String keyword, String kategori) {
        boolean pakaiKeyword = keyword != null && !keyword.trim().isEmpty();
        boolean pakaiKategori = kategori != null && !kategori.trim().isEmpty()
                && !"Semua".equalsIgnoreCase(kategori.trim());
        if (!pakaiKeyword && !pakaiKategori) {
            return findAll();
        }
        StringBuilder sql = new StringBuilder(BASE_SELECT);
        List<Object> params = new ArrayList<>();
        sql.append("WHERE ");
        if (pakaiKeyword) {
            sql.append("(nama_produk LIKE ? OR kategori LIKE ?) ");
            String like = "%" + keyword.trim() + "%";
            params.add(like);
            params.add(like);
        }
        if (pakaiKategori) {
            if (pakaiKeyword) {
                sql.append("AND ");
            }
            sql.append("kategori = ? ");
            params.add(kategori.trim());
        }
        sql.append("ORDER BY id_produk ASC");
        return query(sql.toString(), params.toArray());
    }

    /** Produk dengan stok menipis (jumlah_stok <= stok_minimum). */
    public List<Product> findLowStock() {
        return query(BASE_SELECT + "WHERE jumlah_stok <= stok_minimum ORDER BY jumlah_stok ASC");
    }

    public int insert(Product product) {
        String sql = "INSERT INTO produk (nama_produk, kategori, harga_beli, harga_jual, jumlah_stok, stok_minimum)"
                + " VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, product.getNamaProduk());
            ps.setString(2, product.getCategory());
            ps.setDouble(3, product.getHargaBeli());
            ps.setDouble(4, product.getHargaJual());
            ps.setInt(5, product.getJumlahStok());
            ps.setInt(6, product.getStokMinimum());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    product.setIdProduk(id);
                    return id;
                }
            }
            return 0;
        } catch (SQLException e) {
            throw new DatabaseException("Gagal menyimpan produk baru ke database. " + e.getMessage(), e);
        }
    }

    public void update(Product product) {
        String sql = "UPDATE produk SET nama_produk = ?, kategori = ?, harga_beli = ?, harga_jual = ?,"
                + " jumlah_stok = ?, stok_minimum = ? WHERE id_produk = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, product.getNamaProduk());
            ps.setString(2, product.getCategory());
            ps.setDouble(3, product.getHargaBeli());
            ps.setDouble(4, product.getHargaJual());
            ps.setInt(5, product.getJumlahStok());
            ps.setInt(6, product.getStokMinimum());
            ps.setInt(7, product.getIdProduk());
            if (ps.executeUpdate() == 0) {
                throw new DatabaseException("Produk tidak ditemukan, data gagal diperbarui.");
            }
        } catch (SQLException e) {
            throw new DatabaseException("Gagal memperbarui produk. " + e.getMessage(), e);
        }
    }

    public void delete(int idProduk) {
        try (PreparedStatement ps = conn().prepareStatement("DELETE FROM produk WHERE id_produk = ?")) {
            ps.setInt(1, idProduk);
            if (ps.executeUpdate() == 0) {
                throw new DatabaseException("Produk tidak ditemukan, data gagal dihapus.");
            }
        } catch (SQLException e) {
            // Kode 1451 = pelanggaran foreign key (produk sudah dipakai pada detail transaksi)
            if (e.getErrorCode() == 1451) {
                throw new DatabaseException("Produk tidak dapat dihapus karena sudah dipakai pada"
                        + " transaksi pembelian (histori).", e);
            }
            throw new DatabaseException("Gagal menghapus produk. " + e.getMessage(), e);
        }
    }

    /** Menambah stok produk (restock). */
    public void restock(int idProduk, int tambahanStok) {
        String sql = "UPDATE produk SET jumlah_stok = jumlah_stok + ? WHERE id_produk = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, tambahanStok);
            ps.setInt(2, idProduk);
            if (ps.executeUpdate() == 0) {
                throw new DatabaseException("Produk tidak ditemukan, restock gagal.");
            }
        } catch (SQLException e) {
            throw new DatabaseException("Gagal menambah stok produk. " + e.getMessage(), e);
        }
    }

    /** Mengubah batas stok minimum produk. */
    public void updateStokMinimum(int idProduk, int stokMinimum) {
        String sql = "UPDATE produk SET stok_minimum = ? WHERE id_produk = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, stokMinimum);
            ps.setInt(2, idProduk);
            if (ps.executeUpdate() == 0) {
                throw new DatabaseException("Produk tidak ditemukan, stok minimum gagal diubah.");
            }
        } catch (SQLException e) {
            throw new DatabaseException("Gagal mengubah stok minimum. " + e.getMessage(), e);
        }
    }

    public int countAll() {
        return count("SELECT COUNT(*) FROM produk");
    }

    /** Jumlah produk dengan stok menipis (untuk dashboard admin). */
    public int countLowStock() {
        return count("SELECT COUNT(*) FROM produk WHERE jumlah_stok <= stok_minimum");
    }

    private int count(String sql) {
        try (Statement st = conn().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new DatabaseException("Gagal menghitung data produk. " + e.getMessage(), e);
        }
    }
}