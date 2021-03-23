import enumType.ErrorType;
import enumType.ShoutType;
import enumType.StateType;
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
    private Player server = new Player();         //Data Server
    private Player client = new Player();         //Data Opponent
    private Player client2 = new Player();        //Data Opponent2 multiplayer

    private String opponentInsult, opponentComeback;
    private String insult, comeback;
    private boolean gameBool = true;
    private ErrorType errorType;
    private boolean contained;
    private StateType state;
    private int opcode1, opcode2;

    public Game(Socket s1, Socket s2) throws IOException {

        this.database = new Database();
        this.dp = new DatabaseProvider(this.database.getInsults(), this.database.getComebacks());
        this.datagram1 = new Datagram(s1);

        if (s2 != null) {
            this.datagram2 = new Datagram(s2);
        }

        String lg = "Server" + Thread.currentThread().getName() + ".log"; // File name
        (new File("../../../logs")).mkdir(); // Directory
        File f = new File("../../../logs/" + lg); // File
        this.log = new BufferedWriter(new FileWriter(f));

        this.state = StateType.HELLO;

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

        int domain = -1;
        StateType state = StateType.HELLO;

        while (gameBool) {

            if (domain == -1) {
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
            }else{
                if (domain == 0){
                    try {
                        this.opcode1 = this.datagram1.read_opcode();
                    } catch (Exception e) {
                        this.log.write("[Connexion closed]");

                        this.gameBool = false;
                        break;
                    }
                }else{
                    try {
                        this.opcode2 = this.datagram2.read_opcode();
                    } catch (Exception e) {
                        this.log.write("[Connexion closed]");

                        this.gameBool = false;
                        break;
                    }
                }
            }

            if (this.opcode1 == 0x07 || this.opcode2 == 0x07){
                this.opcode1 = 0x07;
            }


            switch (this.opcode1) {

                case 0x01:

                    /* READ HELLO PLAYER1 */
                    try {
                        this.client.setName(this.datagram1.read_hello(this.opcode1));                                              //Read HELLO message
                        this.client.setId(this.datagram1.getIdOpponent());                                             //Obtain opponent data
                    } catch (IOException | OpcodeException e) {
                        this.log.write("C1- HELLO ERROR READ " + e.getMessage() + "\n");
                        this.log.flush();
                        break;
                    }

                    /* READ HELLO PLAYER2 */
                    try {
                        this.client2.setName(this.datagram2.read_hello(this.opcode2));                                              //Read HELLO message
                        this.client2.setId(this.datagram2.getIdOpponent());                                             //Obtain opponent data
                    } catch (IOException | OpcodeException e) {
                        this.log.write("C2- HELLO ERROR READ " + e.getMessage() + "\n");
                        this.log.flush();
                        break;
                    }

                    /* WRITE HELLO PLAYER1 to PLAYER1*/
                    try {
                        this.datagram2.write_hello(this.client.getId(), this.client.getName());                       //Write HELLO message
                    } catch (IOException e) {
                        this.log.write("C1- HELLO ERROR WRITE" + e.getMessage() + "\n");
                        this.log.flush();
                        break;
                    }

                    /* WRITE HELLO PLAYER2 to PLAYER1 */
                    try {
                        this.datagram1.write_hello(this.client2.getId(), this.client2.getName());                       //Write HELLO message
                    } catch (IOException e) {
                        this.log.write("C2- HELLO ERROR WRITE" + e.getMessage() + "\n");
                        this.log.flush();
                        break;
                    }

                    /* LOG OUTPUT */
                    this.log.write("C1- HELLO: " + this.client.getId() + " " + this.client.getName() + "\n");
                    this.log.write("C2- HELLO: " + this.client2.getId() + " " + this.client2.getName() + "\n");

                    break;

                case 0x02:

                    /* READ HASH PLAYER1 */
                    try {
                        this.client.setHash(this.datagram1.read_hash(this.opcode1));
                    } catch (IOException | OpcodeException e) {
                        this.log.write("C1- HASH ERROR READ" + e.getMessage() + "\n");
                        this.log.flush();
                        break;
                    }

                    /* READ HASH PLAYER2 */
                    try {
                        this.client2.setHash(this.datagram2.read_hash(this.opcode2));
                    } catch (IOException | OpcodeException e) {
                        this.log.write("HASH ERROR READ" + e.getMessage() + "\n");
                        this.log.flush();
                        break;
                    }

                    /* WRITE HASH PLAYER1 to PLAYER2*/
                    try {
                        this.datagram2.write_hash2(this.client.getHash());
                    } catch (IOException e) {
                        this.log.write("C1- HASH ERROR WRITE" + e.getMessage() + "\n");
                        this.log.flush();
                        break;
                    }

                    /* WRITE HASH PLAYER2 to PLAYER1*/
                    try {
                        this.datagram1.write_hash2(this.client2.getHash());
                    } catch (IOException e) {
                        this.log.write("C2- HASH ERROR WRITE" + e.getMessage() + "\n");
                        this.log.flush();
                        break;
                    }

                    /* LOG OUTPUT */
                    this.log.write("C1- HASH: " + Arrays.toString(this.client.getHash()) + "\n");
                    this.log.write("C2- HASH: " + Arrays.toString(this.client2.getHash()) + "\n");

                    break;

                case 0x03:

                    /* READ SECRET */
                    try {
                        this.client.setSecret(this.datagram1.read_secret(this.opcode1));
                    } catch (IOException | OpcodeException e) {
                        this.log.write("SECRET ERROR READ" + e.getMessage() + "\n");
                        this.log.flush();
                        break;
                    }

                    /* READ SECRET */
                    try {
                        this.client2.setSecret(this.datagram2.read_secret(this.opcode2));
                    } catch (IOException | OpcodeException e) {
                        this.log.write("SECRET ERROR READ" + e.getMessage() + "\n");
                        this.log.flush();
                        break;
                    }

                    /* WRITE SECRET PLAYER1 to PLAYER2*/
                    try {
                        this.datagram2.write_secret(this.client.getSecret());
                    } catch (IOException e) {
                        this.log.write("C1- SECRET ERROR WRITE" + e.getMessage() + "\n");
                        this.log.flush();
                        break;
                    }

                    /* WRITE SECRET PLAYER2 to PLAYER1*/
                    try {
                        this.datagram1.write_secret(this.client2.getSecret());
                    } catch (IOException e) {
                        this.log.write("C2- SECRET ERROR WRITE" + e.getMessage() + "\n");
                        this.log.flush();
                        break;
                    }

                    /* PROOF HASH - NOT EQUAL ID - EVEN/ODD ^ GREATER/LESSER -> DECIDE STATE */
                    if (this.isEven(this.client2.getSecret(), this.client.getSecret())) {
                        if (this.client2.getId() > this.client.getId()) {
                            domain = 0;
                        } else {
                            domain = 1;
                            this.opcode1 = 0x05;
                        }

                    }else{
                        if (this.client2.getId() < this.client.getId()) {
                            domain = 0;
                        } else {
                            domain = 1;
                            this.opcode1 = 0x05;
                        }
                    }

                    /* LOG OUTPUT */
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

                    try {
                        this.opcode2 = this.datagram2.read_opcode();
                    } catch (Exception e) {
                        this.log.write("[Connexion closed]");

                        this.gameBool = false;
                        break;
                    }

                    try {
                        this.comeback = this.datagram2.read_comeback(this.opcode2);
                    } catch (IOException | OpcodeException e) {
                        this.log.write("C2- INSULT ERROR");
                        this.log.flush();
                        break;
                    }

                    try {
                        this.datagram1.write_comeback(this.comeback);
                    } catch (IOException e) {
                        this.log.write("C2- COMEBACK ERROR");
                        this.log.flush();
                        break;
                    }

                    /* LOG OUTPUT */
                    this.log.write("C1- INSULT: " + this.insult + "\n");
                    this.log.write("C2- COMEBACK: " + this.comeback + "\n");

                    if (this.database.isRightComeback(this.insult, this.comeback)) {
                        this.client2.addRound();
                        domain = 1;

                        if (this.client2.getRound() == 2){
                            domain = -1;
                            this.client2.addDuel();
                            this.client2.resetRound();
                            this.client.resetRound();
                        }
                        if (this.client2.getDuel() == 3){
                            domain = -1;
                            this.client.resetDuel();
                            this.client2.resetDuel();
                        }

                        this.opcode1 = 0x05;

                    }else{
                        this.client.addRound();

                        if (this.client.getRound() == 2){
                            domain = -1;
                            this.client.addDuel();
                            this.client.resetRound();
                            this.client2.resetRound();
                        }
                        if (this.client.getDuel() == 3){
                            domain = -1;
                            this.client.resetDuel();
                            this.client2.resetDuel();
                        }
                    }

                    break;

                case 0x05:

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
                        this.log.write("C2- COMEBACK ERROR");
                        this.log.flush();
                        break;
                    }

                    /*READ COMEBACK*/
                    try {
                        this.opcode1 = this.datagram1.read_opcode();
                    } catch (Exception e) {
                        this.log.write("[Connexion closed]");

                        this.gameBool = false;
                        break;
                    }


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
                        this.log.write("c1- COMEBACK ERROR");
                        this.log.flush();
                        break;
                    }

                    if (this.database.isRightComeback(this.insult, this.comeback)) {
                        this.client.addRound();
                        domain = 0;

                        if (this.client.getRound() == 2){
                            domain = -1;
                            this.client.addDuel();
                            this.client.resetRound();
                            this.client2.resetRound();
                        }
                        if (this.client.getDuel() == 3){
                            domain = -1;
                            this.client.resetDuel();
                            this.client2.resetDuel();
                        }

                        this.opcode1 = 0x04;

                    }else{

                        this.client2.addRound();

                        if (this.client2.getRound() == 2){
                            domain = -1;
                            this.client2.addDuel();
                            this.client2.resetRound();
                            this.client.resetRound();
                        }
                        if (this.client2.getDuel() == 3){
                            domain = -1;
                            this.client.resetDuel();
                            this.client2.resetDuel();
                        }
                    }

                    /* LOG OUTPUT */
                    this.log.write("C1- INSULT: " + this.insult + "\n");
                    this.log.write("C1- COMEBACK: " + this.comeback + "\n");

                    break;

                case 0x06:

                    /* READ SHOUT PLAYER1*/
                    try {
                        clientShout = this.datagram1.read_shout(this.opcode1);
                    } catch (IOException | OpcodeException e) {
                        this.log.write("C1- ERROR SHOUT READ");
                        this.log.flush();
                        break;
                    }

                    /* READ SHOUT PLAYER2*/
                    try {
                        serverShout = this.datagram2.read_shout(this.opcode2);
                    } catch (IOException | OpcodeException e) {
                        this.log.write("C2- ERROR SHOUT READ");
                        this.log.flush();
                        break;
                    }

                    /* WRITE SHOUT PLAYER1 to PLAYER2*/
                    try {
                        this.datagram2.write_shout(clientShout);
                    } catch (IOException e) {
                        this.log.write("C1- ERROR SHOUT WRITE");
                        this.log.flush();
                        break;
                    }

                    /* WRITE SHOUT PLAYER1 to PLAYER2*/
                    try {
                        this.datagram1.write_shout(serverShout);
                    } catch (IOException e) {
                        this.log.write("C2- ERROR SHOUT WRITE");
                        this.log.flush();
                        break;
                    }

                    /* LOG OUTPUT */
                    this.log.write("C1- SHOUT: " + clientShout + "\n");
                    this.log.write("C2- SHOUT: " + serverShout + "\n");

                    break;

                case 0x07:

                    String str = "";

                    /* READ ERROR */
                    if (this.opcode2 == 0x07) {
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

                        /* LOG OUTPUT */
                        this.log.write("C2- ERROR: " + str + "\n");

                    }else{
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

                        /* LOG OUTPUT */
                        this.log.write("C1- ERROR: " + str + "\n");
                    }

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
