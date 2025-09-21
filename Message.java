package chatapp;

import java.io.Serializable;
import java.util.ArrayList;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    private String type; // TEXT, IMAGE, FILE, LOGIN, REGISTER, LOGIN_OK, LOGIN_FAIL, REGISTER_OK, REGISTER_FAIL, USER_LIST, TYPING, LOGOUT
    private String sender;
    private String target; // null => broadcast
    private Object content;

    public Message(String type, String sender, String target, Object content) {
        this.type = type; this.sender = sender; this.target = target; this.content = content;
    }
    public String getType(){ return type; }
    public String getSender(){ return sender; }
    public String getTarget(){ return target; }
    public Object getContent(){ return content; }

    public static class RegisterData implements Serializable {
        private static final long serialVersionUID = 1L;
        public String password;
        public String avatarFilename;
        public byte[] avatarBytes;
        public RegisterData(String password, String avatarFilename, byte[] avatarBytes){
            this.password = password; this.avatarFilename = avatarFilename; this.avatarBytes = avatarBytes;
        }
    }

    public static class UserInfo implements Serializable {
        public String username;
        public boolean online;
        public byte[] avatar;

        // Constructor mặc định
        public UserInfo() { }

        // Constructor đầy đủ
        public UserInfo(String username, boolean online, byte[] avatar) {
            this.username = username;
            this.online = online;
            this.avatar = avatar;
        }
    }

}
