import enumType.ErrorType;
import enumType.ShoutType;
import enumType.StateType;
import exception.OpcodeException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class Game {
    private DatabaseProvider dp;
    private Database database;
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

        this.database = new Database();
        this.dp = new DatabaseProvider(database.getInsults(), database.getComebacks());
        this.state = StateType.HELLO;
        this.datagram = datagram;
        this.menu = new Menu();
        this.gameBool = true;
        this.mode = mode;
        this.run(mode);

    }

    private void run(int mode) {
        if (mode == 1) {
            this.automaticMode();
        }
        if (mode == 0) {
            this.manualMode();
        }
    }

    private void setGameBool(boolean b) {
        this.gameBool = b;
    }

    public void manualMode() {
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
                            this.client.resetInsultsComebacks(); // Remove all insults and comebacks
                            this.dp = new DatabaseProvider(this.database.getInsults(), this.database.getComebacks()); // Renew databaseProvider
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
                    }

                    /* READ HELLO */
                    try {
                        this.server.setName(this.datagram.read_hello());
                        this.server.setId(this.datagram.getIdOpponent());
                    } catch (IOException | OpcodeException e) {
                        System.out.println("Hello Error Read " + e.getMessage());
                        this.errorType = ErrorType.WRONG_OPCODE;
                        this.state = StateType.ERROR;
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
                        this.client.setHash(this.getHash(this.client.getSecret()));
                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                    }

                    /* READ HASH */
                    try {
                        this.server.setHash(this.datagram.read_hash());
                    } catch (IOException | OpcodeException e) {
                        System.out.println(e.getMessage());
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
                    }

                    /* READ SECRET */
                    try {
                        this.server.setSecret(this.datagram.read_secret());
                    } catch (IOException | OpcodeException e) {
                        System.out.println(e.getMessage());
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

                            /* SHOW & SELECT INSULT */
                            this.insult = this.menu.getOption(this.client.getInsults(), "insult");

                            /* WRITE INSULT */
                            try {
                                this.datagram.write_insult(this.insult);
                            } catch (IOException e) {
                                System.out.println("ERROR");
                            }

                            /* READ COMEBACK */
                            try {
                                this.opponentComeback = this.datagram.read_comeback();
                            } catch (IOException | OpcodeException e) {
                                this.errorType = ErrorType.WRONG_OPCODE;
                                this.state = StateType.ERROR;
                                System.out.println("ERROR");
                            }

                            /* ADD COMEBACK AS LEARNED */
                            if (this.database.isComeback(this.opponentComeback)) {
                                this.client.addComeback(this.opponentComeback);
                            } else {
                                this.errorType = ErrorType.INCOMPLETE_MESSAGE;
                                this.state = StateType.ERROR;
                            }

                            /* SYSTEM OUTPUT */
                            System.out.println("------------------------------------------------------------------------------------");
                            System.out.println("C- INSULT: " + this.insult);
                            System.out.println("S- COMEBACK: " + this.opponentComeback);
                            System.out.println("------------------------------------------------------------------------------------");

                            /* CHECK INSULT - COMEBACK WINNER */
                            if (this.database.isRightComeback(this.insult, this.opponentComeback)) {
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
                            }

                            /* ADD COMEBACK AS LEARNED */
                            if (this.database.isInsult(this.opponentInsult)) {
                                this.client.addInsult(this.opponentInsult);
                            } else {
                                this.errorType = ErrorType.INCOMPLETE_MESSAGE;
                                this.state = StateType.ERROR;
                            }

                            /* SYSTEM OUTPUT */
                            System.out.println("------------------------------------------------------------------------------------");
                            System.out.println("INSULT: " + this.opponentInsult);

                            /* SHOW & SELECT COMEBACK */
                            this.comeback = this.menu.getOption(this.client.getComebacks(), "comeback");

                            /* WRITE COMEBACK */
                            try {
                                this.datagram.write_comeback(this.comeback);
                            } catch (IOException e) {
                                System.out.println("ERROR");
                            }

                            System.out.println("COMEBACK: " + this.comeback);
                            System.out.println("------------------------------------------------------------------------------------");

                            /* CHECK INSULT - COMEBACK WINNER */
                            if (this.database.isRightComeback(this.opponentInsult, this.comeback)) {
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
                    if (this.client.getRound() == 2) this.client.addDuel();
                    if (this.server.getRound() == 2) this.server.addDuel();

                    /* CONDITION OF WIN GAME - WIN DUEL */
                    if (this.client.getDuel() == 3 | this.client.getRound() == 2) { // Check if client wins

                        /* WRITE SHOUT */
                        try {
                            clientShout = this.database.getShoutByEnumAddName(ShoutType.I_WIN, this.server.getName());
                            this.datagram.write_shout(clientShout);
                        } catch (IOException e) {
                            System.out.println("ERROR SHOUT");
                        }

                        /* WIN GAME */
                        if (this.client.getDuel() == 3) {
                            this.client.resetDuelRound();
                            this.server.resetDuelRound();
                            this.state = StateType.HELLO;

                            /* WIN DUEL */
                        } else {
                            this.client.resetRound();
                            this.server.resetRound();
                            this.state = StateType.HASH;
                        }
                    }

                    if (this.server.getDuel() == 3 | this.server.getRound() == 2) { // Check if server wins

                        /* WRITE SHOUT */
                        try {
                            clientShout = this.database.getShoutByEnumAddName(ShoutType.YOU_WIN, this.server.getName());
                            this.datagram.write_shout(clientShout);
                        } catch (IOException e) {
                            System.out.println("ERROR SHOUT");
                        }

                        /* WIN DUEL */
                        if (this.server.getDuel() == 3) {
                            this.client.resetDuelRound();
                            this.server.resetDuelRound();
                            this.state = StateType.HELLO;

                            /* WIN ROUND */
                        } else {
                            this.client.resetRound();
                            this.server.resetRound();
                            this.state = StateType.HASH;
                        }
                    }

                    /* READ SHOUT */
                    try {
                        serverShout = this.datagram.read_shout();
                    } catch (IOException | OpcodeException e) {
                        System.out.println("ERROR SHOUT READ");
                    }

                    /* SYSTEM OUTPUT */
                    System.out.println("C- SHOUT: " + clientShout);
                    System.out.println("S- SHOUT: " + serverShout + "\n");

                    if (this.menu.getExit()) {
                        this.gameBool = false;
                        break;
                    }

                    break;

                case ERROR:

                    String errorMessage = this.database.getErrorByEnum(this.errorType);

                    try {
                        this.datagram.write_error(errorMessage);
                    } catch (IOException e) {
                        System.out.println("C- ERROR");
                    }
                    this.gameBool = false;
                    /*
                    try {
                        String error = this.datagram.read_error();
                        System.out.println(error);
                    } catch (IOException | OpcodeException e) {
                        System.out.println("S- ERROR");
                    }*/

                    break;
            }
        }
    }

    public void automaticMode() {

        while (gameBool) {

            switch (this.state) {

                case HELLO:

                    this.client.setName("IA Player");

                    /* ADD RANDOM INSULT-COMEBACK */
                    do { // Check if already contains and always add pair of insults/comebacks
                        contained = this.client.containsWithAddInsultComeback(this.dp.getRandomInsultComeback());
                    } while (contained); // If already contains, get new pairs

                    /* WRITE HELLO */
                    try {
                        this.datagram.write_hello(this.client.generateId(), this.client.getName());
                    } catch (IOException e) {
                        System.out.println("Hello Error Write " + e.getMessage());
                    }

                    /* READ HELLO */
                    try {
                        this.server.setName(this.datagram.read_hello());
                        this.server.setId(this.datagram.getIdOpponent());
                    } catch (IOException | OpcodeException e) {
                        System.out.println("Hello Error Read " + e.getMessage());
                        this.errorType = ErrorType.WRONG_OPCODE;
                        this.state = StateType.ERROR;
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
                        this.client.setHash(this.getHash(this.client.getSecret()));
                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                    }

                    /* READ HASH */
                    try {
                        this.server.setHash(this.datagram.read_hash());
                    } catch (IOException | OpcodeException e) {
                        System.out.println(e.getMessage());
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
                    }

                    /* READ SECRET */
                    try {
                        this.server.setSecret(this.datagram.read_secret());
                    } catch (IOException | OpcodeException e) {
                        System.out.println(e.getMessage());
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

                            /* SELECT RANDOM INSULT */
                            this.insult = this.client.getRandomInsult();

                            /* WRITE INSULT */
                            try {
                                this.datagram.write_insult(this.insult);
                            } catch (IOException e) {
                                System.out.println("ERROR");
                            }

                            /* READ COMEBACK */
                            try {
                                this.opponentComeback = this.datagram.read_comeback();
                            } catch (IOException | OpcodeException e) {
                                this.errorType = ErrorType.WRONG_OPCODE;
                                this.state = StateType.ERROR;
                                System.out.println("ERROR");
                            }

                            /* ADD COMEBACK AS LEARNED */
                            if (this.database.isComeback(this.opponentComeback)) {
                                this.client.addComeback(this.opponentComeback);
                            } else {
                                this.errorType = ErrorType.INCOMPLETE_MESSAGE;
                                this.state = StateType.ERROR;
                            }

                            System.out.println("INSULT: " + this.insult);
                            System.out.println("COMEBACK: " + this.opponentComeback);
                            System.out.println("------------------------------------------------------------------------------------");

                            /* CHECK INSULT - COMEBACK WINNER */
                            if (this.database.isRightComeback(this.insult, this.opponentComeback)) {
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
                            }

                            /* SYSTEM OUTPUT */
                            System.out.println("------------------------------------------------------------------------------------");
                            System.out.println("INSULT: " + this.opponentInsult);

                            /* ADD COMEBACK AS LEARNED */
                            if (this.database.isInsult(this.opponentInsult)) {
                                this.client.addInsult(this.opponentInsult);
                            } else {
                                this.errorType = ErrorType.INCOMPLETE_MESSAGE;
                                this.state = StateType.ERROR;
                            }

                            /* SELECT RANDOM COMEBACK */
                            this.comeback = this.client.getRandomComeback();

                            /* WRITE COMEBACK */
                            try {
                                this.datagram.write_comeback(this.comeback);
                            } catch (IOException e) {
                                System.out.println("ERROR");
                            }

                            System.out.println("COMEBACK: " + this.comeback);
                            System.out.println("------------------------------------------------------------------------------------");

                            /* CHECK INSULT - COMEBACK WINNER */
                            if (this.database.isRightComeback(this.opponentInsult, this.comeback)) {
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
                    if (this.client.getRound() == 2) this.client.addDuel();
                    if (this.server.getRound() == 2) this.server.addDuel();

                    /* CONDITION OF WIN GAME - WIN DUEL */
                    if (this.client.getDuel() == 3 | this.client.getRound() == 2) { // Check if client wins

                        /* WRITE SHOUT */
                        try {
                            clientShout = this.database.getShoutByEnumAddName(ShoutType.I_WIN, this.server.getName());
                            this.datagram.write_shout(clientShout);
                        } catch (IOException e) {
                            System.out.println("ERROR SHOUT");
                        }

                        /* WIN GAME */
                        if (this.client.getDuel() == 3) {
                            this.client.resetDuelRound();
                            this.server.resetDuelRound();
                            this.gameBool = false;

                            /* WIN DUEL */
                        } else {
                            this.client.resetRound();
                            this.server.resetRound();
                            this.state = StateType.HASH;
                        }

                    }

                    if (this.server.getDuel() == 3 | this.server.getRound() == 2) { // Check if server wins

                        /* WRITE SHOUT */
                        try {
                            clientShout = this.database.getShoutByEnumAddName(ShoutType.YOU_WIN, this.server.getName());
                            this.datagram.write_shout(clientShout);
                        } catch (IOException e) {
                            System.out.println("ERROR SHOUT");
                        }

                        /* WIN DUEL */
                        if (this.server.getDuel() == 3) {
                            this.client.resetDuelRound();
                            this.server.resetDuelRound();
                            this.gameBool = false;

                            /* WIN ROUND */
                        } else {
                            this.client.resetRound();
                            this.server.resetRound();
                            this.state = StateType.HASH;
                        }
                    }

                    /* READ SHOUT */
                    try {
                        serverShout = this.datagram.read_shout();
                    } catch (IOException | OpcodeException e) {
                        System.out.println("ERROR SHOUT");
                    }

                    /* SYSTEM OUTPUT */
                    System.out.println("C- SHOUT: " + clientShout);
                    System.out.println("S- SHOUT: " + serverShout + "\n");

                    break;

                case ERROR:

                    String errorMessage = this.database.getErrorByEnum(this.errorType);

                    try {
                        this.datagram.write_error(errorMessage);
                    } catch (IOException e) {
                        System.out.println("C- ERROR");
                    }
                    this.gameBool = false;
                    /*
                    try {
                        String error = this.datagram.read_error();
                        System.out.println(error);
                    } catch (IOException | OpcodeException e) {
                        System.out.println("S- ERROR");
                    }*/

                    break;
            }
        }
    }

    /* WILL BE TESTED IN DATAGRAM CLASS */
    public boolean proofHash(String secret, byte[] hash) {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        if (secret != null || hash != null) {
            byte[] encodedhash = digest.digest(
                    secret.getBytes(StandardCharsets.UTF_8));

            return Arrays.equals(encodedhash, hash);
        } else {
            return false;
        }
    }

    /* WILL BE TESTED IN DATAGRAM CLASS */
    public byte[] getHash(String str) {
        byte hashBytes[] = new byte[32];
        MessageDigest digest = null;

        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        if (str != null) {
            byte[] encodedhash = digest.digest(
                    str.getBytes(StandardCharsets.UTF_8));


            for (int i = 0; i < 32; i++) hashBytes[i] = encodedhash[i];
        }

        return hashBytes;
    }

    /* WILL BE TESTED IN DATAGRAM CLASS */
    public boolean isEven(String s1, String s2) {
        int n1 = Integer.parseInt(s1);
        int n2 = Integer.parseInt(s2);
        return ((n1 + n2) % 2 == 0);
    }
}