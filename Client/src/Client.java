import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;

public class Client {
    public static void main(String[] args) {

        //Tractament de paràmetres de consola

        /*
        client> java –jar client.jar -h (ha de mostrar un help)
        Us: java Client -s <maquina_servidora> -p <port> [-i 0|1]
        (ha de seguir aquest format en aquest ordre i detectar errors)
        */

        HashMap<String, String> options = new HashMap();
        for (int i = 0; i < args.length; i = i + 2) {
            options.put(args[i], args[i + 1]);
        }
        try {
            String hostname = options.get("-s");
            int port = Integer.parseInt(options.get("-p"));
            if (options.containsKey("-i")) {

            }
        } catch (Exception e) {

        }


        String nomMaquina = "";
        int numPort = 0;
        try {
            Socket socket = new Socket(nomMaquina, numPort);
            socket.setSoTimeout(500); //en ms.
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }
}
