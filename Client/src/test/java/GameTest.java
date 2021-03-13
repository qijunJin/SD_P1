import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static org.junit.Assert.assertTrue;

public class GameTest {

    @Test
    public void proof_test() {
        File file = new File("test");
        try {
            file.createNewFile();

            Socket socket = new SocketMock();
            Datagram datagram = new Datagram(socket);
            Menu menu = new Menu();
            int mode = 0;
            Game game = new Game(datagram, menu, mode);

            String s = "21394735986548847365534907392897867"; // Secret

            // FOR TEST
            MessageDigest digest = null;
            try {
                digest = MessageDigest.getInstance("SHA-256");
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            byte[] encodedhash = digest.digest(
                    s.getBytes(StandardCharsets.UTF_8));

            Boolean bool = game.proofHash(s, encodedhash);

            assertTrue(bool);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
