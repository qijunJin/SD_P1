import java.util.ArrayList;
import java.util.Random;

public class DatabaseProvider {
    private final ArrayList<String> insults;
    private final ArrayList<String> comebacks;
    private final Random random = new Random();

    public DatabaseProvider(ArrayList<String> insults, ArrayList<String> comebacks) {
        this.insults = insults;
        this.comebacks = comebacks;
    }

    /* TESTED */
    public ArrayList<String> getRandomInsultComeback() {
        ArrayList<String> list = new ArrayList<>();
        if (!this.insults.isEmpty()) {
            int index = random.nextInt(this.insults.size());
            list.add(this.insults.remove(index));
            list.add(this.comebacks.remove(index));
        }
        return list;
    }

    /* USED FOR TESTING */
    public int getSize() {
        return this.insults.size();
    }
}
