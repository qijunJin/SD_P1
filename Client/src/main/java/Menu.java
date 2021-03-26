import java.util.ArrayList;
import java.util.Scanner;

/**
 * <h1>Menu class</h1>
 * Output/Input interaction with the payer.
 */
public class Menu {
    Scanner scan = new Scanner(System.in);

    /**
     * Method to get the player's name.
     * @return player's name.
     */
    public String getName() {

        System.out.println("Insert your name:");
        boolean check = true;
        String name;

        do {
            name = scan.next();
            for (int i = 0; i < name.length(); i++) {
                if (name.charAt(i) >= '0' && name.charAt(i) <= '9') {
                    System.out.println("Invalid name, insert your name with alphabets:");
                    check = true;
                    break;
                } else {
                    check = false;
                }
            }
        } while (check);

        return name;
    }

    /**
     * Method to get the player's id.
     * @return player's id.
     */
    public int getId() {
        System.out.println("Insert your id:");
        boolean check = true;
        String id;

        do {
            id = scan.next();
            if (id.matches("[0-9]+")) check = false;
            else System.out.println("Invalid id, insert your id with digits: ");
        } while (check);

        return Integer.parseInt(id);
    }

    /**
     * Method to exit or continue the game.
     * @return true to Continue and false to Exit the game.
     *
     */
    public boolean getExit() {
        System.out.println("To continue playing press (C), other key will exit game");
        String answer = scan.next();
        return !answer.equals("C") && !answer.equals("c");
    }

    /**
     * Method to ask the player which option he wants.
     * @param list list with all the possible insults.
     * @param title title of the list.
     * @return the chosen option by player.
     */
    public String getOption(ArrayList<String> list, String title) {
        System.out.println("Insert the number of " + title + ":");
        for (int i = 0; i <= list.size() - 1; i++) {
            System.out.println(i + 1 + ". " + list.get(i));
        }

        String tmp;
        int option;
        do {
            tmp = scan.next();
            if (tmp.matches("[0-9]+")) {
                option = Integer.parseInt(tmp);
                if (option >= 0 && option <= list.size()) break;
                else System.out.println("Invalid option, try again: ");
            } else {
                System.out.println("Invalid option, try again: ");
            }
        } while (true);

        return list.get(option - 1);
    }
}
