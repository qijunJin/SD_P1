import org.junit.Test;
import utils.ComUtils;
import utils.SocketMock;

import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class ComUtilsTest {

    @Test
    public void byte_test() {
        try {
            Socket socket = new SocketMock();
            ComUtils com = new ComUtils(socket);

            int i = 12;
            com.writeByte(i);
            byte readedByte = com.readByte();

            assertEquals(i, readedByte);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void string_test() {
        try {
            Socket socket = new SocketMock();
            ComUtils com = new ComUtils(socket);

            String s = "¡joe!";
            com.writeString(s);
            com.writeByte(0);
            String readedStr = com.readString();

            assertEquals(s, readedStr);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void hash_test() {

        try {
            Socket socket = new SocketMock();
            ComUtils com = new ComUtils(socket);

            String s = "21394735986548847365534907392897867";

            com.writeHash(s);
            byte[] readedBytes = com.readHash();

            MessageDigest digest = null;

            try {
                digest = MessageDigest.getInstance("SHA-256");
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }

            byte[] encodedhash = digest.digest(s.getBytes(StandardCharsets.UTF_8));

            assertArrayEquals(encodedhash, readedBytes);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void int32_test() {

        try {
            Socket socket = new SocketMock();
            ComUtils com = new ComUtils(socket);

            int i = 2349230;
            com.writeInt32(i);
            int readedInt = com.readInt32();

            assertEquals(i, readedInt);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
