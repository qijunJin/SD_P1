
import java.io.*;
import java.math.BigInteger;

import utils.Endianness;

public class Communication {
    private final int STRSIZE = 40;

    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;

    public Communication(InputStream inputStream, OutputStream outputStream) throws IOException {
        dataInputStream = new DataInputStream(inputStream);
        dataOutputStream = new DataOutputStream(outputStream);
    }

    public void write_hello(String str) throws IOException {
        int lenStr = str.length();
        int numBytes = lenStr + 2;

        byte bStr[] = new byte[numBytes];

        bStr[0] = (byte) '1';

        for (int i = 0; i < lenStr; i++)
            bStr[i + 1] = (byte) str.charAt(i);

        bStr[numBytes - 1] = (byte) '0';

        dataOutputStream.write(bStr, 0, numBytes);
    }

    public void write_secret(String str) throws IOException {
        int lenStr = str.length();
        int numBytes = lenStr + 2;

        byte bStr[] = new byte[numBytes];

        bStr[0] = (byte) '3';

        for (int i = 0; i < lenStr; i++)
            bStr[i + 1] = (byte) str.charAt(i);

        bStr[numBytes - 1] = (byte) '0';

        dataOutputStream.write(bStr, 0, numBytes);
    }


    public void write_hash(int number) throws IOException {
        byte hashBytes[] = new byte[33];

        hashBytes[0] = (byte) '2';

        for (int i = 0; i < 8; i++) {
            byte bytes[] = int32ToBytes(number, Endianness.BIG_ENNDIAN);
            for (int j = 0; j < 4; j++) {
                hashBytes[i * 4 + j + 1] = bytes[j];
            }
            number /= Math.pow(2, 32);
        }

        dataOutputStream.write(hashBytes, 0, 33);
    }

    public int read_hash() throws IOException {
        int opcode = Integer.parseInt(String.valueOf(read_char()));

        BigInteger number = BigInteger.valueOf(0);
        double multiplier = Math.pow(2, 32);

        if (opcode == 2) {

            BigInteger aux = BigInteger.valueOf(1);
            int value;
            for (int i = 0; i < 8; i++) {
                byte intBytes[] = read_bytes(4);
                value = bytesToInt32(intBytes, Endianness.BIG_ENNDIAN);
                if(i!=0){
                    aux = aux.multiply(BigInteger.valueOf((long) multiplier));
                }
                number = number.add(BigInteger.valueOf(value));
            }
        }
        return number.intValue();
    }

    public String read_secret() throws IOException {
        int opcode = Integer.parseInt(String.valueOf(read_char()));

        char cStr[] = new char[100];

        if (opcode == 3) {
            int pos = 0;
            do {
                char charToPut = read_char();
                if (charToPut == '0') break;
                cStr[pos] = charToPut;
                pos++;
            } while (true);
        }

        return String.valueOf(cStr).trim();
    }


    public String read_hello() throws IOException {
        int opcode = Integer.parseInt(String.valueOf(read_char()));

        char cStr[] = new char[100];

        if (opcode == 1) {
            int pos = 0;
            do {
                char charToPut = read_char();
                if (charToPut == '0') break;
                cStr[pos] = charToPut;
                pos++;
            } while (true);
        }

        return String.valueOf(cStr).trim();
    }


    private char read_char() throws IOException {
        return (char) read_bytes(1)[0];
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

}
