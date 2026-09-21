package model;

/**
 * ABSTRACT CLASS + ENCAPSULATION.
 *
 * Representasi pengguna terautentikasi sistem. Pada aplikasi ini hanya admin
 * yang memiliki akun (customer tidak perlu login sesuai PRD), sehingga
 * {@link #getRole()} dibuat abstrak agar setiap jenis pengguna wajib
 * mendefinisikan perannya sendiri.
 */
public abstract class User {

    private int idUser;
    private String username = "";
    private String password = "";

    protected User() {
        // constructor untuk pemetaan data dari database
    }

    protected User(int idUser, String username, String password) {
        this.idUser = idUser;
        this.username = username;
        this.password = password;
    }

    /** Role pengguna, nilainya sama dengan kolom {@code user.role}. */
    public abstract String getRole();

    /**
     * Validasi password.
     * Catatan: pada project tugas ini password disimpan apa adanya di database
     * (lihat database.sql) sesuai kesederhanaan scope PRD.
     */
    public boolean checkPassword(String input) {
        return password != null && password.equals(input);
    }

    /** Nama pengguna untuk ditampilkan di GUI, contoh: "admin (ADMIN)". */
    public String getDisplayName() {
        return username + " (" + getRole() + ")";
    }

    public int getIdUser() {
        return idUser;
    }

    public void setIdUser(int idUser) {
        this.idUser = idUser;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return getDisplayName();
    }
}