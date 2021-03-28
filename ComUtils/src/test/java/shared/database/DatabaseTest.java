package shared.database;

import org.junit.Test;
import shared.enumType.ErrorType;
import shared.enumType.ShoutType;

import java.util.HashMap;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * DatabaseTest class
 * Class to test all the Database methods.
 */
public class DatabaseTest {

    /**
     * Test to check if the received insult is correct.
     */
    @Test
    public void isInsult_test() {
        Database database = new Database();
        boolean b = true;
        int size = database.getInsults().size();
        for (int i = 0; i < size; i++) {
            String insult = database.getInsults().get(i);
            if (!database.isInsult(insult)) b = false;
        }
        assertTrue(b);
    }

    /**
     * Test to check if the received insult isn't correct.
     */
    @Test
    public void isInsult_false_test() {
        Database database = new Database();
        String insult = database.getInsults().get(0);
        insult = insult.substring(0, 10);
        assertFalse(database.isInsult(insult));
    }

    /**
     * Test to check if the received comeback is correct.
     */
    @Test
    public void isComeback_test() {
        Database database = new Database();
        boolean b = true;
        int size = database.getComebacks().size();
        for (int i = 0; i < size; i++) {
            String insult = database.getComebacks().get(i);
            if (!database.isComeback(insult)) b = false;
        }
        assertTrue(b);
    }

    /**
     * Test to check if the received comeback isn't correct.
     */
    @Test
    public void isComeback_false_test() {
        Database database = new Database();
        String insult = database.getComebacks().get(0);
        insult = insult.substring(0, 10);
        assertFalse(database.isComeback(insult));
    }

    /**
     * Test to check if the received comeback is correct against an insult.
     */
    @Test
    public void isRightComeback_test() {
        Database database = new Database();
        boolean b = true;
        int size = database.getInsults().size();
        for (int i = 0; i < size; i++) {
            String insult = database.getInsults().get(i);
            String comeback = database.getComebacks().get(i);
            if (!database.isRightComeback(insult, comeback)) b = false;
        }
        assertTrue(b);
    }

    /**
     * Test to check if all the shout messages, selected by enums, are correct.
     */
    @Test
    public void getShoutByEnum_test() {
        Database database = new Database();
        HashMap<ShoutType, String> shouts = database.getShouts(); // Get all errors
        ShoutType[] shoutType = ShoutType.values(); // Get all types
        boolean b = true;
        String name = "AlphaGo";
        for (ShoutType sh : shoutType) {
            String s1 = shouts.get(sh);
            s1 = s1.replace("*", name);
            String s2 = database.getShoutByEnumAddName(sh, name); // Test this method
            if (!s1.equals(s2)) b = false;
        }
        assertTrue(b);
    }

    /**
     * Test to check if all the error messages, selected by enums, are correct.
     */
    @Test
    public void getErrorByEnum_test() {
        Database database = new Database();
        HashMap<ErrorType, String> errors = database.getErrors(); // Get all errors
        ErrorType[] errorTypes = ErrorType.values(); // Get all types
        boolean b = true;
        for (ErrorType er : errorTypes) {
            String e1 = errors.get(er);
            String e2 = database.getErrorByEnum(er); // Test this method
            if (!e1.equals(e2)) b = false;
        }
        assertTrue(b);
    }
}
