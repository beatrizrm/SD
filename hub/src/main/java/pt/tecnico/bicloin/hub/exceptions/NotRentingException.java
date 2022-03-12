package pt.tecnico.bicloin.hub.exceptions;

public class NotRentingException extends Exception {
    private static final long serialVersionUID = 1L;

    public NotRentingException(String username) {
        super("O utilizador " + username + " não está a alugar uma bicicleta");
    }
}
