import java.io.IOException;
import java.net.Socket;

/**
 * <h1> Thread of the server</h1>
 */
public class ServerThread implements Runnable {

    private Game game;

    /**
     * Costructor of the server thread.
     * @param s1 instance of socket of the first player.
     * @param s2 instance of socket of the second player.
     * @throws IOException
     */
    public ServerThread(Socket s1, Socket s2) throws IOException {
        game = new Game(s1, s2);
    }

    /**
     * Method to start the game.
     */
    public void run() {
        try {
            game.run();
        } catch (IOException e) {
            System.out.println("Player disconnected");
        }
    }
}
