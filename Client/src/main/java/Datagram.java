import utils.ComUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

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
    public String read_hello() throws IOException {
        int opcode = readByte();
        char cStr[] = new char[100];
        byte bytes[];

        if (opcode == 1) {
            id = read_int32();
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


    public void write_hello(int id, String str) throws IOException {
        writeByte(1); // OPCODE
        write_int32(id); // ID
        writeString(str); //STRING
        writeByte(0); // END
    }


}


