package shared.functions;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import static org.junit.Assert.*;

/**
 * FunctionsTest class
 * Class to test all the Functions methods.
 */
public class FunctionsTest {

    /**
     * Test to check the parity of 2 number in String.
     */
    @Test
    public void isEven_test() {
        Random random = new Random();
        int i1 = random.nextInt(Integer.MAX_VALUE);
        int i2 = random.nextInt(Integer.MAX_VALUE);
        String s1 = String.valueOf(i1);
        String s2 = String.valueOf(i2);
        if (i1 % 2 == 1 && i2 % 2 == 1 || i1 % 2 == 0 && i2 % 2 == 0) {
            assertTrue(Functions.isEven(s1, s2));
        } else assertFalse(Functions.isEven(s1, s2));
    }

    /**
     * Test to check if the hash of the secret is coincident to the given hash.
     *
     * @throws NoSuchAlgorithmException NoSuchAlgorithmException.
     */
    @Test
    public void proofHash_test() throws NoSuchAlgorithmException {
        String str = "21394735986548847365534907392897867";
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] encodedhash = digest.digest(str.getBytes(StandardCharsets.UTF_8));
        assertTrue(Functions.proofHash(str, encodedhash));
    }

    /**
     * Test to check if the hash conversion of the secret works properly.
     *
     * @throws NoSuchAlgorithmException NoSuchAlgorithmException.
     */
    @Test
    public void toHash_test() throws NoSuchAlgorithmException {
        String str = "21394735986548847365534907392897867";
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] encodedhash = digest.digest(str.getBytes(StandardCharsets.UTF_8));
        assertArrayEquals(Functions.toHash(str), encodedhash);
    }

    /**
     * Test to check the conversion from byte to hexadecimal.
     */
    @Test
    public void byteToHex_test() {
        Random random = new Random();
        byte number = (byte) (random.nextInt(255) - 127);
        String hex = String.format("%02X ", number).toUpperCase().trim();
        assertEquals(hex, Functions.byteToHex(number));
    }

    /**
     * Test to check the conversion from array of byte to array of hexadecimal.
     */
    @Test
    public void encodeHexString_test() {
        Random random = new Random();
        byte byte1 = (byte) (random.nextInt(255) - 127);
        byte byte2 = (byte) (random.nextInt(255) - 127);

        byte[] numberArray = new byte[2];
        numberArray[0] = byte1;
        numberArray[1] = byte2;

        String hex1 = String.format("%02X ", byte1).toUpperCase().trim();
        String hex2 = String.format("%02X ", byte2).toUpperCase().trim();

        String hex = hex1 + " " + hex2;
        assertEquals(hex, Functions.encodeHexString(numberArray));
    }
}
