import enumType.ErrorType;
import enumType.ShoutType;
import exception.OpcodeException;
import org.junit.Test;
import utils.SocketMock;

import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class DatagramTest {

    @Test
    public void hello_test() {
        try {
            Socket socket = new SocketMock();
            Datagram datagram = new Datagram(socket);

            int id = 40;
            String str = "joe";

            datagram.writeIntString(1, id, str);
            String[] readedStr = datagram.readStringArray(datagram.readByte(), 1);

            assertEquals(id, Integer.parseInt(readedStr[0]));
            assertEquals(str, readedStr[1]);

        } catch (IOException | OpcodeException e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void hash_test() {
        try {
            Socket socket = new SocketMock();
            Datagram datagram = new Datagram(socket);

            String s = "21394735986548847365534907392897867";

            datagram.writeHash(2, s);
            byte[] readedBytes = datagram.readHash(datagram.readByte(), 2);

            MessageDigest digest = null;
            try {
                digest = MessageDigest.getInstance("SHA-256");
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }

            byte[] encodedhash = digest.digest(s.getBytes(StandardCharsets.UTF_8));

            assertArrayEquals(encodedhash, readedBytes);

        } catch (IOException | OpcodeException e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void secret_test() {
        try {

            Socket socket = new SocketMock();
            Datagram datagram = new Datagram(socket);

            String str = "123456";
            datagram.writeString(3, str);

            String readedStr = datagram.readString(datagram.readByte(), 3);

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
            Database database = new Database();
            DatabaseProvider databaseProvider = new DatabaseProvider(database.getInsults(), database.getComebacks());

            ArrayList<String> str = databaseProvider.getRandomInsultComeback();
            datagram.writeString(4, str.get(0));

            String readedStr = datagram.readString(datagram.readByte(), 4);

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
            Database database = new Database();
            DatabaseProvider databaseProvider = new DatabaseProvider(database.getInsults(), database.getComebacks());

            ArrayList<String> str = databaseProvider.getRandomInsultComeback();
            datagram.writeString(5, str.get(1));

            String readedStr = datagram.readString(datagram.readByte(), 5);

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
            String name = "AlphaGo";
            String str = database.getShoutByEnumAddName(s, name);

            datagram.writeString(6, str);
            String readedStr = datagram.readString(datagram.readByte(), 6);

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
            datagram.writeString(7, str);

            String readedStr = datagram.readString(datagram.readByte(), 7);

            assertEquals(str, readedStr);

        } catch (IOException | OpcodeException e) {
            System.out.println(e.getMessage());
        }
    }


    @Test
    public void opcodeException_test() {
        OpcodeException ex = new OpcodeException(1, 4);

        String e1 = ex.getMessage();
        String e2 = "";

        try {
            Socket socket = new SocketMock();
            Datagram datagram = new Datagram(socket);

            int id = 40;
            String str = "joe";

            datagram.writeIntString(1, id, str);
            String readedStr = datagram.readString(datagram.readByte(), 4);

        } catch (IOException | OpcodeException e) {
            e2 = e.getMessage();
        }

        assertEquals(e1, e2);
    }
/*
    @Test
    public void isEven_test() {

        try {
            Socket socket = new utils.SocketMock();
            Datagram datagram = new Datagram(socket);
            Random random = new Random();

            int i1 = random.nextInt(Integer.MAX_VALUE);
            int i2 = random.nextInt(Integer.MAX_VALUE);

            String s1 = String.valueOf(i1);
            String s2 = String.valueOf(i2);

            if (i1 % 2 == 1 && i2 % 2 == 1) {
                assertTrue(datagram.isEven(s1, s2));
            } else if (i1 % 2 == 0 && i2 % 2 == 0) {
                assertTrue(datagram.isEven(s1, s2));
            } else {
                assertFalse(datagram.isEven(s1, s2));
            }

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void proofHash_test() {
        try {
            Socket socket = new utils.SocketMock();
            Datagram datagram = new Datagram(socket);
            String secret = "21394735986548847365534907392897867";

            *//* FOR TEST *//*
            MessageDigest digest = null;
            try {
                digest = MessageDigest.getInstance("SHA-256");
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }

            byte[] encodedhash = digest.digest(
                    secret.getBytes(StandardCharsets.UTF_8));

            assertTrue(datagram.proofHash(secret, encodedhash));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getHash_test() {
        try {
            Socket socket = new utils.SocketMock();
            Datagram datagram = new Datagram(socket);
            String secret = "21394735986548847365534907392897867";

            *//* FOR TEST *//*
            MessageDigest digest = null;
            try {
                digest = MessageDigest.getInstance("SHA-256");
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }

            byte[] encodedhash = digest.digest(
                    secret.getBytes(StandardCharsets.UTF_8));

            assertArrayEquals(datagram.getHash(secret), encodedhash);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/
}