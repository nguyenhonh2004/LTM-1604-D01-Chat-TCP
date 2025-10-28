package chatapp;

import javax.swing.*;
import java.awt.*;

public class EmojiPicker extends JDialog {
    public EmojiPicker(Frame owner, java.util.function.Consumer<String> onSelect) {
        super(owner, "Emoji Picker", true);

        // Danh sách emoji
        String[] emojis = {
                "😀","😁","😂","🤣","😊",
                "😍","😘","😜","🤔","😭",
                "😡","👍","👎","🙏","🎉",
                "🔥","❤️"
        };

        // Panel chứa emoji
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(4, 5, 8, 8)); // 4 hàng, 5 cột, gap 8px
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setBackground(Color.WHITE);

        // Tạo nút emoji
        for (String emoji : emojis) {
            JButton btn = new JButton(emoji);
            btn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24)); // font lớn, hiển thị emoji
            btn.setFocusPainted(false);
            btn.setMargin(new Insets(2,2,2,2));
            btn.setContentAreaFilled(false);
            btn.setOpaque(true);
            btn.setBackground(Color.WHITE);

            btn.addActionListener(ev -> {
                onSelect.accept(emoji); // gửi emoji về callback
                dispose(); // đóng dialog
            });

            panel.add(btn);
        }

        add(panel);
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }
}
