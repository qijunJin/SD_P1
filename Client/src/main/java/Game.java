import enumType.ShoutType;
import enumType.StateType;
import exception.EmptyHashException;
import exception.OpcodeException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;

public class Game {
    private DatabaseProvider dp;
    private Datagram datagram;
    private Menu menu;
    private int mode;

    private boolean gameBool, check;                         //Infinite loop
    private StateType state;

    private String insult, comeback;
    private String opponentInsult, opponentComeback;

    private Player player1 = new Player();           //Data Client
    private Player player2 = new Player();           //Data Opponent

    private String name = "";
    private int id = 0;


    public Game(Datagram datagram, int mode) throws IOException {

        this.dp = new DatabaseProvider();
        this.datagram = datagram;
        this.menu = new Menu();
        this.gameBool = true;
        this.mode = mode;
        this.state = StateType.HELLO;
        this.run();

    }

    private void run() {
        while (gameBool) {

            switch (this.state) {

                case HELLO:              //HELLO message

                    if (this.name == "" && this.id == 0) {                     //Check if it's a new game                              //Check if it's a new player

                        this.name = this.menu.getName();                                              //Obtain player data
                        this.id = this.menu.getId();
                        this.player1.setName(this.name);
                        this.player1.setId(this.id);

                    }else{                                                     //Check if client is continuing the game

                        this.player1.setName(this.menu.getName());                                         //Obtain player data
                        this.player1.setId(this.menu.getId());

                        if (!this.name.equals(this.player1.getName()) || this.id != this.player1.getId()){       //Check if it's the same from last game id and name

                            //Remove all insults and comebacks
                            this.player1.removeInsultsComebacks();
                            this.dp = new DatabaseProvider();
                            this.name = this.player1.getName();
                            this.id = this.player1.getId();

                        }

                    }

                    ArrayList<String> list;
                    boolean extra = true;
                    do {

                        list = this.dp.getRandomInsultComeback();
                        extra = this.player1.containsInsultAndComeback(list);

                    }while(extra);

                    this.player1.addInsultComeback(list);
                    extra = true;

                    do {

                        list = this.dp.getRandomInsultComeback();
                        extra = this.player1.containsInsultAndComeback(list);

                    }while(extra);

                    this.player1.addInsultComeback(list);

                    try {
                        this.datagram.write_hello(this.player1.getId(), this.player1.getName());           //Write HELLO message
                    } catch (IOException e) {
                        System.out.println("Hello Error Write " + e.getMessage());
                        System.exit(1);
                    }

                    try {
                        this.player2.setName(this.datagram.read_hello());                                  //Read HELLO message
                        this.player2.setId(this.datagram.getIdOpponent());                                 //Obtain opponent data
                    } catch (IOException | OpcodeException e) {
                        System.out.println("Hello Error Read " + e.getMessage());
                        System.exit(1);
                    }

                    System.out.println("HELLO: " + this.player1.getId() + " " + this.player1.getName());
                    System.out.println("HELLO: " + this.player2.getId() + " " + this.player2.getName());

                    this.check = false;
                    this.state = StateType.HASH;                                                           //Change state to HASH

                    break;

                case HASH:               //HASH message

                    if (check) {
                        //For each duel, one new INSULT and COMEBACK
                        ArrayList<String> list2;
                        boolean extra2 = true;
                        do {

                            list2 = this.dp.getRandomInsultComeback();
                            extra2 = this.player1.containsInsultAndComeback(list2);

                        }while(extra2);

                        this.player1.addInsultComeback(list2);
                    }

                    try {
                        this.datagram.write_hash(this.player1.generateSecret());                           //Write HASH message
                        this.player1.setHash(this.datagram.getHash(this.player1.getSecret()));             //Save player1's HASH
                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                        System.exit(1);
                    }

                    try {
                        this.player2.setHash(this.datagram.read_hash());                                   //Read HASH message
                    } catch (IOException | OpcodeException | EmptyHashException e) {
                        System.out.println(e.getMessage());
                        System.exit(1);
                    }

                    System.out.println("HASH: " + Arrays.toString(this.player1.getHash()));
                    System.out.println("HASH: " + Arrays.toString(this.player2.getHash()));

                    this.check = true;
                    this.state = StateType.SECRET;                                                         //Change state to SECRET

                    break;

                case SECRET:            //SECRET message

                    try {
                        this.datagram.write_secret(this.player1.getSecret());                              //Write SECRET message
                    } catch (IOException e) {
                        System.out.println("ERROR SECRET");
                        System.exit(1);
                    }

                    try {
                        this.player2.setSecret(this.datagram.read_secret());                               //Read SECRET message
                    } catch (IOException | OpcodeException e) {
                        System.out.println(e.getMessage());
                        System.exit(1);
                    }

                    if (this.proofHash(this.player2.getSecret(), this.player2.getHash())) {                //Check correct HASH

                        if (this.player1.getId() != this.player2.getId()) {                                //Check not same ID

                            if (this.isEven(this.player1.getSecret(), this.player2.getSecret())) {         //Check EVEN or ODD

                                if (this.player1.getId() < this.player2.getId()) {
                                    this.state = StateType.INSULT;                                         //Change state to INSULT
                                } else {
                                    this.state = StateType.COMEBACK;                                       //Change state to COMEBACK
                                }

                            } else {

                                if (this.player1.getId() > this.player2.getId()) {
                                    this.state = StateType.INSULT;                                       //Change state to INSULT
                                } else {
                                    this.state = StateType.COMEBACK;                                     //Change state to COMEBACK
                                }

                            }

                            System.out.println("SECRET: " + this.player1.getSecret());
                            System.out.println("SECRET: " + this.player2.getSecret());

                        } else {                                                     //Same ID, ERROR

                            System.out.println("ERROR ID");

                            /*try {
                                this.datagram.write_error("ERROR ID");               //CODIGO INCOMPLETO
                            } catch (IOException e) {
                                System.out.println("ERROR");
                                System.exit(1);
                            }*/

                        }

                    } else {                                                         //Wrong HASH, ERROR

                        System.out.println("ERROR HASH");

                        /*try {
                            this.datagram.write_error("ERROR HASH");                 //CODIGO INCOMPLETO
                        } catch (IOException e) {
                            System.out.println("ERROR");
                            System.exit(1);
                        }*/

                    }

                    break;

                case INSULT:            //INSULT message

                    if (this.player1.getDuel() == 3 || this.player2.getDuel() == 3) {                  //Check if there's a winner

                        this.state = StateType.SHOUT;                                                  //Change state to SHOUT

                    }else {

                        if (this.player1.getRound() == 2 || this.player2.getRound() == 2) {            //Check if someone win duel

                            this.state = StateType.SHOUT;                                              //Change state to SHOUT

                        } else {

                            System.out.println("------------------------------------------------------------------------------------");
                            this.menu.showInsults(this.player1.getInsults());                         //Show INSULTS learned
                            this.insult = this.player1.getInsults().get(this.menu.getOption());       //Get INSULT selected

                            try {
                                this.datagram.write_insult(this.insult);                              //Write INSULT message
                            } catch (IOException e) {
                                System.out.println("ERROR");
                                System.exit(1);
                            }

                            try {
                                this.opponentComeback = this.datagram.read_comeback();                //Read COMEBACK message and get COMEBACK
                            } catch (IOException | OpcodeException e) {
                                System.out.println("ERROR");
                                System.exit(1);
                            }

                            this.player1.addComeback(this.opponentComeback);                          //Get opponent comeback as learned
                            System.out.println("INSULT: " + this.insult);
                            System.out.println("COMEBACK: " + this.opponentComeback);
                            System.out.println("------------------------------------------------------------------------------------");

                            if (this.dp.isRightComeback(this.insult, this.opponentComeback)) {        //Check who win the round

                                this.player2.addRound();
                                this.state = StateType.COMEBACK;

                            } else {

                                this.player1.addRound();

                            }
                        }
                    }

                    break;

                case COMEBACK:

                    if (this.player1.getDuel() == 3 || this.player2.getDuel() == 3) {                  //Check if there's a winner

                        this.state = StateType.SHOUT;                                                  //Change state to SHOUT

                    }else {

                        if (this.player1.getRound() == 2 || this.player2.getRound() == 2) {            //Check if someone win duel

                            this.state = StateType.SHOUT;                                              //Change state to SHOUT

                        } else {

                            try {
                                this.opponentInsult = this.datagram.read_insult();                            //Read COMEBACK message and get COMEBACK
                            } catch (IOException | OpcodeException e) {
                                System.out.println("ERROR");
                                System.exit(1);
                            }

                            System.out.println("------------------------------------------------------------------------------------");
                            System.out.println("INSULT: " + this.opponentInsult);
                            this.player1.addInsult(this.opponentInsult);                                      //Get opponent comeback as learned

                            this.menu.showComebacks(this.player1.getComebacks());                             //Show COMEBACKS learned
                            this.comeback = this.player1.getComebacks().get(this.menu.getOption());           //Get COMEBACK selected

                            try {
                                this.datagram.write_comeback(this.comeback);                                  //Write COMEBACK message
                            } catch (IOException e) {
                                System.out.println("ERROR");
                            }

                            System.out.println("COMEBACK: " + this.comeback);
                            System.out.println("------------------------------------------------------------------------------------");

                            if (this.dp.isRightComeback(this.opponentInsult, this.comeback)) {                //Check who win the round

                                this.player1.addRound();                                                      //Client win round
                                this.state = StateType.INSULT;                                                //Change state to INSULT

                            } else {

                                this.player2.addRound();                                                      //Server win round

                            }
                        }
                    }

                    break;

                case SHOUT:

                    String str = "";
                    String str2 = "";

                    if (this.player1.getRound() == 2) {

                        this.player1.addDuel();

                    } else if (this.player2.getRound() == 2){

                        this.player2.addDuel();

                    }

                    if (this.player1.getDuel() == 3 | this.player1.getRound() == 2) {                                   //Check if Server win something

                        if (this.player1.getDuel() == 3) {

                            try {
                                str = this.dp.getShoutByEnumAddName(ShoutType.I_WIN, this.player2.getName());               //Select SHOUT type message
                                this.datagram.write_shout(str);                                                            //Write SHOUT message
                            } catch (IOException e) {
                                System.out.println("ERROR SHOUT");
                                System.exit(1);
                            }

                            try {
                                str2 = this.datagram.read_shout();                                                          //Read SHOUT message and get message
                            } catch (IOException | OpcodeException e) {
                                System.out.println("ERROR SHOUT");
                                System.exit(1);
                            }

                            this.player1.resetDuelRound();                                                                  //Reset duels
                            this.player2.resetDuelRound();
                            System.out.println("New Game");
                            this.state = StateType.HELLO;

                        } else {

                            try {
                                str = this.dp.getShoutByEnumAddName(ShoutType.I_WIN, this.player2.getName());               //Select SHOUT type message
                                this.datagram.write_shout(str);                                                            //Write SHOUT message
                            } catch (IOException e) {
                                System.out.println("ERROR SHOUT");
                                System.exit(1);
                            }

                            try {
                                str2 = this.datagram.read_shout();                                                          //Read SHOUT message and get message
                            } catch (IOException | OpcodeException e) {
                                System.out.println("ERROR SHOUT");
                                System.exit(1);
                            }

                            this.player1.resetRound();                                                                  //Reset rounds
                            this.player2.resetRound();
                            this.state = StateType.HASH;

                        }

                    } else if (this.player2.getDuel() == 3 | this.player2.getRound() == 2) {

                        if (this.player2.getDuel() == 3) {

                            try {
                                str = this.dp.getShoutByEnumAddName(ShoutType.YOU_WIN, this.player2.getName());               //Select SHOUT type message
                                this.datagram.write_shout(str);                                                            //Write SHOUT message
                            } catch (IOException e) {
                                System.out.println("ERROR SHOUT");
                                System.exit(1);
                            }

                            try {
                                str2 = this.datagram.read_shout();                                                          //Read SHOUT message and get message
                            } catch (IOException | OpcodeException e) {
                                System.out.println("ERROR SHOUT");
                                System.exit(1);
                            }

                            this.player1.resetDuelRound();                                                                  //Reset duels
                            this.player2.resetDuelRound();
                            System.out.println("New Game");
                            this.state = StateType.HELLO;

                        } else {

                            try {
                                str = this.dp.getShoutByEnumAddName(ShoutType.YOU_WIN, this.player2.getName());               //Select SHOUT type message
                                this.datagram.write_shout(str);                                                            //Write SHOUT message
                            } catch (IOException e) {
                                System.out.println("ERROR SHOUT");
                                System.exit(1);
                            }

                            try {
                                str2 = this.datagram.read_shout();                                                          //Read SHOUT message and get message
                            } catch (IOException | OpcodeException e) {
                                System.out.println("ERROR SHOUT");
                                System.exit(1);
                            }

                            this.player1.resetRound();                                                                  //Reset rounds
                            this.player2.resetRound();
                            this.state = StateType.HASH;

                        }

                    }

                    System.out.println("SHOUT: " + str + "\n");
                    System.out.println("SHOUT: " + str2 + "\n");

                    break;

                case ERROR:


            }

        }
    }

    public boolean proofHash(String secret, byte[] hash) {

        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        byte[] encodedhash = digest.digest(
                secret.getBytes(StandardCharsets.UTF_8));

        return Arrays.equals(encodedhash, hash);

    }


    public boolean isEven(String s1, String s2) {
        int n1 = Integer.parseInt(s1);
        int n2 = Integer.parseInt(s2);
        return ((n1 + n2) % 2 == 0);
    }

}

                /*
                if (this.duel < 3) {
                    this.state = 5;                   //Seguimos jugando

                } else {

                    if (this.duelWins == 3 || this.opponentDuelWins == 3) {        //Se acaba la parida pq hay un ganador
                        if (this.duelWins == 3) {        //Ganamos la partida

                            try {
                                String str = this.database.getShoutByEnumAddName(ShoutType.I_WIN, this.opponentName);
                                this.datagram.write_shout(str);
                            } catch (IOException e) {
                                System.out.println("ERROR SHOUT");
                                System.exit(1);
                            }

                            try {
                                System.out.println(this.datagram.read_shout());
                            } catch (IOException | exception.DatagramException e) {
                                System.out.println("ERROR SHOUT");
                                System.exit(1);
                            }

                        } else {            //Perdemos la partida

                            try {
                                String str = this.database.getShoutByEnumAddName(ShoutType.YOU_WIN, this.name);
                                this.datagram.write_shout(str);
                            } catch (IOException e) {
                                System.out.println("ERROR SHOUT");
                                System.exit(1);
                            }

                            try {
                                System.out.println(this.datagram.read_shout());
                            } catch (IOException | exception.DatagramException e) {
                                System.out.println("ERROR");
                                System.exit(1);
                            }

                        }

                        //Reseteamos marcadores pq se acaba la partida
                        this.duel = 0;
                        this.round = 0;
                        this.duelWins = 0;
                        this.roundWins = 0;
                        this.opponentDuelWins = 0;
                        this.opponentRoundWins = 0;
                        this.state = 1;             //Al acabar una partida volvemos a enviar Hash

                    } else {
                        this.state = 5;                   //Seguimos jugando
                    }
                }

            } else if (this.state == 5) {            //Comporbacion de las rondas

                if (this.round < 2) {
                    this.state = 6;                 //Seguimos jugando

                } else {

                    if (this.roundWins == 2 || this.opponentRoundWins == 2) {     //Se acaba el duelo pq alguien ha ganado
                        if (this.roundWins == 2) {         //Ganamos el duelo

                            try {
                                String str = this.database.getShoutByEnumAddName(ShoutType.I_WIN, this.opponentName);
                                this.datagram.write_shout(str);
                            } catch (IOException e) {
                                System.out.println("ERROR");
                                System.exit(1);
                            }

                            try {
                                System.out.println(this.datagram.read_shout());
                            } catch (IOException | exception.DatagramException e) {
                                System.out.println("ERROR");
                                System.exit(1);
                            }

                            this.duelWins++;

                        } else {          //Perdemos el duelo

                            try {
                                String str = this.database.getShoutByEnumAddName(ShoutType.YOU_WIN, this.opponentName);
                                this.datagram.write_shout(str);
                            } catch (IOException e) {
                                System.out.println("ERROR");
                                System.exit(1);
                            }

                            try {
                                System.out.println(this.datagram.read_shout());
                            } catch (IOException | exception.DatagramException e) {
                                System.out.println("ERROR");
                                System.exit(1);
                            }

                            this.opponentDuelWins++;

                        }

                        //resetamos rondas pq pasamos a otro duelo
                        this.round = 0;
                        this.roundWins = 0;
                        this.opponentRoundWins = 0;
                        this.duel++;
                        this.state = 4;

                    } else {
                        this.state = 6;                 //Seguimos jugando
                    }

                }

            } else if (this.state == 6) {       //Envio de INSULTS y COMEBACKS

                if (this.domain) {  //Empieza el cliente insultando

                    this.menu.showInsults(insultsLearned);          //Mostramos insultos aprendidos
                    this.insult = this.insultsLearned.get(this.menu.getOption());
                    System.out.println(this.insult);

                    try {
                        this.datagram.write_insult(this.insult);
                    } catch (IOException e) {
                        System.out.println("ERROR");
                        System.exit(1);
                    }

                    try {
                        this.opponentComeback = this.datagram.read_comeback();
                    } catch (IOException | exception.DatagramException e) {
                        System.out.println("ERROR");
                        System.exit(1);
                    }

                    this.comebacksLearned.add(this.opponentComeback);
                    System.out.println(this.opponentComeback);

                    if (this.database.isRightComeback(this.insult, this.opponentComeback)) {  //Comporbamos quien gana la ronda
                        this.opponentRoundWins++;
                        this.round++;
                        this.domain = false;
                    } else {
                        this.roundWins++;
                        this.round++;
                    }

                } else {                   //Empieza el server insultando

                    try {
                        this.opponentInsult = this.datagram.read_insult();
                    } catch (IOException | exception.DatagramException e) {
                        System.out.println("ERROR");
                        System.exit(1);
                    }

                    this.insultsLearned.add(this.opponentInsult);
                    System.out.println(this.opponentInsult);
                    this.menu.showComebacks(comebacksLearned);          //Mostramos insultos aprendidos
                    this.comeback = this.comebacksLearned.get(this.menu.getOption());
                    System.out.println(this.comeback);

                    try {
                        this.datagram.write_comeback(this.comeback);
                    } catch (IOException e) {
                        System.out.println("ERROR");
                    }

                    if (this.database.isRightComeback(this.opponentInsult, this.comeback)) {  //Comporbamos quien gana la ronda
                        this.roundWins++;
                        this.round++;
                        this.domain = true;
                    } else {
                        this.opponentRoundWins++;
                        this.round++;
                    }

                }
                this.state = 4;
*/
