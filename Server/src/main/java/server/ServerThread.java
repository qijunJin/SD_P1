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
     * @param datagram1 instance of datagram of the first player.
     * @param datagram2 instance of datagram of the second player.
     * @throws IOException IOException.
     */
    public ServerThread(Datagram datagram1, Datagram datagram2) throws IOException {
        game = new Game(datagram1, datagram2);
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
