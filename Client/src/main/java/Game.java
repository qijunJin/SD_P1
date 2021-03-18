import enumType.ErrorType;
import enumType.ShoutType;
import enumType.StateType;
import exception.EmptyHashException;
import exception.OpcodeException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class Game {
    private DatabaseProvider dp;
    private Datagram datagram;
    private Menu menu;
    private int mode;

    String clientShout, serverShout;
    private boolean gameBool; // Infinite loop
    private StateType state;

    private String insult, comeback;
    private String opponentInsult, opponentComeback;
    private boolean contained;
    private Player client = new Player(); // Client data
    private Player server = new Player(); // Server data
    private ErrorType errorType;

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

                case HELLO:

                    /* NEW CLIENT OR NOT */
                    if (!this.client.hasName() && !this.client.hasId()) { // New client & get data

                        this.client.setName(this.menu.getName());
                        this.client.setId(this.menu.getId());

                    } else { // Already playing client & get data to check

                        String name = this.menu.getName();
                        int id = this.menu.getId();

                        if (!this.client.hasSameName(name) || !this.client.hasSameId(id)) { // Check if maintain the same name & id

                            this.client.setName(name);
                            this.client.setId(id);
                            this.client.removeInsultsComebacks(); // Remove all insults and comebacks
                            this.dp = new DatabaseProvider(); // Restart databaseProvider
                        }

                    }

                    /* ADD RANDOM INSULT-COMEBACK */
                    do { // Check if already contains and always add pair of insults/comebacks
                        contained = this.client.containsWithAddInsultComeback(this.dp.getRandomInsultComeback());
                    } while (contained); // If already contains, get new pairs

                    /* WRITE HELLO */
                    try {
                        this.datagram.write_hello(this.client.getId(), this.client.getName());
                    } catch (IOException e) {
                        System.out.println("Hello Error Write " + e.getMessage());
                        System.exit(1);
                    }

                    /* READ HELLO */
                    try {
                        this.server.setName(this.datagram.read_hello());
                        this.server.setId(this.datagram.getIdOpponent());
                    } catch (IOException | OpcodeException e) {
                        System.out.println("Hello Error Read " + e.getMessage());
                        System.exit(1);
                    }

                    /* SYSTEM OUTPUT */
                    System.out.println("C- HELLO: " + this.client.getId() + " " + this.client.getName());
                    System.out.println("S- HELLO: " + this.server.getId() + " " + this.server.getName());

                    this.state = StateType.HASH;
                    break;

                case HASH:

                    /* ADD RANDOM INSULT-COMEBACK */
                    do {
                        contained = this.client.containsWithAddInsultComeback(this.dp.getRandomInsultComeback());
                    } while (contained);

                    /* WRITE HASH */
                    try {
                        this.datagram.write_hash(this.client.generateSecret());
                        this.client.setHash(this.datagram.getHash(this.client.getSecret()));
                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                        System.exit(1);
                    }

                    /* READ HASH */
                    try {
                        this.server.setHash(this.datagram.read_hash());
                    } catch (IOException | OpcodeException | EmptyHashException e) {
                        System.out.println(e.getMessage());
                        System.exit(1);
                    }

                    /* SYSTEM OUTPUT */
                    System.out.println("C- HASH: " + Arrays.toString(this.client.getHash()));
                    System.out.println("S- HASH: " + Arrays.toString(this.server.getHash()));

                    this.state = StateType.SECRET;
                    break;

                case SECRET:

                    /* WRITE SECRET */
                    try {
                        this.datagram.write_secret(this.client.getSecret());
                    } catch (IOException e) {
                        System.out.println("ERROR SECRET");
                        System.exit(1);
                    }

                    /* READ SECRET */
                    try {
                        this.server.setSecret(this.datagram.read_secret());
                    } catch (IOException | OpcodeException e) {
                        System.out.println(e.getMessage());
                        System.exit(1);
                    }

                    /* PROOF HASH - NOT EQUAL ID - EVEN/ODD ^ GREATER/LESSER -> DECIDE STATE */
                    if (this.proofHash(this.server.getSecret(), this.server.getHash())) {
                        if (this.client.getId() != this.server.getId()) {
                            if (isEven(client.getSecret(), server.getSecret()) ^ (client.getId() > server.getId())) {
                                this.state = StateType.INSULT;
                            } else {
                                this.state = StateType.COMEBACK;
                            }
                        } else {
                            System.out.println("C- ERROR SAME ID");
                            this.errorType = ErrorType.INCOMPLETE_MESSAGE;
                            this.state = StateType.ERROR;
                        }
                    } else {
                        System.out.println("C- ERROR NOT COINCIDENT HASH");
                        this.errorType = ErrorType.INCOMPLETE_MESSAGE;
                        this.state = StateType.ERROR;
                    }

                    /* SYSTEM OUTPUT */
                    System.out.println("C- SECRET: " + this.client.getSecret());
                    System.out.println("S- SECRET: " + this.server.getSecret());

                    break;

                case INSULT:

                    /* CONDITION OF WIN GAME - WIN DUEL */
                    if (this.client.getDuel() == 3 || this.server.getDuel() == 3) { // Check if someone win game
                        this.state = StateType.SHOUT;
                    } else {

                        if (this.client.getRound() == 2 || this.server.getRound() == 2) { // Check if someone win duel
                            this.state = StateType.SHOUT;
                        } else {

                            /* SYSTEM OUTPUT */
                            System.out.println("------------------------------------------------------------------------------------");

                            /* SHOW & SELECT INSULT */
                            this.menu.showInsults(this.client.getInsults());
                            this.insult = this.client.getInsults().get(this.menu.getOption());

                            /* WRITE INSULT */
                            try {
                                this.datagram.write_insult(this.insult);
                            } catch (IOException e) {
                                System.out.println("ERROR");
                                System.exit(1);
                            }

                            /* READ COMEBACK */
                            try {
                                this.opponentComeback = this.datagram.read_comeback();
                            } catch (IOException | OpcodeException e) {
                                System.out.println("ERROR");
                                System.exit(1);
                            }

                            this.client.addComeback(this.opponentComeback); // Add comeback as learned

                            System.out.println("INSULT: " + this.insult);
                            System.out.println("COMEBACK: " + this.opponentComeback);
                            System.out.println("------------------------------------------------------------------------------------");

                            /* CHECK INSULT - COMEBACK WINNER */
                            if (this.dp.isRightComeback(this.insult, this.opponentComeback)) {
                                this.server.addRound();
                                this.state = StateType.COMEBACK;
                            } else {
                                this.client.addRound();
                            }
                        }
                    }

                    break;

                case COMEBACK:

                    /* CONDITION OF WIN GAME - WIN DUEL */
                    if (this.client.getDuel() == 3 || this.server.getDuel() == 3) {
                        this.state = StateType.SHOUT;
                    } else {

                        if (this.client.getRound() == 2 || this.server.getRound() == 2) {
                            this.state = StateType.SHOUT;
                        } else {

                            /* READ INSULT */
                            try {
                                this.opponentInsult = this.datagram.read_insult();
                            } catch (IOException | OpcodeException e) {
                                System.out.println("ERROR");
                                System.exit(1);
                            }

                            /* SYSTEM OUTPUT */
                            System.out.println("------------------------------------------------------------------------------------");
                            System.out.println("INSULT: " + this.opponentInsult);
                            this.client.addInsult(this.opponentInsult); // Add opponent comeback as learned

                            /* SHOW & SELECT COMEBACK */
                            this.menu.showComebacks(this.client.getComebacks());
                            this.comeback = this.client.getComebacks().get(this.menu.getOption());

                            /* WRITE COMEBACK */
                            try {
                                this.datagram.write_comeback(this.comeback);
                            } catch (IOException e) {
                                System.out.println("ERROR");
                            }

                            System.out.println("COMEBACK: " + this.comeback);
                            System.out.println("------------------------------------------------------------------------------------");

                            /* CHECK INSULT - COMEBACK WINNER */
                            if (this.dp.isRightComeback(this.opponentInsult, this.comeback)) {
                                this.client.addRound();
                                this.state = StateType.INSULT;
                            } else {
                                this.server.addRound();
                            }
                        }
                    }

                    break;

                case SHOUT:

                    /* CONDITION OF ADD DUEL */
                    if (this.client.getRound() == 2) {
                        this.client.addDuel();
                    } else if (this.server.getRound() == 2) {
                        this.server.addDuel();
                    }

                    /* CONDITION OF WIN GAME - WIN DUEL */
                    if (this.client.getDuel() == 3 | this.client.getRound() == 2) { // Check if client wins

                        /* WRITE SHOUT */
                        try {
                            clientShout = this.dp.getShoutByEnumAddName(ShoutType.I_WIN, this.server.getName());
                            this.datagram.write_shout(clientShout);
                        } catch (IOException e) {
                            System.out.println("ERROR SHOUT");
                            System.exit(1);
                        }

                        /* READ SHOUT */
                        try {
                            serverShout = this.datagram.read_shout();
                        } catch (IOException | OpcodeException e) {
                            System.out.println("ERROR SHOUT");
                            System.exit(1);
                        }

                        /* WIN GAME */
                        if (this.client.getDuel() == 3) {
                            this.client.resetDuelRound();
                            this.server.resetDuelRound();
                            System.out.println("New Game");
                            this.state = StateType.HELLO;

                            /* WIN DUEL */
                        } else {
                            this.client.resetRound();
                            this.server.resetRound();
                            this.state = StateType.HASH;
                        }

                    } else if (this.server.getDuel() == 3 | this.server.getRound() == 2) { // Check if server wins

                        /* WRITE SHOUT */
                        try {
                            clientShout = this.dp.getShoutByEnumAddName(ShoutType.YOU_WIN, this.server.getName());
                            this.datagram.write_shout(clientShout);
                        } catch (IOException e) {
                            System.out.println("ERROR SHOUT");
                            System.exit(1);
                        }

                        /* READ SHOUT */
                        try {
                            serverShout = this.datagram.read_shout();
                        } catch (IOException | OpcodeException e) {
                            System.out.println("ERROR SHOUT");
                            System.exit(1);
                        }

                        /* WIN DUEL */
                        if (this.server.getDuel() == 3) {
                            this.client.resetDuelRound();
                            this.server.resetDuelRound();
                            System.out.println("New Game");
                            this.state = StateType.HELLO;

                            /* WIN ROUND */
                        } else {
                            this.client.resetRound();
                            this.server.resetRound();
                            this.state = StateType.HASH;
                        }

                    }

                    /* SYSTEM OUTPUT */
                    System.out.println("C- SHOUT: " + clientShout);
                    System.out.println("S- SHOUT: " + serverShout + "\n");

                    break;

                case ERROR:

                    String errorMessage = this.dp.getErrorByEnum(this.errorType);
                    try {
                        this.datagram.write_error(errorMessage);
                    } catch (IOException e) {
                        System.out.println("ERROR");
                        System.exit(1);
                    }

                    break;
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
