package etsf20.basesystem.web;

/**
 * Validation error - correctly formatted but invalid or erroneous input
 * 422 Unprocessable entity will be set as the status code
 */
public class ValidationException extends Exception {

    private final String title;

    public ValidationException(String title, String message) {
        super(message);
        this.title = title;
    }

    public ValidationException(String title, String message, Throwable cause) {
        super(message, cause);
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
