package shared.functions;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * Functions interface
 * Basic functions of conversion and calculation.
 */
public interface Functions {

    /**
     * Function that returns the hash of the given secret.
     *
     * @param str the given String.
     * @return the hash value of the secret.
     */
    static byte[] toHash(String str) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(str.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            return new byte[32];
        }
    }

    /**
     * Function to check if the hash of given secret is coincident to the given hash.
     *
     * @param string the given secret in String.
     * @param hash   the given hash in array.
     * @return true as they are coincident, false adversely.
     */
    static boolean proofHash(String string, byte[] hash) {
        byte[] encodedhash = toHash(string);
        return Arrays.equals(encodedhash, hash);
    }

    /**
     * Function to check the parity of two numbers.
     *
     * @param s1 first number in String.
     * @param s2 second number in String.
     * @return the parity of given numbers, true as even, false as odd.
     */
    static boolean isEven(String s1, String s2) {
        return (Integer.parseInt(s1) + Integer.parseInt(s2)) % 2 == 0;
    }

    /**
     * Function that convert byte to hexadecimal representation.
     *
     * @param num the byte value.
     * @return the hexadecimal value.
     */
    static String byteToHex(byte num) {
        char[] hexDigits = new char[2];
        hexDigits[0] = Character.forDigit((num >> 4) & 0xF, 16);
        hexDigits[1] = Character.forDigit((num & 0xF), 16);
        return new String(hexDigits).toUpperCase();
    }

    /**
     * Function that encode array of byte to hexadecimal representation.
     *
     * @param byteArray array of byte values.
     * @return array of hexadecimal values.
     */
    static String encodeHexString(byte[] byteArray) {
        StringBuffer hexStringBuffer = new StringBuffer();
        for (int i = 0; i < byteArray.length; i++) {
            hexStringBuffer.append(byteToHex(byteArray[i]));
            if (i < byteArray.length - 1) hexStringBuffer.append(" ");
        }
        return hexStringBuffer.toString();
    }
}
