import java.io.IOException;

public class Game {
    private Datagram datagram;
    private int mode;
    private int state;
    private boolean gameBool = true;      //Bucle infinito para el game
    private int player;                   //0->Client   1->Server

    private String name, oponentName;
    private int id, oponentId;
    private byte[] hash, oponentHash;
    private String secret, oponentSecret;

    private int duel = 1;
    private int round = 1;
    private int points = 0;



    public Game(Datagram datagram, int mode) {
        this.datagram = datagram;
        this.mode = mode;
        state = 0;

    }

    private void round() throws IOException {
        while (gameBool) {
            if (state == 0) {             //Recopilación de datos del jugador y mensaje HELLO

                try {
                    System.out.println("Insert your name:");
                    this.name = "Joe";
                    System.out.println("Insert your id:");
                    this.id = 1;
                    this.datagram.write_hello(this.id, this.name);
                    this.oponentName = this.datagram.read_hello();
                    this.oponentId = this.datagram.getIdOponent();
                    this.state = 1;
                } catch (IOException e) {
                    System.out.println("ERROR");
                    System.exit(1);
                }

            }else if(state == 1){         //Envio de HASH entre jugadores

                try {
                    System.out.println("Insert secret number:");
                    //.hash = this.datagram.;
                    this.datagram.writeHash(this.hash);
                    oponentHash = this.datagram.read_hash();
                    this.state = 2;
                } catch (IOException e) {
                    System.out.println("ERROR");
                    System.exit(1);
                }

            }else if(state == 2){            //Envio de SECRET entre jugadores

                try {
                    this.datagram.write_secret(this.secret);
                    this.oponentSecret = this.datagram.read_secret();
                    this.state = 3;
                } catch (IOException e) {
                    System.out.println("ERROR");
                    System.exit(1);
                }

            }else if(state == 3){         //Comprobación de HASH correcto y elección de quien comienza el juego.

                /*try {
                    if(this.datagram.proofHash(this.secret, this.oponentSecret)){
                        if(this.id != this.oponentID){
                            if((Integer.parseInt(this.secret) + Integer.parseInt(this.oponentSecret))%2 == 0){
                                if (this.id < this.oponentId){
                                    player = 0;  //Cliente
                                }else{
                                    player = 1; //Server
                                }
                            }else{
                                if (this.id > this.oponentId){
                                    player = 0;  //Cliente
                                }else{
                                    player = 1; //Server
                                }
                            }
                            this.state = 4;
                        }
                    }
                } catch (IOException e) {
                    System.out.println("ERROR");
                    System.exit(1);
                }*/

            }else if (state == 4){

                try {
                    if (this.duel == 3) {           //Ganamos la partida por haber ganado 3 duelos.
                        this.datagram.write_shout("¡He ganado, " + this.oponentName + " !");
                        System.out.println(this.datagram.read_shout());
                        this.state = 7;
                        this.gameBool = false;
                    } else {
                        if (this.round < 3) {
                            this.state = 5;
                        } else {
                            if (this.points == 0) {         //He perdido 2 rondas de 2
                                this.datagram.write_shout("¡Has ganado, " + this.oponentName + " !");
                                System.out.println(this.datagram.read_shout());
                                this.duel++;
                            } else if (this.points == 2) {   //He ganado 2 rondas de 2
                                this.datagram.write_shout("¡He ganado, " + this.oponentName + " !");
                                System.out.println(this.datagram.read_shout());
                                this.duel++;
                            } else {
                                if (this.round == 4) {      //He perdido 2 rondas de 3
                                    this.datagram.write_shout("¡Has ganado, " + this.oponentName + " !");
                                    System.out.println(this.datagram.read_shout());
                                    this.duel++;
                                } else {                     //Empate 1 a 1, sigue el juego.
                                    this.state = 5;
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    System.out.println("ERROR");
                    System.exit(1);
                }

            }else if (this.state == 5){       //Envio de INSULTS y COMEBACKS
                try {
                    if (this.player == 0) {
                        this.datagram.write_insult("Tonto");
                    }
                } catch (IOException e) {
                    System.out.println("ERROR");
                    System.exit(1);
                }
            }
        }
    }


}
