package view;

import config.Session;
import dao.ReviewDAO;
import model.ReviewRating;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class ReviewForm extends JFrame {

    private static final int THUMB_W = 80;
    private static final int THUMB_H = 115;

    private final ReviewDAO dao     = new ReviewDAO();
    private final boolean   isAdmin = Session.isAdmin();
    private final int       myId    = Session.getCurrentUser().getIdUser();

    private JPanel      cardPanel;
    private JScrollPane scrollPane;

    private final JTextField        txtCari   = new JTextField(14);
    private final JComboBox<String> cmbKategori = new JComboBox<>(
                      new String[]{"Semua", "Film", "Series", "Anime"});
    private final JComboBox<String> cmbRating = new JComboBox<>(
                      new String[]{"Semua","1","2","3","4","5","6","7","8","9","10"});

    public ReviewForm() {
        initComponents();
        loadCards();
        setLocationRelativeTo(null);
    }

    // UI
    private void initComponents() {
        setTitle("MediaTrack — Review & Rating");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(820, 680);
        setLayout(new BorderLayout());

        add(buildHeader(), BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout());
        center.add(buildSearchPanel(), BorderLayout.NORTH);
        center.add(buildCardArea(),    BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);
    }
    
    private static Font emojiFont(int size) {
        Font f = new Font("Segoe UI Emoji", Font.PLAIN, size);
        return "Dialog".equalsIgnoreCase(f.getFamily())
            ? new Font("Segoe UI Symbol", Font.PLAIN, size) : f;
    }
    
    private JPanel buildHeader() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBackground(new Color(33, 37, 41));
        pnl.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        JLabel lbl = new JLabel("Review & Rating — Semua User");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lbl.setForeground(Color.WHITE);

        JPanel pnlRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlRight.setOpaque(false);

        JButton btnTulis = new JButton("📝 Tulis Review");
        btnTulis.setFocusPainted(false);
        btnTulis.setFont(emojiFont(12));
        btnTulis.addActionListener(e -> {
            ReviewEditDialog dlg = new ReviewEditDialog(this, null);
            dlg.setVisible(true);
            if (dlg.isChanged()) loadCards();
        });
        pnlRight.add(btnTulis);

        JButton btnBack = new JButton("← Kembali");
        btnBack.setFocusPainted(false);
        btnBack.addActionListener(e -> dispose());
        pnlRight.add(btnBack);

        pnl.add(lbl,      BorderLayout.WEST);
        pnl.add(pnlRight, BorderLayout.EAST);
        return pnl;
    }

    private JPanel buildSearchPanel() {
        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        pnl.setBorder(BorderFactory.createMatteBorder(
            0, 0, 1, 0, new Color(200, 200, 200)));

        pnl.add(new JLabel("Cari Judul :"));
        txtCari.setPreferredSize(new Dimension(150, 26));
        pnl.add(txtCari);

        pnl.add(new JLabel("Kategori :"));
        cmbKategori.setPreferredSize(new Dimension(100, 26));
        pnl.add(cmbKategori);

        pnl.add(new JLabel("Rating :"));
        cmbRating.setPreferredSize(new Dimension(75, 26));
        pnl.add(cmbRating);

        JButton btnCari  = new JButton("Cari");
        JButton btnReset = new JButton("Reset");
        btnCari.setFocusPainted(false);
        btnReset.setFocusPainted(false);
        btnCari.addActionListener(e  -> loadCards());
        btnReset.addActionListener(e -> {
            txtCari.setText("");
            cmbKategori.setSelectedIndex(0);
            cmbRating.setSelectedIndex(0);
            loadCards();
        });
        txtCari.addActionListener(e -> loadCards());

        pnl.add(btnCari);
        pnl.add(btnReset);
        return pnl;
    }

    private JScrollPane buildCardArea() {
        cardPanel = new JPanel();
        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));
        cardPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        cardPanel.setBackground(new Color(245, 245, 245));

        scrollPane = new JScrollPane(cardPanel);
        scrollPane.setHorizontalScrollBarPolicy(
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().setBackground(new Color(245, 245, 245));
        return scrollPane;
    }

    // LOAD CARDS
    private void loadCards() {
        String kw  = txtCari.getText().trim();
        String kat = "Semua".equals(cmbKategori.getSelectedItem())
                     ? "" : (String) cmbKategori.getSelectedItem();
        String rat = "Semua".equals(cmbRating.getSelectedItem())
                     ? "" : (String) cmbRating.getSelectedItem();

        List<ReviewRating> list = dao.searchReviews(kw, kat, rat);
        setTitle("MediaTrack — Review & Rating  (" + list.size() + " hasil)");

        cardPanel.removeAll();

        if (list.isEmpty()) {
            JLabel lbl = new JLabel("Tidak ada review ditemukan.");
            lbl.setFont(new Font("Segoe UI", Font.ITALIC, 14));
            lbl.setForeground(Color.GRAY);
            lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
            cardPanel.add(Box.createVerticalStrut(40));
            cardPanel.add(lbl);
        } else {
            for (ReviewRating rr : list) {
                cardPanel.add(buildCard(rr));
                cardPanel.add(Box.createVerticalStrut(10));
            }
        }

        cardPanel.revalidate();
        cardPanel.repaint();
    }

    // BUILD SINGLE CARD
    private JPanel buildCard(ReviewRating rr) {
        JPanel card = new JPanel(new BorderLayout(10, 0));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(213, 213, 213)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        // Poster thumbnail
        JLabel lblImg = new JLabel("⏳", SwingConstants.CENTER);
        lblImg.setPreferredSize(new Dimension(THUMB_W, THUMB_H));
        lblImg.setMinimumSize(new Dimension(THUMB_W, THUMB_H));
        lblImg.setMaximumSize(new Dimension(THUMB_W, THUMB_H));
        lblImg.setOpaque(true);
        lblImg.setBackground(new Color(220, 220, 220));
        lblImg.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblImg.setForeground(Color.GRAY);

        String url = rr.getImageUrlMedia();
        if (url != null && !url.isEmpty()) {
            loadImageAsync(lblImg, url);
        } else {
            lblImg.setText("No Img");
            lblImg.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        }
        card.add(lblImg, BorderLayout.WEST);

        // Info
        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);

        // Baris atas: User + ID
        JLabel lblTop = new JLabel("ID " + rr.getIdReview()
            + "  ·  👤 " + rr.getUsername()
            + (rr.getIdUser() == myId ? "  (kamu)" : ""));
        lblTop.setFont(emojiFont(12).deriveFont(Font.BOLD));
        lblTop.setAlignmentX(LEFT_ALIGNMENT);

        // Judul media
        JLabel lblJudul = new JLabel(rr.getJudulMedia()
            + "  ·  " + rr.getKategoriMedia());
        lblJudul.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblJudul.setForeground(new Color(60, 60, 60));
        lblJudul.setAlignmentX(LEFT_ALIGNMENT);

        // Rating + Tanggal
        JPanel ratingRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 2));
        ratingRow.setOpaque(false);
        ratingRow.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lblRating = new JLabel("⭐ " + rr.getRating() + "/10");
        lblRating.setFont(emojiFont(13).deriveFont(Font.BOLD));
        lblRating.setForeground(new Color(220, 140, 0));

        JLabel lblTanggal = new JLabel("    📅 " + rr.getTanggalReview());
        lblTanggal.setFont(emojiFont(11));
        lblTanggal.setForeground(Color.GRAY);

        ratingRow.add(lblRating);
        ratingRow.add(lblTanggal);

        info.add(lblTop);
        info.add(Box.createVerticalStrut(3));
        info.add(lblJudul);
        info.add(Box.createVerticalStrut(3));
        info.add(ratingRow);
        info.add(Box.createVerticalStrut(5));

        // Expand review text
        JTextArea txtReview = new JTextArea(
            rr.getReview() != null && !rr.getReview().isEmpty()
                ? rr.getReview() : "(Tidak ada teks review)");
        txtReview.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtReview.setLineWrap(true);
        txtReview.setWrapStyleWord(true);
        txtReview.setEditable(false);
        txtReview.setBackground(new Color(248, 248, 248));
        txtReview.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        txtReview.setAlignmentX(LEFT_ALIGNMENT);
        txtReview.setVisible(false);

        JButton btnExpand = new JButton("▼  Lihat Review");
        btnExpand.setFont(emojiFont(10));
        btnExpand.setFocusPainted(false);
        btnExpand.setBorderPainted(false);
        btnExpand.setBackground(new Color(240, 240, 240));
        btnExpand.setAlignmentX(LEFT_ALIGNMENT);
        btnExpand.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        btnExpand.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnExpand.addActionListener(e -> {
            boolean show = !txtReview.isVisible();
            txtReview.setVisible(show);
            btnExpand.setText(show ? "▲  Sembunyikan" : "▼  Lihat Review");
            card.revalidate();
            card.repaint();
            SwingUtilities.invokeLater(() -> {
                cardPanel.revalidate();
                cardPanel.repaint();
                scrollPane.revalidate();
            });
        });

        info.add(btnExpand);
        info.add(txtReview);

        // Tombol Edit/Hapus (hanya milik sendiri atau admin)
        boolean bolehUbah = isAdmin || rr.getIdUser() == myId;
        if (bolehUbah) {
            info.add(Box.createVerticalStrut(6));
            JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            btnRow.setOpaque(false);
            btnRow.setAlignmentX(LEFT_ALIGNMENT);

            JButton btnEdit  = new JButton("Edit");
            JButton btnHapus = new JButton("Hapus");
            btnEdit.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            btnHapus.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            btnEdit.setFocusPainted(false);
            btnHapus.setFocusPainted(false);
            btnHapus.setForeground(new Color(180, 30, 30));

            btnEdit.addActionListener(e -> {
                ReviewEditDialog dlg = new ReviewEditDialog(this, rr);
                dlg.setVisible(true);
                if (dlg.isChanged()) loadCards();
            });
            btnHapus.addActionListener(e -> handleHapus(rr));

            btnRow.add(btnEdit);
            btnRow.add(btnHapus);
            info.add(btnRow);
        }

        card.add(info, BorderLayout.CENTER);
        return card;
    }

    // Image loading (crop-to-cover, sama seperti MediaForm)
    private void loadImageAsync(JLabel lblImg, String imageUrl) {
        new SwingWorker<ImageIcon, Void>() {
            @Override
            protected ImageIcon doInBackground() {
                try {
                    HttpURLConnection conn =
                        (HttpURLConnection) new URL(imageUrl).openConnection();
                    conn.setRequestProperty("User-Agent",
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
                    conn.setConnectTimeout(6000);
                    conn.setReadTimeout(10000);
                    BufferedImage img = ImageIO.read(conn.getInputStream());
                    if (img == null) return null;
                    return scaleToCover(img, THUMB_W, THUMB_H);
                } catch (Exception e) {
                    return null;
                }
            }
            @Override
            protected void done() {
                try {
                    ImageIcon icon = get();
                    if (icon != null) {
                        lblImg.setIcon(icon);
                        lblImg.setText(null);
                    } else {
                        lblImg.setText("No Img");
                        lblImg.setFont(new Font("Segoe UI", Font.ITALIC, 10));
                    }
                } catch (Exception ex) {
                    lblImg.setText("No Img");
                }
            }
        }.execute();
    }

    private ImageIcon scaleToCover(BufferedImage src, int targetW, int targetH) {
        double ratio  = Math.max((double) targetW / src.getWidth(),
                                 (double) targetH / src.getHeight());
        int scaledW = (int) Math.ceil(src.getWidth()  * ratio);
        int scaledH = (int) Math.ceil(src.getHeight() * ratio);

        BufferedImage scaled = new BufferedImage(scaledW, scaledH,
                                                 BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = scaled.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                            RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(src, 0, 0, scaledW, scaledH, null);
        g2.dispose();

        int x = Math.max(0, (scaledW - targetW) / 2);
        int y = Math.max(0, (scaledH - targetH) / 2);
        int cw = Math.min(targetW,  scaledW - x);
        int ch = Math.min(targetH, scaledH - y);
        return new ImageIcon(scaled.getSubimage(x, y, cw, ch));
    }

    // Hapus
    private void handleHapus(ReviewRating rr) {
        int ok = JOptionPane.showConfirmDialog(this,
            "Hapus review untuk \"" + rr.getJudulMedia() + "\"?",
            "Konfirmasi Hapus",
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok == JOptionPane.YES_OPTION) {
            if (dao.deleteReview(rr.getIdReview())) {
                JOptionPane.showMessageDialog(this,
                    "Review berhasil dihapus.", "Berhasil",
                    JOptionPane.INFORMATION_MESSAGE);
                loadCards();
            } else {
                JOptionPane.showMessageDialog(this,
                    "Gagal menghapus review.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}