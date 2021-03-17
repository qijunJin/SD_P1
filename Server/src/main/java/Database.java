import enumType.ErrorType;
import enumType.ShoutType;

import java.util.ArrayList;

import java.util.HashMap;


public class Database {


    private HashMap<String, String> source;
    private HashMap<ShoutType, String> shouts;
    private HashMap<ErrorType, String> errors;

    public Database() {

        this.source = new HashMap<>();
        this.shouts = new HashMap<>();
        this.errors = new HashMap<>();


        this.initInsultsComebacks();
        this.initErrorsTyped();
        this.initShoutsTyped();
    }


    private void initInsultsComebacks() {

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

    private void initShoutsTyped() {

        this.shouts.put(ShoutType.I_WIN, "¡He ganado, *!");
        this.shouts.put(ShoutType.YOU_WIN, "¡Has ganado, *!");
        this.shouts.put(ShoutType.YOU_WIN_FINAL, "¡Has ganado, *. Eres tan bueno que podrias luchar contra la Sword Master de la isla Mêlée!");

    }

    private void initErrorsTyped() {

        this.errors.put(ErrorType.WRONG_OPCODE, "¡Código de operación inválido, marinero de agua dulce! ¡Hasta la vista!");
        this.errors.put(ErrorType.INCOMPLETE_MESSAGE, "¡Mensaje incompleto, grumete! ¡Hasta la vista!");
        this.errors.put(ErrorType.TIMEOUT, "¡Me he candado de esperar tus mensajes, mequetrefe! ¡Hasta la vista!");

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
    public String getShoutByEnumAddName(ShoutType s, String name) {
        String str = this.shouts.get(s);
        str = str.replace("*", name);
        return str;
    }
/*
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
    }*/

    /*public String getInsultByIndex(int index) {
        return this.insults.get(index);
    }


        this.shouts.put(ShoutType.I_WIN, "¡He ganado, *!");
        this.shouts.put(ShoutType.YOU_WIN, "¡Has ganado, *!");
        this.shouts.put(ShoutType.YOU_WIN_FINAL, "¡Has ganado, *. Eres tan bueno que podrias luchar contra la Sword Master de la isla Mêlée!");

    }

    public boolean isRightComeback(String insult, String comeback) {
        return this.source.get(insult).equals(comeback);
    }


    public String getComebackByInsult(String insult) {
        return this.source.get(insult);
    }


    public ArrayList<String> getInsults() {
        return new ArrayList(this.source.keySet());
    }

    public ArrayList<String> getComebacks() {
        return new ArrayList(this.source.values());
    }

    /* public String getInsultByIndex(int index) {
         return this.insults.get(index);
     }
     public String getComebackByIndex(int index) {
         return this.comebacks.get(index);
     }*/
    public String getShoutByEnumAddName(ShoutType s, String name) {
        String str = this.shouts.get(s);
        str = str.replace("*", name);
        return str;
    }

    public String getErrorByEnum(ErrorType e) {
        return this.errors.get(e);
    }

    public HashMap<ErrorType, String> getErrors() { // For test
        return this.errors;
    }
}