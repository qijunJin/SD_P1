import exception.OpcodeException;
import utils.ComUtils;

import java.io.IOException;
import java.net.Socket;

/**
 * <h1>Datagram class</h1>
 * Encapsulated comUtils' methods for communication between client and server.
 */
public class Datagram extends ComUtils {

    private Socket socket;
    private int id;

    /**
     * Constructor of datagram.
     *
     * @param socket instance of socket.
     * @throws IOException
     */
    public Datagram(Socket socket) throws IOException {
        super(socket);
    }

    /**
     * Function that requires opcode 0x01 to read the hello message.
     *
     * @return the hello message.
     * @throws IOException     read exception.
     * @throws OpcodeException not coincident opcode.
     */
    public String read_hello() throws IOException, OpcodeException {
        int writtenOpcode = readByte();
        String str;

        int requiredOpcode = 1;

        if (writtenOpcode == requiredOpcode) {
            id = readInt32();
            str = readString();
        } else {
            throw new OpcodeException(writtenOpcode, requiredOpcode);
        }
        return str;
    }

    /**
     * Function that write the hello message with opcode 0x01.
     *
     * @param id  Player's ID
     * @param str Player's name
     * @throws IOException write exception.
     */
    public void write_hello(int id, String str) throws IOException {
        writeByte(1);
        writeInt32(id);
        writeString(str);
        writeByte(0);
    }

    /**
     * Function that requires opcode 0x02 to read the hash message.
     *
     * @return the hash message.
     * @throws IOException     read exception.
     * @throws OpcodeException not coincident opcode.
     */
    public byte[] read_hash() throws IOException, OpcodeException {
        int writtenOpcode = readByte();
        byte[] hashBytes;

        int requiredOpcode = 2;

        if (writtenOpcode == requiredOpcode) {
            hashBytes = readHash();
        } else {
            throw new OpcodeException(writtenOpcode, requiredOpcode);
        }

        return hashBytes;
    }

    /**
     * Function that write the hash message with opcode 0x02.
     *
     * @param str the hash message.
     * @throws IOException write exception.
     */
    public void write_hash(String str) throws IOException {
        writeByte(2);
        writeHash(str);
    }

    /**
     * Function that requires opcode 0x03 to read the secret message.
     *
     * @return the secret message.
     * @throws IOException     read exception.
     * @throws OpcodeException not coincident opcode.
     */
    public String read_secret() throws IOException, OpcodeException {
        int requiredOpcode = 3;
        int writtenOpcode = readByte();
        String str;

        if (writtenOpcode == requiredOpcode) {
            str = readString();
        } else {
            throw new OpcodeException(writtenOpcode, requiredOpcode);
        }

        return str;
    }

    /**
     * Function that write the secret message with opcode 0x03.
     *
     * @param str the secret message.
     * @throws IOException write exception.
     */
    public void write_secret(String str) throws IOException {
        writeByte(3);
        writeString(str);
        writeByte(0);
    }

    /**
     * Function that requires opcode 0x04 to read the insult message.
     *
     * @return the insult message.
     * @throws IOException     read exception.
     * @throws OpcodeException not coincident opcode.
     */
    public String read_insult() throws IOException, OpcodeException {
        int requiredOpcode = 4;
        int writtenOpcode = readByte();
        String str;

        if (writtenOpcode == requiredOpcode) {
            str = readString();
        } else {
            throw new OpcodeException(writtenOpcode, requiredOpcode);
        }

        return str;
    }

    /**
     * Function that write the insult message with opcode 0x04.
     *
     * @param str the insult message.
     * @throws IOException write exception.
     */
    public void write_insult(String str) throws IOException {
        writeByte(4);
        writeString(str);
        writeByte(0);
    }

    /**
     * Function that requires opcode 0x05 to read the comeback message.
     *
     * @return the comeback message.
     * @throws IOException     read exception.
     * @throws OpcodeException not coincident opcode.
     */
    public String read_comeback() throws IOException, OpcodeException {
        int requiredOpcode = 5;
        int writtenOpcode = readByte();
        String str;

        if (writtenOpcode == requiredOpcode) {
            str = readString();
        } else {
            throw new OpcodeException(writtenOpcode, requiredOpcode);
        }

        return str;
    }

    /**
     * Function that write the comeback message with opcode 0x05.
     *
     * @param str the comeback message.
     * @throws IOException write exception.
     */
    public void write_comeback(String str) throws IOException {
        writeByte(5);
        writeString(str);
        writeByte(0);
    }

    /**
     * Function that requires opcode 0x06 to read the shout message.
     *
     * @return the shout message.
     * @throws IOException     read exception.
     * @throws OpcodeException not coincident opcode.
     */
    public String read_shout() throws IOException, OpcodeException {
        int requiredOpcode = 6;
        int writtenOpcode = readByte();
        String str;

        if (writtenOpcode == requiredOpcode) {
            str = readString();
        } else {
            throw new OpcodeException(writtenOpcode, requiredOpcode);
        }

        return str;
    }

    /**
     * Function that write the shout message with opcode 0x06.
     *
     * @param str the shout message.
     * @throws IOException write exception.
     */
    public void write_shout(String str) throws IOException {
        writeByte(6);
        writeString(str);
        writeByte(0);
    }

    /**
     * Function that requires opcode 0x07 to read the error message.
     *
     * @return the error message.
     * @throws IOException     read exception.
     * @throws OpcodeException not coincident opcode.
     */
    public String read_error() throws IOException, OpcodeException {
        int requiredOpcode = 7;
        int writtenOpcode = readByte();
        String str;

        if (writtenOpcode == requiredOpcode) {
            str = readString();
        } else {
            throw new OpcodeException(writtenOpcode, requiredOpcode);
        }

        return str;
    }

    /**
     * Function that write the error message with opcode 0x07.
     *
     * @param str the error message.
     * @throws IOException write exception.
     */
    public void write_error(String str) throws IOException {
        writeByte(7);
        writeString(str);
        writeByte(0);
    }

    /**
     * Getter of the player's ID assigned by read hello message.
     *
     * @return the player's ID
     */
    public int getIdOpponent() {
        return this.id;
    }
}


