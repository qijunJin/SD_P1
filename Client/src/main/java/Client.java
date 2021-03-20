import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;

public class Client {
    public static void main(String[] args) {

        /* HELP */
        if (args.length == 1 && args[0].equals("-h")) {
            System.out.println("Use: java Client -s <hostname> -p <port> [-i 0|1]");

        } else if (args.length == 4 | (args.length == 6 && args[4].equals("-i"))) {

            /* CONTROL OF PARAMETERS */
            HashMap<String, String> options = new HashMap<>();
            for (int i = 0; i < args.length; i = i + 2)
                options.put(args[i], args[i + 1]);

            String hostname = "";
            int port = 0;
            int mode = 0; // By default

            try {
                hostname = options.get("-s");
                port = Integer.parseInt(options.get("-p"));
                if (options.containsKey("-i")) {
                    mode = Integer.parseInt(options.get("-i"));
                    if (mode != 0 && mode != 1) throw new Exception();
                }

                /* CREATE SOCKET & GAME */
                try {
                    InetAddress host = InetAddress.getByName(hostname);
                    Socket socket = new Socket(host, port);
                    socket.setSoTimeout(60 * 1000);
                    System.out.println("Connexion established!");

                    Datagram datagram = new Datagram(socket);

                    Game game = new Game(datagram, mode);
                } catch (Exception e) {
                    System.out.println("Connexion failed!");
                }

            } catch (Exception e) {
                System.out.println("Parameters introduced are wrong!");
            }

        } else {
            System.out.println("Use: java Client -h");
        }
    }
}
