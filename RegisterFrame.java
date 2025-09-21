package chatapp;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class RegisterFrame extends JFrame {
    private JTextField tenDangNhapField;
    private JPasswordField matKhauField;
    private JPasswordField xacNhanField;
    private File avatarFile = null;
    private JLabel avatarPreview;

    public RegisterFrame() {
        setTitle("Đăng ký");
        setSize(400, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        add(main);

        // Tiêu đề
        JLabel tieuDe = new JLabel("ĐĂNG KÝ");
        tieuDe.setFont(new Font("Arial", Font.BOLD, 24));
        tieuDe.setAlignmentX(Component.CENTER_ALIGNMENT);
        tieuDe.setForeground(new Color(50, 50, 200));

        // Ô nhập tên đăng nhập
        tenDangNhapField = new JTextField();
        tenDangNhapField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        tenDangNhapField.setBorder(BorderFactory.createTitledBorder("Tên đăng nhập"));

        // Ô nhập mật khẩu
        matKhauField = new JPasswordField();
        matKhauField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        matKhauField.setBorder(BorderFactory.createTitledBorder("Mật khẩu"));

        // Ô nhập xác nhận mật khẩu
        xacNhanField = new JPasswordField();
        xacNhanField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        xacNhanField.setBorder(BorderFactory.createTitledBorder("Xác nhận mật khẩu"));

        // Nút chọn ảnh
        JButton chooseBtn = new JButton("Chọn ảnh đại diện");
        chooseBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        chooseBtn.addActionListener(e -> chonAnh());

        avatarPreview = new JLabel("Chưa chọn ảnh", SwingConstants.CENTER);
        avatarPreview.setForeground(Color.GRAY);
        avatarPreview.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Nút đăng ký
        JButton dangKyBtn = new JButton("Đăng ký");
        dangKyBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        dangKyBtn.setBackground(new Color(50, 50, 200));
        dangKyBtn.setForeground(Color.WHITE);
        dangKyBtn.addActionListener(e -> dangKy());

        // Link quay lại đăng nhập
        JLabel quayLaiLabel = new JLabel("<HTML><U>← Quay lại đăng nhập</U></HTML>");
        quayLaiLabel.setForeground(Color.BLUE.darker());
        quayLaiLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        quayLaiLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        quayLaiLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                dispose();
                new LoginFrame().setVisible(true);
            }
        });

        // Thêm vào layout
        main.add(tieuDe);
        main.add(Box.createRigidArea(new Dimension(0, 20)));
        main.add(tenDangNhapField);
        main.add(Box.createRigidArea(new Dimension(0, 15)));
        main.add(matKhauField);
        main.add(Box.createRigidArea(new Dimension(0, 15)));
        main.add(xacNhanField);
        main.add(Box.createRigidArea(new Dimension(0, 15)));
        main.add(chooseBtn);
        main.add(Box.createRigidArea(new Dimension(0, 10)));
        main.add(avatarPreview);
        main.add(Box.createRigidArea(new Dimension(0, 20)));
        main.add(dangKyBtn);
        main.add(Box.createRigidArea(new Dimension(0, 15)));
        main.add(quayLaiLabel);
    }

    private void chonAnh() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            avatarFile = chooser.getSelectedFile();
            avatarPreview.setText("Đã chọn: " + avatarFile.getName());
        }
    }

    private void dangKy() {
        String u = tenDangNhapField.getText().trim();
        String p = new String(matKhauField.getPassword());
        String c = new String(xacNhanField.getPassword());

        if (u.isEmpty() || p.isEmpty() || c.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin!");
            return;
        }
        if (!p.equals(c)) {
            JOptionPane.showMessageDialog(this, "Mật khẩu xác nhận không khớp!");
            return;
        }
        if (avatarFile == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ảnh đại diện!");
            return;
        }

        // Kiểm tra trùng username
        if (LoginFrame.userDB.containsKey(u)) {
            JOptionPane.showMessageDialog(this, "Tên đăng nhập đã tồn tại!");
            return;
        }

        // Lưu user mới vào database giả lập
        LoginFrame.userDB.put(u, p);

        JOptionPane.showMessageDialog(this, "Đăng ký thành công! Hãy đăng nhập.");
        dispose();
        new LoginFrame().setVisible(true);
    }
}
