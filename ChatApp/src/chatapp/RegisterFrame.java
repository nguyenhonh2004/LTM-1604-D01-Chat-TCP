package chatapp;

import javax.swing.*;
import java.awt.*;

public class RegisterFrame extends JFrame {
    private JTextField tenDangNhapField;
    private JPasswordField matKhauField;
    private JPasswordField xacNhanField;

    public RegisterFrame() {
        setTitle("Đăng ký");
        setSize(400, 380);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        add(main);

        JLabel tieuDe = new JLabel("ĐĂNG KÝ");
        tieuDe.setFont(new Font("Arial", Font.BOLD, 24));
        tieuDe.setAlignmentX(Component.CENTER_ALIGNMENT);
        tieuDe.setForeground(new Color(50, 50, 200));

        tenDangNhapField = new JTextField();
        tenDangNhapField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        tenDangNhapField.setBorder(BorderFactory.createTitledBorder("Tên đăng nhập"));

        matKhauField = new JPasswordField();
        matKhauField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        matKhauField.setBorder(BorderFactory.createTitledBorder("Mật khẩu"));

        xacNhanField = new JPasswordField();
        xacNhanField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        xacNhanField.setBorder(BorderFactory.createTitledBorder("Xác nhận mật khẩu"));

        JButton dangKyBtn = new JButton("Đăng ký");
        dangKyBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        dangKyBtn.setBackground(new Color(50, 50, 200));
        dangKyBtn.setForeground(Color.WHITE);
        dangKyBtn.addActionListener(e -> dangKy());

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

        main.add(tieuDe);
        main.add(Box.createRigidArea(new Dimension(0, 20)));
        main.add(tenDangNhapField);
        main.add(Box.createRigidArea(new Dimension(0, 15)));
        main.add(matKhauField);
        main.add(Box.createRigidArea(new Dimension(0, 15)));
        main.add(xacNhanField);
        main.add(Box.createRigidArea(new Dimension(0, 20)));
        main.add(dangKyBtn);
        main.add(Box.createRigidArea(new Dimension(0, 15)));
        main.add(quayLaiLabel);
    }

    private void dangKy() {
        String u = tenDangNhapField.getText().trim();
        String p = new String(matKhauField.getPassword());
        String c = new String(xacNhanField.getPassword());

        if(u.isEmpty() || p.isEmpty() || c.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        if(!p.equals(c)) {
            JOptionPane.showMessageDialog(this, "Mật khẩu xác nhận không khớp!");
            return;
        }

        if(LoginFrame.userDB.containsKey(u)) {
            JOptionPane.showMessageDialog(this, "Tên đăng nhập đã tồn tại!");
            return;
        }

        // Lưu username/password
        LoginFrame.userDB.put(u, p);

        // Avatar mặc định = null (server hoặc client tự sinh)
        byte[] avatarBytes = null;

        // Có thể tạo RegisterData nếu server cần
        Message.RegisterData regData = new Message.RegisterData(p, null, avatarBytes);

        JOptionPane.showMessageDialog(this, "Đăng ký thành công! Hãy đăng nhập.");
        dispose();
        new LoginFrame().setVisible(true);
    }
}
