package service;

import database.ConfigLoader;
import model.Transaction;
import model.TransactionDetail;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;

/**
 * Service pembuatan nota pembelian dalam format {@code .txt}.
 *
 * Format nama file (PRD bagian 6):
 * {@code nota_(nama pembeli)_(ddMMyyyy)_(HH-mm).txt}
 * Contoh: {@code nota_Budi_20092026_14-35.txt}
 *
 * Karakter ":" tidak dipakai pada nama file agar kompatibel dengan Windows.
 * Seluruh nota disimpan pada folder {@code nota/}.
 */
public class ReceiptService {

    private static final DateTimeFormatter TANGGAL_FILE = DateTimeFormatter.ofPattern("ddMMyyyy");
    private static final DateTimeFormatter WAKTU_FILE = DateTimeFormatter.ofPattern("HH-mm");
    private static final DateTimeFormatter TANGGAL_TAMPIL = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter WAKTU_TAMPIL = DateTimeFormatter.ofPattern("HH:mm:ss");

    private static final String FOLDER_NOTA = "nota";
    private static final String NL = System.lineSeparator();
    private static final int LEBAR = 74;

    public String getStoreName() {
        return ConfigLoader.get("toko.nama", "SITAG - Toko Aksesoris Gadget");
    }

    public String getStoreAddress() {
        return ConfigLoader.get("toko.alamat", "Jl. Merdeka No. 10, Bandung")
                + " | " + ConfigLoader.get("toko.telepon", "0812-3456-7890");
    }

    /** Nama file nota yang seharusnya dimiliki sebuah transaksi. */
    public String expectedFileName(Transaction transaction) {
        return "nota_" + safeName(transaction.getNamaPembeli()) + "_"
                + transaction.getTanggal().format(TANGGAL_FILE) + "_"
                + transaction.getWaktu().format(WAKTU_FILE) + ".txt";
    }

    /**
     * Menulis nota transaksi ke folder {@code nota/}.
     *
     * @return path absolut file nota yang dibuat
     */
    public Path generateReceipt(Transaction transaction, String pricingName, String paymentNote) {
        try {
            Path folder = Paths.get(FOLDER_NOTA);
            Files.createDirectories(folder);
            Path file = folder.resolve(buildUniqueFileName(transaction));
            Files.writeString(file, buildReceiptContent(transaction, pricingName, paymentNote),
                    StandardCharsets.UTF_8);
            return file.toAbsolutePath();
        } catch (IOException e) {
            throw new IllegalStateException("Gagal membuat file nota .txt: " + e.getMessage(), e);
        }
    }

    /** Membaca kembali isi nota (untuk tombol "Lihat Nota" pada histori admin). */
    public String readReceipt(Transaction transaction) {
        Path file = Paths.get(FOLDER_NOTA).resolve(expectedFileName(transaction));
        if (!Files.exists(file)) {
            return null;
        }
        try {
            return Files.readString(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Gagal membaca file nota: " + e.getMessage(), e);
        }
    }

    /**
     * Bila customer yang sama membeli lagi pada menit yang sama, nama file
     * diberi akhiran angka agar nota sebelumnya tidak tertimpa.
     */
    private String buildUniqueFileName(Transaction transaction) {
        Path folder = Paths.get(FOLDER_NOTA);
        String baseName = expectedFileName(transaction);
        if (baseName.endsWith(".txt")) {
            baseName = baseName.substring(0, baseName.length() - 4);
        }
        String candidate = baseName + ".txt";
        int urutan = 2;
        while (Files.exists(folder.resolve(candidate))) {
            candidate = baseName + "_" + urutan + ".txt";
            urutan++;
        }
        return candidate;
    }

    /** Membersihkan nama pembeli agar aman dipakai sebagai nama file Windows. */
    private String safeName(String namaPembeli) {
        String bersih = namaPembeli == null ? "" : namaPembeli.trim().replaceAll("[^A-Za-z0-9]+", "_");
        bersih = bersih.replaceAll("^_+", "").replaceAll("_+$", "");
        return bersih.isEmpty() ? "Pembeli" : bersih;
    }

    /** Menyusun isi teks nota lengkap. */
    public String buildReceiptContent(Transaction transaction, String pricingName, String paymentNote) {
        StringBuilder sb = new StringBuilder();
        sb.append(line('=')).append(NL);
        sb.append(center(getStoreName())).append(NL);
        sb.append(center(getStoreAddress())).append(NL);
        sb.append(line('=')).append(NL);
        sb.append(String.format("ID Transaksi   : %d%n", transaction.getIdTransaksi()));
        sb.append(String.format("Nama Pembeli   : %s%n", transaction.getNamaPembeli()));
        sb.append(String.format("Tanggal        : %s%n", transaction.getTanggal().format(TANGGAL_TAMPIL)));
        sb.append(String.format("Waktu          : %s%n", transaction.getWaktu().format(WAKTU_TAMPIL)));
        sb.append(String.format("Metode Bayar   : %s%n", transaction.getMetodePembayaran()));
        sb.append(line('-')).append(NL);
        sb.append(String.format("%-3s %-30.30s %4s %13s %13s%n", "No", "Produk", "Qty", "Harga", "Subtotal"));
        sb.append(line('-')).append(NL);

        int nomor = 1;
        for (TransactionDetail detail : transaction.getDetails()) {
            sb.append(String.format("%-3d %-30.30s %4d %13s %13s%n",
                    nomor++,
                    detail.getNamaProduk(),
                    detail.getJumlah(),
                    CurrencyUtil.format(detail.getHarga()),
                    CurrencyUtil.format(detail.getSubtotal())));
            if (detail.getKeterangan() != null && !"-".equals(detail.getKeterangan())) {
                sb.append(String.format("      + %s%n", detail.getKeterangan()));
            }
        }

        sb.append(line('-')).append(NL);
        sb.append(String.format("Subtotal Item  : %s%n", CurrencyUtil.format(transaction.getTotal())));
        sb.append(String.format("Promo          : %s%n", pricingName));
        sb.append(String.format("TOTAL BAYAR    : %s%n", CurrencyUtil.format(transaction.getTotal())));
        sb.append(String.format("Pembayaran     : %s%n", paymentNote));
        sb.append(line('=')).append(NL);
        sb.append(center("Terima kasih telah berbelanja di SITAG!")).append(NL);
        sb.append(center("Barang yang sudah dibeli tidak dapat ditukar.")).append(NL);
        sb.append(line('=')).append(NL);
        return sb.toString();
    }

    private String line(char karakter) {
        return String.valueOf(karakter).repeat(LEBAR);
    }

    private String center(String text) {
        String isi = text == null ? "" : text;
        if (isi.length() >= LEBAR) {
            return isi;
        }
        int kiri = (LEBAR - isi.length()) / 2;
        return " ".repeat(kiri) + isi;
    }
}