package view;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

public class AvatarCropDialog extends JDialog {

    private final BufferedImage srcImage;
    private double scale;
    private int    offsetX, offsetY;
    private int    dragStartX, dragStartY, prevOffX, prevOffY;

    private static final int CANVAS = 320;
    private static final int MARGIN = 20;
    private static final int CIRCLE = CANVAS - 2 * MARGIN; // 280
    private static final int RESULT = 128;

    private JPanel canvas;
    private byte[] resultBytes = null;

    public AvatarCropDialog(Frame parent, BufferedImage image) {
        super(parent, "Crop Foto Profil", true);
        this.srcImage = image;

        // Scale awal: cover lingkaran penuh tanpa sisa kosong
        scale = Math.max((double) CIRCLE / image.getWidth(),
                         (double) CIRCLE / image.getHeight());

        // Posisi awal: gambar di tengah lingkaran
        offsetX = (int)(MARGIN + (CIRCLE - image.getWidth()  * scale) / 2);
        offsetY = (int)(MARGIN + (CIRCLE - image.getHeight() * scale) / 2);

        initComponents();
        pack();
        setResizable(false);
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(33, 37, 41));
        header.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        JLabel lbl = new JLabel("Sesuaikan Foto Profil");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(Color.WHITE);
        header.add(lbl, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        // Canvas gambar
        canvas = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);

                // Background gelap
                g2.setColor(new Color(25, 25, 25));
                g2.fillRect(0, 0, CANVAS, CANVAS);

                // Gambar
                int iw = (int)(srcImage.getWidth()  * scale);
                int ih = (int)(srcImage.getHeight() * scale);
                g2.drawImage(srcImage, offsetX, offsetY, iw, ih, null);

                // Overlay di luar lingkaran
                Area outside = new Area(
                    new Rectangle2D.Double(0, 0, CANVAS, CANVAS));
                outside.subtract(new Area(
                    new Ellipse2D.Double(MARGIN, MARGIN, CIRCLE, CIRCLE)));
                g2.setColor(new Color(0, 0, 0, 160));
                g2.fill(outside);

                // Border lingkaran
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2f));
                g2.drawOval(MARGIN, MARGIN, CIRCLE, CIRCLE);

                g2.dispose();
            }
        };
        canvas.setPreferredSize(new Dimension(CANVAS, CANVAS));
        canvas.setBackground(new Color(25, 25, 25));

        // Drag untuk reposisi
        canvas.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                dragStartX = e.getX(); dragStartY = e.getY();
                prevOffX   = offsetX;  prevOffY   = offsetY;
            }
        });
        canvas.addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseDragged(MouseEvent e) {
                offsetX = prevOffX + (e.getX() - dragStartX);
                offsetY = prevOffY + (e.getY() - dragStartY);
                canvas.repaint();
            }
        });

        // Scroll untuk zoom (proporsional, mengarah ke posisi mouse)
        canvas.addMouseWheelListener(e -> {
            double factor  = 1.0 - e.getPreciseWheelRotation() * 0.06;
            double newScale = Math.max(0.1, Math.min(8.0, scale * factor));
            int mx = e.getX(), my = e.getY();
            offsetX = (int)(mx - (mx - offsetX) * newScale / scale);
            offsetY = (int)(my - (my - offsetY) * newScale / scale);
            scale = newScale;
            canvas.repaint();
        });

        JLabel hint = new JLabel(
            "Geser untuk reposisi  ·  Scroll untuk zoom",
            SwingConstants.CENTER);
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        hint.setForeground(Color.GRAY);
        hint.setBorder(BorderFactory.createEmptyBorder(6, 0, 2, 0));

        JPanel center = new JPanel(new BorderLayout());
        center.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        center.add(canvas, BorderLayout.CENTER);
        center.add(hint,   BorderLayout.SOUTH);
        add(center, BorderLayout.CENTER);

        // Tombol
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnPanel.setBorder(BorderFactory.createMatteBorder(
            1, 0, 0, 0, new Color(200, 200, 200)));
        JButton btnOk    = new JButton("Konfirmasi");
        JButton btnBatal = new JButton("Batal");
        btnOk.setPreferredSize(new Dimension(110, 30));
        btnBatal.setPreferredSize(new Dimension(90, 30));
        btnOk.setFocusPainted(false);
        btnBatal.setFocusPainted(false);
        btnOk.addActionListener(e -> { resultBytes = doCrop(); dispose(); });
        btnBatal.addActionListener(e -> dispose());
        btnPanel.add(btnOk);
        btnPanel.add(btnBatal);
        add(btnPanel, BorderLayout.SOUTH);
    }

    // Crop gambar sesuai lingkaran → RESULT × RESULT pixel
    private byte[] doCrop() {
        double f = (double) RESULT / CIRCLE;

        BufferedImage result = new BufferedImage(RESULT, RESULT,
                                                  BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = result.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                            RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        // Clip ke lingkaran
        g2.setClip(new Ellipse2D.Double(0, 0, RESULT, RESULT));

        int iw = (int)(srcImage.getWidth()  * scale * f);
        int ih = (int)(srcImage.getHeight() * scale * f);
        int dx = (int)((offsetX - MARGIN) * f);
        int dy = (int)((offsetY - MARGIN) * f);
        g2.drawImage(srcImage, dx, dy, iw, ih, null);
        g2.dispose();

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(result, "PNG", baos);
            return baos.toByteArray();
        } catch (Exception e) {
            return null;
        }
    }

    public byte[] getResultBytes() { return resultBytes; }
}