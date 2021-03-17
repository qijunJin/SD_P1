import enumType.ErrorType;
import enumType.ShoutType;

import java.util.ArrayList;
import java.util.HashMap;

public class Database {

    //private HashMap<Integer, String> insults;
    // private HashMap<Integer, String> comebacks;
    private HashMap<String, String> source;
    private HashMap<ShoutType, String> shouts;
    private HashMap<ErrorType, String> errors;

    public Database() {

        //this.insults = new HashMap<>();
        // this.comebacks = new HashMap<>();
        this.source = new HashMap<>();
        this.shouts = new HashMap<>();
        this.errors = new HashMap<>();

        // this.initInsultsIndexed();
        //  this.initComebacksIndexed();
        this.initInsultsComebacks();
        this.initErrorsTyped();
        this.initShoutsTyped();
    }
   /* private void initInsultsIndexed() {
        this.insults.put(1, "¿Has dejado ya de usar pañales?");
       this.insults.put(2, "¡No hay palabras para describir lo asqueroso que eres!");
        this.insults.put(3, "¡He hablado con simios más educados que tu!");
        this.insults.put(4, "¡Llevarás mi espada como si fueras un pincho moruno!");
        this.insults.put(5, "¡Luchas como un ganadero!");
        this.insults.put(6, "¡No pienso aguantar tu insolencia aquí sentado!");
        this.insults.put(7, "¡Mi pañuelo limpiará tu sangre!");
        this.insults.put(8, "¡Ha llegado tu HORA, palurdo de ocho patas!");
        this.insults.put(9, "¡Una vez tuve un perro más listo que tu!");
        this.insults.put(10, "¡Nadie me ha sacado sangre jamás, y nadie lo hará!");
        this.insults.put(11, "¡Me das ganas de vomitar!");
        this.insults.put(12, "¡Tienes los modales de un mendigo!");
        this.insults.put(13, "¡He oído que eres un soplón despreciable!");
        this.insults.put(14, "¡La gente cae a mis pies al verme llegar!");
        this.insults.put(15, "¡Demasiado bobo para mi nivel de inteligencia!");
        this.insults.put(16, "Obtuve esta cicatriz en una batalla a muerte!");
    }
    private void initComebacksIndexed() {
        this.comebacks.put(1, "¿Por qué? ¿Acaso querías pedir uno prestado?");
        this.comebacks.put(2, "Sí que las hay, sólo que nunca las has aprendido.");
        this.comebacks.put(3, "Me alegra que asistieras a tu reunión familiar diaria.");
        this.comebacks.put(4, "Primero deberías dejar de usarla como un plumero.");
        this.comebacks.put(5, "Qué apropiado, tú peleas como una vaca.");
        this.comebacks.put(6, "Ya te están fastidiando otra vez las almorranas, ¿Eh?");
        this.comebacks.put(7, "Ah, ¿Ya has obtenido ese trabajo de barrendero?");
        this.comebacks.put(8, "Y yo tengo un SALUDO para ti, ¿Te enteras?");
        this.comebacks.put(9, "Te habrá enseñado todo lo que sabes.");
        this.comebacks.put(10, "¿TAN rápido corres?");
        this.comebacks.put(11, "Me haces pensar que alguien ya lo ha hecho.");
        this.comebacks.put(12, "Quería asegurarme de que estuvieras a gusto conmigo.");
        this.comebacks.put(13, "Qué pena me da que nadie haya oído hablar de ti");
        this.comebacks.put(14, "¿Incluso antes de que huelan tu aliento?");
        this.comebacks.put(15, "Estaría acabado si la usases alguna vez.");
        this.comebacks.put(16, "Espero que ya hayas aprendido a no tocarte la nariz.");
    }*/

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