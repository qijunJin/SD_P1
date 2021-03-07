import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Scanner;

public class Game {
    private Datagram datagram;
    private Menu menu;
    private int mode;
    private int state;
    private boolean gameBool = true;      //Bucle infinito para el game
    private int player;                   //0->Client   1->Server
    private Scanner sc = new Scanner(System.in);

    private String name, oponentName;
    private int id, oponentId;
    private byte[] hash, oponentHash;
    private String secret, oponentSecret;

    private int duel = 0;
    private int round = 1;
    private int points = 0;

    private String[] insultsLearned = {"Tonto", "Estúpido"};
    private String[] comebacksLearned = {"Te rebota", "Lo mismo digo"};
    private int insultId, comebackId;
    private String insult, comeback;
    private String oponentInsult, oponentComeback;



    public Game(Datagram datagram, Menu menu, int mode) throws IOException {
        this.datagram = datagram;
        this.menu = menu;
        this.mode = mode;
        state = 0;
        this.run();

    }

    private void run() throws IOException {
        while (gameBool) {
            if (state == 0) {             //Recopilación de datos del jugador y mensaje HELLO

                this.name = this.menu.getName();
                this.id = this.menu.getId();

                try {
                    this.datagram.write_hello(this.id, this.name);
                    this.oponentName = this.datagram.read_hello();
                    this.oponentId = this.datagram.getIdOponent();
                } catch (IOException e) {
                    System.out.println("ERROR HELLO");
                    System.exit(1);
                }

                this.state = 1;

            }else if(state == 1){         //Envio de HASH entre jugadores


                this.secret = this.menu.getSecret();

                try {
                    this.datagram.writeHash(this.secret);
                    oponentHash = this.datagram.read_hash();
                } catch (IOException e) {
                    System.out.println("ERROR HASH");
                    System.exit(1);
                }

                this.state = 2;

            }else if(state == 2){            //Envio de SECRET entre jugadores

                try {
                    this.datagram.write_secret(this.secret);
                    this.oponentSecret = this.datagram.read_secret();
                } catch (IOException e) {
                    System.out.println("ERROR SECRET");
                    System.exit(1);
                }

                this.state = 3;

            }else if(state == 3){         //Comprobación de HASH correcto y elección de quien comienza el juego.

                if(this.datagram.proofHash(this.secret, this.oponentHash)){
                    if(this.id != this.oponentId){
                        if(this.datagram.isEven(this.secret, this.oponentSecret)){
                            if (this.id < this.oponentId){
                                player = 0;  //Cliente
                            }else{
                                player = 1;  //Server
                            }
                        }else{
                            if (this.id > this.oponentId){
                                player = 0;  //Cliente
                            }else{
                                player = 1;  //Server
                            }
                        }
                        this.state = 4;
                    }else{
                        try {
                            this.datagram.write_error("ERROR ID");
                        } catch (IOException e) {
                            System.out.println("ERROR ID");
                        }
                    }
                }else{
                    try {
                        this.datagram.write_error("ERROR HASH");
                    } catch (IOException e) {
                        System.out.println("ERROR HASH");
                    }
                }


            }else if (state == 4){

                if (this.duel == 3) {           //Ganamos la partida por haber ganado 3 duelos.
                    try {
                        this.datagram.write_shout("¡He ganado, " + this.oponentName + " !");
                        System.out.println(this.datagram.read_shout());
                    } catch (IOException e){
                        System.out.println("ERROR SHOUT");
                    }
                    this.duel = 0;           //Reiniciamos contador de duelos ganados
                    this.state = 7;          //Estado de victoria
                    this.gameBool = false;   //Cerramos bucle infinito
                } else {
                    if (this.round < 3) {
                        this.state = 5;                 //Seguimos jugando
                    } else {
                        if (this.points == 0) {         //He perdido 2 rondas de 2

                            try {
                                this.datagram.write_shout("¡Has ganado, " + this.oponentName + " !");
                                System.out.println(this.datagram.read_shout());
                            }catch (IOException e){
                                System.out.println("ERROR");
                            }

                        } else if (this.points == 2) {   //He ganado 2 rondas de 2

                            try {
                                this.datagram.write_shout("¡He ganado, " + this.oponentName + " !");
                                System.out.println(this.datagram.read_shout());
                            }catch (IOException e){
                                System.out.println("ERROR");
                            }

                            this.duel++;         //+1 a duelos ganados
                            this.round = 1;      //Reiniciamos rondas para el siguiente duelo

                        } else {
                            if (this.round == 4) {      //He perdido 2 rondas de 3

                                try {
                                    this.datagram.write_shout("¡Has ganado, " + this.oponentName + " !");
                                    System.out.println(this.datagram.read_shout());
                                }catch (IOException e){
                                    System.out.println("ERROR");
                                }

                                this.round = 1;  //Reiniciamos rondas para el siguiente duelo

                                /*
                                Alomejor pasamos a otro estado en el que preguntamos
                                al jugador si quiere seguir jugando?
                                */

                            } else {                     //Empate 1 a 1, sigue el juego.
                                this.state = 5;
                            }
                        }
                    }
                }

            }else if (this.state == 5){       //Envio de INSULTS y COMEBACKS

                    if (this.player == 0) {  //Empieza el cliente insultando

                        this.menu.showInsults(insultsLearned);          //Mostramos insultos aprendidos
                        this.insultId = sc.nextInt();
                        this.insult  = this.insultsLearned[this.insultId-1];

                        try {
                            this.datagram.write_insult(this.insult);
                            this.oponentComeback = this.datagram.read_comeback();
                        } catch (IOException e) {
                            System.out.println("ERROR");
                        }

                        this.state = 6;

                    }else{                   //Empieza el server insultando

                        try {
                            System.out.println(this.datagram.read_insult());
                        } catch (IOException e) {
                            System.out.println("ERROR");
                        }

                        this.menu.showComebacks(comebacksLearned);          //Mostramos insultos aprendidos
                        this.comebackId = sc.nextInt();
                        this.comeback  = this.comebacksLearned[this.comebackId-1];

                        try {
                            this.datagram.write_comeback(this.comeback);
                        } catch (IOException e) {
                            System.out.println("ERROR");
                        }

                        this.state = 6;

                    }

            }else if(this.state == 6){

                //Comprobar si el comeback o insulto es correcto.

            }
        }
    }


}
