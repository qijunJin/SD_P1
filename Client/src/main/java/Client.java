import utils.Datagram;

import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;

/**
 * Client class
 * Main class of the project Client.
 */
public class Client {

    static final String HELP = "Use: java -jar client-1.0-jar-with-dependencies.jar -s <hostname> -p <port> [-i 0|1]";
    static final String WRONG_PARAMETERS_USE = "Parameters are incorrect. Use: java -jar client-1.0-jar-with-dependencies.jar -s <hostname> -p <port> [-i 0|1]";
    static final String WRONG_PARAMETERS = "Parameters introduced are wrong!";

    /**
     * Main programme of Client.
     *
     * @param args arguments expected: -s [SERVER] -p [PORT] -i [0 | 1] or -h.
     */
    public static void main(String[] args) {

        if (args.length == 4 | (args.length == 6 && args[4].equals("-i"))) { // Control of parameters
            int port;
            int mode = 0;
            String hostname;
            Socket socket = null;

            HashMap<String, String> options = new HashMap<>();
            for (int i = 0; i < args.length; i = i + 2) options.put(args[i], args[i + 1]);
            hostname = options.get("-s");
            port = Integer.parseInt(options.get("-p"));
            if (options.containsKey("-i")) mode = Integer.parseInt(options.get("-i"));
            if (mode != 0 && mode != 1) {
                System.out.println("Parameters introduced are wrong!");
                return;
            }

            try { // Connexion & create game
                socket = new Socket(InetAddress.getByName(hostname), port);
                socket.setSoTimeout(30 * 1000);
                System.out.println("Connexion established!");
                Datagram datagram = new Datagram(socket);
                Game game = new Game(datagram, mode);
            } catch (Exception e) {
                System.out.println("IOException: " + e.getMessage());
            } finally {
                try {
                    if (socket != null) socket.close();
                } catch (Exception e) {
                    System.out.println("Connexion closed");
                }
            }
        } else System.out.println(args.length == 1 && args[0].equals("-h") ? HELP : WRONG_PARAMETERS_USE);
    }
}
