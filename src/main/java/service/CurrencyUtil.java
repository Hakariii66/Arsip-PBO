package service;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Utilitas format &amp; parsing nilai rupiah.
 *
 * Dipakai oleh GUI (view), strategy pembayaran, dan nota agar format uang
 * konsisten di seluruh aplikasi: {@code Rp 120.000}.
 */
public final class CurrencyUtil {

    private static final DecimalFormat FORMAT = new DecimalFormat(
            "#,##0", DecimalFormatSymbols.getInstance(Locale.forLanguageTag("id-ID")));

    private CurrencyUtil() {
        // Kelas utilitas tidak untuk di-instansiasi.
    }

    /** Format angka menjadi teks rupiah, contoh: 120000 -> "Rp 120.000". */
    public static String format(double amount) {
        return "Rp " + FORMAT.format(Math.round(amount));
    }

    /**
     * Mengubah teks input pengguna menjadi angka.
     * Menerima "40000", "40.000", "Rp 40.000", maupun "40.000,50".
     *
     * @throws IllegalArgumentException jika teks kosong atau bukan angka valid.
     */
    public static double parse(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Nilai rupiah belum diisi.");
        }
        String cleaned = text.trim()
                .replace("Rp", "")
                .replace("rp", "")
                .replace(" ", "")
                .replace(".", "")
                .replace(",", ".");
        try {
            return Double.parseDouble(cleaned);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Format angka tidak valid: " + text);
        }
    }
}