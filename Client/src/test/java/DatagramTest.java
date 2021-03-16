import enumType.ErrorType;
import enumType.ShoutType;
import exception.EmptyHashException;
import exception.OpcodeException;
import org.junit.Test;

import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

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

        } catch (IOException | OpcodeException e) {
            System.out.println(e.getMessage());
        }
    }


    @Test
    public void exception_test() {
        OpcodeException ex = new OpcodeException(1, 4);

        String e1 = ex.getMessage();
        String e2 = "";

        try {
            Socket socket = new SocketMock();
            Datagram datagram = new Datagram(socket);

            int id = 40;
            String str = "joe";

            datagram.write_hello(id, str);
            String readedStr = datagram.read_insult();

        } catch (IOException | OpcodeException e) {
            e2 = e.getMessage();
        }

        assertEquals(e1, e2);

        // System.out.println(e1);
        // System.out.println(e2);
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

        } catch (IOException | OpcodeException | EmptyHashException e) {
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

        } catch (IOException | OpcodeException e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void insult_test() {
        try {

            Socket socket = new SocketMock();
            Datagram datagram = new Datagram(socket);
            DatabaseProvider database = new DatabaseProvider();


            ArrayList<String> str = database.getRandomInsultComeback();
            datagram.write_insult(str.get(0));

            String readedStr = datagram.read_insult();

            assertEquals(str.get(0), readedStr);

        } catch (IOException | OpcodeException e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void comeback_test() {
        try {
            Socket socket = new SocketMock();
            Datagram datagram = new Datagram(socket);
            DatabaseProvider database = new DatabaseProvider();

            ArrayList<String> str = database.getRandomInsultComeback();
            datagram.write_comeback(str.get(1));

            String readedStr = datagram.read_comeback();

            assertEquals(str.get(1), readedStr);

        } catch (IOException | OpcodeException e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void shout_test() {
        try {
            Socket socket = new SocketMock();
            Datagram datagram = new Datagram(socket);
            Database database = new Database();

            ShoutType s = ShoutType.I_WIN;
            String name = "Qijun";
            String str = database.getShoutByEnumAddName(s, name);

            datagram.write_shout(str);
            String readedStr = datagram.read_shout();

            assertEquals(str, readedStr);

        } catch (IOException | OpcodeException e) {
            System.out.println(e.getMessage());
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

        } catch (IOException | OpcodeException e) {
            System.out.println(e.getMessage());
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