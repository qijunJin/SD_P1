import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.Assert.assertEquals;

public class ClientTest {

    @Test
    public void help_test() throws Exception {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));

        String[] args = {"-h"};
        Client client = new Client();
        client.main(args);
        assertEquals(out.toString().trim(), "Use: java -jar client-1.0-jar-with-dependencies.jar -s <hostname> -p <port> [-i 0|1]");
    }

    @Test
    public void wrong_num_parameters_test() throws Exception {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));

        String[] args = {"-p", "5555", "-m"};
        Client client = new Client();
        client.main(args);
        assertEquals(out.toString().trim(), "Parameters are incorrect. Use: java -jar client-1.0-jar-with-dependencies.jar -s <hostname> -p <port> [-i 0|1]");
    }

    @Test
    public void wrong_i_parameter_test() throws Exception {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));

        String[] args = {"-p", "5555", "-m", "1", "-f"};
        Client client = new Client();
        client.main(args);
        assertEquals(out.toString().trim(), "Parameters are incorrect. Use: java -jar client-1.0-jar-with-dependencies.jar -s <hostname> -p <port> [-i 0|1]");
    }

    @Test(expected = Exception.class)
    public void wrong_parameters_test() throws Exception {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));

        String[] args = {"-p", "5555", "-m", "5", "-i", "5"};
        Client client = new Client();
        client.main(args);
        assertEquals(out.toString().trim(), "Parameters introduced are wrong!");
    }
}
