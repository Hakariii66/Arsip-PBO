package dao;

import database.DatabaseConnection;
import database.DatabaseException;
import model.Transaction;
import model.TransactionDetail;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO untuk tabel {@code transaksi} dan {@code detail_transaksi}.
 *
 * Penyimpanan transaksi dilakukan dalam SATU database transaction
 * (auto-commit dimatikan): header transaksi, seluruh detail, dan pengurangan
 * stok produk harus berhasil bersama-sama; bila ada satu yang gagal maka
 * semuanya di-rollback (non-functional requirement: Reliability).
 */
public class TransactionDAO {

    private Connection conn() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    /**
     * Menyimpan transaksi lengkap beserta detailnya dan mengurangi stok produk.
     *
     * @throws DatabaseException bila penyimpanan gagal (seluruh perubahan dibatalkan)
     */
    public void save(Transaction transaction) {
        if (transaction.getDetails().isEmpty()) {
            throw new IllegalArgumentException("Transaksi tanpa detail produk tidak dapat disimpan.");
        }
        Connection c;
        try {
            c = conn();
        } catch (SQLException e) {
            throw new DatabaseException(e.getMessage(), e);
        }
        try {
            c.setAutoCommit(false);
            long idTransaksi = insertHeader(c, transaction);
            transaction.setIdTransaksi(idTransaksi);
            for (TransactionDetail detail : transaction.getDetails()) {
                lockDanValidasiStok(c, detail);
                insertDetail(c, idTransaksi, detail);
                kurangiStok(c, detail);
            }
            c.commit();
        } catch (SQLException e) {
            rollback(c);
            throw new DatabaseException("Transaksi gagal disimpan sehingga seluruh perubahan dibatalkan"
                    + " (rollback). " + e.getMessage(), e);
        } catch (RuntimeException e) {
            rollback(c);
            throw e;
        } finally {
            try {
                c.setAutoCommit(true);
            } catch (SQLException ignored) {
                // koneksi tetap dapat dipakai pada pemanggilan berikutnya
            }
        }
    }

    private long insertHeader(Connection c, Transaction transaction) throws SQLException {
        String sql = "INSERT INTO transaksi (nama_pembeli, tanggal, waktu, total, metode_pembayaran)"
                + " VALUES (?, ?, ?, ?, ?)";
        LocalDateTime waktu = transaction.getWaktuTransaksi();
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, transaction.getNamaPembeli());
            ps.setDate(2, Date.valueOf(waktu.toLocalDate()));
            ps.setTime(3, Time.valueOf(waktu.toLocalTime().withNano(0)));
            ps.setDouble(4, transaction.getTotal());
            ps.setString(5, transaction.getMetodePembayaran());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        }
        throw new SQLException("ID transaksi tidak dapat dibaca setelah penyimpanan.");
    }

    private void insertDetail(Connection c, long idTransaksi, TransactionDetail detail) throws SQLException {
        String sql = "INSERT INTO detail_transaksi (id_transaksi, id_produk, jumlah, harga, subtotal)"
                + " VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, idTransaksi);
            ps.setInt(2, detail.getIdProduk());
            ps.setInt(3, detail.getJumlah());
            ps.setDouble(4, detail.getHarga());
            ps.setDouble(5, detail.getSubtotal());
            ps.executeUpdate();
            detail.setIdTransaksi(idTransaksi);
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    detail.setIdDetail(keys.getLong(1));
                }
            }
        }
    }

    /** Business rule: customer hanya boleh membeli produk dengan stok mencukupi. */
    private void lockDanValidasiStok(Connection c, TransactionDetail detail) throws SQLException {
        String sql = "SELECT jumlah_stok FROM produk WHERE id_produk = ? FOR UPDATE";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, detail.getIdProduk());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Produk id " + detail.getIdProduk() + " tidak ditemukan.");
                }
                int stok = rs.getInt("jumlah_stok");
                if (stok < detail.getJumlah()) {
                    throw new SQLException("Stok " + detail.getNamaProduk() + " tersisa " + stok
                            + " pcs, tidak cukup untuk " + detail.getJumlah() + " pcs.");
                }
            }
        }
    }

    /** Mengurangi stok setelah transaksi berhasil (atomik). */
    private void kurangiStok(Connection c, TransactionDetail detail) throws SQLException {
        String sql = "UPDATE produk SET jumlah_stok = jumlah_stok - ? WHERE id_produk = ? AND jumlah_stok >= ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, detail.getJumlah());
            ps.setInt(2, detail.getIdProduk());
            ps.setInt(3, detail.getJumlah());
            if (ps.executeUpdate() != 1) {
                throw new SQLException("Pengurangan stok " + detail.getNamaProduk() + " gagal.");
            }
        }
    }

    private void rollback(Connection c) {
        try {
            c.rollback();
        } catch (SQLException ignored) {
            // rollback gagal: penyebab utama tetap dilempar ke pemanggil
        }
    }

    private Transaction mapRow(ResultSet rs) throws SQLException {
        LocalDateTime waktu = LocalDateTime.of(
                rs.getDate("tanggal").toLocalDate(),
                rs.getTime("waktu").toLocalTime());
        Transaction transaction = new Transaction(
                rs.getString("nama_pembeli"), waktu, rs.getString("metode_pembayaran"));
        transaction.setIdTransaksi(rs.getLong("id_transaksi"));
        transaction.setTotal(rs.getDouble("total"));
        return transaction;
    }

    /** Seluruh histori transaksi, terbaru di urutan pertama (hanya admin). */
    public List<Transaction> findAll() {
        String sql = "SELECT id_transaksi, nama_pembeli, tanggal, waktu, total, metode_pembayaran"
                + " FROM transaksi ORDER BY tanggal DESC, waktu DESC, id_transaksi DESC";
        List<Transaction> list = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Gagal membaca histori transaksi. " + e.getMessage(), e);
        }
        return list;
    }

    /** Detail produk pada satu transaksi (termasuk nama produk dari tabel produk). */
    public List<TransactionDetail> findDetails(long idTransaksi) {
        String sql = "SELECT d.id_detail, d.id_transaksi, d.id_produk, p.nama_produk,"
                + " d.jumlah, d.harga, d.subtotal"
                + " FROM detail_transaksi d JOIN produk p ON p.id_produk = d.id_produk"
                + " WHERE d.id_transaksi = ? ORDER BY d.id_detail ASC";
        List<TransactionDetail> list = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, idTransaksi);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TransactionDetail detail = new TransactionDetail();
                    detail.setIdDetail(rs.getLong("id_detail"));
                    detail.setIdTransaksi(rs.getLong("id_transaksi"));
                    detail.setIdProduk(rs.getInt("id_produk"));
                    detail.setNamaProduk(rs.getString("nama_produk"));
                    detail.setJumlah(rs.getInt("jumlah"));
                    detail.setHarga(rs.getDouble("harga"));
                    detail.setSubtotal(rs.getDouble("subtotal"));
                    list.add(detail);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Gagal membaca detail transaksi. " + e.getMessage(), e);
        }
        return list;
    }

    /** Jumlah transaksi hari ini (untuk kartu statistik dashboard admin). */
    public int countToday() {
        return count("SELECT COUNT(*) FROM transaksi WHERE tanggal = CURDATE()");
    }

    /** Total nilai penjualan hari ini. */
    public double sumToday() {
        String sql = "SELECT COALESCE(SUM(total), 0) FROM transaksi WHERE tanggal = CURDATE()";
        try (PreparedStatement ps = conn().prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getDouble(1) : 0;
        } catch (SQLException e) {
            throw new DatabaseException("Gagal menghitung penjualan hari ini. " + e.getMessage(), e);
        }
    }

    private int count(String sql) {
        try (PreparedStatement ps = conn().prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new DatabaseException("Gagal menghitung data transaksi. " + e.getMessage(), e);
        }
    }
}