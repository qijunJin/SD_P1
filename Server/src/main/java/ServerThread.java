import java.io.IOException;
import java.net.Socket;

public class ServerThread implements Runnable {

    private Game game;

    public ServerThread(Socket s1, Socket s2, int mode) throws IOException {
        game = new Game(s1, s2, mode);
    }

    public void run() {
        try {
            game.run();
        } catch (IOException e) {
            System.out.println("Player disconnected");
        }
    }
}
