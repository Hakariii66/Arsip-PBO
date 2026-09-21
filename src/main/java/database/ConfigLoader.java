package database;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 * Pembaca file {@code config.properties} (host, port, nama database, user,
 * password, dan data toko untuk nota).
 *
 * File dibaca sekali lalu di-cache; pencarian dilakukan dari classpath dahulu,
 * kemudian dari working directory project (default saat dijalankan di
 * IntelliJ IDEA adalah root project).
 */
public final class ConfigLoader {

    private static final String CONFIG_FILE = "config.properties";

    private static Properties cache;

    private ConfigLoader() {
        // Kelas utilitas tidak untuk di-instansiasi.
    }

    /** Mengembalikan seluruh isi konfigurasi (di-cache setelah pembacaan pertama). */
    public static synchronized Properties load() {
        if (cache == null) {
            cache = read();
        }
        return cache;
    }

    /** Nilai konfigurasi sebagai teks; {@code defaultValue} dipakai bila belum diisi. */
    public static String get(String key, String defaultValue) {
        String value = load().getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        return value.trim();
    }

    /** Nilai konfigurasi sebagai boolean. */
    public static boolean getBoolean(String key, boolean defaultValue) {
        String value = get(key, String.valueOf(defaultValue));
        return Boolean.parseBoolean(value);
    }

    private static Properties read() {
        Properties props = new Properties();
        // 1) Coba dari classpath (berguna bila config ikut dicopy ke folder output).
        try (InputStream in = ConfigLoader.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (in != null) {
                props.load(in);
                return props;
            }
        } catch (Exception ignored) {
            // lanjut ke pembacaan file
        }
        // 2) Coba dari working directory project.
        try (InputStream in = new FileInputStream(CONFIG_FILE)) {
            props.load(in);
        } catch (Exception ignored) {
            // Tidak ada file config: aplikasi memakai nilai default.
        }
        return props;
    }
}