import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class Server {

    public static void main(String[] args) throws Exception {

        /* Control of parameters */
        HashMap<String, String> options = new HashMap();
        for (int i = 0; i < args.length; i = i + 2)
            options.put(args[i], args[i + 1]);

        ServerSocket serverSocket = null;
        Socket socket = null;

        String hostname;
        int numPort;
        int mode = 0; // By default

        try {
            hostname = options.get("-s");
            numPort = Integer.parseInt(options.get("-p"));
            if (options.containsKey("-i")) {
                mode = Integer.parseInt(options.get("-i"));
            }
        } catch (Exception e) {
            throw new Exception("Parameters introduced are wrong!");
        }

        //Creamos server
        try{
            serverSocket = new ServerSocket(numPort);
            socket = serverSocket.accept();
            socket.setSoTimeout(500);
            System.out.println("Connexió acceptada d'un client.");

        }catch (IOException e) {
            System.out.println("IOException: "+ e.getMessage());
        }

        System.out.println("Creant Game.");
        try {
            //Thread para un solo jugador
            Thread t = (new Thread(new GameThread(socket)));
            t.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}