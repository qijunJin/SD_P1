package server;

import shared.database.Database;
import shared.enumType.ErrorType;
import shared.enumType.ShoutType;
import shared.exception.OpcodeException;
import shared.functions.Functions;
import shared.model.DatabaseProvider;
import shared.model.Player;
import utils.Datagram;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Game class
 * The logic of both single player and multiplayer mode of the game.
 */
public class Game implements Functions {

    private final BufferedWriter log;
    private final Database database = new Database();
    private final Player server = new Player();
    private final Player client = new Player();
    private final Player client2 = new Player();

    private DatabaseProvider dp;
    private Datagram datagram1;
    private Datagram datagram2;

    private String clientShout, serverShout;
    private String insult, comeback;
    private int opcode1, opcode2;
    private boolean gameBool = true;
    private boolean turn = true;
    private boolean key1 = false;
    private boolean key2 = false;

    /**
     * Constructor of game
     *
     * @param datagram1 instance of datagram 1.
     * @param datagram2 instance of datagram 2.
     * @throws IOException IOException.
     */
    public Game(Datagram datagram1, Datagram datagram2) throws IOException {

        this.datagram1 = datagram1;
        if (datagram2 != null) this.datagram2 = datagram2;

        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy_HH.mm.ss");
        String lg = "Server_" + Thread.currentThread().getId() + "_" + formatter.format(new Date()) + ".log"; // File name
        boolean directoryCreated = (new File("../../logs")).mkdir(); // Directory
        this.log = new BufferedWriter(new FileWriter("../../logs/" + lg));
    }

    /**
     * Method to start the game.
     *
     * @throws IOException IOException.
     */
    public void run() throws IOException {
        if (this.datagram2 == null) this.singlePlayer();
        else this.multiPlayer();
        this.log.close();
    }

    /**
     * Game in single player mode.
     *
     * @throws IOException IOException.
     */
    public void singlePlayer() throws IOException {

        while (gameBool) {

            try {
                this.opcode1 = this.datagram1.readByte();
            } catch (Exception e) {
                this.log.write("[Connexion closed]");
                this.log.flush();
                this.gameBool = false;
                break;
            }

            boolean contained;
            String opponentInsult, opponentComeback;
            switch (this.opcode1) {

                case 0x01:

                    /* SET DATA */
                    this.server.setName("AlphaGo");
                    this.server.resetInsultsComebacks(); // Renew INSULTS and COMEBACKS
                    this.dp = new DatabaseProvider(this.database.getInsults(), this.database.getComebacks());

                    /* ADD RANDOM INSULT-COMEBACK */
                    do {
                        contained = this.server.containsWithAddInsultComeback(this.dp.getRandomInsultComeback());
                    } while (contained);

                    /* READ HELLO */
                    try {
                        String[] str = this.datagram1.readIntString(1, this.opcode1);
                        this.client.setId(Integer.parseInt(str[0]));
                        this.client.setName(str[1]);
                    } catch (IOException | OpcodeException e) {
                        this.log.write("S- EXCEPTION: " + e.getMessage());
                        this.log.flush();
                        this.gameBool = false;
                        break;
                    }

                    /* LOG OUTPUT */
                    this.log.write("C- HELLO: " + this.client.getId() + " " + this.client.getName() + "\n");
                    this.log.flush();

                    /* WRITE HELLO */
                    try {
                        this.datagram1.writeIntString(1, this.server.generateId(), this.server.getName());
                    } catch (IOException e) {
                        this.log.write("S- EXCEPTION: " + e.getMessage());
                        this.log.flush();
                        this.gameBool = false;
                        break;
                    }

                    /* LOG OUTPUT */
                    this.log.write("S- HELLO: " + this.server.getId() + " " + this.server.getName() + "\n");
                    this.log.flush();

                    break;

                case 0x02:

                    /* ADD RANDOM INSULT-COMEBACK */
                    do {
                        contained = this.server.containsWithAddInsultComeback(this.dp.getRandomInsultComeback());
                    } while (contained);

                    /* READ HASH */
                    try {
                        this.client.setHash(this.datagram1.readHash(2, this.opcode1));
                    } catch (IOException | OpcodeException e) {
                        this.log.write("S- EXCEPTION: " + e.getMessage());
                        this.log.flush();
                        this.gameBool = false;
                        break;
                    }

                    /* LOG OUTPUT */
                    this.log.write("C- HASH: " + Functions.encodeHexString(this.client.getHash()) + "\n");
                    this.log.flush();

                    /* WRITE HASH */
                    try {
                        this.datagram1.writeHash(2, this.server.generateSecret());
                    } catch (IOException e) {
                        this.log.write("S- EXCEPTION: " + e.getMessage());
                        this.log.flush();
                        this.gameBool = false;
                        break;
                    }

                    /* LOG OUTPUT */
                    this.log.write("S- HASH: " + Functions.encodeHexString(this.server.getHash()) + "\n");
                    this.log.flush();

                    break;

                case 0x03:

                    /* READ SECRET */
                    try {
                        this.client.setSecret(this.datagram1.readString(3, this.opcode1));
                    } catch (IOException | OpcodeException e) {
                        this.log.write("S- EXCEPTION: " + e.getMessage());
                        this.log.flush();
                        this.gameBool = false;
                        break;
                    }

                    /* LOG OUTPUT */
                    this.log.write("S- SECRET: " + this.client.getSecret() + "\n");
                    this.log.flush();

                    /* WRITE SECRET */
                    try {
                        this.datagram1.writeString(3, this.server.getSecret());
                    } catch (IOException e) {
                        this.log.write("S- EXCEPTION: " + e.getMessage());
                        this.log.flush();
                        this.gameBool = false;
                        break;
                    }

                    this.log.write("C- SECRET: " + this.server.getSecret() + "\n");
                    this.log.flush();

                    /* PROOF HASH - NOT EQUAL ID - EVEN/ODD ^ GREATER/LESSER -> DECIDE STATE */
                    if (Functions.proofHash(this.client.getSecret(), this.client.getHash())) {
                        if (this.server.getId() != this.client.getId()) {
                            if (Functions.isEven(this.server.getSecret(), this.client.getSecret()) ^ (this.server.getId() > this.client.getId())) {

                                /* WRITE INSULT */
                                this.insult = this.server.getRandomInsult();
                                try {
                                    this.datagram1.writeString(4, this.insult);
                                    this.log.write("S- INSULT: " + this.insult + "\n");
                                    this.log.flush();
                                } catch (IOException e) {
                                    this.log.write("S- EXCEPTION: " + e.getMessage());
                                    this.log.flush();
                                    this.gameBool = false;
                                    break;
                                }
                            }
                        } else {
                            try {
                                this.datagram1.writeString(7, this.database.getErrorByEnum(ErrorType.SAME_ID));
                                this.log.write("S- ERROR: " + this.database.getErrorByEnum(ErrorType.SAME_ID));
                                this.log.write("[Connexion closed]");
                                this.gameBool = false;
                                break;
                            } catch (IOException e) {
                                this.log.write("S- EXCEPTION: " + e.getMessage());
                                this.log.flush();
                                this.gameBool = false;
                                break;
                            }
                        }
                    } else {
                        try {
                            this.datagram1.writeString(7, this.database.getErrorByEnum(ErrorType.NOT_IDENTIFIED));
                            this.log.write("S- ERROR: " + this.database.getErrorByEnum(ErrorType.NOT_IDENTIFIED) + "\n");
                            this.log.write("[Connexion closed]");
                            this.gameBool = false;
                            break;
                        } catch (IOException e) {
                            this.log.write("S- EXCEPTION: " + e.getMessage());
                            this.log.flush();
                            this.gameBool = false;
                            break;
                        }
                    }

                    break;

                case 0x04:

                    /* READ INSULT */
                    try {
                        opponentInsult = this.datagram1.readString(4, this.opcode1);
                    } catch (IOException | OpcodeException e) {
                        this.log.write("S- EXCEPTION: " + e.getMessage());
                        this.log.flush();
                        this.gameBool = false;
                        break;
                    }

                    this.log.write("C- INSULT: " + opponentInsult + "\n");
                    this.log.flush();

                    /* ADD COMEBACK AS LEARNED */
                    if (this.database.isInsult(opponentInsult)) {

                        this.server.addInsult(opponentInsult);

                        /* SELECT & WRITE COMEBACK */
                        this.comeback = this.server.getRandomComeback();

                        try {
                            this.datagram1.writeString(5, this.comeback);
                        } catch (IOException e) {
                            this.log.write("S- EXCEPTION: " + e.getMessage());
                            this.log.flush();
                            this.gameBool = false;
                            break;
                        }

                        this.log.write("S- COMEBACK: " + this.comeback + "\n");
                        this.log.flush();

                        /* CHECK INSULT - COMEBACK WINNER */
                        if (this.database.isRightComeback(opponentInsult, this.comeback)) {
                            this.server.addRound();

                            /* WRITE INSULT */
                            if (this.server.getRound() < 2) {
                                this.insult = this.server.getRandomInsult();
                                try {
                                    this.datagram1.writeString(4, this.insult);
                                    this.log.write("S- INSULT: " + this.insult + "\n");
                                    this.log.flush();
                                    break;
                                } catch (IOException e) {
                                    this.log.write("S- EXCEPTION: " + e.getMessage());
                                    this.log.flush();
                                    this.gameBool = false;
                                    break;
                                }
                            }

                            if (this.server.getRound() == 2) {
                                this.server.addDuel();

                                if (this.server.getDuel() == 3) {
                                    /* WRITE SHOUT */
                                    try {
                                        serverShout = this.database.getShoutByEnumAddName(ShoutType.I_WIN, this.client.getName());
                                        this.datagram1.writeString(6, serverShout);
                                        this.log.write("S- SHOUT: " + serverShout + "\n");
                                        this.log.flush();
                                    } catch (IOException e) {
                                        this.log.write("S- EXCEPTION: " + e.getMessage());
                                        this.log.flush();
                                        this.gameBool = false;
                                        break;
                                    }

                                    this.server.resetRound();
                                    this.client.resetRound();
                                    this.client.resetDuel();
                                    this.server.resetDuel();

                                } else {

                                    /* WRITE SHOUT */
                                    try {
                                        serverShout = this.database.getShoutByEnumAddName(ShoutType.I_WIN, this.client.getName());
                                        this.datagram1.writeString(6, serverShout);
                                        this.log.write("S- SHOUT: " + serverShout + "\n");
                                        this.log.flush();
                                    } catch (IOException e) {
                                        this.log.write("S- EXCEPTION: " + e.getMessage());
                                        this.log.flush();
                                        this.gameBool = false;
                                        break;
                                    }

                                    this.server.resetRound();
                                    this.client.resetRound();

                                }
                            }

                        } else {
                            this.client.addRound();

                            if (this.client.getRound() == 2) {
                                this.client.addDuel();

                                if (this.client.getDuel() == 3) {

                                    /* WRITE SHOUT */
                                    try {
                                        serverShout = this.database.getShoutByEnumAddName(ShoutType.YOU_WIN_FINAL, this.client.getName());
                                        this.datagram1.writeString(6, serverShout);
                                        this.log.write("S- SHOUT: " + serverShout + "\n");
                                        this.log.flush();
                                    } catch (IOException e) {
                                        this.log.write("S- EXCEPTION: " + e.getMessage());
                                        this.log.flush();
                                        this.gameBool = false;
                                        break;
                                    }

                                    this.server.resetRound();
                                    this.client.resetRound();
                                    this.client.resetDuel();
                                    this.server.resetDuel();

                                } else {

                                    /* WRITE SHOUT */
                                    try {
                                        serverShout = this.database.getShoutByEnumAddName(ShoutType.YOU_WIN, this.client.getName());
                                        this.datagram1.writeString(6, serverShout);
                                        this.log.write("S- SHOUT: " + serverShout + "\n");
                                        this.log.flush();
                                    } catch (IOException e) {
                                        this.log.write("S- EXCEPTION: " + e.getMessage());
                                        this.log.flush();
                                        this.gameBool = false;
                                        break;
                                    }

                                    this.server.resetRound();
                                    this.client.resetRound();

                                }
                            }
                        }
                    } else {
                        try {
                            this.datagram1.writeString(7, this.database.getErrorByEnum(ErrorType.INCOMPLETE_MESSAGE));
                            this.gameBool = false;
                            break;
                        } catch (IOException e) {
                            this.log.write("S- EXCEPTION: " + e.getMessage());
                            this.log.flush();
                            this.gameBool = false;
                            break;
                        }
                    }

                    break;

                case 0x05:

                    /* READ COMEBACK */
                    try {
                        opponentComeback = this.datagram1.readString(5, this.opcode1);
                    } catch (IOException | OpcodeException e) {
                        this.log.write("S- EXCEPTION: " + e.getMessage());
                        this.log.flush();
                        this.gameBool = false;
                        break;
                    }

                    this.log.write("C- COMEBACK: " + opponentComeback + "\n");
                    this.log.flush();

                    /* ADD COMEBACK AS LEARNED */
                    if (this.database.isComeback(opponentComeback)) {
                        this.server.addComeback(opponentComeback);

                        /* CHECK INSULT - COMEBACK WINNER */
                        if (this.database.isRightComeback(this.insult, opponentComeback)) {
                            this.client.addRound();

                            if (this.client.getRound() == 2) {
                                this.client.addDuel();

                                if (this.client.getDuel() == 3) {

                                    /* WRITE SHOUT */
                                    try {
                                        serverShout = this.database.getShoutByEnumAddName(ShoutType.YOU_WIN_FINAL, this.client.getName());
                                        this.datagram1.writeString(6, serverShout);
                                        this.log.write("S- SHOUT: " + serverShout + "\n");
                                        this.log.flush();
                                    } catch (IOException e) {
                                        this.log.write("S- EXCEPTION: " + e.getMessage());
                                        this.log.flush();
                                        this.gameBool = false;
                                        break;
                                    }

                                    this.server.resetRound();
                                    this.client.resetRound();
                                    this.client.resetDuel();
                                    this.server.resetDuel();

                                } else {

                                    /* WRITE SHOUT */
                                    try {
                                        serverShout = this.database.getShoutByEnumAddName(ShoutType.YOU_WIN, this.client.getName());
                                        this.datagram1.writeString(6, serverShout);
                                        this.log.write("S- SHOUT: " + serverShout + "\n");
                                        this.log.flush();
                                    } catch (IOException e) {
                                        this.log.write("S- EXCEPTION: " + e.getMessage());
                                        this.log.flush();
                                        this.gameBool = false;
                                        break;
                                    }

                                    this.server.resetRound();
                                    this.client.resetRound();

                                }
                            }

                        } else {

                            this.server.addRound();

                            /* WRITE INSULT */
                            if (this.server.getRound() < 2) {
                                this.insult = this.server.getRandomInsult();

                                try {
                                    this.datagram1.writeString(4, this.insult);
                                    this.log.write("S- INSULT: " + this.insult + "\n");
                                    this.log.flush();
                                } catch (IOException e) {
                                    this.log.write("S- EXCEPTION: " + e.getMessage());
                                    this.log.flush();
                                    this.gameBool = false;
                                    break;
                                }
                            }

                            if (this.server.getRound() == 2) {
                                this.server.addDuel();

                                if (this.server.getDuel() == 3) {
                                    /* WRITE SHOUT */
                                    try {
                                        serverShout = this.database.getShoutByEnumAddName(ShoutType.I_WIN, this.client.getName());
                                        this.datagram1.writeString(6, serverShout);
                                        this.log.write("S- SHOUT: " + serverShout + "\n");
                                        this.log.flush();
                                    } catch (IOException e) {
                                        this.log.write("S- EXCEPTION: " + e.getMessage());
                                        this.log.flush();
                                        this.gameBool = false;
                                        break;
                                    }

                                    this.server.resetRound();
                                    this.client.resetRound();
                                    this.client.resetDuel();
                                    this.server.resetDuel();

                                } else {

                                    /* WRITE SHOUT */
                                    try {
                                        serverShout = this.database.getShoutByEnumAddName(ShoutType.I_WIN, this.client.getName());
                                        this.datagram1.writeString(6, serverShout);
                                        this.log.write("S- SHOUT: " + serverShout + "\n");
                                        this.log.flush();
                                    } catch (IOException e) {
                                        this.log.write("S- EXCEPTION: " + e.getMessage());
                                        this.log.flush();
                                        this.gameBool = false;
                                        break;
                                    }

                                    this.server.resetRound();
                                    this.client.resetRound();

                                }
                            }
                        }
                    } else {
                        try {
                            this.datagram1.writeString(7, this.database.getErrorByEnum(ErrorType.INCOMPLETE_MESSAGE));
                            this.gameBool = false;
                            break;
                        } catch (IOException e) {
                            this.log.write("S- EXCEPTION: " + e.getMessage());
                            this.log.flush();
                            this.gameBool = false;
                            break;
                        }
                    }

                    break;

                case 0x06:

                    /* READ SHOUT */
                    try {
                        clientShout = this.datagram1.readString(6, this.opcode1);
                    } catch (IOException | OpcodeException e) {
                        this.log.write("S- EXCEPTION: " + e.getMessage());
                        this.log.flush();
                        this.gameBool = false;
                        break;
                    }

                    /* LOG OUTPUT */
                    this.log.write("C- SHOUT: " + clientShout + "\n");
                    this.log.flush();

                    break;

                case 0x07:

                    /* READ ERROR */
                    try {
                        this.log.write("S- ERROR: " + this.datagram1.readString(7, this.opcode1));
                        this.gameBool = false;
                    } catch (IOException | OpcodeException e) {
                        this.log.write("S- EXCEPTION: " + e.getMessage());
                        this.log.flush();
                        this.gameBool = false;
                        break;
                    }

                    break;
            }
        }
    }

    /**
     * Game in multiplayer mode.
     *
     * @throws IOException IOException.
     */
    public void multiPlayer() throws IOException {

        while (gameBool) {

            if (turn) {
                try {
                    this.opcode1 = this.datagram1.readByte();
                } catch (Exception e) {
                    this.log.write("C1- [Connexion closed]\n");
                    this.log.flush();
                    try {
                        this.opcode2 = this.datagram2.readByte();
                        if (opcode2 == 0x07) {
                            this.log.write("C2- ERROR: " + this.datagram2.readString(7, this.opcode2) + "\n");
                            this.log.write("C2- [Connexion closed]");
                            this.log.flush();
                        }
                    } catch (Exception e3) {
                        this.log.write("C2- [Connexion closed]");
                        this.log.flush();
                        break;
                    }
                    break;
                }
            } else {
                try {
                    this.opcode2 = this.datagram2.readByte();
                } catch (Exception e) {
                    this.log.write("C2- [Connexion closed]\n");
                    this.log.flush();
                    try {
                        this.opcode1 = this.datagram1.readByte();
                        if (opcode1 == 0x07) {
                            this.log.write("C1- ERROR: " + this.datagram1.readString(7, this.opcode1) + "\n");
                            this.log.write("C1- [Connexion closed]");
                            this.log.flush();
                        }
                    } catch (Exception e3) {
                        this.log.write("C1- [Connexion closed]");
                        this.log.flush();
                        break;
                    }
                    break;
                }
            }


            if (this.opcode1 == 0x01) {

                /* READ HELLO PLAYER1 */
                try {
                    String[] str = this.datagram1.readIntString(1, this.opcode1);
                    this.client.setId(Integer.parseInt(str[0]));
                    this.client.setName(str[1]);
                } catch (IOException | OpcodeException e) {
                    this.log.write("C1- EXCEPTION: " + e.getMessage() + "\n");
                    this.log.flush();
                    break;
                }

                /* WRITE HELLO PLAYER1 -> PLAYER2 */
                try {
                    this.datagram2.writeIntString(1, this.client.getId(), this.client.getName());
                } catch (IOException e) {
                    this.log.write("C2- EXCEPTION: " + e.getMessage() + "\n");
                    this.log.flush();
                    break;
                }

                /* LOG OUTPUT */
                this.log.write("C1- HELLO: " + this.client.getId() + " " + this.client.getName() + "\n");
                this.log.flush();
                this.opcode1 = 0x00;
                turn = false;

            } else if (this.opcode2 == 0x01) {

                /* READ HELLO PLAYER2 */
                try {
                    String[] str = this.datagram2.readIntString(1, this.opcode2);
                    this.client2.setId(Integer.parseInt(str[0]));
                    this.client2.setName(str[1]);
                } catch (IOException | OpcodeException e) {
                    this.log.write("C2- EXCEPTION: " + e.getMessage() + "\n");
                    this.log.flush();
                    break;
                }

                /* WRITE HELLO PLAYER2 -> PLAYER1 */
                try {
                    this.datagram1.writeIntString(1, this.client2.getId(), this.client2.getName());
                } catch (IOException e) {
                    this.log.write("C1- EXCEPTION: " + e.getMessage() + "\n");
                    this.log.flush();
                    break;
                }

                /* LOG OUTPUT */
                this.log.write("C2- HELLO: " + this.client2.getId() + " " + this.client2.getName() + "\n");
                this.log.flush();
                this.opcode2 = 0x00;
                turn = true;

            } else if (this.opcode1 == 0x02) {

                /* READ HASH PLAYER1 */
                try {
                    this.client.setHash(this.datagram1.readHash(2, this.opcode1));
                } catch (IOException | OpcodeException e) {
                    this.log.write("C1- EXCEPTION: " + e.getMessage() + "\n");
                    this.log.flush();
                    break;
                }

                /* WRITE HASH PLAYER1 to PLAYER2*/
                try {
                    this.datagram2.writeHashArray(2, this.client.getHash());
                } catch (IOException e) {
                    this.log.write("C2- EXCEPTION: " + e.getMessage() + "\n");
                    this.log.flush();
                    break;
                }

                /* LOG OUTPUT */
                this.log.write("C1- HASH: " + Functions.encodeHexString(this.client.getHash()) + "\n");
                this.log.flush();
                this.opcode1 = 0x00;
                turn = false;

            } else if (this.opcode2 == 0x02) {

                /* READ HASH PLAYER2 */
                try {
                    this.client2.setHash(this.datagram2.readHash(2, this.opcode2));
                } catch (IOException | OpcodeException e) {
                    this.log.write("C2- EXCEPTION: " + e.getMessage() + "\n");
                    this.log.flush();
                    break;
                }

                /* WRITE HASH PLAYER2 to PLAYER1*/
                try {
                    this.datagram1.writeHashArray(2, this.client2.getHash());
                } catch (IOException e) {
                    this.log.write("C1- EXCEPTION: " + e.getMessage() + "\n");
                    this.log.flush();
                    break;
                }

                /* LOG OUTPUT */

                this.log.write("C2- HASH: " + Functions.encodeHexString(this.client2.getHash()) + "\n");
                this.log.flush();
                this.opcode2 = 0x00;
                turn = true;

            } else if (this.opcode1 == 0x03) {

                /* READ SECRET */
                try {
                    this.client.setSecret(this.datagram1.readString(3, this.opcode1));
                } catch (IOException | OpcodeException e) {
                    this.log.write("C1- EXCEPTION: " + e.getMessage() + "\n");
                    this.log.flush();
                    break;
                }

                /* WRITE SECRET PLAYER1 -> PLAYER2 */
                try {
                    this.datagram2.writeString(3, this.client.getSecret());
                } catch (IOException e) {
                    this.log.write("C2- EXCEPTION: " + e.getMessage() + "\n");
                    this.log.flush();
                    break;
                }

                this.log.write("C1- SECRET: " + this.client.getSecret() + "\n");
                this.log.flush();

                this.opcode1 = 0x00;

                if (!Functions.proofHash(this.client.getSecret(), this.client.getHash())) {
                    try {
                        this.datagram1.writeString(7, this.database.getErrorByEnum(ErrorType.NOT_IDENTIFIED));
                        this.log.write("C1- ERROR: " + this.database.getErrorByEnum(ErrorType.NOT_IDENTIFIED)+"\n");
                        this.log.write("C1- [Connexion closed]");
                        this.gameBool = false;
                        break;
                    } catch (IOException e) {
                        this.log.write("S- EXCEPTION: " + e.getMessage());
                        this.log.flush();
                        this.gameBool = false;
                        break;
                    }
                }

                turn = false;

            } else if (this.opcode2 == 0x03) {

                /* READ SECRET */
                try {
                    this.client2.setSecret(this.datagram2.readString(3, this.opcode2));
                } catch (IOException | OpcodeException e) {
                    this.log.write("C2- EXCEPTION: " + e.getMessage() + "\n");
                    this.log.flush();
                    break;
                }

                /* WRITE SECRET PLAYER2 -> PLAYER1 */
                try {
                    this.datagram1.writeString(3, this.client2.getSecret());
                } catch (IOException e) {
                    this.log.write("C1- EXCEPTION: " + e.getMessage() + "\n");
                    this.log.flush();
                    break;
                }

                this.log.write("C2- SECRET: " + this.client2.getSecret() + "\n");
                this.log.flush();

                this.opcode2 = 0x00;

                if (!Functions.proofHash(this.client2.getSecret(), this.client2.getHash())) {
                    try {
                        this.datagram2.writeString(7, this.database.getErrorByEnum(ErrorType.NOT_IDENTIFIED));
                        this.log.write("C2- ERROR: " + this.database.getErrorByEnum(ErrorType.NOT_IDENTIFIED)+ "\n");
                        this.log.write("C2- [Connexion closed]");
                        this.gameBool = false;
                        break;
                    } catch (IOException e) {
                        this.log.write("S- EXCEPTION: " + e.getMessage());
                        this.log.flush();
                        this.gameBool = false;
                        break;
                    }
                }

                if (this.client.getId() == this.client2.getId()) {
                    try {
                        this.datagram1.writeString(7, this.database.getErrorByEnum(ErrorType.SAME_ID));
                        this.log.write("C1- ERROR: " + this.database.getErrorByEnum(ErrorType.SAME_ID) + "\n");
                        this.log.write("C1- [Connexion closed]"+ "\n");
                        this.gameBool = false;
                    } catch (IOException e) {
                        this.log.write("S- EXCEPTION: " + e.getMessage());
                        this.log.flush();
                        this.gameBool = false;
                    }

                    try {
                        this.datagram2.writeString(7, this.database.getErrorByEnum(ErrorType.SAME_ID));
                        this.log.write("C2- ERROR: " + this.database.getErrorByEnum(ErrorType.SAME_ID)+ "\n");
                        this.log.write("C2- [Connexion closed]");
                        this.gameBool = false;
                    } catch (IOException e) {
                        this.log.write("S- EXCEPTION: " + e.getMessage());
                        this.log.flush();
                        this.gameBool = false;
                        break;
                    }
                }
                turn = Functions.isEven(this.client.getSecret(), this.client2.getSecret()) == this.client2.getId() > this.client.getId();

            } else if (this.opcode1 == 0x04) {

                /* READ INSULT */
                try {
                    this.insult = this.datagram1.readString(4, this.opcode1);
                } catch (OpcodeException e) {
                    this.log.write("C1- EXCEPTION: " + e.getMessage());
                    this.log.flush();
                }

                /* WRITE INSULT */
                try {
                    this.datagram2.writeString(4, this.insult);
                } catch (IOException e) {
                    this.log.write("C2- EXCEPTION: " + e.getMessage());
                    this.log.flush();
                }

                /* LOG OUTPUT */
                this.log.write("C1- INSULT: " + this.insult + "\n");
                this.log.flush();
                this.opcode1 = 0x00;
                turn = false;

            } else if (this.opcode2 == 0x04) {

                /* READ INSULT */
                try {
                    this.insult = this.datagram2.readString(4, this.opcode2);
                } catch (OpcodeException e) {
                    this.log.write("C2- EXCEPTION: " + e.getMessage());
                    this.log.flush();
                }

                /* WRITE INSULT */
                try {
                    this.datagram1.writeString(4, this.insult);
                } catch (IOException e) {
                    this.log.write("C1- EXCEPTION: " + e.getMessage());
                    this.log.flush();
                }

                /* LOG OUTPUT */
                this.log.write("C2- INSULT: " + this.insult + "\n");
                this.log.flush();
                this.opcode2 = 0x00;
                turn = true;

            } else if (this.opcode1 == 0x05) {

                /* READ COMEBACK */
                try {
                    this.comeback = this.datagram1.readString(5, this.opcode1);
                } catch (IOException | OpcodeException e) {
                    this.log.write("C1- EXCEPTION: " + e.getMessage());
                    this.log.flush();
                }

                try {
                    this.datagram2.writeString(5, this.comeback);
                } catch (IOException e) {
                    this.log.write("C2- EXCEPTION: " + e.getMessage());
                    this.log.flush();
                }

                /* LOG OUTPUT */
                this.log.write("C1- COMEBACK: " + this.comeback + "\n");
                this.log.flush();
                this.opcode1 = 0x00;

                this.turn = !this.database.isInsult(this.insult) || this.database.isRightComeback(this.insult, this.comeback);

            } else if (this.opcode2 == 0x05) {

                try {
                    this.comeback = this.datagram2.readString(5, this.opcode2);
                } catch (IOException | OpcodeException e) {
                    this.log.write("C2- EXCEPTION: " + e.getMessage());
                    this.log.flush();
                }

                try {
                    this.datagram1.writeString(5, this.comeback);
                } catch (IOException e) {
                    this.log.write("C1- EXCEPTION: " + e.getMessage());
                    this.log.flush();
                }

                /* LOG OUTPUT */
                this.log.write("C2- COMEBACK: " + this.comeback + "\n");
                this.log.flush();
                this.opcode2 = 0x00;

                this.turn = this.database.isInsult(this.insult) && !this.database.isRightComeback(this.insult, this.comeback);

            } else if (this.opcode1 == 0x06) {

                try {
                    this.clientShout = this.datagram1.readString(6, this.opcode1);
                } catch (IOException | OpcodeException e) {
                    this.log.write("C1- EXCEPTION: " + e.getMessage());
                    this.log.flush();
                    break;
                }

                try {
                    this.datagram2.writeString(6, clientShout);
                } catch (IOException e) {
                    this.log.write("C2- EXCEPTION: " + e.getMessage());
                    this.log.flush();
                    break;
                }

                this.log.write("C1- SHOUT: " + clientShout + "\n");
                this.log.flush();
                this.opcode1 = 0x00;

                key1 = true;
                turn = false;

                if (key2) {
                    key1 = false;
                    key2 = false;
                    turn = true;
                }

            } else if (this.opcode2 == 0x06) {

                try {
                    this.serverShout = this.datagram2.readString(6, this.opcode2);
                } catch (IOException | OpcodeException e) {
                    this.log.write("C2- EXCEPTION: " + e.getMessage());
                    this.log.flush();
                    break;
                }

                try {
                    this.datagram1.writeString(6, serverShout);
                } catch (IOException e) {
                    this.log.write("C1- EXCEPTION: " + e.getMessage());
                    this.log.flush();
                    break;
                }

                this.log.write("C2- SHOUT: " + serverShout + "\n");
                this.log.flush();
                this.opcode2 = 0x00;

                key2 = true;
                if (key1) {
                    key1 = false;
                    key2 = false;
                }
                turn = true;

            } else if (this.opcode1 == 0x07) {

                try {
                    this.log.write("C1- ERROR: " + this.datagram1.readString(7, this.opcode1));
                    this.log.flush();
                } catch (IOException | OpcodeException e) {
                    System.out.println("C1- EXIT");
                }

                this.gameBool = false;
                break;

            } else if (this.opcode2 == 0x07) {

                try {
                    this.log.write("C2- ERROR: " + this.datagram2.readString(7, this.opcode2));
                    this.log.flush();
                } catch (IOException | OpcodeException e) {
                    System.out.println("C2- EXIT");
                }

                this.gameBool = false;
                break;
            }
        }
    }
}
