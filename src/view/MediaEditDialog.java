package view;

import dao.MediaDAO;
import model.Media;
import javax.swing.*;
import java.awt.*;

public class MediaEditDialog extends JDialog {

    private final MediaDAO dao;
    private final Media    existing;

    private JTextField        txtJudul;
    private JComboBox<String> cmbKategori;
    private JTextField        txtTahun;
    private JTextField        txtSutradara;
    private JTextArea         txtDeskripsi;
    private JTextField        txtImageUrl;

    private boolean changed = false;

    public MediaEditDialog(Frame parent, Media existing, MediaDAO dao) {
        super(parent, existing == null ? "Tambah Media" : "Edit Media", true);
        this.dao      = dao;
        this.existing = existing;
        initComponents();
        if (existing != null) populateFields();
        pack();
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(33, 37, 41));
        header.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        JLabel lbl = new JLabel(existing == null ? "Tambah Media Baru" : "Edit Media");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(Color.WHITE);
        header.add(lbl, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        // Form
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(16, 20, 10, 20));
        form.setPreferredSize(new Dimension(520, 340));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(5, 5, 5, 5);

        // Judul
        c.gridy = 0; c.gridx = 0; c.weightx = 0.2;
        form.add(new JLabel("Judul :"), c);
        txtJudul = new JTextField();
        c.gridx = 1; c.weightx = 0.8;
        form.add(txtJudul, c);

        // Kategori + Tahun
        c.gridy = 1; c.gridx = 0; c.weightx = 0.2;
        form.add(new JLabel("Kategori :"), c);
        JPanel katTahun = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        cmbKategori = new JComboBox<>(new String[]{"Film", "Series", "Anime"});
        cmbKategori.setPreferredSize(new Dimension(120, 26));
        txtTahun = new JTextField(6);
        katTahun.add(cmbKategori);
        katTahun.add(new JLabel("  Tahun :"));
        katTahun.add(txtTahun);
        c.gridx = 1; c.weightx = 0.8;
        form.add(katTahun, c);

        // Sutradara
        c.gridy = 2; c.gridx = 0; c.weightx = 0.2;
        form.add(new JLabel("Sutradara / Studio :"), c);
        txtSutradara = new JTextField();
        c.gridx = 1; c.weightx = 0.8;
        form.add(txtSutradara, c);

        // Deskripsi
        c.gridy = 3; c.gridx = 0; c.weightx = 0.2;
        c.anchor = GridBagConstraints.NORTHWEST;
        form.add(new JLabel("Deskripsi :"), c);
        c.anchor = GridBagConstraints.CENTER;
        txtDeskripsi = new JTextArea(4, 20);
        txtDeskripsi.setLineWrap(true);
        txtDeskripsi.setWrapStyleWord(true);
        txtDeskripsi.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        c.gridx = 1; c.weightx = 0.8;
        c.weighty = 1.0;
        c.fill = GridBagConstraints.BOTH;
        JScrollPane spDesc = new JScrollPane(txtDeskripsi);
        spDesc.setPreferredSize(new Dimension(300, 120));
        form.add(spDesc, c);
        c.weighty = 0;
        c.fill = GridBagConstraints.HORIZONTAL;

        // Image URL
        c.gridy = 4; c.gridx = 0; c.weightx = 0.2;
        form.add(new JLabel("Image URL :"), c);
        txtImageUrl = new JTextField();
        c.gridx = 1; c.weightx = 0.8;
        form.add(txtImageUrl, c);

        add(form, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnPanel.setBorder(BorderFactory.createMatteBorder(
            1, 0, 0, 0, new Color(200, 200, 200)));
        JButton btnSimpan = new JButton("Simpan");
        JButton btnBatal  = new JButton("Batal");
        btnSimpan.setPreferredSize(new Dimension(100, 30));
        btnBatal.setPreferredSize(new Dimension(100, 30));
        btnSimpan.setFocusPainted(false);
        btnBatal.setFocusPainted(false);
        btnSimpan.addActionListener(e -> handleSimpan());
        btnBatal.addActionListener(e  -> dispose());
        btnPanel.add(btnSimpan);
        btnPanel.add(btnBatal);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private void populateFields() {
        txtJudul.setText(existing.getJudul());
        cmbKategori.setSelectedItem(existing.getKategori());
        txtTahun.setText(String.valueOf(existing.getTahunRilis()));
        txtSutradara.setText(existing.getSutradara() != null ? existing.getSutradara() : "");
        txtDeskripsi.setText(existing.getDeskripsi() != null ? existing.getDeskripsi() : "");
        txtImageUrl.setText(existing.getImageUrl() != null ? existing.getImageUrl() : "");
    }

    private void handleSimpan() {
        String judul = txtJudul.getText().trim();
        if (judul.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Judul wajib diisi!",
                "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int tahun;
        try {
            tahun = Integer.parseInt(txtTahun.getText().trim());
            if (tahun < 1888 || tahun > 2100) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Tahun harus angka valid (1888–2100)!",
                "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Media m = new Media();
        m.setJudul(judul);
        m.setKategori((String) cmbKategori.getSelectedItem());
        m.setTahunRilis(tahun);
        m.setSutradara(txtSutradara.getText().trim());
        m.setDeskripsi(txtDeskripsi.getText().trim());
        m.setImageUrl(txtImageUrl.getText().trim());

        boolean ok;
        if (existing == null) {
            ok = dao.insertMedia(m);
        } else {
            m.setIdMedia(existing.getIdMedia());
            ok = dao.updateMedia(m);
        }

        if (ok) {
            JOptionPane.showMessageDialog(this,
                existing == null ? "Media berhasil ditambahkan."
                                 : "Media berhasil diubah.",
                "Berhasil", JOptionPane.INFORMATION_MESSAGE);
            changed = true;
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan media.",
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isChanged() { return changed; }
}