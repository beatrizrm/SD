package pt.tecnico.bicloin.hub.exceptions;

public class ErrorConnectingException extends Exception {
    private static final long serialVersionUID = 1L;

    public ErrorConnectingException() {
        super("Não foi possível contactar o servidor");
    }

    public ErrorConnectingException(String msg) {
        super("Não foi possível contactar o servidor: " + msg);
    }
}
