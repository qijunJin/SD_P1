import java.util.ArrayList;
import java.util.Scanner;

public class Menu {

    Scanner scan = new Scanner(System.in);

    public String getName(){
        System.out.println("Insert your name:");
        return scan.nextLine();
    }

    public String getSecret(){
        System.out.println("Insert your secret number:");
        return scan.nextLine();
    }

    public void showInsults(ArrayList<String> insults){
        System.out.println("Inserta el numero del insulto que quieras:");
        for (int i = 0; i <= insults.size() - 1; i++){
            System.out.println(i+1 + ". " + insults.get(i));
        }

    }

    public void showComebacks(ArrayList<String> comebacks){
        System.out.println("Inserta el numero del comeback que quieras:");
        for (int i = 0; i <= comebacks.size() - 1; i++){
            System.out.println(i+1 + ". " + comebacks.get(i));
        }

    }
}
