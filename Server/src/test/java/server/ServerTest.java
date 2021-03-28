package server;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.Assert.assertEquals;

/**
 * ServerTest class
 * Class to test all the Server methods.
 */
public class ServerTest {

    /**
     * Test the help command.
     */
    @Test
    public void help_test() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        String[] args = {"-h"};
        Server.main(args);
        assertEquals(out.toString().trim(), Server.HELP);
    }

    /**
     * Test with incomplete parameters.
     */
    @Test
    public void wrong_num_parameters_test() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        String[] args = {"-p", "5555", "-m"};
        Server.main(args);
        assertEquals(out.toString().trim(), Server.WRONG_PARAMETERS_USE);
    }

    /**
     * Test with wrong parameters.
     */
    @Test
    public void wrong_parameters_test() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        String[] args = {"-p", "5555", "-m", "5"};
        Server.main(args);
        assertEquals(out.toString().trim(), Server.WRONG_PARAMETERS);
    }
}
