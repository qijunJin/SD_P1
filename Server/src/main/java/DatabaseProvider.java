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
        this.insults = this.database.getInsults();
        this.comebacks = this.database.getComebacks();
    }

    public ArrayList<String> getRandomInsultComeback() {
        int index = 0;
        String insult = "";
        String comeback = "";
        ArrayList<String> list = new ArrayList<>();

        if (!this.insults.isEmpty()) {
            index = random.nextInt(this.insults.size());
            insult = this.insults.remove(index);
            list.add(insult);
        }
        if (!this.comebacks.isEmpty()) {
            index = random.nextInt(this.comebacks.size());
            comeback = this.comebacks.remove(index);
            list.add(comeback);
        }
        return list;
    }


    public boolean isRightComeback(String insult, String comeback) {
        return this.database.isRightComeback(insult, comeback);
    }

    public String getShoutByEnumAddName(ShoutType s, String name) {
        return this.database.getShoutByEnumAddName(s, name);
    }

    public String getErrorByEnum(ErrorType e) {
        return this.database.getErrorByEnum(e);
    }

}
/*
private Database database;
    private Random random = new Random();
    private int len = 16;
    private ArrayList<Integer> indexes;

    public DatabaseProvider() {
        this.database = new Database();
        this.indexes = new ArrayList<>();

        this.init();
    }

    private void init() {
        for (int i = 0; i < len; i++) indexes.add(i);
    }

    private int generateRandomIndex() {
        return this.indexes.remove(random.nextInt(indexes.size()));
    }

    public boolean isRightComeback(String insult, String comeback) {
        return this.database.isRightComeback(insult, comeback);
    }

    public ArrayList<String, String> getRandomInsultComeback() {
        int index = this.generateRandomIndex();
        Pair<String, String> h = new Pair<>("", "");
        String insult = this.database.getInsultByIndex(index);
        String comeback = this.database.getComebackByIndex(index);
        h.put(insult, comeback);
        return h;
    }
* */