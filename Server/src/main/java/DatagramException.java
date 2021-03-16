public class DatagramException extends Exception {

    public DatagramException(int writtenOpcode, int requiredOpcode) {
        super("Written opcode: " + writtenOpcode + ". Expected opcode: " + requiredOpcode);
    }
}
