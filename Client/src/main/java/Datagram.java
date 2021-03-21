import exception.OpcodeException;
import utils.ComUtils;

import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class Datagram extends ComUtils {

    private Socket socket;
    private int id;

    public Datagram(Socket socket) throws IOException {
        super(socket);
    }

    /* TESTED */
    public String read_hello() throws IOException, OpcodeException {
        int writtenOpcode = readByte();
        String str;

        int requiredOpcode = 1;

        if (writtenOpcode == requiredOpcode) {
            id = readInt32();
            str = readString();
        } else {
            throw new OpcodeException(writtenOpcode, requiredOpcode);
        }
        return str;
    }

    /* TESTED */
    public void write_hello(int id, String str) throws IOException {
        writeByte(1); // OPCODE
        writeInt32(id); // ID
        writeString(str); //STRING
        writeByte(0); // END
    }

    /* TESTED */
    public byte[] read_hash() throws IOException, OpcodeException {
        int writtenOpcode = readByte();
        byte[] hashBytes;

        int requiredOpcode = 2;

        if (writtenOpcode == requiredOpcode) {
            hashBytes = readHash();
        } else {
            throw new OpcodeException(writtenOpcode, requiredOpcode);
        }

        return hashBytes;
    }

    /* TESTED */
    public void write_hash(String str) throws IOException {
        writeByte(2); // OPCODE
        writeHash(str); // HASH
    }

    /* TESTED */
    public String read_secret() throws IOException, OpcodeException {
        int requiredOpcode = 3;
        int writtenOpcode = readByte();
        String str;

        if (writtenOpcode == requiredOpcode) {
            str = readString();
        } else {
            throw new OpcodeException(writtenOpcode, requiredOpcode);
        }

        return str;
    }

    /* TESTED */
    public void write_secret(String str) throws IOException {
        writeByte(3);
        writeString(str);
        writeByte(0);
    }

    /* TESTED */
    public String read_insult() throws IOException, OpcodeException {
        int requiredOpcode = 4;
        int writtenOpcode = readByte();
        String str;

        if (writtenOpcode == requiredOpcode) {
            str = readString();
        } else {
            throw new OpcodeException(writtenOpcode, requiredOpcode);
        }

        return str;
    }

    /* TESTED */
    public void write_insult(String str) throws IOException {
        writeByte(4);
        writeString(str);
        writeByte(0);
    }

    /* TESTED */
    public String read_comeback() throws IOException, OpcodeException {
        int requiredOpcode = 5;
        int writtenOpcode = readByte();
        String str;

        if (writtenOpcode == requiredOpcode) {
            str = readString();
        } else {
            throw new OpcodeException(writtenOpcode, requiredOpcode);
        }

        return str;
    }

    /* TESTED */
    public void write_comeback(String str) throws IOException {
        writeByte(5);
        writeString(str);
        writeByte(0);
    }

    /* TESTED */
    public String read_shout() throws IOException, OpcodeException {
        int requiredOpcode = 6;
        int writtenOpcode = readByte();
        String str;

        if (writtenOpcode == requiredOpcode) {
            str = readString();
        } else {
            throw new OpcodeException(writtenOpcode, requiredOpcode);
        }

        return str;
    }

    /* TESTED */
    public void write_shout(String str) throws IOException {
        writeByte(6);
        writeString(str);
        writeByte(0);
    }

    /* TESTED */
    public String read_error() throws IOException, OpcodeException {
        int requiredOpcode = 7;
        int writtenOpcode = readByte();
        String str;

        if (writtenOpcode == requiredOpcode) {
            str = readString();
        } else {
            throw new OpcodeException(writtenOpcode, requiredOpcode);
        }

        return str;
    }

    /* TESTED */
    public void write_error(String str) throws IOException {
        writeByte(7);
        writeString(str);
        writeByte(0);
    }

    /* TESTED */
    public int getIdOpponent() {
        return this.id;
    }


    /* TESTED */
    public boolean proofHash(String secret, byte[] hash) {
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

    /* TESTED */
    public byte[] getHash(String str) {
        byte hashBytes[] = new byte[32];
        MessageDigest digest = null;

        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        byte[] encodedhash = digest.digest(
                str.getBytes(StandardCharsets.UTF_8));

        for (int i = 0; i < 32; i++) hashBytes[i] = encodedhash[i];

        return hashBytes;
    }

    /* TESTED */
    public boolean isEven(String s1, String s2) {
        int n1 = Integer.parseInt(s1);
        int n2 = Integer.parseInt(s2);
        return ((n1 + n2) % 2 == 0);
    }
}


