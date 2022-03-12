package pt.tecnico.bicloin.app.exceptions;

public class ValueOutOfRangeException extends Exception {
    private static final long serialVersionUID = 1L;

    public ValueOutOfRangeException(String valueName, double min, double max) {
        super(valueName + " deve ser um valor entre " + min + " e " + max);
    }

    public ValueOutOfRangeException(String valueName, int min, int max) {
        super(valueName + " deve ser um valor entre " + min + " e " + max);
    }
}
