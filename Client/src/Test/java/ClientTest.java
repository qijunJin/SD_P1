import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.Assert.assertEquals;

public class ClientTest {

    @Test
    public void help_test() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        String[] args = {"-h"};
        Client.main(args);
        assertEquals(out.toString().trim(), Client.HELP);
    }

    @Test
    public void wrong_num_parameters_test() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        String[] args = {"-p", "5555", "-m"};
        Client.main(args);
        assertEquals(out.toString().trim(), Client.WRONG_PARAMETERS_USE);
    }

    @Test
    public void wrong_i_parameter_test() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        String[] args = {"-p", "5555", "-m", "1", "-f"};
        Client.main(args);
        assertEquals(out.toString().trim(), Client.WRONG_PARAMETERS_USE);
    }

    @Test
    public void wrong_parameters_test() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        String[] args = {"-p", "5555", "-m", "5", "-i", "5"};
        Client.main(args);
        assertEquals(out.toString().trim(), Client.WRONG_PARAMETERS);
    }
}
