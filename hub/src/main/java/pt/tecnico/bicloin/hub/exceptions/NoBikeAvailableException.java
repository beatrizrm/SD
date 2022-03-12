package pt.tecnico.bicloin.hub.exceptions;

public class NoBikeAvailableException extends Exception {
    private static final long serialVersionUID = 1L;

    public NoBikeAvailableException(String station) {
        super("A estação " + station + " não tem bicicletas disponíveis");
    }
}
