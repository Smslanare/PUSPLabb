package etsf20.basesystem.web.pages;

/**
 * Alert
 * @see   Page#setAlert(Alert)
 * @param type         message type
 * @param message      message content
 * @param dismissible  if the message should be user dismissible
 */
public record Alert(String message, AlertType type, boolean dismissible) {
    public Alert(String message) {
        this(message, AlertType.INFORMATION, true);
    }
}
