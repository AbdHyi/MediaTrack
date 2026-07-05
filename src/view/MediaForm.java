package view;

import config.Session;
import dao.MediaDAO;
import model.Media;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MediaForm extends JFrame {

    private static final int CARD_W = 210;
    private static final int IMG_H  = 290;
    private static final int GAP    = 14;
    private static final int PAD    = 14;

    private final MediaDAO dao     = new MediaDAO();
    private final boolean  isAdmin = Session.isAdmin();

    private JPanel      cardPanel;
    private JScrollPane scrollPane;
    private Timer       resizeTimer;
    private List<Media> currentList = new ArrayList<>();

    private final JTextField        txtCari           = new JTextField(14);
    private final JComboBox<String> cmbFilterKategori = new JComboBox<>(
                      new String[]{"Semua", "Film", "Series", "Anime"});
    private final JTextField        txtFilterTahun    = new JTextField(6);

    public MediaForm() {
        initComponents();
        loadCards();
        setLocationRelativeTo(null);
    }

    private static Font emojiFont(int size) {
        Font f = new Font("Segoe UI Emoji", Font.PLAIN, size);
        return "Dialog".equalsIgnoreCase(f.getFamily())
            ? new Font("Segoe UI Symbol", Font.PLAIN, size) : f;
    }

    private void initComponents() {
        setTitle("MediaTrack — Data Media");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(900, 680);
        setMinimumSize(new Dimension(490, 540));
        setLayout(new BorderLayout());

        add(buildHeader(), BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout());
        center.add(buildSearchPanel(), BorderLayout.NORTH);
        center.add(buildCardArea(),    BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        resizeTimer = new Timer(150, e -> renderCards());
        resizeTimer.setRepeats(false);
        addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                resizeTimer.restart();
            }
        });
    }

    // Hitung cards per baris berdasarkan lebar viewport saat ini
    private int cardsPerRow() {
        int vw = (scrollPane != null)
            ? scrollPane.getViewport().getWidth()
            : Math.max(1, getWidth() - 35);
        if (vw <= 0) vw = getWidth() - 35;
        return Math.max(1, Math.min(8, (vw - 2 * PAD + GAP) / (CARD_W + GAP)));
    }

    private JPanel buildHeader() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBackground(new Color(33, 37, 41));
        pnl.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        JLabel lbl = new JLabel("Data Media — Film · Series · Anime");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lbl.setForeground(Color.WHITE);

        JPanel pnlRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlRight.setOpaque(false);

        if (isAdmin) {
            JButton btnTambah = new JButton("+ Tambah");
            btnTambah.setFocusPainted(false);
            btnTambah.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            btnTambah.addActionListener(e -> {
                MediaEditDialog dlg = new MediaEditDialog(this, null, dao);
                dlg.setVisible(true);
                if (dlg.isChanged()) loadCards();
            });
            pnlRight.add(btnTambah);
        }

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
        txtCari.setPreferredSize(new Dimension(160, 26));
        pnl.add(txtCari);

        pnl.add(new JLabel("Kategori :"));
        cmbFilterKategori.setPreferredSize(new Dimension(110, 26));
        pnl.add(cmbFilterKategori);

        pnl.add(new JLabel("Tahun :"));
        txtFilterTahun.setPreferredSize(new Dimension(70, 26));
        pnl.add(txtFilterTahun);

        JButton btnCari  = new JButton("Cari");
        JButton btnReset = new JButton("Reset");
        btnCari.setFocusPainted(false);
        btnReset.setFocusPainted(false);
        btnCari.addActionListener(e  -> loadCards());
        btnReset.addActionListener(e -> {
            txtCari.setText("");
            cmbFilterKategori.setSelectedIndex(0);
            txtFilterTahun.setText("");
            loadCards();
        });
        txtCari.addActionListener(e        -> loadCards());
        txtFilterTahun.addActionListener(e -> loadCards());

        pnl.add(btnCari);
        pnl.add(btnReset);
        return pnl;
    }

    private JScrollPane buildCardArea() {
        cardPanel = new JPanel();
        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));
        cardPanel.setBorder(BorderFactory.createEmptyBorder(PAD, PAD, PAD, PAD));
        cardPanel.setBackground(new Color(245, 245, 245));

        scrollPane = new JScrollPane(cardPanel);
        scrollPane.setVerticalScrollBarPolicy(
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().setBackground(new Color(245, 245, 245));
        return scrollPane;
    }

    // Fetch dari DB → simpan ke currentList → render
    private void loadCards() {
        String kw  = txtCari.getText().trim();
        String kat = "Semua".equals(cmbFilterKategori.getSelectedItem())
                     ? "" : (String) cmbFilterKategori.getSelectedItem();
        String thn = txtFilterTahun.getText().trim();
        currentList = dao.searchMedia(kw, kat, thn);
        renderCards();
    }

    // Layout ulang dari currentList (tanpa DB query)
    private void renderCards() {
        int cpr = cardsPerRow();
        setTitle("MediaTrack — Data Media  (" + currentList.size() + " hasil)");

        cardPanel.removeAll();

        if (currentList.isEmpty()) {
            JLabel lbl = new JLabel("Tidak ada media ditemukan.");
            lbl.setFont(new Font("Segoe UI", Font.ITALIC, 14));
            lbl.setForeground(Color.GRAY);
            lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
            cardPanel.add(Box.createVerticalStrut(40));
            cardPanel.add(lbl);
        } else {
            List<JPanel> cards = new ArrayList<>();
            for (Media m : currentList) cards.add(buildCard(m));

            for (int i = 0; i < cards.size(); i += cpr) {
                if (i > 0) cardPanel.add(Box.createVerticalStrut(GAP));

                JPanel row = new JPanel();
                row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
                row.setOpaque(false);
                row.setAlignmentX(Component.LEFT_ALIGNMENT);

                int end = Math.min(i + cpr, cards.size());
                for (int j = i; j < end; j++) {
                    if (j > i) row.add(Box.createHorizontalStrut(GAP));
                    row.add(cards.get(j));
                }

                // Slot kosong di baris terakhir agar kartu rata kiri
                int empty = cpr - (end - i);
                for (int e = 0; e < empty; e++) {
                    row.add(Box.createHorizontalStrut(GAP));
                    row.add(Box.createRigidArea(new Dimension(CARD_W, 0)));
                }

                cardPanel.add(row);
            }
        }

        cardPanel.revalidate();
        cardPanel.repaint();
    }

    private JPanel buildCard(Media m) {
        JPanel card = new JPanel(new BorderLayout(0, 0));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(213, 213, 213)),
            BorderFactory.createEmptyBorder(0, 0, 6, 0)));
        card.setMinimumSize(new Dimension(CARD_W, 0));
        card.setMaximumSize(new Dimension(CARD_W, Integer.MAX_VALUE));

        // Poster
        JLabel lblImg = new JLabel("...", SwingConstants.CENTER);
        lblImg.setPreferredSize(new Dimension(CARD_W, IMG_H));
        lblImg.setMinimumSize(new Dimension(CARD_W, IMG_H));
        lblImg.setMaximumSize(new Dimension(CARD_W, IMG_H));
        lblImg.setBackground(new Color(210, 210, 210));
        lblImg.setOpaque(true);
        lblImg.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblImg.setForeground(Color.GRAY);

        String url = m.getImageUrl();
        if (url != null && !url.isEmpty()) {
            loadImageAsync(lblImg, url);
        } else {
            lblImg.setText("No Image");
            lblImg.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        }
        card.add(lblImg, BorderLayout.NORTH);

        // Info
        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setBackground(Color.WHITE);
        info.setBorder(BorderFactory.createEmptyBorder(8, 10, 2, 10));

        String judulDisp = m.getJudul().length() > 22
                         ? m.getJudul().substring(0, 20) + "…" : m.getJudul();
        JLabel lblJudul = new JLabel("ID " + m.getIdMedia() + "  ·  " + judulDisp);
        lblJudul.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblJudul.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lblKatThn = new JLabel(m.getKategori() + "  ·  " + m.getTahunRilis());
        lblKatThn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblKatThn.setForeground(new Color(100, 100, 100));
        lblKatThn.setAlignmentX(LEFT_ALIGNMENT);

        String sut = m.getSutradara() != null ? m.getSutradara() : "–";
        if (sut.length() > 24) sut = sut.substring(0, 22) + "…";
        JLabel lblSut = new JLabel(sut);
        lblSut.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSut.setAlignmentX(LEFT_ALIGNMENT);

        info.add(lblJudul);
        info.add(Box.createVerticalStrut(3));
        info.add(lblKatThn);
        info.add(Box.createVerticalStrut(2));
        info.add(lblSut);
        info.add(Box.createVerticalStrut(6));

        // Expand sinopsis
        JTextArea txtDesc = new JTextArea(
            m.getDeskripsi() != null ? m.getDeskripsi() : "Tidak ada deskripsi.");
        txtDesc.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        txtDesc.setLineWrap(true);
        txtDesc.setWrapStyleWord(true);
        txtDesc.setEditable(false);
        txtDesc.setBackground(new Color(248, 248, 248));
        txtDesc.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        txtDesc.setAlignmentX(LEFT_ALIGNMENT);
        txtDesc.setMaximumSize(new Dimension(CARD_W - 20, Integer.MAX_VALUE));
        txtDesc.setVisible(false);

        JButton btnSyn = new JButton("▼  Sinopsis");
        btnSyn.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        btnSyn.setFocusPainted(false);
        btnSyn.setBorderPainted(false);
        btnSyn.setBackground(new Color(240, 240, 240));
        btnSyn.setAlignmentX(LEFT_ALIGNMENT);
        btnSyn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        btnSyn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSyn.addActionListener(e -> {
            boolean show = !txtDesc.isVisible();
            txtDesc.setVisible(show);
            btnSyn.setText(show ? "▲  Sinopsis" : "▼  Sinopsis");
            card.revalidate();
            card.repaint();
            SwingUtilities.invokeLater(() -> {
                cardPanel.revalidate();
                cardPanel.repaint();
                scrollPane.revalidate();
            });
        });

        info.add(btnSyn);
        info.add(txtDesc);
        info.add(Box.createVerticalStrut(4));

        // Tombol aksi
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 2));
        btnRow.setBackground(Color.WHITE);
        btnRow.setAlignmentX(LEFT_ALIGNMENT);

        if (isAdmin) {
            JButton btnEdit  = new JButton("Edit");
            JButton btnHapus = new JButton("Hapus");
            btnEdit.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            btnHapus.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            btnEdit.setFocusPainted(false);
            btnHapus.setFocusPainted(false);
            btnHapus.setForeground(new Color(180, 30, 30));
            btnEdit.addActionListener(e -> {
                Frame owner = (Frame) SwingUtilities.getWindowAncestor(card);
                MediaEditDialog dlg = new MediaEditDialog(owner, m, dao);
                dlg.setVisible(true);
                if (dlg.isChanged()) loadCards();
            });
            btnHapus.addActionListener(e -> handleHapus(m));
            btnRow.add(btnEdit);
            btnRow.add(btnHapus);
        } else {
            JButton btnReq = new JButton("Request");
            btnReq.setFont(emojiFont(11));
            btnReq.setFocusPainted(false);
            btnReq.addActionListener(e -> {
                Frame owner = (Frame) SwingUtilities.getWindowAncestor(card);
                new RequestDialog(owner, m).setVisible(true);
            });
            btnRow.add(btnReq);
        }

        info.add(btnRow);
        info.add(Box.createVerticalGlue());
        card.add(info, BorderLayout.CENTER);
        return card;
    }

    private void loadImageAsync(JLabel lblImg, String imageUrl) {
        new SwingWorker<ImageIcon, Void>() {
            @Override protected ImageIcon doInBackground() {
                try {
                    HttpURLConnection conn =
                        (HttpURLConnection) new URL(imageUrl).openConnection();
                    conn.setRequestProperty("User-Agent",
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
                    conn.setConnectTimeout(6000);
                    conn.setReadTimeout(10000);
                    BufferedImage img = ImageIO.read(conn.getInputStream());
                    if (img == null) return null;
                    return scaleToCover(img, CARD_W, IMG_H);
                } catch (Exception e) { return null; }
            }
            @Override protected void done() {
                try {
                    ImageIcon icon = get();
                    if (icon != null) { lblImg.setIcon(icon); lblImg.setText(null); }
                    else { lblImg.setText("No Image"); lblImg.setFont(new Font("Segoe UI", Font.ITALIC, 11)); }
                } catch (Exception ex) { lblImg.setText("No Image"); }
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
        int cw = Math.min(targetW, scaledW - x);
        int ch = Math.min(targetH, scaledH - y);
        return new ImageIcon(scaled.getSubimage(x, y, cw, ch));
    }

    private void handleHapus(Media m) {
        int ok = JOptionPane.showConfirmDialog(this,
            "Hapus \"" + m.getJudul() + "\"?\nSemua review terkait ikut terhapus.",
            "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok == JOptionPane.YES_OPTION) {
            if (dao.deleteMedia(m.getIdMedia())) {
                JOptionPane.showMessageDialog(this, "Media berhasil dihapus.",
                    "Berhasil", JOptionPane.INFORMATION_MESSAGE);
                loadCards();
            } else {
                JOptionPane.showMessageDialog(this, "Gagal menghapus media.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}