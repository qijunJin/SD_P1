import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class MenuTest {

    @Test
    public void selectInsult_test() {
        ArrayList<String> insultsLearned = new ArrayList<String>();
        insultsLearned.add("Tonto");
        insultsLearned.add("Pendejo");
        insultsLearned.add("Descarado");
        insultsLearned.add("Malechor");
        File file = new File("test");

        Menu menu = new Menu();
        menu.showInsults(insultsLearned);
        int insultId = 2;                    //Insulto seleccionado
        String insult  = insultsLearned.get(insultId-1);

        try {
            file.createNewFile();

            Datagram datagram = new Datagram(new FileInputStream(file), new FileOutputStream(file));

            datagram.write_insult(insult);
            String readedStr = datagram.read_insult();

            assertEquals("Pendejo", readedStr);

        } catch (IOException | DatagramException e) {
            e.printStackTrace();
        }

    }
}
