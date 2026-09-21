# PRD — Sistem Manajemen Inventaris Toko Aksesoris Gadget

## 1. Informasi Project

| Item | Detail |
|---|---|
| Nama Project | Sistem Manajemen Inventaris Toko Aksesoris Gadget |
| Platform | Desktop |
| Bahasa Pemrograman | Java |
| IDE | IntelliJ IDEA |
| GUI | Java Swing |
| Database | MySQL |
| Database Management | phpMyAdmin |
| Target Pengguna | Admin dan Customer/Pembeli |
| Tujuan | Memenuhi kebutuhan tugas akhir mata kuliah Pemrograman Berorientasi Objek (PBO) |

## 2. Latar Belakang

Sistem Manajemen Inventaris Toko Aksesoris Gadget merupakan aplikasi desktop sederhana untuk membantu pengelolaan produk, stok, serta proses pembelian pada toko aksesoris gadget.

Sistem memiliki dua jalur penggunaan. Admin harus melakukan login untuk mengelola data toko, sedangkan customer tidak perlu membuat akun atau login dan dapat langsung mengakses halaman pembelian.

Project dirancang dengan fitur bisnis yang sederhana agar fokus utama tetap pada penerapan konsep Object-Oriented Programming (OOP), empat design pattern, Java Swing, database MySQL, serta pemodelan UML.

## 3. Tujuan Sistem

1. Mengelola data produk dan stok toko.
2. Menyediakan proses pembelian sederhana untuk customer.
3. Mencatat setiap transaksi pembelian ke database.
4. Menghasilkan nota pembelian dalam format `.txt`.
5. Menyediakan histori pembelian yang dapat dilihat admin.
6. Menerapkan konsep OOP dan design pattern secara nyata dalam sistem.

## 4. Aktor

### 4.1 Admin

Admin merupakan pengguna terautentikasi yang bertanggung jawab mengelola sistem.

Hak akses:
- Login.
- Logout.
- Melihat dashboard.
- Melihat daftar produk.
- Menambah produk.
- Mengubah produk.
- Menghapus produk.
- Mencari produk.
- Melihat stok.
- Melakukan restock.
- Menentukan stok minimum.
- Melihat peringatan stok minimum.
- Melihat histori pembelian.

### 4.2 Customer/Pembeli

Customer tidak memiliki akun.

Hak akses:
- Masuk langsung ke halaman pembelian.
- Melihat produk.
- Mencari produk.
- Menambah produk ke keranjang.
- Mengubah jumlah produk di keranjang.
- Menghapus produk dari keranjang.
- Checkout.
- Mengisi nama pembeli saat checkout.
- Memilih metode pembayaran.
- Menyelesaikan transaksi.
- Menerima nota pembelian.

Metode pembayaran:
- Cash.
- Transfer.

## 5. Scope Fitur

### 5.1 Main Menu

Saat aplikasi dijalankan tersedia:
- Masuk Admin.
- Pembelian.

Customer dapat langsung memilih `Pembelian` tanpa login.

### 5.2 Login Admin

Input:
- Username.
- Password.

Fungsi:
- Validasi kredensial admin.
- Masuk ke Admin Dashboard.
- Logout.

### 5.3 Dashboard Admin

Dashboard minimal menampilkan:
- Total produk.
- Jumlah produk dengan stok minimum/menipis.
- Jumlah transaksi hari ini.
- Jam digital realtime.

Menu:
- Produk.
- Stok.
- Histori Pembelian.
- Logout.

### 5.4 Manajemen Produk

Admin dapat:
- Menambah produk.
- Mengubah produk.
- Menghapus produk.
- Mencari produk.
- Melihat daftar produk.

Data produk:
- ID produk.
- Nama produk.
- Kategori.
- Harga beli.
- Harga jual.
- Jumlah stok.
- Stok minimum.

Kategori produk dapat mencakup:
- Cable.
- Charger.
- Case.
- Headset.

Kategori dapat diperluas sesuai kebutuhan implementasi.

### 5.5 Manajemen Stok

Fungsi:
- Melihat stok saat ini.
- Menambah stok melalui restock.
- Mengurangi stok secara otomatis setelah transaksi berhasil.
- Menampilkan peringatan ketika stok kurang dari atau sama dengan stok minimum.

Kondisi peringatan:

`jumlah_stok <= stok_minimum`

### 5.6 Halaman Pembelian Customer

Customer dapat:
- Melihat katalog produk.
- Mencari produk.
- Melihat harga.
- Melihat stok.
- Menambahkan produk ke keranjang.

### 5.7 Keranjang

Customer dapat:
- Melihat produk yang dipilih.
- Mengubah quantity.
- Menghapus item.
- Melihat subtotal setiap item.
- Melihat total transaksi.
- Melanjutkan ke checkout.

### 5.8 Checkout

Customer mengisi:
- Nama pembeli.
- Metode pembayaran.

Metode pembayaran:
- CASH.
- TRANSFER.

Setelah checkout berhasil sistem:
1. Membuat transaksi.
2. Menyimpan detail transaksi.
3. Mengurangi stok produk.
4. Menghasilkan nota `.txt`.
5. Menyelesaikan transaksi.

### 5.9 Histori Pembelian

Histori pembelian hanya dapat dilihat oleh admin.

Informasi:
- ID transaksi.
- Nama pembeli.
- Tanggal.
- Waktu.
- Total.
- Metode pembayaran.

Customer tidak memiliki histori akun karena tidak memiliki akun.

## 6. Sistem Nota

Setiap transaksi berhasil menghasilkan satu file nota dalam format `.txt`.

Format nama file:

`nota_(nama pembeli)_(tanggal bulan tahun)_(waktu 24 jam).txt`

Format tanggal:

`ddMMyyyy`

Format waktu pada nama file:

`HH-mm`

Contoh:

`nota_Budi_20092026_14-35.txt`

Karakter `:` tidak digunakan pada nama file agar kompatibel dengan Windows.

Contoh pembelian berulang oleh customer yang sama:

- `nota_Budi_20092026_14-35.txt`
- `nota_Budi_20092026_15-12.txt`
- `nota_Budi_20092026_16-47.txt`

Waktu transaksi diambil dari `LocalDateTime.now()`.

Isi nota minimal:
- Nama toko.
- ID transaksi.
- Nama pembeli.
- Tanggal.
- Waktu.
- Metode pembayaran.
- Daftar produk.
- Quantity.
- Harga.
- Subtotal.
- Total.
- Pesan penutup.

Lokasi penyimpanan nota:

`nota/`

## 7. Jam Digital

Sistem memiliki jam digital realtime sebagai fitur tambahan/gimik.

Jam ditampilkan pada:
- Admin Dashboard.
- Halaman pembelian customer.

Format tampilan:

`HH:mm:ss`

Jam diperbarui setiap satu detik menggunakan `javax.swing.Timer`.

Waktu sistem juga digunakan sebagai sumber tanggal dan waktu transaksi.

## 8. Kebutuhan OOP

### 8.1 Encapsulation

Atribut utama pada model dibuat `private`.

Contoh:
- Product.
- User.
- Admin.
- Cart.
- CartItem.
- Transaction.
- TransactionDetail.

Akses data dilakukan melalui getter/setter atau method yang sesuai.

### 8.2 Inheritance

Hierarchy produk:

```text
Product
├── Cable
├── Charger
├── Case
└── Headset
```

`Product` menjadi parent class.

### 8.3 Abstraction

`Product` dibuat sebagai abstract class yang mendefinisikan perilaku umum produk.

Contoh method abstrak:
- `getCategory()`
- `calculatePrice()`

`User` juga dapat menjadi abstract class untuk merepresentasikan pengguna terautentikasi sistem.

### 8.4 Polymorphism

Reference parent class dapat digunakan untuk object subclass.

Contoh konsep:

```java
Product product = new Charger();
Product product2 = new Cable();
```

### 8.5 Abstract Class

Abstract class utama:
- `Product`
- `User`

### 8.6 Interface

Interface digunakan untuk mendefinisikan perilaku yang memiliki banyak implementasi.

Interface utama:
- `PaymentStrategy`
- `PricingStrategy`
- `ProductComponent`

## 9. Design Pattern

### 9.1 Singleton — Database Connection

Class:

`DatabaseConnection`

Tujuan:
- Menyediakan satu instance pengelola koneksi database.
- Digunakan oleh DAO.

Contoh struktur:

```text
DatabaseConnection
├── ProductDAO
├── UserDAO
└── TransactionDAO
```

### 9.2 Decorator — Fitur Tambahan Produk

Decorator digunakan untuk menambahkan fitur opsional pada produk tanpa mengubah class produk utama.

Fitur contoh:
- Extended Warranty.
- Gift Wrapping.

Struktur:

```text
ProductComponent
└── ProductDecorator
    ├── WarrantyDecorator
    └── GiftWrapDecorator
```

### 9.3 Iterator — Koleksi Produk/Keranjang

Iterator digunakan untuk menelusuri:
- Koleksi produk.
- Item dalam keranjang.

Implementasi dapat menggunakan `java.util.Iterator`.

### 9.4 Strategy — Metode Pembayaran

Interface:

`PaymentStrategy`

Implementasi:
- `CashPayment`
- `TransferPayment`

Tujuan:
- Memungkinkan metode pembayaran diganti tanpa mengubah keseluruhan proses checkout.

### 9.5 Strategy Tambahan — Pricing

Interface:

`PricingStrategy`

Implementasi yang dapat digunakan:
- `NormalPricing`
- `DiscountPricing`

Penerapan diskon bersifat tambahan dan tidak wajib menjadi fitur bisnis utama.

## 10. Arsitektur Project

Struktur package yang direkomendasikan:

```text
src/main/java/
├── main/
├── model/
├── strategy/
├── decorator/
├── iterator/
├── database/
├── dao/
├── service/
└── view/
```

### Model

Merepresentasikan data dan object domain:
- Product.
- Cable.
- Charger.
- Case.
- Headset.
- User.
- Admin.
- Cart.
- CartItem.
- Transaction.
- TransactionDetail.

### Strategy

- PaymentStrategy.
- CashPayment.
- TransferPayment.
- PricingStrategy.
- NormalPricing.
- DiscountPricing.

### Decorator

- ProductComponent.
- ProductDecorator.
- WarrantyDecorator.
- GiftWrapDecorator.

### Iterator

- ProductCollection.
- ProductIterator.

### Database

- DatabaseConnection.

### DAO

- ProductDAO.
- UserDAO.
- TransactionDAO.

### Service

- ProductService.
- TransactionService.
- ReceiptService.

### View

- MainFrame.
- LoginFrame.
- AdminDashboard.
- ProductFrame.
- StockFrame.
- TransactionFrame.
- BuyerFrame.
- CartFrame.
- CheckoutFrame.

## 11. Database

Database utama:

`inventaris_toko`

Tabel utama:
- `user`
- `produk`
- `transaksi`
- `detail_transaksi`

### 11.1 user

Digunakan untuk akun admin.

Atribut:
- `id_user`
- `username`
- `password`
- `role`

Customer tidak disimpan dalam tabel `user`.

### 11.2 produk

Atribut:
- `id_produk`
- `nama_produk`
- `kategori`
- `harga_beli`
- `harga_jual`
- `jumlah_stok`
- `stok_minimum`

### 11.3 transaksi

Atribut:
- `id_transaksi`
- `nama_pembeli`
- `tanggal`
- `waktu`
- `total`
- `metode_pembayaran`

### 11.4 detail_transaksi

Atribut:
- `id_detail`
- `id_transaksi`
- `id_produk`
- `jumlah`
- `harga`
- `subtotal`

Relasi:

```text
transaksi 1 ----- N detail_transaksi N ----- 1 produk
```

## 12. Business Rule

1. Hanya admin yang membutuhkan login.
2. Customer dapat membeli tanpa akun.
3. Nama customer diinput saat checkout.
4. Customer hanya dapat membeli produk dengan stok mencukupi.
5. Quantity pembelian harus lebih dari 0.
6. Total transaksi merupakan akumulasi subtotal seluruh item.
7. Stok otomatis berkurang setelah transaksi berhasil.
8. Histori transaksi tersimpan di database.
9. Nota dibuat setelah transaksi berhasil.
10. Nama file nota menggunakan nama pembeli, tanggal, dan waktu.
11. Jam digital berjalan realtime.
12. Metode pembayaran hanya Cash dan Transfer.
13. Produk dengan `jumlah_stok <= stok_minimum` ditandai sebagai stok minimum/menipis.

## 13. Non-Functional Requirements

### Usability
- GUI sederhana dan mudah dipahami.
- Navigasi antar halaman jelas.
- Warna utama biru tua dan biru muda.
- Layout standar dan tidak berlebihan.

### Performance
- Operasi CRUD dan transaksi berjalan secara responsif pada skala data tugas kuliah.
- Jam digital diperbarui setiap satu detik.

### Reliability
- Transaksi hanya dianggap berhasil apabila penyimpanan transaksi dan pengurangan stok berhasil.
- Data transaksi tidak boleh kehilangan detail produk.

### Compatibility
- Aplikasi ditujukan untuk desktop.
- Nama file nota harus kompatibel dengan sistem operasi Windows.

## 14. UML

### 14.1 Use Case Diagram

Aktor:
- Admin.
- Customer.

Admin:
- Login.
- Kelola Produk.
- Kelola Stok.
- Restock.
- Lihat Histori Pembelian.
- Logout.

Customer:
- Lihat Produk.
- Cari Produk.
- Kelola Keranjang.
- Checkout.
- Input Nama Pembeli.
- Pilih Metode Pembayaran.
- Melakukan Pembayaran.
- Menerima Nota.

### 14.2 Class Diagram

Class Diagram minimal merepresentasikan:

```text
<<abstract>> Product
├── Cable
├── Charger
├── Case
└── Headset

<<abstract>> User
└── Admin

<<interface>> PaymentStrategy
├── CashPayment
└── TransferPayment

<<interface>> PricingStrategy
├── NormalPricing
└── DiscountPricing

<<interface>> ProductComponent
└── ProductDecorator
    ├── WarrantyDecorator
    └── GiftWrapDecorator

ProductCollection
└── ProductIterator

DatabaseConnection
├── ProductDAO
├── UserDAO
└── TransactionDAO

Cart
└── CartItem

Transaction
└── TransactionDetail
```

## 15. Alur Checkout Terintegrasi

```text
Customer
→ Lihat Produk
→ Pilih Produk
→ Tambah ke Keranjang
→ Checkout
→ Input Nama Pembeli
→ Pilih Cash/Transfer
→ Strategy Pattern
→ Pembayaran Berhasil
→ Ambil LocalDateTime.now()
→ Kurangi Stok
→ Simpan Transaksi ke Database
→ Generate Nota .txt
→ Transaksi Selesai
```

Konsep yang dapat ditunjukkan pada alur tersebut:

```text
Inheritance
→ Product → Charger

Abstraction
→ abstract Product

Polymorphism
→ Product product = new Charger()

Encapsulation
→ private attribute

Interface
→ PaymentStrategy

Strategy
→ CashPayment / TransferPayment

Decorator
→ Warranty / Gift Wrap

Iterator
→ Koleksi produk/keranjang

Singleton
→ DatabaseConnection
```

## 16. Fitur Tambahan/Gimik

Fitur tambahan yang tetap berada dalam scope proyek:
- Jam digital realtime.
- Search produk.
- Filter kategori.
- Peringatan stok minimum.
- Gift wrapping.
- Extended warranty.

## 17. Fitur di Luar Scope

Project tidak mencakup:
- Customer login.
- Customer registration.
- Profil customer.
- Histori pembelian per akun customer.
- Payment gateway.
- Integrasi bank.
- QRIS API sungguhan.
- Sistem pengiriman.
- Integrasi marketplace.
- Multi-cabang.
- Supplier management kompleks.
- REST API.
- Cloud infrastructure.

## 18. Target Hasil Akhir

Aplikasi dinyatakan memenuhi target apabila:
1. Admin dapat login.
2. Admin dapat melakukan CRUD produk.
3. Admin dapat mengelola stok dan restock.
4. Sistem dapat memberi peringatan stok minimum.
5. Customer dapat berbelanja tanpa login.
6. Customer dapat melakukan checkout.
7. Sistem mendukung Cash dan Transfer.
8. Sistem menyimpan transaksi dan detail transaksi ke MySQL.
9. Histori transaksi dapat dilihat admin.
10. Sistem menghasilkan nota `.txt`.
11. Jam digital tampil realtime.
12. Seluruh konsep OOP yang dipersyaratkan diterapkan.
13. Seluruh empat design pattern yang dipersyaratkan diterapkan.
14. Use Case Diagram dan Class Diagram sesuai dengan implementasi.
