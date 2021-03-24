import enumType.ShoutType;
import exception.OpcodeException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class Game {

    private DatabaseProvider dp;
    private BufferedWriter log;
    private Database database;
    Datagram datagram1;
    Datagram datagram2;

    String clientShout, serverShout;
    private Player server = new Player();
    private Player client = new Player();
    private Player client2 = new Player();

    private String opponentInsult, opponentComeback;
    private String insult, comeback;
    private boolean gameBool = true;
    private boolean contained;
    private int opcode1, opcode2;
    private boolean turn, a, b;

    public Game(Socket s1, Socket s2) throws IOException {

        this.database = new Database();
        this.dp = new DatabaseProvider(this.database.getInsults(), this.database.getComebacks());
        this.datagram1 = new Datagram(s1);

        if (s2 != null) {
            this.datagram2 = new Datagram(s2);
            turn = true;
            a = false;
            b = false;
        }

        String lg = "Server" + Thread.currentThread().getName() + ".log"; // File name
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
                this.opcode1 = this.datagram1.read_opcode();
            } catch (Exception e) {
                this.log.write("[Connexion closed]");
                this.gameBool = false;
                break;
            }

            switch (this.opcode1) {

                case 0x01:

                    /* SET DATA */
                    this.server.setName("AlphaGo");
                    this.server.resetInsultsComebacks(); // Renew INSULTS and COMEBACKS

                    /* ADD RANDOM INSULT-COMEBACK */
                    do {
                        contained = this.server.containsWithAddInsultComeback(this.dp.getRandomInsultComeback());
                    } while (contained);

                    /* READ HELLO */
                    try {
                        this.client.setName(this.datagram1.read_hello(this.opcode1));                                              //Read HELLO message
                        this.client.setId(this.datagram1.getIdOpponent());                                             //Obtain opponent data
                    } catch (IOException | OpcodeException e) {
                        this.log.write("S- ERROR: " + e.getMessage());
                        this.log.flush();
                        this.gameBool = false;
                        break;
                    }

                    /* WRITE HELLO */
                    try {
                        this.datagram1.write_hello(this.server.generateId(), this.server.getName());                       //Write HELLO message
                    } catch (IOException e) {
                        this.log.write("S- ERROR: " + e.getMessage());
                        this.log.flush();
                        this.gameBool = false;
                        break;
                    }

                    /* LOG OUTPUT */
                    this.log.write("C- HELLO: " + this.client.getId() + " " + this.client.getName() + "\n");
                    this.log.write("S- HELLO: " + this.server.getId() + " " + this.server.getName() + "\n");

                    break;

                case 0x02:

                    /* ADD RANDOM INSULT-COMEBACK */
                    do {
                        contained = this.server.containsWithAddInsultComeback(this.dp.getRandomInsultComeback());
                    } while (contained);

                    /* READ HASH */
                    try {
                        this.client.setHash(this.datagram1.read_hash(this.opcode1));
                    } catch (IOException | OpcodeException e) {
                        this.log.write("S- ERROR: " + e.getMessage());
                        this.log.flush();
                        this.gameBool = false;
                        break;
                    }

                    /* WRITE HASH */
                    try {
                        this.datagram1.write_hash(this.server.generateSecret());
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

                    break;

                case 0x03:

                    /* READ SECRET */
                    try {
                        this.client.setSecret(this.datagram1.read_secret(this.opcode1));
                    } catch (IOException | OpcodeException e) {
                        this.log.write("S- ERROR: " + e.getMessage());
                        this.log.flush();
                        this.gameBool = false;
                        break;
                    }

                    /* WRITE SECRET */
                    try {
                        this.datagram1.write_secret(this.server.getSecret());
                    } catch (IOException e) {
                        this.log.write("S- ERROR: " + e.getMessage());
                        this.log.flush();
                        this.gameBool = false;
                        break;
                    }

                    /* PROOF HASH - NOT EQUAL ID - EVEN/ODD ^ GREATER/LESSER -> DECIDE STATE */
                    if (this.proofHash(this.client.getSecret(), this.client.getHash())) {
                        if (this.server.getId() != this.client.getId()) {
                            if (this.isEven(this.server.getSecret(), this.client.getSecret()) ^ (this.server.getId() > this.client.getId())) {

                                /* WRITE INSULT */
                                this.insult = this.server.getRandomInsult();
                                try {
                                    this.datagram1.write_insult(this.insult);
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

                    /* LOG OUTPUT */
                    this.log.write("C- SECRET: " + this.server.getSecret() + "\n");
                    this.log.write("S- SECRET: " + this.client.getSecret() + "\n");

                    break;

                case 0x04:

                    /* READ INSULT */
                    try {
                        this.opponentInsult = this.datagram1.read_insult(this.opcode1);
                    } catch (IOException | OpcodeException e) {
                        this.log.write("S- ERROR: " + e.getMessage());
                        this.log.flush();
                        this.gameBool = false;
                        break;
                    }

                    /* ADD COMEBACK AS LEARNED */
                    if (this.database.isInsult(this.opponentInsult)) this.server.addInsult(this.opponentInsult);

                    /* SELECT & WRITE COMEBACK */
                    this.comeback = this.server.getRandomComeback();

                    try {
                        this.datagram1.write_comeback(this.comeback);
                    } catch (IOException e) {
                        this.log.write("S- ERROR: " + e.getMessage());
                        this.log.flush();
                        this.gameBool = false;
                        break;
                    }

                    /* LOG OUTPUT */
                    this.log.write("C- INSULT: " + this.opponentInsult + "\n");
                    this.log.write("S- COMEBACK: " + this.comeback + "\n");

                    /* CHECK INSULT - COMEBACK WINNER */
                    if (this.database.isRightComeback(this.opponentInsult, this.comeback)) {
                        this.server.addRound();
                        if (this.server.getRound() == 2) this.server.addDuel();

                        /* WRITE INSULT */
                        if (this.server.getRound() < 2) {
                            this.insult = this.server.getRandomInsult();
                            try {
                                this.datagram1.write_insult(this.insult);
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

                    break;

                case 0x05:

                    /* READ COMEBACK */
                    try {
                        this.opponentComeback = this.datagram1.read_comeback(this.opcode1);
                    } catch (IOException | OpcodeException e) {
                        this.log.write("S- ERROR: " + e.getMessage());
                        this.log.flush();
                        this.gameBool = false;
                        break;
                    }

                    /* ADD COMEBACK AS LEARNED */
                    if (this.database.isComeback(this.opponentComeback)) this.server.addComeback(this.opponentComeback);

                    /* LOG OUTPUT */
                    this.log.write("S- INSULT: " + this.insult + "\n");
                    this.log.write("C- COMEBACK: " + this.opponentComeback + "\n");

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
                                this.datagram1.write_insult(this.insult);
                            } catch (IOException e) {
                                this.log.write("S- ERROR: " + e.getMessage());
                                this.log.flush();
                                this.gameBool = false;
                                break;
                            }
                        }
                    }

                    break;

                case 0x06:

                    /* READ SHOUT */
                    try {
                        clientShout = this.datagram1.read_shout(this.opcode1);
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
                            this.datagram1.write_shout(serverShout);
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
                                this.datagram1.write_shout(serverShout);                                                            //Write SHOUT message
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
                                this.datagram1.write_shout(serverShout);                                                            //Write SHOUT message
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

                    break;

                case 0x07:

                    String error;

                    /* READ ERROR */
                    try {
                        error = this.datagram1.read_error(this.opcode1);
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
                    this.opcode1 = this.datagram1.read_opcode();
                } catch (Exception e) {
                    this.log.write("C1- [Connexion closed]\n");
                    this.log.flush();
                    try {
                        this.opcode2 = this.datagram2.read_opcode();
                        if (opcode2 == 0x07) {
                            this.log.write("C2- ERROR: " + this.datagram2.read_error(this.opcode2) + "\n");
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
                    this.opcode2 = this.datagram2.read_opcode();
                } catch (Exception e) {
                    this.log.write("C2- [Connexion closed]\n");
                    this.log.flush();
                    try {
                        this.opcode1 = this.datagram1.read_opcode();
                        if (opcode1 == 0x07) {
                            this.log.write("C1- ERROR: " + this.datagram1.read_error(this.opcode1) + "\n");
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
                    this.client.setName(this.datagram1.read_hello(this.opcode1));
                    this.client.setId(this.datagram1.getIdOpponent());
                } catch (IOException | OpcodeException e) {
                    this.log.write("C1- HELLO ERROR READ " + e.getMessage() + "\n");
                    this.log.flush();
                    break;
                }

                /* WRITE HELLO PLAYER1 -> PLAYER2 */
                try {
                    this.datagram2.write_hello(this.client.getId(), this.client.getName());
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
                    this.client2.setName(this.datagram2.read_hello(this.opcode2));
                    this.client2.setId(this.datagram2.getIdOpponent());
                } catch (IOException | OpcodeException e) {
                    this.log.write("C2- HELLO ERROR READ " + e.getMessage() + "\n");
                    this.log.flush();
                    break;
                }

                /* WRITE HELLO PLAYER2 -> PLAYER1 */
                try {
                    this.datagram1.write_hello(this.client2.getId(), this.client2.getName());
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
                    this.client.setHash(this.datagram1.read_hash(this.opcode1));
                } catch (IOException | OpcodeException e) {
                    this.log.write("C1- HASH ERROR READ" + e.getMessage() + "\n");
                    this.log.flush();
                    break;
                }

                /* WRITE HASH PLAYER1 to PLAYER2*/
                try {
                    this.datagram2.write_hash_array(this.client.getHash());
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
                    this.client2.setHash(this.datagram2.read_hash(this.opcode2));
                } catch (IOException | OpcodeException e) {
                    this.log.write("C2- HASH ERROR READ" + e.getMessage() + "\n");
                    this.log.flush();
                    break;
                }

                /* WRITE HASH PLAYER2 to PLAYER1*/
                try {
                    this.datagram1.write_hash_array(this.client2.getHash());
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
                    this.client.setSecret(this.datagram1.read_secret(this.opcode1));
                } catch (IOException | OpcodeException e) {
                    this.log.write("SECRET ERROR READ" + e.getMessage() + "\n");
                    this.log.flush();
                    break;
                }

                /* WRITE SECRET PLAYER1 -> PLAYER2 */
                try {
                    this.datagram2.write_secret(this.client.getSecret());
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
                    this.client2.setSecret(this.datagram2.read_secret(this.opcode2));
                } catch (IOException | OpcodeException e) {
                    this.log.write("SECRET ERROR READ" + e.getMessage() + "\n");
                    this.log.flush();
                    break;
                }

                /* WRITE SECRET PLAYER2 -> PLAYER1 */
                try {
                    this.datagram1.write_secret(this.client2.getSecret());
                } catch (IOException e) {
                    this.log.write("C1- SECRET ERROR WRITE" + e.getMessage() + "\n");
                    this.log.flush();
                    break;
                }

                this.log.write("C2- SECRET: " + this.client2.getSecret() + "\n");
                this.log.flush();

                this.opcode2 = 0x00;

                if (this.isEven(this.client.getSecret(), this.client2.getSecret()) ^ (this.client2.getId() > this.client.getId())) {
                    turn = false;
                } else {
                    turn = true;
                }

            } else if (this.opcode1 == 0x04) {

                /* READ INSULT */
                try {
                    this.insult = this.datagram1.read_insult(this.opcode1);
                } catch (OpcodeException e) {
                    this.log.write("ERROR");
                    this.log.flush();
                }

                /* WRITE INSULT */
                try {
                    this.datagram2.write_insult(this.insult);
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
                    this.insult = this.datagram2.read_insult(this.opcode2);
                } catch (OpcodeException e) {
                    this.log.write("ERROR");
                    this.log.flush();
                }

                /* WRITE INSULT */
                try {
                    this.datagram1.write_insult(this.insult);
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
                    this.comeback = this.datagram1.read_comeback(this.opcode1);
                } catch (IOException | OpcodeException e) {
                    this.log.write("C1- COMEBACK ERROR");
                    this.log.flush();
                    break;
                }

                try {
                    this.datagram2.write_comeback(this.comeback);
                } catch (IOException e) {
                    this.log.write("C2- COMEBACK ERROR");
                    this.log.flush();
                    break;
                }

                /* LOG OUTPUT */
                this.log.write("C1- COMEBACK: " + this.comeback + "\n");
                this.log.flush();
                this.opcode1 = 0x00;
                if (this.database.isRightComeback(this.insult, this.comeback)) {
                    turn = true;
                } else {
                    turn = false;
                }


            } else if (this.opcode2 == 0x05) {

                try {
                    this.comeback = this.datagram2.read_comeback(this.opcode2);
                } catch (IOException | OpcodeException e) {
                    this.log.write("COMEBACK ERROR");
                    this.log.flush();
                    break;
                }

                try {
                    this.datagram1.write_comeback(this.comeback);
                } catch (IOException e) {
                    this.log.write("COMEBACK ERROR");
                    this.log.flush();
                    break;
                }

                /* LOG OUTPUT */
                this.log.write("C2- COMEBACK: " + this.comeback + "\n");
                this.log.flush();
                this.opcode2 = 0x00;
                if (this.database.isRightComeback(this.insult, this.comeback)) {
                    turn = false;
                } else {
                    turn = true;
                }

            } else if (this.opcode1 == 0x06) {

                try {
                    this.clientShout = this.datagram1.read_shout(this.opcode1);
                } catch (IOException | OpcodeException e) {
                    this.log.write("C1- ERROR SHOUT READ");
                    this.log.flush();
                    break;
                }

                try {
                    this.datagram2.write_shout(clientShout);
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
                    this.serverShout = this.datagram2.read_shout(this.opcode2);
                } catch (IOException | OpcodeException e) {
                    this.log.write("C2- ERROR SHOUT READ");
                    this.log.flush();
                    break;
                }

                try {
                    this.datagram1.write_shout(serverShout);
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
                    e1 = this.datagram1.read_error(this.opcode1);
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
                    e2 = this.datagram2.read_error(this.opcode2);
                } catch (IOException | OpcodeException e) {
                    System.out.println("C2- EXIT");
                }

                this.log.write("C2- ERROR: " + e2);
                this.log.flush();

                this.gameBool = false;
                break;
            }

/*
            switch (this.opcode1) {


                case 0x03:


                    *//* PROOF HASH - NOT EQUAL ID - EVEN/ODD ^ GREATER/LESSER -> DECIDE STATE *//*
             *//*if (this.proofHash(this.client.getSecret(), this.client.getHash())) {
                        if (this.server.getId() != this.client.getId()) {
                            if (this.isEven(this.server.getSecret(), this.client.getSecret()) ^ (this.server.getId() > this.client.getId())) {
                                /* WRITE INSULT
                                this.insult = this.server.getRandomInsult();
                                try {
                                    this.datagram1.write_insult(this.insult);
                                } catch (IOException e) {
                                    this.log.write("ERROR");
                                    this.log.flush();
                                }
                            }
                            /* LOG OUTPUT
                            this.log.write("C- SECRET: " + this.server.getSecret() + "\n");
                            this.log.write("S- SECRET: " + this.client.getSecret() + "\n");

                        } else {
                            domain = 1;
                            this.opcode1 = 0x05;
                        }

                    } else {
                        this.log.write("C- ERROR NOT COINCIDENT HASH");
                    }


             *//* LOG OUTPUT *//*
                    this.log.write("C1- SECRET: " + this.client.getSecret() + "\n");
                    this.log.write("C2- SECRET: " + this.client2.getSecret() + "\n");

                    break;

                case 0x04:



                    try {
                        this.insult = this.datagram1.read_insult(this.opcode1);
                    } catch (IOException | OpcodeException e) {
                        this.log.write("C1- INSULT ERROR");
                        this.log.flush();
                        break;
                    }

                    try {

                        this.datagram2.write_insult(this.insult);
                    } catch (IOException e) {
                        this.log.write("C1- INSULT ERROR");
                        this.log.flush();
                        break;
                    }

                    this.log.write("C1- INSULT: " + this.insult + "\n");


                    try {
                        this.opcode2 = this.datagram2.read_opcode();
                    } catch (Exception e) {
                        this.log.write("[Connexion closed]");
                        continue;
                    }
                    if (this.opcode2 == 0x07) {
                        try {
                            this.datagram2.read_error(0x07);
                        } catch (OpcodeException e) {
                            continue;
                        }
                        this.gameBool = false;
                        break;
                    }


                    try {
                        this.insult = this.datagram2.read_insult(this.opcode2);
                    } catch (IOException | OpcodeException e) {
                        this.log.write("C2- INSULT ERROR");

                        this.log.flush();
                        break;
                    }

                    try {
                        this.datagram1.write_insult(this.insult);
                    } catch (IOException e) {
                        this.log.write("C2- INSULT ERROR");
                        this.log.flush();
                        break;
                    }

                    *//* LOG OUTPUT *//*
                    this.log.write("C2- INSULT: " + this.insult + "\n");



                    break;

                case 0x05:


                    if (this.opcode1 == 0x05) {
                        try {
                            this.comeback = this.datagram1.read_comeback(this.opcode1);
                        } catch (IOException | OpcodeException e) {
                            this.log.write("C1- COMEBACK ERROR");
                            this.log.flush();
                            break;
                        }


                    try {
                        this.datagram1.write_insult(this.insult);
                    } catch (IOException e) {
                        this.log.write("C2- COMEBACK ERROR");
                        this.log.flush();
                        break;
                    }


                        this.log.write("C1- COMEBACK: " + this.comeback + "\n");

                    } else {
                        try {
                            this.comeback = this.datagram2.read_comeback(this.opcode2);
                        } catch (IOException | OpcodeException e) {
                            this.log.write("COMEBACK ERROR");
                            this.log.flush();
                            break;
                        }



                        this.log.write("C2- COMEBACK: " + this.comeback + "\n");
                    }

                    break;

                case 0x06:

                    *//* READ SHOUT PLAYER1*//*
                    try {
                        clientShout = this.datagram1.read_shout(this.opcode1);
                    } catch (IOException | OpcodeException e) {
                        this.log.write("C1- ERROR SHOUT READ");
                        this.log.flush();
                        break;
                    }

                    *//* READ SHOUT PLAYER2*//*
                    try {
                        serverShout = this.datagram2.read_shout(this.opcode2);
                    } catch (IOException | OpcodeException e) {
                        this.log.write("C2- ERROR SHOUT READ");
                        this.log.flush();
                        break;
                    }

                    *//* WRITE SHOUT PLAYER1 to PLAYER2*//*
                    try {
                        this.datagram2.write_shout(clientShout);
                    } catch (IOException e) {
                        this.log.write("C1- ERROR SHOUT WRITE");
                        this.log.flush();
                        break;
                    }

                    *//* WRITE SHOUT PLAYER1 to PLAYER2*//*
                    try {
                        this.datagram1.write_shout(serverShout);
                    } catch (IOException e) {
                        this.log.write("C2- ERROR SHOUT WRITE");
                        this.log.flush();
                        break;
                    }


                    this.log.write("C1- SHOUT: " + clientShout + "\n");
                    this.log.write("C2- SHOUT: " + serverShout + "\n");

                    break;

                case 0x07:

                    String str = "";


                    if (this.opcode1 == 0x07) {
                        try {
                            str = this.datagram2.read_error(this.opcode2);
                        } catch (IOException | OpcodeException e) {
                            System.out.println("C2- EXIT");
                        }

                        try {
                            this.datagram1.write_error(str);
                        } catch (IOException e) {
                            System.out.println("C2- EXIT");
                        }


                        this.log.write("C1- ERROR: " + str + "\n");

                    } else {
                        try {
                            str = this.datagram1.read_error(this.opcode1);
                        } catch (IOException | OpcodeException e) {
                            System.out.println("C1- EXIT");
                        }

                        try {
                            this.datagram2.write_error(str);
                        } catch (IOException e) {
                            System.out.println("C1- EXIT");
                        }


                        this.log.write("C2- ERROR: " + str + "\n");

                    }

                    this.gameBool = false;
                    break;
            }*/
        }

    }

/*
    public void multiPlayer() throws IOException {

        while (gameBool) {

            try {
                this.opcode1 = this.datagram1.read_opcode();
            } catch (Exception e) {
                this.log.write("[Connexion closed]");

                this.gameBool = false;
                break;
            }

            try {
                this.opcode2 = this.datagram2.read_opcode();
            } catch (Exception e) {
                this.log.write("[Connexion closed]");

                this.gameBool = false;
                break;
            }

            if (this.opcode1 == 0x07 | this.opcode1 == 0x07) {
                log.write("Error");
            }

            *//* READ HELLO PLAYER1 *//*
            if (this.opcode1 == 0x01 && this.opcode2 == 0x01) {
                try {
                    this.client.setName(this.datagram1.read_hello(this.opcode1));
                    this.client.setId(this.datagram1.getIdOpponent());
                } catch (IOException | OpcodeException e) {
                    this.log.write("C1- HELLO ERROR READ " + e.getMessage() + "\n");
                    this.log.flush();
                    continue;
                }

                *//* READ HELLO PLAYER2 *//*
                try {
                    this.client2.setName(this.datagram2.read_hello(this.opcode2));
                    this.client2.setId(this.datagram2.getIdOpponent());
                } catch (IOException | OpcodeException e) {
                    this.log.write("C2- HELLO ERROR READ " + e.getMessage() + "\n");
                    this.log.flush();
                    continue;
                }

                *//* WRITE HELLO PLAYER2 *//*
                try {
                    this.datagram1.write_hello(this.client2.getId(), this.client2.getName());                       //Write HELLO message
                } catch (IOException e) {
                    this.log.write("C1- HELLO ERROR WRITE" + e.getMessage() + "\n");
                    this.log.flush();
                    continue;
                }

                *//* WRITE HELLO PLAYER1 *//*
                try {
                    this.datagram2.write_hello(this.client.getId(), this.client.getName());                       //Write HELLO message
                } catch (IOException e) {
                    this.log.write("C2- HELLO ERROR WRITE" + e.getMessage() + "\n");
                    this.log.flush();
                    continue;
                }

                *//* LOG OUTPUT *//*
                this.log.write("C1- HELLO: " + this.client.getId() + " " + this.client.getName() + "\n");
                this.log.write("C2- HELLO: " + this.client2.getId() + " " + this.client2.getName() + "\n");
            } else if (this.opcode1 == 0x02) {*//* READ HASH PLAYER1 *//*
                try {
                    this.client.setHash(this.datagram1.read_hash(this.opcode1));
                } catch (IOException | OpcodeException e) {
                    this.log.write("C1- HASH ERROR READ" + e.getMessage() + "\n");
                    this.log.flush();
                    continue;
                }

                *//* READ HASH PLAYER2 *//*
                try {
                    this.client2.setHash(this.datagram2.read_hash(this.opcode2));
                } catch (IOException | OpcodeException e) {
                    this.log.write("HASH ERROR READ" + e.getMessage() + "\n");
                    this.log.flush();
                    continue;
                }

                *//* WRITE HASH PLAYER1 to PLAYER2*//*
                try {
                    this.datagram2.write_hash(this.client.getSecret());
                } catch (IOException e) {
                    this.log.write("C1- HASH ERROR WRITE" + e.getMessage() + "\n");
                    this.log.flush();
                    continue;
                }

                *//* WRITE HASH PLAYER2 to PLAYER1*//*
                try {
                    this.datagram1.write_hash(this.client2.getSecret());
                } catch (IOException e) {
                    this.log.write("C2- HASH ERROR WRITE" + e.getMessage() + "\n");
                    this.log.flush();
                    continue;
                }

                *//* LOG OUTPUT *//*
                this.log.write("C1- HASH: " + Arrays.toString(this.client.getHash()) + "\n");
                this.log.write("C2- HASH: " + Arrays.toString(this.client2.getHash()) + "\n");
            } else if (this.opcode1 == 0x03) {*//* READ SECRET *//*
                try {
                    this.client.setSecret(this.datagram1.read_secret(this.opcode1));
                } catch (IOException | OpcodeException e) {
                    this.log.write("SECRET ERROR READ" + e.getMessage() + "\n");
                    this.log.flush();
                    continue;
                }

                *//* READ SECRET *//*
                try {
                    this.client2.setSecret(this.datagram2.read_secret(this.opcode2));
                } catch (IOException | OpcodeException e) {
                    this.log.write("SECRET ERROR READ" + e.getMessage() + "\n");
                    this.log.flush();
                    continue;
                }

                *//* WRITE SECRET PLAYER1 to PLAYER2*//*
                try {
                    this.datagram2.write_secret(this.client.getSecret());
                } catch (IOException e) {
                    this.log.write("C1- SECRET ERROR WRITE" + e.getMessage() + "\n");
                    this.log.flush();
                    continue;
                }

                *//* WRITE SECRET PLAYER2 to PLAYER1*//*
                try {
                    this.datagram1.write_secret(this.client2.getSecret());
                } catch (IOException e) {
                    this.log.write("C2- SECRET ERROR WRITE" + e.getMessage() + "\n");
                    this.log.flush();
                    continue;
                }

                *//* PROOF HASH - NOT EQUAL ID - EVEN/ODD ^ GREATER/LESSER -> DECIDE STATE *//*
     *//*if (this.proofHash(this.client.getSecret(), this.client.getHash())) {
                        if (this.server.getId() != this.client.getId()) {
                            if (this.isEven(this.server.getSecret(), this.client.getSecret()) ^ (this.server.getId() > this.client.getId())) {

                                /* WRITE INSULT
                                this.insult = this.server.getRandomInsult();
                                try {
                                    this.datagram1.write_insult(this.insult);
                                } catch (IOException e) {
                                    this.log.write("ERROR");
                                    this.log.flush();
                                }
                            }

                            /* LOG OUTPUT
                            this.log.write("C- SECRET: " + this.server.getSecret() + "\n");
                            this.log.write("S- SECRET: " + this.client.getSecret() + "\n");

                        } else {
                            this.log.write("C- ERROR SAME ID");
                        }
                    } else {
                        this.log.write("C- ERROR NOT COINCIDENT HASH");
                    }*//*

     *//* LOG OUTPUT *//*
                this.log.write("C1- SECRET: " + this.client.getSecret() + "\n");
                this.log.write("C2- SECRET: " + this.client2.getSecret() + "\n");
            } else if (this.opcode1 == 0x04) {*//* READ INSULT *//*
                if (this.opcode1 == 0x04) {
                    try {
                        this.insult = this.datagram1.read_insult(this.opcode1);
                    } catch (IOException | OpcodeException e) {
                        this.log.write("C1- INSULT ERROR");
                        this.log.flush();
                        continue;
                    }

                    try {
                        this.datagram2.write_insult(this.insult);
                    } catch (IOException e) {
                        this.log.write("C1- INSULT ERROR");
                        this.log.flush();
                        continue;
                    }

                    *//* LOG OUTPUT *//*
                    this.log.write("C1- INSULT: " + this.insult + "\n");

                } else {
                    try {
                        this.insult = this.datagram2.read_insult(this.opcode2);
                    } catch (IOException | OpcodeException e) {
                        this.log.write("C2- INSULT ERROR");
                        this.log.flush();
                        continue;
                    }

                    try {
                        this.datagram1.write_insult(this.insult);
                    } catch (IOException e) {
                        this.log.write("C2- INSULT ERROR");
                        this.log.flush();
                        continue;
                    }

                    *//* LOG OUTPUT *//*
                    this.log.write("C2- INSULT: " + this.insult + "\n");
                }

                *//*NO HACE FALTA HACER COMPROBACIONES PQ YA LAS HACEN LOS CLIENTES*//*

     *//* CHECK INSULT - COMEBACK WINNER *//*
     *//*if (this.database.isRightComeback(this.opponentInsult, this.comeback)) {
                        this.server.addRound();
                        if (this.server.getRound() == 2) this.server.addDuel();

                        /* WRITE INSULT
                        if (this.server.getRound() < 2) {
                            this.insult = this.server.getRandomInsult();
                            try {
                                this.datagram1.write_insult(this.insult);
                            } catch (IOException e) {
                                this.log.write("ERROR");
                                this.log.flush();
                            }
                        }
                    } else {
                        this.client.addRound();
                        if (this.client.getRound() == 2) this.client.addDuel();
                    }*//*
            } else if (this.opcode1 == 0x05) {*//* READ COMEBACK *//*
                if (this.opcode1 == 0x05) {
                    try {
                        this.comeback = this.datagram1.read_comeback(this.opcode1);
                    } catch (IOException | OpcodeException e) {
                        this.log.write("C1- COMEBACK ERROR");
                        this.log.flush();
                        continue;
                    }

                    try {
                        this.datagram2.write_comeback(this.comeback);
                    } catch (IOException e) {
                        this.log.write("c1- COMEBACK ERROR");
                        this.log.flush();
                        continue;
                    }

                    *//* LOG OUTPUT *//*
                    this.log.write("C1- COMEBACK: " + this.comeback + "\n");

                } else {
                    try {
                        this.comeback = this.datagram2.read_comeback(this.opcode2);
                    } catch (IOException | OpcodeException e) {
                        this.log.write("COMEBACK ERROR");
                        this.log.flush();
                        continue;
                    }

                    try {
                        this.datagram1.write_comeback(this.comeback);
                    } catch (IOException e) {
                        this.log.write("COMEBACK ERROR");
                        this.log.flush();
                        continue;
                    }

                    *//* LOG OUTPUT *//*
                    this.log.write("C2- COMEBACK: " + this.comeback + "\n");
                }

                *//*NO HACE FALTA HACER COMPROBACIONES PQ YA LAS HACEN LOS CLIENTES*//*

     *//* CHECK INSULT - COMEBACK WINNER *//*
     *//*if (this.database.isRightComeback(this.insult, this.opponentComeback)) {
                        this.client.addRound();
                        if (this.client.getRound() == 2) this.client.addDuel();

                    } else {
                        this.server.addRound();
                        if (this.server.getRound() == 2) this.server.addDuel();

                        /* WRITE INSULT
                        if (this.server.getRound() < 2) {
                            this.insult = this.server.getRandomInsult();
                            try {
                                this.datagram1.write_insult(this.insult);
                            } catch (IOException e) {
                                this.log.write("ERROR");
                                this.log.flush();
                            }
                        }
                    }*//*
            } else if (this.opcode1 == 0x06) {*//* READ SHOUT PLAYER1*//*
                try {
                    clientShout = this.datagram1.read_shout(this.opcode1);
                } catch (IOException | OpcodeException e) {
                    this.log.write("C1- ERROR SHOUT READ");
                    this.log.flush();
                    continue;
                }

                *//* READ SHOUT PLAYER2*//*
                try {
                    serverShout = this.datagram2.read_shout(this.opcode2);
                } catch (IOException | OpcodeException e) {
                    this.log.write("C2- ERROR SHOUT READ");
                    this.log.flush();
                    continue;
                }

                *//* WRITE SHOUT PLAYER1 to PLAYER2*//*
                try {
                    this.datagram2.write_shout(clientShout);
                } catch (IOException e) {
                    this.log.write("C1- ERROR SHOUT WRITE");
                    this.log.flush();
                    continue;
                }

                *//* WRITE SHOUT PLAYER1 to PLAYER2*//*
                try {
                    this.datagram1.write_shout(serverShout);
                } catch (IOException e) {
                    this.log.write("C2- ERROR SHOUT WRITE");
                    this.log.flush();
                    continue;
                }

                *//*NO HACE FALTA HACER COMPROBACIONES PQ YA LAS HACEN LOS CLIENTES*//*

     *//* SERVER - WIN GAME - WIN DUEL *//*
     *//*if (this.server.getDuel() == 3 | this.server.getRound() == 2) {

                        /* WRITE SHOUT
                        try {
                            serverShout = this.database.getShoutByEnumAddName(ShoutType.I_WIN, this.client.getName());
                            this.datagram1.write_shout(serverShout);
                        } catch (IOException e) {
                            this.log.write("ERROR SHOUT WRITE");
                        }

                        /* WIN GAME
                        if (this.server.getDuel() == 3) {
                            this.client.resetDuel();
                            this.server.resetDuel();
                        }

                        /* WIN GAME OR DUEL
                        this.server.resetRound();
                        this.client.resetRound();
                    }

                    /* CLIENT - WIN GAME - WIN DUEL
                    if (this.client.getDuel() == 3 | this.client.getRound() == 2) {

                        /* WIN GAME
                        if (this.client.getDuel() == 3) {
                            /* WRITE SHOUT
                            try {
                                serverShout = this.database.getShoutByEnumAddName(ShoutType.YOU_WIN_FINAL, this.client.getName());               //Select SHOUT type message
                                this.datagram1.write_shout(serverShout);                                                            //Write SHOUT message
                            } catch (IOException e) {
                                this.log.write("ERROR SHOUT");
                            }
                            this.server.resetDuel();
                            this.client.resetDuel();

                            /* WIN DUEL
                        } else {
                            /* WRITE SHOUT
                            try {
                                serverShout = this.database.getShoutByEnumAddName(ShoutType.YOU_WIN, this.client.getName());               //Select SHOUT type message
                                this.datagram1.write_shout(serverShout);                                                            //Write SHOUT message
                            } catch (IOException e) {
                                this.log.write("ERROR SHOUT");
                            }
                        }

                        /* WIN GAME OR DUEL
                        this.server.resetRound();
                        this.client.resetRound();
                    }*//*

     *//* LOG OUTPUT *//*
                this.log.write("C1- SHOUT: " + clientShout + "\n");
                this.log.write("C2- SHOUT: " + serverShout + "\n");
            } else if (this.opcode1 == 0x07) {
                String str = "";

                *//* READ ERROR *//*
                if (this.opcode1 == 0x07) {
                    try {
                        str = this.datagram1.read_error(this.opcode1);
                    } catch (IOException | OpcodeException e) {
                        System.out.println("C1- EXIT");
                    }

                    try {
                        this.datagram2.write_error(str);
                    } catch (IOException e) {
                        System.out.println("C1- EXIT");
                    }

                    *//* LOG OUTPUT *//*
                    this.log.write("C1- ERROR: " + str + "\n");

                } else {
                    try {
                        str = this.datagram2.read_error(this.opcode2);
                    } catch (IOException | OpcodeException e) {
                        System.out.println("C2- EXIT");
                    }

                    try {
                        this.datagram1.write_error(str);
                    } catch (IOException e) {
                        System.out.println("C2- EXIT");
                    }

                    *//* LOG OUTPUT *//*
                    this.log.write("C2- ERROR: " + str + "\n");
                }

                this.gameBool = false;
            }
        }
    }*/

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
