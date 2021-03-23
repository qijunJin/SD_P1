import exception.OpcodeException;
import utils.ComUtils;

import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class Datagram extends ComUtils {

    private int id;

    public Datagram(Socket socket) throws IOException {
        super(socket);
    }

    /* READER */
    public int read_opcode() throws IOException {
        return this.readByte();
    }

    public String read_hello(int opcode) throws IOException, OpcodeException {
        int writtenOpcode = opcode;
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

    public byte[] read_hash(int opcode) throws IOException, OpcodeException {
        int writtenOpcode = opcode;
        byte[] hashBytes;

        int requiredOpcode = 2;

        if (writtenOpcode != requiredOpcode) {
            throw new OpcodeException(writtenOpcode, requiredOpcode);
        } else {
            hashBytes = readHash();
        }

        return hashBytes;
    }

    public String read_secret(int opcode) throws IOException, OpcodeException {
        int writtenOpcode = opcode;
        String str;

        int requiredOpcode = 3;

        if (writtenOpcode == requiredOpcode) {
            str = readString();
        } else {
            throw new OpcodeException(writtenOpcode, requiredOpcode);
        }

        return str;
    }

    public String read_insult(int opcode) throws IOException, OpcodeException {
        int writtenOpcode = opcode;
        String str;

        int requiredOpcode = 4;

        if (writtenOpcode == requiredOpcode) {
            str = readString();
        } else {
            throw new OpcodeException(writtenOpcode, requiredOpcode);
        }

        return str;
    }

    public String read_comeback(int opcode) throws IOException, OpcodeException {
        int writtenOpcode = opcode;
        String str;

        int requiredOpcode = 5;

        if (writtenOpcode == requiredOpcode) {
            str = readString();
        } else {
            throw new OpcodeException(writtenOpcode, requiredOpcode);
        }

        return str;
    }

    public String read_shout(int opcode) throws IOException, OpcodeException {
        int writtenOpcode = opcode;
        String str;

        int requiredOpcode = 6;

        if (writtenOpcode == requiredOpcode) {
            str = readString();
        } else {
            throw new OpcodeException(writtenOpcode, requiredOpcode);
        }

        return str;
    }

    public String read_error(int opcode) throws IOException, OpcodeException {
        int writtenOpcode = opcode;
        String str;

        int requiredOpcode = 7;

        if (writtenOpcode == requiredOpcode) {
            str = readString();
        } else {
            throw new OpcodeException(writtenOpcode, requiredOpcode);
        }

        return str;
    }

    /* WRITER */
    public void write_hello(int id, String str) throws IOException {
        writeByte(1); // OPCODE
        writeInt32(id); // ID
        writeString(str); //STRING
        writeByte(0); // END
    }

    public void write_hash(String str) throws IOException {
        writeByte(2); // OPCODE
        writeHash(str); // HASH
    }

    public void write_hash_array(byte[] bytes) throws IOException {
        writeByte(2); // OPCODE
        writeHashArray(bytes); // HASH
    }

    public void write_secret(String str) throws IOException {
        writeByte(3);
        writeString(str);
        writeByte(0);
    }

    public void write_insult(String str) throws IOException {
        writeByte(4);
        writeString(str);
        writeByte(0);
    }

    public void write_comeback(String str) throws IOException {
        writeByte(5);
        writeString(str);
        writeByte(0);
    }

    public void write_shout(String str) throws IOException {
        writeByte(6);
        writeString(str);
        writeByte(0);
    }

    public void write_error(String str) throws IOException {
        writeByte(7);
        writeString(str);
        writeByte(0);
    }

    public int getIdOpponent() {
        return this.id;
    }

    public boolean proofHash(String secret, byte[] hash) {

        byte[] encodedhash = new byte[0];
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            encodedhash = digest.digest(
                    secret.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }


        return Arrays.equals(encodedhash, hash);

    }

    public byte[] getHash(String str){
        byte hashBytes[] = new byte[32];
        MessageDigest digest = null;

        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        byte[] encodedhash = digest.digest(
                str.getBytes(StandardCharsets.UTF_8));

        for (int i = 0; i < 32; i++)
            hashBytes[i] = encodedhash[i];

        return hashBytes;
    }

}


