package pt.tecnico.bicloin.hub.exceptions;

public class WrongPhoneNumberException extends Exception {
    private static final long serialVersionUID = 1L;

    public WrongPhoneNumberException(String username, String phoneNumber) {
        super("O número de telemóvel " + phoneNumber + " não está associado ao utilizador " + username);
    }
}
