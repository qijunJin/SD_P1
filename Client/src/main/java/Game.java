import enumType.ErrorType;
import enumType.ShoutType;
import enumType.StateType;
import exception.OpcodeException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * <h1>Game class</h1>
 * The logic of both manual mode and automatic mode of the game.
 */
public class Game {
    private DatabaseProvider dp;
    private final Database database;
    private final Datagram datagram;
    private final Menu menu;

    String clientShout, serverShout;
    private boolean gameBool; // Infinite loop
    private StateType state;

    private String insult, comeback;
    private String opponentInsult, opponentComeback;
    private boolean contained;
    private final Player client = new Player(); // Client data
    private final Player server = new Player(); // Server data
    private ErrorType errorType;

    /**
     * Constructor of game
     *
     * @param datagram instance of datagram
     * @param mode     mode of game
     */
    public Game(Datagram datagram, int mode) {
        this.database = new Database();
        this.dp = new DatabaseProvider(database.getInsults(), database.getComebacks());
        this.state = StateType.HELLO;
        this.datagram = datagram;
        this.menu = new Menu();
        this.gameBool = true;
        this.run(mode);
    }

    /**
     * @param mode mode of game
     */
    private void run(int mode) {
        if (mode == 1) this.automaticMode();
        if (mode == 0) this.manualMode();
    }

    /**
     * Game in manual mode
     */
    public void manualMode() {


        while (gameBool) {

            /*
            this.state = StateType.HELLO;
                switch (this.state) {

                    (opcode = 0x01) {
                        write_hello();
                        this.state = StateType.HASH;
                    }
                    (opcode = 0x02) {
                        write_hash();
                    }

                    (this.state  = StateType.INSULT) {
                        write_insult();
                    }
                    (opcode = 0x07) {

                    }
                }

                this.opcode = this.read_opcode();

                switch(this.opcode){

                    (opcode = 0x01) {
                       read_hello();
                    }
                    (opcode = 0x02) {
                        read_hash();
                    }
                    (opcode = 0x03) {
                        read_secret();
                    }
                    (opcode = 0x04) {
                        read_insult();
                        this.state = StateType.COMEBACK;
                    }
                    this.state = StateType.BLOCK;
                    (opcode = 0x07){

                    }
                }
            }*/

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
                        this.datagram.writeIntString(1, this.client.getId(), this.client.getName());
                    } catch (IOException e) {
                        System.out.println("C- ERROR: " + e.getMessage());
                        this.gameBool = false;
                        break;
                    }

                    /*
                      try {
                        this.opcode = this.datagram.read_opcode();
                        if(this.opcode == 0x01){
                            this.server.setName(this.datagram.read_hello());
                            this.server.setId(this.datagram.getIdOpponent());
                        }
                        else if( 0x07){
                            this.emessage = this.datagram.read_error(this.opcode);
                            System.out.println(emessage);
                            this.gameBool = false;
                        }
                    */

                    /* READ HELLO */
                    try {
                        String[] str = this.datagram.readStringArray(this.datagram.readByte(), 1);
                        this.server.setId(Integer.parseInt(str[0]));
                        this.server.setName(str[1]);
                    } catch (Exception e) {
                        System.out.println("C- ERROR: " + e.getMessage());
                        if (e instanceof IOException) this.errorType = ErrorType.TIMEOUT;
                        if (e instanceof OpcodeException) this.errorType = ErrorType.WRONG_OPCODE;
                        this.state = StateType.ERROR;
                        break;
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
                        this.datagram.writeHash(2, this.client.generateSecret());
                        this.client.setHash(this.getHash(this.client.getSecret()));
                    } catch (IOException e) {
                        System.out.println("C- ERROR: " + e.getMessage());
                        this.gameBool = false;
                        break;
                    }

                    /* READ HASH */
                    try {
                        this.server.setHash(this.datagram.readHash(this.datagram.readByte(), 2));
                    } catch (Exception e) {
                        System.out.println("C- ERROR: " + e.getMessage());
                        if (e instanceof IOException) this.errorType = ErrorType.TIMEOUT;
                        if (e instanceof OpcodeException) this.errorType = ErrorType.WRONG_OPCODE;
                        this.state = StateType.ERROR;
                        break;
                    }

                    /* SYSTEM OUTPUT */
                    System.out.println("C- HASH: " + Arrays.toString(this.client.getHash()));
                    System.out.println("S- HASH: " + Arrays.toString(this.server.getHash()));

                    this.state = StateType.SECRET;
                    break;

                case SECRET:

                    /* WRITE SECRET */
                    try {
                        this.datagram.writeString(3, this.client.getSecret());
                    } catch (IOException e) {
                        System.out.println("C- ERROR: " + e.getMessage());
                        this.gameBool = false;
                        break;
                    }

                    /* READ SECRET */
                    try {
                        this.server.setSecret(this.datagram.readString(this.datagram.readByte(), 3));
                    } catch (Exception e) {
                        System.out.println("C- ERROR: " + e.getMessage());
                        if (e instanceof IOException) this.errorType = ErrorType.TIMEOUT;
                        if (e instanceof OpcodeException) this.errorType = ErrorType.WRONG_OPCODE;
                        this.state = StateType.ERROR;
                        break;
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
                            System.out.println("C- ERROR: SAME ID");
                            this.errorType = ErrorType.NOT_SAME;
                            this.state = StateType.ERROR;
                            break;
                        }
                    } else {
                        System.out.println("C- ERROR: NOT COINCIDENT HASH");
                        this.errorType = ErrorType.NOT_SAME;
                        this.state = StateType.ERROR;
                        break;
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
                                this.datagram.writeString(4, this.insult);
                            } catch (IOException e) {
                                System.out.println("C- ERROR: " + e.getMessage());
                                this.gameBool = false;
                                break;
                            }

                            /* READ COMEBACK */
                            try {
                                this.opponentComeback = this.datagram.readString(this.datagram.readByte(), 5);
                            } catch (Exception e) {
                                System.out.println("C- ERROR: " + e.getMessage());
                                if (e instanceof IOException) this.errorType = ErrorType.TIMEOUT;
                                if (e instanceof OpcodeException) this.errorType = ErrorType.WRONG_OPCODE;
                                this.state = StateType.ERROR;
                                break;
                            }

                            /* ADD COMEBACK AS LEARNED */
                            if (this.database.isComeback(this.opponentComeback)) {
                                this.client.addComeback(this.opponentComeback);
                            } else {
                                this.errorType = ErrorType.INCOMPLETE_MESSAGE;
                                this.state = StateType.ERROR;
                                break;
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
                                this.opponentInsult = this.datagram.readString(this.datagram.readByte(), 4);
                            } catch (Exception e) {
                                System.out.println("C- ERROR: " + e.getMessage());
                                if (e instanceof IOException) this.errorType = ErrorType.TIMEOUT;
                                if (e instanceof OpcodeException) this.errorType = ErrorType.WRONG_OPCODE;
                                this.state = StateType.ERROR;
                                break;
                            }

                            /* ADD COMEBACK AS LEARNED */
                            if (this.database.isInsult(this.opponentInsult)) {
                                this.client.addInsult(this.opponentInsult);
                            } else {
                                this.errorType = ErrorType.INCOMPLETE_MESSAGE;
                                this.state = StateType.ERROR;
                                break;
                            }

                            /* SYSTEM OUTPUT */
                            System.out.println("------------------------------------------------------------------------------------");
                            System.out.println("INSULT: " + this.opponentInsult);

                            /* SHOW & SELECT COMEBACK */
                            this.comeback = this.menu.getOption(this.client.getComebacks(), "comeback");

                            /* WRITE COMEBACK */
                            try {
                                this.datagram.writeString(5, this.comeback);
                            } catch (IOException e) {
                                System.out.println("C- ERROR: " + e.getMessage());
                                this.gameBool = false;
                                break;
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

                    /* CLIENT - WIN GAME - WIN DUEL */
                    if (this.client.getDuel() == 3 | this.client.getRound() == 2) {

                        /* WRITE SHOUT */
                        try {
                            clientShout = this.database.getShoutByEnumAddName(ShoutType.I_WIN, this.server.getName());
                            this.datagram.writeString(6, clientShout);
                        } catch (IOException e) {
                            System.out.println("C- ERROR: " + e.getMessage());
                            this.gameBool = false;
                            break;
                        }

                        /* WIN GAME */
                        if (this.client.getDuel() == 3) {
                            this.client.resetDuel();
                            this.server.resetDuel();
                            this.state = StateType.HELLO;

                            /* WIN DUEL */
                        } else {
                            this.state = StateType.HASH;
                        }
                    }

                    /* SERVER - WIN GAME - WIN DUEL */
                    if (this.server.getDuel() == 3 | this.server.getRound() == 2) {

                        /* WRITE SHOUT */
                        try {
                            clientShout = this.database.getShoutByEnumAddName(ShoutType.YOU_WIN, this.server.getName());
                            this.datagram.writeString(6, clientShout);
                        } catch (IOException e) {
                            System.out.println("C- ERROR: " + e.getMessage());
                            this.gameBool = false;
                            break;
                        }

                        /* WIN GAME */
                        if (this.server.getDuel() == 3) {
                            this.client.resetDuel();
                            this.server.resetDuel();
                            this.state = StateType.HELLO;

                            /* WIN DUEL */
                        } else {
                            this.state = StateType.HASH;
                        }
                    }

                    this.client.resetRound();
                    this.server.resetRound();

                    /* READ SHOUT */
                    try {
                        serverShout = this.datagram.readString(this.datagram.readByte(), 6);
                    } catch (Exception e) {
                        System.out.println("C- ERROR: " + e.getMessage());
                        if (e instanceof IOException) this.errorType = ErrorType.TIMEOUT;
                        if (e instanceof OpcodeException) this.errorType = ErrorType.WRONG_OPCODE;
                        this.state = StateType.ERROR;
                        break;
                    }

                    /* SYSTEM OUTPUT */
                    System.out.println("C- SHOUT: " + clientShout);
                    System.out.println("S- SHOUT: " + serverShout + "\n");

                    if (this.menu.getExit()) {
                        System.out.println("[Connexion closed]");
                        this.gameBool = false;
                        break;
                    }

                    break;

                case ERROR:

                    String errorMessage = this.database.getErrorByEnum(this.errorType);

                    /* WRITE ERROR */
                    try {
                        this.datagram.writeString(7, errorMessage);
                    } catch (IOException e) {
                        System.out.println("C- ERROR: " + e.getMessage());
                    }

                    this.gameBool = false;
                    break;
            }
        }
    }

    /**
     * Game in automatic mode
     */
    public void automaticMode() {

        while (gameBool) {

            switch (this.state) {

                case HELLO:

                    this.client.setName("IA Player");

                    /* ADD RANDOM INSULT-COMEBACK */
                    do {
                        contained = this.client.containsWithAddInsultComeback(this.dp.getRandomInsultComeback());
                    } while (contained);

                    /* WRITE HELLO */
                    try {
                        this.datagram.writeIntString(1, this.client.generateId(), this.client.getName());
                    } catch (IOException e) {
                        System.out.println("C- ERROR: " + e.getMessage());
                        this.gameBool = false;
                        break;
                    }

                    /* READ HELLO */
                    try {
                        String[] str = this.datagram.readStringArray(this.datagram.readByte(), 1);
                        this.server.setId(Integer.parseInt(str[0]));
                        this.server.setName(str[1]);
                    } catch (IOException | OpcodeException e) {
                        System.out.println("C- ERROR: " + e.getMessage());
                        this.errorType = ErrorType.WRONG_OPCODE;
                        this.state = StateType.ERROR;
                        break;
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
                        this.datagram.writeHash(2, this.client.generateSecret());
                        this.client.setHash(this.getHash(this.client.getSecret()));
                    } catch (IOException e) {
                        System.out.println("C- ERROR: " + e.getMessage());
                        this.gameBool = false;
                        break;
                    }

                    /* READ HASH */
                    try {
                        this.server.setHash(this.datagram.readHash(this.datagram.readByte(), 2));
                    } catch (IOException | OpcodeException e) {
                        System.out.println("C- ERROR: " + e.getMessage());
                        this.errorType = ErrorType.WRONG_OPCODE;
                        this.state = StateType.ERROR;
                        break;
                    }

                    /* SYSTEM OUTPUT */
                    System.out.println("C- HASH: " + Arrays.toString(this.client.getHash()));
                    System.out.println("S- HASH: " + Arrays.toString(this.server.getHash()));

                    this.state = StateType.SECRET;
                    break;

                case SECRET:

                    /* WRITE SECRET */
                    try {
                        this.datagram.writeString(3, this.client.getSecret());
                    } catch (IOException e) {
                        System.out.println("C- ERROR: " + e.getMessage());
                        this.gameBool = false;
                        break;
                    }

                    /* READ SECRET */
                    try {
                        this.server.setSecret(this.datagram.readString(this.datagram.readByte(), 3));
                    } catch (IOException | OpcodeException e) {
                        System.out.println("C- ERROR: " + e.getMessage());
                        this.errorType = ErrorType.WRONG_OPCODE;
                        this.state = StateType.ERROR;
                        break;
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
                            System.out.println("C- ERROR: SAME ID");
                            this.errorType = ErrorType.NOT_SAME;
                            this.state = StateType.ERROR;
                            break;
                        }
                    } else {
                        System.out.println("C- ERROR: NOT COINCIDENT HASH");
                        this.errorType = ErrorType.NOT_SAME;
                        this.state = StateType.ERROR;
                        break;
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
                                this.datagram.writeString(4, this.insult);
                            } catch (IOException e) {
                                System.out.println("C- ERROR: " + e.getMessage());
                                this.gameBool = false;
                                break;
                            }

                            /* READ COMEBACK */
                            try {
                                this.opponentComeback = this.datagram.readString(this.datagram.readByte(), 5);
                            } catch (IOException | OpcodeException e) {
                                System.out.println("C- ERROR: " + e.getMessage());
                                this.errorType = ErrorType.WRONG_OPCODE;
                                this.state = StateType.ERROR;
                                break;
                            }

                            /* ADD COMEBACK AS LEARNED */
                            if (this.database.isComeback(this.opponentComeback)) {
                                this.client.addComeback(this.opponentComeback);
                            } else {
                                this.errorType = ErrorType.INCOMPLETE_MESSAGE;
                                this.state = StateType.ERROR;
                                break;
                            }

                            System.out.println("C- INSULT: " + this.insult);
                            System.out.println("S- COMEBACK: " + this.opponentComeback);
                            System.out.println("------------------------------------------------------------------------------------");

                            /* CHECK INSULT - COMEBACK WINNER */
                            if (this.database.isRightComeback(this.insult, this.opponentComeback)) {
                                this.server.addRound();
                                this.state = StateType.COMEBACK;

                                System.out.println("S- WIN");
                            } else {
                                this.client.addRound();
                                System.out.println("C- WIN");
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
                                this.opponentInsult = this.datagram.readString(this.datagram.readByte(), 4);
                            } catch (IOException | OpcodeException e) {
                                System.out.println("C- ERROR: " + e.getMessage());
                                this.errorType = ErrorType.WRONG_OPCODE;
                                this.state = StateType.ERROR;
                                break;
                            }

                            /* SYSTEM OUTPUT */
                            System.out.println("------------------------------------------------------------------------------------");
                            System.out.println("S- INSULT: " + this.opponentInsult);

                            /* ADD COMEBACK AS LEARNED */
                            if (this.database.isInsult(this.opponentInsult)) {
                                this.client.addInsult(this.opponentInsult);
                            } else {
                                this.errorType = ErrorType.INCOMPLETE_MESSAGE;
                                this.state = StateType.ERROR;
                                break;
                            }

                            /* SELECT RANDOM COMEBACK */
                            this.comeback = this.client.getRandomComeback();

                            /* WRITE COMEBACK */
                            try {
                                this.datagram.writeString(5, this.comeback);
                            } catch (IOException e) {
                                System.out.println("C- ERROR: " + e.getMessage());
                                this.gameBool = false;
                                break;
                            }

                            System.out.println("C- COMEBACK: " + this.comeback);
                            System.out.println("------------------------------------------------------------------------------------");

                            /* CHECK INSULT - COMEBACK WINNER */
                            if (this.database.isRightComeback(this.opponentInsult, this.comeback)) {
                                this.client.addRound();
                                this.state = StateType.INSULT;

                                System.out.println("C- WIN");
                            } else {
                                this.server.addRound();
                                System.out.println("S- WIN");
                            }
                        }
                    }

                    break;

                case SHOUT:

                    /* CONDITION OF ADD DUEL */
                    if (this.client.getRound() == 2) this.client.addDuel();
                    if (this.server.getRound() == 2) this.server.addDuel();

                    /* CLIENT - WIN DUEL */
                    if (this.client.getRound() == 2) {

                        /* WRITE SHOUT */
                        try {
                            clientShout = this.database.getShoutByEnumAddName(ShoutType.I_WIN, this.server.getName());
                            this.datagram.writeString(6, clientShout);
                        } catch (IOException e) {
                            System.out.println("C- ERROR: " + e.getMessage());
                            this.gameBool = false;
                            break;
                        }

                        /* WIN DUEL */
                        this.client.resetRound();
                        this.server.resetRound();
                        this.state = StateType.HASH;
                    }

                    /* SERVER - WIN DUEL */
                    if (this.server.getRound() == 2) {

                        /* WRITE SHOUT */
                        try {
                            clientShout = this.database.getShoutByEnumAddName(ShoutType.YOU_WIN, this.server.getName());
                            this.datagram.writeString(6, clientShout);
                        } catch (IOException e) {
                            System.out.println("C- ERROR: " + e.getMessage());
                            this.gameBool = false;
                            break;
                        }

                        /* WIN ROUND */
                        this.client.resetRound();
                        this.server.resetRound();
                        this.state = StateType.HASH;
                    }

                    /* READ SHOUT */
                    try {
                        serverShout = this.datagram.readString(this.datagram.readByte(), 6);
                    } catch (IOException | OpcodeException e) {
                        System.out.println("C- ERROR: " + e.getMessage());
                        this.errorType = ErrorType.WRONG_OPCODE;
                        this.state = StateType.ERROR;
                        break;
                    }

                    /* SYSTEM OUTPUT */
                    System.out.println("C- SHOUT: " + clientShout);
                    System.out.println("S- SHOUT: " + serverShout + "\n");

                    /* WIN GAME */
                    if (this.client.getDuel() == 3 | this.server.getDuel() == 3) {
                        System.out.println("[Connexion closed]");
                        this.gameBool = false;
                    }

                    break;

                case ERROR:

                    String errorMessage = this.database.getErrorByEnum(this.errorType);

                    /* WRITE ERROR */
                    try {
                        this.datagram.writeString(7, errorMessage);
                    } catch (IOException e) {
                        System.out.println("C- ERROR: " + e.getMessage());
                    }

                    this.gameBool = false;
                    break;
            }
        }
    }

    /**
     * Function to check if the hash of given secret is coincident to the given hash
     *
     * @param secret the given secret in String
     * @param hash   the given hash in Array
     * @return true as they are coincident, false adversely.
     */
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

    /**
     * Function that returns the hash of the given secret
     *
     * @param secret the given secret in String
     * @return the hash value of the secret
     */
    public byte[] getHash(String secret) {
        byte hashBytes[] = new byte[32];
        MessageDigest digest = null;

        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        if (secret != null) {
            byte[] encodedhash = digest.digest(
                    secret.getBytes(StandardCharsets.UTF_8));


            for (int i = 0; i < 32; i++) hashBytes[i] = encodedhash[i];
        }

        return hashBytes;
    }

    /**
     * Function to check the parity of two numbers
     *
     * @param s1 first number in String
     * @param s2 second number in String
     * @return the parity of given numbers, true as even, false as odd
     */
    public boolean isEven(String s1, String s2) {
        int n1 = Integer.parseInt(s1);
        int n2 = Integer.parseInt(s2);
        return ((n1 + n2) % 2 == 0);
    }
}