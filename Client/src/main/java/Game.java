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

    private int duel = 0;
    private int round = 1;
    private int points = 0;

    private String insulto, comeback;



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
                    this.secret = "123456789";
                    this.datagram.writeHash(this.secret);
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

                if(this.datagram.proofHash(this.secret, this.oponentHash)){
                    if(this.id != this.oponentId){
                        if(this.datagram.isEven(this.secret, this.oponentSecret)){
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
                    }else{
                        try {
                            this.datagram.write_error("ERROR ID");
                        } catch (IOException e) {
                            System.out.println("ERROR ID");
                        }
                    }
                }else{
                    try {
                        this.datagram.write_error("ERROR HASH");
                    } catch (IOException e) {
                        System.out.println("ERROR HASH");
                    }
                }


            }else if (state == 4){

                if (this.duel == 3) {           //Ganamos la partida por haber ganado 3 duelos.
                    try {
                        this.datagram.write_shout("¡He ganado, " + this.oponentName + " !");
                        System.out.println(this.datagram.read_shout());
                    } catch (IOException e){
                        System.out.println("ERROR");
                    }
                    this.duel = 0;
                    this.state = 7;
                    this.gameBool = false;
                } else {
                    if (this.round < 3) {
                        this.state = 5;
                    } else {
                        if (this.points == 0) {         //He perdido 2 rondas de 2
                            try {
                                this.datagram.write_shout("¡Has ganado, " + this.oponentName + " !");
                                System.out.println(this.datagram.read_shout());
                            }catch (IOException e){
                                System.out.println("ERROR");
                            }
                        } else if (this.points == 2) {   //He ganado 2 rondas de 2
                            try {
                                this.datagram.write_shout("¡He ganado, " + this.oponentName + " !");
                                System.out.println(this.datagram.read_shout());
                            }catch (IOException e){
                                System.out.println("ERROR");
                            }
                            this.duel++;
                            this.round = 1;
                        } else {
                            if (this.round == 4) {      //He perdido 2 rondas de 3
                                try {
                                    this.datagram.write_shout("¡Has ganado, " + this.oponentName + " !");
                                    System.out.println(this.datagram.read_shout());
                                }catch (IOException e){
                                    System.out.println("ERROR");
                                }
                                this.round = 1;
                            } else {                     //Empate 1 a 1, sigue el juego.
                                this.state = 5;
                            }
                        }
                    }
                }

            }else if (this.state == 5){       //Envio de INSULTS y COMEBACKS

                    if (this.player == 0) {
                        try {
                            this.datagram.write_insult("Tonto");
                            this.comeback = this.datagram.read_comeback();
                        } catch (IOException e) {
                            System.out.println("ERROR");
                        }
                        this.state = 6;
                    }else{
                        try {
                            System.out.println(this.datagram.read_insult());
                            //System.out.println(comebacks);
                            String Comeback = "tonto";
                            this.datagram.write_comeback(Comeback);
                        } catch (IOException e) {
                            System.out.println("ERROR");
                        }
                        this.state = 6;
                    }

            }else if(this.state == 6){

                //Comprobar si el comeback o insulto es correcto.

            }
        }
    }


}
