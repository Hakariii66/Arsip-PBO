package database;

/**
 * Runtime exception untuk kegagalan akses database.
 *
 * Dipakai DAO/service agar GUI tidak perlu menangani {@code SQLException}
 * secara langsung: pesan sudah disusun ramah pengguna dan penyebab aslinya
 * tetap tersimpan pada {@code cause}.
 */
public class DatabaseException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}