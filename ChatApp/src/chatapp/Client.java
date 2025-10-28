package chatapp;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class Client extends JFrame {

    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 12346;

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String username;

    private DefaultListModel<Message.UserInfo> userListModel = new DefaultListModel<>();
    private JList<Message.UserInfo> userJList;
    private JPanel chatPanel;
    private JScrollPane chatScroll;
    private JTextField inputField;
    private JButton sendBtn, fileBtn, imgBtn, emojiBtn;
    private JLabel profileAvatar, profileName, typingLabel;
    private String currentTarget = null;
    private javax.swing.Timer typingTimer;
    private boolean isTypingSent = false;

    public Client(String username) {
        this.username = username;
        setTitle("Lovely Chat - " + username);
        setSize(1100, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initUI();
        connectToServer();
        
        
    }
   
 // hàm resize icon (đặt trong class, ví dụ ChatPanel hoặc MainUI)
    private ImageIcon resizeIcon(String path, int width, int height) {
        ImageIcon icon = new ImageIcon(path);
        Image img = icon.getImage();
        Image scaled = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    
    
    private void initUI() {
        setLayout(new BorderLayout());

        // ===== LEFT PANEL =====
        JPanel leftPanel = new JPanel(new BorderLayout()) {
            int[][] stars = new int[50][3]; // ngôi sao
            {
                for (int i = 0; i < stars.length; i++) {
                    stars[i][0] = (int) (Math.random() * 260);
                    stars[i][1] = (int) (Math.random() * 700);
                    stars[i][2] = (int) (Math.random() * 3 + 1);
                }
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gp = new GradientPaint(0, 0, new Color(180, 220, 255),
                        0, getHeight(), new Color(255, 180, 220));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());

                g2.setColor(new Color(255, 255, 255, 180));
                for (int[] s : stars) g2.fillOval(s[0], s[1], s[2], s[2]);
            }
        };
        leftPanel.setPreferredSize(new Dimension(260, 0));

        userJList = new JList<>(userListModel);
        userJList.setCellRenderer(new UserCellRenderer(username));
        JScrollPane leftScroll = new JScrollPane(userJList);
        leftScroll.setOpaque(false);
        leftScroll.getViewport().setOpaque(false);
        leftPanel.add(leftScroll, BorderLayout.CENTER);

        userJList.addListSelectionListener(e -> updateProfile());

        // ===== CENTER PANEL =====
        chatPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(220, 240, 255),
                        0, getHeight(), new Color(255, 180, 220));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        chatPanel.setOpaque(false);
        chatScroll = new JScrollPane(chatPanel);
        chatScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        chatScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        chatScroll.getVerticalScrollBar().setUnitIncrement(16);
        chatScroll.setBorder(BorderFactory.createEmptyBorder());

        // ===== RIGHT PANEL =====
        JPanel rightPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(180, 220, 255),
                        0, getHeight(), new Color(255, 180, 220));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBorder(new EmptyBorder(16, 16, 16, 16));
        rightPanel.setPreferredSize(new Dimension(260, 0));

        profileAvatar = new JLabel(defaultAvatarCircle(true));
        profileAvatar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        profileAvatar.setAlignmentX(Component.CENTER_ALIGNMENT);
        profileAvatar.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) { changeAvatar(); }
        });

        profileName = new JLabel("<html>" + username + " (You) <font color='green'>(Online)</font></html>");
        profileName.setFont(profileName.getFont().deriveFont(Font.BOLD, 16f));
        profileName.setAlignmentX(Component.CENTER_ALIGNMENT);

        typingLabel = new JLabel("");
        typingLabel.setForeground(Color.GRAY);
        typingLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        rightPanel.add(profileAvatar);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        rightPanel.add(profileName);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 6)));
        rightPanel.add(typingLabel);

        // ===== BOTTOM PANEL =====
        JPanel bottomPanel = new JPanel(new BorderLayout(8, 8));
        bottomPanel.setBorder(new EmptyBorder(8, 8, 8, 8));
        bottomPanel.setBackground(new Color(255, 240, 245));

        inputField = new JTextField();
        inputField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.PINK, 2),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        inputField.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));

        // Panel chứa các nút
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 2));
        controls.setOpaque(false);

        // emoji button
        emojiBtn = new JButton(resizeIcon("icons/emoji.png", 20, 20));
        emojiBtn.setToolTipText("Chèn emoji");
        emojiBtn.setFocusPainted(false);
        emojiBtn.setContentAreaFilled(false);
        emojiBtn.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
        emojiBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // image button
        imgBtn = new JButton(resizeIcon("icons/image.png", 20, 20));
        imgBtn.setToolTipText("Gửi ảnh");
        imgBtn.setFocusPainted(false);
        imgBtn.setContentAreaFilled(false);
        imgBtn.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
        imgBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // file button
        fileBtn = new JButton(resizeIcon("icons/file.png", 20, 20));
        fileBtn.setToolTipText("Đính kèm tệp");
        fileBtn.setFocusPainted(false);
        fileBtn.setContentAreaFilled(false);
        fileBtn.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
        fileBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // send button (gọn, bo tròn nhẹ, tím pastel)
        sendBtn = new JButton("", resizeIcon("icons/send.png", 16, 16));
        sendBtn.setForeground(Color.WHITE);
        sendBtn.setFocusPainted(false);
        sendBtn.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        sendBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        sendBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        sendBtn.setOpaque(true);
        

        controls.add(emojiBtn);
        controls.add(imgBtn);
        controls.add(fileBtn);
        controls.add(sendBtn);

        bottomPanel.add(inputField, BorderLayout.CENTER);
        bottomPanel.add(controls, BorderLayout.EAST);

        // ===== ADD TO FRAME =====
        add(leftPanel, BorderLayout.WEST);
        add(chatScroll, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);

        // ===== ACTIONS =====
        sendBtn.addActionListener(e -> sendText());
        inputField.addActionListener(e -> sendText());
        emojiBtn.addActionListener(e -> openEmojiPicker());
        imgBtn.addActionListener(e -> sendImage());
        fileBtn.addActionListener(e -> sendFile());

        inputField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            void changed() { startTyping(); }
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { changed(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { changed(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { changed(); }
        });
        typingTimer = new javax.swing.Timer(1500, ev -> sendMessage("TYPING", "STOP"));
        typingTimer.setRepeats(false);
    }

    private void updateProfile() {
        Message.UserInfo sel = userJList.getSelectedValue();
        if(sel != null) {
            currentTarget = sel.username;
            String status = sel.online ? "<font color='green'>(Online)</font>" : "<font color='red'>(Offline)</font>";
            String you = sel.username.equals(username) ? " (You) " : " ";
            profileName.setText("<html>"+sel.username+you+status+"</html>");
            profileAvatar.setIcon(toAvatarIcon(sel.avatar,140,sel.online));
        }
    }

    // đổi avatar
    private void changeAvatar() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Images", "png", "jpg", "jpeg", "gif"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File f = chooser.getSelectedFile();
        try {
            byte[] data = java.nio.file.Files.readAllBytes(f.toPath());
            profileAvatar.setIcon(toAvatarIcon(data, 140, true));
            profileAvatar.repaint();

            // Update user list
            for (int i = 0; i < userListModel.size(); i++) {
                Message.UserInfo u = userListModel.get(i);
                if (u.username.equals(username)) { u.avatar = data; break; }
            }
            userJList.repaint();

            // Send avatar update to server
            sendMessage("AVATAR_UPDATE", data);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Cannot load image: " + ex.getMessage());
        }
    }

    // connect server
    private void connectToServer() {
        try {
            socket = new Socket(SERVER_HOST, SERVER_PORT);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            // reset GUI trước khi login
            userListModel.clear();
            chatPanel.removeAll();
            chatPanel.revalidate();
            chatPanel.repaint();
            profileAvatar.setIcon(defaultAvatarCircle(true));
            profileName.setText("<html>" + username + " (You) <font color='green'>(Online)</font></html>");

            sendMessage("LOGIN", username);
            startListening();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Cannot connect to server.", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void sendMessage(String type, Object content) {
        if (out == null) return;
        try {
            Message m = new Message(type, username, currentTarget, content);
            out.writeObject(m);
            out.flush();
        } catch (IOException e) {
            showSystem("Send failed.");
        }
    }

    private void startListening() {
        new Thread(() -> {
            try {
                while (true) {
                    Object obj = in.readObject();
                    if (!(obj instanceof Message)) continue;
                    handleMessage((Message) obj);
                }
            } catch (Exception e) {
                showSystem("Disconnected from server.");
            }
        }).start();
    }

    // handle message từ server
    private void handleMessage(Message m) {
        switch (m.getType()) {
            case "LOGIN_OK":
                showSystem("Connected as " + username);
                Message.UserInfo me = new Message.UserInfo();
                me.username = username;
                me.avatar = null;
                me.online = true;
                if (!userListModel.contains(me)) userListModel.addElement(me);
                break;
            case "TEXT":
                boolean mine = m.getSender().equals(username);
                addBubble(m.getSender(), mine ? "Me" : m.getSender(), (String) m.getContent(), mine);
                break;
            case "USER_LIST":
                if (m.getContent() instanceof ArrayList<?> raw) {
                    SwingUtilities.invokeLater(() -> {
                        userListModel.clear();
                        for (Object o : raw) {
                            if (o instanceof Message.UserInfo u && !"Server".equals(u.username))
                                userListModel.addElement(u);
                        }
                    });
                }
                break;
            case "TYPING":
                if ("START".equals(m.getContent())) typingLabel.setText(m.getSender() + " is typing...");
                else typingLabel.setText(" ");
                break;
            case "FILE":
            case "IMAGE":
                if (m.getContent() instanceof String s) {
                    ReceivedFile rf = ReceivedFile.fromString(s);
                    boolean isMine = m.getSender().equals(username);
                    if ("FILE".equals(m.getType()))
                        addFileBubble(m.getSender(), rf.getFilename(), rf.getData(), isMine);
                    else
                        addImageBubble(m.getSender(), isMine ? "Me" : m.getSender(), rf.getFilename(), rf.getData(), isMine);
                }
                break;
            case "AVATAR_UPDATE":
                if (m.getContent() instanceof byte[] data) {
                    for (int i = 0; i < userListModel.size(); i++) {
                        Message.UserInfo u = userListModel.get(i);
                        if (u.username.equals(m.getSender())) u.avatar = data;
                    }
                    userJList.repaint();
                }
                break;
        }
    }

    private void startTyping() {
        if (!isTypingSent) { sendMessage("TYPING", "START"); isTypingSent = true; }
        typingTimer.restart();
    }

    private void sendText() {
        String t = inputField.getText().trim();
        if (t.isEmpty()) return;
        sendMessage("TEXT", t);
        inputField.setText("");
        sendMessage("TYPING", "STOP");
        isTypingSent = false;
    }

    private void openEmojiPicker() {
        EmojiPicker p = new EmojiPicker(this, s -> inputField.setText(inputField.getText() + s));
        p.setVisible(true);
    }

    private void sendImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Image", "png", "jpg", "jpeg", "gif"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File f = chooser.getSelectedFile();
        try {
            byte[] data = java.nio.file.Files.readAllBytes(f.toPath());
            String payload = new ReceivedFile(f.getName(), data).encodeToString();
            sendMessage("IMAGE", payload);
        } catch (Exception ex) { showSystem("Image send failed."); }
    }

    private void sendFile() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File f = chooser.getSelectedFile();
        try {
            byte[] data = java.nio.file.Files.readAllBytes(f.toPath());
            String payload = new ReceivedFile(f.getName(), data).encodeToString();
            sendMessage("FILE", payload);
            showSystem("Sent file: " + f.getName());
        } catch (Exception ex) { showSystem("File send failed."); }
    }

    private void addBubble(String who, String name, String text, boolean mine) {
        JPanel bubble = ChatBubble.createTextBubble(name, text, mine);
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);

        if (mine) {
            // chỉ hiển thị bubble bên phải, không có avatar nhỏ
            wrapper.add(bubble, BorderLayout.EAST);
        } else {
            // hiển thị avatar của người khác + bubble bên trái
            ImageIcon avatarIcon = defaultAvatarCircle(true);
            for (int i = 0; i < userListModel.size(); i++) {
                Message.UserInfo u = userListModel.get(i);
                if (u.username.equals(who)) { 
                    avatarIcon = toAvatarIcon(u.avatar, 32, u.online); 
                    break; 
                }
            }
            JLabel avatarLbl = new JLabel(avatarIcon);
            avatarLbl.setBorder(new EmptyBorder(0, 4, 0, 4));
            wrapper.add(avatarLbl, BorderLayout.WEST);
            wrapper.add(bubble, BorderLayout.CENTER);
        }

        SwingUtilities.invokeLater(() -> {
            chatPanel.add(Box.createVerticalStrut(8));
            chatPanel.add(wrapper);
            chatPanel.revalidate();
            chatScroll.getVerticalScrollBar().setValue(chatScroll.getVerticalScrollBar().getMaximum());
        });
    }

    // chat file
    private void addFileBubble(String from, String fileName, byte[] data, boolean mine) {
        JPanel bubble = new JPanel(new FlowLayout(mine ? FlowLayout.RIGHT : FlowLayout.LEFT));
        bubble.setBackground(new Color(245, 245, 245));
        bubble.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        JLabel label = new JLabel(from + " sent file: ");
        JButton btn = new JButton(fileName);
        btn.addActionListener(e -> {
            try {
                File dir = new File(System.getProperty("user.home"), "Downloads");
                if (!dir.exists()) dir.mkdirs();
                File saved = new File(dir, fileName);
                java.nio.file.Files.write(saved.toPath(), data);
                if (Desktop.isDesktopSupported()) Desktop.getDesktop().open(saved);
                else JOptionPane.showMessageDialog(this, "Saved at " + saved.getAbsolutePath());
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Cannot open file"); }
        });
        bubble.add(label); bubble.add(btn);
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(bubble, mine ? BorderLayout.EAST : BorderLayout.WEST);

        SwingUtilities.invokeLater(() -> {
            chatPanel.add(Box.createVerticalStrut(6));
            chatPanel.add(wrapper);
            chatPanel.revalidate();
            chatPanel.repaint();
            chatScroll.getVerticalScrollBar().setValue(chatScroll.getVerticalScrollBar().getMaximum());
        });
    }

    // chat image
    private void addImageBubble(String who, String name, String filename, byte[] data, boolean mine) {
        JPanel imageBubble = ChatBubble.createImageBubble(name, data, filename, mine);
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);

        if (!mine) { // chỉ hiển thị avatar của người khác
            ImageIcon avatarIcon = defaultAvatarCircle(true);
            for (int i = 0; i < userListModel.size(); i++) {
                Message.UserInfo u = userListModel.get(i);
                if (u.username.equals(who)) { avatarIcon = toAvatarIcon(u.avatar, 32, u.online); break; }
            }
            JLabel avatarLbl = new JLabel(avatarIcon);
            avatarLbl.setBorder(new EmptyBorder(0, 4, 0, 4));
            wrapper.add(avatarLbl, BorderLayout.WEST);
            wrapper.add(imageBubble, BorderLayout.CENTER);
        } else {
            wrapper.add(imageBubble, BorderLayout.EAST);
        }

        SwingUtilities.invokeLater(() -> {
            chatPanel.add(Box.createVerticalStrut(8));
            chatPanel.add(wrapper);
            chatPanel.revalidate();
            chatScroll.getVerticalScrollBar().setValue(chatScroll.getVerticalScrollBar().getMaximum());
        });
    }


    private void showSystem(String s) {
        if (chatPanel == null || chatScroll == null) return;
        SwingUtilities.invokeLater(() -> {
            JLabel lbl = new JLabel(s, SwingConstants.CENTER);
            lbl.setForeground(Color.DARK_GRAY);
            JPanel wrap = new JPanel(new BorderLayout());
            wrap.setOpaque(false);
            wrap.add(lbl, BorderLayout.CENTER);
            chatPanel.add(Box.createVerticalStrut(6));
            chatPanel.add(wrap);
            chatPanel.revalidate();
            chatScroll.getVerticalScrollBar().setValue(chatScroll.getVerticalScrollBar().getMaximum());
        });
    }

    // avatar helper
    public static ImageIcon toAvatarIcon(byte[] data, int size, boolean online) {
        BufferedImage bi = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bi.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(235, 235, 235)); g.fillOval(0, 0, size, size);
        if (data != null) {
            try {
                BufferedImage img = javax.imageio.ImageIO.read(new ByteArrayInputStream(data));
                Image scaled = img.getScaledInstance(size, size, Image.SCALE_SMOOTH);
                g.setClip(new java.awt.geom.Ellipse2D.Float(0, 0, size, size));
                g.drawImage(scaled, 0, 0, null);
                g.setClip(null);
            } catch (Exception e) { e.printStackTrace(); }
        }
        g.setStroke(new BasicStroke(3));
        g.setColor(online ? Color.GREEN : Color.RED);
        g.drawOval(1, 1, size - 2, size - 2);
        g.dispose();
        return new ImageIcon(bi);
    }

    private static ImageIcon defaultAvatarCircle(boolean online) {
        int size = 48;
        BufferedImage bi = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bi.createGraphics();
        g.setColor(new Color(235, 235, 235));
        g.fillOval(0, 0, size, size);
        g.setStroke(new BasicStroke(3));
        g.setColor(online ? Color.GREEN : Color.RED);
        g.drawOval(1, 1, size - 2, size - 2);
        g.dispose();
        return new ImageIcon(bi);
    }

    // user list renderer
    static class UserCellRenderer extends JPanel implements ListCellRenderer<Message.UserInfo> {
        private final JLabel lblAvatar = new JLabel();
        private final JLabel lblName = new JLabel();
        private final String currentUser;
        public UserCellRenderer(String currentUser) { this.currentUser = currentUser; setLayout(new BorderLayout(8, 6)); add(lblAvatar, BorderLayout.WEST); add(lblName, BorderLayout.CENTER); setBorder(new EmptyBorder(6,8,6,8)); }
        @Override
        public Component getListCellRendererComponent(JList<? extends Message.UserInfo> list,
                                                      Message.UserInfo value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            lblAvatar.setIcon(Client.toAvatarIcon(value.avatar, 48, value.online));
            String status = value.online ? "<font color='green'>(Online)</font>" : "<font color='red'>(Offline)</font>";
            String you = value.username.equals(currentUser) ? " (You) " : " ";
            lblName.setText("<html>" + value.username + you + status + "</html>");
            setBackground(isSelected ? new Color(255, 192, 203) : new Color(255, 228, 241));
            setOpaque(true);
            return this;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}