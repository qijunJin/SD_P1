import java.util.ArrayList;
import java.util.Scanner;

public class Menu {
    private static final String ALPHABETS = "^[a-zA-Z]*$";
    Scanner scan = new Scanner(System.in);

    public String getName() {
        System.out.println("Insert your name:");
        /*String name = "";
        while (!scan.hasNext(ALPHABETS)) {
            name = scan.next();
            if (name.matches(ALPHABETS)) break;
            System.out.println("Invalid name, insert your name with alphabets:");
        }
        return name;*/
        return scan.next();
    }

    public int getId() {
        System.out.println("Insert your id:");

        /*while (!scan.hasNextInt()) {
            scan.next();
            System.out.println("Invalid id, insert your id with digits: ");
        }*/

        return scan.nextInt();
    }

    public void showInsults(ArrayList<String> insults) {
        System.out.println("Inserta el numero del insulto que quieras:");
        for (int i = 0; i <= insults.size() - 1; i++) {
            System.out.println(i + 1 + ". " + insults.get(i));
        }
    }

    public void showComebacks(ArrayList<String> comebacks) {
        System.out.println("Inserta el numero del comeback que quieras:");
        for (int i = 0; i <= comebacks.size() - 1; i++) {
            System.out.println(i + 1 + ". " + comebacks.get(i));
        }
    }

    public int getOption() {
        return scan.nextInt() - 1;
    }
}
