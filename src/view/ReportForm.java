package view;

import config.Session;
import dao.MediaDAO;
import dao.MediaRequestDAO;
import dao.NotificationDAO;
import dao.ReportDAO;
import model.Media;
import model.MediaRequest;
import model.Notification;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ReportForm extends JFrame {

    private final ReportDAO       reportDAO  = new ReportDAO();
    private final MediaRequestDAO requestDAO = new MediaRequestDAO();
    private final NotificationDAO notifDAO   = new NotificationDAO();
    private final MediaDAO        mediaDAO   = new MediaDAO();

    // Summary cards
    private JLabel lblTotalMedia, lblTotalUser, lblTotalReview;

    // Tab 1–3
    private DefaultTableModel tmRating, tmTop10, tmRekap;

    // Tab 4: Request
    private DefaultTableModel  tmRequest;
    private JTable             requestTable;
    private List<MediaRequest> requestList = new ArrayList<>();
    private JComboBox<String>  cmbStatusFilter;
    private JTextArea          txtIsiDetail;
    private JButton            btnSetujui, btnTolak;

    public ReportForm() {
        initComponents();
        loadAll();
        setLocationRelativeTo(null);
    }

    // Init
    private void initComponents() {
        setTitle("MediaTrack — Laporan & Statistik");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(860, 640);
        setResizable(false);
        setLayout(new BorderLayout());

        add(buildHeader(),  BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout());
        center.add(buildSummary(), BorderLayout.NORTH);
        center.add(buildTabs(),    BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        add(buildFooter(), BorderLayout.SOUTH);
    }

    // Header
    private JPanel buildHeader() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBackground(new Color(33, 37, 41));
        pnl.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        JLabel lbl = new JLabel("Laporan & Statistik");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lbl.setForeground(Color.WHITE);

        JButton btnBack = new JButton("← Kembali");
        btnBack.setFocusPainted(false);
        btnBack.addActionListener(e -> dispose());

        pnl.add(lbl,     BorderLayout.WEST);
        pnl.add(btnBack, BorderLayout.EAST);
        return pnl;
    }

    // Summary Cards
    private JPanel buildSummary() {
        Font valFont  = new Font("Segoe UI", Font.BOLD, 28);
        Font nameFont = new Font("Segoe UI", Font.PLAIN, 11);

        lblTotalMedia  = styledLabel(valFont, new Color(13, 110, 253));
        lblTotalUser   = styledLabel(valFont, new Color(25, 135, 84));
        lblTotalReview = styledLabel(valFont, new Color(220, 120, 0));

        JPanel pnl = new JPanel(new GridLayout(1, 3, 12, 0));
        pnl.setBackground(new Color(245, 245, 245));
        pnl.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(14, 20, 14, 20)));
        pnl.add(buildCard(lblTotalMedia,  "Total Media",  nameFont));
        pnl.add(buildCard(lblTotalUser,   "Total User",   nameFont));
        pnl.add(buildCard(lblTotalReview, "Total Review", nameFont));
        return pnl;
    }

    private JLabel styledLabel(Font font, Color color) {
        JLabel lbl = new JLabel("–", SwingConstants.CENTER);
        lbl.setFont(font);
        lbl.setForeground(color);
        return lbl;
    }

    private JPanel buildCard(JLabel lblValue, String nama, Font nameFont) {
        JPanel card = new JPanel(new GridLayout(2, 1));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220)),
            BorderFactory.createEmptyBorder(8, 14, 8, 14)));
        JLabel lblName = new JLabel(nama, SwingConstants.CENTER);
        lblName.setFont(nameFont);
        lblName.setForeground(Color.GRAY);
        card.add(lblValue);
        card.add(lblName);
        return card;
    }

    // Tabs
    private JTabbedPane buildTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        tmRating = newModel(new String[]{
            "Judul", "Kategori", "Tahun", "Jml Review", "Avg Rating"});
        tmTop10  = newModel(new String[]{
            "#", "Judul", "Kategori", "Tahun", "Jml Review", "Avg Rating"});
        tmRekap  = newModel(new String[]{
            "Kategori", "Jml Media", "Total Review", "Avg Rating"});
        tmRequest = newModel(new String[]{
            "ID", "Tipe", "Judul", "User", "Kategori", "Status", "Tanggal"});

        tabs.addTab("Rating Per Media", wrapTable(tmRating));
        tabs.addTab("Top 10 Terbaik",   wrapTable(tmTop10));
        tabs.addTab("Rekap Kategori",   wrapTable(tmRekap));
        tabs.addTab("Request & Saran",  buildRequestTab());

        return tabs;
    }

    // Tab 4: Request
    private JPanel buildRequestTab() {
        JPanel pnl = new JPanel(new BorderLayout(0, 6));
        pnl.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // Filter bar
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        filterBar.add(new JLabel("Filter Status :"));
        cmbStatusFilter = new JComboBox<>(
            new String[]{"Semua", "pending", "approved", "rejected"});
        cmbStatusFilter.addActionListener(e -> loadRequestTab());
        filterBar.add(cmbStatusFilter);
        pnl.add(filterBar, BorderLayout.NORTH);

        // Table
        requestTable = new JTable(tmRequest) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        requestTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        requestTable.setRowHeight(26);
        requestTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        requestTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        requestTable.getColumnModel().getColumn(0).setMaxWidth(40);
        requestTable.getColumnModel().getColumn(1).setMaxWidth(90);
        requestTable.getColumnModel().getColumn(3).setMaxWidth(110);
        requestTable.getColumnModel().getColumn(4).setMaxWidth(90);
        requestTable.getColumnModel().getColumn(5).setMaxWidth(90);
        requestTable.getColumnModel().getColumn(6).setMaxWidth(105);

        // Status column color renderer
        requestTable.getColumnModel().getColumn(5)
            .setCellRenderer(new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(
                        JTable t, Object val, boolean sel,
                        boolean focus, int row, int col) {
                    super.getTableCellRendererComponent(
                        t, val, sel, focus, row, col);
                    if (!sel) {
                        String s = val != null ? val.toString() : "";
                        switch (s) {
                            case "approved" -> setForeground(new Color(25, 135, 84));
                            case "rejected" -> setForeground(new Color(180, 30, 30));
                            default         -> setForeground(new Color(180, 120, 0));
                        }
                    }
                    return this;
                }
            });

        // Klik baris → tampilkan isi pengajuan
        requestTable.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int row = requestTable.getSelectedRow();
            if (row >= 0 && row < requestList.size()) {
                MediaRequest req = requestList.get(row);
                txtIsiDetail.setText(req.getIsiPengajuan());
                boolean isPending = "pending".equals(req.getStatus());
                btnSetujui.setEnabled(isPending);
                btnTolak.setEnabled(isPending);
            } else {
                txtIsiDetail.setText("");
                btnSetujui.setEnabled(false);
                btnTolak.setEnabled(false);
            }
        });

        pnl.add(new JScrollPane(requestTable), BorderLayout.CENTER);

        // Panel bawah: isi detail + tombol
        JPanel bottom = new JPanel(new BorderLayout(0, 4));
        bottom.setBorder(BorderFactory.createMatteBorder(
            1, 0, 0, 0, new Color(200, 200, 200)));
        bottom.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(6, 4, 4, 4)));

        JLabel lblIsi = new JLabel("Isi Pengajuan :");
        lblIsi.setFont(new Font("Segoe UI", Font.BOLD, 11));
        bottom.add(lblIsi, BorderLayout.NORTH);

        txtIsiDetail = new JTextArea(3, 30);
        txtIsiDetail.setEditable(false);
        txtIsiDetail.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtIsiDetail.setLineWrap(true);
        txtIsiDetail.setWrapStyleWord(true);
        txtIsiDetail.setBackground(new Color(248, 248, 248));
        bottom.add(new JScrollPane(txtIsiDetail), BorderLayout.CENTER);

        // Tombol Setujui & Tolak
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 4));
        btnSetujui = new JButton("✓  Setujui");
        btnTolak   = new JButton("✗  Tolak");
        btnSetujui.setPreferredSize(new Dimension(120, 30));
        btnTolak.setPreferredSize(new Dimension(120, 30));
        btnSetujui.setFocusPainted(false);
        btnTolak.setFocusPainted(false);
        btnSetujui.setForeground(new Color(25, 135, 84));
        btnTolak.setForeground(new Color(180, 30, 30));
        btnSetujui.setEnabled(false);
        btnTolak.setEnabled(false);
        btnSetujui.addActionListener(e -> {
            int row = requestTable.getSelectedRow();
            if (row >= 0 && row < requestList.size())
                handleSetujui(requestList.get(row));
        });
        btnTolak.addActionListener(e -> {
            int row = requestTable.getSelectedRow();
            if (row >= 0 && row < requestList.size())
                handleTolak(requestList.get(row));
        });
        btnRow.add(btnSetujui);
        btnRow.add(btnTolak);
        bottom.add(btnRow, BorderLayout.SOUTH);

        pnl.add(bottom, BorderLayout.SOUTH);
        return pnl;
    }

    // Handle Approve
    private void handleSetujui(MediaRequest req) {
        String adminName = Session.getCurrentUser().getNamaLengkap();
        String pesanOk;

        if ("tambah".equals(req.getTipe())) {
            // Insert media baru dengan data dasar dari request
            Media m = new Media();
            m.setJudul(req.getJudul());
            m.setKategori(req.getKategori() != null ? req.getKategori() : "Film");
            m.setTahunRilis(Calendar.getInstance().get(Calendar.YEAR));
            m.setSutradara("");
            m.setDeskripsi(req.getIsiPengajuan());
            m.setImageUrl(null);
            mediaDAO.insertMedia(m);
            pesanOk = "Media \"" + req.getJudul() + "\" ditambahkan ke katalog.\n"
                    + "Silakan lengkapi detail (tahun, sutradara, poster) via menu Data Media.";
        } else {
            pesanOk = "Request perbaikan \"" + req.getJudul() + "\" disetujui.\n"
                    + "Silakan terapkan perubahan secara manual di Data Media.";
        }

        requestDAO.updateStatus(req.getIdRequest(), "approved");

        // Kirim notifikasi ke user
        Notification notif = new Notification();
        notif.setIdUserTo(req.getIdUser());
        notif.setIdUserFrom(Session.getCurrentUser().getIdUser());
        notif.setJudulNotif("Request Disetujui ✓");
        notif.setIsi("Request " + req.getTipe() + " \""
            + req.getJudul() + "\" disetujui oleh " + adminName + "."
            + ("tambah".equals(req.getTipe())
                ? " Media baru telah ditambahkan ke katalog."
                : " Admin akan memproses perbaikan sesegera mungkin."));
        notif.setTipe("approved");
        notifDAO.insertNotif(notif);

        JOptionPane.showMessageDialog(this, pesanOk,
            "Disetujui", JOptionPane.INFORMATION_MESSAGE);
        loadRequestTab();
    }

    // Handle Reject
    private void handleTolak(MediaRequest req) {
        JTextArea txtAlasan = new JTextArea(4, 30);
        txtAlasan.setLineWrap(true);
        txtAlasan.setWrapStyleWord(true);
        txtAlasan.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        int result = JOptionPane.showConfirmDialog(this,
            new Object[]{"Pesan untuk user (opsional):", new JScrollPane(txtAlasan)},
            "Konfirmasi Penolakan",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);

        if (result != JOptionPane.OK_OPTION) return;

        String alasan    = txtAlasan.getText().trim();
        String adminName = Session.getCurrentUser().getNamaLengkap();

        requestDAO.updateStatus(req.getIdRequest(), "rejected");

        Notification notif = new Notification();
        notif.setIdUserTo(req.getIdUser());
        notif.setIdUserFrom(Session.getCurrentUser().getIdUser());
        notif.setJudulNotif("Request Ditolak ✗");
        notif.setIsi("Request " + req.getTipe() + " \""
            + req.getJudul() + "\" ditolak oleh " + adminName + "."
            + "\nPesan Admin: " + (alasan.isEmpty() ? "-" : alasan));
        notif.setTipe("rejected");
        notifDAO.insertNotif(notif);

        JOptionPane.showMessageDialog(this,
            "Request ditolak dan user telah diberitahu.",
            "Ditolak", JOptionPane.INFORMATION_MESSAGE);
        loadRequestTab();
    }

    // Load Data
    private void loadAll() {
        lblTotalMedia.setText(String.valueOf(reportDAO.countTotal("media")));
        lblTotalUser.setText(String.valueOf(reportDAO.countTotal("users")));
        lblTotalReview.setText(String.valueOf(reportDAO.countTotal("review_rating")));

        tmRating.setRowCount(0);
        reportDAO.getRatingPerMedia().forEach(tmRating::addRow);

        tmTop10.setRowCount(0);
        reportDAO.getTop10Media().forEach(tmTop10::addRow);

        tmRekap.setRowCount(0);
        reportDAO.getRekapKategori().forEach(tmRekap::addRow);

        loadRequestTab();
    }

    private void loadRequestTab() {
        tmRequest.setRowCount(0);
        txtIsiDetail.setText("");
        btnSetujui.setEnabled(false);
        btnTolak.setEnabled(false);

        String filter = (String) cmbStatusFilter.getSelectedItem();
        requestList = requestDAO.getAllRequests(filter);

        for (MediaRequest req : requestList) {
            String tgl = req.getCreatedAt() != null
                ? req.getCreatedAt().substring(0, 10) : "-";
            tmRequest.addRow(new Object[]{
                req.getIdRequest(), req.getTipe(), req.getJudul(),
                req.getNamaUser(), req.getKategori() != null
                    ? req.getKategori() : "-",
                req.getStatus(), tgl
            });
        }
    }

    // Footer
    private JPanel buildFooter() {
        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 8));
        pnl.setBorder(BorderFactory.createMatteBorder(
            1, 0, 0, 0, new Color(210, 210, 210)));
        JButton btnRefresh = new JButton("↻  Refresh Data");
        btnRefresh.setFocusPainted(false);
        btnRefresh.setPreferredSize(new Dimension(140, 30));
        btnRefresh.addActionListener(e -> loadAll());
        pnl.add(btnRefresh);
        return pnl;
    }

    // Helper
    private DefaultTableModel newModel(String[] cols) {
        return new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
    }

    private JScrollPane wrapTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(26);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        return new JScrollPane(table);
    }
}