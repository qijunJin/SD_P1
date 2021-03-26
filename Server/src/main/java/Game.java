import shared.database.Database;
import shared.enumType.ShoutType;
import shared.exception.OpcodeException;
import shared.model.DatabaseProvider;
import shared.model.Player;
import utils.Datagram;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class Game {

    private DatabaseProvider dp;
    private final BufferedWriter log;
    private final Database database;
    Datagram datagram1;
    Datagram datagram2;

    String clientShout, serverShout;
    private final Player server = new Player();
    private final Player client = new Player();
    private final Player client2 = new Player();

    private String opponentInsult, opponentComeback;
    private String insult, comeback;
    private boolean gameBool = true;
    private boolean contained;
    private int opcode1, opcode2;
    private boolean turn, a, b;

    public Game(Socket s1, Socket s2) throws IOException {

        this.database = new Database();
        this.datagram1 = new Datagram(s1);

        if (s2 != null) {
            this.datagram2 = new Datagram(s2);
            turn = true;
            a = false;
            b = false;
        }

        Date asd = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy_HH.mm.ss");
        String lg = "Server_" + Thread.currentThread().getId() + "_" + formatter.format(asd) + ".log"; // File name
        (new File("../../logs")).mkdir(); // Directory
        File f = new File("../../logs/" + lg); // File
        this.log = new BufferedWriter(new FileWriter(f));
    }

    public void run() throws IOException {
        if (this.datagram2 == null) {
            this.singlePlayer();
        } else {
            this.multiPlayer();
        }
        this.log.close();
    }

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
                        this.log.write("S- ERROR: " + e.getMessage());
                        this.log.flush();
                        this.gameBool = false;
                        break;
                    }

                    /* WRITE HELLO */
                    try {
                        this.datagram1.writeIntString(1, this.server.generateId(), this.server.getName());
                    } catch (IOException e) {
                        this.log.write("S- ERROR: " + e.getMessage());
                        this.log.flush();
                        this.gameBool = false;
                        break;
                    }

                    /* LOG OUTPUT */
                    this.log.write("C- HELLO: " + this.client.getId() + " " + this.client.getName() + "\n");
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
                        this.log.write("S- ERROR: " + e.getMessage());
                        this.log.flush();
                        this.gameBool = false;
                        break;
                    }

                    /* WRITE HASH */
                    try {
                        this.datagram1.writeHash(2, this.server.generateSecret());
                        this.server.setHash(this.getHash(this.server.getSecret()));
                    } catch (IOException e) {
                        this.log.write("S- ERROR: " + e.getMessage());
                        this.log.flush();
                        this.gameBool = false;
                        break;
                    }

                    /* LOG OUTPUT */
                    this.log.write("C- HASH: " + Arrays.toString(this.client.getHash()) + "\n");
                    this.log.write("S- HASH: " + Arrays.toString(this.server.getHash()) + "\n");
                    this.log.flush();

                    break;

                case 0x03:

                    /* READ SECRET */
                    try {
                        this.client.setSecret(this.datagram1.readString(3, this.opcode1));
                    } catch (IOException | OpcodeException e) {
                        this.log.write("S- ERROR: " + e.getMessage());
                        this.log.flush();
                        this.gameBool = false;
                        break;
                    }

                    /* WRITE SECRET */
                    try {
                        this.datagram1.writeString(3, this.server.getSecret());
                    } catch (IOException e) {
                        this.log.write("S- ERROR: " + e.getMessage());
                        this.log.flush();
                        this.gameBool = false;
                        break;
                    }

                    /* LOG OUTPUT */
                    this.log.write("C- SECRET: " + this.server.getSecret() + "\n");
                    this.log.write("S- SECRET: " + this.client.getSecret() + "\n");
                    this.log.flush();

                    /* PROOF HASH - NOT EQUAL ID - EVEN/ODD ^ GREATER/LESSER -> DECIDE STATE */
                    if (this.proofHash(this.client.getSecret(), this.client.getHash())) {
                        if (this.server.getId() != this.client.getId()) {
                            if (this.isEven(this.server.getSecret(), this.client.getSecret()) ^ (this.server.getId() > this.client.getId())) {

                                /* WRITE INSULT */
                                this.insult = this.server.getRandomInsult();
                                try {
                                    this.datagram1.writeString(4, this.insult);
                                    this.log.write("S- INSULT: " + this.insult + "\n");
                                    this.log.flush();
                                } catch (IOException e) {
                                    this.log.write("S- ERROR: " + e.getMessage());
                                    this.log.flush();
                                    this.gameBool = false;
                                    break;
                                }
                            }
                        } else {
                            this.log.write("S- ERROR: SAME ID");
                            this.log.flush();
                            this.gameBool = false;
                            break;
                        }
                    } else {
                        this.log.write("S- ERROR: NOT COINCIDENT HASH");
                        this.log.flush();
                        this.gameBool = false;
                        break;
                    }

                    break;

                case 0x04:

                    /* READ INSULT */
                    try {
                        this.opponentInsult = this.datagram1.readString(4, this.opcode1);
                    } catch (IOException | OpcodeException e) {
                        this.log.write("S- ERROR: " + e.getMessage());
                        this.log.flush();
                        this.gameBool = false;
                        break;
                    }
                    this.log.write("C- INSULT: " + this.opponentInsult + "\n");
                    this.log.flush();

                    /* ADD COMEBACK AS LEARNED */
                    if (this.database.isInsult(this.opponentInsult)) {

                        this.server.addInsult(this.opponentInsult);

                        /* SELECT & WRITE COMEBACK */
                        this.comeback = this.server.getRandomComeback();

                        try {
                            this.datagram1.writeString(5, this.comeback);
                        } catch (IOException e) {
                            this.log.write("S- ERROR: " + e.getMessage());
                            this.log.flush();
                            this.gameBool = false;
                            break;
                        }
                        this.log.write("S- COMEBACK: " + this.comeback + "\n");
                        this.log.flush();

                        /* CHECK INSULT - COMEBACK WINNER */
                        if (this.database.isRightComeback(this.opponentInsult, this.comeback)) {
                            this.server.addRound();
                            if (this.server.getRound() == 2) this.server.addDuel();

                            /* WRITE INSULT */
                            if (this.server.getRound() < 2) {
                                this.insult = this.server.getRandomInsult();
                                try {
                                    this.datagram1.writeString(4, this.insult);
                                    this.log.write("S- INSULT: " + this.insult + "\n");
                                    this.log.flush();
                                } catch (IOException e) {
                                    this.log.write("S- ERROR: " + e.getMessage());
                                    this.log.flush();
                                    this.gameBool = false;
                                    break;
                                }
                            }
                        } else {
                            this.client.addRound();
                            if (this.client.getRound() == 2) this.client.addDuel();
                        }
                    } else {
                        this.log.write("S- ERROR: WRONG INSULT");
                        this.log.flush();
                        this.gameBool = false;
                        break;
                    }

                    break;

                case 0x05:

                    /* READ COMEBACK */
                    try {
                        this.opponentComeback = this.datagram1.readString(5, this.opcode1);
                    } catch (IOException | OpcodeException e) {
                        this.log.write("S- ERROR: " + e.getMessage());
                        this.log.flush();
                        this.gameBool = false;
                        break;
                    }

                    this.log.write("C- COMEBACK: " + this.opponentComeback + "\n");
                    this.log.flush();

                    /* ADD COMEBACK AS LEARNED */
                    if (this.database.isComeback(this.opponentComeback)) {
                        this.server.addComeback(this.opponentComeback);

                        /* CHECK INSULT - COMEBACK WINNER */
                        if (this.database.isRightComeback(this.insult, this.opponentComeback)) {
                            this.client.addRound();
                            if (this.client.getRound() == 2) this.client.addDuel();

                        } else {
                            this.server.addRound();
                            if (this.server.getRound() == 2) this.server.addDuel();

                            /* WRITE INSULT */
                            if (this.server.getRound() < 2) {
                                this.insult = this.server.getRandomInsult();
                                try {
                                    this.datagram1.writeString(4, this.insult);
                                    this.log.write("S- INSULT: " + this.insult + "\n");
                                    this.log.flush();
                                } catch (IOException e) {
                                    this.log.write("S- ERROR: " + e.getMessage());
                                    this.log.flush();
                                    this.gameBool = false;
                                    break;
                                }
                            }
                        }
                    } else {
                        this.log.write("S- ERROR: WRONG COMEBACK");
                        this.log.flush();
                        this.gameBool = false;
                        break;
                    }

                    break;

                case 0x06:

                    /* READ SHOUT */
                    try {
                        clientShout = this.datagram1.readString(6, this.opcode1);
                    } catch (IOException | OpcodeException e) {
                        this.log.write("S- ERROR: " + e.getMessage());
                        this.log.flush();
                        this.gameBool = false;
                        break;
                    }

                    /* SERVER - WIN GAME - WIN DUEL */
                    if (this.server.getDuel() == 3 | this.server.getRound() == 2) {

                        /* WRITE SHOUT */
                        try {
                            serverShout = this.database.getShoutByEnumAddName(ShoutType.I_WIN, this.client.getName());
                            this.datagram1.writeString(6, serverShout);
                        } catch (IOException e) {
                            this.log.write("S- ERROR: " + e.getMessage());
                            this.log.flush();
                            this.gameBool = false;
                            break;
                        }

                        /* WIN GAME */
                        if (this.server.getDuel() == 3) {
                            this.client.resetDuel();
                            this.server.resetDuel();
                        }

                        /* WIN GAME OR DUEL */
                        this.server.resetRound();
                        this.client.resetRound();
                    }

                    /* CLIENT - WIN GAME - WIN DUEL */
                    if (this.client.getDuel() == 3 | this.client.getRound() == 2) {

                        /* WIN GAME */
                        if (this.client.getDuel() == 3) {
                            /* WRITE SHOUT */
                            try {
                                serverShout = this.database.getShoutByEnumAddName(ShoutType.YOU_WIN_FINAL, this.client.getName());               //Select SHOUT type message
                                this.datagram1.writeString(6, serverShout);                                                            //Write SHOUT message
                            } catch (IOException e) {
                                this.log.write("S- ERROR: " + e.getMessage());
                                this.log.flush();
                                this.gameBool = false;
                                break;
                            }
                            this.server.resetDuel();
                            this.client.resetDuel();

                            /* WIN DUEL */
                        } else {
                            /* WRITE SHOUT */
                            try {
                                serverShout = this.database.getShoutByEnumAddName(ShoutType.YOU_WIN, this.client.getName());               //Select SHOUT type message
                                this.datagram1.writeString(6, serverShout);                                                            //Write SHOUT message
                            } catch (IOException e) {
                                this.log.write("S- ERROR: " + e.getMessage());
                                this.log.flush();
                                this.gameBool = false;
                                break;
                            }
                        }

                        /* WIN GAME OR DUEL */
                        this.server.resetRound();
                        this.client.resetRound();
                    }

                    /* LOG OUTPUT */
                    this.log.write("C- SHOUT: " + clientShout + "\n");
                    this.log.write("S- SHOUT: " + serverShout + "\n");
                    this.log.flush();

                    break;

                case 0x07:

                    String error;

                    /* READ ERROR */
                    try {
                        error = this.datagram1.readString(7, this.opcode1);
                    } catch (IOException | OpcodeException e) {
                        this.log.write("S- ERROR: " + e.getMessage());
                        this.log.flush();
                        this.gameBool = false;
                        break;
                    }

                    /* LOG OUTPUT */
                    this.log.write("S- ERROR: " + error);
                    this.gameBool = false;
                    break;
            }
        }
    }

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
                    this.log.write("C1- HELLO ERROR READ " + e.getMessage() + "\n");
                    this.log.flush();
                    break;
                }

                /* WRITE HELLO PLAYER1 -> PLAYER2 */
                try {
                    this.datagram2.writeIntString(1, this.client.getId(), this.client.getName());
                } catch (IOException e) {
                    this.log.write("C2- HELLO ERROR WRITE" + e.getMessage() + "\n");
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
                    this.log.write("C2- HELLO ERROR READ " + e.getMessage() + "\n");
                    this.log.flush();
                    break;
                }

                /* WRITE HELLO PLAYER2 -> PLAYER1 */
                try {
                    this.datagram1.writeIntString(1, this.client2.getId(), this.client2.getName());
                } catch (IOException e) {
                    this.log.write("C1- HELLO ERROR WRITE" + e.getMessage() + "\n");
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
                    this.log.write("C1- HASH ERROR READ" + e.getMessage() + "\n");
                    this.log.flush();
                    break;
                }

                /* WRITE HASH PLAYER1 to PLAYER2*/
                try {
                    this.datagram2.writeHashArray(2, this.client.getHash());
                } catch (IOException e) {
                    this.log.write("C2- HASH ERROR WRITE" + e.getMessage() + "\n");
                    this.log.flush();
                    break;
                }

                /* LOG OUTPUT */
                this.log.write("C1- HASH: " + Arrays.toString(this.client.getHash()) + "\n");
                this.log.flush();
                this.opcode1 = 0x00;
                turn = false;

            } else if (this.opcode2 == 0x02) {

                /* READ HASH PLAYER2 */
                try {
                    this.client2.setHash(this.datagram2.readHash(2, this.opcode2));
                } catch (IOException | OpcodeException e) {
                    this.log.write("C2- HASH ERROR READ" + e.getMessage() + "\n");
                    this.log.flush();
                    break;
                }

                /* WRITE HASH PLAYER2 to PLAYER1*/
                try {
                    this.datagram1.writeHashArray(2, this.client2.getHash());
                } catch (IOException e) {
                    this.log.write("C1- HASH ERROR WRITE" + e.getMessage() + "\n");
                    this.log.flush();
                    break;
                }

                /* LOG OUTPUT */

                this.log.write("C2- HASH: " + Arrays.toString(this.client2.getHash()) + "\n");
                this.log.flush();
                this.opcode2 = 0x00;
                turn = true;

            } else if (this.opcode1 == 0x03) {

                /* READ SECRET */
                try {
                    this.client.setSecret(this.datagram1.readString(3, this.opcode1));
                } catch (IOException | OpcodeException e) {
                    this.log.write("SECRET ERROR READ" + e.getMessage() + "\n");
                    this.log.flush();
                    break;
                }

                /* WRITE SECRET PLAYER1 -> PLAYER2 */
                try {
                    this.datagram2.writeString(3, this.client.getSecret());
                } catch (IOException e) {
                    this.log.write("C2- SECRET ERROR WRITE" + e.getMessage() + "\n");
                    this.log.flush();
                    break;
                }

                this.log.write("C1- SECRET: " + this.client.getSecret() + "\n");
                this.log.flush();

                this.opcode1 = 0x00;
                turn = false;

            } else if (this.opcode2 == 0x03) {

                /* READ SECRET */
                try {
                    this.client2.setSecret(this.datagram2.readString(3, this.opcode2));
                } catch (IOException | OpcodeException e) {
                    this.log.write("SECRET ERROR READ" + e.getMessage() + "\n");
                    this.log.flush();
                    break;
                }

                /* WRITE SECRET PLAYER2 -> PLAYER1 */
                try {
                    this.datagram1.writeString(3, this.client2.getSecret());
                } catch (IOException e) {
                    this.log.write("C1- SECRET ERROR WRITE" + e.getMessage() + "\n");
                    this.log.flush();
                    break;
                }

                this.log.write("C2- SECRET: " + this.client2.getSecret() + "\n");
                this.log.flush();

                this.opcode2 = 0x00;

                turn = this.isEven(this.client.getSecret(), this.client2.getSecret()) == this.client2.getId() > this.client.getId();

            } else if (this.opcode1 == 0x04) {

                /* READ INSULT */
                try {
                    this.insult = this.datagram1.readString(4, this.opcode1);
                } catch (OpcodeException e) {
                    this.log.write("ERROR");
                    this.log.flush();
                }

                /* WRITE INSULT */
                try {
                    this.datagram2.writeString(4, this.insult);
                } catch (IOException e) {
                    this.log.write("ERROR");
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
                    this.log.write("ERROR");
                    this.log.flush();
                }

                /* WRITE INSULT */
                try {
                    this.datagram1.writeString(4, this.insult);
                } catch (IOException e) {
                    this.log.write("ERROR");
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
                    this.log.write("C1- COMEBACK ERROR");
                    this.log.flush();
                    break;
                }

                try {
                    this.datagram2.writeString(5, this.comeback);
                } catch (IOException e) {
                    this.log.write("C2- COMEBACK ERROR");
                    this.log.flush();
                    break;
                }

                /* LOG OUTPUT */
                this.log.write("C1- COMEBACK: " + this.comeback + "\n");
                this.log.flush();
                this.opcode1 = 0x00;

                if (this.database.isInsult(this.insult)) {
                    this.turn = this.database.isRightComeback(this.insult, this.comeback);
                } else {
                    this.turn = true;
                }


            } else if (this.opcode2 == 0x05) {

                try {
                    this.comeback = this.datagram2.readString(5, this.opcode2);
                } catch (IOException | OpcodeException e) {
                    this.log.write("COMEBACK ERROR");
                    this.log.flush();
                    break;
                }

                try {
                    this.datagram1.writeString(5, this.comeback);
                } catch (IOException e) {
                    this.log.write("COMEBACK ERROR");
                    this.log.flush();
                    break;
                }

                /* LOG OUTPUT */
                this.log.write("C2- COMEBACK: " + this.comeback + "\n");
                this.log.flush();
                this.opcode2 = 0x00;
                if (this.database.isInsult(this.insult)) {
                    this.turn = !this.database.isRightComeback(this.insult, this.comeback);
                } else {
                    this.turn = false;
                }

            } else if (this.opcode1 == 0x06) {

                try {
                    this.clientShout = this.datagram1.readString(6, this.opcode1);
                } catch (IOException | OpcodeException e) {
                    this.log.write("C1- ERROR SHOUT READ");
                    this.log.flush();
                    break;
                }

                try {
                    this.datagram2.writeString(6, clientShout);
                } catch (IOException e) {
                    this.log.write("C1- ERROR SHOUT WRITE");
                    this.log.flush();
                    break;
                }

                this.log.write("C1- SHOUT: " + clientShout + "\n");
                this.log.flush();
                this.opcode1 = 0x00;

                a = true;
                turn = false;

                if (b) {
                    a = false;
                    b = false;
                    turn = true;
                }

            } else if (this.opcode2 == 0x06) {

                try {
                    this.serverShout = this.datagram2.readString(6, this.opcode2);
                } catch (IOException | OpcodeException e) {
                    this.log.write("C2- ERROR SHOUT READ");
                    this.log.flush();
                    break;
                }

                try {
                    this.datagram1.writeString(6, serverShout);
                } catch (IOException e) {
                    this.log.write("C1- ERROR SHOUT WRITE");
                    this.log.flush();
                    break;
                }

                this.log.write("C2- SHOUT: " + serverShout + "\n");
                this.log.flush();
                this.opcode2 = 0x00;

                b = true;
                if (a) {
                    a = false;
                    b = false;
                }
                turn = true;

            } else if (this.opcode1 == 0x07) {

                String e1 = "";
                try {
                    e1 = this.datagram1.readString(7, this.opcode1);
                } catch (IOException | OpcodeException e) {
                    System.out.println("C1- EXIT");
                }

                this.log.write("C1- ERROR: " + e1);
                this.log.flush();

                this.gameBool = false;
                break;

            } else if (this.opcode2 == 0x07) {

                String e2 = "";
                try {
                    e2 = this.datagram2.readString(7, this.opcode2);
                } catch (IOException | OpcodeException e) {
                    System.out.println("C2- EXIT");
                }

                this.log.write("C2- ERROR: " + e2);
                this.log.flush();

                this.gameBool = false;
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
