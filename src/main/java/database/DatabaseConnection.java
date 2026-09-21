package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * PATTERN: SINGLETON.
 *
 * Menyediakan SATU instance pengelola koneksi database untuk seluruh aplikasi
 * (semua DAO memakai {@code DatabaseConnection.getInstance().getConnection()}),
 * sehingga koneksi tidak dibuat berulang-ulang.
 *
 * - Constructor private agar tidak dapat di-instansiasi dari luar.
 * - Instance diambil via {@code getInstance()} yang thread-safe
 *   (double-checked locking + volatile).
 * - Konfigurasi dibaca dari {@code config.properties} melalui {@link ConfigLoader}.
 * - Bila {@code db.auto.init=true}, database + tabel + data awal dibuat otomatis
 *   sehingga project dapat langsung dijalankan tanpa import manual.
 */
public final class DatabaseConnection {

    private static volatile DatabaseConnection instance;

    // ---------- DDL tabel, disamakan dengan file database.sql ----------

    private static final String TABLE_USER = "CREATE TABLE IF NOT EXISTS user ("
            + " id_user INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,"
            + " username VARCHAR(50) NOT NULL UNIQUE,"
            + " password VARCHAR(255) NOT NULL,"
            + " role ENUM('ADMIN') NOT NULL DEFAULT 'ADMIN'"
            + ") ENGINE=InnoDB";

    private static final String TABLE_PRODUK = "CREATE TABLE IF NOT EXISTS produk ("
            + " id_produk INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,"
            + " nama_produk VARCHAR(150) NOT NULL,"
            + " kategori VARCHAR(50) NOT NULL,"
            + " harga_beli DECIMAL(15,2) NOT NULL,"
            + " harga_jual DECIMAL(15,2) NOT NULL,"
            + " jumlah_stok INT UNSIGNED NOT NULL DEFAULT 0,"
            + " stok_minimum INT UNSIGNED NOT NULL DEFAULT 0,"
            + " CONSTRAINT chk_produk_harga_beli CHECK (harga_beli >= 0),"
            + " CONSTRAINT chk_produk_harga_jual CHECK (harga_jual >= 0)"
            + ") ENGINE=InnoDB";

    private static final String TABLE_TRANSAKSI = "CREATE TABLE IF NOT EXISTS transaksi ("
            + " id_transaksi BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,"
            + " nama_pembeli VARCHAR(150) NOT NULL,"
            + " tanggal DATE NOT NULL,"
            + " waktu TIME NOT NULL,"
            + " total DECIMAL(15,2) NOT NULL,"
            + " metode_pembayaran ENUM('CASH', 'TRANSFER') NOT NULL,"
            + " CONSTRAINT chk_transaksi_total CHECK (total >= 0),"
            + " INDEX idx_transaksi_tanggal (tanggal),"
            + " INDEX idx_transaksi_nama_pembeli (nama_pembeli)"
            + ") ENGINE=InnoDB";

    private static final String TABLE_DETAIL_TRANSAKSI = "CREATE TABLE IF NOT EXISTS detail_transaksi ("
            + " id_detail BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,"
            + " id_transaksi BIGINT UNSIGNED NOT NULL,"
            + " id_produk INT UNSIGNED NOT NULL,"
            + " jumlah INT UNSIGNED NOT NULL,"
            + " harga DECIMAL(15,2) NOT NULL,"
            + " subtotal DECIMAL(15,2) NOT NULL,"
            + " CONSTRAINT chk_detail_jumlah CHECK (jumlah > 0),"
            + " CONSTRAINT chk_detail_harga CHECK (harga >= 0),"
            + " CONSTRAINT chk_detail_subtotal CHECK (subtotal >= 0),"
            + " CONSTRAINT fk_detail_transaksi FOREIGN KEY (id_transaksi)"
            + "   REFERENCES transaksi (id_transaksi) ON DELETE CASCADE ON UPDATE CASCADE,"
            + " CONSTRAINT fk_detail_produk FOREIGN KEY (id_produk)"
            + "   REFERENCES produk (id_produk) ON DELETE RESTRICT ON UPDATE CASCADE,"
            + " INDEX idx_detail_transaksi (id_transaksi),"
            + " INDEX idx_detail_produk (id_produk)"
            + ") ENGINE=InnoDB";

    private static final String[] TABLE_DDL = {
        TABLE_USER, TABLE_PRODUK, TABLE_TRANSAKSI, TABLE_DETAIL_TRANSAKSI
    };
    private final String host;
    private final String port;
    private final String dbName;
    private final String user;
    private final String password;
    private final boolean autoInit;

    private Connection connection;

    private DatabaseConnection() {
        Properties config = ConfigLoader.load();
        this.host = value(config, "db.host", "localhost");
        this.port = value(config, "db.port", "3306");
        this.dbName = value(config, "db.name", "inventaris_toko_aksesoris_gadget");
        this.user = value(config, "db.user", "root");
        this.password = config.getProperty("db.password", "");
        this.autoInit = ConfigLoader.getBoolean("db.auto.init", true);
    }

    /** Satu-satunya pintu untuk mendapatkan instance pengelola koneksi. */
    public static DatabaseConnection getInstance() {
        if (instance == null) {
            synchronized (DatabaseConnection.class) {
                if (instance == null) {
                    instance = new DatabaseConnection();
                }
            }
        }
        return instance;
    }

    private static String value(Properties config, String key, String defaultValue) {
        String result = config.getProperty(key);
        return (result == null || result.trim().isEmpty()) ? defaultValue : result.trim();
    }

    private String buildJdbcUrl(String database) {
        String url = "jdbc:mysql://" + host + ":" + port;
        if (database != null && !database.isEmpty()) {
            url += "/" + database;
        }
        return url + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Jakarta"
                + "&characterEncoding=UTF-8&useUnicode=true";
    }

    /**
     * Mengembalikan koneksi aktif; koneksi dibuka ulang bila belum ada/tertutup.
     *
     * @throws SQLException bila MySQL tidak dapat dihubungi
     */
    public synchronized Connection getConnection() throws SQLException {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(buildJdbcUrl(dbName), user, password);
            }
            return connection;
        } catch (SQLException e) {
            throw new SQLException("Gagal terhubung ke MySQL pada " + getJdbcUrl()
                    + " (user=" + user + "). Pastikan server MySQL/phpMyAdmin sudah berjalan dan "
                    + "konfigurasi config.properties sudah benar. Detail: " + e.getMessage(), e);
        }
    }
    /**
     * Menyiapkan database &amp; tabel, lalu mengisi data awal bila masih kosong.
     * Dipanggil sekali saat aplikasi dijalankan.
     */
    public synchronized void initialize() throws SQLException {
        if (autoInit) {
            createDatabaseIfMissing();
            Connection c = getConnection();
            createTables(c);
            seedDefaultData(c);
        } else {
            verifyTables(getConnection());
        }
    }

    private void createDatabaseIfMissing() throws SQLException {
        try (Connection server = DriverManager.getConnection(buildJdbcUrl(null), user, password);
             Statement st = server.createStatement()) {
            st.executeUpdate("CREATE DATABASE IF NOT EXISTS `" + dbName + "`"
                    + " CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
        } catch (SQLException e) {
            throw new SQLException("Gagal menyiapkan database `" + dbName + "` pada " + host + ":" + port
                    + ". Pastikan server MySQL berjalan dan user '" + user + "' memiliki hak membuat database,"
                    + " atau import database.sql melalui phpMyAdmin. Detail: " + e.getMessage(), e);
        }
    }

    private void createTables(Connection c) throws SQLException {
        for (String ddl : TABLE_DDL) {
            try (Statement st = c.createStatement()) {
                st.executeUpdate(ddl);
            }
        }
    }

    private void seedDefaultData(Connection c) throws SQLException {
        if (countRows(c, "SELECT COUNT(*) FROM user") == 0) {
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO user (username, password, role) VALUES (?, ?, 'ADMIN')")) {
                ps.setString(1, "admin");
                ps.setString(2, "admin123");
                ps.executeUpdate();
            }
        }
        if (countRows(c, "SELECT COUNT(*) FROM produk") == 0) {
            String sql = "INSERT INTO produk (nama_produk, kategori, harga_beli, harga_jual, jumlah_stok, stok_minimum)"
                    + " VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                insertProduk(ps, "Kabel Data Type-C", "Cable", 25000, 40000, 30, 5);
                insertProduk(ps, "Charger Fast Charging 20W", "Charger", 85000, 120000, 20, 5);
                insertProduk(ps, "Case Silicone Universal", "Case", 30000, 55000, 25, 5);
                insertProduk(ps, "Headset Bluetooth", "Headset", 120000, 175000, 15, 3);
            }
        }
    }

    private void insertProduk(PreparedStatement ps, String nama, String kategori,
                              double hargaBeli, double hargaJual, int stok, int stokMinimum) throws SQLException {
        ps.setString(1, nama);
        ps.setString(2, kategori);
        ps.setDouble(3, hargaBeli);
        ps.setDouble(4, hargaJual);
        ps.setInt(5, stok);
        ps.setInt(6, stokMinimum);
        ps.executeUpdate();
    }

    private int countRows(Connection c, String sql) throws SQLException {
        try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    /** Memastikan tabel wajib sudah ada ketika {@code db.auto.init=false}. */
    private void verifyTables(Connection c) throws SQLException {
        String[] tables = { "user", "produk", "transaksi", "detail_transaksi" };
        for (String table : tables) {
            try (Statement st = c.createStatement()) {
                st.executeQuery("SELECT 1 FROM `" + table + "` LIMIT 1").close();
            } catch (SQLException e) {
                throw new SQLException("Tabel `" + table + "` belum tersedia di database `" + dbName
                        + "`. Import file database.sql melalui phpMyAdmin atau set"
                        + " db.auto.init=true pada config.properties.", e);
            }
        }
    }

    /** Menutup koneksi (dipanggil saat aplikasi keluar). */
    public synchronized void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ignored) {
                // koneksi memang akan dilepas, kegagalan menutup tidak perlu menghentikan aplikasi
            } finally {
                connection = null;
            }
        }
    }

    /** True bila koneksi sedang terbuka. */
    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    public String getHost() {
        return host;
    }

    public String getPort() {
        return port;
    }

    public String getDbName() {
        return dbName;
    }

    public String getUser() {
        return user;
    }

    public boolean isAutoInit() {
        return autoInit;
    }

    public String getJdbcUrl() {
        return buildJdbcUrl(dbName);
    }

    /** Keterangan singkat koneksi untuk status bar GUI. */
    public String getDatabaseInfo() {
        return "MySQL " + host + ":" + port + " / " + dbName + " (user: " + user + ")";
    }
}