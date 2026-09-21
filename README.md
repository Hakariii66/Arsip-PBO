# SITAG — Sistem Manajemen Inventaris Toko Aksesoris Gadget

Aplikasi **desktop Java Swing + MySQL** untuk tugas akhir mata kuliah Pemrograman Berorientasi Objek (PBO).
Dibuat berdasarkan `prd.md` (semua fitur, business rule, dan design pattern yang dipersyaratkan sudah diterapkan).

- Main menu: **Masuk Admin** (login) dan **Pembelian** (customer tanpa login).
- Warna GUI: hanya **biru tua** dan **biru muda** (lihat `view/Theme.java`).
- Setiap transaksi menghasilkan nota `.txt` pada folder `nota/`.

---

## 1. Kebutuhan

| Kebutuhan | Keterangan |
|---|---|
| JDK | temurin-25 (project SDK sudah di-set `temurin-25` pada `.idea/misc.xml`) |
| IDE | IntelliJ IDEA 2025.3 (project ini adalah modul Java biasa, bukan Maven/Gradle) |
| Database | MySQL 8 (`localhost:3306`, user `root`, password kosong — dapat diubah) |
| Driver JDBC | sudah tersedia di `lib/mysql-connector-j-8.3.0.jar` dan sudah ditautkan ke modul |

## 2. Cara Menjalankan di IntelliJ (tanpa error)

1. Buka IntelliJ → **File ▸ Open** → pilih folder `SITAG`.
2. Pastikan **File ▸ Project Structure ▸ Project**: SDK = **temurin-25** (bahasa: Java 25).
   Module `SITAG` sudah otomatis memakai JDK project + library `mysql-connector-j` (folder `lib/`).
3. Jalankan server MySQL (Laragon/XAMPP/MySQL Server).
4. Klik kanan `src/main/java/main/Main.java` → **Run 'Main'**
   (run configuration `Main` sudah tersedia pada `.idea/runConfigurations/Main.xml`, working directory = root project).
5. Login admin memakai akun default: **username `admin` / password `admin123`**.

> Database **tidak perlu** diimport manual. Jika `db.auto.init=true` (default pada `config.properties`),
> aplikasi otomatis membuat database `inventaris_toko_aksesoris_gadget`, keempat tabel, akun admin, dan
> 4 contoh produk saat pertama kali dijalankan.
>
> Ingin membuat database manual lewat phpMyAdmin? Import file `database.sql`, lalu ubah
> `db.auto.init=false` pada `config.properties`.

## 3. Konfigurasi

`config.properties` (root project):

```properties
db.host=localhost
db.port=3306
db.name=inventaris_toko_aksesoris_gadget
db.user=root
db.password=
db.auto.init=true
toko.nama=SITAG - Toko Aksesoris Gadget
toko.alamat=Jl. Merdeka No. 10, Bandung
toko.telepon=0812-3456-7890
```

## 4. Alur Pemakaian

**Admin**: Main menu → Masuk Admin → login → Dashboard (total produk, stok menipis, transaksi hari ini, jam digital)
→ menu **Produk** (CRUD + cari + filter kategori), **Stok** (restock + ubah stok minimum + peringatan
`jumlah_stok <= stok_minimum`), **Histori Pembelian** (daftar transaksi + detail + tombol **Lihat Nota**), **Logout**.

**Customer**: Main menu → Pembelian → cari/filter produk → pilih jumlah + fitur tambahan
(Gift Wrapping / Extended Warranty) → Tambah ke Keranjang → Lihat Keranjang (ubah jumlah / hapus item) →
Checkout → isi **nama pembeli** → pilih **Cash** (isi uang diterima, otomatis menghitung kembalian) atau
**Transfer** → Bayar & Simpan Transaksi → nota `.txt` terbentuk.

## 5. Nota Pembelian

- Folder: `nota/`
- Nama file: `nota_(nama pembeli)_(ddMMyyyy)_(HH-mm).txt`, contoh: `nota_Budi_20092026_14-35.txt`
- Isi: nama toko, ID transaksi, nama pembeli, tanggal, waktu, metode pembayaran, daftar produk,
  quantity, harga, subtotal, total, dan pesan penutup.

## 6. Struktur Package

```text
src/main/java/
├── main/        Main.java (entry point, penyiapan tema & database)
├── model/       Product (abstract), Cable, Charger, Case, Headset, ProductFactory,
│                User (abstract), Admin, Cart, CartItem, Transaction, TransactionDetail
├── strategy/    PaymentStrategy, CashPayment, TransferPayment,
│                PricingStrategy, NormalPricing, DiscountPricing
├── decorator/   ProductComponent, ProductDecorator, WarrantyDecorator, GiftWrapDecorator
├── iterator/    ProductCollection, ProductIterator
├── database/    DatabaseConnection (Singleton), ConfigLoader, DatabaseException
├── dao/         ProductDAO, UserDAO, TransactionDAO
├── service/     ProductService, TransactionService, ReceiptService, CurrencyUtil
└── view/        Theme, DigitalClockLabel, BaseFrame, MainFrame, LoginFrame, AdminDashboard,
                 ProductFrame, ProductFormDialog, StockFrame, TransactionFrame, BuyerFrame,
                 CartFrame, CheckoutFrame
```

## 7. Pemetaan Konsep OOP & Design Pattern

| Konsep (PRD) | Implementasi |
|---|---|
| Encapsulation | atribut `private` + getter/setter pada `Product`, `User`, `Admin`, `Cart`, `CartItem`, `Transaction`, `TransactionDetail` |
| Inheritance | `Product` → `Cable`, `Charger`, `Case`, `Headset`; `User` → `Admin` |
| Abstraction | `Product` dan `User` adalah abstract class (`getCategory()`, `calculatePrice()`, `getRole()`) |
| Polymorphism | `Product product = new Charger()` (dipakai `ProductFactory`, DAO, dan GUI) |
| Interface | `PaymentStrategy`, `PricingStrategy`, `ProductComponent` |
| Singleton | `database/DatabaseConnection` (constructor private, `getInstance()` double-checked locking) |
| Decorator | `ProductComponent` → `ProductDecorator` → `WarrantyDecorator`, `GiftWrapDecorator` |
| Iterator | `iterator/ProductCollection` + `iterator/ProductIterator`, serta `Cart implements Iterable<CartItem>` |
| Strategy (pembayaran) | `CashPayment` / `TransferPayment` dipilih saat checkout |
| Strategy (harga) | `NormalPricing` / `DiscountPricing` (promo diskon opsional di halaman checkout) |

Detail teknis penting:

- **Total transaksi** = akumulasi subtotal seluruh detail (dihitung ulang di `Transaction.recalculateTotal()`).
- **Checkout atomik**: `TransactionDAO.save()` menyimpan header + detail + pengurangan stok dalam satu
  database transaction; bila ada yang gagal → `rollback` (tidak ada data setengah jadi).
- **Jam digital** (`view/DigitalClockLabel`) diperbarui setiap 1 detik dengan `javax.swing.Timer` dan
  memakai `LocalDateTime.now()`, sumber waktu yang sama dengan tanggal/waktu transaksi.
- **Kategori** produk dibatasi 4 subclass (`Cable`, `Charger`, `Case`, `Headset`). Untuk menambah kategori
  baru: buat subclass `Product` baru, daftarkan pada `ProductFactory`, dan tambahkan pada combo kategori.

## 8. Troubleshooting

| Gejala | Solusi |
|---|---|
| Dialog “Aplikasi tidak dapat terhubung ke database MySQL” | Jalankan server MySQL; periksa `db.host/db.port/db.user/db.password` pada `config.properties` |
| `Access denied for user 'root'@'localhost'` | Isi `db.password` sesuai password MySQL Anda |
| Tabel tidak ditemukan (saat `db.auto.init=false`) | Import `database.sql` melalui phpMyAdmin, atau set `db.auto.init=true` |
| Nama produk tampil aneh di console | Console Windows: jalankan dengan encoding UTF-8 (aplikasi GUI-nya sendiri tidak terpengaruh) |
| Login gagal | Gunakan `admin` / `admin123`, atau lihat isi tabel `user` |

## 9. Akun & Data Awal

- Admin: `admin` / `admin123` (tabel `user`, role `ADMIN`).
- Contoh produk: Kabel Data Type-C (Cable), Charger Fast Charging 20W (Charger),
  Case Silicone Universal (Case), Headset Bluetooth (Headset) — lengkap dengan harga beli, harga jual,
  stok, dan stok minimum.
