import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class Game {
    private Datagram datagram;
    private Database data = new Database();
    private Menu menu;
    private int mode;
    private int state;
    private boolean gameBool = true;      //Bucle infinito para el game
    private boolean dominio = false;                   //true->Client   false->Server
    private Scanner sc = new Scanner(System.in);

    private String name, oponentName;
    private int id, oponentId;
    private byte[] oponentHash;
    private String secret, oponentSecret;

    private int duel = 0;
    private int round = 0;
    private int duelWins = 0;
    private int roundWins = 0;
    private int oponentDuelWins = 0;
    private int oponentRoundWins = 0;

    private ArrayList<String> insultsLearned = new ArrayList<String>();      //Aqui guardamos los insultos que aprendemos
    private ArrayList<String> comebacksLearned = new ArrayList<String>();    //Aqui guardamos los comebacks que aprendemos

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


                data.getRandomInsultComeback();
                this.insultsLearned.addAll(data.getRandomInsults());
                this.comebacksLearned.addAll(data.getRandomComebacks());

                Random rand = new Random();
                this.name = this.menu.getName();
                this.id = rand.nextInt((int) Math.pow((double) 2, 31));  //Id aleatorio

                try {
                    this.datagram.write_hello(this.id, this.name);
                } catch (IOException e) {
                    System.out.println("ERROR HELLO");
                    System.exit(1);
                }

                try{
                    this.oponentName = this.datagram.read_hello();
                } catch (IOException e) {
                    System.out.println("ERROR HELLO");
                    System.exit(1);
                }

                this.oponentId = this.datagram.getIdOponent();
                this.state = 1;

            }else if(state == 1){         //Envio de HASH entre jugadores


                this.secret = this.menu.getSecret();

                try {
                    this.datagram.writeHash(this.secret);
                } catch (IOException e) {
                    System.out.println("ERROR HASH");
                    System.exit(1);
                }

                try{
                    this.oponentHash = this.datagram.read_hash();
                } catch (IOException e) {
                    System.out.println("ERROR HASH");
                    System.exit(1);
                }

                this.state = 2;

            }else if(state == 2){            //Envio de SECRET entre jugadores

                try {
                    this.datagram.write_secret(this.secret);
                } catch (IOException e) {
                    System.out.println("ERROR SECRET");
                    System.exit(1);
                }

                try{
                    this.oponentSecret = this.datagram.read_secret();
                } catch (IOException e) {
                    System.out.println("ERROR SECRET");
                    System.exit(1);
                }

                this.state = 3;

            }else if(state == 3){         //Comprobación de HASH correcto y elección de quien comienza el juego.

                if(this.datagram.proofHash(this.oponentSecret, this.oponentHash)){
                    if(this.id != this.oponentId){
                        if(this.datagram.isEven(this.secret, this.oponentSecret)){
                            if (this.id < this.oponentId){
                                dominio = true;  //Cliente
                            }else{
                                dominio = false;  //Server
                            }
                        }else{
                            if (this.id > this.oponentId){
                                dominio = false;  //Server
                            }else{
                                dominio = true;  //Cliente
                            }
                        }
                        this.state = 4;
                    }else{
                        try {
                            this.datagram.write_error("ERROR ID");
                        } catch (IOException e) {
                            System.out.println("ERROR ID");
                            System.exit(1);
                        }
                    }
                }else{
                    try {
                        this.datagram.write_error("ERROR HASH");
                    } catch (IOException e) {
                        System.out.println("ERROR HASH");
                        System.exit(1);
                    }
                }


            }else if (state == 4) {                   //Comprobación de los duelos

                if (this.duel < 3) {
                    this.state = 5;                   //Seguimos jugando

                } else {

                    if (this.duelWins == 3 || this.oponentDuelWins == 3) {        //Se acaba la parida pq hay un ganador
                        if (this.duelWins == 3) {        //Ganamos la partida

                            try {
                                this.datagram.write_shout("¡He ganado, " + this.oponentName + " !");
                            } catch (IOException e) {
                                System.out.println("ERROR SHOUT");
                                System.exit(1);
                            }

                            try{
                                System.out.println(this.datagram.read_shout());
                            } catch (IOException e) {
                                System.out.println("ERROR SHOUT");
                                System.exit(1);
                            }

                        } else {            //Perdemos la partida

                            try {
                                this.datagram.write_shout("¡Has ganado, " + this.oponentName + " !");
                            } catch (IOException e) {
                                System.out.println("ERROR SHOUT");
                                System.exit(1);
                            }

                            try{
                                System.out.println(this.datagram.read_shout());
                            } catch (IOException e) {
                                System.out.println("ERROR");
                                System.exit(1);
                            }

                        }

                        //Reseteamos marcadores pq se acaba la partida
                        this.duel = 0;
                        this.round = 0;
                        this.duelWins = 0;
                        this.roundWins = 0;
                        this.oponentDuelWins = 0;
                        this.oponentRoundWins = 0;
                        this.state = 1;             //Al acabar una partida volvemos a enviar Hash

                    }else{
                        this.state = 5;                   //Seguimos jugando
                    }
                }

            }else if (this.state == 5) {            //Comporbacion de las rondas

                if (this.round < 2) {
                    this.state = 6;                 //Seguimos jugando

                } else {

                    if (this.roundWins == 2 || this.oponentRoundWins == 2) {     //Se acaba el duelo pq alguien ha ganado
                        if (this.roundWins == 2) {         //Ganamos el duelo

                            try {
                                this.datagram.write_shout("¡He ganado, " + this.oponentName + " !");
                            } catch (IOException e) {
                                System.out.println("ERROR");
                                System.exit(1);
                            }

                            try{
                                System.out.println(this.datagram.read_shout());
                            } catch (IOException e) {
                                System.out.println("ERROR");
                                System.exit(1);
                            }

                            this.duelWins++;

                        }else {          //Perdemos el duelo

                            try {
                                this.datagram.write_shout("¡Has ganado, " + this.oponentName + " !");
                            } catch (IOException e) {
                                System.out.println("ERROR");
                                System.exit(1);
                            }

                            try{
                                System.out.println(this.datagram.read_shout());
                            } catch (IOException e) {
                                System.out.println("ERROR");
                                System.exit(1);
                            }

                            this.oponentDuelWins++;

                        }

                        //resetamos rondas pq pasamos a otro duelo
                        this.round = 0;
                        this.roundWins = 0;
                        this.oponentRoundWins = 0;
                        this.duel++;
                        this.state = 4;

                    }else{
                        this.state = 6;                 //Seguimos jugando
                    }

                }

            }else if (this.state == 6){       //Envio de INSULTS y COMEBACKS

                if (this.dominio) {  //Empieza el cliente insultando

                    this.menu.showInsults(insultsLearned);          //Mostramos insultos aprendidos
                    this.insult  = this.insultsLearned.get(this.menu.getOption());
                    System.out.println(this.insult);

                    try {
                        this.datagram.write_insult(this.insult);
                    } catch (IOException e) {
                        System.out.println("ERROR");
                        System.exit(1);
                    }

                    try{
                        this.oponentComeback = this.datagram.read_comeback();
                    } catch (IOException e) {
                        System.out.println("ERROR");
                        System.exit(1);
                    }

                        this.comebacksLearned.add(this.oponentComeback);
                        System.out.println(this.oponentComeback);

                    if (this.data.isRightComeback(this.insult, this.oponentComeback)){  //Comporbamos quien gana la ronda
                        this.oponentRoundWins++;
                        this.round++;
                        this.dominio = false;
                    }else{
                        this.roundWins++;
                        this.round++;
                    }

                }else{                   //Empieza el server insultando

                    try {
                        this.oponentInsult = this.datagram.read_insult();
                    } catch (IOException e) {
                        System.out.println("ERROR");
                        System.exit(1);
                    }

                    this.insultsLearned.add(this.oponentInsult);
                    System.out.println(this.oponentInsult);
                    this.menu.showComebacks(comebacksLearned);          //Mostramos insultos aprendidos
                    this.comeback  = this.comebacksLearned.get(this.menu.getOption());
                    System.out.println(this.comeback);

                    try {
                        this.datagram.write_comeback(this.comeback);
                    } catch (IOException e) {
                        System.out.println("ERROR");
                    }

                    if (this.data.isRightComeback(this.oponentInsult, this.comeback)){  //Comporbamos quien gana la ronda
                        this.roundWins++;
                        this.round++;
                        this.dominio = true;
                    }else{
                        this.oponentRoundWins++;
                        this.round++;
                    }

                }
                this.state = 4;

            }
        }
    }


}
