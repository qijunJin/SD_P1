package server;

import utils.Datagram;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

/**
 * Server class
 * Main class of the project Server.
 */
public class Server {

    static final String HELP = "Use: java -jar server-1.0-jar-with-dependencies.jar -p <port> -m [1|2]";
    static final String WRONG_PARAMETERS_USE = "Parameters are incorrect. Use: java -jar server-1.0-jar-with-dependencies.jar -p <port> -m [1|2]";
    static final String WRONG_PARAMETERS = "Parameters introduced are wrong!";

    /**
     * Main programme of Server.
     *
     * @param args arguments expected: -p [PORT] -m [1 | 2] or -h.
     */
    public static void main(String[] args) { // Control of parameters

        if (args.length == 4) {
            int numPort;
            int mode = 1;
            ServerSocket serverSocket = null;

            HashMap<String, String> options = new HashMap<>();
            for (int i = 0; i < args.length; i = i + 2) options.put(args[i], args[i + 1]);
            numPort = Integer.parseInt(options.get("-p"));
            if (options.containsKey("-m")) mode = Integer.parseInt(options.get("-m"));
            if (mode != 1 && mode != 2) {
                System.out.println(WRONG_PARAMETERS);
                return;
            }

            try { // Connexion & create game
                serverSocket = new ServerSocket(numPort);
                System.out.println("Connexion has been accepted with port: " + numPort);
                if (mode == 1) singlePlayer(serverSocket);
                else multiPlayer(serverSocket);
            } catch (IOException e) {
                System.out.println("IOException: " + e.getMessage());
            } finally {
                try {
                    if (serverSocket != null) serverSocket.close();
                } catch (IOException e) {
                    System.out.println("Connexion closed");
                }
            }
        } else System.out.println(args.length == 1 && args[0].equals("-h") ? HELP : WRONG_PARAMETERS_USE);
    }

    /**
     * Method which the server accept one player.
     *
     * @param serverSocket instance of server socket.
     */
    private static void singlePlayer(ServerSocket serverSocket) {

        boolean onGame = true;
        while (onGame) {
            System.out.println("------------------------------------------------------------------------------------");
            System.out.println("Waiting for player");
            Socket socket1 = null;

            try { // Connexion
                socket1 = serverSocket.accept();
                socket1.setSoTimeout(60 * 1000);
                System.out.println("Player connected");
            } catch (IOException e) {
                System.out.println("IOException: " + e.getMessage());
                onGame = false;
            }

            try { // Create game
                Datagram datagram1 = new Datagram(socket1);
                Thread t = new Thread(new ServerThread(datagram1, null));
                t.start();
            } catch (IOException e) {
                System.out.println("IOException: " + e.getMessage());
                onGame = false;
            }
        }
    }

    /**
     * Method which the server accept two players.
     *
     * @param serverSocket instance of server socket.
     */
    private static void multiPlayer(ServerSocket serverSocket) {

        boolean onGame = true;
        while (onGame) {
            System.out.println("------------------------------------------------------------------------------------");
            System.out.println("Waiting for players [0/2]");
            Socket socket = null;
            Socket socket2 = null;

            try { // Connexion socket
                socket = serverSocket.accept();
                socket.setSoTimeout(60 * 1000);
                System.out.println("Player connected [1/2]");
            } catch (IOException e) {
                System.out.println("IOException: " + e.getMessage());
                onGame = false;
            }

            try { // Connexion socket 2
                socket2 = serverSocket.accept();
                socket2.setSoTimeout(60 * 1000);
                System.out.println("Player connected [2/2]");
            } catch (IOException e) {
                System.out.println("IOException: " + e.getMessage());
                onGame = false;
            }

            try { // Create game
                Datagram datagram = new Datagram(socket);
                Datagram datagram2 = new Datagram(socket2);
                Thread t = new Thread(new ServerThread(datagram, datagram2));
                t.start();
            } catch (IOException e) {
                System.out.println(e.getMessage());
                onGame = false;
            }
        }
    }
}