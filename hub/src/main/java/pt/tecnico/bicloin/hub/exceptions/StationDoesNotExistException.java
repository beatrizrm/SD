package pt.tecnico.bicloin.hub.exceptions;

public class StationDoesNotExistException extends Exception {
    private static final long serialVersionUID = 1L;

    public StationDoesNotExistException(String abrev) {
        super("Estação \"" + abrev + "\" não encontrada");
    }
}
