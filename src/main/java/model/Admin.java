package model;

/**
 * INHERITANCE: admin adalah {@link User} yang berhak mengelola data toko.
 */
public class Admin extends User {

    public static final String ROLE = "ADMIN";

    public Admin() {
        super();
    }

    public Admin(int idUser, String username, String password) {
        super(idUser, username, password);
    }

    @Override
    public String getRole() {
        return ROLE;
    }

    public boolean isAdmin() {
        return true;
    }
}