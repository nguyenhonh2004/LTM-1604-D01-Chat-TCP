package chatapp;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Method;
import java.net.*;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server extends JFrame {
    private static final int SERVER_PORT = 12346;

    private ServerSocket serverSocket;
    private final java.util.List<ClientHandler> clients = new CopyOnWriteArrayList<>();
    private final java.util.Map<String, Message.UserInfo> userDB = new java.util.HashMap<>();

    // UI components
    private DefaultListModel<Message.UserInfo> userListModel = new DefaultListModel<>();
    private JList<Message.UserInfo> userJList;
    private JPanel chatPanel;
    private JScrollPane chatScroll;
    private JTextField inputField;
    private JButton sendBtn, fileBtn, imgBtn, emojiBtn;
    private JLabel profileAvatar, profileName;
    private String currentTarget = null;

    public Server() {
        setTitle("Server Chat");
        setSize(1100, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initUI();
        setVisible(true);
        startServer();
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

        // LEFT: user list panel with darker blue-purple gradient
        JPanel leftPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c1 = new Color(150, 180, 220); // nhạt hơn màu cũ
                Color c2 = new Color(190, 160, 210); // nhạt hơn màu cũ
                GradientPaint gp = new GradientPaint(0, 0, c1, 0, getHeight(), c2);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());

                g2.dispose();
            }
        };
        leftPanel.setPreferredSize(new Dimension(260, 0));
        leftPanel.setBorder(new EmptyBorder(8, 8, 8, 8));

        userJList = new JList<>(userListModel);
        userJList.setCellRenderer(new UserCellRenderer("Server"));
        userJList.setOpaque(false);
        JScrollPane leftScroll = new JScrollPane(userJList);
        leftScroll.setOpaque(false);
        leftScroll.getViewport().setOpaque(false);
        leftScroll.setBorder(BorderFactory.createEmptyBorder());
        leftPanel.add(leftScroll, BorderLayout.CENTER);

        userJList.addListSelectionListener(e -> {
            Message.UserInfo sel = userJList.getSelectedValue();
            if (sel != null && !"Server".equals(sel.username)) currentTarget = sel.username;
            else currentTarget = null;
            updateProfilePanel(sel);
        });

        add(leftPanel, BorderLayout.WEST);

        // CENTER: chat panel with softer gradient
        chatPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c1 = new Color(190, 220, 255); // light blue
                Color c2 = new Color(235, 200, 240); // soft pink-lavender
                GradientPaint gp = new GradientPaint(0, 0, c1, 0, getHeight(), c2);
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
        chatScroll.getViewport().setOpaque(false);
        add(chatScroll, BorderLayout.CENTER);

        // RIGHT: profile panel with same darker gradient as left (visually balanced)
        JPanel rightPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c1 = new Color(220, 240, 255);   // nhạt hơn
                Color c2 = new Color(245, 220, 250);   // nhạt hơn

                GradientPaint gp = new GradientPaint(0, 0, c2, 0, getHeight(), c1); // inverse direction
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBorder(new EmptyBorder(16, 16, 16, 16));
        rightPanel.setPreferredSize(new Dimension(260, 0));

        profileAvatar = new JLabel(defaultAvatarCircle(true));
        profileAvatar.setAlignmentX(Component.CENTER_ALIGNMENT);

        profileName = new JLabel("<html><span style='color:white;font-weight:bold'>Server (Online)</span></html>");
        profileName.setAlignmentX(Component.CENTER_ALIGNMENT);
        profileName.setBorder(new EmptyBorder(8, 0, 0, 0));

        rightPanel.add(Box.createVerticalGlue());
        rightPanel.add(profileAvatar);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        rightPanel.add(profileName);
        rightPanel.add(Box.createVerticalGlue());

        add(rightPanel, BorderLayout.EAST);

        // BOTTOM: input panel with subtle purple background
        JPanel bottom = new JPanel(new BorderLayout(8, 8));
        bottom.setBorder(new EmptyBorder(10, 10, 10, 10));
        bottom.setBackground(new Color(230, 220, 240)); // light lavender

        inputField = new JTextField();
        inputField.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));


        inputField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(150, 120, 200), 2, true),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));


        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
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


        bottom.add(inputField, BorderLayout.CENTER);
        bottom.add(controls, BorderLayout.EAST);

        add(bottom, BorderLayout.SOUTH);

        // Actions
        sendBtn.addActionListener(e -> sendText());
        inputField.addActionListener(e -> sendText());
        emojiBtn.addActionListener(e -> openEmojiPicker());
        imgBtn.addActionListener(e -> sendImage());
        fileBtn.addActionListener(e -> sendFile());
    }

    private void updateProfilePanel(Message.UserInfo sel) {
        if (sel == null) {
            profileName.setText("<html><span style='color:white;font-weight:bold'>Server (Online)</span></html>");
            profileAvatar.setIcon(defaultAvatarCircle(true));
        } else {
            profileName.setText("<html><span style='color:white;font-weight:bold'>" + sel.username + (sel.online ? " (Online)" : " (Offline)") + "</span></html>");
            profileAvatar.setIcon(toAvatarIcon(sel.avatar, 120, sel.online));

        }
    }

    private void sendText() {
        if (currentTarget == null || "Server".equals(currentTarget)) {
            JOptionPane.showMessageDialog(this, "Chọn 1 user bên trái để gửi.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String text = inputField.getText().trim();
        if (text.isEmpty()) return;

        // show on server UI
        // ===== SỬA LỖI: Lời gọi này có 3 tham số, khớp với hàm `addBubble` đã sửa =====
        addBubble("Server → " + currentTarget, text, true);

        // send to user
        Message m = new Message("TEXT", "Server", currentTarget, text);
        sendToUser(currentTarget, m);

        inputField.setText("");
    }

    private void sendToUser(String username, Message m) {
        for (ClientHandler ch : clients) {
            if (ch.username != null && ch.username.equals(username)) ch.sendMessage(m);
        }
    }

    private void sendImage() {
        if (currentTarget == null) {
            JOptionPane.showMessageDialog(this, "Chọn 1 user để gửi ảnh.");
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Image", "png", "jpg", "jpeg", "gif"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File f = chooser.getSelectedFile();
        try {
            byte[] data = java.nio.file.Files.readAllBytes(f.toPath());
            String payload = new ReceivedFile(f.getName(), data).encodeToString();
            Message m = new Message("IMAGE", "Server", currentTarget, payload);
            sendToUser(currentTarget, m);
            
            // ===== SỬA LỖI: Đảo vị trí data và f.getName() =====
            // Chữ ký mới: (String name, byte[] data, String filename, boolean mine)
            addImageBubble("Server", data, f.getName(), true);
        } catch (Exception ex) {
            ex.printStackTrace();
            showSystem("Image send failed: " + ex.getMessage());
        }
    }

    private void sendFile() {
        if (currentTarget == null) {
            JOptionPane.showMessageDialog(this, "Chọn 1 user để gửi file.");
            return;
        }
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File f = chooser.getSelectedFile();
        try {
            byte[] data = java.nio.file.Files.readAllBytes(f.toPath());
            String payload = new ReceivedFile(f.getName(), data).encodeToString();
            Message m = new Message("FILE", "Server", currentTarget, payload);
            sendToUser(currentTarget, m);
            // Vẫn dùng addFileBubble cũ vì nó có logic lưu file
            addFileBubble("Server", f.getName(), data, true);
        } catch (Exception ex) {
            ex.printStackTrace();
            showSystem("File send failed: " + ex.getMessage());
        }
    }

    private void openEmojiPicker() {
        EmojiPicker p = new EmojiPicker(this, s -> inputField.setText(inputField.getText() + s));
        p.setVisible(true);
    }

    private void appendSystem(String s) {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        JLabel lbl = new JLabel(s, SwingConstants.CENTER);
        lbl.setForeground(new Color(40, 40, 40));
        wrap.add(lbl, BorderLayout.CENTER);
        SwingUtilities.invokeLater(() -> {
            chatPanel.add(Box.createVerticalStrut(6));
            chatPanel.add(wrap);
            chatPanel.revalidate();
            chatScroll.getVerticalScrollBar().setValue(chatScroll.getVerticalScrollBar().getMaximum());
        });
    }

    // ===== SỬA LỖI: Bỏ tham số 'who' không cần thiết =====
    private void addBubble(String name, String text, boolean mine) {
        // 'name' ở đây là tên người gửi (vd: "Ly" hoặc "Server -> Ly")
        // 'text' là nội dung
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
                // ===== SỬA LỖI: Dùng 'name' để tìm avatar (thay vì 'who') =====
                if (u.username.equals(name)) { 
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


    private void addFileBubble(String from, String fileName, byte[] data, boolean right) {
        // Giữ nguyên hàm này vì nó chứa logic download file,
        // ChatBubble.createFileBubble không có
        SwingUtilities.invokeLater(() -> {
            JPanel bubble = new JPanel(new FlowLayout(right ? FlowLayout.RIGHT : FlowLayout.LEFT));
            bubble.setBackground(new Color(250, 250, 250));
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
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Cannot open file: " + ex.getMessage());
                }
            });
            bubble.add(label);
            bubble.add(btn);
            JPanel wrapper = new JPanel(new BorderLayout());
            wrapper.setOpaque(false);
            wrapper.add(bubble, right ? BorderLayout.EAST : BorderLayout.WEST);
            chatPanel.add(wrapper);
            chatPanel.revalidate();
            chatPanel.repaint();
            chatScroll.getVerticalScrollBar().setValue(chatScroll.getVerticalScrollBar().getMaximum());
        });
    }

    // ===== SỬA LỖI: Sửa chữ ký hàm cho đúng (String name, byte[] data, String filename, boolean mine) =====
    private void addImageBubble(String name, byte[] data, String filename, boolean mine) {
        // Lời gọi này đã khớp với ChatBubble.java
        JPanel p = ChatBubble.createImageBubble(name, data, filename, mine);
        SwingUtilities.invokeLater(() -> {
            chatPanel.add(Box.createVerticalStrut(8));
            chatPanel.add(p);
            chatPanel.revalidate();
            chatScroll.getVerticalScrollBar().setValue(chatScroll.getVerticalScrollBar().getMaximum());
        });
    }

    private void showSystem(String s) {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        JLabel lbl = new JLabel(s, SwingConstants.CENTER);
        lbl.setForeground(Color.DARK_GRAY);
        wrap.add(lbl, BorderLayout.CENTER);
        SwingUtilities.invokeLater(() -> {
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
        int size = 64;
        BufferedImage bi = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bi.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(235, 235, 235));
        g.fillOval(0, 0, size, size);
        g.setStroke(new BasicStroke(3));
        g.setColor(online ? Color.GREEN : Color.RED);
        g.drawOval(2, 2, size - 4, size - 4);
        g.dispose();
        return new ImageIcon(bi);
    }

    // ---------- Server logic ----------
    private void startServer() {
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(SERVER_PORT);
                SwingUtilities.invokeLater(() -> appendSystem("Server started on port " + SERVER_PORT));

                // Add server as user
                Message.UserInfo me = new Message.UserInfo();
                me.username = "Server";
                me.online = true;
                me.avatar = null;
                SwingUtilities.invokeLater(() -> userListModel.addElement(me));

                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    ClientHandler handler = new ClientHandler(clientSocket);
                    clients.add(handler);
                    new Thread(handler).start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void broadcastUserList() {
        ArrayList<Message.UserInfo> list = new ArrayList<>();
        for (int i = 0; i < userListModel.size(); i++) list.add(userListModel.get(i));
        Message m = new Message("USER_LIST", "Server", null, list);
        for (ClientHandler ch : clients) ch.sendMessage(m);
    }

    // ---------- Client Handler ----------
    class ClientHandler implements Runnable {
        private Socket socket;
        private ObjectOutputStream out;
        private ObjectInputStream in;
        private String username;

        public ClientHandler(Socket s) {
            this.socket = s;
        }

        public void sendMessage(Message m) {
            try {
                if (out != null) {
                    out.writeObject(m);
                    out.flush();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());

                while (true) {
                    Object obj = in.readObject();
                    if (!(obj instanceof Message)) continue;
                    Message m = (Message) obj;

                    switch (m.getType()) {
                        case "REGISTER": {
                            Message.RegisterData reg = (Message.RegisterData) m.getContent();
                            String regUsername = m.getSender();

                            Message.UserInfo newUser = new Message.UserInfo();
                            newUser.username = regUsername;
                            newUser.online = true;
                            newUser.avatar = reg.avatarBytes;

                            userDB.put(regUsername, newUser);

                            SwingUtilities.invokeLater(() -> {
                                if (!userListModel.contains(newUser)) userListModel.addElement(newUser);
                            });

                            out.writeObject(new Message("REGISTER_OK", "Server", regUsername, "Success"));
                            broadcastUserList();
                            break;
                        }

                        case "LOGIN": {
                            username = m.getSender();

                            Message.UserInfo uinfo = userDB.get(username);
                            if (uinfo == null) {
                                uinfo = new Message.UserInfo();
                                uinfo.username = username;
                                uinfo.online = true;
                                uinfo.avatar = null;
                                userDB.put(username, uinfo);
                            } else uinfo.online = true;

                            final Message.UserInfo ref = uinfo;
                            SwingUtilities.invokeLater(() -> {
                                if (!userListModel.contains(ref)) userListModel.addElement(ref);
                                else {
                                    // refresh status
                                    for (int i = 0; i < userListModel.size(); i++) {
                                        Message.UserInfo uu = userListModel.get(i);
                                        if (uu.username.equals(ref.username)) {
                                            uu.online = true;
                                            userListModel.set(i, uu);
                                            userJList.repaint();
                                            break;
                                        }
                                    }
                                }
                            });

                            ArrayList<Message.UserInfo> list = new ArrayList<>(userDB.values());
                            out.writeObject(new Message("USER_LIST", "Server", username, list));
                            broadcastUserList();
                            break;
                        }

                        case "TEXT": {
                            // show on server UI and forward to intended recipient (reflection-based)
                            // ===== SỬA LỖI: Lời gọi 3 tham số, đã đúng =====
                            addBubble(m.getSender(), (String) m.getContent(), false);
                            String rec = getRecipient(m);
                            broadcastToUser(rec, m);
                            break;
                        }

                        case "IMAGE": {
                            ReceivedFile rfImg = ReceivedFile.fromString((String) m.getContent());
                            
                            // ===== SỬA LỖI: Đảo vị trí rfImg.getData() và rfImg.getFilename() =====
                            addImageBubble(m.getSender(), rfImg.getData(), rfImg.getFilename(), false);
                            String recImg = getRecipient(m);
                            broadcastToUser(recImg, m);
                            break;
                        }

                        case "FILE": {
                            ReceivedFile rfFile = ReceivedFile.fromString((String) m.getContent());
                            // Vẫn dùng addFileBubble cũ
                            SwingUtilities.invokeLater(() -> addFileBubble(m.getSender(), rfFile.getFilename(), rfFile.getData(), false));
                            String recFile = getRecipient(m);
                            broadcastToUser(recFile, m);
                            break;
                        }

                        case "AVATAR_UPDATE": {
                            if (m.getContent() instanceof byte[] bytes) {
                                // update DB and UI
                                String user = m.getSender();
                                if (userDB.containsKey(user)) userDB.get(user).avatar = bytes;
                                SwingUtilities.invokeLater(() -> {
                                    for (int i = 0; i < userListModel.size(); i++) {
                                        Message.UserInfo u = userListModel.get(i);
                                        if (u.username.equals(user)) {
                                            u.avatar = bytes;
                                            userListModel.set(i, u);
                                            userJList.repaint();
                                            break;
                                        }
                                    }
                                });
                                broadcastUserList();
                            }
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (username != null) {
                    SwingUtilities.invokeLater(() -> {
                        for (int i = 0; i < userListModel.size(); i++) {
                            Message.UserInfo u = userListModel.get(i);
                            if (u.username.equals(username)) {
                                u.online = false;
                                userListModel.set(i, u);
                                userJList.repaint();
                                break;
                            }
                        }
                    });
                }
                clients.remove(this);
                try { if (socket != null && !socket.isClosed()) socket.close(); } catch (IOException ignored) {}
                broadcastUserList();
            }
        }
    }

    // Try several possible getter names by reflection to obtain recipient from Message
    private String getRecipient(Message m) {
        if (m == null) return null;
        String[] names = {"getReceiver", "getTarget", "getTo", "getRecipient", "getReceiverUsername", "getDest"};
        for (String nm : names) {
            try {
                Method method = m.getClass().getMethod(nm);
                Object res = method.invoke(m);
                if (res instanceof String s) return s;
            } catch (NoSuchMethodException ignored) {
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        // fallback: try getReceiver field directly
        try {
            var fld = m.getClass().getDeclaredField("receiver");
            fld.setAccessible(true);
            Object val = fld.get(m);
            if (val instanceof String s) return s;
        } catch (Exception ignored) {}
        // As final fallback, return sender (so message won't get lost)
        try {
            return m.getSender();
        } catch (Exception ignored) {
            return null;
        }
    }

    private void broadcastToUser(String username, Message m) {
        if (username == null) return;
        for (ClientHandler ch : clients) {
            if (ch.username != null && ch.username.equals(username)) {
                ch.sendMessage(m);
            }
        }
    }

    // ---------- User cell renderer ----------
    static class UserCellRenderer extends JPanel implements ListCellRenderer<Message.UserInfo> {
        private JLabel lblAvatar = new JLabel();
        private JLabel lblName = new JLabel();
        private String currentUser;

        public UserCellRenderer(String currentUser) {
            this.currentUser = currentUser;
            setLayout(new BorderLayout(8, 6));
            add(lblAvatar, BorderLayout.WEST);
            add(lblName, BorderLayout.CENTER);
            setBorder(new EmptyBorder(8, 8, 8, 8));
            setOpaque(false);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Message.UserInfo> list,
                                                      Message.UserInfo value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            // avatar
        	lblAvatar.setIcon(Server.toAvatarIcon(value.avatar, 48, value.online));

            lblName.setText("<html><span style='color:white;'>" + value.username + (value.online ? " (Online)" : " (Offline)") + "</span></html>");

            // highlight selected slightly
            if (isSelected) setBackground(new Color(100, 70, 160, 120));
            else setBackground(new Color(0, 0, 0, 0)); // transparent so gradient shows
            setOpaque(isSelected);
            return this;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Server::new);
    }
}