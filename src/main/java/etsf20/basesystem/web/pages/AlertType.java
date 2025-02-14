package etsf20.basesystem.web.pages;

/**
 * Type of alert
 * @see Alert
 */
public enum AlertType {
    NONE("",""),
    INFORMATION("primary", "info"),
    SUCCESS("success", "check-circle"),
    WARNING("warning", "exclamation-triangle"),
    ERROR("danger", "exclamation-triangle");

    private final String bootstrapClass;
    private final String bootstrapIcon;

    AlertType(String bootstrapClass, String bootstrapIcon) {
        this.bootstrapClass = bootstrapClass;
        this.bootstrapIcon = bootstrapIcon;
    }

    public String getBootstrapClass() {
        return bootstrapClass;
    }

    public String getBootstrapIcon() {
        return bootstrapIcon;
    }
}
