package chatapp;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Server chat nhiều client (multi-threaded) hỗ trợ:
 *  - Đăng ký
 *  - Đăng nhập
 *  - Gửi/nhận tin nhắn
 *  - Danh sách bạn bè online/offline
 */
public class Server {
    private static final int PORT = 12345; // cổng server

    // Lưu thông tin user đã đăng ký
    static class UserRecord {
        String password;
        byte[] avatar;
        UserRecord(String p, byte[] a) {
            password = p;
            avatar = a;
        }
    }

    // "Database" user
    private final Map<String, UserRecord> users = new ConcurrentHashMap<>();
    // Danh sách user đang online
    private final Map<String, ClientHandler> online = new ConcurrentHashMap<>();

    public static void main(String[] args) throws Exception {
        new Server().start();
    }

    // Bắt đầu server
    public void start() throws Exception {
        ServerSocket ss = new ServerSocket(PORT);
        System.out.println("✅ Server lắng nghe tại cổng " + PORT);

        while (true) {
            Socket s = ss.accept();
            System.out.println("🔗 Kết nối mới từ " + s.getInetAddress());
            // xử lý đăng ký/đăng nhập trước
            new Thread(() -> handleClient(s)).start();
        }
    }

    /**
     * Xử lý client giai đoạn đầu (đăng ký / đăng nhập)
     */
    private void handleClient(Socket s) {
        try {
            ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(s.getInputStream());

            String username = null;
            boolean loggedIn = false;

            while (!loggedIn) {
                Object o = in.readObject();
                if (!(o instanceof Message)) continue;
                Message m = (Message) o;

                switch (m.getType()) {
                    case "REGISTER": {
                        String u = m.getSender();
                        Message.RegisterData rd = (Message.RegisterData) m.getContent();
                        if (u == null || rd == null || rd.password == null) {
                            out.writeObject(new Message("REGISTER_FAIL", "SERVER", u, "Dữ liệu không hợp lệ"));
                        } else if (users.containsKey(u)) {
                            out.writeObject(new Message("REGISTER_FAIL", "SERVER", u, "Tên tài khoản đã tồn tại"));
                        } else {
                            users.put(u, new UserRecord(rd.password, rd.avatarBytes));
                            out.writeObject(new Message("REGISTER_OK", "SERVER", u, "Đăng ký thành công"));
                            System.out.println("🆕 Người dùng đăng ký: " + u);
                        }
                        out.flush();
                        break;
                    
                  
                    }
                    default:
                       
                }
            }

        } catch (EOFException | SocketException eof) {
            System.out.println("⚠ Client ngắt kết nối trước khi đăng nhập.");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /** Gửi tin nhắn cho 1 user */
    private void sendTo(String user, Message m) {
        ClientHandler ch = online.get(user);
        if (ch != null) {
            ch.send(m);
        }
    }

    /** Gửi tin nhắn cho tất cả user online */
    private void broadcast(Message m) {
        for (ClientHandler ch : online.values()) {
            ch.send(m);
        }
    }

    /** Cập nhật danh sách user cho tất cả client */
    private void broadcastUserList() {
        ArrayList<Message.UserInfo> list = new ArrayList<>();
        for (Map.Entry<String, UserRecord> e : users.entrySet()) {
            String u = e.getKey();
            boolean on = online.containsKey(u);
            byte[] av = e.getValue().avatar;
            list.add(new Message.UserInfo(u, on, av));
        }
        broadcast(new Message("USER_LIST", "SERVER", null, list));
    }

    /** Xóa user khỏi danh sách online */
    private void removeOnline(String username) {
        online.remove(username);
        broadcastUserList();
        System.out.println("🚪 Người dùng thoát: " + username);
    }

    /**
     * Xử lý sau khi client đăng nhập thành công
     */
    class ClientHandler implements Runnable {
        final String username;
        final Socket socket;
        final ObjectInputStream in;
        final ObjectOutputStream out;

        ClientHandler(String username, Socket socket, ObjectInputStream in, ObjectOutputStream out) {
            this.username = username;
            this.socket = socket;
            this.in = in;
            this.out = out;
        }

        @Override
        public void run() {
            try {
                while (true) {
                    Object o = in.readObject();
                    if (!(o instanceof Message)) continue;
                    Message m = (Message) o;

                    switch (m.getType()) {
                        case "TEXT":
                        case "IMAGE":
                        case "FILE": {
                            if (m.getTarget() == null) broadcast(m);
                            else {
                                sendTo(m.getTarget(), m);
                                if (!m.getSender().equals(m.getTarget())) sendTo(m.getSender(), m);
                            }
                            break;
                        }
                        case "TYPING": {
                            if (m.getTarget() != null) sendTo(m.getTarget(), m);
                            else broadcast(m);
                            break;
                        }
                        case "LOGOUT": {
                            removeOnline(username);
                            socket.close();
                            return;
                        }
                        default:
                            System.out.println("⚠ Loại tin nhắn không hỗ trợ: " + m.getType());
                    }
                }
            } catch (EOFException | SocketException eof) {
                System.out.println("⚠ Client mất kết nối: " + username);
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                removeOnline(username);
            }
        }

        void send(Message m) {
            try {
                out.writeObject(m);
                out.flush();
            } catch (IOException ignored) {}
        }
    }
}
