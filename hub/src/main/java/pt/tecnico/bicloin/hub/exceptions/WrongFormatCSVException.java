package pt.tecnico.bicloin.hub.exceptions;

public class WrongFormatCSVException extends Exception {
    private static final long serialVersionUID = 1L;

    public WrongFormatCSVException() {
        super("Ficheiro CSV mal formatado");
    }

    public WrongFormatCSVException(String error) {
        super("Ficheiro CSV mal formatado (Erro: " + error + ")");
    }
}
