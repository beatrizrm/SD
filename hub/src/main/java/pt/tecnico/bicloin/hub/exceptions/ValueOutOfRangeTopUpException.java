package pt.tecnico.bicloin.hub.exceptions;

public class ValueOutOfRangeTopUpException extends Exception {
    private static final long serialVersionUID = 1L;

    public ValueOutOfRangeTopUpException(long amount) {
        super("O montante (" + amount + ") que inseriu não está entre 1 e 20 euros.");
    }
}
