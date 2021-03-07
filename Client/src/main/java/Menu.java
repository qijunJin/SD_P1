import java.util.Scanner;

public class Menu {

    Scanner scan = new Scanner(System.in);

    public String getName(){
        System.out.println("Insert your name:");
        return scan.nextLine();
    }

    public int getId(){
        System.out.println("Insert your id:");
        return scan.nextInt();
    }

    public String getSecret(){
        System.out.println("Insert your secret number:");
        return scan.nextLine();
    }

    public void showInsults(String[] insults){
        System.out.println("Inserta el numero del insulto que quieras:");
        for (int i = 0; i <= insults.length - 1; i++){
            System.out.println(i+1 + ". " + insults[i]);
        }

    }

    public void showComebacks(String[] comebacks){
        System.out.println("Inserta el numero del comeback que quieras:");
        for (int i = 0; i <= comebacks.length - 1; i++){
            System.out.println(i+1 + ". " + comebacks[i]);
        }

    }
}
