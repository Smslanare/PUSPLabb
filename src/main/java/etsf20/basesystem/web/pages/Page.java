package etsf20.basesystem.web.pages;

import io.javalin.http.Context;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.net.URIBuilder;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Base class for all pages
 */
public abstract class Page {
    private UUID stateRef;
    private Session session;
    private Alert alert;
    protected final Context ctx;

    public Page(Context ctx) {
        this.ctx = ctx;

        // used for return path messages - can be overridden by code
        String message = ctx.queryParam("message");
        String typename = ctx.queryParam("message-type");

        if(message != null) {
            AlertType type = AlertType.INFORMATION;
            if(typename != null) {
                try {
                    type = AlertType.valueOf(typename);
                } catch (IllegalArgumentException e) {
                    LoggerFactory.getLogger(Page.class).error("Invalid message type: {}, reverting to INFORMATION.", typename);
                }
            }

            setAlert(new Alert(message, type, true));
        }

        String stateRef = ctx.queryParam("_s");
        if(stateRef != null) {
            try {
                this.stateRef = UUID.fromString(stateRef);
            } catch (IllegalArgumentException ignored) { }
        }
    }

    /**
     * Get requested path without query string
     */
    public String path() {
        return ctx.path();
    }

    /**
     * Get full path (includes query string)
     */
    public String fullPath() {
        String path = ctx.path();
        String qs = ctx.queryString();
        if(qs != null) {
            path += "?" + qs;
        }
        return path;
    }

    /**
     * Create query pair
     *
     * @param key    parameter key
     * @param value  parameter value
     * @see Page#fullPath(NameValuePair...)
     */
    public NameValuePair param(String key, String value) {
        return new BasicNameValuePair(key, value);
    }

    /**
     * Get full path (includes query string)
     * @param params new query pairs to add, replaces existing ones
     * @return path with new query strings
     */
    public String fullPath(NameValuePair...params) {
        try {
            URIBuilder uriBuilder = new URIBuilder(fullPath());
            for (NameValuePair param : params) {
                uriBuilder.setParameter(param.getName(), param.getValue());
            }
            return uriBuilder.build().toString();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get full path including query path, but make sure there is a stateful reference
     * @return stateful path
     */
    public String statefulPath() {
        return fullPath(new BasicNameValuePair("_s", stateReference().toString()));
    }

    /**
     * Get return path URL or use provided default
     * @param defaultLink default value to use
     * @return link
     */
    public String returnPathOrDefault(String defaultLink) {
        String returnPath = ctx.queryParam("returnPath");
        if(returnPath != null) {
            return returnPath;
        } else {
            return defaultLink;
        }
    }

    /**
     * Unique state reference for this page to be used to main state over multiple page loads
     *
     * <p>Creates new if no prior exist, expected to be use <pre>_s</pre> as its query parameter
     */
    public UUID stateReference() {
        if(this.stateRef == null) {
            this.stateRef = UUID.randomUUID();
        }

        return this.stateRef;
    }

    /**
     * Get state
     * @param stateClass class representing the state
     * @param defaultValue default value if no state could be found
     * @return state or default value
     * @param <T> serialized state type
     */
    public <T extends Serializable> T state(Class<T> stateClass, Supplier<T> defaultValue) {
        return session().getOrDefault(stateReference(), stateClass, defaultValue);
    }

    /**
     * Set state
     * @param value new state
     * @param <T> serialized state type
     */
    public <T extends Serializable> void setState(T value) {
        session().put(stateReference(), value);
    }

    /**
     * Get current session
     */
    public Session session() {
        if(this.session == null) {
            this.session = Session.from(ctx);
        }

        return session;
    }

    /**
     * Get active alert if any
     * @return alert or null if no active alert
     */
    public Alert getAlert() {
        return alert;
    }

    /**
     * Set alert, will be visible in supported layouts
     * @param alert alert to set
     */
    public void setAlert(Alert alert) {
        this.alert = alert;
    }

    /**
     * Helper method to get a default parameter map with this page
     */
    protected Map<String,Page> templateMap() {
        return Collections.singletonMap("page", this);
    }

    /**
     * Render page using the specified JTE view template
     * @param viewTemplate path to view template, relative to {@code src/jte} in the workspace root
     */
    public void render(String viewTemplate) {
        ctx.render(viewTemplate, templateMap());
    }

    /**
     * Render page
     */
    public abstract void render();
}
