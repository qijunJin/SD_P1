package utils;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * ComUtils test class
 */
public class ComUtilsTest {

    /**
     * Test to check I/O of byte.
     *
     * @throws IOException IOException.
     */
    @Test
    public void byte_test() throws IOException {
        ComUtils com = new ComUtils(new SocketMock());
        int i = 12;
        com.writeByte(i);
        assertEquals(i, com.readByte());
    }

    /**
     * Test to check I/O of String.
     *
     * @throws IOException IOException.
     */
    @Test
    public void string_test() throws IOException {
        ComUtils com = new ComUtils(new SocketMock());
        String s = "¡joe!";
        com.writeString(s);
        com.writeByte(0);
        assertEquals(s, com.readString());
    }

    /**
     * Test to check I/O of Hash.
     *
     * @throws IOException IOException.
     */
    @Test
    public void hash_test() throws IOException, NoSuchAlgorithmException {
        ComUtils com = new ComUtils(new SocketMock());
        String s = "21394735986548847365534907392897867";
        com.writeHash(s);
        byte[] readedBytes = com.readHash();
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] encodedhash = digest.digest(s.getBytes(StandardCharsets.UTF_8));
        assertArrayEquals(encodedhash, readedBytes);
    }

    /**
     * Test to check I/O of int.
     *
     * @throws IOException IOException.
     */
    @Test
    public void int32_test() throws IOException {
        ComUtils com = new ComUtils(new SocketMock());
        int i = 2349230;
        com.writeInt32(i);
        assertEquals(i, com.readInt32());
    }
}
