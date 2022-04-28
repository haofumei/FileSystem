package exceptions;

public class NoFreeBlockException extends RuntimeException {
    public NoFreeBlockException() { }

    public NoFreeBlockException(String message) {
        super(message);
    }
}
