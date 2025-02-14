package etsf20.basesystem.web.pages.forms;

import io.javalin.http.Context;

import java.util.List;
import java.util.Map;

/**
 * Form field for validation
 * @param <T>
 */
public interface FormField<T> {
    /**
     * Get form field errors after validation
     * @return map with field name to list of errors
     */
    Map<String, List<String>> errors();

    /**
     * Get field value if any
     */
    T get();

    /**
     * Get field value if has been read otherwise default value
     * @param defaultValue default value
     * @return field value or default value if no form has been posted
     */
    T getOrDefault(T defaultValue);

    /**
     * @return name of field
     */
    String getFieldName();

    /**
     * Read and validate field
     * @param ctx context
     */
    void readValidate(Context ctx);
}
