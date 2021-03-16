package exception;

public class EmptyHashException extends Exception {
    public EmptyHashException() {
        super("Empty hash");
    }
}
