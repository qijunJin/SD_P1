import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;

import static org.junit.Assert.assertEquals;

public class MenusTest {

    @Test
    public void selectInsult_test() {
        String[] insultsLearned = {"Tonto", "Gentuzo", "Maleducado"};
        File file = new File("test");

        Menu menu = new Menu();
        menu.showInsults(insultsLearned);
        int insultId = 2;                    //Insulto seleccionado
        String insult  = insultsLearned[insultId-1];

        try {
            file.createNewFile();

            Datagram datagram = new Datagram(new FileInputStream(file), new FileOutputStream(file));

            datagram.write_insult(insult);
            String readedStr = datagram.read_insult();

            assertEquals("Gentuzo", readedStr);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
