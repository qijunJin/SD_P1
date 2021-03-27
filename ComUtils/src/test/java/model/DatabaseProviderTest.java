package model;

import org.junit.Test;
import shared.database.Database;
import shared.model.DatabaseProvider;

import java.util.ArrayList;

import static org.junit.Assert.assertTrue;

/**
 * DatabaseProviderTest class
 * Class to test all the DatabaseProvider methods.
 */
public class DatabaseProviderTest {

    /**
     * Test to check if the given insult and comeback are a good match.
     */
    @Test
    public void getRandomInsultComeback_test() {
        Database database = new Database();
        DatabaseProvider databaseProvider = new DatabaseProvider(database.getInsults(), database.getComebacks());
        boolean b = true;
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
