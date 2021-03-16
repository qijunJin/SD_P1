


import exception.EmptyHashException;
import exception.OpcodeException;
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

    /* OPCODE 1: HELLO */
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


    public void write_hello(int id, String str) throws IOException {
        writeByte(1); // OPCODE
        writeInt32(id); // ID
        writeString(str); //STRING
        writeByte(0); // END
    }

    /* OPCODE 2: HASH */
    public byte[] read_hash() throws IOException, OpcodeException, EmptyHashException {
        int writtenOpcode = readByte();
        byte[] hashBytes;

        int requiredOpcode = 2;

        if (writtenOpcode != requiredOpcode) {
            throw new OpcodeException(writtenOpcode, requiredOpcode);
        } else {
            try {
                hashBytes = readHash();
            } catch (Exception e) {
                throw new EmptyHashException();
            }


            /*System.out.println("Hash len: " + hashBytes.length);
            for (int i = 0; i < hashBytes.length; i++) {
                System.out.println(hashBytes[i]);
            }*/
        }

        return hashBytes;
    }

    public void write_hash(String str) throws IOException {
        writeByte(2); // OPCODE
        writeHash(str); // HASH
    }


    /* OPCODE 3: SECRET */
    public String read_secret() throws IOException, OpcodeException {
        int writtenOpcode = readByte();
        String str;

        int requiredOpcode = 3;

        if (writtenOpcode == requiredOpcode) {
            str = readString();
        } else {
            throw new OpcodeException(writtenOpcode, requiredOpcode);
        }

        return str;
    }

    public void write_secret(String str) throws IOException {
        writeByte(3);
        writeString(str);
        writeByte(0);
    }


    /* OPCODE 4: INSULT */
    public String read_insult() throws IOException, OpcodeException {
        int writtenOpcode = readByte();
        String str;

        int requiredOpcode = 4;

        if (writtenOpcode == requiredOpcode) {
            str = readString();
        } else {
            throw new OpcodeException(writtenOpcode, requiredOpcode);
        }

        return str;
    }

    public void write_insult(String str) throws IOException {
        writeByte(4);
        writeString(str);
        writeByte(0);
    }

    /* OPCODE 5: COMEBACK */
    public String read_comeback() throws IOException, OpcodeException {
        int writtenOpcode = readByte();
        String str;

        int requiredOpcode = 5;

        if (writtenOpcode == requiredOpcode) {
            str = readString();
        } else {
            throw new OpcodeException(writtenOpcode, requiredOpcode);
        }

        return str;
    }

    public void write_comeback(String str) throws IOException {
        writeByte(5);
        writeString(str);
        writeByte(0);
    }

    /* OPCODE 6: SHOUT */
    public String read_shout() throws IOException, OpcodeException {
        int writtenOpcode = readByte();
        String str;

        int requiredOpcode = 6;

        if (writtenOpcode == requiredOpcode) {
            str = readString();
        } else {
            throw new OpcodeException(writtenOpcode, requiredOpcode);
        }

        return str;
    }

    public void write_shout(String str) throws IOException {
        writeByte(6);
        writeString(str);
        writeByte(0);
    }

    /* OPCODE 7: ERROR */
    public String read_error() throws IOException, OpcodeException {
        int writtenOpcode = readByte();
        String str;

        int requiredOpcode = 7;

        if (writtenOpcode == requiredOpcode) {
            str = readString();
        } else {
            throw new OpcodeException(writtenOpcode, requiredOpcode);
        }

        return str;
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


