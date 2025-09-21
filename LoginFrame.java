package chatapp;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;

public class LoginFrame extends JFrame {

    private JTextField tenDangNhapField;
    private JPasswordField matKhauField;

    // Giả lập database bằng HashMap (username, password)
    public static HashMap<String, String> userDB = new HashMap<>();

    static {
        userDB.put("admin", "123"); 
        userDB.put("hong", "456");
    }

    public LoginFrame() {
        setTitle("Đăng nhập");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        add(main);

        JLabel tieuDe = new JLabel("ĐĂNG NHẬP");
        tieuDe.setFont(new Font("Arial", Font.BOLD, 24));
        tieuDe.setAlignmentX(Component.CENTER_ALIGNMENT);
        tieuDe.setForeground(new Color(50, 50, 200));

        tenDangNhapField = new JTextField();
        tenDangNhapField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        tenDangNhapField.setBorder(BorderFactory.createTitledBorder("Tên đăng nhập"));

        matKhauField = new JPasswordField();
        matKhauField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        matKhauField.setBorder(BorderFactory.createTitledBorder("Mật khẩu"));

        JButton dangNhapBtn = new JButton("Đăng nhập");
        dangNhapBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        dangNhapBtn.setBackground(new Color(50, 50, 200));
        dangNhapBtn.setForeground(Color.WHITE);

        JLabel chuyenLabel = new JLabel("<HTML><U>Chưa có tài khoản? Đăng ký ngay</U></HTML>");
        chuyenLabel.setForeground(Color.BLUE.darker());
        chuyenLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        chuyenLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        main.add(tieuDe);
        main.add(Box.createRigidArea(new Dimension(0, 20)));
        main.add(tenDangNhapField);
        main.add(Box.createRigidArea(new Dimension(0, 15)));
        main.add(matKhauField);
        main.add(Box.createRigidArea(new Dimension(0, 20)));
        main.add(dangNhapBtn);
        main.add(Box.createRigidArea(new Dimension(0, 15)));
        main.add(chuyenLabel);

        // hành động
        dangNhapBtn.addActionListener(e -> dangNhap());
        chuyenLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                dispose();
                new RegisterFrame().setVisible(true);
            }
        });
    }

    private void dangNhap() {
        String u = tenDangNhapField.getText().trim();
        String p = new String(matKhauField.getPassword());

        if (u.isEmpty() || p.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu!");
            return;
        }

        boolean loginOK = userDB.containsKey(u) && userDB.get(u).equals(p);

        if (loginOK) {
            dispose();
            SwingUtilities.invokeLater(() -> {
                Client clientUI = new Client(u); // mở Client với username
                clientUI.setVisible(true);
            });
        } else {
            JOptionPane.showMessageDialog(this, "Sai tên đăng nhập hoặc mật khẩu!");
        }
    }

    // ✅ Chỉ chạy ứng dụng từ đây
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoginFrame login = new LoginFrame();
            login.setVisible(true);
        });
    }
}