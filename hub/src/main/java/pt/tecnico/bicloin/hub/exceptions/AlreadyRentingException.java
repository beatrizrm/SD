package pt.tecnico.bicloin.hub.exceptions;

public class AlreadyRentingException extends Exception {
    private static final long serialVersionUID = 1L;

    public AlreadyRentingException(String username) {
        super("O utilizador " + username + " já está a alugar uma bicicleta");
    }
}
