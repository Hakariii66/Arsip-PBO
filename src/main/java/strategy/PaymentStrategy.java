package strategy;

/**
 * Strategy Pattern: perilaku pembayaran.
 *
 * Checkout hanya berinteraksi dengan interface ini, sehingga metode pembayaran
 * (Cash / Transfer) dapat diganti saat runtime tanpa mengubah alur checkout.
 */
public interface PaymentStrategy {

    /** Nama metode pembayaran sesuai kolom {@code transaksi.metode_pembayaran}. */
    String getMethodName();

    /** Nama metode untuk ditampilkan pada GUI. */
    String getDisplayName();

    /** True jika GUI perlu meminta nominal uang yang diterima (khusus tunai). */
    boolean isCashInputRequired();

    /**
     * Memproses pembayaran.
     *
     * @param total      total yang harus dibayar
     * @param amountPaid nominal yang diberikan/ditransfer pembeli
     * @throws IllegalArgumentException jika pembayaran tidak valid (mis. uang kurang)
     */
    void processPayment(double total, double amountPaid);

    /** Keterangan pembayaran untuk ditampilkan di nota &amp; konfirmasi. */
    String getPaymentNote(double total, double amountPaid);
}