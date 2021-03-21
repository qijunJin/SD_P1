import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class PlayerTest {

    @Test
    public void generateSecret_test() {
        Player player = new Player();

        int secret = Integer.parseInt(player.generateSecret());

        assertTrue(secret > 0);
        assertTrue(secret < Integer.MAX_VALUE);
    }

    @Test
    public void generateId_test() {
        Player player = new Player();

        int id = player.generateId();

        assertTrue(id > 0);
        assertTrue(id < Integer.MAX_VALUE);
    }

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

        Boolean contained;
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

        assertTrue(dismissed == 2);
        assertTrue(databaseProvider2.getSize() == 0);
        /* We can also see that databaseProvider2 initiate with 16 pairs of insult - comeback */
        /* And we remove pair by pair of data until it is empty */
    }

    @Test
    public void getRandomInsult_test() {
        Player player = new Player();
        Database database = new Database();
        DatabaseProvider databaseProvider = new DatabaseProvider(database.getInsults(), database.getComebacks());

        player.containsWithAddInsultComeback(databaseProvider.getRandomInsultComeback());
        player.containsWithAddInsultComeback(databaseProvider.getRandomInsultComeback());

        assertTrue(player.getInsults().contains(player.getRandomInsult()));
    }

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
