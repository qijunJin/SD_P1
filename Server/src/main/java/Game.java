import enumType.ErrorType;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class Game {

    Datagram datagram1;
    Datagram datagram2;
    Random rand = new Random();
    private BufferedWriter log;
    private Database database = new Database();
    private boolean gameBool = true;
    private int state = 0;


    public Game(Socket s1, Socket s2) throws IOException {
        this.datagram1 = new Datagram(s1);
        if (s2 != null) {
            this.datagram2 = new Datagram(s2);
        }

        String lg = "Server" + Thread.currentThread().getName() + ".log"; // File name
        (new File("../../logs")).mkdir(); // Directory
        File f = new File("../../logs/" + lg); // File
        this.log = new BufferedWriter(new FileWriter(f));
    }

    public void run() throws IOException {
        this.singlePlayer();
        this.log.close();
    }

    public void singlePlayer() throws IOException {

        ArrayList<String> insultsLearned = new ArrayList<>();      //Aqui guardamos los insultos que aprendemos
        ArrayList<String> comebacksLearned = new ArrayList<>();    //Aqui guardamos los comebacks que aprendemos

        String name = "";
        String opponentName = "";
        int id = 0;
        int opponentId = 0;

        String secret = "";
        String opponentSecret = "";
        byte[] opponentHash = new byte[0];

        boolean domain = true;   // True -> Servidor, False -> Client

        int duel = 0;
        int round = 0;
        int duelWins = 0;
        int roundWins = 0;
        int opponentDuelWins = 0;
        int opponentRoundWins = 0;

        String insult = "";
        String comeback = "";
        String opponentInsult = "";
        String opponentComeback = "";

        while (gameBool) {

            switch (state) {
                case 0:              //Recopilación de datos del jugador y mensaje HELLO

                    ArrayList<Integer> indexes = this.getRandomIndexes();
                    insultsLearned.addAll(this.database.getInsultsByIndexes(indexes));
                    comebacksLearned.addAll(this.database.getComebacksByIndexes(indexes));

                    name = "Barba Negra";
                    id = rand.nextInt((int) Math.pow((double) 2, 31));  //Id aleatorio
                    this.log.write("Servidor" + "\n");
                    System.out.println("Servidor");
                    try {
                        opponentName = this.datagram1.read_hello();
                    } catch (DatagramException | IOException e) {
                        this.log.write(e.getMessage() + "\n");
                        this.log.flush();
                        System.exit(1);
                    }

                    opponentId = this.datagram1.getIdOpponent();


                    try {

                        // id = this.datagram2.getIdOpponent();
                        // name = this.datagram2.read_hello();
                        this.datagram1.write_hello(id, name);

                    } catch (IOException e) {
                        this.log.write(e.getMessage() + "\n");
                        this.log.flush();
                        System.exit(1);
                    }

                    this.log.write("HELLO: " + "id: " + opponentId + "name: " + opponentName + "\n");
                    this.log.write("HELLO: " + "id: " + id + "name: " + name + "\n");
                    this.state = 1;

                    break;
                case 1:          //Envio de HASH entre jugadores

                    /*Añadir insultos y combeacks aleatorios despues de cada partida*/

                    try {
                        opponentHash = this.datagram1.read_hash();
                    } catch (IOException | DatagramException e) {
                        this.log.write(e.getMessage() + "\n");
                        this.log.flush();
                        System.exit(1);
                    }

                    secret = Integer.toString(rand.nextInt(Integer.MAX_VALUE));

                    try {
                        this.datagram1.write_hash(secret);
                    } catch (IOException e) {
                        this.log.write(e.getMessage() + "\n");
                        this.log.flush();
                        System.exit(1);
                    }

                    this.log.write("HASH: " + Arrays.toString(opponentHash) + "\n");
                    this.log.write("HASH: " + Arrays.toString(getHash(secret)) + "\n");
                    this.state = 2;

                    break;

                case 2:             //Envio de SECRET entre jugadores

                    try {
                        opponentSecret = this.datagram1.read_secret();
                    } catch (IOException | DatagramException e) {
                        System.out.println(e.getMessage());
                        this.log.write(e.getMessage() + "\n");
                        this.log.flush();
                        System.exit(1);
                    }

                    try {
                        this.datagram1.write_secret(secret);
                    } catch (IOException e) {
                        this.log.write(e.getMessage() + "\n");
                        this.log.flush();
                        System.exit(1);
                    }

                    this.log.write("SECRET ME: " + opponentSecret + "\n");
                    this.log.write("SECRET OPPO: " + secret + "\n");
                    this.state = 3;

                    break;
                case 3:          //Comprobación de HASH correcto y elección de quien comienza el juego.
                    this.log.write("3 \n");
                    this.log.flush();
                    if (this.proofHash(opponentSecret, opponentHash)) {
                        if (id != opponentId) {
                            if (datagram1.isEven(secret, opponentSecret)) {
                                domain = id < opponentId; // True -> Servidor, False -> Client
                            } else {
                                domain = id > opponentId; // 1001<12
                            }
                            this.state = 4;
                        } else {
                            try {
                                this.datagram1.write_error("ERROR ID");
                            } catch (IOException e) {
                                this.log.write(e.getMessage() + "\n");
                                this.log.flush();
                                System.exit(1);
                            }
                            this.log.write(this.database.getErrorByEnum(ErrorType.WRONG_OPCODE));  //CAMBIAR
                        }
                    } else {
                        try {
                            this.datagram1.write_error("ERROR HASH");
                        } catch (IOException e) {
                            this.log.write(e.getMessage() + "\n");
                            this.log.flush();
                            System.exit(1);
                        }
                        this.log.write(this.database.getErrorByEnum(ErrorType.WRONG_OPCODE));  //CAMBIAR
                    }

                    break;
                case 4:                    //Comprobación de los duelos
                    this.log.write("4 \n");
                    this.log.flush();

                    if (duel < 3) {
                        this.state = 5;                   //Seguimos jugando

                    } else {

                        if (duelWins == 3 || opponentDuelWins == 3) {        //Se acaba la parida pq hay un ganador
                            if (duelWins == 3) {        //Ganamos la partida

                                try {
                                    System.out.println(this.datagram1.read_shout());
                                } catch (IOException e) {
                                    this.log.write(e.getMessage() + "\n");
                                    this.log.flush();
                                    System.exit(1);
                                }

                                try {
                                    this.datagram1.write_shout("¡He ganado, " + opponentName + " !");
                                } catch (IOException e) {
                                    this.log.write(e.getMessage() + "\n");
                                    this.log.flush();
                                    System.exit(1);
                                }

                                this.log.write("SHOUT: ¡Has ganado, " + name + " !");
                                this.log.write("SHOUT: ¡He ganado, " + opponentName + " !");

                            } else {            //Perdemos la partida

                                try {
                                    System.out.println(this.datagram1.read_shout());
                                } catch (IOException e) {
                                    this.log.write(e.getMessage() + "\n");
                                    this.log.flush();
                                    System.exit(1);
                                }

                                try {
                                    this.datagram1.write_shout("¡Has ganado, " + opponentName + " !");
                                } catch (IOException e) {
                                    this.log.write(e.getMessage() + "\n");
                                    this.log.flush();
                                    System.exit(1);
                                }

                                this.log.write("SHOUT: ¡He ganado, " + name + " !");
                                this.log.write("SHOUT: ¡Has ganado, " + opponentName + " !");

                            }

                            //Reseteamos marcadores pq se acaba la partida
                            duel = 0;
                            round = 0;
                            duelWins = 0;
                            roundWins = 0;
                            opponentDuelWins = 0;
                            opponentRoundWins = 0;
                            state = 1;             //Al acabar una partida volvemos a enviar Hash

                        } else {
                            this.state = 5;                   //Seguimos jugando
                        }
                    }

                    break;
                case 5:             //Comporbacion de las rondas

                    this.log.write("5 \n");
                    this.log.flush();
                    if (round < 2) {
                        this.state = 6;                 //Seguimos jugando

                    } else {

                        if (roundWins == 2 || opponentRoundWins == 2) {     //Se acaba el duelo pq alguien ha ganado
                            if (roundWins == 2) {         //Ganamos el duelo

                                try {
                                    System.out.println(this.datagram1.read_shout());
                                } catch (IOException e) {
                                    this.log.write(e.getMessage() + "\n");
                                    this.log.flush();
                                    System.exit(1);
                                }

                                try {
                                    this.datagram1.write_shout("¡He ganado, " + opponentName + " !");
                                } catch (IOException e) {
                                    this.log.write(e.getMessage() + "\n");
                                    this.log.flush();
                                    System.exit(1);
                                }

                                this.log.write("SHOUT: ¡Has ganado, " + name + " !");
                                this.log.write("SHOUT: ¡He ganado, " + opponentName + " !");
                                duelWins++;

                            } else {          //Perdemos el duelo

                                try {
                                    System.out.println(this.datagram1.read_shout());
                                } catch (IOException e) {
                                    this.log.write(e.getMessage() + "\n");
                                    this.log.flush();
                                    System.exit(1);
                                }

                                try {
                                    this.datagram1.write_shout("¡Has ganado, " + opponentName + " !");
                                } catch (IOException e) {
                                    this.log.write(e.getMessage() + "\n");
                                    this.log.flush();
                                    System.exit(1);
                                }

                                this.log.write("SHOUT: ¡He ganado, " + name + " !");
                                this.log.write("SHOUT: ¡Has ganado, " + opponentName + " !");
                                opponentDuelWins++;

                            }

                            //resetamos rondas pq pasamos a otro duelo
                            round = 0;
                            roundWins = 0;
                            opponentRoundWins = 0;
                            duel++;
                            this.state = 4;

                        } else {
                            this.state = 6;                 //Seguimos jugando
                        }

                    }

                    break;
                case 6:        //Envio de INSULTS y COMEBACKS
                    this.log.write("6 \n");
                    this.log.flush();
                    if (domain) {  //Empieza el servidor insultando

                        int n = rand.nextInt(insultsLearned.size());
                        insult = insultsLearned.get(n);
                        this.log.write(insult);
                        this.log.write("domain true \n");
                        this.log.flush();
                        try {
                            this.datagram1.write_insult(insult);
                        } catch (IOException e) {
                            this.log.write(e.getMessage() + "\n");
                            this.log.flush();
                            System.exit(1);
                        }

                        try {
                            opponentComeback = this.datagram1.read_comeback();
                        } catch (IOException e) {
                            this.log.write(e.getMessage() + "\n");
                            this.log.flush();
                            System.exit(1);
                        }

                        comebacksLearned.add(opponentComeback);
                        this.log.write("INSULT: " + insult);
                        this.log.write("COMEBACK: " + opponentComeback);

                        if (this.database.isRightComeback(insult, opponentComeback)) {  //Comporbamos quien gana la ronda
                            opponentRoundWins++;
                            round++;
                            domain = false;
                        } else {
                            roundWins++;
                            round++;
                        }

                    } else {                   //Empieza el cliente insultando
                        this.log.write("domain false \n");
                        this.log.flush();
                        try {
                            opponentInsult = this.datagram1.read_insult();
                        } catch (IOException | DatagramException e) {
                            this.log.write(e.getMessage());
                            this.log.flush();
                            System.exit(1);
                        }

                        insultsLearned.add(opponentInsult);
                        int n = rand.nextInt(insultsLearned.size());
                        comeback = comebacksLearned.get(n);

                        try {
                            this.datagram1.write_comeback(comeback);
                        } catch (IOException e) {
                            this.log.write(e.getMessage() + "\n");
                            this.log.flush();
                            System.exit(1);
                        }

                        this.log.write("INSULT: " + opponentInsult);
                        this.log.write("COMEBACK: " + comeback);

                        if (this.database.isRightComeback(opponentInsult, comeback)) {  //Comporbamos quien gana la ronda
                            roundWins++;
                            round++;
                            domain = true;
                        } else {
                            opponentRoundWins++;
                            round++;
                        }

                    }
                    this.state = 4;

                    break;
            }
        }
    }

    private ArrayList<Integer> getRandomIndexes() {
        Random rand = new Random(); // Insultos y Comebacks aprendidos aleatoriamente

        ArrayList<Integer> indexes = new ArrayList<>();
        ArrayList<Integer> searchedIndexes = new ArrayList<>();

        for (int i = 0; i < 16; i++) indexes.add(i);

        for (int j = 0; j < 2; j++) {
            int pos = rand.nextInt(15 - j); // 0 - 15
            searchedIndexes.add(indexes.get(pos));
            indexes.remove(pos);
        }

        return searchedIndexes;
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

    public byte[] getHash(String str) {
        byte hashBytes[] = new byte[32];
        MessageDigest digest = null;

        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        byte[] encodedhash = digest.digest(
                str.getBytes(StandardCharsets.UTF_8));

        for (int i = 0; i < 32; i++)
            hashBytes[i] = encodedhash[i];

        return hashBytes;
    }
}
