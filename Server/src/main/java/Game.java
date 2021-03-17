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

    private Player server = new Player();         //Data Server
    private Player client = new Player();         //Data Opponent

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

                    System.out.println("hello");

                    this.server.setName("AlphaGo");                                                                //Obtain server data
                    int id = this.server.generateRandomID();

                    try {
                        this.client.setName(this.datagram1.read_hello());                                              //Read HELLO message
                        this.client.setId(this.datagram1.getIdOpponent());                                             //Obtain opponent data
                    } catch (IOException | OpcodeException e) {
                        this.log.write("Hello Error Read " + e.getMessage());
                        this.log.flush();
                        System.exit(1);
                    }

                    try {
                        this.datagram1.write_hello(this.server.getId(), this.server.getName());                       //Write HELLO message
                    } catch (IOException e) {
                        this.log.write("Hello Error Read " + e.getMessage());
                        this.log.flush();
                        System.exit(1);
                    }

                    this.log.write("HELLO: " + this.client.getId() + " " + this.client.getName() + "\n");
                    this.log.write("HELLO: " + this.server.getId() + " " + this.server.getName() + "\n");

                    this.state = StateType.HASH;                                                                        //Change state to HASH

                    break;

                case HASH:               //HASH message

                    System.out.println("hash");

                    //For each game, two new INSULTS and COMEBACKS
                    this.server.addInsultComeback(this.dp.getRandomInsultComeback());
                    this.server.addInsultComeback(this.dp.getRandomInsultComeback());

                    try {
                        this.client.setHash(this.datagram1.read_hash());                                               //Read HASH message
                    } catch (IOException | OpcodeException | EmptyHashException e) {
                        this.log.write(e.getMessage());
                        this.log.flush();
                        System.exit(1);
                    }

                    try {
                        this.datagram1.write_hash(this.server.generateSecret());                                       //Write HASH message
                        this.server.setHash(this.datagram1.getHash(this.server.getSecret()));                         //Save player1's HASH
                    } catch (IOException e) {
                        this.log.write(e.getMessage());
                        this.log.flush();
                        System.exit(1);
                    }

                    this.log.write("HASH: " + Arrays.toString(this.client.getHash()) + "\n");
                    this.log.write("HASH: " + Arrays.toString(this.server.getHash()) + "\n");

                    this.state = StateType.SECRET;                                                                      //Change state to SECRET

                    break;

                case SECRET:            //SECRET message

                    System.out.println("secret");

                    try {
                        this.client.setSecret(this.datagram1.read_secret());                                           //Read SECRET message
                    } catch (IOException | OpcodeException e) {
                        this.log.write(e.getMessage());
                        this.log.flush();
                        System.exit(1);
                    }

                    try {
                        this.datagram1.write_secret(this.server.getSecret());                                          //Write SECRET message
                    } catch (IOException e) {
                        this.log.write("ERROR SECRET");
                        this.log.flush();
                        System.exit(1);
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

                            this.log.write("SECRET: " + this.server.getSecret() + "\n");
                            this.log.write("SECRET: " + this.client.getSecret() + "\n");

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

                    System.out.println("insult");

                    if (this.server.getDuel() == 3 || this.client.getDuel() == 3) {                  //Check if there's a winner

                        this.state = StateType.SHOUT;                                                  //Change state to SHOUT

                    } else {

                        if (this.server.getRound() == 2 || this.client.getRound() == 2) {            //Check if someone win duel

                            this.state = StateType.SHOUT;                                              //Change state to SHOUT

                        } else {

                            insult = this.server.getRandomInsult();                                  //Random INSULT

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

                            this.server.addComeback(opponentComeback);                              //Get opponent comeback as learned
                            this.log.write("INSULT: " + insult + "\n");
                            this.log.write("COMEBACK: " + opponentComeback + "\n");

                            if (this.dp.isRightComeback(insult, opponentComeback)) {                 //Check who win the round

                                this.client.addRound();
                                this.state = StateType.COMEBACK;

                            } else {

                                this.server.addRound();

                            }
                        }
                    }

                    break;

                case COMEBACK:

                    System.out.println("comeback");

                    if (this.server.getDuel() == 3 || this.client.getDuel() == 3) {                  //Check if there's a winner

                        this.state = StateType.SHOUT;                                                  //Change state to SHOUT

                    } else {

                        if (this.server.getRound() == 2 || this.client.getRound() == 2) {            //Check if someone win duel

                            this.state = StateType.SHOUT;                                              //Change state to SHOUT

                        } else {

                            try {
                                opponentInsult = this.datagram1.read_insult();                                 //Read COMEBACK message and get COMEBACK
                            } catch (IOException | OpcodeException e) {
                                System.out.println("ERROR");
                                System.exit(1);
                            }

                            this.server.addInsult(opponentInsult);                                            //Get opponent comeback as learned

                            comeback = this.server.getRandomComeback();                                       //Get random COMEBACK

                            try {
                                this.datagram1.write_comeback(comeback);                                       //Write COMEBACK message
                            } catch (IOException e) {
                                System.out.println("ERROR");
                            }

                            this.log.write("INSULT: " + opponentInsult + "\n");
                            this.log.write("COMEBACK: " + comeback + "\n");

                            if (this.dp.isRightComeback(opponentInsult, comeback)) {                          //Check who win the round

                                this.server.addRound();                                                      //Client win round
                                this.state = StateType.INSULT;                                                //Change state to INSULT

                            } else {

                                this.client.addRound();                                                      //Server win round

                            }
                        }
                    }

                    break;

                case SHOUT:

                    System.out.println("shout");

                    if (this.server.getDuel() == 3 | this.server.getRound() == 2) {                                   //Check if Server win something

                        String str = "";
                        String str2 = "";

                        try {
                            str2 = this.datagram1.read_shout();                                                         //Read SHOUT message and get message
                        } catch (IOException | OpcodeException e) {
                            System.out.println("ERROR SHOUT");
                            System.exit(1);
                        }

                        try {
                            str = this.dp.getShoutByEnumAddName(ShoutType.I_WIN, this.client.getName());               //Select SHOUT type message
                            this.datagram1.write_shout(str);                                                            //Write SHOUT message
                        } catch (IOException e) {
                            System.out.println("ERROR SHOUT");
                            System.exit(1);
                        }

                        this.log.write("SHOUT: " + str2 + "\n");
                        this.log.write("SHOUT: " + str + "\n");

                    } else if (this.client.getDuel() == 3 | this.client.getRound() == 2) {                            //Check if opponent win something

                        String str = "";
                        String str2 = "";

                        try {
                            str2 = this.datagram1.read_shout();                                                         //Read SHOUT message and get message
                        } catch (IOException | OpcodeException e) {
                            System.out.println("ERROR");
                            System.exit(1);
                        }

                        if (this.client.getDuel() == 3) {
                            try {
                                str = this.dp.getShoutByEnumAddName(ShoutType.YOU_WIN_FINAL, this.client.getName());       //Select SHOUT type message
                                this.datagram1.write_shout(str);                                                            //Write SHOUT message
                            } catch (IOException e) {
                                System.out.println("ERROR SHOUT");
                                System.exit(1);
                            }
                        } else {

                            try {
                                str = this.dp.getShoutByEnumAddName(ShoutType.YOU_WIN, this.client.getName());               //Select SHOUT type message
                                this.datagram1.write_shout(str);                                                            //Write SHOUT message
                            } catch (IOException e) {
                                System.out.println("ERROR SHOUT");
                                System.exit(1);
                            }

                        }

                        this.log.write("SHOUT: " + str2 + "\n");
                        this.log.write("SHOUT: " + str + "\n");

                    }

                    if (this.server.getRound() == 2 | this.client.getRound() == 2) {                                  //Check round points

                        if (this.server.getRound() == 2) {                                                             //Check client's round point

                            this.server.addDuel();                                                                     //Client win duel

                        } else {

                            this.client.addDuel();                                                                     //Opponent win duel

                        }
                        this.server.resetRound();                                                                  //Reset rounds
                        this.client.resetRound();
                        state = StateType.HASH;                                                                   //Change state to INSULT
                    }

                    if (this.server.getDuel() == 3 | this.client.getDuel() == 3) {                                    //Check duel points

                        this.server.resetDuelRound();                                                                  //Reset duels
                        this.client.resetDuelRound();
                        System.out.println("New Game");
                        this.state = StateType.HASH;                                                                    //Change state to HASH

                    }

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
