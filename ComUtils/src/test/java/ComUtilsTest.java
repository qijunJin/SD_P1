import org.junit.Test;

import static org.junit.Assert.*;

import utils.ComUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class ComUtilsTest {

/*

    @Test
    public void example_test() {
        File file = new File("test");
        try {
            file.createNewFile();
            ComUtils comUtils = new ComUtils(new FileInputStream(file), new FileOutputStream(file));
            comUtils.write_int32(2);
            int readedInt = comUtils.read_int32();

            assertEquals(2, readedInt);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
*/


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
            bytes[0] = 1;
            com.write_hash(bytes);
            byte[] readedBytes = com.read_hash();

            assertArrayEquals(bytes, readedBytes);

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
