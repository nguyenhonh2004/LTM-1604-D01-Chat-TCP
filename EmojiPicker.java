package chatapp;

import javax.swing.*;
import java.awt.*;

public class EmojiPicker extends JDialog {
    public EmojiPicker(Frame owner, java.util.function.Consumer<String> onSelect) {
        super(owner, "Emoji", true);
        String[] emojis = {"😀","😁","😂","🤣","😊","😍","😘","😜","🤔","😭","😡","👍","👎","🙏","🎉","🔥","❤️"};
        JPanel p = new JPanel(new GridLayout(4,5,6,6));
        for (String e : emojis) {
            JButton b = new JButton(e);
            b.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
            b.addActionListener(ev -> {
                onSelect.accept(e);
                dispose();
            });
            p.add(b);
        }
        add(p);
        pack();
        setLocationRelativeTo(owner);
    }
}
