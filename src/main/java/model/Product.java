package model;

import decorator.ProductComponent;

/**
 * ABSTRACT CLASS + ENCAPSULATION + POLYMORPHISM.
 *
 * Parent dari seluruh produk toko aksesoris gadget. Atribut data produk dibuat
 * private (encapsulation) dan hanya dapat diakses melalui getter/setter.
 *
 * Method {@link #getCategory()} dan {@link #calculatePrice()} bersifat abstrak
 * sehingga wajib diimplementasikan oleh setiap subclass (Cable, Charger, Case,
 * Headset) sesuai aturan bisnis kategorinya masing-masing.
 *
 * Sebagai polymorphic reference: {@code Product product = new Charger();}
 */
public abstract class Product implements ProductComponent {

    private int idProduk;
    private String namaProduk = "";
    private double hargaBeli;
    private double hargaJual;
    private int jumlahStok;
    private int stokMinimum;

    protected Product() {
        // constructor untuk pemetaan data dari database (dilengkapi setter)
    }

    protected Product(String namaProduk, double hargaBeli, double hargaJual, int jumlahStok, int stokMinimum) {
        this.namaProduk = namaProduk;
        this.hargaBeli = hargaBeli;
        this.hargaJual = hargaJual;
        this.jumlahStok = jumlahStok;
        this.stokMinimum = stokMinimum;
    }

    /** Kategori produk (mis. "Cable", "Charger", "Case", "Headset"). */
    public abstract String getCategory();

    /**
     * Harga jual satuan yang berlaku untuk customer.
     * Setiap kategori dapat memiliki aturan pembulatan/pemrosesan harga sendiri.
     */
    public abstract double calculatePrice();

    // ------------------------- ProductComponent -------------------------

    @Override
    public String getName() {
        return namaProduk;
    }

    @Override
    public double getPrice() {
        return calculatePrice();
    }

    @Override
    public String getDescription() {
        return getCategory() + " - " + namaProduk;
    }

    // ------------------------- Business rule -------------------------

    /** Business rule PRD: produk dianggap stok menipis bila stok <= stok minimum. */
    public boolean isLowStock() {
        return jumlahStok <= stokMinimum;
    }

    /** Status stok untuk ditampilkan pada tabel GUI. */
    public String getStatusStok() {
        return isLowStock() ? "MENIPIS" : "AMAN";
    }

    /** Pembulatan harga ke atas (dipakai subclass untuk aturan harga kategorinya). */
    protected static double roundUpTo(double value, double step) {
        if (step <= 0) {
            return value;
        }
        return Math.ceil(value / step) * step;
    }

    // ------------------------- Getter & Setter -------------------------

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

    public double getHargaBeli() {
        return hargaBeli;
    }

    public void setHargaBeli(double hargaBeli) {
        this.hargaBeli = hargaBeli;
    }

    public double getHargaJual() {
        return hargaJual;
    }

    public void setHargaJual(double hargaJual) {
        this.hargaJual = hargaJual;
    }

    public int getJumlahStok() {
        return jumlahStok;
    }

    public void setJumlahStok(int jumlahStok) {
        this.jumlahStok = jumlahStok;
    }

    public int getStokMinimum() {
        return stokMinimum;
    }

    public void setStokMinimum(int stokMinimum) {
        this.stokMinimum = stokMinimum;
    }

    @Override
    public String toString() {
        return namaProduk + " (" + getCategory() + ")";
    }
}