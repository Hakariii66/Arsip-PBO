package strategy;

import service.CurrencyUtil;

/**
 * Strategy Pattern: pembayaran transfer bank (TRANSFER).
 *
 * Pembayaran dianggap berhasil setelah pembeli mentransfer nominal penuh ke
 * rekening toko (pada project ini proses transfer disimulasikan, tanpa
 * payment gateway sesuai batasan PRD).
 */
public class TransferPayment implements PaymentStrategy {

    public static final String METHOD = "TRANSFER";

    private final String tujuanTransfer;

    public TransferPayment() {
        this("BCA 1234567890 a.n. SITAG Store");
    }

    public TransferPayment(String tujuanTransfer) {
        this.tujuanTransfer = tujuanTransfer;
    }

    public String getTujuanTransfer() {
        return tujuanTransfer;
    }

    @Override
    public String getMethodName() {
        return METHOD;
    }

    @Override
    public String getDisplayName() {
        return "Transfer Bank";
    }

    @Override
    public boolean isCashInputRequired() {
        return false;
    }

    @Override
    public void processPayment(double total, double amountPaid) {
        if (total <= 0) {
            throw new IllegalArgumentException("Total pembayaran harus lebih dari 0.");
        }
        if (amountPaid < total) {
            throw new IllegalArgumentException("Nominal transfer " + CurrencyUtil.format(amountPaid)
                    + " belum memenuhi total " + CurrencyUtil.format(total) + ".");
        }
    }

    @Override
    public String getPaymentNote(double total, double amountPaid) {
        return "Transfer " + CurrencyUtil.format(total) + " ke " + tujuanTransfer + " | Status: BERHASIL (simulasi)";
    }
}