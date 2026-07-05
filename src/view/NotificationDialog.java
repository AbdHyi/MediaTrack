package view;

import config.Session;
import dao.NotificationDAO;
import model.Notification;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class NotificationDialog extends JDialog {

    private final NotificationDAO dao = new NotificationDAO();
    private JPanel listPanel;

    public NotificationDialog(Frame parent) {
        super(parent, "Kotak Pesan", true);
        initComponents();
        loadNotifs();
        dao.markAllRead(Session.getCurrentUser().getIdUser());
        setSize(520, 580);
        setMinimumSize(new Dimension(420, 400));
        setLocationRelativeTo(parent);
    }

    private static Font emojiFont(int size) {
        Font f = new Font("Segoe UI Emoji", Font.PLAIN, size);
        return "Dialog".equalsIgnoreCase(f.getFamily())
            ? new Font("Segoe UI Symbol", Font.PLAIN, size) : f;
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(33, 37, 41));
        header.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        JLabel lbl = new JLabel("Kotak Pesan");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(Color.WHITE);
        header.add(lbl, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(new Color(245, 245, 245));
        listPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getViewport().setBackground(new Color(245, 245, 245));
        add(scroll, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        footer.setBorder(BorderFactory.createMatteBorder(
            1, 0, 0, 0, new Color(210, 210, 210)));
        JButton btnTutup = new JButton("Tutup");
        btnTutup.setPreferredSize(new Dimension(100, 30));
        btnTutup.setFocusPainted(false);
        btnTutup.addActionListener(e -> dispose());
        footer.add(btnTutup);
        add(footer, BorderLayout.SOUTH);
    }

    private void loadNotifs() {
        listPanel.removeAll();
        List<Notification> notifs =
            dao.getNotifsByUser(Session.getCurrentUser().getIdUser());

        if (notifs.isEmpty()) {
            JLabel lbl = new JLabel("Belum ada pesan.");
            lbl.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            lbl.setForeground(Color.GRAY);
            lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
            listPanel.add(Box.createVerticalStrut(40));
            listPanel.add(lbl);
        } else {
            for (Notification n : notifs) {
                listPanel.add(buildCard(n));
                listPanel.add(Box.createVerticalStrut(8));
            }
        }
        listPanel.revalidate();
        listPanel.repaint();
    }

    private JPanel buildCard(Notification n) {
        boolean approved = "approved".equals(n.getTipe());
        boolean rejected = "rejected".equals(n.getTipe());

        JPanel card = new JPanel(new BorderLayout(12, 0));
        card.setBackground(n.isRead() ? Color.WHITE : new Color(255, 252, 235));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220)),
            BorderFactory.createEmptyBorder(12, 14, 12, 14)));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        String ikonTeks = approved ? "OK" : rejected ? "X" : "i";
        Color ikonWarna = approved ? new Color(25, 135, 84)
                        : rejected ? new Color(180, 30, 30)
                        : new Color(60, 120, 200);
        JLabel lblIcon = new JLabel(ikonTeks, SwingConstants.CENTER);
        lblIcon.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblIcon.setForeground(Color.WHITE);
        lblIcon.setOpaque(true);
        lblIcon.setBackground(ikonWarna);
        lblIcon.setPreferredSize(new Dimension(36, 36));
        lblIcon.setBorder(BorderFactory.createEmptyBorder());
        
        JPanel iconWrap = new JPanel(new GridBagLayout());
        iconWrap.setOpaque(false);
        iconWrap.setPreferredSize(new Dimension(44, 44));
        JLabel iconCircle = new JLabel(ikonTeks, SwingConstants.CENTER) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ikonWarna);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        iconCircle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        iconCircle.setForeground(Color.WHITE);
        iconCircle.setPreferredSize(new Dimension(36, 36));
        iconCircle.setOpaque(false);
        iconWrap.add(iconCircle);
        card.add(iconWrap, BorderLayout.WEST);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        String judulBersih = approved ? "Request Disetujui"
                            : rejected ? "Request Ditolak"
                            : n.getJudulNotif().replaceAll("[^\\x20-\\xFF]", "").trim();
        JLabel lblJudul = new JLabel(judulBersih);
        lblJudul.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblJudul.setForeground(ikonWarna);
        lblJudul.setAlignmentX(LEFT_ALIGNMENT);

        String tgl = n.getCreatedAt() != null
            ? n.getCreatedAt().substring(0, 16) : "";
        JLabel lblFrom = new JLabel("Dari: " + n.getNamaFrom() + "   ·   " + tgl);
        lblFrom.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblFrom.setForeground(Color.GRAY);
        lblFrom.setAlignmentX(LEFT_ALIGNMENT);

        JTextArea txtIsi = new JTextArea(n.getIsi() != null ? n.getIsi() : "");
        txtIsi.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtIsi.setLineWrap(true);
        txtIsi.setWrapStyleWord(true);
        txtIsi.setEditable(false);
        txtIsi.setOpaque(false);
        txtIsi.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));
        txtIsi.setAlignmentX(LEFT_ALIGNMENT);

        content.add(lblJudul);
        content.add(Box.createVerticalStrut(2));
        content.add(lblFrom);
        content.add(txtIsi);

        card.add(content, BorderLayout.CENTER);
        return card;
    }
}