package utils;

import java.io.*;
import java.net.Socket;
import java.security.MessageDigest;

public class ComUtils {
    private final int STRSIZE = 40;

    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;

    public ComUtils(InputStream inputStream, OutputStream outputStream) throws IOException {
        dataInputStream = new DataInputStream(inputStream);
        dataOutputStream = new DataOutputStream(outputStream);
    }

    public ComUtils(Socket socket) throws IOException {
        dataInputStream = new DataInputStream(socket.getInputStream());
        dataOutputStream = new DataOutputStream(socket.getOutputStream());
    }

    public void writeByte(int i) throws IOException {
        byte bStr[] = new byte[1];
        bStr[0] = (byte) i;
        dataOutputStream.write(bStr, 0, 1);
    }

    public void writeString(String str) throws IOException {
        int lenStr = str.length();
        int numBytes = lenStr;

        byte bStr[] = new byte[numBytes];

        for (int i = 0; i < lenStr; i++)
            bStr[i] = (byte) str.charAt(i);

        dataOutputStream.write(bStr, 0, numBytes);
    }

    public byte[] readHash() throws IOException {
        byte hashBytes[] = new byte[32];

        hashBytes = read_bytes(32);

        return hashBytes;
    }

    public void writeHash(byte[] bytes) throws IOException {
        byte hashBytes[] = new byte[32];

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(bytes);
            for (int i = 0; i < 32; i++)
                hashBytes[i + 1] = encodedhash[i];
        } catch (Exception e) {
            e.printStackTrace();
        }

        dataOutputStream.write(hashBytes, 0, 32);
    }

    public String readString() throws IOException {
        char cStr[] = new char[100];
        int pos = 0;

        do {
            byte b = readByte();
            if (b == 0) break;
            cStr[pos] = (char) b;
            pos++;
        } while (true);

        return String.valueOf(cStr).trim();
    }


    /* Functions */
    protected byte[] read_bytes(int numBytes) throws IOException {
        int len = 0;
        byte bStr[] = new byte[numBytes];
        int bytesread = 0;
        do {
            bytesread = dataInputStream.read(bStr, len, numBytes - len);
            if (bytesread == -1)
                throw new IOException("Broken Pipe");
            len += bytesread;
        } while (len < numBytes);
        return bStr;
    }

    protected byte readByte() throws IOException {
        return dataInputStream.readByte();
    }

    public int read_int32() throws IOException {
        byte bytes[] = read_bytes(4);

        return bytesToInt32(bytes, Endianness.BIG_ENNDIAN);
    }

    public void write_int32(int number) throws IOException {
        byte bytes[] = int32ToBytes(number, Endianness.BIG_ENNDIAN);

        dataOutputStream.write(bytes, 0, 4);
    }

    protected byte[] int32ToBytes(int number, Endianness endianness) {
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
}
