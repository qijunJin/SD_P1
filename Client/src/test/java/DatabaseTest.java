import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static org.junit.Assert.assertTrue;

public class DatabaseTest {

    @Test
    public void isRightComeback_test() {
        File file = new File("test");
        try {
            file.createNewFile();


            Database database = new Database();

            String insult = "¡Me das ganas de vomitar!";
            String comeback = "Me haces pensar que alguien ya lo ha hecho.";
            Boolean b = database.isRightComeback(insult, comeback);
            assertTrue(b);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void random_insult_test() {
        Database data = new Database();
        data.getRandomInsultComeback();
        ArrayList<String> insults = data.getRandomInsults();
        ArrayList<String> comebacks = data.getRandomComebacks();

        for (int i = 0; i<2; i++){
            System.out.println(insults.get(i));
            System.out.println(comebacks.get(i));
        }

    }

}
