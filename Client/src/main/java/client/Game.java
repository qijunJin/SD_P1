package client;

import shared.database.Database;
import shared.enumType.ErrorType;
import shared.enumType.ShoutType;
import shared.exception.OpcodeException;
import shared.functions.Functions;
import shared.model.DatabaseProvider;
import shared.model.Player;
import utils.Datagram;

import java.io.IOException;
import java.util.Arrays;

/**
 * Game class
 * The logic of both manual mode and automatic mode of the game.
 */
public class Game implements Functions {

    private final Menu menu = new Menu();
    private final Database database = new Database();
    private final Player client = new Player();
    private final Player server = new Player();
    private Datagram datagram;
    private DatabaseProvider dp;

    private String insult, comeback;
    private String opponentInsult, opponentComeback;
    private boolean contained;
    private boolean newGame = true;
    private int opcode;

    /**
     * Constructor of game.
     *
     * @param datagram instance of datagram.
     * @param mode     mode of game.
     */
    public Game(Datagram datagram, int mode) {
        this.datagram = datagram;
        if (mode == 0) this.manualMode();
        if (mode == 1) this.automaticMode();
    }

    /**
     * Game in manual mode.
     */
    public void manualMode() {

        while (true) {

            if (this.newGame) { // New game

                String name = this.menu.getName();
                int id = this.menu.getId();
                if (!this.client.hasName() && !this.client.hasId()) { // New client & get data
                    this.client.setName(name);
                    this.client.setId(id);
                    this.dp = new DatabaseProvider(this.database.getInsults(), this.database.getComebacks());
                } else if (!this.client.hasSameName(name) || !this.client.hasSameId(id)) { // Check if maintain the same name & id
                    this.client.setName(name);
                    this.client.setId(id);
                    this.dp = new DatabaseProvider(this.database.getInsults(), this.database.getComebacks()); // Renew databaseProvider
                    this.client.resetInsultsComebacks(); // Remove all insults and comebacks
                }

                do { // Add random insult - comeback pair to client
                    contained = this.client.containsWithAddInsultComeback(this.dp.getRandomInsultComeback());
                } while (contained);

                try {
                    this.datagram.writeIntString(1, this.client.getId(), this.client.getName());
                    System.out.println("C- HELLO: " + this.client.getId() + " " + this.client.getName());
                } catch (IOException e) {
                    System.out.println("C- EXCEPTION: " + e.getMessage());
                    break;
                }
                this.newGame = false;
            }

            try {
                this.opcode = this.datagram.readByte();
            } catch (Exception e) {
                writeErrorToStream(e.getMessage(), ErrorType.WRONG_OPCODE);
            }

            if (this.opcode == 0x01) {

                try {
                    String[] str = this.datagram.readIntString(1, this.opcode);
                    this.server.setId(Integer.parseInt(str[0]));
                    this.server.setName(str[1]);
                    System.out.println("S- HELLO: " + this.server.getId() + " " + this.server.getName());
                } catch (IOException | OpcodeException e) {
                    if (e instanceof IOException) writeErrorToStream(e.getMessage(), ErrorType.TIMEOUT);
                    if (e instanceof OpcodeException) writeErrorToStream(e.getMessage(), ErrorType.WRONG_OPCODE);
                    break;
                }

                try {
                    this.datagram.writeHash(2, this.client.generateSecret());
                    this.client.setHash(Functions.toHash(this.client.getSecret()));
                    System.out.println("C- HASH: " + Functions.encodeHexString(this.client.getHash()));
                } catch (IOException e) {
                    System.out.println("C- EXCEPTION: " + e.getMessage());
                    break;
                }

            } else if (this.opcode == 0x02) {

                do {
                    contained = this.client.containsWithAddInsultComeback(this.dp.getRandomInsultComeback());
                } while (contained);

                try {
                    this.server.setHash(this.datagram.readHash(2, this.opcode));
                    System.out.println("S- HASH: " + Functions.encodeHexString(this.server.getHash()));
                } catch (IOException | OpcodeException e) {
                    if (e instanceof IOException) writeErrorToStream(e.getMessage(), ErrorType.TIMEOUT);
                    if (e instanceof OpcodeException) writeErrorToStream(e.getMessage(), ErrorType.WRONG_OPCODE);
                    break;
                }

                try {
                    this.datagram.writeString(3, this.client.getSecret());
                    System.out.println("C- SECRET: " + this.client.getSecret());
                } catch (IOException e) {
                    System.out.println("C- EXCEPTION: " + e.getMessage());
                    break;
                }

            } else if (this.opcode == 0x03) {

                try {
                    this.server.setSecret(this.datagram.readString(3, this.opcode));
                    System.out.println("S- SECRET: " + this.server.getSecret());
                } catch (IOException | OpcodeException e) {
                    if (e instanceof IOException) writeErrorToStream(e.getMessage(), ErrorType.TIMEOUT);
                    if (e instanceof OpcodeException) writeErrorToStream(e.getMessage(), ErrorType.WRONG_OPCODE);
                    break;
                }

                if (Functions.proofHash(this.server.getSecret(), this.server.getHash())) { // Decide turn
                    if (this.client.getId() != this.server.getId()) {
                        if (Functions.isEven(client.getSecret(), server.getSecret()) ^ (client.getId() > server.getId())) {

                            this.insult = this.menu.getOption(this.client.getInsults(), "insult");
                            try {
                                this.datagram.writeString(4, this.insult);
                                System.out.println("------------------------------------------------------------------------------------");
                                System.out.println("C- INSULT: " + this.insult);
                            } catch (IOException e) {
                                System.out.println("C- EXCEPTION: " + e.getMessage());
                                break;
                            }
                        }
                    } else {
                        this.writeErrorToStream("C- EXCEPTION: SAME ID", ErrorType.SAME);
                    }
                } else {
                    this.writeErrorToStream("C- EXCEPTION: NOT COINCIDENT HASH", ErrorType.SAME);
                }

            } else if (this.opcode == 0x04) { // Read insult

                try {
                    this.opponentInsult = this.datagram.readString(4, this.opcode);
                    System.out.println("------------------------------------------------------------------------------------");
                    System.out.println("INSULT: " + this.opponentInsult);
                } catch (IOException | OpcodeException e) {
                    if (e instanceof IOException) writeErrorToStream(e.getMessage(), ErrorType.TIMEOUT);
                    if (e instanceof OpcodeException) writeErrorToStream(e.getMessage(), ErrorType.WRONG_OPCODE);
                    break;
                }

                if (this.database.isInsult(this.opponentInsult)) {
                    this.client.addInsult(this.opponentInsult);

                    this.comeback = this.menu.getOption(this.client.getComebacks(), "comeback");
                    try {
                        this.datagram.writeString(5, this.comeback);
                    } catch (IOException e) {
                        System.out.println("C- EXCEPTION: " + e.getMessage());
                        break;
                    }
                    System.out.println("COMEBACK: " + this.comeback);
                    System.out.println("------------------------------------------------------------------------------------");

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
                                System.out.println("C- EXCEPTION: " + e.getMessage());
                                break;
                            }
                        }
                    } else {
                        this.server.addRound();
                        if (this.server.getRound() == 2) this.server.addDuel();
                    }
                } else {
                    this.writeErrorToStream("C- EXCEPTION: MESSAGE INCOMPLETE", ErrorType.INCOMPLETE_MESSAGE);
                    break;
                }

                /* CLIENT - WIN GAME - WIN DUEL */
                if (this.client.getDuel() == 3 | this.client.getRound() == 2) {
                    try {
                        this.datagram.writeString(6, this.database.getShoutByEnumAddName(ShoutType.I_WIN, this.server.getName()));
                        System.out.println("C- SHOUT: " + this.database.getShoutByEnumAddName(ShoutType.I_WIN, this.server.getName()));
                    } catch (IOException e) {
                        System.out.println("C- EXCEPTION: " + e.getMessage());
                        break;
                    }
                }

                /* SERVER - WIN GAME - WIN DUEL */
                if (this.server.getDuel() == 3 | this.server.getRound() == 2) {
                    try {
                        this.datagram.writeString(6, this.database.getShoutByEnumAddName(ShoutType.YOU_WIN, this.server.getName()));
                        System.out.println("C- SHOUT: " + this.database.getShoutByEnumAddName(ShoutType.YOU_WIN, this.server.getName()));
                    } catch (IOException e) {
                        System.out.println("C- EXCEPTION: " + e.getMessage());
                        break;
                    }
                }
            } else if (this.opcode == 0x05) { // Read comeback

                try {
                    this.opponentComeback = this.datagram.readString(5, this.opcode);
                    System.out.println("S- COMEBACK: " + this.opponentComeback);
                    System.out.println("------------------------------------------------------------------------------------");
                } catch (IOException | OpcodeException e) {
                    if (e instanceof IOException) writeErrorToStream(e.getMessage(), ErrorType.TIMEOUT);
                    if (e instanceof OpcodeException) writeErrorToStream(e.getMessage(), ErrorType.WRONG_OPCODE);
                    break;
                }

                if (this.database.isComeback(this.opponentComeback)) {
                    this.client.addComeback(this.opponentComeback);

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
                                System.out.println("C- EXCEPTION: " + e.getMessage());
                                break;
                            }
                        }
                    }
                } else {
                    this.writeErrorToStream("C- EXCEPTION: MESSAGE INCOMPLETE", ErrorType.INCOMPLETE_MESSAGE);
                    break;
                }

                /* CLIENT - WIN GAME - WIN DUEL */
                if (this.client.getDuel() == 3 | this.client.getRound() == 2) {
                    try {
                        this.datagram.writeString(6, this.database.getShoutByEnumAddName(ShoutType.I_WIN, this.server.getName()));
                        System.out.println("C- SHOUT: " + this.database.getShoutByEnumAddName(ShoutType.I_WIN, this.server.getName()));
                    } catch (IOException e) {
                        System.out.println("C- EXCEPTION: " + e.getMessage());
                        break;
                    }
                }

                /* SERVER - WIN GAME - WIN DUEL */
                if (this.server.getDuel() == 3 | this.server.getRound() == 2) {
                    try {
                        this.datagram.writeString(6, this.database.getShoutByEnumAddName(ShoutType.YOU_WIN, this.server.getName()));
                        System.out.println("C- SHOUT: " + this.database.getShoutByEnumAddName(ShoutType.YOU_WIN, this.server.getName()));
                    } catch (IOException e) {
                        System.out.println("C- EXCEPTION: " + e.getMessage());
                        break;
                    }
                }

            } else if (this.opcode == 0x06) { // Read shout

                try {
                    System.out.println("S- SHOUT: " + this.datagram.readString(6, this.opcode) + "\n");
                } catch (IOException | OpcodeException e) {
                    if (e instanceof IOException) writeErrorToStream(e.getMessage(), ErrorType.TIMEOUT);
                    if (e instanceof OpcodeException) writeErrorToStream(e.getMessage(), ErrorType.WRONG_OPCODE);
                    break;
                }

                if (this.menu.getExit()) {
                    System.out.println("[Connexion closed]");

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
                        this.client.setHash(Functions.toHash(this.client.getSecret()));
                        System.out.println("C- HASH: " + Functions.encodeHexString(this.client.getHash()));
                    } catch (IOException e) {
                        System.out.println("C- EXCEPTION: " + e.getMessage());
                        break;
                    }
                }
                this.client.resetRound();
                this.server.resetRound();

            } else if (this.opcode == 0x07) {

                try {
                    System.out.println("S- ERROR: " + this.datagram.readString(7, this.opcode) + "\n");
                } catch (IOException | OpcodeException e) {
                    if (e instanceof IOException) writeErrorToStream(e.getMessage(), ErrorType.TIMEOUT);
                    if (e instanceof OpcodeException) writeErrorToStream(e.getMessage(), ErrorType.WRONG_OPCODE);
                    break;
                }
            }
        }
    }

    /**
     * Method that write error to stream.
     *
     * @param s         exception message.
     * @param errorType type of error to write.
     */
    private void writeErrorToStream(String s, ErrorType errorType) {
        System.out.println("S- EXCEPTION: " + s);
        try {
            String errorMessage = this.database.getErrorByEnum(errorType);
            this.datagram.writeString(7, errorMessage);
            System.out.println("C- ERROR: " + errorMessage);
        } catch (IOException e2) {
            System.out.println("C- EXCEPTION: " + e2.getMessage());
        }
    }

    /**
     * Game in automatic mode.
     */
    public void automaticMode() {

        while (true) {
            if (this.newGame) {

                this.client.setName("IA Player");
                this.dp = new DatabaseProvider(this.database.getInsults(), this.database.getComebacks());

                do {
                    contained = this.client.containsWithAddInsultComeback(this.dp.getRandomInsultComeback());
                } while (contained);

                try {
                    this.datagram.writeIntString(1, this.client.generateId(), this.client.getName());
                    System.out.println("C- HELLO: " + this.client.getId() + " " + this.client.getName());
                } catch (IOException e) {
                    System.out.println("C- EXCEPTION: " + e.getMessage());
                    break;
                }
                this.newGame = false;
            }

            try {
                this.opcode = this.datagram.readByte();
            } catch (Exception e) {
                writeErrorToStream(e.getMessage(), ErrorType.WRONG_OPCODE);
            }

            if (this.opcode == 0x01) {

                try {
                    String[] str = this.datagram.readIntString(1, this.opcode);
                    this.server.setId(Integer.parseInt(str[0]));
                    this.server.setName(str[1]);
                    System.out.println("S- HELLO: " + this.server.getId() + " " + this.server.getName());
                } catch (IOException | OpcodeException e) {
                    if (e instanceof IOException) writeErrorToStream(e.getMessage(), ErrorType.TIMEOUT);
                    if (e instanceof OpcodeException) writeErrorToStream(e.getMessage(), ErrorType.WRONG_OPCODE);
                    break;
                }

                try {
                    this.datagram.writeHash(2, this.client.generateSecret());
                    this.client.setHash(Functions.toHash(this.client.getSecret()));
                    System.out.println("C- HASH: " + Functions.encodeHexString(this.client.getHash()));
                } catch (IOException e) {
                    System.out.println("C- EXCEPTION: " + e.getMessage());
                    break;
                }

            } else if (this.opcode == 0x02) {

                do {
                    contained = this.client.containsWithAddInsultComeback(this.dp.getRandomInsultComeback());
                } while (contained);

                try {
                    this.server.setHash(this.datagram.readHash(2, this.opcode));
                    System.out.println("S- HASH: " + Functions.encodeHexString(this.server.getHash()) );
                } catch (IOException | OpcodeException e) {
                    if (e instanceof IOException) writeErrorToStream(e.getMessage(), ErrorType.TIMEOUT);
                    if (e instanceof OpcodeException) writeErrorToStream(e.getMessage(), ErrorType.WRONG_OPCODE);
                    break;
                }

                try {
                    this.datagram.writeString(3, this.client.getSecret());
                    System.out.println("C- SECRET: " + this.client.getSecret());
                } catch (IOException e) {
                    System.out.println("C- EXCEPTION: " + e.getMessage());
                    break;
                }

            } else if (this.opcode == 0x03) {

                try {
                    this.server.setSecret(this.datagram.readString(3, this.opcode));
                    System.out.println("S- SECRET: " + this.server.getSecret());
                } catch (IOException | OpcodeException e) {
                    if (e instanceof IOException) writeErrorToStream(e.getMessage(), ErrorType.TIMEOUT);
                    if (e instanceof OpcodeException) writeErrorToStream(e.getMessage(), ErrorType.WRONG_OPCODE);
                    break;
                }

                if (Functions.proofHash(this.server.getSecret(), this.server.getHash())) { // Decide turn
                    if (this.client.getId() != this.server.getId()) {
                        if (Functions.isEven(client.getSecret(), server.getSecret()) ^ (client.getId() > server.getId())) {

                            this.insult = this.client.getRandomInsult();
                            try {
                                this.datagram.writeString(4, this.insult);
                                System.out.println("------------------------------------------------------------------------------------");
                                System.out.println("C- INSULT: " + this.insult);
                            } catch (IOException e) {
                                System.out.println("C- EXCEPTION: " + e.getMessage());
                                break;
                            }
                        }
                    } else {
                        this.writeErrorToStream("C- EXCEPTION: SAME ID", ErrorType.SAME);
                    }
                } else {
                    this.writeErrorToStream("C- EXCEPTION: NOT COINCIDENT HASH", ErrorType.SAME);
                }

            } else if (this.opcode == 0x04) {

                try {
                    this.opponentInsult = this.datagram.readString(4, this.opcode);
                    System.out.println("------------------------------------------------------------------------------------");
                    System.out.println("INSULT: " + this.opponentInsult);
                } catch (IOException | OpcodeException e) {
                    if (e instanceof IOException) writeErrorToStream(e.getMessage(), ErrorType.TIMEOUT);
                    if (e instanceof OpcodeException) writeErrorToStream(e.getMessage(), ErrorType.WRONG_OPCODE);
                    break;
                }

                if (this.database.isInsult(this.opponentInsult)) {
                    this.client.addInsult(this.opponentInsult);

                    this.comeback = this.client.getRandomComeback();
                    try {
                        this.datagram.writeString(5, this.comeback);
                        System.out.println("COMEBACK: " + this.comeback);
                        System.out.println("------------------------------------------------------------------------------------");
                    } catch (IOException e) {
                        System.out.println("C- EXCEPTION: " + e.getMessage());
                        break;
                    }

                    if (this.database.isRightComeback(this.opponentInsult, this.comeback)) {
                        this.client.addRound();
                        if (this.client.getRound() == 2) this.client.addDuel();
                        if (this.client.getRound() < 2) {
                            this.insult = this.client.getRandomInsult();
                            try {
                                this.datagram.writeString(4, this.insult);
                                System.out.println("------------------------------------------------------------------------------------");
                                System.out.println("C- INSULT: " + this.insult);
                            } catch (IOException e) {
                                System.out.println("C- EXCEPTION: " + e.getMessage());
                                break;
                            }
                        }
                    } else {
                        this.server.addRound();
                        if (this.server.getRound() == 2) this.server.addDuel();
                    }
                } else {
                    this.writeErrorToStream("C- EXCEPTION: MESSAGE INCOMPLETE", ErrorType.INCOMPLETE_MESSAGE);
                    break;
                }

                /* CLIENT - WIN GAME - WIN DUEL */
                if (this.client.getDuel() == 3 | this.client.getRound() == 2) {

                    try {
                        this.datagram.writeString(6, this.database.getShoutByEnumAddName(ShoutType.I_WIN, this.server.getName()));
                        System.out.println("C- SHOUT: " + this.database.getShoutByEnumAddName(ShoutType.I_WIN, this.server.getName()));
                    } catch (IOException e) {
                        System.out.println("C- ERROR: " + e.getMessage());
                        break;
                    }
                }

                /* SERVER - WIN GAME - WIN DUEL */
                if (this.server.getDuel() == 3 | this.server.getRound() == 2) {

                    try {
                        this.datagram.writeString(6, this.database.getShoutByEnumAddName(ShoutType.YOU_WIN, this.server.getName()));
                        System.out.println("C- SHOUT: " + this.database.getShoutByEnumAddName(ShoutType.YOU_WIN, this.server.getName()));
                    } catch (IOException e) {
                        System.out.println("C- ERROR: " + e.getMessage());
                        break;
                    }
                }

            } else if (this.opcode == 0x05) {

                try {
                    this.opponentComeback = this.datagram.readString(5, this.opcode);
                    System.out.println("S- COMEBACK: " + this.opponentComeback);
                    System.out.println("------------------------------------------------------------------------------------");
                } catch (IOException | OpcodeException e) {
                    if (e instanceof IOException) writeErrorToStream(e.getMessage(), ErrorType.TIMEOUT);
                    if (e instanceof OpcodeException) writeErrorToStream(e.getMessage(), ErrorType.WRONG_OPCODE);
                    break;
                }

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

                            this.insult = this.client.getRandomInsult();
                            try {
                                this.datagram.writeString(4, this.insult);
                                System.out.println("------------------------------------------------------------------------------------");
                                System.out.println("C- INSULT: " + this.insult);
                            } catch (IOException e) {
                                System.out.println("C- ERROR: " + e.getMessage());
                                break;
                            }
                        }
                    }
                } else {
                    this.writeErrorToStream("C- EXCEPTION: MESSAGE INCOMPLETE", ErrorType.INCOMPLETE_MESSAGE);
                    break;
                }

                /* CLIENT - WIN GAME - WIN DUEL */
                if (this.client.getDuel() == 3 | this.client.getRound() == 2) {

                    try {
                        this.datagram.writeString(6, this.database.getShoutByEnumAddName(ShoutType.I_WIN, this.server.getName()));
                        System.out.println("C- SHOUT: " + this.database.getShoutByEnumAddName(ShoutType.I_WIN, this.server.getName()));
                    } catch (IOException e) {
                        System.out.println("C- EXCEPTION: " + e.getMessage());
                        break;
                    }
                }

                /* SERVER - WIN GAME - WIN DUEL */
                if (this.server.getDuel() == 3 | this.server.getRound() == 2) {

                    try {
                        this.datagram.writeString(6, this.database.getShoutByEnumAddName(ShoutType.YOU_WIN, this.server.getName()));
                        System.out.println("C- SHOUT: " + this.database.getShoutByEnumAddName(ShoutType.YOU_WIN, this.server.getName()));
                    } catch (IOException e) {
                        System.out.println("C- EXCEPTION: " + e.getMessage());
                        break;
                    }
                }

            } else if (this.opcode == 0x06) {

                try {
                    System.out.println("S- SHOUT: " + this.datagram.readString(6, this.opcode) + "\n");
                } catch (IOException | OpcodeException e) {
                    if (e instanceof IOException) writeErrorToStream(e.getMessage(), ErrorType.TIMEOUT);
                    if (e instanceof OpcodeException) writeErrorToStream(e.getMessage(), ErrorType.WRONG_OPCODE);
                    break;
                }

                /* WIN GAME */
                if (this.client.getDuel() == 3 | this.server.getDuel() == 3) {
                    this.client.resetDuel();
                    this.server.resetDuel();
                    System.out.println("[Connexion closed]");
                    break;

                } else {
                    try {
                        this.datagram.writeHash(2, this.client.generateSecret());
                        this.client.setHash(Functions.toHash(this.client.getSecret()));
                        System.out.println("C- HASH: " + Functions.encodeHexString(this.client.getHash()));
                    } catch (IOException e) {
                        System.out.println("C- EXCEPTION: " + e.getMessage());
                        break;
                    }
                }

                this.client.resetRound();
                this.server.resetRound();

            } else if (this.opcode == 0x07) {

                try {
                    System.out.println("S- ERROR: " + this.datagram.readString(7, this.opcode) + "\n");
                } catch (IOException | OpcodeException e) {
                    if (e instanceof IOException) writeErrorToStream(e.getMessage(), ErrorType.TIMEOUT);
                    if (e instanceof OpcodeException) writeErrorToStream(e.getMessage(), ErrorType.WRONG_OPCODE);
                    break;
                }
            }
        }
    }
}
