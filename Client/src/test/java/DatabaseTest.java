import enumType.ErrorType;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.Assert.assertTrue;

public class DatabaseTest {

    @Test
    public void isRightComeback_test() {
        DatabaseProvider database = new DatabaseProvider();

        ArrayList<String> str = database.getRandomInsultComeback();

        String insult = str.get(0);
        String comeback = str.get(1);

        Boolean b = database.isRightComeback(insult, comeback);
        assertTrue(b);
    }

/*    @Test
    public void getRandomInsultComeback_test() {
        Database database = new Database();

        ArrayList<Integer> indexes = new ArrayList<>(); //Simulate random indexes
        indexes.add(2);
        indexes.add(13);

        ArrayList<String> insultsLearned = database.getInsultsByIndexes(indexes);
        ArrayList<String> comebacksLearned = database.getComebacksByIndexes(indexes);
        Boolean b = true;

        for (int i = 0; i < indexes.size(); i++) {
            String insult = insultsLearned.get(i);
            String comeback = comebacksLearned.get(i);
            if (!database.isRightComeback(insult, comeback)) b = false;
        }

        assertTrue(b);
    }*/

    @Test
    public void error_test() {
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
