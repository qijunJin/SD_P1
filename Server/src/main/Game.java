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

    Datagram datagram;
    private BufferedWriter log;
    private Database database = new Database();
    private boolean gameBool = true;
    Random rand = new Random();
    private int state = 0;


    public Game(Socket socket) throws IOException {
        this.datagram = new Datagram(socket);

        String lg = "Server"+Thread.currentThread().getName()+".log";
        (new File("logs")).mkdir();
        File f = new File("logs/" + lg);
        this.log = new BufferedWriter(new FileWriter(f));
    }

    public void run() throws IOException {
        //Comienza el juego.
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

                    try {
                        opponentName = this.datagram.read_hello();
                    } catch (DatagramException | IOException e) {
                        this.log.write(e.getMessage());
                        this.log.flush();
                        System.exit(1);
                    }

                    opponentId = this.datagram.getIdOpponent();

                    try {
                        this.datagram.write_hello(id, name);
                    } catch (IOException e) {
                        this.log.write(e.getMessage());
                        this.log.flush();
                        System.exit(1);
                    }

                    this.log.write("HELLO: " + opponentId + opponentName);
                    this.log.write("HELLO: " + id + name);
                    this.state = 1;

                    break;
                case 1:          //Envio de HASH entre jugadores

                    /*Añadir insultos y combeacks aleatorios despues de cada partida*/

                    try {
                        opponentHash = this.datagram.read_hash();
                    } catch (IOException | DatagramException e) {
                        this.log.write(e.getMessage());
                        this.log.flush();
                        System.exit(1);
                    }

                    secret = Integer.toString(rand.nextInt());

                    try {
                        this.datagram.write_hash(secret);
                    } catch (IOException e) {
                        this.log.write(e.getMessage());
                        this.log.flush();
                        System.exit(1);
                    }

                    this.log.write("HASH: " + opponentHash);
                    this.log.write("HASH: " + getHash(secret));
                    this.state = 2;

                    break;

                case 2:             //Envio de SECRET entre jugadores

                    try {
                        opponentSecret = this.datagram.read_secret();
                    } catch (IOException | DatagramException e) {
                        System.out.println(e.getMessage());
                        this.log.write(e.getMessage());
                        this.log.flush();
                        System.exit(1);
                    }

                    try {
                        this.datagram.write_secret(secret);
                    } catch (IOException e) {
                        this.log.write(e.getMessage());
                        this.log.flush();
                        System.exit(1);
                    }

                    this.log.write("SECRET: " + opponentSecret);
                    this.log.write("SECRET: " + secret);
                    this.state = 3;

                    break;
                case 3:          //Comprobación de HASH correcto y elección de quien comienza el juego.

                    if (this.proofHash(opponentSecret, opponentHash)) {
                        if (id != opponentId) {
                            if (datagram.isEven(secret, opponentSecret)) {
                                domain = id > opponentId; // True -> Servidor, False -> Client
                            } else {
                                domain = id < opponentId;
                            }
                            this.state = 4;
                        } else {
                            try {
                                this.datagram.write_error("ERROR ID");
                            } catch (IOException e) {
                                this.log.write(e.getMessage());
                                this.log.flush();
                                System.exit(1);
                            }
                            this.log.write(this.database.getErrorByEnum(ErrorType.WRONG_OPCODE));  //CAMBIAR
                        }
                    } else {
                        try {
                            this.datagram.write_error("ERROR HASH");
                        } catch (IOException e) {
                            this.log.write(e.getMessage());
                            this.log.flush();
                            System.exit(1);
                        }
                        this.log.write(this.database.getErrorByEnum(ErrorType.WRONG_OPCODE));  //CAMBIAR
                    }

                    break;
                case 4:                    //Comprobación de los duelos

                    if (duel < 3) {
                        this.state = 5;                   //Seguimos jugando

                    } else {

                        if (duelWins == 3 || opponentDuelWins == 3) {        //Se acaba la parida pq hay un ganador
                            if (duelWins == 3) {        //Ganamos la partida

                                try {
                                    System.out.println(this.datagram.read_shout());
                                } catch (IOException e) {
                                    this.log.write(e.getMessage());
                                    this.log.flush();
                                    System.exit(1);
                                }

                                try {
                                    this.datagram.write_shout("¡He ganado, " + opponentName + " !");
                                } catch (IOException e) {
                                    this.log.write(e.getMessage());
                                    this.log.flush();
                                    System.exit(1);
                                }

                                this.log.write("SHOUT: ¡Has ganado, " + name + " !");
                                this.log.write("SHOUT: ¡He ganado, " + opponentName + " !");

                            } else {            //Perdemos la partida

                                try {
                                    System.out.println(this.datagram.read_shout());
                                } catch (IOException e) {
                                    this.log.write(e.getMessage());
                                    this.log.flush();
                                    System.exit(1);
                                }

                                try {
                                    this.datagram.write_shout("¡Has ganado, " + opponentName + " !");
                                } catch (IOException e) {
                                    this.log.write(e.getMessage());
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

                    if (round < 2) {
                        this.state = 6;                 //Seguimos jugando

                    } else {

                        if (roundWins == 2 || opponentRoundWins == 2) {     //Se acaba el duelo pq alguien ha ganado
                            if (roundWins == 2) {         //Ganamos el duelo

                                try {
                                    System.out.println(this.datagram.read_shout());
                                } catch (IOException e) {
                                    this.log.write(e.getMessage());
                                    this.log.flush();
                                    System.exit(1);
                                }

                                try {
                                    this.datagram.write_shout("¡He ganado, " + opponentName + " !");
                                } catch (IOException e) {
                                    this.log.write(e.getMessage());
                                    this.log.flush();
                                    System.exit(1);
                                }

                                this.log.write("SHOUT: ¡Has ganado, " + name + " !");
                                this.log.write("SHOUT: ¡He ganado, " + opponentName + " !");
                                duelWins++;

                            } else {          //Perdemos el duelo

                                try {
                                    System.out.println(this.datagram.read_shout());
                                } catch (IOException e) {
                                    this.log.write(e.getMessage());
                                    this.log.flush();
                                    System.exit(1);
                                }

                                try {
                                    this.datagram.write_shout("¡Has ganado, " + opponentName + " !");
                                } catch (IOException e) {
                                    this.log.write(e.getMessage());
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

                    if (domain) {  //Empieza el servidor insultando

                        int n = rand.nextInt(insultsLearned.size());
                        insult =insultsLearned.get(n);

                        try {
                            this.datagram.write_insult(insult);
                        } catch (IOException e) {
                            this.log.write(e.getMessage());
                            this.log.flush();
                            System.exit(1);
                        }

                        try {
                            opponentComeback = this.datagram.read_comeback();
                        } catch (IOException e) {
                            this.log.write(e.getMessage());
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

                        try {
                            opponentInsult = this.datagram.read_insult();
                        } catch (IOException | DatagramException e) {
                            this.log.write(e.getMessage());
                            this.log.flush();
                            System.exit(1);
                        }

                        insultsLearned.add(opponentInsult);
                        int n = rand.nextInt(insultsLearned.size());
                        comeback = comebacksLearned.get(n);

                        try {
                            this.datagram.write_comeback(comeback);
                        } catch (IOException e) {
                            this.log.write(e.getMessage());
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

    public byte[] getHash(String str){
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
