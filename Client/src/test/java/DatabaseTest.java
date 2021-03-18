import enumType.ErrorType;
import enumType.ShoutType;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertTrue;

public class DatabaseTest {

    @Test
    public void isRightComeback_test() {
        Database database = new Database();

        Boolean b = true;
        int size = database.getInsults().size();

        for (int i = 0; i < size; i++) {
            String insult = database.getInsults().get(i);
            String comeback = database.getComebacks().get(i);
            if (!database.isRightComeback(insult, comeback)) b = false;
        }

        assertTrue(b);
    }

    @Test
    public void getShoutByEnum_test() {
        Database database = new Database();

        HashMap<ShoutType, String> shouts = database.getShouts(); // Get all errors
        ShoutType[] shoutType = ShoutType.values(); // Get all types

        Boolean b = true;
        String name = "AlphaGo";

        for (ShoutType sh : shoutType) {
            String s1 = shouts.get(sh);
            s1 = s1.replace("*", name);

            String s2 = database.getShoutByEnumAddName(sh, name); // Test this method
            if (!s1.equals(s2)) b = false;
        }

        assertTrue(b);
    }


    @Test
    public void getErrorByEnum_test() {
        Database database = new Database();

        HashMap<ErrorType, String> errors = database.getErrors(); // Get all errors
        ErrorType[] errorTypes = ErrorType.values(); // Get all types

        Boolean b = true;

        for (ErrorType er : errorTypes) {
            String e1 = errors.get(er);
            String e2 = database.getErrorByEnum(er); // Test this method
            if (!e1.equals(e2)) b = false;
        }

        assertTrue(b);
    }
}
