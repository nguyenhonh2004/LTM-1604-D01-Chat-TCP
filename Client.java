package chatapp;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.*;
import java.awt.image.BufferedImage;

/**
 * Client UI + networking.
 */
public class Client extends JFrame {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 12345;

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String username;

    // UI
    private DefaultListModel<Message.UserInfo> userListModel = new DefaultListModel<>();
    private JList<Message.UserInfo> userJList;
    private JPanel chatPanel;
    private JScrollPane chatScroll;
    private JTextField inputField;
    private JButton sendBtn, fileBtn, imgBtn, emojiBtn;
    private JLabel profileAvatar;
    private JLabel profileName;
    private JLabel typingLabel;
    private String currentTarget = null;

    // typing
    private javax.swing.Timer typingTimer;
    private boolean isTypingSent = false;

    public Client(String username) {
        this.username = username;

        initUI();
        setSize(1100, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        connectToServer(); // kết nối server + startListening
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // LEFT: users
        userJList = new JList<>(userListModel);
        userJList.setCellRenderer(new UserCellRenderer());
        JScrollPane leftScroll = new JScrollPane(userJList);
        leftScroll.setPreferredSize(new Dimension(260, 0));
        userJList.addListSelectionListener(e -> {
            Message.UserInfo sel = userJList.getSelectedValue();
            if (sel != null) {
                currentTarget = sel.username;
                profileName.setText(sel.username + (sel.online ? " (Online)" : " (Offline)"));
                profileAvatar.setIcon(toAvatarIcon(sel.avatar, 140));
            }
        });

        // CENTER: chat panel
        chatPanel = new JPanel();
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        chatPanel.setBackground(Color.WHITE);
        chatScroll = new JScrollPane(chatPanel);
        chatScroll.getVerticalScrollBar().setUnitIncrement(16);

        // RIGHT: profile
        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setBorder(new EmptyBorder(16,16,16,16));
        profileAvatar = new JLabel(defaultAvatarIcon(140));
        profileName = new JLabel(username + " (You)");
        profileName.setFont(profileName.getFont().deriveFont(Font.BOLD, 16f));
        profileName.setAlignmentX(Component.CENTER_ALIGNMENT);
        typingLabel = new JLabel("");
        typingLabel.setForeground(Color.GRAY);
        right.add(profileAvatar);
        right.add(Box.createRigidArea(new Dimension(0,10)));
        right.add(profileName);
        right.add(Box.createRigidArea(new Dimension(0,6)));
        right.add(typingLabel);
        right.setPreferredSize(new Dimension(260,0));

        // BOTTOM: input
        JPanel bottom = new JPanel(new BorderLayout(8,8));
        bottom.setBorder(new EmptyBorder(8,8,8,8));
        inputField = new JTextField();
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT,6,0));
        emojiBtn = new JButton("😊");
        imgBtn = new JButton("🖼");
        fileBtn = new JButton("📎");
        sendBtn = new JButton("Send");
        controls.add(emojiBtn); controls.add(imgBtn); controls.add(fileBtn); controls.add(sendBtn);
        bottom.add(inputField, BorderLayout.CENTER);
        bottom.add(controls, BorderLayout.EAST);

        add(leftScroll, BorderLayout.WEST);
        add(chatScroll, BorderLayout.CENTER);
        add(right, BorderLayout.EAST);
        add(bottom, BorderLayout.SOUTH);

        // actions
        sendBtn.addActionListener(e -> sendText());
        inputField.addActionListener(e -> sendText());
        emojiBtn.addActionListener(e -> openEmojiPicker());
        imgBtn.addActionListener(e -> sendImage());
        fileBtn.addActionListener(e -> sendFile());

        // typing indicator
        inputField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            void changed(){ startTyping(); }
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e){ changed(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e){ changed(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e){ changed(); }
        });

        typingTimer = new javax.swing.Timer(1500, ev -> sendMessage("TYPING", "STOP"));
        typingTimer.setRepeats(false);
    }

    private void connectToServer() {
        try {
            socket = new Socket(SERVER_HOST, SERVER_PORT);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            // gửi login lên server
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
            e.printStackTrace();
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

    private void handleMessage(Message m) {
        switch (m.getType()) {
            case "LOGIN_OK":
                showSystem("Server: " + m.getContent());
                // Thêm user hiện tại vào list an toàn
                Message.UserInfo me = new Message.UserInfo();
                me.username = username;
                me.avatar = null;
                me.online = true;
                SwingUtilities.invokeLater(() -> userListModel.addElement(me));
                break;
            case "LOGIN_FAIL":
            case "REGISTER_FAIL":
                JOptionPane.showMessageDialog(this, (String)m.getContent(), "Error", JOptionPane.ERROR_MESSAGE);
                break;
            case "REGISTER_OK":
                JOptionPane.showMessageDialog(this, "Registered. Now login.", "Info", JOptionPane.INFORMATION_MESSAGE);
                break;
            case "TEXT":
                String txt = (String)m.getContent();
                boolean mine = Objects.equals(m.getSender(), username);
                addBubble(mine? "Me" : m.getSender(), txt, mine);
                break;
            case "USER_LIST":
                if (m.getContent() instanceof ArrayList) {
                    ArrayList<?> raw = (ArrayList<?>) m.getContent();
                    SwingUtilities.invokeLater(() -> {
                        userListModel.clear();
                        for (Object o : raw) {
                            if (o instanceof Message.UserInfo)
                                userListModel.addElement((Message.UserInfo)o);
                        }
                    });
                }
                break;

            case "IMAGE":
                if (m.getContent() instanceof String) {
                    ReceivedFile rf = ReceivedFile.fromString((String)m.getContent());
                    boolean meFlag = Objects.equals(m.getSender(), username);
                    addImageBubble(m.getSender(), rf.getFilename(), rf.getData(), meFlag);
                }
                break;
            case "FILE":
                if (m.getContent() instanceof String) {
                    ReceivedFile rf = ReceivedFile.fromString((String)m.getContent());
                    int r = JOptionPane.showConfirmDialog(this, "Receive file '" + rf.getFilename() + "' from " + m.getSender() + "? Save?", "File Receive", JOptionPane.YES_NO_OPTION);
                    if (r == JOptionPane.YES_OPTION) {
                        JFileChooser chooser = new JFileChooser();
                        chooser.setSelectedFile(new File(rf.getFilename()));
                        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                            try (FileOutputStream fos = new FileOutputStream(chooser.getSelectedFile())) {
                                fos.write(rf.getData());
                                showSystem("Saved file to " + chooser.getSelectedFile().getAbsolutePath());
                            } catch (IOException ex) { showSystem("Save fail."); }
                        }
                    }
                }
                break;
            case "TYPING":
                if ("START".equals(m.getContent())) typingLabel.setText(m.getSender() + " is typing...");
                else typingLabel.setText(" ");
                break;
            case "ERROR":
                JOptionPane.showMessageDialog(this, (String)m.getContent(), "Server Error", JOptionPane.ERROR_MESSAGE);
                break;
            default:
                // bỏ qua các message chưa xử lý
                break;
        }
    }

    private void startTyping(){
        if (!isTypingSent) {
            sendMessage("TYPING", "START");
            isTypingSent=true;
        }
        typingTimer.restart();
    }

    private void sendText() {
        String t = inputField.getText().trim();
        if (t.isEmpty()) return;
       
        sendMessage("TEXT", t);
        addBubble("Me", t, true);
        inputField.setText("");
        sendMessage("TYPING", "STOP");
        isTypingSent = false;
    }

    private void openEmojiPicker(){
        EmojiPicker p = new EmojiPicker(this, s -> inputField.setText(inputField.getText()+s));
        p.setVisible(true);
    }

    private void sendImage(){
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Image","png","jpg","jpeg","gif"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File f = chooser.getSelectedFile();
        try {
            byte[] data = java.nio.file.Files.readAllBytes(f.toPath());
            String payload = new ReceivedFile(f.getName(), data).encodeToString();
            sendMessage("IMAGE", payload);
            addImageBubble("Me", f.getName(), data, true);
        } catch (Exception ex) { showSystem("Image send failed."); ex.printStackTrace(); }
    }

    private void sendFile(){
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File f = chooser.getSelectedFile();
        try {
            byte[] data = java.nio.file.Files.readAllBytes(f.toPath());
            String payload = new ReceivedFile(f.getName(), data).encodeToString();
            sendMessage("FILE", payload);
            showSystem("Sent file: " + f.getName());
        } catch (Exception ex) { showSystem("File send failed."); ex.printStackTrace(); }
    }

    private void addBubble(String who, String text, boolean mine){
        JPanel p = ChatBubble.createTextBubble(text, mine);
        SwingUtilities.invokeLater(() -> {
            chatPanel.add(Box.createVerticalStrut(8));
            chatPanel.add(p);
            chatPanel.revalidate();
            chatScroll.getVerticalScrollBar().setValue(chatScroll.getVerticalScrollBar().getMaximum());
        });
    }

    private void addImageBubble(String who, String filename, byte[] data, boolean mine){
        JPanel p = ChatBubble.createImageBubble(data, filename, mine);
        SwingUtilities.invokeLater(() -> {
            chatPanel.add(Box.createVerticalStrut(8));
            chatPanel.add(p);
            chatPanel.revalidate();
            chatScroll.getVerticalScrollBar().setValue(chatScroll.getVerticalScrollBar().getMaximum());
        });
    }

    private void showSystem(String s){
        SwingUtilities.invokeLater(() -> {
            JLabel lbl = new JLabel(s, SwingConstants.CENTER);
            lbl.setForeground(Color.DARK_GRAY);
            JPanel wrap = new JPanel(new BorderLayout()); wrap.setOpaque(false); wrap.add(lbl, BorderLayout.CENTER);
            chatPanel.add(Box.createVerticalStrut(6)); chatPanel.add(wrap);
            chatPanel.revalidate();
            chatScroll.getVerticalScrollBar().setValue(chatScroll.getVerticalScrollBar().getMaximum());
        });
    }

    // UI helpers
    private ImageIcon defaultAvatarIcon(int size){
        BufferedImage bi = new BufferedImage(size,size,BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bi.createGraphics();
        g.setColor(new Color(235,235,235)); g.fillOval(0,0,size,size);
        g.setColor(new Color(160,160,160)); g.setFont(g.getFont().deriveFont(Font.BOLD, size/6f));
        g.drawString("U", size/2 - size/10, size/2 + size/12);
        g.dispose(); return new ImageIcon(bi);
    }

    private ImageIcon toAvatarIcon(byte[] data, int size){
        if (data==null) return defaultAvatarIcon(size);
        ImageIcon ic = new ImageIcon(data); Image im = ic.getImage().getScaledInstance(size,size,Image.SCALE_SMOOTH);
        BufferedImage bi = new BufferedImage(size,size,BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bi.createGraphics(); g.setClip(new java.awt.geom.Ellipse2D.Float(0,0,size,size)); g.drawImage(im,0,0,null); g.dispose();
        return new ImageIcon(bi);
    }

    public static void main(String[] args){
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }

    static class UserCellRenderer extends JPanel implements ListCellRenderer<Message.UserInfo> {
        private final JLabel lblAvatar = new JLabel();
        private final JLabel lblName = new JLabel();
        public UserCellRenderer(){
            setLayout(new BorderLayout(8,6));
            add(lblAvatar, BorderLayout.WEST); add(lblName, BorderLayout.CENTER);
            setBorder(new EmptyBorder(6,8,6,8));
        }
        @Override
        public Component getListCellRendererComponent(JList<? extends Message.UserInfo> list, Message.UserInfo value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value.avatar != null) {
                ImageIcon ic = new ImageIcon(value.avatar);
                Image im = ic.getImage().getScaledInstance(48,48,Image.SCALE_SMOOTH);
                lblAvatar.setIcon(new ImageIcon(im));
            } else lblAvatar.setIcon(new ImageIcon(new BufferedImage(48,48,BufferedImage.TYPE_INT_ARGB)));
            lblName.setText(value.username + (value.online ? " (Online)" : " (Offline)"));
            setBackground(isSelected ? new Color(230,240,255) : Color.WHITE);
            setOpaque(true);
            return this;
        }
    }
}
