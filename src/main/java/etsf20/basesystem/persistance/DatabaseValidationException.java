package etsf20.basesystem.persistance;

/**
 * Used to signal database validation exceptions
 */
public class DatabaseValidationException extends DatabaseException {
    public DatabaseValidationException(Throwable cause) {
        super(cause);
    }

    public DatabaseValidationException(String message) {
        super(message);
    }

    public DatabaseValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
