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

    private String opponentInsult, opponentComeback;
    private boolean gameBool = true;
    private String insult, comeback;
    private ErrorType errorType;
    private boolean contained;
    private StateType state;


    public Game(Socket s1, Socket s2) throws IOException {

        this.database = new Database();
        this.dp = new DatabaseProvider(this.database.getInsults(), this.database.getComebacks());
        this.datagram1 = new Datagram(s1);

        if (s2 != null) {
            this.datagram2 = new Datagram(s2);
        }

        String lg = "Server" + Thread.currentThread().getName() + ".log"; // File name
        (new File("../../logs")).mkdir(); // Directory
        File f = new File("../../logs/" + lg); // File
        this.log = new BufferedWriter(new FileWriter(f));

        this.state = StateType.HELLO;

    }

    public void run() throws IOException {
        this.singlePlayer();
        this.log.close();
    }

    public void singlePlayer() throws IOException {

        while (gameBool) {

            switch (this.state) {

                case HELLO:              //HELLO message

                    this.server.setName("AlphaGo");                                                                //Obtain server data

                    //For each game, renew INSULTS and COMEBACKS
                    this.server.resetInsultsComebacks();

                    /* ADD RANDOM INSULT-COMEBACK */
                    do { // Check if already contains and always add pair of insults/comebacks
                        contained = this.server.containsWithAddInsultComeback(this.dp.getRandomInsultComeback());
                    } while (contained); // If already contains, get new pairs

                    try {
                        this.client.setName(this.datagram1.read_hello());                                              //Read HELLO message
                        this.client.setId(this.datagram1.getIdOpponent());                                             //Obtain opponent data
                    } catch (IOException | OpcodeException e) {
                        this.log.write("Hello Error Read " + e.getMessage());
                        this.errorType = ErrorType.WRONG_OPCODE;
                        this.state = StateType.ERROR;
                        this.log.flush();
                    }

                    try {
                        this.datagram1.write_hello(this.server.generateRandomID(), this.server.getName());                       //Write HELLO message
                    } catch (IOException e) {
                        this.log.write("Hello Error Read " + e.getMessage());
                        this.log.flush();
                    }

                    this.log.write("C- HELLO: " + this.client.getId() + " " + this.client.getName() + "\n");
                    this.log.write("S- HELLO: " + this.server.getId() + " " + this.server.getName() + "\n");

                    this.state = StateType.HASH;                                                                        //Change state to HASH

                    break;

                case HASH:               //HASH message

                    do { // Check if already contains and always add pair of insults/comebacks
                        contained = this.server.containsWithAddInsultComeback(this.dp.getRandomInsultComeback());
                    } while (contained); // If already contains, get new pairs

                    try {
                        this.client.setHash(this.datagram1.read_hash());                                               //Read HASH message
                    } catch (IOException | OpcodeException e) {
                        this.log.write(e.getMessage());
                        this.log.flush();
                    }

                    try {
                        this.datagram1.write_hash(this.server.generateSecret());                                       //Write HASH message
                        this.server.setHash(this.getHash(this.server.getSecret()));                                    //Save server's HASH
                    } catch (IOException e) {
                        this.log.write(e.getMessage());
                        this.log.flush();
                    }

                    this.log.write("C- HASH: " + Arrays.toString(this.client.getHash()) + "\n");
                    this.log.write("S- HASH: " + Arrays.toString(this.server.getHash()) + "\n");

                    this.state = StateType.SECRET;                                                                      //Change state to SECRET

                    break;

                case SECRET:            //SECRET message

                    try {
                        this.client.setSecret(this.datagram1.read_secret());                                           //Read SECRET message
                    } catch (IOException | OpcodeException e) {
                        this.log.write(e.getMessage());
                        this.log.flush();
                    }

                    try {
                        this.datagram1.write_secret(this.server.getSecret());                                          //Write SECRET message
                    } catch (IOException e) {
                        this.log.write("ERROR SECRET");
                        this.log.flush();
                    }

                    if (this.proofHash(this.client.getSecret(), this.client.getHash())) {                             //Check correct HASH

                        if (this.server.getId() != this.client.getId()) {                                             //Check not same ID

                            if (this.isEven(this.server.getSecret(), this.client.getSecret())) {                      //Check EVEN or ODD

                                if (this.server.getId() < this.client.getId()) {
                                    this.state = StateType.INSULT;                                                      //Change state to INSULT
                                } else {
                                    this.state = StateType.COMEBACK;                                                    //Change state to COMEBACK
                                }

                            } else {

                                if (this.server.getId() > this.client.getId()) {
                                    this.state = StateType.INSULT;                                                      //Change state to INSULT
                                } else {
                                    this.state = StateType.COMEBACK;                                                    //Change state to COMEBACK
                                }

                            }

                            this.log.write("C- SECRET: " + this.server.getSecret() + "\n");
                            this.log.write("S- SECRET: " + this.client.getSecret() + "\n");

                        } else {                                                     //Same ID, ERROR

                            this.log.write("C- ERROR SAME ID");
                            this.errorType = ErrorType.INCOMPLETE_MESSAGE;
                            this.state = StateType.ERROR;

                        }

                    } else {                                                         //Wrong HASH, ERROR

                        this.log.write("C- ERROR NOT COINCIDENT HASH");
                        this.errorType = ErrorType.INCOMPLETE_MESSAGE;
                        this.state = StateType.ERROR;

                    }

                    break;

                case INSULT:            //INSULT message

                    if (this.server.getDuel() == 3 || this.client.getDuel() == 3) {                  //Check if there's a winner

                        this.state = StateType.SHOUT;                                                  //Change state to SHOUT

                    } else {

                        if (this.server.getRound() == 2 || this.client.getRound() == 2) {            //Check if someone win duel

                            this.state = StateType.SHOUT;                                              //Change state to SHOUT

                        } else {

                            this.insult = this.server.getRandomInsult();                                  //Random INSULT

                            try {
                                this.datagram1.write_insult(this.insult);                                  //Write INSULT message
                            } catch (IOException e) {
                                this.log.write("ERROR");
                                this.log.flush();
                            }

                            try {
                                this.opponentComeback = this.datagram1.read_comeback();                    //Read COMEBACK message and get COMEBACK
                            } catch (IOException | OpcodeException e) {
                                this.errorType = ErrorType.WRONG_OPCODE;
                                this.state = StateType.ERROR;
                                this.log.write("ERROR");
                            }

                            /* ADD COMEBACK AS LEARNED */
                            if (this.database.isComeback(this.opponentComeback)) {
                                this.server.addComeback(this.opponentComeback);
                            } else {
                                this.errorType = ErrorType.INCOMPLETE_MESSAGE;
                                this.state = StateType.ERROR;
                            }                              //Get opponent comeback as learned

                            this.log.write("S- INSULT: " + this.insult + "\n");
                            this.log.write("C- COMEBACK: " + this.opponentComeback + "\n");

                            if (this.database.isRightComeback(this.insult, this.opponentComeback)) {                 //Check who win the round

                                this.client.addRound();
                                this.state = StateType.COMEBACK;

                            } else {

                                this.server.addRound();

                            }
                        }
                    }

                    break;

                case COMEBACK:

                    if (this.server.getDuel() == 3 || this.client.getDuel() == 3) {                  //Check if there's a winner

                        this.state = StateType.SHOUT;                                                  //Change state to SHOUT

                    } else {

                        if (this.server.getRound() == 2 || this.client.getRound() == 2) {            //Check if someone win duel

                            this.state = StateType.SHOUT;                                              //Change state to SHOUT

                        } else {

                            try {
                                this.opponentInsult = this.datagram1.read_insult();                                 //Read COMEBACK message and get COMEBACK
                            } catch (IOException | OpcodeException e) {
                                this.log.write("ERROR");
                            }

                            /* ADD COMEBACK AS LEARNED */
                            if (this.database.isInsult(this.opponentInsult)) {
                                this.server.addInsult(this.opponentInsult);
                            } else {
                                this.errorType = ErrorType.INCOMPLETE_MESSAGE;
                                this.state = StateType.ERROR;
                            }                                         //Get opponent comeback as learned

                            this.comeback = this.server.getRandomComeback();                                       //Get random COMEBACK

                            try {
                                this.datagram1.write_comeback(this.comeback);                                       //Write COMEBACK message
                            } catch (IOException e) {
                                this.log.write("ERROR");
                            }

                            this.log.write("C- INSULT: " + this.opponentInsult + "\n");
                            this.log.write("S- COMEBACK: " + this.comeback + "\n");

                            if (this.database.isRightComeback(this.opponentInsult, this.comeback)) {                          //Check who win the round

                                this.server.addRound();                                                      //Client win round
                                this.state = StateType.INSULT;                                                //Change state to INSULT

                            } else {

                                this.client.addRound();                                                      //Server win round

                            }
                        }
                    }

                    break;

                case SHOUT:

                    if (this.server.getRound() == 2) {

                        this.server.addDuel();

                    } else if (this.client.getRound() == 2){

                        this.client.addDuel();

                    }

                    try {
                        clientShout = this.datagram1.read_shout();                                                          //Read SHOUT message and get message
                    } catch (IOException | OpcodeException e) {
                        this.log.write("ERROR SHOUT");
                    }

                    if (this.server.getDuel() == 3 | this.server.getRound() == 2) {                                   //Check if Server win something

                        if (this.server.getDuel() == 3) {

                            try {
                                serverShout = this.database.getShoutByEnumAddName(ShoutType.I_WIN, this.client.getName());               //Select SHOUT type message
                                this.datagram1.write_shout(serverShout);                                                            //Write SHOUT message
                            } catch (IOException e) {
                                this.log.write("ERROR SHOUT");
                            }


                            this.server.resetDuelRound();                                                                  //Reset duels
                            this.client.resetDuelRound();
                            System.out.println("New Game");
                            this.state = StateType.HELLO;

                        } else {

                            try {
                                serverShout = this.database.getShoutByEnumAddName(ShoutType.I_WIN, this.client.getName());               //Select SHOUT type message
                                this.datagram1.write_shout(serverShout);                                                            //Write SHOUT message
                            } catch (IOException e) {
                                this.log.write("ERROR SHOUT");
                            }

                            this.server.resetRound();                                                                  //Reset rounds
                            this.client.resetRound();
                            this.state = StateType.HASH;

                        }

                    } else if (this.client.getDuel() == 3 | this.client.getRound() == 2) {

                        if (this.client.getDuel() == 3) {

                            try {
                                serverShout = this.database.getShoutByEnumAddName(ShoutType.YOU_WIN_FINAL, this.client.getName());               //Select SHOUT type message
                                this.datagram1.write_shout(serverShout);                                                            //Write SHOUT message
                            } catch (IOException e) {
                                this.log.write("ERROR SHOUT");
                            }

                            this.server.resetDuelRound();                                                                  //Reset duels
                            this.client.resetDuelRound();
                            System.out.println("New Game");
                            this.state = StateType.HELLO;

                        } else {

                            try {
                                serverShout = this.database.getShoutByEnumAddName(ShoutType.YOU_WIN, this.client.getName());               //Select SHOUT type message
                                this.datagram1.write_shout(serverShout);                                                            //Write SHOUT message
                            } catch (IOException e) {
                                this.log.write("ERROR SHOUT");
                            }


                            this.server.resetRound();                                                                  //Reset rounds
                            this.client.resetRound();
                            this.state = StateType.HASH;

                        }

                    }

                    this.log.write("SHOUT: " + clientShout + "\n");
                    this.log.write("SHOUT: " + serverShout + "\n");

                    break;

                case ERROR:

                    String errorMessage = this.database.getErrorByEnum(this.errorType);

                    try {
                        this.datagram1.write_error(errorMessage);
                    } catch (IOException e) {
                        System.out.println("S- EXIT");
                    }

                    this.gameBool = false;

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
        }
        else{
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
