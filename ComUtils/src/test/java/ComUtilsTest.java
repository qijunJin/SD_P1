import org.junit.Test;

import static org.junit.Assert.*;

import utils.ComUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

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
            String readedStr = com.read_hello();

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
            com.write_hash(1025);
            int readedHash = com.read_hash();

            assertEquals(1025, readedHash);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
