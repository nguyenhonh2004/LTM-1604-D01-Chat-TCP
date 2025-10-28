package chatapp;

import java.io.Serializable;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    private String type;   // TEXT, IMAGE, FILE, LOGIN, REGISTER, LOGIN_OK, REGISTER_OK, USER_LIST...
    private String sender;
    private String target; // null => broadcast
    private Object content;

    public Message(String type, String sender, String target, Object content) {
        this.type = type;
        this.sender = sender;
        this.target = target;
        this.content = content;
    }

    public String getType()   { return type; }
    public String getSender() { return sender; }
    public String getTarget() { return target; }
    public Object getContent(){ return content; }

    // ---- RegisterData ----
    public static class RegisterData implements Serializable {
        private static final long serialVersionUID = 1L;
        public String password;
        public String avatarFilename;
        public byte[] avatarBytes;

        public RegisterData(String password, String avatarFilename, byte[] avatarBytes) {
            this.password = password;
            this.avatarFilename = avatarFilename;
            this.avatarBytes = avatarBytes;
        }
    }

    // ---- UserInfo ----
    public static class UserInfo implements Serializable {
        private static final long serialVersionUID = 1L;

        public String username;
        public boolean online;
        public byte[] avatar;

        // Constructor mặc định
        public UserInfo() {}

        // Constructor đầy đủ
        public UserInfo(String username, boolean online, byte[] avatar) {
            this.username = username;
            this.online = online;
            this.avatar = avatar;
        }

        @Override
        public String toString() {
            return username + (online ? " (Online)" : " (Offline)");
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof UserInfo)) return false;
            UserInfo other = (UserInfo) obj;
            return username != null && username.equals(other.username);
        }

        @Override
        public int hashCode() {
            return username != null ? username.hashCode() : 0;
        }
    }
}
