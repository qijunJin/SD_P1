import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;

import static org.junit.Assert.assertEquals;

public class DatagramTest {


    @Test
    public void hello_test() {
        File file = new File("test");
        try {
            file.createNewFile();
            Socket socket = new Socket();
            //Datagram datagram = new Datagram(socket);
            
            Datagram datagram = new Datagram(new FileInputStream(file), new FileOutputStream(file));

            datagram.send_hello(97, "joe");
            String readedStr = datagram.receive_hello();

            assertEquals("ajoe", readedStr);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}