package shared.exception;

/**
 * Opcode exception
 */
public class OpcodeException extends Exception {

    /**
     * Override Exception toString method.
     *
     * @param opcode        the expected opcode.
     * @param writtenOpcode the written opcode.
     */
    public OpcodeException(int opcode, int writtenOpcode) {
        super("Expected opcode: " + opcode + ". Written opcode: " + writtenOpcode + "\n");
    }
}
