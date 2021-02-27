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
}
