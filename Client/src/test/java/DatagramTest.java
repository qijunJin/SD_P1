import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static org.junit.Assert.*;

public class DatagramTest {


    @Test
    public void hello_test() {
        File file = new File("test");
        try {
            file.createNewFile();
            Socket socket = new Socket();
            //Datagram datagram = new Datagram(socket);

            Datagram datagram = new Datagram(new FileInputStream(file), new FileOutputStream(file));

            datagram.write_hello(40, "joe");
            String readedStr = datagram.read_hello();

            assertEquals("joe", readedStr);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void hash_test() {
        File file = new File("test");
        try {
            file.createNewFile();
            Datagram datagram = new Datagram(new FileInputStream(file), new FileOutputStream(file));

            String s = "21394735986548847365534907392897867"; // Secret

            datagram.writeHash(s); // WRITE
            byte[] readedBytes = datagram.readHash(); // READ

            // FOR TEST
            MessageDigest digest = null;
            try {
                digest = MessageDigest.getInstance("SHA-256");
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            byte[] encodedhash = digest.digest(
                    s.getBytes(StandardCharsets.UTF_8));

            // Ensure test passed
            assertArrayEquals(encodedhash, readedBytes);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void secret_test() {
        File file = new File("test");
        try {
            file.createNewFile();
            Datagram datagram = new Datagram(new FileInputStream(file), new FileOutputStream(file));

            String str = "123456";
            datagram.write_secret(str);

            String readedStr = datagram.read_secret();

            assertEquals(str, readedStr);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void proof_test() {
        File file = new File("test");
        try {
            file.createNewFile();
            Datagram datagram = new Datagram(new FileInputStream(file), new FileOutputStream(file));

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

            Boolean bool = datagram.proofHash(s, encodedhash);

            assertTrue(bool);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}