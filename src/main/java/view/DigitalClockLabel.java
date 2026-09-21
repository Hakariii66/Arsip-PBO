package view;

import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Jam digital realtime (fitur tambahan PRD bagian 7).
 *
 * Diperbarui setiap 1 detik memakai {@link javax.swing.Timer} dan memakai
 * {@code LocalDateTime.now()} sebagai sumber waktu - waktu yang sama juga
 * dipakai sebagai sumber tanggal &amp; waktu transaksi.
 */
public class DigitalClockLabel extends JLabel {

    private static final long serialVersionUID = 1L;

    /** Format tampilan jam sesuai PRD: HH:mm:ss. */
    public static final DateTimeFormatter FORMAT_JAM = DateTimeFormatter.ofPattern("HH:mm:ss");

    private static final DateTimeFormatter FORMAT_LENGKAP = DateTimeFormatter.ofPattern(
            "EEEE, dd MMMM yyyy - HH:mm:ss", Locale.forLanguageTag("id-ID"));

    private final Timer timer;
    private final boolean tampilkanTanggal;

    public DigitalClockLabel() {
        this(false);
    }

    public DigitalClockLabel(boolean tampilkanTanggal) {
        this.tampilkanTanggal = tampilkanTanggal;
        setFont(Theme.FONT_CLOCK);
        setForeground(Theme.LIGHT_BLUE);
        setHorizontalAlignment(SwingConstants.RIGHT);
        perbarui();
        this.timer = new Timer(1000, e -> perbarui());
        this.timer.setInitialDelay(0);
        this.timer.start();
    }

    /** Memperbarui teks jam dari waktu sistem saat ini. */
    public final void perbarui() {
        LocalDateTime sekarang = LocalDateTime.now();
        setText(tampilkanTanggal ? sekarang.format(FORMAT_LENGKAP) : sekarang.format(FORMAT_JAM));
    }

    /** Menghentikan timer (dipanggil saat frame ditutup agar tidak berjalan terus). */
    public void stopClock() {
        if (timer != null) {
            timer.stop();
        }
    }

    /** Waktu sistem dalam format HH:mm:ss. */
    public static String waktuSekarang() {
        return LocalDateTime.now().format(FORMAT_JAM);
    }
}