import enumType.ErrorType;
import enumType.ShoutType;

import java.util.ArrayList;
import java.util.Random;

public class DatabaseProvider {
    private Random random = new Random();

    private ArrayList<String> insults;
    private ArrayList<String> comebacks;

    public DatabaseProvider(ArrayList<String> insults, ArrayList<String> comebacks) {
        this.insults = insults;
        this.comebacks = comebacks;
    }

    /* TESTED */
    public ArrayList<String> getRandomInsultComeback() {
        ArrayList<String> list = new ArrayList<>();
        if (!this.insults.isEmpty()) {
            int index = random.nextInt(this.insults.size());
            String insult = this.insults.remove(index);
            String comeback = this.comebacks.remove(index);
            list.add(insult);
            list.add(comeback);
        }
        return list;
    }


    /* USED FOR TESTING */
    public int getSize() {
        return this.insults.size();
    }
}