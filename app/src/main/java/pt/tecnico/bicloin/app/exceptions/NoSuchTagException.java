package pt.tecnico.bicloin.app.exceptions;

public class NoSuchTagException extends Exception {
    private static final long serialVersionUID = 1L;

    public NoSuchTagException(String tag) {
        super("tag " + tag + " não existe");
    }
}
