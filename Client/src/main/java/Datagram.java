import utils.ComUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Datagram {

    private ComUtils comUtils;
    private Socket socket;

    public Datagram(Socket socket) throws IOException {
        this.socket = socket;
        this.comUtils = new ComUtils(socket);
    }

    public Datagram(InputStream i, OutputStream o) throws IOException {
        this.comUtils = new ComUtils(i, o);
    }

    public void send_hello(String s) throws IOException {
        comUtils.write_hello(s);
    }

    public String receive_hello() throws IOException {
        return comUtils.read_hello();
    }

    public void send_hash(byte[] bytes) throws IOException {
        comUtils.write_hash(bytes);
    }

    public byte[] receive_hash() throws IOException {
        return comUtils.read_hash();
    }


}


