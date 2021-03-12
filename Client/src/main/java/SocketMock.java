import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class SocketMock extends Socket {

    private List<Byte> bytes = new ArrayList<>();

    public InputStream getInputStream(){
        return new InputStream() {
            @Override
            public int read(){
                return bytes.remove(0);
            }
        };
    }

    public OutputStream getOutputStream(){

        return new OutputStream() {
            @Override
            public void write(int b){
                bytes.add((byte)b);
            }
        };
    }

}