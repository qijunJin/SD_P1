import java.util.ArrayList;
import java.util.Random;

/**
 * <h1>Database Provider class</h1>
 * It contains the insult - comeback pairs and only can be extracted in pair by random generated index.
 * The relation of player with this class is 1 to 1.
 */
public class DatabaseProvider {
    private final ArrayList<String> insults;
    private final ArrayList<String> comebacks;
    private final Random random = new Random();

    /**
     * Constructor of database provider.
     *
     * @param insults   list of insults provided by database.
     * @param comebacks list of comebacks provided by database.
     */
    public DatabaseProvider(ArrayList<String> insults, ArrayList<String> comebacks) {
        this.insults = insults;
        this.comebacks = comebacks;
    }

    /**
     * Getter of the size of insults. As it will be extracted in pair, the size remains the same.
     *
     * @return the size of pair that remains
     */
    public int getSize() {
        return this.insults.size();
    }

    /**
     * Getter of the insult - comeback pair extracted from the databaseProvider by the random generated index.
     *
     * @return Pair of insult - comeback. The position 0 is insult and the position 1 is comeback.
     */
    public ArrayList<String> getRandomInsultComeback() {
        ArrayList<String> list = new ArrayList<>();
        if (!this.insults.isEmpty()) {
            int index = random.nextInt(this.insults.size());
            list.add(this.insults.remove(index));
            list.add(this.comebacks.remove(index));
        }
        return list;
    }
}
