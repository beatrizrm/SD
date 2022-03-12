package pt.tecnico.bicloin.hub.exceptions;

public class AmountNotInEurosException extends Exception {
    private static final long serialVersionUID = 1L;

    public AmountNotInEurosException() {
        super("O montante a adicionar não está em euros.");
    }
}
