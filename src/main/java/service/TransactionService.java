package service;

import dao.ProductDAO;
import dao.TransactionDAO;
import model.Cart;
import model.CartItem;
import model.Product;
import model.Transaction;
import model.TransactionDetail;
import strategy.PaymentStrategy;
import strategy.PricingStrategy;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service yang mengatur alur checkout terintegrasi (PRD bagian 15):
 *
 * Customer -> keranjang -> input nama pembeli -> pilih metode pembayaran
 * -> Strategy Pattern (pembayaran &amp; harga) -> pembayaran berhasil
 * -> LocalDateTime.now() -> kurangi stok -> simpan transaksi + detail ke database
 * -> generate nota .txt -> transaksi selesai.
 */
public class TransactionService {

    private final ProductDAO productDAO = new ProductDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final ReceiptService receiptService = new ReceiptService();

    /**
     * Memproses checkout customer.
     *
     * @param cart           keranjang belanja yang berisi item pilihan customer
     * @param namaPembeli    nama pembeli yang diinput saat checkout
     * @param paymentStrategy strategy pembayaran (CashPayment / TransferPayment)
     * @param uangDiterima   nominal uang yang diterima (untuk transfer = total)
     * @return transaksi yang sudah tersimpan dan memiliki nota
     */
    public Transaction checkout(Cart cart, String namaPembeli,
                                PaymentStrategy paymentStrategy, double uangDiterima) {
        if (cart == null || cart.isEmpty()) {
            throw new IllegalArgumentException("Keranjang masih kosong, belum ada produk yang dibeli.");
        }
        String pembeli = namaPembeli == null ? "" : namaPembeli.trim();
        if (pembeli.isEmpty()) {
            throw new IllegalArgumentException("Nama pembeli wajib diisi sebelum checkout.");
        }
        if (paymentStrategy == null) {
            throw new IllegalArgumentException("Metode pembayaran belum dipilih.");
        }

        PricingStrategy pricingStrategy = cart.getPricingStrategy();
        Transaction transaction = new Transaction(pembeli, LocalDateTime.now(), paymentStrategy.getMethodName());

        for (CartItem item : cart.getItems()) {
            detailTransaksi(transaction, item, pricingStrategy);
        }

        double total = transaction.getTotal();
        // Strategy Pattern: pembayaran + validasi nominal (tunai harus >= total)
        paymentStrategy.processPayment(total, uangDiterima);

        // Simpan transaksi + detail dan kurangi stok dalam satu database transaction
        transactionDAO.save(transaction);

        // Nota dibuat setelah transaksi berhasil disimpan
        Path nota = receiptService.generateReceipt(transaction, pricingStrategy.getName(),
                paymentStrategy.getPaymentNote(total, uangDiterima));
        transaction.setNotaPath(nota.toString());
        return transaction;
    }

    /** Menyusun satu baris detail transaksi berdasarkan item keranjang. */
    private void detailTransaksi(Transaction transaction, CartItem item, PricingStrategy pricingStrategy) {
        Product produkTerkini = productDAO.findById(item.getProduct().getIdProduk());
        if (produkTerkini == null) {
            throw new IllegalArgumentException("Produk " + item.getProduct().getNamaProduk()
                    + " sudah tidak tersedia di database.");
        }
        if (produkTerkini.getJumlahStok() < item.getQuantity()) {
            throw new IllegalArgumentException("Stok " + produkTerkini.getNamaProduk() + " tersisa "
                    + produkTerkini.getJumlahStok() + " pcs, tidak cukup untuk " + item.getQuantity() + " pcs.");
        }
        double hargaSatuan = item.getUnitPrice(pricingStrategy);
        TransactionDetail detail = new TransactionDetail(produkTerkini.getIdProduk(), item.getQuantity(), hargaSatuan);
        detail.setNamaProduk(produkTerkini.getNamaProduk());
        detail.setKeterangan(item.getAddOnLabel());
        transaction.addDetail(detail);
    }

    /** Histori pembelian (hanya admin). */
    public List<Transaction> getHistory() {
        return transactionDAO.findAll();
    }

    public List<TransactionDetail> getDetails(long idTransaksi) {
        return transactionDAO.findDetails(idTransaksi);
    }

    public int countToday() {
        return transactionDAO.countToday();
    }

    public double sumToday() {
        return transactionDAO.sumToday();
    }

    /** Isi file nota (.txt) untuk transaksi tertentu, null bila file tidak ditemukan. */
    public String readReceipt(Transaction transaction) {
        return receiptService.readReceipt(transaction);
    }

    /** Nama file nota yang seharusnya dimiliki transaksi tersebut. */
    public String getReceiptFileName(Transaction transaction) {
        return receiptService.expectedFileName(transaction);
    }
}