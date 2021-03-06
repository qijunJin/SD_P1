import java.io.IOException;
import java.net.Socket;

public class Game {

    Datagram comUtils;
    String nomOponent;

    public Game(Socket socket) throws IOException {
        this.comUtils = new Datagram(socket);
    }

    public void run() throws IOException {
        //Comienza el juego.
        //this.nomOponent = this.comUtils.read_hello();
    }
}
