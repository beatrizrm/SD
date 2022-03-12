package pt.tecnico.bicloin.hub.exceptions;

public class NoMoneyException extends Exception {
    private static final long serialVersionUID = 1L;

    public NoMoneyException(String username) {
        super("O utilizador " + username + " não tem saldo suficiente");
    }
}
