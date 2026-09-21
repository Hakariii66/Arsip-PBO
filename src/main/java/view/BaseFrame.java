package view;

import javax.swing.JFrame;
import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Frame dasar seluruh halaman SITAG.
 *
 * Menangani tiga hal yang sama pada semua halaman:
 * 1. Tema dasar (latar biru muda, layout BorderLayout).
 * 2. Navigasi antar halaman (tombol "Kembali" kembali ke frame sebelumnya).
 * 3. Penghentian timer jam digital ketika frame ditutup.
 */
public abstract class BaseFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    private final JFrame backTarget;
    /** Daftar jam digital; transient karena frame Swing tidak pernah diserialisasi. */
    private final transient List<DigitalClockLabel> clocks = new ArrayList<>();
    private boolean suppressRestore;

    protected BaseFrame(String title, JFrame backTarget) {
        super(title);
        this.backTarget = backTarget;
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Theme.LIGHT_BLUE_SOFT);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                stopClocks();
                if (!suppressRestore) {
                    restoreBackTarget();
                }
            }
        });
    }

    /** Frame yang ditampilkan kembali setelah frame ini ditutup (boleh null). */
    protected JFrame getBackTarget() {
        return backTarget;
    }

    /** Membuat jam digital sekaligus mendaftarkannya agar timer ikut berhenti saat ditutup. */
    protected DigitalClockLabel clockLabel(boolean denganTanggal) {
        DigitalClockLabel clock = new DigitalClockLabel(denganTanggal);
        clocks.add(clock);
        return clock;
    }

    /** Membuka halaman berikutnya (frame ini ditutup tanpa menampilkan kembali frame sebelumnya). */
    protected void openNext(JFrame next) {
        setVisible(false);
        suppressRestore = true;
        dispose();
        next.setVisible(true);
    }

    /** Menutup halaman ini lalu menampilkan frame tujuan tertentu (mis. logout ke menu utama). */
    protected void closeTo(JFrame target) {
        setVisible(false);
        suppressRestore = true;
        dispose();
        if (target != null && target.isDisplayable()) {
            target.setVisible(true);
            target.toFront();
        }
    }

    /** Kembali ke halaman sebelumnya. */
    protected void goBack() {
        setVisible(false);
        dispose();
    }

    protected void showError(String message) {
        Theme.error(this, message);
    }

    protected void showInfo(String message) {
        Theme.info(this, message);
    }

    /** Memusatkan frame di layar. */
    protected void center() {
        setLocationRelativeTo(null);
    }

    private void stopClocks() {
        for (DigitalClockLabel clock : clocks) {
            clock.stopClock();
        }
    }

    private void restoreBackTarget() {
        if (backTarget != null && backTarget.isDisplayable()) {
            backTarget.setVisible(true);
            backTarget.toFront();
        }
    }
}