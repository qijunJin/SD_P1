package shared.database;

import shared.enumType.ErrorType;
import shared.enumType.ShoutType;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Database class
 * Database of the game, includes insults, comebacks, shouts, errors.
 */
public class Database {

    private final HashMap<String, String> sources;
    private final HashMap<ShoutType, String> shouts;
    private final HashMap<ErrorType, String> errors;

    /**
     * Constructor of database.
     */
    public Database() {
        this.sources = new HashMap<>();
        this.shouts = new HashMap<>();
        this.errors = new HashMap<>();

        this.initInsultsComebacks();
        this.initErrorsTyped();
        this.initShoutsTyped();
    }

    /**
     * Initialization of insults and comebacks.
     */
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

    /**
     * Initialization of errors.
     */
    private void initErrorsTyped() {
        this.errors.put(ErrorType.WRONG_OPCODE, "¡Código de operación inválido, marinero de agua dulce! ¡Hasta la vista!");
        this.errors.put(ErrorType.INCOMPLETE_MESSAGE, "¡Mensaje incompleto, grumete! ¡Hasta la vista!");
        this.errors.put(ErrorType.TIMEOUT, "¡Me he candado de esperar tus mensajes, mequetrefe! ¡Hasta la vista!");
        this.errors.put(ErrorType.NOT_IDENTIFIED, "¡No eres tú, soy yo! !Hasta la vista!");
        this.errors.put(ErrorType.SAME_ID, "¡Copión del ID! !Hasta la vista!");
    }

    /**
     * Initialization of shouts.
     */
    private void initShoutsTyped() {
        this.shouts.put(ShoutType.I_WIN, "¡He ganado, *!");
        this.shouts.put(ShoutType.YOU_WIN, "¡Has ganado, *!");
        this.shouts.put(ShoutType.YOU_WIN_FINAL, "¡Has ganado, *. Eres tan bueno que podrias luchar contra la Sword Master de la isla Mêlée!");
    }

    /**
     * Getter of all insults.
     *
     * @return list of insults.
     */
    public ArrayList<String> getInsults() {
        return new ArrayList<>(this.sources.keySet());
    }

    /**
     * Getter of all comebacks.
     *
     * @return list of comebacks.
     */
    public ArrayList<String> getComebacks() {
        return new ArrayList<>(this.sources.values());
    }

    /**
     * Getter of all errors.
     *
     * @return list of errors.
     */
    public HashMap<ErrorType, String> getErrors() {
        return this.errors;
    }

    /**
     * Getter of all shouts.
     *
     * @return list of shouts.
     */
    public HashMap<ShoutType, String> getShouts() {
        return this.shouts;
    }


    /**
     * Getter of one shout according to type of shout that requires name.
     *
     * @param s    the type of shout.
     * @param name the name given.
     * @return the specific shout with name added.
     */
    public String getShoutByEnumAddName(ShoutType s, String name) {
        return this.shouts.get(s).replace("*", name);
    }

    /**
     * Getter of one error according to type of error.
     *
     * @param e the type of error.
     * @return the specific error.
     */
    public String getErrorByEnum(ErrorType e) {
        return this.errors.get(e);
    }

    /**
     * Check if insult is in our database.
     *
     * @param insult the insult to check.
     * @return true if it is our database and false if it is not in our database.
     */
    public boolean isInsult(String insult) {
        return this.getInsults().contains(insult);
    }

    /**
     * Check if comeback is in our database.
     *
     * @param comeback the comeback to check.
     * @return true if it is our database and false if it is not in our database.
     */
    public boolean isComeback(String comeback) {
        return this.getComebacks().contains(comeback);
    }

    /**
     * Check if it is the right comeback to the insult.
     *
     * @param insult   the insult to check.
     * @param comeback the comeback to check.
     * @return true if it is the right comeback to the insult according to our database.
     */
    public boolean isRightComeback(String insult, String comeback) {
        return this.sources.get(insult).equals(comeback);
    }
}