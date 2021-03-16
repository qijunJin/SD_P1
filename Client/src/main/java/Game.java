import enumType.ShoutType;
import enumType.StateType;
import exception.EmptyHashException;
import exception.OpcodeException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class Game {
    private Datagram datagram;
    private Menu menu;
    private int mode;
    private StateType state;
    private boolean gameBool = true; // Bucle infinito para el game

    private String insult, comeback;
    private String opponentInsult, opponentComeback;

    private DatabaseProvider dp;

    private Player player1 = new Player(); // Me
    private Player player2 = new Player(); // Opponent

    public Game(Datagram datagram, int mode) throws IOException {
        this.datagram = datagram;
        this.menu = new Menu();
        this.mode = mode;
        state = StateType.START;
        this.run();
    }

    private void run() {
        while (gameBool) {
            if (state == StateType.START) {

                this.dp = new DatabaseProvider();
                this.player1.addInsultComeback(this.dp.getRandomInsultComeback());
                this.player1.addInsultComeback(this.dp.getRandomInsultComeback());
                this.state = StateType.HELLO;

            } else if (state == StateType.HELLO) {

                /* Obtain the data */
                this.player1.setName(this.menu.getName());
                this.player1.setId(this.menu.getId());

                try {
                    this.datagram.write_hello(this.player1.getId(), this.player1.getName());
                } catch (IOException e) {
                    System.out.println("Hello Error Write " + e.getMessage());
                    System.exit(1);
                }

                try {
                    this.player2.setName(this.datagram.read_hello());
                    this.player2.setId(this.datagram.getIdOpponent());
                } catch (IOException | OpcodeException e) {
                    System.out.println("Hello Error Read " + e.getMessage());
                    System.exit(1);
                }

                this.state = StateType.HASH;

            } else if (state == StateType.HASH) {

                try {
                    this.datagram.write_hash(this.player1.generateSecret());
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                    System.exit(1);
                }

                try {
                    this.player2.setHash(this.datagram.read_hash());
                } catch (IOException | OpcodeException | EmptyHashException e) {
                    System.out.println(e.getMessage());
                    System.exit(1);
                }

                this.state = StateType.SECRET;

            } else if (state == StateType.SECRET) {            //Envio de SECRET entre jugadores
                try {
                    this.datagram.write_secret(this.player1.getSecret());
                    this.menu.showSecret(this.player1.getSecret());
                } catch (IOException e) {
                    System.out.println("ERROR SECRET");
                    System.exit(1);
                }
                try {
                    this.player2.setSecret(this.datagram.read_secret());
                } catch (IOException | OpcodeException e) {
                    System.out.println(e.getMessage());
                    System.exit(1);
                }

                if (this.proofHash(this.player2.getSecret(), this.player2.getHash())) {
                    System.out.println("Proof Hash");
                    if (this.player1.getId() != this.player2.getId()) {
                        System.out.println("Dif ID");
                        if (this.isEven(this.player1.getSecret(), this.player2.getSecret())) {
                            System.out.println("Par");
                            if (this.player1.getId() < this.player2.getId()) { // True -> Cliente, False -> Server
                                this.state = StateType.INSULT;
                                System.out.println("Par Insult");
                            } else {
                                this.state = StateType.COMEBACK;
                                System.out.println("Par Comeback");
                            }
                        } else {
                            System.out.println("Impar");
                            if (this.player1.getId() > this.player2.getId()) {
                                this.state = StateType.INSULT;
                                System.out.println("Impar Insult");
                            } else {
                                this.state = StateType.COMEBACK;
                                System.out.println("Impar Comeback");
                            }
                            /*try {
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
                        */
                        }
                    }
                    System.out.println("Term");
                    //this.state = StateType.INSULT;
                }
            } else if (state == StateType.INSULT) {                   //Comprobación de los duelos
                System.out.println("Insult");

                this.menu.showInsults(this.player1.getInsults());          //Mostramos insultos aprendidos
                this.insult = this.player1.getInsults().get(this.menu.getOption());
                System.out.println(this.insult);

                try {
                    this.datagram.write_insult(this.insult);
                } catch (IOException e) {
                    System.out.println("ERROR");
                    System.exit(1);
                }

                try {
                    this.opponentComeback = this.datagram.read_comeback();
                } catch (IOException | OpcodeException e) {
                    System.out.println("ERROR");
                    System.exit(1);
                }

                this.player1.addComeback(this.opponentComeback);
                System.out.println(this.opponentComeback);

                if (this.dp.isRightComeback(this.insult, this.opponentComeback)) {  //Comporbamos quien gana la ronda
                    this.player2.addRound();
                    this.state = StateType.COMEBACK;
                } else {
                    this.player1.addRound();
                }

                if (this.player1.getRound() == 2 | this.player2.getRound() == 2) {
                    state = StateType.SHOUT;
                }

            } else if (state == StateType.COMEBACK) {
                System.out.println("Comeback");
                try {
                    this.opponentInsult = this.datagram.read_insult();
                } catch (IOException | OpcodeException e) {
                    System.out.println("ERROR");
                    System.exit(1);
                }

                this.player1.addInsult(this.opponentInsult);
                System.out.println(this.opponentInsult);

                this.menu.showComebacks(this.player1.getComebacks());          //Mostramos insultos aprendidos
                this.comeback = this.player1.getComebacks().get(this.menu.getOption());
                System.out.println(this.comeback);

                try {
                    this.datagram.write_comeback(this.comeback);
                } catch (IOException e) {
                    System.out.println("ERROR");
                }

                if (this.dp.isRightComeback(this.opponentInsult, this.comeback)) {  //Comporbamos quien gana la ronda
                    this.player1.addRound();
                    this.state = StateType.INSULT;
                } else {
                    this.player2.addRound();
                }

                if (this.player1.getRound() == 2 | this.player2.getRound() == 2) {
                    state = StateType.SHOUT;
                }

            } else if (state == StateType.SHOUT) {

                // if (this.player1.getDuel() == 3 | this.player2.getDuel() == 3 | this.player1.getRound() == 2 | this.player2.getRound() == 2) {
                if (this.player1.getDuel() == 3 | this.player1.getRound() == 2) {
                    try {
                        String str = this.dp.getShoutByEnumAddName(ShoutType.I_WIN, this.player2.getName());
                        this.datagram.write_shout(str);
                    } catch (IOException e) {
                        System.out.println("ERROR SHOUT");
                        System.exit(1);
                    }

                    try {
                        System.out.println(this.datagram.read_shout());
                    } catch (IOException | OpcodeException e) {
                        System.out.println("ERROR SHOUT");
                        System.exit(1);
                    }

                } else if (this.player2.getDuel() == 3 | this.player2.getRound() == 2) {
                    try {
                        String str = this.dp.getShoutByEnumAddName(ShoutType.YOU_WIN, this.player1.getName());
                        this.datagram.write_shout(str);
                    } catch (IOException e) {
                        System.out.println("ERROR SHOUT");
                        System.exit(1);
                    }

                    try {
                        System.out.println(this.datagram.read_shout());
                    } catch (IOException | OpcodeException e) {
                        System.out.println("ERROR");
                        System.exit(1);
                    }
                }

                if (this.player1.getRound() == 2 | this.player2.getRound() == 2) {
                    if (this.player1.getRound() == 2) {
                        this.player1.addDuel();

                        this.player1.resetRound();
                        this.player2.resetRound();

                        state = StateType.INSULT;
                    }
                    if (this.player2.getRound() == 2) {
                        this.player1.addDuel();

                        this.player1.resetRound();
                        this.player2.resetRound();

                        state = StateType.COMEBACK;
                    }
                }

                if (this.player1.getDuel() == 3 | this.player2.getDuel() == 3) {
                    this.player1.resetDuelRound();
                    this.player2.resetDuelRound();

                    this.state = StateType.HASH;

                }
            } else if (state == StateType.ERROR) {


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

                /*
                if (this.duel < 3) {
                    this.state = 5;                   //Seguimos jugando

                } else {

                    if (this.duelWins == 3 || this.opponentDuelWins == 3) {        //Se acaba la parida pq hay un ganador
                        if (this.duelWins == 3) {        //Ganamos la partida

                            try {
                                String str = this.database.getShoutByEnumAddName(ShoutType.I_WIN, this.opponentName);
                                this.datagram.write_shout(str);
                            } catch (IOException e) {
                                System.out.println("ERROR SHOUT");
                                System.exit(1);
                            }

                            try {
                                System.out.println(this.datagram.read_shout());
                            } catch (IOException | exception.DatagramException e) {
                                System.out.println("ERROR SHOUT");
                                System.exit(1);
                            }

                        } else {            //Perdemos la partida

                            try {
                                String str = this.database.getShoutByEnumAddName(ShoutType.YOU_WIN, this.name);
                                this.datagram.write_shout(str);
                            } catch (IOException e) {
                                System.out.println("ERROR SHOUT");
                                System.exit(1);
                            }

                            try {
                                System.out.println(this.datagram.read_shout());
                            } catch (IOException | exception.DatagramException e) {
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
                                String str = this.database.getShoutByEnumAddName(ShoutType.I_WIN, this.opponentName);
                                this.datagram.write_shout(str);
                            } catch (IOException e) {
                                System.out.println("ERROR");
                                System.exit(1);
                            }

                            try {
                                System.out.println(this.datagram.read_shout());
                            } catch (IOException | exception.DatagramException e) {
                                System.out.println("ERROR");
                                System.exit(1);
                            }

                            this.duelWins++;

                        } else {          //Perdemos el duelo

                            try {
                                String str = this.database.getShoutByEnumAddName(ShoutType.YOU_WIN, this.opponentName);
                                this.datagram.write_shout(str);
                            } catch (IOException e) {
                                System.out.println("ERROR");
                                System.exit(1);
                            }

                            try {
                                System.out.println(this.datagram.read_shout());
                            } catch (IOException | exception.DatagramException e) {
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
                    } catch (IOException | exception.DatagramException e) {
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
                    } catch (IOException | exception.DatagramException e) {
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
*/
