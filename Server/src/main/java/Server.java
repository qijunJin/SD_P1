import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

/**
 * <h1>Server class</h1>
 * Main class of the project server.
 */
public class Server {

    /**
     * Main program of server.
     * @param args arguments expected: -p [PORT] -m [MODE] or -h.
     */
    public static void main(String[] args) throws Exception {

        if (args.length == 4) {

            /* Control of parameters */
            HashMap<String, String> options = new HashMap<>();
            for (int i = 0; i < args.length; i = i + 2)
                options.put(args[i], args[i + 1]);

            int numPort;
            int mode = 1; // By default

            ServerSocket serverSocket = null;

            try {
                numPort = Integer.parseInt(options.get("-p"));
                if (options.containsKey("-m")) {
                    mode = Integer.parseInt(options.get("-m"));
                    if (mode != 1 && mode != 2) throw new Exception();
                }
            } catch (Exception e) {
                throw new Exception("Parameters introduced are wrong!");
            }

            try {
                serverSocket = new ServerSocket(numPort);
                System.out.println("Connexion has been accepted with port: " + numPort);

                if (mode == 1) {
                    singlePlayer(serverSocket);
                } else {
                    multiPlayer(serverSocket);
                }

            } catch (IOException e) {
                System.out.println("IOException: " + e.getMessage());
            } finally {
                try {
                    if (serverSocket != null) serverSocket.close();
                } catch (IOException e) {
                    System.out.println("Connexion closed");
                }
            }

        } else if (args.length == 1 && args[0].equals("-h")) {
            System.out.println("Use: java -jar server-1.0-jar-with-dependencies.jar -p <port> -m [1|2]");
        } else {
            System.out.println("Parameters are incorrect. Use: java Server -p <port> -m [1|2]");
        }
    }

    /**
     * Method where the server accept one player.
     * @param serverSocket instance of server socket.
     */
    private static void singlePlayer(ServerSocket serverSocket) {

        while (true) {
            System.out.println("------------------------------------------------------------------------------------");

            System.out.println("Waiting for player");
            Socket socket = null;

            try {
                socket = serverSocket.accept();
                socket.setSoTimeout(60 * 1000);
                System.out.println("Player connected");
            } catch (IOException e) {
                System.out.println("IOException: " + e.getMessage());
            }

            try {
                Thread t = new Thread(new ServerThread(socket, null));
                t.start();
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    /**
     * Method where the server accept two players.
     * @param serverSocket instance of server socket.
     */
    private static void multiPlayer(ServerSocket serverSocket) {

        while (true) {
            System.out.println("------------------------------------------------------------------------------------");

            /* SOCKET 1 */
            System.out.println("Waiting for players [0/2]");
            Socket socket = null;

            try {
                socket = serverSocket.accept();
                socket.setSoTimeout(60 * 1000);
                System.out.println("Player connected [1/2]");
            } catch (IOException e) {
                System.out.println("IOException: " + e.getMessage());
            }

            /* SOCKET 2 */
            Socket socket2 = null;

            try {
                socket2 = serverSocket.accept();
                socket2.setSoTimeout(60 * 1000);
                System.out.println("Player connected [2/2]");
            } catch (IOException e) {
                System.out.println("IOException: " + e.getMessage());
            }

            try {
                Thread t = new Thread(new ServerThread(socket, socket2));
                t.start();
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }
}