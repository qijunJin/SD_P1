package utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * ComUtils class
 * Basic methods for communication between Client and Server.
 */
public class ComUtils {

    private final DataInputStream dataInputStream;
    private final DataOutputStream dataOutputStream;

    /**
     * Constructor of ComUtils.
     *
     * @param socket instance of Socket.
     * @throws IOException IOException.
     */
    public ComUtils(Socket socket) throws IOException {
        dataInputStream = new DataInputStream(socket.getInputStream());
        dataOutputStream = new DataOutputStream(socket.getOutputStream());
    }

    /**
     * Method that read one byte from Socket.
     *
     * @return one byte.
     * @throws IOException IOException.
     */
    public byte readByte() throws IOException {
        return this.readBytes(1)[0];
    }

    /**
     * Method that write one byte to Socket.
     *
     * @param i one byte.
     * @throws IOException IOException.
     */
    public void writeByte(int i) throws IOException {
        byte[] bytes = new byte[1];
        bytes[0] = (byte) i;
        dataOutputStream.write(bytes, 0, 1);
    }

    /**
     * Method that read one String from Socket.
     *
     * @return one String.
     * @throws IOException IOException.
     */
    public String readString() throws IOException {
        char[] cStr = new char[1];
        int pos = 0;
        do {
            char c = (char) readByte();
            if (c == 0) break;
            if (Character.getNumericValue(c) < 0) c = (char) (c & 0xff);
            cStr[pos] = c;
            pos++;
            cStr = Arrays.copyOf(cStr, cStr.length + 1);
        } while (true);
        return String.valueOf(cStr).trim();
    }

    /**
     * Method that write one String to Socket.
     *
     * @param str one String.
     * @throws IOException IOException.
     */
    public void writeString(String str) throws IOException {
        int lenStr = str.length();
        byte[] bStr = new byte[lenStr];
        for (int i = 0; i < lenStr; i++) bStr[i] = (byte) str.charAt(i);
        dataOutputStream.write(bStr, 0, lenStr);
    }

    /**
     * Method that read array of 32 bytes from Socket.
     *
     * @return array of 32 bytes.
     * @throws IOException IOException.
     */
    public byte[] readHash() throws IOException {
        return readBytes(32);
    }

    /**
     * Method that write array of 32 bytes of given String to Socket.
     *
     * @param str the given String.
     * @throws IOException IOException.
     */
    public void writeHash(String str) throws IOException {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        dataOutputStream.write(digest.digest(str.getBytes(StandardCharsets.UTF_8)), 0, 32);
    }

    /**
     * Method that write array of 32 bytes to Socket.
     *
     * @param bytes array of 32 bytes.
     * @throws IOException IOException.
     */
    public void writeHashArray(byte[] bytes) throws IOException {
        dataOutputStream.write(bytes, 0, 32);
    }

    /**
     * Method that read int of 32 bits from Socket.
     *
     * @return int of 32 bits.
     * @throws IOException IOException.
     */
    public int readInt32() throws IOException {
        return bytesToInt32(readBytes(4), Endianness.BIG_ENNDIAN);
    }

    /**
     * Method that write int of 32 bits to Socket.
     *
     * @param number int of 32 bits.
     * @throws IOException IOException.
     */
    public void writeInt32(int number) throws IOException {
        dataOutputStream.write(int32ToBytes(number, Endianness.BIG_ENNDIAN), 0, 4);
    }

    /**
     * Method that read byte by the given length number from Socket.
     *
     * @param numBytes the given length number.
     * @return the read bytes.
     * @throws IOException IOException.
     */
    private byte[] readBytes(int numBytes) throws IOException {
        int len = 0;
        byte[] bStr = new byte[numBytes];
        int bytesRead;
        do {
            bytesRead = dataInputStream.read(bStr, len, numBytes - len);
            if (bytesRead == -1)
                throw new IOException("Broken Pipe");
            len += bytesRead;
        } while (len < numBytes);
        return bStr;
    }

    /**
     * Method that convert int of 32 bits to 4 bytes.
     *
     * @param number     the given int of 32 bits.
     * @param endianness the Endianness of conversion.
     * @return array of 4 bytes.
     */
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

    /**
     * Method that convert 4 bytes to int of 32 bits.
     *
     * @param bytes      array of 4 bytes.
     * @param endianness the Endianness of conversion.
     * @return int of 32 bits.
     */
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
}

