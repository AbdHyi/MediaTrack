package view;

import config.Session;
import dao.NotificationDAO;
import dao.UserDAO;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

public class MainMenu extends JFrame {

    private final NotificationDAO notifDAO = new NotificationDAO();
    private JButton         btnNotif;
    private AvatarComponent avatarComp;

    private static final Color[] AVATAR_COLORS = {
        new Color(52,  152, 219),
        new Color(46,  204, 113),
        new Color(155,  89, 182),
        new Color(230, 126,  34),
        new Color(231,  76,  60),
        new Color(26,  188, 156),
    };

    public MainMenu() {
        initComponents();
        setLocationRelativeTo(null);
    }

    private Color getAvatarColor(String username) {
        return AVATAR_COLORS[Math.abs(username.hashCode()) % AVATAR_COLORS.length];
    }

    private void initComponents() {
        setTitle("MediaTrack — Menu Utama");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 480);
        setResizable(false);
        setLayout(new BorderLayout());

        add(buildHeader(),      BorderLayout.NORTH);
        add(buildButtonPanel(), BorderLayout.CENTER);
        add(buildFooter(),      BorderLayout.SOUTH);

        updateNotifBadge();
    }

    // Header
    private JPanel buildHeader() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBackground(new Color(33, 37, 41));
        pnl.setBorder(BorderFactory.createEmptyBorder(16, 30, 16, 30));

        JLabel lblTitle = new JLabel("MediaTrack");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(Color.WHITE);

        JLabel lblSub = new JLabel("Katalog Film  ·  Series  ·  Anime");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSub.setForeground(new Color(170, 170, 170));

        JPanel pnlLeft = new JPanel(new GridLayout(2, 1, 0, 3));
        pnlLeft.setOpaque(false);
        pnlLeft.add(lblTitle);
        pnlLeft.add(lblSub);

        // Kanan: notif + avatar + nama
        JPanel pnlRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlRight.setOpaque(false);

        btnNotif = new JButton("✉");
        btnNotif.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        btnNotif.setFocusPainted(false);
        btnNotif.setForeground(Color.WHITE);
        btnNotif.setBackground(new Color(33, 37, 41));
        btnNotif.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
        btnNotif.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnNotif.addActionListener(e -> {
            new NotificationDialog(this).setVisible(true);
            updateNotifBadge();
        });

        avatarComp = new AvatarComponent();

        String namaUser = Session.getCurrentUser().getNamaLengkap();
        String roleUser = Session.getCurrentUser().getRole().toUpperCase();
        JLabel lblUser = new JLabel(namaUser + "  |  " + roleUser);
        lblUser.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblUser.setForeground(new Color(170, 170, 170));

        pnlRight.add(btnNotif);
        pnlRight.add(avatarComp);
        pnlRight.add(lblUser);

        pnl.add(pnlLeft,  BorderLayout.WEST);
        pnl.add(pnlRight, BorderLayout.EAST);
        return pnl;
    }

    private void updateNotifBadge() {
        int unread = notifDAO.countUnread(Session.getCurrentUser().getIdUser());
        btnNotif.setText(unread > 0 ? "✉ (" + unread + ")" : "✉");
        btnNotif.setForeground(unread > 0
            ? new Color(255, 193, 7) : Color.WHITE);
    }

    // Button Panel
    private JPanel buildButtonPanel() {
        JPanel pnl = new JPanel(new GridBagLayout());
        pnl.setBorder(BorderFactory.createEmptyBorder(30, 60, 20, 60));

        GridBagConstraints c = new GridBagConstraints();
        c.fill    = GridBagConstraints.BOTH;
        c.insets  = new Insets(10, 10, 10, 10);
        c.weightx = 0.5;
        c.weighty = 0.5;

        boolean isAdmin = Session.isAdmin();

        c.gridx = 0; c.gridy = 0;
        pnl.add(buatTombol("Data User",
            "Kelola data akun pengguna", isAdmin,
            e -> new UserForm().setVisible(true)), c);

        c.gridx = 1;
        pnl.add(buatTombol("Data Media",
            "Kelola katalog film, series, dan anime", true,
            e -> new MediaForm().setVisible(true)), c);

        c.gridx = 0; c.gridy = 1;
        pnl.add(buatTombol("Review & Rating",
            "Lihat dan kelola review pengguna", true,
            e -> new ReviewForm().setVisible(true)), c);

        c.gridx = 1;
        pnl.add(buatTombol("Laporan",
            "Lihat laporan dan statistik media", isAdmin,
            e -> new ReportForm().setVisible(true)), c);

        return pnl;
    }

    private JPanel buildFooter() {
        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 10));
        pnl.setBorder(BorderFactory.createMatteBorder(
            1, 0, 0, 0, new Color(210, 210, 210)));
        JButton btnLogout = new JButton("Logout");
        btnLogout.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnLogout.setPreferredSize(new Dimension(100, 30));
        btnLogout.setFocusPainted(false);
        btnLogout.addActionListener(e -> handleLogout());
        pnl.add(btnLogout);
        return pnl;
    }

    private JButton buatTombol(String teks, String tooltip,
                                boolean aktif, ActionListener aksi) {
        JButton btn = new JButton(teks);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        btn.setEnabled(aktif);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(240, 80));
        if (aktif) {
            btn.setToolTipText(tooltip);
            btn.addActionListener(aksi);
        } else {
            btn.setToolTipText("Hanya dapat diakses oleh Admin");
        }
        return btn;
    }

    private void handleLogout() {
        int pilih = JOptionPane.showConfirmDialog(this,
            "Yakin ingin logout?", "Konfirmasi Logout",
            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (pilih == JOptionPane.YES_OPTION) {
            Session.logout();
            new LoginForm().setVisible(true);
            dispose();
        }
    }

    // Avatar Component (inner class)
    private class AvatarComponent extends JComponent {

        private static final int SIZE = 42;
        private BufferedImage cachedImage = null;

        AvatarComponent() {
            setPreferredSize(new Dimension(SIZE, SIZE));
            setMinimumSize(new Dimension(SIZE, SIZE));
            setMaximumSize(new Dimension(SIZE, SIZE));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setToolTipText("Klik untuk ubah foto profil");
            loadPic();
            addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    showMenu(e);
                }
            });
        }

        void loadPic() {
            byte[] data = new UserDAO().getProfilePic(
                Session.getCurrentUser().getIdUser());
            cachedImage = null;
            if (data != null) {
                try {
                    cachedImage = ImageIO.read(new ByteArrayInputStream(data));
                } catch (Exception ignored) {}
            }
            repaint();
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                                RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            if (cachedImage != null) {
                // Gambar profil custom
                g2.setClip(new Ellipse2D.Double(0, 0, SIZE, SIZE));
                g2.drawImage(cachedImage, 0, 0, SIZE, SIZE, null);
                g2.setClip(null);
            } else {
                // Default avatar
                boolean admin = Session.isAdmin();
                Color bg = admin
                    ? new Color(33, 37, 41)
                    : getAvatarColor(Session.getCurrentUser().getUsername());
                g2.setColor(bg);
                g2.fillOval(0, 0, SIZE, SIZE);

                String txt = admin ? "ADM"
                    : String.valueOf(
                        Session.getCurrentUser().getNamaLengkap().charAt(0)
                      ).toUpperCase();
                g2.setFont(new Font("Segoe UI", Font.BOLD, admin ? 11 : 16));
                g2.setColor(Color.WHITE);
                FontMetrics fm = g2.getFontMetrics();
                int tx = (SIZE - fm.stringWidth(txt)) / 2;
                int ty = (SIZE - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(txt, tx, ty);
            }

            // Border ring tipis
            g2.setClip(null);
            g2.setColor(new Color(255, 255, 255, 80));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawOval(1, 1, SIZE - 2, SIZE - 2);
            g2.dispose();
        }

        private void showMenu(MouseEvent e) {
            JPopupMenu menu = new JPopupMenu();
            JMenuItem itemGanti = new JMenuItem("Ganti Foto Profil");
            JMenuItem itemHapus = new JMenuItem("Hapus Foto Profil");
            itemHapus.setForeground(new Color(180, 30, 30));
            itemHapus.setEnabled(cachedImage != null);
            itemGanti.addActionListener(ev -> handleGanti());
            itemHapus.addActionListener(ev -> handleHapus());
            menu.add(itemGanti);
            menu.add(itemHapus);
            menu.show(this, e.getX(), e.getY());
        }

        private void handleGanti() {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Pilih Foto Profil");
            chooser.setFileFilter(new FileNameExtensionFilter(
                "Gambar (JPG, PNG)", "jpg", "jpeg", "png"));
            if (chooser.showOpenDialog(MainMenu.this)
                    != JFileChooser.APPROVE_OPTION) return;
            try {
                BufferedImage img = ImageIO.read(chooser.getSelectedFile());
                if (img == null) {
                    JOptionPane.showMessageDialog(MainMenu.this,
                        "File tidak valid.", "Error",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                AvatarCropDialog dlg = new AvatarCropDialog(MainMenu.this, img);
                dlg.setVisible(true);
                byte[] result = dlg.getResultBytes();
                if (result != null) {
                    new UserDAO().updateProfilePic(
                        Session.getCurrentUser().getIdUser(), result);
                    loadPic();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(MainMenu.this,
                    "Gagal memuat gambar: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        private void handleHapus() {
            int ok = JOptionPane.showConfirmDialog(MainMenu.this,
                "Hapus foto profil dan kembali ke tampilan default?",
                "Konfirmasi", JOptionPane.YES_NO_OPTION);
            if (ok == JOptionPane.YES_OPTION) {
                new UserDAO().updateProfilePic(
                    Session.getCurrentUser().getIdUser(), null);
                loadPic();
            }
        }
    }
}