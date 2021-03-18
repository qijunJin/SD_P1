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

    public void addInsult(String insult) {
        insults.add(insult);
    }

    public void addComeback(String comeback) {
        this.comebacks.add(comeback);
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

    public void removeInsultsComebacks() {
        this.insults.clear();
        this.comebacks.clear();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

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

    public void setName(String name) {
        this.name = name;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String generateSecret() {
        int s = random.nextInt(Integer.MAX_VALUE);
        this.secret = String.valueOf(s);
        return String.valueOf(s);
    }

    public byte[] getHash() {
        return hash;
    }

    public void setHash(byte[] hash) {
        this.hash = hash;
    }

    public int getDuel() {
        return duel;
    }

    public void addDuel() {
        this.duel++;
    }

    public int getRound() {
        return round;
    }

    public void addRound() {
        this.round++;
    }

    public ArrayList<String> getInsults() {
        return new ArrayList(insults);
    }

    public ArrayList<String> getComebacks() {
        return new ArrayList(comebacks);
    }

    public void resetDuelRound() {
        this.duel = 0;
        this.round = 0;
    }

    public void resetRound() {
        this.round = 0;
    }
}
