package exception;

public class OpcodeException extends Exception {

    public OpcodeException(int writtenOpcode, int requiredOpcode) {
        super("Written opcode: " + writtenOpcode + ". Expected opcode: " + requiredOpcode);
    }
}
