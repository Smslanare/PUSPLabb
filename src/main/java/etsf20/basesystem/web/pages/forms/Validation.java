package etsf20.basesystem.web.pages.forms;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Validation {
    /**
     * Get and merge errors from multiple fields
     * @param fields fields to read from
     * @return merged errors if any otherwise empty map
     */
    public static Map<String, List<String>> collectErrors(FormField<?> ...fields) {
        Map<String,List<String>> map = new HashMap<>();
        for (FormField<?> field : fields) {
            map.putAll(field.errors());
        }
        return map;
    }
}
