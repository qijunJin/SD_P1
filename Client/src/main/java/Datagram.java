import exception.OpcodeException;
import utils.ComUtils;

import java.io.IOException;
import java.net.Socket;

/**
 * Datagram class
 * Encapsulated comUtils' methods for communication between client and server.
 */
public class Datagram extends ComUtils {

    /**
     * Constructor of datagram.
     *
     * @param socket instance of socket.
     * @throws IOException comUtils operations exception
     */
    public Datagram(Socket socket) throws IOException {
        super(socket);
    }

    /**
     * Function that requires opcode to read the single data from stream.
     *
     * @return one stream data.
     * @throws IOException     read exception.
     * @throws OpcodeException not coincident opcode.
     */
    public String readString(int opcode, int requiredOpcode) throws IOException, OpcodeException {
        if (opcode == requiredOpcode) return readString();
        throw new OpcodeException(opcode, requiredOpcode);
    }

    /**
     * Function that requires opcode to read the double data from stream.
     *
     * @return two stream data.
     * @throws IOException     read exception.
     * @throws OpcodeException not coincident opcode.
     */
    public String[] readStringArray(int opcode, int requiredOpcode) throws IOException, OpcodeException {
        if (opcode == requiredOpcode) return new String[]{String.valueOf(readInt32()), readString()};
        throw new OpcodeException(opcode, requiredOpcode);
    }

    /**
     * Function that requires opcode 0x02 to read the hash message.
     *
     * @return the hash message.
     * @throws IOException     read exception.
     * @throws OpcodeException not coincident opcode.
     */
    public byte[] readHash(int opcode, int requiredOpcode) throws IOException, OpcodeException {
        if (opcode == requiredOpcode) return readHash();
        throw new OpcodeException(opcode, requiredOpcode);
    }

    /**
     * Function that write the one stream data with opcode.
     *
     * @param str one stream data.
     * @throws IOException write exception.
     */
    public void writeString(int opcode, String str) throws IOException {
        writeByte(opcode);
        writeString(str);
        writeByte(0);
    }

    /**
     * Function that write the hello message with opcode 0x01.
     *
     * @param id  Player's ID
     * @param str Player's name
     * @throws IOException write exception.
     */
    public void writeIntString(int opcode, int id, String str) throws IOException {
        writeByte(opcode);
        writeInt32(id);
        writeString(str);
        writeByte(0);
    }

    /**
     * Function that write the hash message with opcode 0x02.
     *
     * @param str the hash message.
     * @throws IOException write exception.
     */
    public void writeHash(int opcode, String str) throws IOException {
        writeByte(opcode);
        writeHash(str);
    }
}


