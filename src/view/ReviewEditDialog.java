package view;

import config.Session;
import dao.MediaDAO;
import dao.ReviewDAO;
import model.Media;
import model.ReviewRating;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ReviewEditDialog extends JDialog {

    private final ReviewDAO     dao = new ReviewDAO();
    private final ReviewRating  existing;

    private JComboBox<Media> cmbMedia;
    private JSpinner         spnRating;
    private JTextArea        txtReview;

    private boolean changed = false;

    public ReviewEditDialog(Frame parent, ReviewRating existing) {
        super(parent, existing == null ? "Tulis Review Baru" : "Ubah Review", true);
        this.existing = existing;
        initComponents();
        if (existing != null) populateFields();
        setMinimumSize(new Dimension(460, 480));
        pack();
        setSize(460, 500);
        setLocationRelativeTo(parent);
        setResizable(true);
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(33, 37, 41));
        header.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        JLabel lbl = new JLabel(existing == null ? "Tulis Review Baru" : "Ubah Review");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(Color.WHITE);
        header.add(lbl, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(16, 20, 10, 20));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(5, 5, 5, 5);

        // Media
        c.gridy = 0; c.gridx = 0; c.weightx = 0.25;
        form.add(new JLabel("Media :"), c);
        cmbMedia = new JComboBox<>();
        List<Media> mediaList = new MediaDAO().getAllMedia();
        for (Media m : mediaList) cmbMedia.addItem(m);
        c.gridx = 1; c.weightx = 0.75;
        form.add(cmbMedia, c);

        // Rating
        c.gridy = 1; c.gridx = 0; c.weightx = 0.25;
        form.add(new JLabel("Rating (1–10) :"), c);
        spnRating = new JSpinner(new SpinnerNumberModel(7, 1, 10, 1));
        c.gridx = 1; c.weightx = 0.75;
        form.add(spnRating, c);

        // Review
        c.gridy = 2; c.gridx = 0; c.weightx = 0.25;
        c.anchor = GridBagConstraints.NORTHWEST;
        form.add(new JLabel("Review :"), c);
        c.anchor = GridBagConstraints.CENTER;
        txtReview = new JTextArea(9, 30);
        txtReview.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtReview.setLineWrap(true);
        txtReview.setWrapStyleWord(true);
        c.gridx = 1; c.weightx = 0.75;
        c.weighty = 1.0;
        c.fill = GridBagConstraints.BOTH;
        JScrollPane spReview = new JScrollPane(txtReview);
        spReview.setPreferredSize(new Dimension(300, 160));
        form.add(spReview, c);
        c.weighty = 0;
        c.fill = GridBagConstraints.HORIZONTAL;

        add(form, BorderLayout.CENTER);

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
        for (int i = 0; i < cmbMedia.getItemCount(); i++) {
            if (cmbMedia.getItemAt(i).getIdMedia() == existing.getIdMedia()) {
                cmbMedia.setSelectedIndex(i);
                break;
            }
        }
        spnRating.setValue(existing.getRating());
        txtReview.setText(existing.getReview() != null ? existing.getReview() : "");
    }

    private void handleSimpan() {
        if (cmbMedia.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Pilih media terlebih dahulu.",
                "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Media media = (Media) cmbMedia.getSelectedItem();

        ReviewRating rr = new ReviewRating();
        rr.setIdMedia(media.getIdMedia());
        rr.setRating((int) spnRating.getValue());
        rr.setReview(txtReview.getText().trim());

        boolean ok;
        if (existing == null) {
            rr.setIdUser(Session.getCurrentUser().getIdUser());
            ok = dao.insertReview(rr);
        } else {
            rr.setIdReview(existing.getIdReview());
            ok = dao.updateReview(rr);
        }

        if (ok) {
            JOptionPane.showMessageDialog(this,
                existing == null ? "Review berhasil ditambahkan."
                                 : "Review berhasil diubah.",
                "Berhasil", JOptionPane.INFORMATION_MESSAGE);
            changed = true;
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan review.",
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isChanged() { return changed; }
}