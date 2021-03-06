import java.io.IOException;
import java.net.Socket;

public class GameThread implements Runnable{

    private Game game;

    public GameThread(Socket socket) throws IOException {
        game = new Game(socket);
    }

    public void run(){
        try {
            game.run();
        } catch (IOException e) {
            System.out.println("Info> Player disconnected abruptly, check log for more details.");
        }
    }
}
