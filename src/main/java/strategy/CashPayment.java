package strategy;

import service.CurrencyUtil;

/**
 * Strategy Pattern: pembayaran tunai (CASH).
 *
 * Ciri khas implementasi ini adalah pembeli menyerahkan uang tunai, sehingga
 * sistem harus menghitung kembalian.
 */
public class CashPayment implements PaymentStrategy {

    public static final String METHOD = "CASH";

    @Override
    public String getMethodName() {
        return METHOD;
    }

    @Override
    public String getDisplayName() {
        return "Cash (Tunai)";
    }

    @Override
    public boolean isCashInputRequired() {
        return true;
    }

    @Override
    public void processPayment(double total, double amountPaid) {
        if (total <= 0) {
            throw new IllegalArgumentException("Total pembayaran harus lebih dari 0.");
        }
        if (amountPaid <= 0) {
            throw new IllegalArgumentException("Uang tunai yang diterima belum diisi.");
        }
        if (amountPaid < total) {
            throw new IllegalArgumentException("Uang tunai " + CurrencyUtil.format(amountPaid)
                    + " kurang dari total " + CurrencyUtil.format(total) + ".");
        }
    }

    /** Kembalian yang harus diberikan ke pembeli (0 bila uang pas). */
    public double getChange(double total, double amountPaid) {
        return Math.max(0, amountPaid - total);
    }

    @Override
    public String getPaymentNote(double total, double amountPaid) {
        return "Tunai diterima " + CurrencyUtil.format(amountPaid)
                + " | Kembalian " + CurrencyUtil.format(getChange(total, amountPaid));
    }
}