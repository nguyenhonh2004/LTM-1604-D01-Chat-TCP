package chatapp;

import java.io.Serializable;
import java.util.Base64;

public class ReceivedFile implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String filename;
    private final byte[] data;

    public ReceivedFile(String filename, byte[] data){ this.filename = filename; this.data = data; }
    public String getFilename(){ return filename; }
    public byte[] getData(){ return data; }

    public String encodeToString(){
        return filename + "::" + Base64.getEncoder().encodeToString(data);
    }

    public static ReceivedFile fromString(String s){
        if (s==null) return null;
        int idx = s.indexOf("::");
        if (idx < 0) return null;
        String name = s.substring(0, idx);
        byte[] d = Base64.getDecoder().decode(s.substring(idx+2));
        return new ReceivedFile(name, d);
    }
}
