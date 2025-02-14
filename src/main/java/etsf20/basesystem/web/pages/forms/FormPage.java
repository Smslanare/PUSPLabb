package etsf20.basesystem.web.pages.forms;

import io.javalin.http.Context;
import io.javalin.http.HandlerType;
import io.javalin.http.HttpStatus;
import etsf20.basesystem.web.pages.Alert;
import etsf20.basesystem.web.pages.AlertType;
import etsf20.basesystem.web.pages.Page;

import java.util.*;

/**
 * Page that has a form element in it that needs to be read and validated
 */
public abstract class FormPage extends Page {

    private Map<String, List<String>> errors = Collections.emptyMap();
    private final Set<String> fields = new HashSet<>();
    private boolean post = false;

    public FormPage(Context ctx) {
        super(ctx);
    }

    /**
     * Register that field exists
     * @param fieldName name of field
     */
    protected void registerField(String fieldName) {
        this.fields.add(fieldName);
    }

    /**
     * Read and register field, initially it will have no errors
     * @param fieldName name of field
     * @return form value
     */
    protected String readField(String fieldName) {
        this.fields.add(fieldName);
        return ctx.formParam(fieldName);
    }

    /**
     * Read and register field, this will also do validation of given field
     * @param field defined field
     */
    protected void readField(FormField<?> field) {
        this.fields.add(field.getFieldName());
        field.readValidate(ctx);
    }

    /**
     * Reads the form if the page is posted to, must be called by child class to read the form
     */
    public void readForm() {
        if(ctx.method() == HandlerType.POST) {
            this.post = true;
            errors = step();
            if(!errors.isEmpty()) {
                setAlert(new Alert("one ore more form fields has errors", AlertType.ERROR, true));
                ctx.status(HttpStatus.UNPROCESSABLE_CONTENT);
            }
        }
    }

    /**
     * Validate form and update internal state based on form data
     *
     * @see FormPage#readForm()
     * @return errors as a map that maps field name to list of errors
     */
    protected abstract Map<String, List<String>> step();

    /**
     * Get error feedback for a given field
     * @throws IllegalArgumentException if field has not been registered in {@code step(Context)}
     * @see FormPage#step()
     */
    public String getErrorFeedback(String field) {
        if(post) {
            if (!this.fields.contains(field)) {
                throw new IllegalArgumentException("field '" + field + "' is not registered, check your step() method.");
            }

            List<String> validationErrors = errors.getOrDefault(field, Collections.emptyList());
            if (validationErrors.isEmpty()) {
                return "";
            }

            return String.join(", ", validationErrors);
        }

        return "";
    }

    /**
     * Checks if the is form valid
     * @return true if the content of the form is valid
     */
    public boolean isFormValid() {
        return errors.isEmpty();
    }

    /**
     * Check if a field has a validation error
     * @param field field to check
     * @throws IllegalArgumentException if field has not been registered in {@code step(Context)}
     * @see FormPage#step()
     * @return true if there is an error
     */
    public boolean fieldHasError(String field) {
        if(post) {
            if (!this.fields.contains(field)) {
                throw new IllegalArgumentException("field '" + field + "' is not registered, check your step() method.");
            }

            return !errors.getOrDefault(field, Collections.emptyList()).isEmpty();
        }

        return false;
    }

    /**
     * Get the bootstrap validation class for a field
     * @return is-valid or is-invalid classes
     * @throws IllegalArgumentException if field has not been registered in {@code step(Context)}
     * @see FormPage#fieldHasError(String)
     * @see FormPage#isFormValid()
     * @see FormPage#step()
     */
    public String getFieldBootstrapCssClass(String field) {
        if(post) {
            if(!this.fields.contains(field)) {
                throw new IllegalArgumentException("field '" + field + "' is not registered, check your step() method.");
            }

            if(errors.getOrDefault(field, Collections.emptyList()).isEmpty()) {
                return "is-valid";
            } else {
                return "is-invalid";
            }
        }

        return "";
    }

    /**
     * Get form errors as field to list of errors
     * @return form errors, empty map if no errors
     */
    public Map<String, List<String>> getErrors() {
        return errors;
    }
}
