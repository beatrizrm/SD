package pt.tecnico.bicloin.hub.exceptions;

public class NoDockAvailableException extends Exception {
    private static final long serialVersionUID = 1L;

    public NoDockAvailableException(String station) {
        super("A estação " + station + " não tem docks disponíveis");
    }
}
