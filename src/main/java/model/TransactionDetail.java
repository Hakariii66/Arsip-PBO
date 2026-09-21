package model;

/**
 * Detail satu baris transaksi (tabel {@code detail_transaksi}).
 *
 * Menyimpan snapshot harga satuan dan subtotal pada saat transaksi terjadi,
 * sehingga histori tidak berubah walaupun harga produk diubah admin.
 */
public class TransactionDetail {

    private long idDetail;
    private long idTransaksi;
    private int idProduk;
    private String namaProduk = "";
    private int jumlah;
    private double harga;
    private double subtotal;
    private String keterangan = "";

    public TransactionDetail() {
        // constructor untuk pemetaan data dari database
    }

    public TransactionDetail(int idProduk, int jumlah, double harga) {
        this.idProduk = idProduk;
        this.jumlah = jumlah;
        this.harga = harga;
        this.subtotal = harga * jumlah;
    }

    public long getIdDetail() {
        return idDetail;
    }

    public void setIdDetail(long idDetail) {
        this.idDetail = idDetail;
    }

    public long getIdTransaksi() {
        return idTransaksi;
    }

    public void setIdTransaksi(long idTransaksi) {
        this.idTransaksi = idTransaksi;
    }

    public int getIdProduk() {
        return idProduk;
    }

    public void setIdProduk(int idProduk) {
        this.idProduk = idProduk;
    }

    public String getNamaProduk() {
        return namaProduk;
    }

    public void setNamaProduk(String namaProduk) {
        this.namaProduk = namaProduk;
    }

    public int getJumlah() {
        return jumlah;
    }

    public void setJumlah(int jumlah) {
        this.jumlah = jumlah;
    }

    public double getHarga() {
        return harga;
    }

    public void setHarga(double harga) {
        this.harga = harga;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    /** Keterangan tambahan (mis. fitur tambahan) - hanya untuk nota, tidak disimpan ke database. */
    public String getKeterangan() {
        return keterangan;
    }

    public void setKeterangan(String keterangan) {
        this.keterangan = keterangan;
    }

    @Override
    public String toString() {
        return namaProduk + " x" + jumlah + " = " + subtotal;
    }
}