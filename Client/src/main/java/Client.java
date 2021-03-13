import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;

public class Client {
    public static void main(String[] args) throws Exception {
        /*
        client> java –jar client.jar -h (ha de mostrar un help)
        Us: java Client -s <maquina_servidora> -p <port> [-i 0|1]
        (ha de seguir aquest format en aquest ordre i detectar errors)
        */

        /* Control of parameters */
        HashMap<String, String> options = new HashMap();
        for (int i = 0; i < args.length; i = i + 2)
            options.put(args[i], args[i + 1]);

        String hostname;
        int port;
        int mode = 0; // By default

        try {
            hostname = options.get("-s");
            port = Integer.parseInt(options.get("-p"));
            if (options.containsKey("-i")) {
                mode = Integer.parseInt(options.get("-i"));
            }
        } catch (Exception e) {
            throw new Exception("Parameters introduced are wrong!");
        }

        /* Socket & Create game */
        try {
            Socket socket = new Socket(hostname, port);
            socket.setSoTimeout(500);

            Datagram datagram = new Datagram(socket);
            Menu menu = new Menu();
            Game game = new Game(datagram, menu, mode);

        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }
}
