import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.Assert.assertEquals;

/**
 * ClientTest class
 * Class to test all the Client methods.
 */
public class ClientTest {

    /**
     * Test the help command.
     */
    @Test
    public void help_test() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        String[] args = {"-h"};
        Client.main(args);
        assertEquals(out.toString().trim(), Client.HELP);
    }

    /**
     * Test with incomplete parameters.
     */
    @Test
    public void wrong_num_parameters_test() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        String[] args = {"-p", "5555", "-m"};
        Client.main(args);
        assertEquals(out.toString().trim(), Client.WRONG_PARAMETERS_USE);
    }

    /**
     * Test with a wrong additional parameter.
     */
    @Test
    public void wrong_i_parameter_test() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        String[] args = {"-p", "5555", "-m", "1", "-f"};
        Client.main(args);
        assertEquals(out.toString().trim(), Client.WRONG_PARAMETERS_USE);
    }

    /**
     * Test with wrong parameters.
     */
    @Test
    public void wrong_parameters_test() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        String[] args = {"-p", "5555", "-m", "5", "-i", "5"};
        Client.main(args);
        assertEquals(out.toString().trim(), Client.WRONG_PARAMETERS);
    }
}
