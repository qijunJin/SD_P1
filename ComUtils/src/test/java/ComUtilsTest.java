import org.junit.Test;

import static org.junit.Assert.*;

import utils.ComUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class ComUtilsTest {

   @Test
    public void hello_test() {
        File file = new File("test");
        try {
            file.createNewFile();
            Communication com = new Communication(new FileInputStream(file), new FileOutputStream(file));
            com.write_hello("joe");
            String readedStr = com.reader();

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
            Communication com = new Communication(new FileInputStream(file), new FileOutputStream(file));
            byte[] bytes = new byte[32];
            bytes[0] = 1; // Assign secret_number

            com.write_hash(bytes);
            byte[] readedBytes = com.read_hash();

            byte[] encodedhash = new byte[32]; // Create h(secret_number) with propose to test
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                encodedhash = digest.digest(bytes);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }

            assertArrayEquals(encodedhash, readedBytes); // Ensure test passed

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void secret_test() {
        File file = new File("test");
        try {
            file.createNewFile();
            Communication com = new Communication(new FileInputStream(file), new FileOutputStream(file));
            com.write_secret("secret");
            String readedStr = com.reader();

            assertEquals("secret", readedStr);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void insult_test() {
        File file = new File("test");
        try {
            file.createNewFile();
            Communication com = new Communication(new FileInputStream(file), new FileOutputStream(file));
            com.write_insult("Obtuve esta cicatriz en una batalla a muerte!");
            String readedStr = com.reader();

            assertEquals("Obtuve esta cicatriz en una batalla a muerte!", readedStr);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void comeback_test() {
        File file = new File("test");
        try {
            file.createNewFile();
            Communication com = new Communication(new FileInputStream(file), new FileOutputStream(file));
            com.write_comeback("Espero que ya hayas aprendido a no tocarte la nariz");
            String readedStr = com.reader();

            assertEquals("Espero que ya hayas aprendido a no tocarte la nariz", readedStr);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void shout_test() {
        File file = new File("test");
        try {
            file.createNewFile();
            Communication com = new Communication(new FileInputStream(file), new FileOutputStream(file));
            com.write_shout("!He ganado, Name2!");
            String readedStr = com.reader();

            assertEquals("!He ganado, Name2!", readedStr);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void error_test() {
        File file = new File("test");
        try {
            file.createNewFile();
            Communication com = new Communication(new FileInputStream(file), new FileOutputStream(file));
            com.write_insult("Espero que ya hayas aprendido a no tocarte la nariz!");
            String readedStr = com.reader();

             if (!readedStr.equals("Espero que ya hayas aprendido a no tocarte la nariz")){
                 com.write_error("!Mensaje incompleto, grumete! !Hasta la vista!");
                 readedStr = com.reader();
                 assertEquals("!Mensaje incompleto, grumete! !Hasta la vista!", readedStr);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
