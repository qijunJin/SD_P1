import utils.ComUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class Datagram extends ComUtils {

    private Socket socket;
    private int id;

    public Datagram(InputStream inputStream, OutputStream outputStream) throws IOException {
        super(inputStream, outputStream);
    }

    public Datagram(Socket socket) throws IOException {
        super(socket);
    }

    public boolean isEven(String s1, String s2) {
        int n1 = Integer.parseInt(s1);
        int n2 = Integer.parseInt(s2);
        return ((n1 + n2) % 2 == 0);
    }

    public int getIdOponent() {
        return this.id;
    }

    public boolean proofHash(String secret, byte[] hash) {

        // FOR TEST
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        byte[] encodedhash = digest.digest(
                secret.getBytes(StandardCharsets.UTF_8));

        return Arrays.equals(encodedhash, hash);

    }


    /* OPCODE 1: HELLO */
    public String read_hello() throws IOException {
        int opcode = readByte();
        String str = "";

        if (opcode == 1) {
            id = read_int32();
            str = readString();
        }
        return str;
    }

    public void write_hello(int id, String str) throws IOException {
        writeByte(1); // OPCODE
        write_int32(id); // ID
        writeString(str); //STRING
        writeByte(0); // END
    }

    /* OPCODE 2: HASH */
    public byte[] readHash() throws IOException {
        int opcode = readByte();
        byte hashBytes[] = new byte[32];

        if (opcode == 2) {
            hashBytes = read_hash();
        }

        return hashBytes;
    }

    public void writeHash(String str) throws IOException {
        writeByte(2); // OPCODE
        write_hash(str); // HASH
    }

    public void writeHash(byte[] bytes) throws IOException {
        writeByte(2); // OPCODE
        write_hash(bytes); // HASH
    }


    /* OPCODE 3: SECRET */
    public String read_secret() throws IOException {
        int opcode = readByte();
        String str = "";

        if (opcode == 3) {
            str = readString();
        }

        return str;
    }

    public void write_secret(String str) throws IOException {
        writeByte(3);
        writeString(str);
        writeByte(0);
    }


    /* OPCODE 4: INSULT */
    public String read_insult() throws IOException {
        int opcode = readByte();
        String str = "";

        if (opcode == 4) {
            str = readString();
        }

        return str;
    }

    public void write_insult(String str) throws IOException {
        writeByte(4);
        writeString(str);
        writeByte(0);
    }

    /* OPCODE 5: COMEBACK */
    public String read_comeback() throws IOException {
        int opcode = readByte();
        String str = "";

        if (opcode == 5) {
            str = readString();
        }

        return str;
    }

    public void write_comeback(String str) throws IOException {
        writeByte(5);
        writeString(str);
        writeByte(0);
    }

    /* OPCODE 6: SHOUT */
    public String read_shout() throws IOException {
        int opcode = readByte();
        String str = "";

        if (opcode == 6) {
            str = readString();
        }

        return str;
    }

    public void write_shout(String str) throws IOException {
        writeByte(6);
        writeString(str);
        writeByte(0);
    }

    /* OPCODE 7: ERROR */
    public String read_error() throws IOException {
        int opcode = readByte();
        String str = "";

        if (opcode == 7) {
            str = readString();
        }

        return str;
    }

    public void write_error(String str) throws IOException {
        writeByte(7);
        writeString(str);
        writeByte(0);
    }


}


