import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * <h1>SocketMock class</h1>
 * Mock of socket for the test propose.
 */
public class SocketMock extends Socket {

    private List<Byte> bytes = new ArrayList<>();

    /**
     * Method to get the inputs of the stream.
     * @return the input byte.
     */
    public InputStream getInputStream() {
        return new InputStream() {
            @Override
            public int read() {
                return bytes.remove(0);
            }
        };
    }

    /**
     * Method to get the outputs of the stream.
     * @return the output byte.
     */
    public OutputStream getOutputStream() {
        return new OutputStream() {
            @Override
            public void write(int b) {
                bytes.add((byte) b);
            }
        };
    }
}