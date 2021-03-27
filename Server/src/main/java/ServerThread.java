import java.io.IOException;
import java.net.Socket;

/**
 * Thread of the server
 */
public class ServerThread implements Runnable {

    private final Game game;

    /**
     * Costructor of the server thread.
     *
     * @param s1 instance of socket of the first player.
     * @param s2 instance of socket of the second player.
     * @throws IOException exception of log.
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
