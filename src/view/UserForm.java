package view;

import config.Session;
import dao.UserDAO;
import model.User;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class UserForm extends JFrame {

    private final UserDAO dao = new UserDAO();

    private JTable             table;
    private DefaultTableModel  tableModel;
    private JTextField         txtUsername;
    private JPasswordField     txtPassword;
    private JTextField         txtNama;
    private JComboBox<String>  cmbRole;
    private JButton            btnTambah, btnUbah, btnHapus, btnBersihkan;

    private int selectedId = -1;

    public UserForm() {
        initComponents();
        loadTable();
        setLocationRelativeTo(null);
    }

    // UI
    private void initComponents() {
        setTitle("MediaTrack — Data User");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(760, 560);
        setResizable(false);
        setLayout(new BorderLayout());

        add(buildHeader(),    BorderLayout.NORTH);
        add(buildTable(),     BorderLayout.CENTER);
        add(buildFormPanel(), BorderLayout.SOUTH);

        btnTambah.addActionListener(e    -> handleTambah());
        btnUbah.addActionListener(e      -> handleUbah());
        btnHapus.addActionListener(e     -> handleHapus());
        btnBersihkan.addActionListener(e -> bersihkanForm());
    }

    private JPanel buildHeader() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBackground(new Color(33, 37, 41));
        pnl.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        JLabel lbl = new JLabel("Data User");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lbl.setForeground(Color.WHITE);

        JButton btnBack = new JButton("← Kembali");
        btnBack.setFocusPainted(false);
        btnBack.addActionListener(e -> dispose());

        pnl.add(lbl,     BorderLayout.WEST);
        pnl.add(btnBack, BorderLayout.EAST);
        return pnl;
    }

    private JScrollPane buildTable() {
        String[] cols = {"ID", "Username", "Nama Lengkap", "Role"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(26);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.getColumnModel().getColumn(0).setMaxWidth(50);

        table.getSelectionModel().addListSelectionListener(
            e -> { if (!e.getValueIsAdjusting()) populateForm(); });

        return new JScrollPane(table);
    }

    private JPanel buildFormPanel() {
        JPanel pnl = new JPanel(new GridBagLayout());
        pnl.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(12, 20, 10, 20)
        ));
        GridBagConstraints c = new GridBagConstraints();
        c.fill    = GridBagConstraints.HORIZONTAL;
        c.insets  = new Insets(4, 6, 4, 6);

        // Baris 1: Username | Nama Lengkap
        c.gridy = 0; c.gridx = 0; c.weightx = 0.12;
        pnl.add(new JLabel("Username :"), c);
        txtUsername = new JTextField();
        c.gridx = 1; c.weightx = 0.38;
        pnl.add(txtUsername, c);

        c.gridx = 2; c.weightx = 0.12;
        pnl.add(new JLabel("Nama Lengkap :"), c);
        txtNama = new JTextField();
        c.gridx = 3; c.weightx = 0.38;
        pnl.add(txtNama, c);

        // Baris 2: Password | Role
        c.gridy = 1; c.gridx = 0; c.weightx = 0.12;
        pnl.add(new JLabel("Password :"), c);
        txtPassword = new JPasswordField();
        c.gridx = 1; c.weightx = 0.38;
        pnl.add(txtPassword, c);

        // Catatan password
        JLabel lblNote = new JLabel("* Kosongkan jika tidak ingin mengubah password");
        lblNote.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        lblNote.setForeground(Color.GRAY);
        c.gridx = 2; c.gridwidth = 2; c.weightx = 0.5;
        pnl.add(lblNote, c);
        c.gridwidth = 1;

        // Baris 3: Dropdown Role (di kolom Role)
        c.gridy = 2; c.gridx = 0; c.weightx = 0.12;
        pnl.add(new JLabel("Role :"), c);
        cmbRole = new JComboBox<>(new String[]{"user", "admin"});
        c.gridx = 1; c.weightx = 0.38;
        pnl.add(cmbRole, c);

        // Baris 4: Tombol
        JPanel pnlBtn = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 4));
        btnTambah    = new JButton("Tambah");
        btnUbah      = new JButton("Ubah");
        btnHapus     = new JButton("Hapus");
        btnBersihkan = new JButton("Bersihkan");

        Dimension btnSize = new Dimension(100, 30);
        for (JButton b : new JButton[]{btnTambah, btnUbah, btnHapus, btnBersihkan}) {
            b.setPreferredSize(btnSize);
            b.setFocusPainted(false);
            pnlBtn.add(b);
        }
        btnHapus.setForeground(new Color(180, 30, 30));

        c.gridy = 3; c.gridx = 0; c.gridwidth = 4; c.weightx = 1;
        pnl.add(pnlBtn, c);

        return pnl;
    }

    // Data
    private void loadTable() {
        tableModel.setRowCount(0);
        for (User u : dao.getAllUsers()) {
            tableModel.addRow(new Object[]{
                u.getIdUser(), u.getUsername(), u.getNamaLengkap(), u.getRole()
            });
        }
    }

    private void populateForm() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        selectedId = (int) tableModel.getValueAt(row, 0);
        txtUsername.setText((String) tableModel.getValueAt(row, 1));
        txtNama.setText((String) tableModel.getValueAt(row, 2));
        cmbRole.setSelectedItem(tableModel.getValueAt(row, 3));
        txtPassword.setText("");
        txtPassword.requestFocus();
    }

    private void bersihkanForm() {
        selectedId = -1;
        txtUsername.setText("");
        txtPassword.setText("");
        txtNama.setText("");
        cmbRole.setSelectedIndex(0);
        table.clearSelection();
        txtUsername.requestFocus();
    }

    // Validasi
    private boolean validasi(boolean passwordWajib) {
        if (txtUsername.getText().trim().isEmpty()
                || txtNama.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Username dan Nama Lengkap wajib diisi!",
                "Validasi", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (passwordWajib
                && new String(txtPassword.getPassword()).trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Password wajib diisi untuk user baru!",
                "Validasi", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    // TAMBAH
    private void handleTambah() {
        if (!validasi(true)) return;

        String username = txtUsername.getText().trim();
        if (dao.isUsernameExists(username, 0)) {
            JOptionPane.showMessageDialog(this,
                "Username \"" + username + "\" sudah dipakai!",
                "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        User u = new User();
        u.setUsername(username);
        u.setPassword(new String(txtPassword.getPassword()).trim());
        u.setNamaLengkap(txtNama.getText().trim());
        u.setRole((String) cmbRole.getSelectedItem());

        if (dao.insertUser(u)) {
            JOptionPane.showMessageDialog(this,
                "User berhasil ditambahkan.",
                "Berhasil", JOptionPane.INFORMATION_MESSAGE);
            loadTable();
            bersihkanForm();
        } else {
            JOptionPane.showMessageDialog(this,
                "Gagal menambahkan user.",
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // UBAH
    private void handleUbah() {
        if (selectedId < 0) {
            JOptionPane.showMessageDialog(this,
                "Pilih user dari tabel terlebih dahulu.",
                "Perhatian", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!validasi(false)) return;

        String username = txtUsername.getText().trim();
        if (dao.isUsernameExists(username, selectedId)) {
            JOptionPane.showMessageDialog(this,
                "Username \"" + username + "\" sudah dipakai!",
                "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        User u = new User();
        u.setIdUser(selectedId);
        u.setUsername(username);
        u.setNamaLengkap(txtNama.getText().trim());
        u.setRole((String) cmbRole.getSelectedItem());

        String pwd = new String(txtPassword.getPassword()).trim();
        u.setPassword(pwd.isEmpty() ? null : pwd); // null = jangan ubah password

        if (dao.updateUser(u)) {
            JOptionPane.showMessageDialog(this,
                "Data user berhasil diubah.",
                "Berhasil", JOptionPane.INFORMATION_MESSAGE);
            loadTable();
            bersihkanForm();
        } else {
            JOptionPane.showMessageDialog(this,
                "Gagal mengubah data user.",
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // HAPUS
    private void handleHapus() {
        if (selectedId < 0) {
            JOptionPane.showMessageDialog(this,
                "Pilih user dari tabel terlebih dahulu.",
                "Perhatian", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (selectedId == Session.getCurrentUser().getIdUser()) {
            JOptionPane.showMessageDialog(this,
                "Tidak dapat menghapus akun yang sedang digunakan!",
                "Tidak Diizinkan", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int pilih = JOptionPane.showConfirmDialog(this,
            "Yakin hapus user ini?\nReview miliknya akan ikut terhapus otomatis.",
            "Konfirmasi Hapus",
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (pilih == JOptionPane.YES_OPTION) {
            if (dao.deleteUser(selectedId)) {
                JOptionPane.showMessageDialog(this,
                    "User berhasil dihapus.",
                    "Berhasil", JOptionPane.INFORMATION_MESSAGE);
                loadTable();
                bersihkanForm();
            } else {
                JOptionPane.showMessageDialog(this,
                    "Gagal menghapus user.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}