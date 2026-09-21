-- ==================================================================
-- SITAG - Sistem Manajemen Inventaris Toko Aksesoris Gadget
-- Script database MySQL (import melalui phpMyAdmin / MySQL client)
-- Dibuat kompatibel dengan aplikasi Java (package berbasis JDBC).
-- ==================================================================
CREATE DATABASE IF NOT EXISTS inventaris_toko_aksesoris_gadget
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE inventaris_toko_aksesoris_gadget;

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS detail_transaksi;
DROP TABLE IF EXISTS transaksi;
DROP TABLE IF EXISTS produk;
DROP TABLE IF EXISTS user;

SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE user (
    id_user INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role ENUM('ADMIN') NOT NULL DEFAULT 'ADMIN'
) ENGINE=InnoDB;

CREATE TABLE produk (
    id_produk INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    nama_produk VARCHAR(150) NOT NULL,
    kategori VARCHAR(50) NOT NULL,
    harga_beli DECIMAL(15,2) NOT NULL,
    harga_jual DECIMAL(15,2) NOT NULL,
    jumlah_stok INT UNSIGNED NOT NULL DEFAULT 0,
    stok_minimum INT UNSIGNED NOT NULL DEFAULT 0,
    CONSTRAINT chk_produk_harga_beli CHECK (harga_beli >= 0),
    CONSTRAINT chk_produk_harga_jual CHECK (harga_jual >= 0)
) ENGINE=InnoDB;

CREATE TABLE transaksi (
    id_transaksi BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    nama_pembeli VARCHAR(150) NOT NULL,
    tanggal DATE NOT NULL,
    waktu TIME NOT NULL,
    total DECIMAL(15,2) NOT NULL,
    metode_pembayaran ENUM('CASH', 'TRANSFER') NOT NULL,
    CONSTRAINT chk_transaksi_total CHECK (total >= 0),
    INDEX idx_transaksi_tanggal (tanggal),
    INDEX idx_transaksi_nama_pembeli (nama_pembeli)
) ENGINE=InnoDB;

CREATE TABLE detail_transaksi (
    id_detail BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    id_transaksi BIGINT UNSIGNED NOT NULL,
    id_produk INT UNSIGNED NOT NULL,
    jumlah INT UNSIGNED NOT NULL,
    harga DECIMAL(15,2) NOT NULL,
    subtotal DECIMAL(15,2) NOT NULL,
    CONSTRAINT chk_detail_jumlah CHECK (jumlah > 0),
    CONSTRAINT chk_detail_harga CHECK (harga >= 0),
    CONSTRAINT chk_detail_subtotal CHECK (subtotal >= 0),
    CONSTRAINT fk_detail_transaksi
        FOREIGN KEY (id_transaksi)
        REFERENCES transaksi (id_transaksi)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_detail_produk
        FOREIGN KEY (id_produk)
        REFERENCES produk (id_produk)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    INDEX idx_detail_transaksi (id_transaksi),
    INDEX idx_detail_produk (id_produk)
) ENGINE=InnoDB;

INSERT INTO user (username, password, role)
VALUES ('admin', 'admin123', 'ADMIN');

INSERT INTO produk
    (nama_produk, kategori, harga_beli, harga_jual, jumlah_stok, stok_minimum)
VALUES
    ('Kabel Data Type-C', 'Cable', 25000.00, 40000.00, 30, 5),
    ('Charger Fast Charging 20W', 'Charger', 85000.00, 120000.00, 20, 5),
    ('Case Silicone Universal', 'Case', 30000.00, 55000.00, 25, 5),
    ('Headset Bluetooth', 'Headset', 120000.00, 175000.00, 15, 3);
