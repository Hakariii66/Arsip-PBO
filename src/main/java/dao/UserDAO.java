package dao;

import database.DatabaseConnection;
import database.DatabaseException;
import model.Admin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * DAO untuk tabel {@code user} (akun admin).
 *
 * Customer tidak disimpan pada tabel ini karena customer dapat berbelanja
 * tanpa akun sesuai PRD.
 */
public class UserDAO {

    private Connection conn() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    private Admin mapRow(ResultSet rs) throws SQLException {
        return new Admin(rs.getInt("id_user"), rs.getString("username"), rs.getString("password"));
    }

    /** Mencari akun admin berdasarkan username. */
    public Admin findByUsername(String username) {
        String sql = "SELECT id_user, username, password FROM user WHERE username = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Gagal membaca data user. " + e.getMessage(), e);
        }
    }

    /**
     * Validasi kredensial login admin.
     *
     * @return object {@link Admin} bila username &amp; password cocok, atau null bila tidak.
     */
    public Admin findByCredentials(String username, String password) {
        String sql = "SELECT id_user, username, password FROM user WHERE username = ? AND password = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Gagal memvalidasi login admin. " + e.getMessage(), e);
        }
    }

    public int countAll() {
        try (PreparedStatement ps = conn().prepareStatement("SELECT COUNT(*) FROM user");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new DatabaseException("Gagal menghitung data user. " + e.getMessage(), e);
        }
    }
}