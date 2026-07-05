package view;

import config.Session;
import dao.UserDAO;
import model.User;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;

public class LoginForm extends JFrame {

    private JTextField    txtUsername;
    private JPasswordField txtPassword;
    private JButton       btnLogin;
    private JButton       btnReset;

    public LoginForm() {
        initComponents();
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        setTitle("MediaTrack — Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(420, 310);
        setResizable(false);

        // Panel utama dengan padding
        JPanel pnlMain = new JPanel(new GridBagLayout());
        pnlMain.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(5, 5, 5, 5);

        // Judul
        JLabel lblTitle = new JLabel("MediaTrack", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        c.gridx = 0; c.gridy = 0; c.gridwidth = 2;
        pnlMain.add(lblTitle, c);

        JLabel lblSub = new JLabel("Katalog Film · Series · Anime",
                                    SwingConstants.CENTER);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSub.setForeground(Color.GRAY);
        c.gridy = 1;
        pnlMain.add(lblSub, c);

        // Garis pemisah
        c.gridy = 2; c.insets = new Insets(12, 0, 12, 0);
        pnlMain.add(new JSeparator(), c);
        c.insets = new Insets(5, 5, 5, 5);

        // Field Username
        c.gridwidth = 1;
        c.gridx = 0; c.gridy = 3; c.weightx = 0.35;
        pnlMain.add(new JLabel("Username :"), c);

        txtUsername = new JTextField();
        c.gridx = 1; c.weightx = 0.65;
        pnlMain.add(txtUsername, c);

        // Field Password
        c.gridx = 0; c.gridy = 4; c.weightx = 0.35;
        pnlMain.add(new JLabel("Password :"), c);

        txtPassword = new JPasswordField();
        c.gridx = 1; c.weightx = 0.65;
        pnlMain.add(txtPassword, c);

        // Tombol Login & Reset
        JPanel pnlBtn = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btnLogin = new JButton("Login");
        btnReset = new JButton("Reset");
        btnLogin.setPreferredSize(new Dimension(90, 30));
        btnReset.setPreferredSize(new Dimension(90, 30));
        pnlBtn.add(btnLogin);
        pnlBtn.add(btnReset);

        c.gridx = 0; c.gridy = 5; c.gridwidth = 2;
        c.insets = new Insets(18, 5, 0, 5);
        pnlMain.add(pnlBtn, c);

        add(pnlMain);

        // Event: tombol
        btnLogin.addActionListener(e -> handleLogin());
        btnReset.addActionListener(e -> handleReset());

        // Event: tekan Enter di field manapun → login
        KeyAdapter enterKey = new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) handleLogin();
            }
        };
        txtUsername.addKeyListener(enterKey);
        txtPassword.addKeyListener(enterKey);
    }

    // Logic login
    private void handleLogin() {
        String username = txtUsername.getText().trim();

        char[] pwdChars = txtPassword.getPassword();
        String password = new String(pwdChars).trim();
        Arrays.fill(pwdChars, '0'); // bersihkan password dari memori

        // Validasi field kosong
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Username dan password wajib diisi!",
                "Perhatian", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Query ke database
        UserDAO dao  = new UserDAO();
        User    user = dao.login(username, password);

        if (user != null) {
            Session.setCurrentUser(user);

            JOptionPane.showMessageDialog(this,
                "Selamat datang, " + user.getNamaLengkap() + "!",
                "Login Berhasil", JOptionPane.INFORMATION_MESSAGE);

            new MainMenu().setVisible(true);
            dispose(); // tutup LoginForm
        } else {
            JOptionPane.showMessageDialog(this,
                "Username atau password salah. Silakan coba lagi.",
                "Login Gagal", JOptionPane.ERROR_MESSAGE);
            txtPassword.setText("");
            txtUsername.requestFocus();
        }
    }

    // Logic reset
    private void handleReset() {
        txtUsername.setText("");
        txtPassword.setText("");
        txtUsername.requestFocus();
    }

    // Entry point aplikasi
    public static void main(String[] args) {
        // Nimbus membuat tampilan lebih modern dari default Metal
        try {
            for (UIManager.LookAndFeelInfo lf :
                    UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(lf.getName())) {
                    UIManager.setLookAndFeel(lf.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> new LoginForm().setVisible(true));
    }
}