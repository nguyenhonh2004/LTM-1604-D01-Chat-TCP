package chatapp;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatBubble {

    private static final int MAX_TEXT_WIDTH = 250;
    private static final int MAX_IMAGE_SIZE = 240;

    // ===== TEXT BUBBLE =====
    // ===== THAY ĐỔI: Thêm tham số 'String name' =====
    public static JPanel createTextBubble(String name, String text, boolean mine) {
        JTextPane tp = new JTextPane();
        tp.setEditorKit(new WrapEditorKit());
        tp.setEditable(false);
        tp.setOpaque(false);
        tp.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        tp.setText(text);

        // Wrap và tính preferred size để không mất chữ cuối
        tp.setSize(MAX_TEXT_WIDTH, Short.MAX_VALUE);
        Dimension pref = tp.getPreferredSize();
        tp.setPreferredSize(pref);
        tp.setMaximumSize(pref);

        JLabel timeLabel = new JLabel(new SimpleDateFormat("HH:mm").format(new Date()));
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        timeLabel.setForeground(Color.WHITE);
        timeLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.add(tp, BorderLayout.CENTER);
        content.add(timeLabel, BorderLayout.SOUTH);

        GradientPanel bubble = new GradientPanel(mine);
        bubble.setLayout(new BorderLayout());
        bubble.add(content, BorderLayout.CENTER);
        bubble.setBorder(new EmptyBorder(8, 12, 8, 12));

        JPanel wrapper = new JPanel(new FlowLayout(mine ? FlowLayout.RIGHT : FlowLayout.LEFT, 5, 0));
        wrapper.setOpaque(false);
        wrapper.add(bubble);

        JPanel outer = new JPanel();
        outer.setOpaque(false);
        outer.setLayout(new BoxLayout(outer, BoxLayout.Y_AXIS));

        // ===== THAY ĐỔI: Thêm tên nếu không phải là "mine" =====
        if (!mine && name != null && !name.isEmpty()) {
            JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0)); // Căn lề 5px giống wrapper
            namePanel.setOpaque(false);
            JLabel nameLabel = new JLabel(name);
            nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            nameLabel.setForeground(new Color(150, 150, 150)); // Màu xám nhạt
            namePanel.add(nameLabel);
            outer.add(namePanel);
            outer.add(Box.createVerticalStrut(3)); // Khoảng cách nhỏ giữa tên và bubble
        }
        // ===============================================

        outer.add(wrapper);
        outer.add(Box.createVerticalStrut(6));

        return outer;
    }

    // ===== IMAGE BUBBLE =====
    // ===== THAY ĐỔI: Thêm tham số 'String name' =====
    public static JPanel createImageBubble(String name, byte[] data, boolean mine) {
        ImageIcon icon = new ImageIcon(data);
        int w = icon.getIconWidth();
        int h = icon.getIconHeight();
        double scale = Math.min((double) MAX_IMAGE_SIZE / w, (double) MAX_IMAGE_SIZE / h);
        if (scale < 1) {
            w = (int) (w * scale);
            h = (int) (h * scale);
        }
        Image scaled = icon.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
        JLabel lbl = new JLabel(new ImageIcon(scaled));
        lbl.setBorder(new EmptyBorder(4, 4, 4, 4));

        JLabel timeLabel = new JLabel(new SimpleDateFormat("HH:mm").format(new Date()));
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        timeLabel.setForeground(Color.WHITE);
        timeLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.add(lbl, BorderLayout.CENTER);
        content.add(timeLabel, BorderLayout.SOUTH);

        GradientPanel bubble = new GradientPanel(mine);
        bubble.setLayout(new BorderLayout());
        bubble.add(content, BorderLayout.CENTER);
        bubble.setBorder(new EmptyBorder(4, 4, 4, 4));
        bubble.setPreferredSize(new Dimension(w + 20, h + 20));

        JPanel wrapper = new JPanel(new FlowLayout(mine ? FlowLayout.RIGHT : FlowLayout.LEFT, 5, 0));
        wrapper.setOpaque(false);
        wrapper.add(bubble);

        JPanel outer = new JPanel();
        outer.setOpaque(false);
        outer.setLayout(new BoxLayout(outer, BoxLayout.Y_AXIS));

        // ===== THAY ĐỔI: Thêm tên nếu không phải là "mine" =====
        if (!mine && name != null && !name.isEmpty()) {
            JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0)); // Căn lề 5px giống wrapper
            namePanel.setOpaque(false);
            JLabel nameLabel = new JLabel(name);
            nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            nameLabel.setForeground(new Color(150, 150, 150));
            namePanel.add(nameLabel);
            outer.add(namePanel);
            outer.add(Box.createVerticalStrut(3));
        }
        // ===============================================

        outer.add(wrapper);
        outer.add(Box.createVerticalStrut(6));

        return outer;
    }

    // ===== IMAGE BUBBLE KÈM FILENAME =====
    // ===== FILE BUBBLE =====
 // ChatBubble.java - đặt trong class ChatBubble
    public static JPanel createImageBubble(String name, byte[] data, String filename, boolean mine) {
        // Tên hiển thị trên cùng (chỉ khi không phải mine)
        JPanel outer = new JPanel();
        outer.setOpaque(false);
        outer.setLayout(new BoxLayout(outer, BoxLayout.Y_AXIS));

        if (!mine && name != null && !name.isEmpty()) {
            JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            namePanel.setOpaque(false);
            JLabel nameLabel = new JLabel(name);
            nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            nameLabel.setForeground(new Color(150, 150, 150));
            namePanel.add(nameLabel);
            outer.add(namePanel);
            outer.add(Box.createVerticalStrut(3));
        }

        // Tạo ảnh thu nhỏ
        ImageIcon icon = new ImageIcon(data);
        int w = icon.getIconWidth();
        int h = icon.getIconHeight();
        double scale = 1.0;
        if (w > 0 && h > 0) {
            scale = Math.min((double) MAX_IMAGE_SIZE / w, (double) MAX_IMAGE_SIZE / h);
        }
        if (scale < 1.0) {
            w = (int) (w * scale);
            h = (int) (h * scale);
        }
        Image scaled = icon.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
        JLabel imgLbl = new JLabel(new ImageIcon(scaled));
        imgLbl.setBorder(new EmptyBorder(4, 4, 4, 4));

        // tên file dưới ảnh (nếu cần)
        JLabel fileLabel = null;
        if (filename != null && !filename.isEmpty()) {
            fileLabel = new JLabel(filename);
            fileLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            fileLabel.setForeground(Color.WHITE);
            fileLabel.setBorder(new EmptyBorder(4, 6, 0, 6));
        }

        // time label
        JLabel timeLabel = new JLabel(new java.text.SimpleDateFormat("HH:mm").format(new java.util.Date()));
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        timeLabel.setForeground(Color.WHITE);
        timeLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BorderLayout());
        content.add(imgLbl, BorderLayout.CENTER);

        // nếu có tên file, đặt fileLabel ở SOUTH thay timeLabel, hoặc gom cả 2 vào box
        JPanel south = new JPanel(new BorderLayout());
        south.setOpaque(false);
        if (fileLabel != null) south.add(fileLabel, BorderLayout.WEST);
        south.add(timeLabel, BorderLayout.EAST);
        content.add(south, BorderLayout.SOUTH);

        GradientPanel bubble = new GradientPanel(mine);
        bubble.setLayout(new BorderLayout());
        bubble.add(content, BorderLayout.CENTER);
        bubble.setBorder(new EmptyBorder(6, 6, 6, 6));
        bubble.setPreferredSize(new Dimension(w + 20, h + 40)); // + space for time/filename

        // wrapper để căn trái/phải
        JPanel wrapper = new JPanel(new FlowLayout(mine ? FlowLayout.RIGHT : FlowLayout.LEFT, 5, 0));
        wrapper.setOpaque(false);
        wrapper.add(bubble);

        outer.add(wrapper);
        outer.add(Box.createVerticalStrut(4));
        return outer;
    }

    // ===== GRADIENT PANEL =====
    static class GradientPanel extends JPanel {
        boolean mine;

        public GradientPanel(boolean mine) {
            this.mine = mine;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Shadow
            g2.setColor(new Color(0,0,0,30));
            g2.fill(new RoundRectangle2D.Double(2,2,getWidth()-4,getHeight()-4,20,20));

            // Gradient
            GradientPaint gp = mine
                    ? new GradientPaint(0,0,new Color(100,50,200),getWidth(),getHeight(),new Color(200,50,150))
                    : new GradientPaint(0,0,new Color(160,120,255),getWidth(),getHeight(),new Color(255,150,200));
            g2.setPaint(gp);
            g2.fillRoundRect(0,0,getWidth()-6,getHeight()-6,20,20);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ===== WRAP EDITOR KIT =====
    static class WrapEditorKit extends StyledEditorKit {
        ViewFactory defaultFactory = new WrapColumnFactory();
        public ViewFactory getViewFactory() { return defaultFactory; }
    }

    static class WrapColumnFactory implements ViewFactory {
        public View create(Element elem) {
            String kind = elem.getName();
            if(kind!=null) {
                switch(kind){
                    case AbstractDocument.ContentElementName: return new WrapLabelView(elem);
                    case AbstractDocument.ParagraphElementName: return new ParagraphView(elem);
                    case AbstractDocument.SectionElementName: return new BoxView(elem, View.Y_AXIS);
                    case StyleConstants.ComponentElementName: return new ComponentView(elem);
                    case StyleConstants.IconElementName: return new IconView(elem);
                }
            }
            return new LabelView(elem);
        }
    }

    static class WrapLabelView extends LabelView {
        public WrapLabelView(Element elem){ super(elem); }
        public float getMinimumSpan(int axis){ return 0; }
    }

    // ===== DEFAULT AVATAR =====
    public static ImageIcon defaultAvatar(int size){
        BufferedImage bi = new BufferedImage(size,size,BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bi.createGraphics();
        g.setColor(new Color(200,200,200));
        g.fillOval(0,0,size,size);
        g.dispose();
        return new ImageIcon(bi);
    }
}