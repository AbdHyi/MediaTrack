package view;

import config.Session;
import dao.MediaRequestDAO;
import model.Media;
import model.MediaRequest;
import javax.swing.*;
import java.awt.*;

public class RequestDialog extends JDialog {

    private final MediaRequestDAO dao = new MediaRequestDAO();
    private final Media           media;

    private JComboBox<String> cmbTipe;
    private JTextField        txtJudul;
    private JLabel            lblKategori;
    private JComboBox<String> cmbKategori;
    private JTextArea         txtIsi;

    public RequestDialog(Frame parent, Media media) {
        super(parent, "Ajukan Request ke Admin", true);
        this.media = media;
        initComponents();
        pack();
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    private void initComponents() {
        setPreferredSize(new Dimension(480, 400));
        setLayout(new BorderLayout());

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(33, 37, 41));
        header.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        JLabel lbl = new JLabel("Ajukan Request ke Admin");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(Color.WHITE);
        header.add(lbl);
        add(header, BorderLayout.NORTH);

        // Form
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(16, 20, 10, 20));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(5, 5, 5, 5);

        // Tipe
        c.gridy = 0; c.gridx = 0; c.weightx = 0.28;
        form.add(new JLabel("Tipe Request :"), c);
        cmbTipe = new JComboBox<>(new String[]{"tambah", "perbaikan"});
        if (media != null) cmbTipe.setSelectedItem("perbaikan");
        cmbTipe.addActionListener(e -> updateKategoriVis());
        c.gridx = 1; c.weightx = 0.72;
        form.add(cmbTipe, c);

        // Judul
        c.gridy = 1; c.gridx = 0; c.weightx = 0.28;
        form.add(new JLabel("Judul Media :"), c);
        txtJudul = new JTextField();
        if (media != null) {
            txtJudul.setText(media.getJudul());
            txtJudul.setEditable(false);
            txtJudul.setForeground(Color.GRAY);
        }
        c.gridx = 1; c.weightx = 0.72;
        form.add(txtJudul, c);

        // Kategori
        c.gridy = 2; c.gridx = 0; c.weightx = 0.28;
        lblKategori = new JLabel("Kategori :");
        form.add(lblKategori, c);
        cmbKategori = new JComboBox<>(new String[]{"Film", "Series", "Anime"});
        if (media != null && media.getKategori() != null)
            cmbKategori.setSelectedItem(media.getKategori());
        c.gridx = 1; c.weightx = 0.72;
        form.add(cmbKategori, c);

        // Isi pengajuan
        c.gridy = 3; c.gridx = 0; c.weightx = 0.28;
        c.anchor = GridBagConstraints.NORTHWEST;
        form.add(new JLabel("<html>Isi<br>Pengajuan :</html>"), c);
        c.anchor = GridBagConstraints.CENTER;
        txtIsi = new JTextArea(6, 20);
        txtIsi.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtIsi.setLineWrap(true);
        txtIsi.setWrapStyleWord(true);
        c.gridx = 1; c.weightx = 0.72;
        form.add(new JScrollPane(txtIsi), c);

        add(form, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnPanel.setBorder(BorderFactory.createMatteBorder(
            1, 0, 0, 0, new Color(200, 200, 200)));
        JButton btnKirim = new JButton("Kirim Request");
        JButton btnBatal = new JButton("Batal");
        btnKirim.setPreferredSize(new Dimension(130, 30));
        btnBatal.setPreferredSize(new Dimension(90, 30));
        btnKirim.setFocusPainted(false);
        btnBatal.setFocusPainted(false);
        btnKirim.addActionListener(e -> handleKirim());
        btnBatal.addActionListener(e  -> dispose());
        btnPanel.add(btnKirim);
        btnPanel.add(btnBatal);
        add(btnPanel, BorderLayout.SOUTH);

        updateKategoriVis();
    }

    private void updateKategoriVis() {
        boolean isTambah = "tambah".equals(cmbTipe.getSelectedItem());
        lblKategori.setVisible(isTambah);
        cmbKategori.setVisible(isTambah);
    }

    private void handleKirim() {
        String judul = txtJudul.getText().trim();
        String isi   = txtIsi.getText().trim();
        String tipe  = (String) cmbTipe.getSelectedItem();

        if (judul.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Judul wajib diisi!",
                "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (isi.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Isi pengajuan wajib diisi!",
                "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        MediaRequest req = new MediaRequest();
        req.setIdUser(Session.getCurrentUser().getIdUser());
        req.setTipe(tipe);
        req.setJudul(judul);
        req.setKategori("tambah".equals(tipe)
            ? (String) cmbKategori.getSelectedItem()
            : (media != null ? media.getKategori() : null));
        req.setIsiPengajuan(isi);
        req.setIdMediaRef(media != null ? media.getIdMedia() : 0);

        if (dao.insertRequest(req)) {
            JOptionPane.showMessageDialog(this,
                "Request berhasil dikirim!\nAdmin akan meninjau pengajuanmu.",
                "Terkirim", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this,
                "Gagal mengirim request.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}