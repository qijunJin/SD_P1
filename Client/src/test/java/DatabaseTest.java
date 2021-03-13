import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertTrue;

public class DatabaseTest {

    @Test
    public void isRightComeback_test() {
        Database database = new Database();

        int index = 4;
        String insult = database.getInsultByIndex(index);
        String comeback = database.getComebackByIndex(4);

        Boolean b = database.isRightComeback(insult, comeback);
        assertTrue(b);
    }

    @Test
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
    }


    @Test
    public void random_insult_test() {
        Database data = new Database();
        data.getRandomInsultComeback();
        ArrayList<String> insults = data.getRandomInsults();
        ArrayList<String> comebacks = data.getRandomComebacks();

        for (int i = 0; i < 2; i++) {
            System.out.println(insults.get(i));
            System.out.println(comebacks.get(i));
        }

    }

}
