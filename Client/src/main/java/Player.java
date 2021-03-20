import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

public class Player {
    private HashSet<String> insults;
    private HashSet<String> comebacks;
    private Random random = new Random();

    private String name;
    private int id;
    private byte[] hash;
    private String secret;
    private int duel = 0;
    private int round = 0;

    public Player() {
        this.insults = new HashSet<>();
        this.comebacks = new HashSet<>();
        this.name = "";
        this.id = -1;
    }

    public boolean containsWithAddInsultComeback(ArrayList<String> list) {
        boolean b = false;
        if (!list.isEmpty()) {
            b = this.insults.contains(list.get(0)) && this.comebacks.contains(list.get(1));
            this.insults.add(list.get(0));
            this.comebacks.add(list.get(1));
        }
        return b;
    }

    /* TESTED -> secret in range of 0 - MAX_VALUE */
    public String generateSecret() {
        int s = random.nextInt(Integer.MAX_VALUE);
        this.secret = String.valueOf(s);
        return String.valueOf(s);
    }

    /* TESTED */
    public int generateId() {
        this.id = random.nextInt(Integer.MAX_VALUE);
        return this.id;
    }

    /* TESTED */
    public String getRandomInsult() {
        ArrayList<String> list = new ArrayList(insults);
        int i = random.nextInt(this.insults.size());
        return list.get(i);
    }

    /* TESTED */
    public String getRandomComeback() {
        ArrayList<String> list = new ArrayList(comebacks);
        int i = random.nextInt(this.comebacks.size());
        return list.get(i);
    }

    /* GETTER - SETTER */
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte[] getHash() {
        return hash;
    }

    public void setHash(byte[] hash) {
        this.hash = hash;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public ArrayList<String> getInsults() {
        return new ArrayList(insults);
    }

    public ArrayList<String> getComebacks() {
        return new ArrayList(comebacks);
    }

    public int getInsultSize() {
        return this.insults.size();
    }

    public int getComebackSize() {
        return this.comebacks.size();
    }

    public int getDuel() {
        return duel;
    }

    public int getRound() {
        return round;
    }

    /* EQUAL - HAS */
    public boolean hasSameName(String str) {
        return this.name.equals(str);
    }

    public boolean hasSameId(int id) {
        return this.id == id;
    }

    public boolean hasName() {
        return !this.name.equals("");
    }

    public boolean hasId() {
        return this.id != -1;
    }

    /* ADD - RESET */
    public void addInsult(String insult) {
        insults.add(insult);
    }

    public void addComeback(String comeback) {
        this.comebacks.add(comeback);
    }

    public void resetInsultsComebacks() {
        this.insults.clear();
        this.comebacks.clear();
    }

    public void addDuel() {
        this.duel++;
    }

    public void addRound() {
        this.round++;
    }

    public void resetRound() {
        this.round = 0;
    }

    public void resetDuelRound() {
        this.duel = 0;
        this.round = 0;
    }
}
