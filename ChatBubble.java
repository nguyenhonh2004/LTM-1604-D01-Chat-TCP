package chatapp;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ChatBubble {
    public static JPanel createTextBubble(String text, boolean isMe) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        JTextArea ta = new JTextArea(text);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        ta.setEditable(false);
        ta.setOpaque(true);
        ta.setBorder(new EmptyBorder(8,10,8,10));
        ta.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        if (isMe) {
            ta.setBackground(new Color(140, 220, 255));
            wrapper.add(ta, BorderLayout.EAST);
        } else {
            ta.setBackground(new Color(245,245,245));
            wrapper.add(ta, BorderLayout.WEST);
        }
        return wrapper;
    }

    public static JPanel createImageBubble(byte[] data, String filename, boolean isMe) {
        ImageIcon ic = new ImageIcon(data);
        Image img = ic.getImage().getScaledInstance(240, -1, Image.SCALE_SMOOTH);
        JLabel lbl = new JLabel(new ImageIcon(img));
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        if (isMe) wrapper.add(lbl, BorderLayout.EAST);
        else wrapper.add(lbl, BorderLayout.WEST);
        return wrapper;
    }
}
