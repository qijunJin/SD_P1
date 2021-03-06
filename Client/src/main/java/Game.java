public class Game {
    private Datagram datagram;
    private int mode;
    private int id;
    private int state;

    public Game(Datagram datagram, int mode) {
        this.datagram = datagram;
        this.mode = mode;
        state = 0;

    }

    private void round() {
        if (state == 0) {
            id = 400;

        }
    }


}
