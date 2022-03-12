package pt.tecnico.bicloin.hub.exceptions;

public class TooFarException extends Exception {
    private static final long serialVersionUID = 1L;

    public TooFarException(String username, String station) {
        super("O utilizador " + username + " está longe da estação " + station);
    }
}
