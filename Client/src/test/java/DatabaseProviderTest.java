import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertTrue;

public class DatabaseProviderTest {

    @Test
    public void getRandomInsultComeback_test() {
        DatabaseProvider databaseProvider = new DatabaseProvider();

        Boolean b = true;
        int size = databaseProvider.getSize();

        for (int i = 0; i < size; i++) {
            ArrayList<String> str = databaseProvider.getRandomInsultComeback(); // Remove pair insult - comeback
            String insult = str.get(0);
            String comeback = str.get(1);
            if (!databaseProvider.isRightComeback(insult, comeback)) b = false; // Pair not coincident
            if (databaseProvider.getSize() != size - i - 1) b = false; // Size reduced
        }

        assertTrue(b);
    }
}
