import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class Game {
    private Datagram datagram;
    private Database database = new Database();
    private Menu menu;
    private int mode;
    private int state;
    private boolean gameBool = true; // Bucle infinito para el game
    private boolean domain = false; // true->Client   false->Server

    private String name, opponentName;
    private int id, opponentId;
    private byte[] opponentHash;
    private String secret, opponentSecret;

    private int duel = 0;
    private int round = 0;
    private int duelWins = 0;
    private int roundWins = 0;
    private int opponentDuelWins = 0;
    private int opponentRoundWins = 0;

    private ArrayList<String> insultsLearned = new ArrayList<>();      //Aqui guardamos los insultos que aprendemos
    private ArrayList<String> comebacksLearned = new ArrayList<>();    //Aqui guardamos los comebacks que aprendemos

    private ArrayList<String> opponentInsults = new ArrayList<>();
    private ArrayList<String> opponentComebacks = new ArrayList<>();

    private String insult, comeback;
    private String opponentInsult, opponentComeback;

    public Game(Datagram datagram, Menu menu, int mode) throws IOException {
        this.datagram = datagram;
        this.menu = menu;
        this.mode = mode;
        state = 0;
        this.run();
    }


    private void run() throws IOException {
        while (gameBool) {
            if (state == 0) {             //Recopilación de datos del jugador y mensaje HELLO

                //HashMap<String, String> learned = data.getRandomInsultComeback2();
                ArrayList<Integer> indexes = this.getRandomIndexes();
                ArrayList<String> insultsLearned = this.database.getInsultsByIndexes(indexes);
                ArrayList<String> comebacksLearned = this.database.getComebacksByIndexes(indexes);
                this.insultsLearned.addAll(insultsLearned);
                this.comebacksLearned.addAll(comebacksLearned);

                /*
                data.getRandomInsultComeback();
                this.insultsLearned.addAll(data.getRandomInsults());
                this.comebacksLearned.addAll(data.getRandomComebacks());
                 */

                Random rand = new Random();
                this.name = this.menu.getName();
                this.id = rand.nextInt((int) Math.pow((double) 2, 31));  //Id aleatorio

                try {
                    this.datagram.write_hello(this.id, this.name);
                } catch (IOException e) {
                    System.out.println("ERROR HELLO");
                    System.exit(1);
                }

                try {
                    this.opponentName = this.datagram.read_hello();
                } catch (Exception e) {
                    if (e instanceof IOException) {
                        System.out.println(e.getMessage());
                    } else if (e instanceof DatagramException) {
                        System.out.println(e.getMessage());
                    }
                    System.exit(1);
                }

                this.opponentId = this.datagram.getIdOpponent();
                this.state = 1;

            } else if (state == 1) {         //Envio de HASH entre jugadores


                this.secret = this.menu.getSecret();

                try {
                    this.datagram.write_hash(this.secret);
                } catch (IOException e) {
                    System.out.println("ERROR HASH");
                    System.exit(1);
                }

                try {
                    this.opponentHash = this.datagram.read_hash();
                } catch (IOException | DatagramException e) {
                    System.out.println(e.getMessage());
                    System.exit(1);
                }

                this.state = 2;

            } else if (state == 2) {            //Envio de SECRET entre jugadores

                try {
                    this.datagram.write_secret(this.secret);
                } catch (IOException e) {
                    System.out.println("ERROR SECRET");
                    System.exit(1);
                }

                try {
                    this.opponentSecret = this.datagram.read_secret();
                } catch (IOException | DatagramException e) {
                    System.out.println(e.getMessage());
                    System.exit(1);
                }

                this.state = 3;

            } else if (state == 3) {         //Comprobación de HASH correcto y elección de quien comienza el juego.

                if (this.proofHash(this.opponentSecret, this.opponentHash)) {
                    if (this.id != this.opponentId) {
                        if (this.datagram.isEven(this.secret, this.opponentSecret)) {
                            domain = this.id < this.opponentId; // True -> Cliente, False -> Server
                        } else {
                            domain = this.id > this.opponentId;
                        }
                        this.state = 4;
                    } else {
                        try {
                            this.datagram.write_error("ERROR ID");
                        } catch (IOException e) {
                            System.out.println("ERROR ID");
                            System.exit(1);
                        }
                    }
                } else {
                    try {
                        this.datagram.write_error("ERROR HASH");
                    } catch (IOException e) {
                        System.out.println("ERROR HASH");
                        System.exit(1);
                    }
                }

            } else if (state == 4) {                   //Comprobación de los duelos

                if (this.duel < 3) {
                    this.state = 5;                   //Seguimos jugando

                } else {

                    if (this.duelWins == 3 || this.opponentDuelWins == 3) {        //Se acaba la parida pq hay un ganador
                        if (this.duelWins == 3) {        //Ganamos la partida

                            try {
                                this.datagram.write_shout("¡He ganado, " + this.opponentName + " !");
                            } catch (IOException e) {
                                System.out.println("ERROR SHOUT");
                                System.exit(1);
                            }

                            try {
                                System.out.println(this.datagram.read_shout());
                            } catch (IOException e) {
                                System.out.println("ERROR SHOUT");
                                System.exit(1);
                            }

                        } else {            //Perdemos la partida

                            try {
                                this.datagram.write_shout("¡Has ganado, " + this.opponentName + " !");
                            } catch (IOException e) {
                                System.out.println("ERROR SHOUT");
                                System.exit(1);
                            }

                            try {
                                System.out.println(this.datagram.read_shout());
                            } catch (IOException e) {
                                System.out.println("ERROR");
                                System.exit(1);
                            }

                        }

                        //Reseteamos marcadores pq se acaba la partida
                        this.duel = 0;
                        this.round = 0;
                        this.duelWins = 0;
                        this.roundWins = 0;
                        this.opponentDuelWins = 0;
                        this.opponentRoundWins = 0;
                        this.state = 1;             //Al acabar una partida volvemos a enviar Hash

                    } else {
                        this.state = 5;                   //Seguimos jugando
                    }
                }

            } else if (this.state == 5) {            //Comporbacion de las rondas

                if (this.round < 2) {
                    this.state = 6;                 //Seguimos jugando

                } else {

                    if (this.roundWins == 2 || this.opponentRoundWins == 2) {     //Se acaba el duelo pq alguien ha ganado
                        if (this.roundWins == 2) {         //Ganamos el duelo

                            try {
                                this.datagram.write_shout("¡He ganado, " + this.opponentName + " !");
                            } catch (IOException e) {
                                System.out.println("ERROR");
                                System.exit(1);
                            }

                            try {
                                System.out.println(this.datagram.read_shout());
                            } catch (IOException e) {
                                System.out.println("ERROR");
                                System.exit(1);
                            }

                            this.duelWins++;

                        } else {          //Perdemos el duelo

                            try {
                                this.datagram.write_shout("¡Has ganado, " + this.opponentName + " !");
                            } catch (IOException e) {
                                System.out.println("ERROR");
                                System.exit(1);
                            }

                            try {
                                System.out.println(this.datagram.read_shout());
                            } catch (IOException e) {
                                System.out.println("ERROR");
                                System.exit(1);
                            }

                            this.opponentDuelWins++;

                        }

                        //resetamos rondas pq pasamos a otro duelo
                        this.round = 0;
                        this.roundWins = 0;
                        this.opponentRoundWins = 0;
                        this.duel++;
                        this.state = 4;

                    } else {
                        this.state = 6;                 //Seguimos jugando
                    }

                }

            } else if (this.state == 6) {       //Envio de INSULTS y COMEBACKS

                if (this.domain) {  //Empieza el cliente insultando

                    this.menu.showInsults(insultsLearned);          //Mostramos insultos aprendidos
                    this.insult = this.insultsLearned.get(this.menu.getOption());
                    System.out.println(this.insult);

                    try {
                        this.datagram.write_insult(this.insult);
                    } catch (IOException e) {
                        System.out.println("ERROR");
                        System.exit(1);
                    }

                    try {
                        this.opponentComeback = this.datagram.read_comeback();
                    } catch (IOException e) {
                        System.out.println("ERROR");
                        System.exit(1);
                    }

                    this.comebacksLearned.add(this.opponentComeback);
                    System.out.println(this.opponentComeback);

                    if (this.database.isRightComeback(this.insult, this.opponentComeback)) {  //Comporbamos quien gana la ronda
                        this.opponentRoundWins++;
                        this.round++;
                        this.domain = false;
                    } else {
                        this.roundWins++;
                        this.round++;
                    }

                } else {                   //Empieza el server insultando

                    try {
                        this.opponentInsult = this.datagram.read_insult();
                    } catch (IOException | DatagramException e) {
                        System.out.println("ERROR");
                        System.exit(1);
                    }

                    this.insultsLearned.add(this.opponentInsult);
                    System.out.println(this.opponentInsult);
                    this.menu.showComebacks(comebacksLearned);          //Mostramos insultos aprendidos
                    this.comeback = this.comebacksLearned.get(this.menu.getOption());
                    System.out.println(this.comeback);

                    try {
                        this.datagram.write_comeback(this.comeback);
                    } catch (IOException e) {
                        System.out.println("ERROR");
                    }

                    if (this.database.isRightComeback(this.opponentInsult, this.comeback)) {  //Comporbamos quien gana la ronda
                        this.roundWins++;
                        this.round++;
                        this.domain = true;
                    } else {
                        this.opponentRoundWins++;
                        this.round++;
                    }

                }
                this.state = 4;

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


}
