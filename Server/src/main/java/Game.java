import enumType.ShoutType;
import enumType.StateType;
import exception.EmptyHashException;
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
    Datagram datagram1;
    Datagram datagram2;

    private Player player1 = new Player();         //Data Server
    private Player player2 = new Player();         //Data Opponent

    private boolean gameBool = true;
    private StateType state;


    public Game(Socket s1, Socket s2) throws IOException {

        this.dp = new DatabaseProvider();
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

        String insult = "";
        String comeback = "";
        String opponentInsult = "";
        String opponentComeback = "";

        while (gameBool) {

            switch (this.state) {

                case HELLO:              //HELLO message

                    this.player1.setName("Barba Negra");                                                                //Obtain server data

                    //For each game, two new INSULTS and COMEBACKS
                    this.player1.removeInsultsComebacks();
                    this.player1.addInsultComeback(this.dp.getRandomInsultComeback());
                    this.player1.addInsultComeback(this.dp.getRandomInsultComeback());

                    try {
                        this.player2.setName(this.datagram1.read_hello());                                              //Read HELLO message
                        this.player2.setId(this.datagram1.getIdOpponent());                                             //Obtain opponent data
                    } catch (IOException | OpcodeException e) {
                        this.log.write("Hello Error Read " + e.getMessage());
                        this.log.flush();
                        System.exit(1);
                    }

                    try {
                        this.datagram1.write_hello(this.player1.generateRandomID(), this.player1.getName());                       //Write HELLO message
                    } catch (IOException e) {
                        this.log.write("Hello Error Read " + e.getMessage());
                        this.log.flush();
                        System.exit(1);
                    }

                    this.log.write("HELLO: " + this.player2.getId() + " " + this.player2.getName() + "\n");
                    this.log.write("HELLO: " + this.player1.getId() + " " + this.player1.getName() + "\n");

                    this.state = StateType.HASH;                                                                        //Change state to HASH

                    break;

                case HASH:               //HASH message

                    try {
                        this.player2.setHash(this.datagram1.read_hash());                                               //Read HASH message
                    } catch (IOException | OpcodeException | EmptyHashException e) {
                        this.log.write(e.getMessage());
                        this.log.flush();
                        System.exit(1);
                    }

                    try {
                        this.datagram1.write_hash(this.player1.generateSecret());                                       //Write HASH message
                        this.player1.setHash(this.datagram1.getHash(this.player1.getSecret()));                         //Save player1's HASH
                    } catch (IOException e) {
                        this.log.write(e.getMessage());
                        this.log.flush();
                        System.exit(1);
                    }

                    this.log.write("HASH: " + Arrays.toString(this.player2.getHash()) + "\n");
                    this.log.write("HASH: " + Arrays.toString(this.player1.getHash()) + "\n");

                    this.state = StateType.SECRET;                                                                      //Change state to SECRET

                    break;

                case SECRET:            //SECRET message

                    try {
                        this.player2.setSecret(this.datagram1.read_secret());                                           //Read SECRET message
                    } catch (IOException | OpcodeException e) {
                        this.log.write(e.getMessage());
                        this.log.flush();
                        System.exit(1);
                    }

                    try {
                        this.datagram1.write_secret(this.player1.getSecret());                                          //Write SECRET message
                    } catch (IOException e) {
                        this.log.write("ERROR SECRET");
                        this.log.flush();
                        System.exit(1);
                    }

                    if (this.proofHash(this.player2.getSecret(), this.player2.getHash())) {                             //Check correct HASH

                        if (this.player1.getId() != this.player2.getId()) {                                             //Check not same ID

                            if (this.isEven(this.player1.getSecret(), this.player2.getSecret())) {                      //Check EVEN or ODD

                                if (this.player1.getId() < this.player2.getId()) {
                                    this.state = StateType.INSULT;                                                      //Change state to INSULT
                                } else {
                                    this.state = StateType.COMEBACK;                                                    //Change state to COMEBACK
                                }

                            } else {

                                if (this.player1.getId() > this.player2.getId()) {
                                    this.state = StateType.INSULT;                                                      //Change state to INSULT
                                } else {
                                    this.state = StateType.COMEBACK;                                                    //Change state to COMEBACK
                                }

                            }

                            this.log.write("SECRET: " + this.player1.getSecret() + "\n");
                            this.log.write("SECRET: " + this.player2.getSecret() + "\n");

                        } else {                                                     //Same ID, ERROR

                            System.out.println("ERROR ID");

                            /*try {
                                this.datagram1.write_error("ERROR ID");               //CODIGO INCOMPLETO
                            } catch (IOException e) {
                                this.log.write("ERROR");
                                this.log.flush();
                                System.exit(1);
                            }*/

                        }

                    } else {                                                         //Wrong HASH, ERROR

                        System.out.println("ERROR HASH");

                        /*try {
                            this.datagram1.write_error("ERROR HASH");                 //CODIGO INCOMPLETO
                        } catch (IOException e) {
                            this.log.write("ERROR");
                            this.log.flush();
                            System.exit(1);
                        }*/

                    }

                    break;

                case INSULT:            //INSULT message

                    if (this.player1.getDuel() == 3 || this.player2.getDuel() == 3) {                  //Check if there's a winner

                        this.state = StateType.SHOUT;                                                  //Change state to SHOUT

                    } else {

                        if (this.player1.getRound() == 2 || this.player2.getRound() == 2) {            //Check if someone win duel

                            this.state = StateType.SHOUT;                                              //Change state to SHOUT

                        } else {

                            insult = this.player1.getRandomInsult();                                  //Random INSULT

                            try {
                                this.datagram1.write_insult(insult);                                  //Write INSULT message
                            } catch (IOException e) {
                                this.log.write("ERROR");
                                this.log.flush();
                                System.exit(1);
                            }

                            try {
                                opponentComeback = this.datagram1.read_comeback();                    //Read COMEBACK message and get COMEBACK
                            } catch (IOException | OpcodeException e) {
                                this.log.write("ERROR");
                                this.log.flush();
                                System.exit(1);
                            }

                            this.player1.addComeback(opponentComeback);                              //Get opponent comeback as learned
                            this.log.write("INSULT: " + insult + "\n");
                            this.log.write("COMEBACK: " + opponentComeback + "\n");

                            if (this.dp.isRightComeback(insult, opponentComeback)) {                 //Check who win the round

                                this.player2.addRound();
                                this.state = StateType.COMEBACK;

                            } else {

                                this.player1.addRound();

                            }
                        }
                    }

                    break;

                case COMEBACK:

                    if (this.player1.getDuel() == 3 || this.player2.getDuel() == 3) {                  //Check if there's a winner

                        this.state = StateType.SHOUT;                                                  //Change state to SHOUT

                    } else {

                        if (this.player1.getRound() == 2 || this.player2.getRound() == 2) {            //Check if someone win duel

                            this.state = StateType.SHOUT;                                              //Change state to SHOUT

                        } else {

                            try {
                                opponentInsult = this.datagram1.read_insult();                                 //Read COMEBACK message and get COMEBACK
                            } catch (IOException | OpcodeException e) {
                                System.out.println("ERROR");
                                System.exit(1);
                            }

                            this.player1.addInsult(opponentInsult);                                            //Get opponent comeback as learned

                            comeback = this.player1.getRandomComeback();                                       //Get random COMEBACK

                            try {
                                this.datagram1.write_comeback(comeback);                                       //Write COMEBACK message
                            } catch (IOException e) {
                                System.out.println("ERROR");
                            }

                            this.log.write("INSULT: " + opponentInsult + "\n");
                            this.log.write("COMEBACK: " + comeback + "\n");

                            if (this.dp.isRightComeback(opponentInsult, comeback)) {                          //Check who win the round

                                this.player1.addRound();                                                      //Client win round
                                this.state = StateType.INSULT;                                                //Change state to INSULT

                            } else {

                                this.player2.addRound();                                                      //Server win round

                            }
                        }
                    }

                    break;

                case SHOUT:

                    String str = "";
                    String str2 = "";

                    if (this.player1.getRound() == 2) {

                        this.player1.addDuel();

                    } else if (this.player2.getRound() == 2){

                        this.player2.addDuel();

                    }

                    if (this.player1.getDuel() == 3 | this.player1.getRound() == 2) {                                   //Check if Server win something

                        if (this.player1.getDuel() == 3) {

                            try {
                                str = this.datagram1.read_shout();                                                          //Read SHOUT message and get message
                            } catch (IOException | OpcodeException e) {
                                System.out.println("ERROR SHOUT");
                                System.exit(1);
                            }

                            try {
                                str2 = this.dp.getShoutByEnumAddName(ShoutType.I_WIN, this.player2.getName());               //Select SHOUT type message
                                this.datagram1.write_shout(str2);                                                            //Write SHOUT message
                            } catch (IOException e) {
                                System.out.println("ERROR SHOUT");
                                System.exit(1);
                            }


                            this.player1.resetDuelRound();                                                                  //Reset duels
                            this.player2.resetDuelRound();
                            System.out.println("New Game");
                            this.state = StateType.HELLO;

                        } else {

                            try {
                                str = this.datagram1.read_shout();                                                          //Read SHOUT message and get message
                            } catch (IOException | OpcodeException e) {
                                System.out.println("ERROR SHOUT");
                                System.exit(1);
                            }

                        } else {


                            try {
                                str2 = this.dp.getShoutByEnumAddName(ShoutType.I_WIN, this.player2.getName());               //Select SHOUT type message
                                this.datagram1.write_shout(str2);                                                            //Write SHOUT message
                            } catch (IOException e) {
                                System.out.println("ERROR SHOUT");
                                System.exit(1);
                            }

                            this.player1.resetRound();                                                                  //Reset rounds
                            this.player2.resetRound();
                            this.state = StateType.HASH;

                        }

                    } else if (this.player2.getDuel() == 3 | this.player2.getRound() == 2) {

                        if (this.player2.getDuel() == 3) {

                            try {
                                str = this.datagram1.read_shout();                                                          //Read SHOUT message and get message
                            } catch (IOException | OpcodeException e) {
                                System.out.println("ERROR SHOUT");
                                System.exit(1);
                            }

                            try {
                                str2 = this.dp.getShoutByEnumAddName(ShoutType.YOU_WIN_FINAL, this.player2.getName());               //Select SHOUT type message
                                this.datagram1.write_shout(str2);                                                            //Write SHOUT message
                            } catch (IOException e) {
                                System.out.println("ERROR SHOUT");
                                System.exit(1);
                            }

                            this.player1.resetDuelRound();                                                                  //Reset duels
                            this.player2.resetDuelRound();
                            System.out.println("New Game");
                            this.state = StateType.HELLO;

                        } else {

                            try {
                                str = this.datagram1.read_shout();                                                          //Read SHOUT message and get message
                            } catch (IOException | OpcodeException e) {
                                System.out.println("ERROR SHOUT");
                                System.exit(1);
                            }


                            try {
                                str2 = this.dp.getShoutByEnumAddName(ShoutType.YOU_WIN, this.player2.getName());               //Select SHOUT type message
                                this.datagram1.write_shout(str2);                                                            //Write SHOUT message
                            } catch (IOException e) {
                                System.out.println("ERROR SHOUT");
                                System.exit(1);
                            }

                        } else {


                            this.player1.resetRound();                                                                  //Reset rounds
                            this.player2.resetRound();
                            this.state = StateType.HASH;

                        }

                    }

                    this.log.write("SHOUT: " + str + "\n");
                    this.log.write("SHOUT: " + str2 + "\n");

                    break;

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
