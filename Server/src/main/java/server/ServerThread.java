package server;

import utils.Datagram;

import java.io.IOException;

/**
 * Thread of the server
 */
public class ServerThread implements Runnable {

    private final Game game;

    /**
     * Constructor of the server thread.
     *
     * @param d  instance of datagram of the first player.
     * @param d2 instance of datagram of the second player.
     * @throws IOException IOException.
     */
    public ServerThread(Datagram d, Datagram d2) throws IOException {
        game = new Game(d, d2);
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
