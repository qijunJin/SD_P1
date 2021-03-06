import java.io.*;
import java.security.MessageDigest;

public class Communication {
    private final int STRSIZE = 40;

    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;

    public Communication(InputStream inputStream, OutputStream outputStream) throws IOException {
        dataInputStream = new DataInputStream(inputStream);
        dataOutputStream = new DataOutputStream(outputStream);
    }

    /* OPCODE 1: HELLO */
    public String read_hello() throws IOException {
        int opcode = readByte();
        char cStr[] = new char[100];

        if (opcode == 1) {
            int pos = 0;
            do {
                byte b = readByte();
                if (b == 0) break;
                cStr[pos] = (char) b;
                pos++;
            } while (true);
        }
        return String.valueOf(cStr).trim();
    }

    public void write_hello(String str) throws IOException {
        int lenStr = str.length();
        int numBytes = lenStr + 2;

        byte bStr[] = new byte[numBytes];

        bStr[0] = (byte) 1;

        for (int i = 0; i < lenStr; i++)
            bStr[i + 1] = (byte) str.charAt(i);

        bStr[numBytes - 1] = (byte) 0;

        dataOutputStream.write(bStr, 0, numBytes);
    }

    /* OPCODE 2: HASH */
    public byte[] read_hash() throws IOException {
        int opcode = readByte();
        byte hashBytes[] = new byte[32];

        if (opcode == 2) {
            hashBytes = read_bytes(32);
        }
        return hashBytes;
    }

    public void write_hash(byte[] bytes) throws IOException {
        byte hashBytes[] = new byte[33];

        hashBytes[0] = (byte) 2;

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(bytes);
            for (int i = 0; i < 32; i++)
                hashBytes[i + 1] = encodedhash[i];
        } catch (Exception e) {
            e.printStackTrace();
        }

        dataOutputStream.write(hashBytes, 0, 33);
    }

    /* OPCODE 3: SECRET */
    public String read_secret() throws IOException {
        int opcode = readByte();
        char cStr[] = new char[100];

        if (opcode == 3) {
            int pos = 0;
            do {
                byte b = readByte();
                if (b == 0) break;
                cStr[pos] = (char) b;
                pos++;
            } while (true);
        }
        return String.valueOf(cStr).trim();
    }

    public void write_secret(String str) throws IOException {
        int lenStr = str.length();
        int numBytes = lenStr + 2;

        byte bStr[] = new byte[numBytes];

        bStr[0] = (byte) 3;

        for (int i = 0; i < lenStr; i++)
            bStr[i + 1] = (byte) str.charAt(i);

        bStr[numBytes - 1] = (byte) 0;

        dataOutputStream.write(bStr, 0, numBytes);
    }

    /* OPCODE 4: INSULT */
    public String read_insult() throws IOException {
        int opcode = readByte();
        char cStr[] = new char[100];

        if (opcode == 4) {
            int pos = 0;
            do {
                byte b = readByte();
                if (b == 0) break;
                cStr[pos] = (char) b;
                pos++;
            } while (true);
        }
        return String.valueOf(cStr).trim();
    }

    public void write_insult(String str) throws IOException {
        int lenStr = str.length();
        int numBytes = lenStr + 2;

        byte bStr[] = new byte[numBytes];

        bStr[0] = (byte) 4;

        for (int i = 0; i < lenStr; i++)
            bStr[i + 1] = (byte) str.charAt(i);

        bStr[numBytes - 1] = (byte) 0;

        dataOutputStream.write(bStr, 0, numBytes);
    }

    /* OPCODE 5: COMEBACK */
    public String read_comeback() throws IOException {
        int opcode = readByte();
        char cStr[] = new char[100];

        if (opcode == 5) {
            int pos = 0;
            do {
                byte b = readByte();
                if (b == 0) break;
                cStr[pos] = (char) b;
                pos++;
            } while (true);
        }
        return String.valueOf(cStr).trim();
    }

    public void write_comeback(String str) throws IOException {
        int lenStr = str.length();
        int numBytes = lenStr + 2;

        byte bStr[] = new byte[numBytes];

        bStr[0] = (byte) 5;

        for (int i = 0; i < lenStr; i++)
            bStr[i + 1] = (byte) str.charAt(i);

        bStr[numBytes - 1] = (byte) 0;

        dataOutputStream.write(bStr, 0, numBytes);
    }

    /* OPCODE 6: SHOUT */
    public String read_shout() throws IOException {
        int opcode = readByte();
        char cStr[] = new char[100];

        if (opcode == 6) {
            int pos = 0;
            do {
                byte b = readByte();
                if (b == 0) break;
                cStr[pos] = (char) b;
                pos++;
            } while (true);
        }
        return String.valueOf(cStr).trim();
    }

    public void write_shout(String str) throws IOException {
        int lenStr = str.length();
        int numBytes = lenStr + 2;

        byte bStr[] = new byte[numBytes];

        bStr[0] = (byte) 6;

        for (int i = 0; i < lenStr; i++)
            bStr[i + 1] = (byte) str.charAt(i);

        bStr[numBytes - 1] = (byte) 0;

        dataOutputStream.write(bStr, 0, numBytes);
    }

    /* OPCODE 7: ERROR */
    public String read_error() throws IOException {
        int opcode = readByte();
        char cStr[] = new char[100];

        if (opcode == 7) {
            int pos = 0;
            do {
                byte b = readByte();
                if (b == 0) break;
                cStr[pos] = (char) b;
                pos++;
            } while (true);
        }
        return String.valueOf(cStr).trim();
    }

    public void write_error(String str) throws IOException {
        int lenStr = str.length();
        int numBytes = lenStr + 2;

        byte bStr[] = new byte[numBytes];

        bStr[0] = (byte) 7;

        for (int i = 0; i < lenStr; i++)
            bStr[i + 1] = (byte) str.charAt(i);

        bStr[numBytes - 1] = (byte) 0;

        dataOutputStream.write(bStr, 0, numBytes);
    }

    private byte[] read_bytes(int numBytes) throws IOException {
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

    private byte readByte() throws IOException {
        return dataInputStream.readByte();
    }
}
