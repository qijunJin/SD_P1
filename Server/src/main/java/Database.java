import enumType.ErrorType;
import enumType.ShoutType;

import java.util.ArrayList;
import java.util.HashMap;

public class Database {

    private final HashMap<String, String> sources;
    private final HashMap<ShoutType, String> shouts;
    private final HashMap<ErrorType, String> errors;

    public Database() {
        this.sources = new HashMap<>();
        this.shouts = new HashMap<>();
        this.errors = new HashMap<>();

        this.initInsultsComebacks();
        this.initErrorsTyped();
        this.initShoutsTyped();
    }

    private void initInsultsComebacks() {

        this.sources.put("¿Has dejado ya de usar pañales?", "¿Por qué? ¿Acaso querías pedir uno prestado?");
        this.sources.put("¡No hay palabras para describir lo asqueroso que eres!", "Sí que las hay, sólo que nunca las has aprendido.");
        this.sources.put("¡He hablado con simios más educados que tu!", "Me alegra que asistieras a tu reunión familiar diaria.");
        this.sources.put("¡Llevarás mi espada como si fueras un pincho moruno!", "Primero deberías dejar de usarla como un plumero.");
        this.sources.put("¡Luchas como un ganadero!", "Qué apropiado, tú peleas como una vaca.");
        this.sources.put("¡No pienso aguantar tu insolencia aquí sentado!", "Ya te están fastidiando otra vez las almorranas, ¿Eh?");
        this.sources.put("¡Mi pañuelo limpiará tu sangre!", "Ah, ¿Ya has obtenido ese trabajo de barrendero?");
        this.sources.put("¡Ha llegado tu HORA, palurdo de ocho patas!", "Y yo tengo un SALUDO para ti, ¿Te enteras?");
        this.sources.put("¡Una vez tuve un perro más listo que tu!", "Te habrá enseñado todo lo que sabes.");
        this.sources.put("¡Nadie me ha sacado sangre jamás, y nadie lo hará!", "¿TAN rápido corres?");
        this.sources.put("¡Me das ganas de vomitar!", "Me haces pensar que alguien ya lo ha hecho.");
        this.sources.put("¡Tienes los modales de un mendigo!", "Quería asegurarme de que estuvieras a gusto conmigo.");
        this.sources.put("¡He oído que eres un soplón despreciable!", "Qué pena me da que nadie haya oído hablar de ti");
        this.sources.put("¡La gente cae a mis pies al verme llegar!", "¿Incluso antes de que huelan tu aliento?");
        this.sources.put("¡Demasiado bobo para mi nivel de inteligencia!", "Estaría acabado si la usases alguna vez.");
        this.sources.put("Obtuve esta cicatriz en una batalla a muerte!", "Espero que ya hayas aprendido a no tocarte la nariz.");

    }

    private void initErrorsTyped() {

        this.errors.put(ErrorType.WRONG_OPCODE, "¡Código de operación inválido, marinero de agua dulce! ¡Hasta la vista!");
        this.errors.put(ErrorType.INCOMPLETE_MESSAGE, "¡Mensaje incompleto, grumete! ¡Hasta la vista!");
        this.errors.put(ErrorType.TIMEOUT, "¡Me he candado de esperar tus mensajes, mequetrefe! ¡Hasta la vista!");

    }

    private void initShoutsTyped() {

        this.shouts.put(ShoutType.I_WIN, "¡He ganado, *!");
        this.shouts.put(ShoutType.YOU_WIN, "¡Has ganado, *!");
        this.shouts.put(ShoutType.YOU_WIN_FINAL, "¡Has ganado, *. Eres tan bueno que podrias luchar contra la Sword Master de la isla Mêlée!");

    }

    /* GETTER */
    public ArrayList<String> getInsults() {
        return new ArrayList<>(this.sources.keySet());
    }

    public ArrayList<String> getComebacks() {
        return new ArrayList<>(this.sources.values());
    }

    public HashMap<ErrorType, String> getErrors() { // For test
        return this.errors;
    }

    public HashMap<ShoutType, String> getShouts() { // For test
        return this.shouts;
    }

    /* TESTED */
    public boolean isInsult(String insult) {
        return this.getInsults().contains(insult);
    }

    /* TESTED */
    public boolean isComeback(String comeback) {
        return this.getComebacks().contains(comeback);
    }

    /* TESTED */
    public boolean isRightComeback(String insult, String comeback) {
        return this.sources.get(insult).equals(comeback);
    }

    /* TESTED */
    public String getShoutByEnumAddName(ShoutType s, String name) {
        return this.shouts.get(s).replace("*", name);
    }

    /* TESTED */
    public String getErrorByEnum(ErrorType e) {
        return this.errors.get(e);
    }
}