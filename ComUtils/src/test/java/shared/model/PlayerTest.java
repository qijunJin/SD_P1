package shared.model;

import org.junit.Test;
import shared.database.Database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * PlayerTest class
 * Class to test all the Player methods.
 */
public class PlayerTest {

    /**
     * Test to check if the randomly generated secret is suitable.
     */
    @Test
    public void generateSecret_test() {
        Player player = new Player();
        int secret = Integer.parseInt(player.generateSecret());
        assertTrue(secret > 0 && secret < Integer.MAX_VALUE);
    }

    /**
     * Test to check if the randomly generated ID is suitable.
     */
    @Test
    public void generateId_test() {
        Player player = new Player();
        int id = player.generateId();
        assertTrue(id > 0 && id < Integer.MAX_VALUE);
    }

    /**
     * Test to check if the method discard the repeated insults and comebacks properly.
     */
    @Test
    public void containsWithAddInsultComeback_test() {
        Player player = new Player();
        Database database = new Database();
        DatabaseProvider databaseProvider = new DatabaseProvider(database.getInsults(), database.getComebacks());
        player.containsWithAddInsultComeback(databaseProvider.getRandomInsultComeback());
        player.containsWithAddInsultComeback(databaseProvider.getRandomInsultComeback());
        assertTrue(player.getInsultSize() == 2 && player.getComebackSize() == 2);
        /* Until here we have 2 pairs of insult - comeback */
        /* Now we try to add 16 pairs by this method -> We will see that 2 of them is dismissed */
        boolean contained;
        int dismissed = 0;
        DatabaseProvider databaseProvider2 = new DatabaseProvider(database.getInsults(), database.getComebacks());
        int i = 16;
        while (i > 0) {
            do {
                contained = player.containsWithAddInsultComeback(databaseProvider2.getRandomInsultComeback());
                if (contained) dismissed++;
            } while (contained);
            i--;
        }
        assertEquals(2, dismissed);
        assertEquals(0, databaseProvider2.getSize());
        /* We can also see that databaseProvider2 initiate with 16 pairs of insult - comeback */
        /* And we remove pair by pair of data until it is empty */
    }

    /**
     * Test to check if the randomly selected insult is contained in the player's insult list.
     */
    @Test
    public void getRandomInsult_test() {
        Player player = new Player();
        Database database = new Database();
        DatabaseProvider databaseProvider = new DatabaseProvider(database.getInsults(), database.getComebacks());
        player.containsWithAddInsultComeback(databaseProvider.getRandomInsultComeback());
        player.containsWithAddInsultComeback(databaseProvider.getRandomInsultComeback());
        assertTrue(player.getInsults().contains(player.getRandomInsult()));
    }

    /**
     * Test to check if the randomly selected comeback is contained in the player's comeback list.
     */
    @Test
    public void getRandomComeback_test() {
        Player player = new Player();
        Database database = new Database();
        DatabaseProvider databaseProvider = new DatabaseProvider(database.getComebacks(), database.getComebacks());
        player.containsWithAddInsultComeback(databaseProvider.getRandomInsultComeback());
        player.containsWithAddInsultComeback(databaseProvider.getRandomInsultComeback());
        assertTrue(player.getComebacks().contains(player.getRandomComeback()));
    }
}
