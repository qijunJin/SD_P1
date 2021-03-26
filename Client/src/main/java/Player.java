import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

/**
 * <h1>Player class</h1>
 * Store all tha data of a player.
 */
public class Player {
    private final HashSet<String> insults;
    private final HashSet<String> comebacks;
    private final Random random = new Random();

    private String name;
    private int id;
    private byte[] hash;
    private String secret;
    private int duel = 0;
    private int round = 0;

    /**
     * Constructor of class Player.
     */
    public Player() {
        this.insults = new HashSet<>();
        this.comebacks = new HashSet<>();
        this.name = "";
        this.id = -1;
    }

    /**
     * Method to add an insult and a comeback to the player's lists of insults and a comebacks.
     * It add both, insult and comeback, only if they aren't contained previously into lists.
     * @param list list with one insult and comeback.
     * @return true to warn that the insult and comeback have been added properly and false to warn us to search a new insult and comeback.
     */

    public boolean containsWithAddInsultComeback(ArrayList<String> list) {
        boolean b = false;
        if (!list.isEmpty()) {
            b = this.insults.contains(list.get(0)) && this.comebacks.contains(list.get(1));
            this.insults.add(list.get(0));
            this.comebacks.add(list.get(1));
        }
        return b;
    }

    /**
     * Method to generate a random int converted to String.
     * @return a random secret.
     */
    public String generateSecret() {
        int s = random.nextInt(Integer.MAX_VALUE);
        this.secret = String.valueOf(s);
        return String.valueOf(s);
    }

    /**
     * Method to generate a random int and assing it to the player's ID.
     * @return the player's ID.
     */
    public int generateId() {
        this.id = random.nextInt(Integer.MAX_VALUE);
        return this.id;
    }

    /**
     * Method to generate a random int to select a random insult from the player's insult list.
     * @return a random insult.
     */
    public String getRandomInsult() {
        ArrayList<String> list = new ArrayList<>(insults);
        int i = random.nextInt(this.insults.size());
        return list.get(i);
    }

    /**
     * Method to generate a random int to select a random comeback from the player's comeback list.
     * @return a random comeback.
     */
    public String getRandomComeback() {
        ArrayList<String> list = new ArrayList<>(comebacks);
        int i = random.nextInt(this.comebacks.size());
        return list.get(i);
    }

    /**
     * Method to get the player's ID.
     * @return the player's ID.
     */
    public int getId() {
        return id;
    }

    /**
     * Method to set the value of player's ID.
     * @param id value of player's ID.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Method to get the player's name.
     * @return the player's name.
     */
    public String getName() {
        return name;
    }

    /**
     * Method to set the value of player's name.
     * @param name value of player's name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Method to get the player's hash.
     * @return the player's hash.
     */
    public byte[] getHash() {
        return hash;
    }

    /**
     * ethod to set the value of player's hash.
     * @param hash value of player's hash
     */
    public void setHash(byte[] hash) {
        this.hash = hash;
    }

    /**
     * Method to get the player's secret
     * @return the player's secret
     */
    public String getSecret() {
        return secret;
    }

    /**
     * Method to set the value of player's secret.
     * @param secret value of player's secret.
     */
    public void setSecret(String secret) {
        this.secret = secret;
    }

    /**
     * Method to get the player's insults list.
     * @return the player's list of insults.
     */
    public ArrayList<String> getInsults() {
        return new ArrayList<>(insults);
    }

    /**
     * Method to get the player's comebacks list.
     * @return the player's list of comebacks.
     */
    public ArrayList<String> getComebacks() {
        return new ArrayList<>(comebacks);
    }

    /**
     * Method to get the size of the player's insults list.
     * @return the size of the insults list.
     */
    public int getInsultSize() {
        return this.insults.size();
    }

    /**
     * Method to get the size of the player's comebacks list.
     * @return the size of the comebacks list.
     */
    public int getComebackSize() {
        return this.comebacks.size();
    }

    /**
     * Method to know how many duels the player have won.
     * @return the num of duels won by the player.
     */
    public int getDuel() {
        return duel;
    }

    /**
     * Method to know how many rounds the player have won.
     * @return the num of rounds won by the player.
     */
    public int getRound() {
        return round;
    }

    /**
     * Method to check if the value specified it's the same as player.
     * @param str value to compare it with the player's name.
     * @return true if it's the same name or false if not.
     */
    public boolean hasSameName(String str) {
        return this.name.equals(str);
    }

    /**
     * Method to check if the value specified it's the same as the player.
     * @param id value to compare it with the player's ID.
     * @return
     */
    public boolean hasSameId(int id) {
        return this.id == id;
    }

    /**
     * Method to check if the player has a name.
     * @return true if he has a name or false if not.
     */
    public boolean hasName() {
        return !this.name.equals("");
    }

    /**
     * Method to check if the player has an ID.
     * @return true if he has an ID or false if not.
     */
    public boolean hasId() {
        return this.id != -1;
    }

    /**
     * Method to add an insult to the player's insult list.
     * @param insult new insult to add.
     */
    public void addInsult(String insult) {
        insults.add(insult);
    }

    /**
     * Method to add an comeback to the player's comeback list.
     * @param comeback new comeback to add.
     */
    public void addComeback(String comeback) {
        this.comebacks.add(comeback);
    }

    /**
     * Method to reset the lists of insults and comebacks of the player.
     */
    public void resetInsultsComebacks() {
        this.insults.clear();
        this.comebacks.clear();
    }

    /**
     * Method to add a won duel to the player.
     */
    public void addDuel() {
        this.duel++;
    }

    /**
     * Method to add a won round to the player.
     */
    public void addRound() {
        this.round++;
    }

    /**
     * Method to reset the rounds of the player to 0.
     */
    public void resetRound() {
        this.round = 0;
    }

    /**
     * Method to reset the duels of the player to 0.
     */
    public void resetDuel() {
        this.duel = 0;
    }
}
