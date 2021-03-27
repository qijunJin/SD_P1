package utils;

import org.junit.Test;
import shared.database.Database;
import shared.enumType.ErrorType;
import shared.enumType.ShoutType;
import shared.exception.OpcodeException;
import shared.model.DatabaseProvider;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class DatagramTest {

    @Test
    public void hello_test() throws IOException, OpcodeException {
        Datagram datagram = new Datagram(new SocketMock());
        int id = 40;
        String str = "joe";
        datagram.writeIntString(1, id, str);
        String[] readedStr = datagram.readIntString(1, datagram.readByte());
        assertEquals(id, Integer.parseInt(readedStr[0]));
        assertEquals(str, readedStr[1]);
    }

    @Test
    public void hash_test() throws IOException, OpcodeException, NoSuchAlgorithmException {
        Datagram datagram = new Datagram(new SocketMock());
        String s = "21394735986548847365534907392897867";
        datagram.writeHash(2, s);
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] encodedhash = digest.digest(s.getBytes(StandardCharsets.UTF_8));
        assertArrayEquals(encodedhash, datagram.readHash(2, datagram.readByte()));
    }

    @Test
    public void secret_test() throws IOException, OpcodeException {
        Datagram datagram = new Datagram(new SocketMock());
        String str = "123456";
        datagram.writeString(3, str);
        assertEquals(str, datagram.readString(3, datagram.readByte()));
    }

    @Test
    public void insult_test() throws IOException, OpcodeException {
        Datagram datagram = new Datagram(new SocketMock());
        Database database = new Database();
        DatabaseProvider databaseProvider = new DatabaseProvider(database.getInsults(), database.getComebacks());
        ArrayList<String> str = databaseProvider.getRandomInsultComeback();
        datagram.writeString(4, str.get(0));
        assertEquals(str.get(0), datagram.readString(4, datagram.readByte()));
    }

    @Test
    public void comeback_test() throws IOException, OpcodeException {
        Datagram datagram = new Datagram(new SocketMock());
        Database database = new Database();
        DatabaseProvider databaseProvider = new DatabaseProvider(database.getInsults(), database.getComebacks());
        ArrayList<String> str = databaseProvider.getRandomInsultComeback();
        datagram.writeString(5, str.get(1));
        assertEquals(str.get(1), datagram.readString(5, datagram.readByte()));
    }

    @Test
    public void shout_test() throws IOException, OpcodeException {
        Datagram datagram = new Datagram(new SocketMock());
        Database database = new Database();
        String name = "AlphaGo";
        String str = database.getShoutByEnumAddName(ShoutType.I_WIN, name);
        datagram.writeString(6, str);
        assertEquals(str, datagram.readString(6, datagram.readByte()));
    }

    @Test
    public void error_test() throws IOException, OpcodeException {
        Datagram datagram = new Datagram(new SocketMock());
        Database database = new Database();
        String str = database.getErrorByEnum(ErrorType.WRONG_OPCODE);
        datagram.writeString(7, str);
        assertEquals(str, datagram.readString(7, datagram.readByte()));
    }

    @Test
    public void opcodeException_test() {
        OpcodeException ex = new OpcodeException(4, 1);
        String e1 = ex.getMessage();
        String e2 = "";
        try {
            Datagram datagram = new Datagram(new SocketMock());
            int id = 40;
            String str = "joe";
            datagram.writeIntString(1, id, str);
            datagram.readString(4, datagram.readByte());
        } catch (IOException | OpcodeException e) {
            e2 = e.getMessage();
        }
        assertEquals(e1, e2);
    }
}