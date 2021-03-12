import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;

import static org.junit.Assert.assertEquals;

public class MenusTest {

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

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void random_insult_comeback_test() {
        Database data = new Database();

        String[] all_insults = data.getInsults();           //Todos los insultos
        String[] all_comebacks = data.getComebacks();       //Todos los comebacks

        ArrayList<String> insultsLearned = new ArrayList<String>();
        ArrayList<String> comebacksLearned = new ArrayList<String>();

        Random rand = new Random();
        for (int i = 0; i<2; i++){
            int numRan = rand.nextInt(17);
            insultsLearned.add(all_insults[numRan]);
            comebacksLearned.add(all_comebacks[numRan]);
        }


        for (int i = 0; i<=insultsLearned.size()-1; i++){
            System.out.println(insultsLearned.get(i));
            System.out.println(comebacksLearned.get(i));
            System.out.println(i);
        }



    }
}
