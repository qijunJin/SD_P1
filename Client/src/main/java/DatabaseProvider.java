import enumType.ErrorType;
import enumType.ShoutType;

import java.util.ArrayList;
import java.util.Random;

public class DatabaseProvider {
    private Database database;
    private Random random = new Random();

    private ArrayList<String> insults;
    private ArrayList<String> comebacks;

    public DatabaseProvider() {
        this.database = new Database();
        this.insults = this.database.getInsults(); // Initiate with all insults
        this.comebacks = this.database.getComebacks(); // Initiate with all comebacks
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

    /* TESTED IN DATABASE */
    public boolean isInsult(String insult) {
        return this.database.isInsult(insult);
    }

    /* TESTED IN DATABASE */
    public boolean isComeback(String comeback) {
        return this.database.isComeback(comeback);
    }

    /* TESTED IN DATABASE */
    public boolean isRightComeback(String insult, String comeback) {
        return this.database.isRightComeback(insult, comeback);
    }

    /* TESTED IN DATABASE */
    public String getShoutByEnumAddName(ShoutType s, String name) {
        return this.database.getShoutByEnumAddName(s, name);
    }

    /* TESTED IN DATABASE */
    public String getErrorByEnum(ErrorType e) {
        return this.database.getErrorByEnum(e);
    }

    /* USED FOR TESTING */
    public int getSize() {
        return this.insults.size();
    }
}
