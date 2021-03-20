import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
public class Server {

    public static void main(String[] args) throws Exception {

        if (args.length == 4) {
            /* Control of parameters */
            HashMap<String, String> options = new HashMap();
            for (int i = 0; i < args.length; i = i + 2)
                options.put(args[i], args[i + 1]);

            int numPort;
            int mode = 1; // By default

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
                ServerSocket serverSocket = new ServerSocket(numPort);
                System.out.println("Connexion has been accepted with port: " + numPort);

                if (mode == 1) {
                    singlePlayer(serverSocket);
                } else {
                    // multiPlayer(serverSocket);
                }
            } catch (IOException e) {
                System.out.println("IOException: " + e.getMessage());
            }

        } else if (args.length == 1 && args[0].equals("-h")) {
            System.out.println("Use: java Server -p <port> -m [1|2]");
        } else {
            System.out.println("Parameters are incorrect. Use: java Server -p <port> -m [1|2]");
        }

    }

    private static void singlePlayer(ServerSocket serverSocket) {

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
            Thread t = new Thread(new ServerThread(socket, null, 1));
            t.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}