import utils.ComUtils;

import java.io.IOException;
import java.net.Socket;

public class Datagram {

    private Socket socket;
    private ComUtils utils;

    public Datagram(Socket socket) throws IOException {
        this.socket = socket;
        this.utils = new ComUtils(this.socket);
    }

    public void write_hello(int id, String name) throws IOException {
        this.utils.write_hello(id, name);
    }

    public String read_hello() throws IOException {
        return this.utils.read_hello();
    }
}
