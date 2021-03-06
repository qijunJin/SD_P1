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
        String str = "";

        if (opcode == 1) {
            id = read_int32();
            str = readString();
        }
        return str;
    }

    public void write_hello(int id, String str) throws IOException {
        writeByte(1); // OPCODE
        write_int32(id); // ID
        writeString(str); //STRING
        writeByte(0); // END
    }

    /* OPCODE 2: HASH */
    public byte[] read_hash() throws IOException {
        int opcode = readByte();
        byte hashBytes[] = new byte[32];

        if (opcode == 2) {
            hashBytes = readHash();
        }

        return hashBytes;
    }

    public void writeHash(byte[] bytes) throws IOException {
        writeByte(2); // OPCODE
        writeHash(bytes); // HASH
    }


    /* OPCODE 3: SECRET */
    public String read_secret() throws IOException {
        int opcode = readByte();
        String str = "";

        if (opcode == 3) {
            str = readString();
        }

        return str;
    }

    public void write_secret(String str) throws IOException {
        writeByte(1);
        writeString(str);
        writeByte(0);
    }


    /* OPCODE 4: INSULT */
    public String read_insult() throws IOException {
        int opcode = readByte();
        String str = "";

        if (opcode == 4) {
            str = readString();
        }

        return str;
    }

    public void write_insult(String str) throws IOException {
        writeByte(4);
        writeString(str);
        writeByte(0);
    }

    /* OPCODE 5: COMEBACK */
    public String read_comeback() throws IOException {
        int opcode = readByte();
        String str = "";

        if (opcode == 5) {
            str = readString();
        }

        return str;
    }

    public void write_comeback(String str) throws IOException {
        writeByte(5);
        writeString(str);
        writeByte(0);
    }

    /* OPCODE 6: SHOUT */
    public String read_shout() throws IOException {
        int opcode = readByte();
        String str = "";

        if (opcode == 6) {
            str = readString();
        }

        return str;
    }

    public void write_shout(String str) throws IOException {
        writeByte(7);
        writeString(str);
        writeByte(0);
    }

    /* OPCODE 7: ERROR */
    public String read_error() throws IOException {
        int opcode = readByte();
        String str = "";

        if (opcode == 7) {
            str = readString();
        }

        return str;
    }

    public void write_error(String str) throws IOException {
        writeByte(7);
        writeString(str);
        writeByte(0);
    }


}


