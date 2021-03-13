import enumType.ErrorType;
import enumType.ShoutType;
import org.junit.Test;

import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static org.junit.Assert.*;

public class DatagramTest {

    @Test
    public void hello_test() {
        try {
            Socket socket = new SocketMock();
            Datagram datagram = new Datagram(socket);

            int id = 40;
            String str = "joe";

            datagram.write_hello(id, str);
            String readedStr = datagram.read_hello();

            assertEquals(str, readedStr);
            assertEquals(id, datagram.getIdOpponent());

        } catch (IOException | DatagramException e) {
            System.out.println(e.getMessage());
        }
    }


    @Test
    public void hello_exception_test() {
        String e1 = "Written opcode: 1. Expected opcode: 4";
        String e2 = "";
        try {
            Socket socket = new SocketMock();
            Datagram datagram = new Datagram(socket);

            int id = 40;
            String str = "joe";

            datagram.write_hello(id, str);
            String readedStr = datagram.read_insult();

        } catch (IOException | DatagramException e) {
            e2 = e.getMessage();
        }

        assertEquals(e1, e2);
    }


    @Test
    public void hash_test() {
        try {
            Socket socket = new SocketMock();
            Datagram datagram = new Datagram(socket);

            String s = "21394735986548847365534907392897867";

            datagram.write_hash(s);
            byte[] readedBytes = datagram.read_hash();

            MessageDigest digest = null;
            try {
                digest = MessageDigest.getInstance("SHA-256");
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }

            byte[] encodedhash = digest.digest(s.getBytes(StandardCharsets.UTF_8));

            assertArrayEquals(encodedhash, readedBytes);

        } catch (IOException | DatagramException e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void secret_test() {
        try {

            Socket socket = new SocketMock();
            Datagram datagram = new Datagram(socket);

            String str = "123456";
            datagram.write_secret(str);

            String readedStr = datagram.read_secret();

            assertEquals(str, readedStr);

        } catch (IOException | DatagramException e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void insult_test() {
        try {

            Socket socket = new SocketMock();
            Datagram datagram = new Datagram(socket);
            Database database = new Database();

            String str = database.getInsultByIndex(2);
            datagram.write_insult(str);

            String readedStr = datagram.read_insult();

            assertEquals(str, readedStr);

        } catch (IOException | DatagramException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void comeback_test() {
        try {
            Socket socket = new SocketMock();
            Datagram datagram = new Datagram(socket);
            Database database = new Database();

            String str = database.getComebackByIndex(2);
            datagram.write_comeback(str);

            String readedStr = datagram.read_comeback();

            assertEquals(str, readedStr);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void shout_test() {
        try {
            Socket socket = new SocketMock();
            Datagram datagram = new Datagram(socket);
            Database database = new Database();

            String str = database.getShoutByEnum(ShoutType.I_WIN);
            String name = "Qijun";
            str = str.replace("*", name);

            datagram.write_shout(str);
            String readedStr = datagram.read_shout();

            assertEquals(str, readedStr);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void error_test() {
        try {

            Socket socket = new SocketMock();
            Datagram datagram = new Datagram(socket);
            Database database = new Database();

            String str = database.getErrorByEnum(ErrorType.WRONG_OPCODE);
            datagram.write_error(str);

            String readedStr = datagram.read_error();

            assertEquals(str, readedStr);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void proof_test() {
        try {
            Socket socket = new SocketMock();
            Datagram datagram = new Datagram(socket);

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