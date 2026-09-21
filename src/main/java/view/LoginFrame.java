package view;

import dao.UserDAO;
import database.DatabaseConnection;
import database.DatabaseException;
import model.Admin;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;

/**
 * Halaman login admin (PRD bagian 5.2).
 *
 * Hanya admin yang memiliki akun; customer tidak melalui halaman ini.
 */
public class LoginFrame extends BaseFrame {

    private static final long serialVersionUID = 1L;

    private final UserDAO userDAO = new UserDAO();

    private final JTextField usernameField = Theme.textField(16);
    private final JPasswordField passwordField = Theme.passwordField(16);
    private final JLabel pesanLabel = Theme.textBold(" ");

    public LoginFrame(JFrame mainFrame) {
        super("Login Admin", mainFrame);
        setSize(460, 400);
        add(Theme.headerPanel("Login Admin", "Masukkan Username dan Password yang tepat", null), BorderLayout.NORTH);
        add(buildForm(), BorderLayout.CENTER);
        center();
    }

    private JPanel buildForm() {
        JPanel panel = Theme.lightPanel();
        panel.setLayout(new BorderLayout(0, 12));
        panel.setBorder(Theme.padding(24));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JPanel form = Theme.lightPanel();
        form.setLayout(new GridBagLayout());

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        form.add(Theme.text("Username"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        form.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        form.add(Theme.text("Password"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        form.add(passwordField, gbc);

        JButton loginButton = Theme.primaryButton("Login");
        loginButton.addActionListener(e -> login());
        JButton backButton = Theme.secondaryButton("Kembali");
        backButton.addActionListener(e -> goBack());

        JPanel tombol = Theme.lightPanel();
        tombol.setLayout(new GridLayout(1, 2, 10, 10));
        tombol.add(loginButton);
        tombol.add(backButton);

        panel.add(form, BorderLayout.NORTH);
        panel.add(tombol, BorderLayout.CENTER);

        JPanel bawah = Theme.lightPanel();
        bawah.setLayout(new GridLayout(2, 1, 0, 4));
        bawah.add(pesanLabel);
        bawah.add(Theme.text("Identitas admin demo: admin / admin123"));
        panel.add(bawah, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(loginButton);
        return panel;
    }

    private void login() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        if (username.isEmpty() || password.isEmpty()) {
            pesanLabel.setText("Username dan password wajib diisi.");
            return;
        }
        try {
            Admin admin = userDAO.findByCredentials(username, password);
            if (admin == null) {
                pesanLabel.setText("Username atau password salah.");
                return;
            }
            openNext(new AdminDashboard(getBackTarget(), admin));
        } catch (DatabaseException e) {
            pesanLabel.setText("Gagal login: " + e.getMessage());
        }
    }
}