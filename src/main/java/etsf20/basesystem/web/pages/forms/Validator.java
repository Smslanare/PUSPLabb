package etsf20.basesystem.web.pages.forms;

/**
 * Validator function
 */
@FunctionalInterface
public interface Validator {
    /**
     * Validate input
     * @param value value to validate
     * @return true if contents is valid, false if not
     */
    boolean validate(String value);
}
