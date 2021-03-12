import org.junit.Test;

import java.io.File;
import java.io.IOException;

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
    public void insult_test() {
        Database data = new Database();
        String[] insults;
        insults = data.getInsults();
        for (int i = 0; i < insults.length - 1; i++){
            System.out.println(insults[i]);
        }
    }

    @Test
    public void comeback_test() {
        Database data = new Database();
        String[] comebacks;
        comebacks = data.getComebacks();
        for (int i = 0; i < comebacks.length - 1; i++){
            System.out.println(comebacks[i]);
        }
    }

}
