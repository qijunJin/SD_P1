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

    private boolean gameBool;                         // Infinite loop
    private StateType state;

    private String insult, comeback;
    private String opponentInsult, opponentComeback;

    private Player client;           // Data Client
    private Player server;           // Data Opponent


    public Game(Datagram datagram, int mode) throws IOException {

        this.datagram = datagram;
        this.mode = mode;

        this.menu = new Menu();
        this.client = new Player();
        this.server = new Player();

        this.state = StateType.HELLO;
        this.gameBool = true;

        this.run();

    }

    private void run() {
        while (gameBool) {
            switch (this.state) {
                case HELLO:

                    this.dp = new DatabaseProvider();

                    this.client.setName(this.menu.getName());                                              // Obtain player data
                    this.client.setId(this.menu.getId());

                    try {
                        this.datagram.write_hello(this.client.getId(), this.client.getName());            // Write HELLO message
                    } catch (IOException e) {
                        System.out.println("ERROR ON HELLO STATE - WRITE: " + e.getMessage());
                        System.exit(1);
                    }

                    try {
                        this.server.setName(this.datagram.read_hello());                                  // Read HELLO message
                        this.server.setId(this.datagram.getIdOpponent());                                 // Obtain opponent data
                    } catch (IOException | OpcodeException e) {
                        System.out.println("ERROR ON HELLO STATE - READ: " + e.getMessage());
                        System.exit(1);
                    }

                    System.out.println("C- HELLO: " + this.client.getId() + " " + this.client.getName());
                    System.out.println("S- HELLO: " + this.server.getId() + " " + this.server.getName());

                    this.state = StateType.HASH;                                                           // Change state to HASH

                    break;

                case HASH:               // HASH message

                    // For each game, two new INSULTS and COMEBACKS
                    if (!this.dp.isEmptyInsults()) {
                        this.client.addInsultComeback(this.dp.getRandomInsultComeback());
                        this.client.addInsultComeback(this.dp.getRandomInsultComeback());
                    }

                    try {
                        this.datagram.write_hash(this.client.generateSecret());                           // Write HASH message
                        this.client.setHash(this.datagram.getHash(this.client.getSecret()));             // Save player1's HASH
                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                        System.exit(1);
                    }

                    try {
                        this.server.setHash(this.datagram.read_hash());                                   // Read HASH message
                    } catch (IOException | OpcodeException | EmptyHashException e) {
                        System.out.println(e.getMessage());
                        System.exit(1);
                    }

                    System.out.println("HASH: " + Arrays.toString(this.client.getHash()));
                    System.out.println("HASH: " + Arrays.toString(this.server.getHash()));

                    this.state = StateType.SECRET;                                                         // Change state to SECRET

                    break;

                case SECRET:            // SECRET message

                    try {
                        this.datagram.write_secret(this.client.getSecret());                              // Write SECRET message
                    } catch (IOException e) {
                        System.out.println("ERROR SECRET");
                        System.exit(1);
                    }

                    try {
                        this.server.setSecret(this.datagram.read_secret());                               // Read SECRET message
                    } catch (IOException | OpcodeException e) {
                        System.out.println(e.getMessage());
                        System.exit(1);
                    }

                    if (this.proofHash(this.server.getSecret(), this.server.getHash())) {                // Check correct HASH

                        if (this.client.getId() != this.server.getId()) {                                // Check not same ID

                            if (this.isEven(this.client.getSecret(), this.server.getSecret())) {         // Check EVEN or ODD

                                if (this.client.getId() < this.server.getId()) {
                                    this.state = StateType.INSULT;                                         // Change state to INSULT
                                } else {
                                    this.state = StateType.COMEBACK;                                       // Change state to COMEBACK
                                }

                            } else {

                                if (this.client.getId() > this.server.getId()) {
                                    this.state = StateType.INSULT;                                       // Change state to INSULT
                                } else {
                                    this.state = StateType.COMEBACK;                                     // Change state to COMEBACK
                                }

                            }

                            System.out.println("SECRET: " + this.client.getSecret());
                            System.out.println("SECRET: " + this.server.getSecret());

                        } else {                                                     // Same ID, ERROR

                            System.out.println("ERROR ID");

                            /*try {
                                this.datagram.write_error("ERROR ID");               // CODIGO INCOMPLETO
                            } catch (IOException e) {
                                System.out.println("ERROR");
                                System.exit(1);
                            }*/

                        }

                    } else {                                                         // Wrong HASH, ERROR

                        System.out.println("ERROR HASH");

                        /*try {
                            this.datagram.write_error("ERROR HASH");                 // CODIGO INCOMPLETO
                        } catch (IOException e) {
                            System.out.println("ERROR");
                            System.exit(1);
                        }*/

                    }

                    break;

                case INSULT:            // INSULT message

                    if (this.client.getDuel() == 3 || this.server.getDuel() == 3) {                  // Check if there's a winner

                        this.state = StateType.SHOUT;                                                  // Change state to SHOUT

                    } else {

                        if (this.client.getRound() == 2 || this.server.getRound() == 2) {            // Check if someone win duel

                            this.state = StateType.SHOUT;                                              // Change state to SHOUT

                        } else {

                            this.menu.showInsults(this.client.getInsults());                         // Show INSULTS learned
                            this.insult = this.client.getInsults().get(this.menu.getOption());       // Get INSULT selected

                            try {
                                this.datagram.write_insult(this.insult);                              // Write INSULT message
                            } catch (IOException e) {
                                System.out.println("ERROR");
                                System.exit(1);
                            }

                            try {
                                this.opponentComeback = this.datagram.read_comeback();                // Read COMEBACK message and get COMEBACK
                            } catch (IOException | OpcodeException e) {
                                System.out.println("ERROR");
                                System.exit(1);
                            }

                            this.client.addComeback(this.opponentComeback);                          // Get opponent comeback as learned
                            System.out.println("INSULT: " + this.insult);
                            System.out.println("COMEBACK: " + this.opponentComeback);

                            if (this.dp.isRightComeback(this.insult, this.opponentComeback)) {        // Check who win the round

                                this.server.addRound();
                                this.state = StateType.COMEBACK;

                            } else {

                                this.client.addRound();

                            }
                        }
                    }

                    break;

                case COMEBACK:

                    if (this.client.getDuel() == 3 || this.server.getDuel() == 3) {                  // Check if there's a winner

                        this.state = StateType.SHOUT;                                                  // Change state to SHOUT

                    } else {

                        if (this.client.getRound() == 2 || this.server.getRound() == 2) {            // Check if someone win duel

                            this.state = StateType.SHOUT;                                              // Change state to SHOUT

                        } else {

                            try {
                                this.opponentInsult = this.datagram.read_insult();                            // Read COMEBACK message and get COMEBACK
                            } catch (IOException | OpcodeException e) {
                                System.out.println("ERROR");
                                System.exit(1);
                            }

                            System.out.println(this.opponentInsult);
                            this.client.addInsult(this.opponentInsult);                                      // Get opponent comeback as learned

                            this.menu.showComebacks(this.client.getComebacks());                             // Show COMEBACKS learned
                            this.comeback = this.client.getComebacks().get(this.menu.getOption());           // Get COMEBACK selected

                            try {
                                this.datagram.write_comeback(this.comeback);                                  // Write COMEBACK message
                            } catch (IOException e) {
                                System.out.println("ERROR");
                            }

                            System.out.println("INSULT: " + this.opponentInsult);
                            System.out.println("COMEBACK: " + this.comeback);

                            if (this.dp.isRightComeback(this.opponentInsult, this.comeback)) {                // Check who win the round

                                this.client.addRound();                                                      // Client win round
                                this.state = StateType.INSULT;                                                // Change state to INSULT

                            } else {

                                this.server.addRound();                                                      // Server win round

                            }
                        }
                    }

                    break;

                case SHOUT:

                    if (this.client.getDuel() == 3 | this.client.getRound() == 2) {                                   // Check if Client win something

                        String str = "";
                        String str2 = "";

                        try {
                            str = this.dp.getShoutByEnumAddName(ShoutType.I_WIN, this.server.getName());               // Select SHOUT type message
                            this.datagram.write_shout(str);                                                             // Write SHOUT message
                        } catch (IOException e) {
                            System.out.println("ERROR SHOUT");
                            System.exit(1);
                        }

                        try {
                            str2 = this.datagram.read_shout();                                                          // Read SHOUT message and get message
                        } catch (IOException | OpcodeException e) {
                            System.out.println("ERROR SHOUT");
                            System.exit(1);
                        }

                        System.out.println("SHOUT: " + str);
                        System.out.println("SHOUT: " + str2);

                    } else if (this.server.getDuel() == 3 | this.server.getRound() == 2) {                            // Check if opponent win something

                        String str = "";
                        String str2 = "";

                        try {
                            str = this.dp.getShoutByEnumAddName(ShoutType.YOU_WIN, this.server.getName());             // Select SHOUT type message
                            this.datagram.write_shout(str);                                                             // Write SHOUT message
                        } catch (IOException e) {
                            System.out.println("ERROR SHOUT");
                            System.exit(1);
                        }

                        try {
                            str2 = this.datagram.read_shout();                                                          // Read SHOUT message and get message
                        } catch (IOException | OpcodeException e) {
                            System.out.println("ERROR");
                            System.exit(1);
                        }

                        System.out.println("SHOUT: " + str);
                        System.out.println("SHOUT: " + str2);

                    }

                    if (this.client.getRound() == 2 | this.server.getRound() == 2) {                                  // Check round points

                        // Change state to INSULT
                        // Reset rounds
                        if (this.client.getRound() == 2) {                                                             // Check client's round point

                            this.client.addDuel();                                                                     // Client win duel

                        } else {

                            this.server.addDuel();                                                                     // Opponent win duel

                        }
                        this.client.resetRound();                                                                  // Reset rounds
                        this.server.resetRound();
                        state = StateType.HASH;                                                                   // Change state to INSULT
                    }

                    if (this.client.getDuel() == 3 | this.server.getDuel() == 3) {                                    // Check duel points

                        this.client.resetDuelRound();                                                                  // Reset duels
                        this.server.resetDuelRound();
                        System.out.println("New Game");

                        this.state = StateType.HASH;                                                                    // Change state to HASH

                    }

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
                    this.state = 5;                   // Seguimos jugando

                } else {

                    if (this.duelWins == 3 || this.opponentDuelWins == 3) {        // Se acaba la parida pq hay un ganador
                        if (this.duelWins == 3) {        // Ganamos la partida

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

                        } else {            // Perdemos la partida

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

                        // Reseteamos marcadores pq se acaba la partida
                        this.duel = 0;
                        this.round = 0;
                        this.duelWins = 0;
                        this.roundWins = 0;
                        this.opponentDuelWins = 0;
                        this.opponentRoundWins = 0;
                        this.state = 1;             // Al acabar una partida volvemos a enviar Hash

                    } else {
                        this.state = 5;                   // Seguimos jugando
                    }
                }

            } else if (this.state == 5) {            // Comporbacion de las rondas

                if (this.round < 2) {
                    this.state = 6;                 // Seguimos jugando

                } else {

                    if (this.roundWins == 2 || this.opponentRoundWins == 2) {     // Se acaba el duelo pq alguien ha ganado
                        if (this.roundWins == 2) {         // Ganamos el duelo

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

                        } else {          // Perdemos el duelo

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

                        // resetamos rondas pq pasamos a otro duelo
                        this.round = 0;
                        this.roundWins = 0;
                        this.opponentRoundWins = 0;
                        this.duel++;
                        this.state = 4;

                    } else {
                        this.state = 6;                 // Seguimos jugando
                    }

                }

            } else if (this.state == 6) {       // Envio de INSULTS y COMEBACKS

                if (this.domain) {  // Empieza el cliente insultando

                    this.menu.showInsults(insultsLearned);          // Mostramos insultos aprendidos
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

                    if (this.database.isRightComeback(this.insult, this.opponentComeback)) {  // Comporbamos quien gana la ronda
                        this.opponentRoundWins++;
                        this.round++;
                        this.domain = false;
                    } else {
                        this.roundWins++;
                        this.round++;
                    }

                } else {                   // Empieza el server insultando

                    try {
                        this.opponentInsult = this.datagram.read_insult();
                    } catch (IOException | exception.DatagramException e) {
                        System.out.println("ERROR");
                        System.exit(1);
                    }

                    this.insultsLearned.add(this.opponentInsult);
                    System.out.println(this.opponentInsult);
                    this.menu.showComebacks(comebacksLearned);          // Mostramos insultos aprendidos
                    this.comeback = this.comebacksLearned.get(this.menu.getOption());
                    System.out.println(this.comeback);

                    try {
                        this.datagram.write_comeback(this.comeback);
                    } catch (IOException e) {
                        System.out.println("ERROR");
                    }

                    if (this.database.isRightComeback(this.opponentInsult, this.comeback)) {  // Comporbamos quien gana la ronda
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
