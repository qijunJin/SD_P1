package utils;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class ComUtils {

    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private Socket socket;
    public ComUtils(InputStream inputStream, OutputStream outputStream) throws IOException {
        dataInputStream = new DataInputStream(inputStream);
        dataOutputStream = new DataOutputStream(outputStream);
    }

    public ComUtils(Socket socket) throws IOException {
        dataInputStream = new DataInputStream(socket.getInputStream());
        dataOutputStream = new DataOutputStream(socket.getOutputStream());
        this.socket = socket;
    }

    /* BYTE */
    public byte readByte() throws IOException {
        return this.readBytes(1)[0];
        // return dataInputStream.readByte();
    }

    public void writeByte(int i) throws IOException {
        byte bytes[] = new byte[1];
        bytes[0] = (byte) i;
        dataOutputStream.write(bytes, 0, 1);
    }

    /* STRING */
    public String readString() throws IOException {
        char cStr[] = new char[1];
        int pos = 0;

        do {
            byte b = readByte();
            if (b == 0) break;
            int unsignedByte = b;
            if (b < 0) {
                unsignedByte = b + 256;
            }
            cStr[pos] = (char) unsignedByte;
            pos++;
            cStr = Arrays.copyOf(cStr, cStr.length + 1);
        } while (true);

        return String.valueOf(cStr).trim();
    }

    public void writeString(String str) throws IOException {
        int lenStr = str.length();
        int numBytes = lenStr;

        byte bStr[] = new byte[numBytes];

        for (int i = 0; i < lenStr; i++)
            bStr[i] = (byte) str.charAt(i);

        dataOutputStream.write(bStr, 0, numBytes);
    }

    /* HASH */
    public byte[] readHash() throws IOException {
        return readBytes(32);
    }

    public void writeHash(String str) throws IOException {
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

        dataOutputStream.write(hashBytes, 0, 32);
    }

    public void writeHash2(byte[] encodedhash) throws IOException {

        dataOutputStream.write(encodedhash, 0, 32);

    }

    /* INT32 */
    public int readInt32() throws IOException {
        byte bytes[] = readBytes(4);

        return bytesToInt32(bytes, Endianness.BIG_ENNDIAN);
    }

    public void writeInt32(int number) throws IOException {
        byte bytes[] = int32ToBytes(number, Endianness.BIG_ENNDIAN);

        dataOutputStream.write(bytes, 0, 4);
    }

    /* Private Functions */
    private byte[] readBytes(int numBytes) throws IOException {
        int len = 0;
        byte bStr[] = new byte[numBytes];
        int bytesRead;
        do {
            bytesRead = dataInputStream.read(bStr, len, numBytes - len);
            if (bytesRead == -1)
                throw new IOException("Broken Pipe");
            len += bytesRead;
        } while (len < numBytes);
        return bStr;
    }

    private byte[] int32ToBytes(int number, Endianness endianness) {
        byte[] bytes = new byte[4];

        if (Endianness.BIG_ENNDIAN == endianness) {
            bytes[0] = (byte) ((number >> 24) & 0xFF);
            bytes[1] = (byte) ((number >> 16) & 0xFF);
            bytes[2] = (byte) ((number >> 8) & 0xFF);
            bytes[3] = (byte) (number & 0xFF);
        } else {
            bytes[0] = (byte) (number & 0xFF);
            bytes[1] = (byte) ((number >> 8) & 0xFF);
            bytes[2] = (byte) ((number >> 16) & 0xFF);
            bytes[3] = (byte) ((number >> 24) & 0xFF);
        }

        return bytes;
    }

    private int bytesToInt32(byte bytes[], Endianness endianness) {
        int number;

        if (Endianness.BIG_ENNDIAN == endianness) {
            number = ((bytes[0] & 0xFF) << 24) | ((bytes[1] & 0xFF) << 16) |
                    ((bytes[2] & 0xFF) << 8) | (bytes[3] & 0xFF);
        } else {
            number = (bytes[0] & 0xFF) | ((bytes[1] & 0xFF) << 8) |
                    ((bytes[2] & 0xFF) << 16) | ((bytes[3] & 0xFF) << 24);
        }
        return number;
    }

    public Socket getSocket() {
        return socket;
    }
}