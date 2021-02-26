
import java.io.*;

import utils.ComUtils;

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



}
