import shared.database.Database;
import shared.enumType.ErrorType;
import shared.enumType.ShoutType;
import shared.exception.OpcodeException;
import shared.model.DatabaseProvider;
import shared.model.Player;
import utils.Datagram;

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
    private boolean newGame;
    private StateType state;

    private String insult, comeback;
    private String opponentInsult, opponentComeback;
    private boolean contained;
    private final Player client = new Player(); // Client data
    private final Player server = new Player(); // Server data
    private ErrorType errorType;
    private String errorMessage;
    private int opcode;

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
        this.newGame = true;
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

            if (this.newGame) {

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

                do {
                    contained = this.client.containsWithAddInsultComeback(this.dp.getRandomInsultComeback());
                } while (contained); // If already contains, get new pairs

                try {
                    this.datagram.writeIntString(1, this.client.getId(), this.client.getName());
                    System.out.println("C- HELLO: " + this.client.getId() + " " + this.client.getName());
                } catch (IOException e) {
                    System.out.println("C- ERROR: " + e.getMessage());
                    this.gameBool = false;
                    break;
                }
                this.newGame = false;
            }

            try {
                this.opcode = this.datagram.readByte();
            } catch (Exception e) {
                writeErrorToStream(e.getMessage(), ErrorType.WRONG_OPCODE);
            }

            switch (this.opcode) {
                case 0x01:

                    try {
                        String[] str = this.datagram.readIntString(1, this.opcode);
                        this.server.setId(Integer.parseInt(str[0]));
                        this.server.setName(str[1]);
                        System.out.println("S- HELLO: " + this.server.getId() + " " + this.server.getName());
                    } catch (IOException | OpcodeException e) {
                        if (e instanceof IOException)
                            writeErrorToStream("S- ERROR: " + e.getMessage(), ErrorType.TIMEOUT);
                        if (e instanceof OpcodeException)
                            writeErrorToStream("S- ERROR: " + e.getMessage(), ErrorType.WRONG_OPCODE);
                        break;
                    }

                    try {
                        this.datagram.writeHash(2, this.client.generateSecret());
                        this.client.setHash(this.getHash(this.client.getSecret()));
                        System.out.println("C- HASH: " + Arrays.toString(this.client.getHash()));
                    } catch (IOException e) {
                        System.out.println("C- ERROR: " + e.getMessage());
                        this.gameBool = false;
                        break;
                    }

                    break;

                case 0x02:

                    do {
                        contained = this.client.containsWithAddInsultComeback(this.dp.getRandomInsultComeback());
                    } while (contained);

                    try {
                        this.server.setHash(this.datagram.readHash(2, this.opcode));
                        System.out.println("S- HASH: " + Arrays.toString(this.server.getHash()));
                    } catch (IOException | OpcodeException e) {
                        if (e instanceof IOException)
                            writeErrorToStream("S- ERROR: " + e.getMessage(), ErrorType.TIMEOUT);
                        if (e instanceof OpcodeException)
                            writeErrorToStream("S- ERROR: " + e.getMessage(), ErrorType.WRONG_OPCODE);
                        break;
                    }

                    try {
                        this.datagram.writeString(3, this.client.getSecret());
                        System.out.println("C- SECRET: " + this.client.getSecret());
                    } catch (IOException e) {
                        System.out.println("C- ERROR: " + e.getMessage());
                        this.gameBool = false;
                        break;
                    }

                    break;

                case 0x03:

                    try {
                        this.server.setSecret(this.datagram.readString(3, this.opcode));
                        System.out.println("S- SECRET: " + this.server.getSecret());
                    } catch (IOException | OpcodeException e) {
                        if (e instanceof IOException)
                            writeErrorToStream("S- ERROR: " + e.getMessage(), ErrorType.TIMEOUT);
                        if (e instanceof OpcodeException)
                            writeErrorToStream("S- ERROR: " + e.getMessage(), ErrorType.WRONG_OPCODE);
                        break;
                    }

                    /* PROOF HASH - NOT EQUAL ID - EVEN/ODD ^ GREATER/LESSER -> DECIDE STATE */
                    if (this.proofHash(this.server.getSecret(), this.server.getHash())) {
                        if (this.client.getId() != this.server.getId()) {
                            if (isEven(client.getSecret(), server.getSecret()) ^ (client.getId() > server.getId())) {

                                this.insult = this.menu.getOption(this.client.getInsults(), "insult");
                                try {
                                    this.datagram.writeString(4, this.insult);
                                    System.out.println("------------------------------------------------------------------------------------");
                                    System.out.println("C- INSULT: " + this.insult);
                                } catch (IOException e) {
                                    System.out.println("C- ERROR: " + e.getMessage());
                                    this.gameBool = false;
                                    break;
                                }
                            }
                        } else {
                            this.writeErrorToStream("C- ERROR: SAME ID", ErrorType.SAME);
                            break;
                        }
                    } else {
                        this.writeErrorToStream("C- ERROR: NOT COINCIDENT HASH", ErrorType.SAME);
                        break;
                    }

                    break;

                case 0x04:

                    /* READ INSULT */
                    try {
                        this.opponentInsult = this.datagram.readString(4, this.opcode);
                        System.out.println("------------------------------------------------------------------------------------");
                        System.out.println("INSULT: " + this.opponentInsult);
                    } catch (IOException | OpcodeException e) {
                        if (e instanceof IOException)
                            writeErrorToStream("S- ERROR: " + e.getMessage(), ErrorType.TIMEOUT);
                        if (e instanceof OpcodeException)
                            writeErrorToStream("S- ERROR: " + e.getMessage(), ErrorType.WRONG_OPCODE);
                        break;
                    }

                    /* ADD COMEBACK AS LEARNED */
                    if (this.database.isInsult(this.opponentInsult)) {
                        this.client.addInsult(this.opponentInsult);

                        this.comeback = this.menu.getOption(this.client.getComebacks(), "comeback");

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
                            if (this.client.getRound() == 2) this.client.addDuel();
                            if (this.client.getRound() < 2) {
                                this.insult = this.menu.getOption(this.client.getInsults(), "insult");
                                try {
                                    this.datagram.writeString(4, this.insult);
                                    System.out.println("------------------------------------------------------------------------------------");
                                    System.out.println("C- INSULT: " + this.insult);
                                } catch (IOException e) {
                                    System.out.println("C- ERROR: " + e.getMessage());
                                    this.gameBool = false;
                                    break;
                                }
                            }
                        } else {
                            this.server.addRound();
                            if (this.server.getRound() == 2) this.server.addDuel();
                        }
                    } else {
                        this.writeErrorToStream("", ErrorType.INCOMPLETE_MESSAGE);
                        break;
                    }

                    /* CLIENT - WIN GAME - WIN DUEL */
                    if (this.client.getDuel() == 3 | this.client.getRound() == 2) {

                        /* WRITE SHOUT */
                        try {
                            clientShout = this.database.getShoutByEnumAddName(ShoutType.I_WIN, this.server.getName());
                            this.datagram.writeString(6, clientShout);
                            System.out.println("C- SHOUT: " + clientShout);
                        } catch (IOException e) {
                            System.out.println("C- ERROR: " + e.getMessage());
                            this.gameBool = false;
                            break;
                        }
                    }

                    /* SERVER - WIN GAME - WIN DUEL */
                    if (this.server.getDuel() == 3 | this.server.getRound() == 2) {

                        /* WRITE SHOUT */
                        try {
                            clientShout = this.database.getShoutByEnumAddName(ShoutType.YOU_WIN, this.server.getName());
                            this.datagram.writeString(6, clientShout);
                            System.out.println("C- SHOUT: " + clientShout);
                        } catch (IOException e) {
                            System.out.println("C- ERROR: " + e.getMessage());
                            this.gameBool = false;
                            break;
                        }
                    }


                    break;

                case 0x05:

                    /* READ COMEBACK */
                    try {
                        this.opponentComeback = this.datagram.readString(5, this.opcode);
                        System.out.println("S- COMEBACK: " + this.opponentComeback);
                        System.out.println("------------------------------------------------------------------------------------");
                    } catch (IOException | OpcodeException e) {
                        if (e instanceof IOException)
                            writeErrorToStream("S- ERROR: " + e.getMessage(), ErrorType.TIMEOUT);
                        if (e instanceof OpcodeException)
                            writeErrorToStream("S- ERROR: " + e.getMessage(), ErrorType.WRONG_OPCODE);
                        break;
                    }

                    /* ADD COMEBACK AS LEARNED */
                    if (this.database.isComeback(this.opponentComeback)) {
                        this.client.addComeback(this.opponentComeback);

                        /* CHECK INSULT - COMEBACK WINNER */
                        if (this.database.isRightComeback(this.insult, this.opponentComeback)) {
                            this.server.addRound();
                            if (this.server.getRound() == 2) this.server.addDuel();
                        } else {
                            this.client.addRound();
                            if (this.client.getRound() == 2) this.client.addDuel();
                            if (this.client.getRound() < 2) {

                                this.insult = this.menu.getOption(this.client.getInsults(), "insult");

                                try {
                                    this.datagram.writeString(4, this.insult);
                                    System.out.println("------------------------------------------------------------------------------------");
                                    System.out.println("C- INSULT: " + this.insult);
                                } catch (IOException e) {
                                    System.out.println("C- ERROR: " + e.getMessage());
                                    this.gameBool = false;
                                    break;
                                }
                            }
                        }
                    } else {
                        this.writeErrorToStream("", ErrorType.INCOMPLETE_MESSAGE);
                        break;
                    }

                    /* CLIENT - WIN GAME - WIN DUEL */
                    if (this.client.getDuel() == 3 | this.client.getRound() == 2) {

                        /* WRITE SHOUT */
                        try {
                            clientShout = this.database.getShoutByEnumAddName(ShoutType.I_WIN, this.server.getName());
                            this.datagram.writeString(6, clientShout);
                            System.out.println("C- SHOUT: " + clientShout);
                        } catch (IOException e) {
                            System.out.println("C- ERROR: " + e.getMessage());
                            this.gameBool = false;
                            break;
                        }
                    }

                    /* SERVER - WIN GAME - WIN DUEL */
                    if (this.server.getDuel() == 3 | this.server.getRound() == 2) {

                        /* WRITE SHOUT */
                        try {
                            clientShout = this.database.getShoutByEnumAddName(ShoutType.YOU_WIN, this.server.getName());
                            this.datagram.writeString(6, clientShout);
                            System.out.println("C- SHOUT: " + clientShout);
                        } catch (IOException e) {
                            System.out.println("C- ERROR: " + e.getMessage());
                            this.gameBool = false;
                            break;
                        }
                    }

                    break;

                case 0x06:

                    /* READ SHOUT */
                    try {
                        serverShout = this.datagram.readString(6, this.opcode);
                        System.out.println("S- SHOUT: " + serverShout + "\n");
                    } catch (IOException | OpcodeException e) {
                        if (e instanceof IOException)
                            writeErrorToStream("S- ERROR: " + e.getMessage(), ErrorType.TIMEOUT);
                        if (e instanceof OpcodeException)
                            writeErrorToStream("S- ERROR: " + e.getMessage(), ErrorType.WRONG_OPCODE);
                        break;
                    }

                    if (this.menu.getExit()) {
                        System.out.println("[Connexion closed]");
                        this.gameBool = false;
                        break;
                    }

                    /* WIN GAME */
                    if (this.client.getDuel() == 3 | this.server.getDuel() == 3) {
                        this.client.resetDuel();
                        this.server.resetDuel();
                        this.newGame = true;
                    } else {
                        try {
                            this.datagram.writeHash(2, this.client.generateSecret());
                            this.client.setHash(this.getHash(this.client.getSecret()));
                            System.out.println("C- HASH: " + Arrays.toString(this.client.getHash()));
                        } catch (IOException e) {
                            System.out.println("C- ERROR: " + e.getMessage());
                            this.gameBool = false;
                            break;
                        }
                    }

                    this.client.resetRound();
                    this.server.resetRound();


                    break;

                case 0x07:

                    this.writeErrorToStream("", this.errorType);

                    break;
            }
        }
    }

    private void writeErrorToStream(String s, ErrorType errorType) {
        System.out.println(s);
        try {
            this.errorMessage = this.database.getErrorByEnum(errorType);
            this.datagram.writeString(7, errorMessage);
            System.out.println(errorMessage);
        } catch (IOException e2) {
            System.out.println("C- ERROR: " + e2.getMessage());
        }
        this.gameBool = false;
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
                        String[] str = this.datagram.readIntString(1, this.datagram.readByte());
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
                        this.server.setHash(this.datagram.readHash(2, this.datagram.readByte()));
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
                        this.server.setSecret(this.datagram.readString(3, this.datagram.readByte()));
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
                            this.errorType = ErrorType.SAME;
                            this.state = StateType.ERROR;
                            break;
                        }
                    } else {
                        System.out.println("C- ERROR: NOT COINCIDENT HASH");
                        this.errorType = ErrorType.SAME;
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
                                this.opponentComeback = this.datagram.readString(5, this.datagram.readByte());
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
                                this.opponentInsult = this.datagram.readString(4, this.datagram.readByte());
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
                        serverShout = this.datagram.readString(6, this.datagram.readByte());
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
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        byte[] encodedhash = new byte[32];
        if (secret != null) {
            encodedhash = digest.digest(
                    secret.getBytes(StandardCharsets.UTF_8));
        }

        return encodedhash;
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
