import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertTrue;

public class DatabaseProviderTest {

    @Test
    public void getRandomInsultComeback_test() {
        Database database = new Database();
        DatabaseProvider databaseProvider = new DatabaseProvider(database.getInsults(), database.getComebacks());

        Boolean b = true;
        int size = databaseProvider.getSize();

        for (int i = 0; i < size; i++) {
            ArrayList<String> str = databaseProvider.getRandomInsultComeback(); // Remove pair insult - comeback
            String insult = str.get(0);
            String comeback = str.get(1);
            if (!database.isRightComeback(insult, comeback)) b = false; // Pair not coincident
            if (databaseProvider.getSize() != size - i - 1) b = false; // Size reduced
        }

        assertTrue(b);
    }
}
