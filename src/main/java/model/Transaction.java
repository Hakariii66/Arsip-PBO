package model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Header transaksi pembelian (tabel {@code transaksi}).
 *
 * Total transaksi adalah akumulasi subtotal seluruh detail (business rule PRD),
 * sehingga setiap penambahan detail otomatis memperbarui total.
 */
public class Transaction {

    private long idTransaksi;
    private String namaPembeli = "";
    private LocalDateTime waktuTransaksi;
    private double total;
    private String metodePembayaran = "";
    private String notaPath = "";
    private final List<TransactionDetail> details = new ArrayList<>();

    public Transaction() {
        this.waktuTransaksi = LocalDateTime.now();
    }

    public Transaction(String namaPembeli, LocalDateTime waktuTransaksi, String metodePembayaran) {
        this.namaPembeli = namaPembeli;
        this.waktuTransaksi = waktuTransaksi == null ? LocalDateTime.now() : waktuTransaksi;
        this.metodePembayaran = metodePembayaran;
    }

    /** Menambahkan detail produk dan menghitung ulang total transaksi. */
    public void addDetail(TransactionDetail detail) {
        if (detail == null) {
            throw new IllegalArgumentException("Detail transaksi tidak boleh null.");
        }
        details.add(detail);
        recalculateTotal();
    }

    /** Total = jumlah subtotal seluruh item (business rule no. 6). */
    public void recalculateTotal() {
        double sum = 0;
        for (TransactionDetail detail : details) {
            sum += detail.getSubtotal();
        }
        this.total = sum;
    }

    public List<TransactionDetail> getDetails() {
        return Collections.unmodifiableList(details);
    }

    public LocalDate getTanggal() {
        return getWaktuTransaksi().toLocalDate();
    }

    public LocalTime getWaktu() {
        return getWaktuTransaksi().toLocalTime();
    }

    public long getIdTransaksi() {
        return idTransaksi;
    }

    public void setIdTransaksi(long idTransaksi) {
        this.idTransaksi = idTransaksi;
    }

    public String getNamaPembeli() {
        return namaPembeli;
    }

    public void setNamaPembeli(String namaPembeli) {
        this.namaPembeli = namaPembeli;
    }

    public LocalDateTime getWaktuTransaksi() {
        return waktuTransaksi == null ? LocalDateTime.now() : waktuTransaksi;
    }

    public void setWaktuTransaksi(LocalDateTime waktuTransaksi) {
        this.waktuTransaksi = waktuTransaksi;
    }

    public double getTotal() {
        return total;
    }

    /** Dipakai DAO saat membaca data dari database (total dihitung ulang dari detail). */
    public void setTotal(double total) {
        this.total = total;
    }

    public String getMetodePembayaran() {
        return metodePembayaran;
    }

    public void setMetodePembayaran(String metodePembayaran) {
        this.metodePembayaran = metodePembayaran;
    }

    /** Lokasi file nota (.txt) hasil transaksi; tidak disimpan ke database. */
    public String getNotaPath() {
        return notaPath;
    }

    public void setNotaPath(String notaPath) {
        this.notaPath = notaPath;
    }

    @Override
    public String toString() {
        return "TRX-" + idTransaksi + " | " + namaPembeli + " | " + total;
    }
}