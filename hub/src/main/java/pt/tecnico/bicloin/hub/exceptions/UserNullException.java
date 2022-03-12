package pt.tecnico.bicloin.hub.exceptions;

public class UserNullException extends Exception {
    private static final long serialVersionUID = 1L;

    public UserNullException(String username) {
        super("O utilizador não está registado.");
    }
}
