import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.Assert.assertEquals;

public class ServerTest {

    @Test
    public void help_test() throws Exception {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));

        String[] args = {"-h"};
        Server server = new Server();
        server.main(args);
        assertEquals(out.toString().trim(), "Use: java -jar server-1.0-jar-with-dependencies.jar -p <port> -m [1|2]");
    }

    @Test
    public void wrong_num_parameters_test() throws Exception {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));

        String[] args = {"-p", "5555", "-m"};
        Server server = new Server();
        server.main(args);
        assertEquals(out.toString().trim(), "Parameters are incorrect. Use: java Server -p <port> -m [1|2]");
    }

    @Test(expected = Exception.class)
    public void wrong_parameters_test() throws Exception {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));

        String[] args = {"-p", "5555", "-m", "5"};
        Server server = new Server();
        server.main(args);
        assertEquals(out.toString().trim(), "Parameters introduced are wrong!");
    }
}
