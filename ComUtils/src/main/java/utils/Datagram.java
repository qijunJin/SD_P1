package utils;

import shared.exception.OpcodeException;

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
     * @throws IOException     IOException.
     * @throws OpcodeException OpcodeException.
     */
    public String readString(int opcode, int writtenOpcode) throws IOException, OpcodeException {
        if (opcode == writtenOpcode) return readString();
        throw new OpcodeException(opcode, writtenOpcode);
    }

    /**
     * Function that requires opcode to read the double data from stream.
     *
     * @return two stream data.
     * @throws IOException     IOException.
     * @throws OpcodeException OpcodeException.
     */
    public String[] readIntString(int opcode, int writtenOpcode) throws IOException, OpcodeException {
        if (opcode == writtenOpcode) return new String[]{String.valueOf(readInt32()), readString()};
        throw new OpcodeException(opcode, writtenOpcode);
    }

    /**
     * Function that requires opcode 0x02 to read the hash message.
     *
     * @return the hash message.
     * @throws IOException     IOException.
     * @throws OpcodeException OpcodeException.
     */
    public byte[] readHash(int opcode, int writtenOpcode) throws IOException, OpcodeException {
        if (opcode == writtenOpcode) return readHash();
        throw new OpcodeException(opcode, writtenOpcode);
    }

    /**
     * Function that write the one stream data with opcode.
     *
     * @param str one stream data.
     * @throws IOException IOException.
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
     * @throws IOException IOException.
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
     * @throws IOException IOException.
     */
    public void writeHash(int opcode, String str) throws IOException {
        writeByte(opcode);
        writeHash(str);
    }

    /**
     * Function that write the hash message with opcode 0x02.
     *
     * @param opcode
     * @param bytes  the hash message in array of bytes.
     * @throws IOException IOException.
     */
    public void writeHashArray(int opcode, byte[] bytes) throws IOException {
        writeByte(opcode);
        writeHashArray(bytes);
    }
}


