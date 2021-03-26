import utils.Datagram;

import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;

/**
 * <h1>Client class</h1>
 * Main class of the project client.
 */
public class Client {

    /**
     * Main program of client.
     * @param args arguments expected: -s [SERVER] -p [PORT] -i [0 o 1] or -h.
     */
    public static void main(String[] args) throws Exception {

        /* HELP */
        if (args.length == 1 && args[0].equals("-h")) {
            System.out.println("Use: java -jar client-1.0-jar-with-dependencies.jar -s <hostname> -p <port> [-i 0|1]");

        } else if (args.length == 4 | (args.length == 6 && args[4].equals("-i"))) {

            /* CONTROL OF PARAMETERS */
            HashMap<String, String> options = new HashMap<>();
            for (int i = 0; i < args.length; i = i + 2)
                options.put(args[i], args[i + 1]);

            String hostname = "";
            int port = 0;
            int mode = 0; // By default

            Socket socket = null;

            try {
                hostname = options.get("-s");
                port = Integer.parseInt(options.get("-p"));
                if (options.containsKey("-i")) {
                    mode = Integer.parseInt(options.get("-i"));
                    if (mode != 0 && mode != 1) throw new Exception();
                }
            } catch (Exception e) {
                throw new Exception("Parameters introduced are wrong!");
            }

            /* CREATE SOCKET & GAME */
            try {
                InetAddress host = InetAddress.getByName(hostname);
                socket = new Socket(host, port);
                socket.setSoTimeout(30 * 1000);
                System.out.println("Connexion established!");

                Datagram datagram = new Datagram(socket);
                Game game = new Game(datagram, mode);

            } catch (Exception e) {
                System.out.println("Connexion failed!");

            } finally {
                try {
                    if (socket != null) socket.close();
                } catch (Exception e) {
                    System.out.println("Connexion closed");
                }
            }

        } else {
            System.out.println("Parameters are incorrect. Use: java -jar client-1.0-jar-with-dependencies.jar -s <hostname> -p <port> [-i 0|1]");
        }
    }
}
