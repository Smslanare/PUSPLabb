package etsf20.basesystem.web.pages.forms;

import io.javalin.http.Context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class StringFormField implements FormField<String> {

    private static class ValidationStep {
        private final Validator validator;
        private final String errorMessage;

        public ValidationStep(Validator validator, String errorMessage) {
            this.validator = validator;
            this.errorMessage = errorMessage;
        }

        public void validate(String value, List<String> outputErrors) {
            if(!validator.validate(value)) {
                outputErrors.add(errorMessage);
            }
        }
    }

    private final String field;
    private String value;
    private final List<String> errors = new ArrayList<>();
    private final List<ValidationStep> validationSteps = new ArrayList<>();


    /**
     * Create new string form field and register it with the given page
     * @param fieldName name of field, should match name attribute of form field
     */
    public StringFormField(String fieldName) {
        this.field = fieldName;
        this.value = null;
    }

    /**
     * Check that string is not empty or only whitespace
     *
     * @param errorMessage error message to display
     */
    public StringFormField requiredNonEmpty(String errorMessage) {
        validationSteps.add(
                new ValidationStep(
                        value -> !(value == null
                                || value.isEmpty()
                                || value.trim().isEmpty())
                        , errorMessage));
        return this;
    }

    /**
     * Check that string is not empty or only whitespace
     *
     * @param errorMessage error message to display
     */
    public StringFormField requiredNonEmpty(String errorMessage, boolean whitespaceAcceptable) {
        validationSteps.add(
                new ValidationStep(value -> {
                    if(!whitespaceAcceptable)
                        return !(value == null
                                || value.isEmpty()
                                || value.trim().isEmpty());

                    return !(value == null || value.isEmpty());
                }, errorMessage)
        );

        return this;
    }

    /**
     * Check length after trimming string
     * @param min minimum length
     * @param max maximum length
     * @param errorMessage error message to display
     */
    public StringFormField checkLength(int min, int max, String errorMessage) {
        validationSteps.add(new ValidationStep(value -> {
            if(value != null) {
                long count = value.trim().codePoints().count();
                return count >= min && count <= max;
            }

            return false;
        }, errorMessage));

        return this;
    }

    public StringFormField check(Validator validator, String message) {
        validationSteps.add(new ValidationStep(value -> {
            if(value != null) {
                return validator.validate(value);
            }

            return false;
        }, message));

        return this;
    }

    @Override
    public Map<String, List<String>> errors() {
        if(!errors.isEmpty()) {
            return Map.of(field, errors);
        }

        return Collections.emptyMap();
    }

    @Override
    public String get() {
        if(value == null)
            return "";

        return value;
    }

    public String getTrimmed() {
        if(value == null)
            return "";

        return value.trim();
    }

    @Override
    public String getOrDefault(String defaultValue) {
        if(value == null) {
            return defaultValue;
        }

        return get();
    }

    @Override
    public String getFieldName() {
        return field;
    }

    @Override
    public void readValidate(Context ctx) {
        errors.clear();
        value = ctx.formParam(field);
        validationSteps.forEach(step -> step.validate(value, errors));
    }
}
