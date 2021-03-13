import java.util.*;

public class Database {

    private HashMap<Integer, String> insults;
    private HashMap<Integer, String> comebacks;
    private HashMap<String, String> source;

    private ArrayList<String> insultLearned = new ArrayList<>();
    private ArrayList<String> comebackLearned = new ArrayList<>();

    public Database() {
        this.insults = new HashMap<>();
        this.comebacks = new HashMap<>();
        this.source = new HashMap<>();

        this.initDatabase();
    }

    private void initDatabase() {

        this.insults.put(1, "¿Has dejado ya de usar pañales?");
        this.comebacks.put(1, "¿Por qué? ¿Acaso querías pedir uno prestado?");

        this.insults.put(2, "¡No hay palabras para describir lo asqueroso que eres!");
        this.comebacks.put(2, "Sí que las hay, sólo que nunca las has aprendido.");

        this.insults.put(3, "¡He hablado con simios más educados que tu!");
        this.comebacks.put(3, "Me alegra que asistieras a tu reunión familiar diaria.");

        this.insults.put(4, "¡Llevarás mi espada como si fueras un pincho moruno!");
        this.comebacks.put(4, "Primero deberías dejar de usarla como un plumero.");

        this.insults.put(5, "¡Luchas como un ganadero!");
        this.comebacks.put(5, "Qué apropiado, tú peleas como una vaca.");

        this.insults.put(6, "¡No pienso aguantar tu insolencia aquí sentado!");
        this.comebacks.put(6, "Ya te están fastidiando otra vez las almorranas, ¿Eh?");

        this.insults.put(7, "¡Mi pañuelo limpiará tu sangre!");
        this.comebacks.put(7, "Ah, ¿Ya has obtenido ese trabajo de barrendero?");

        this.insults.put(8, "¡Ha llegado tu HORA, palurdo de ocho patas!");
        this.comebacks.put(8, "Y yo tengo un SALUDO para ti, ¿Te enteras?");

        this.insults.put(9, "¡Una vez tuve un perro más listo que tu!");
        this.comebacks.put(9, "Te habrá enseñado todo lo que sabes.");

        this.insults.put(10, "¡Nadie me ha sacado sangre jamás, y nadie lo hará!");
        this.comebacks.put(10, "¿TAN rápido corres?");

        this.insults.put(11, "¡Me das ganas de vomitar!");
        this.comebacks.put(11, "Me haces pensar que alguien ya lo ha hecho.");

        this.insults.put(12, "¡Tienes los modales de un mendigo!");
        this.comebacks.put(12, "Quería asegurarme de que estuvieras a gusto conmigo.");

        this.insults.put(13, "¡He oído que eres un soplón despreciable!");
        this.comebacks.put(13, "Qué pena me da que nadie haya oído hablar de ti");

        this.insults.put(14, "¡La gente cae a mis pies al verme llegar!");
        this.comebacks.put(14, "¿Incluso antes de que huelan tu aliento?");

        this.insults.put(15, "¡Demasiado bobo para mi nivel de inteligencia!");
        this.comebacks.put(15, "Estaría acabado si la usases alguna vez.");

        this.insults.put(16, "Obtuve esta cicatriz en una batalla a muerte!");
        this.comebacks.put(16, "Espero que ya hayas aprendido a no tocarte la nariz.");

        this.source.put("¿Has dejado ya de usar pañales?", "¿Por qué? ¿Acaso querías pedir uno prestado?");
        this.source.put("¡No hay palabras para describir lo asqueroso que eres!", "Sí que las hay, sólo que nunca las has aprendido.");
        this.source.put("¡He hablado con simios más educados que tu!", "Me alegra que asistieras a tu reunión familiar diaria.");
        this.source.put("¡Llevarás mi espada como si fueras un pincho moruno!", "Primero deberías dejar de usarla como un plumero.");
        this.source.put("¡Luchas como un ganadero!", "Qué apropiado, tú peleas como una vaca.");
        this.source.put("¡No pienso aguantar tu insolencia aquí sentado!", "Ya te están fastidiando otra vez las almorranas, ¿Eh?");
        this.source.put("¡Mi pañuelo limpiará tu sangre!", "Ah, ¿Ya has obtenido ese trabajo de barrendero?");
        this.source.put("¡Ha llegado tu HORA, palurdo de ocho patas!", "Y yo tengo un SALUDO para ti, ¿Te enteras?");
        this.source.put("¡Una vez tuve un perro más listo que tu!", "Te habrá enseñado todo lo que sabes.");
        this.source.put("¡Nadie me ha sacado sangre jamás, y nadie lo hará!", "¿TAN rápido corres?");
        this.source.put("¡Me das ganas de vomitar!", "Me haces pensar que alguien ya lo ha hecho.");
        this.source.put("¡Tienes los modales de un mendigo!", "Quería asegurarme de que estuvieras a gusto conmigo.");
        this.source.put("¡He oído que eres un soplón despreciable!", "Qué pena me da que nadie haya oído hablar de ti");
        this.source.put("¡La gente cae a mis pies al verme llegar!", "¿Incluso antes de que huelan tu aliento?");
        this.source.put("¡Demasiado bobo para mi nivel de inteligencia!", "Estaría acabado si la usases alguna vez.");
        this.source.put("Obtuve esta cicatriz en una batalla a muerte!", "Espero que ya hayas aprendido a no tocarte la nariz.");

    }

    public boolean isRightComeback(String insult, String comeback) {
        return this.source.get(insult).equals(comeback);
    }

/*    private ArrayList<Integer> getRandomIndexes() {
        Random rand = new Random(); // Insultos y Comebacks aprendidos aleatoriamente

        ArrayList<Integer> indexes = new ArrayList<>();
        ArrayList<Integer> searchedIndexes = new ArrayList<>();

        for (int i = 0; i < 16; i++) indexes.add(i);

        for (int j = 0; j < 2; j++) {
            int pos = rand.nextInt(15 - j); // 0 - 15
            searchedIndexes.add(indexes.get(pos));
            indexes.remove(pos);
        }

        return searchedIndexes;
    }*/

/*    public HashMap<String, String> getRandomInsultComeback2() {
        ArrayList<Integer> indexes = getRandomIndexes();
        HashMap<String, String> learned = new HashMap<>();
        for (int i : indexes) {
            learned.put(this.insults.get(i), this.comebacks.get(i));
        }
        return learned;
    }*/

    public ArrayList<String> getInsultsByIndexes(ArrayList<Integer> indexes) {
        ArrayList<String> insults = new ArrayList<>();
        for (int i : indexes) {
            insults.add(this.getInsultByIndex(i));
        }
        return insults;
    }

    public ArrayList<String> getComebacksByIndexes(ArrayList<Integer> indexes) {
        ArrayList<String> comebacks = new ArrayList<>();
        for (int i : indexes) {
            comebacks.add(this.getComebackByIndex(i));
        }
        return comebacks;
    }

    public String getInsultByIndex(int index) { // Mainly for test
        return this.insults.get(index);
    }

    public String getComebackByIndex(int index) { // Mainly for test
        return this.comebacks.get(index);
    }

    public void getRandomInsultComeback() {
        ArrayList<String> insults = this.getInsults();
        ArrayList<String> comebacks = this.getComebacks();
        Random rand = new Random();
        int index = 16;
        for (int i = 0; i < 2; i++) {
            int numRan = rand.nextInt(index);
            insultLearned.add(insults.get(numRan));
            comebackLearned.add(comebacks.get(numRan));
            insults.remove(insults.get(numRan));
            comebacks.remove(comebacks.get(numRan));
            index--;
        }

    }

    public ArrayList<String> getRandomInsults() {
        return this.insultLearned;
    }

    public ArrayList<String> getRandomComebacks() {
        return this.comebackLearned;
    }

    public ArrayList<String> getInsults() {
        Set<String> keySet = this.source.keySet();
        return new ArrayList<>(keySet);
    }

    public ArrayList<String> getComebacks() {
        Collection<String> keySet = this.source.values();
        return new ArrayList<>(keySet);
    }
}