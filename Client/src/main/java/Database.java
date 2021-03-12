import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class Database {

    private HashMap<String, String> source;
    private ArrayList<String> indexedKeys = new ArrayList<>();

    public Database() {
        this.source = new HashMap<>();
        this.initDatabase();
        this.initIndexedKeys();
    }

    private void initDatabase() {

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

    private void initIndexedKeys() {

        this.indexedKeys.addAll(new ArrayList<>(this.source.keySet()));

    }

    public boolean isRightComeback(String insult, String comeback) {
        return this.source.get(insult).equals(comeback);
    }

    private ArrayList<Integer> getRandomIndexes() {
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
    }

    public ArrayList<String> getRandomInsults() {
        ArrayList<String> insults = new ArrayList<>();
        ArrayList<Integer> indexes = this.getRandomIndexes();
        for (int i = 0; i < 2; i++) {
            int index = indexes.get(i);
            insults.add(this.indexedKeys.get(index));
        }
        return insults;
    }

    public ArrayList<String> getRandomComebacks() {
        ArrayList<String> comebacks = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            String key = this.indexedKeys.get(i);
            comebacks.add(this.source.get(key));
        }
        return comebacks;
    }
}