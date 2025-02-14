package etsf20.basesystem.web.controllers;

import io.javalin.http.Context;
import etsf20.basesystem.web.ValidationException;
import etsf20.basesystem.web.pages.Alert;
import etsf20.basesystem.web.pages.AlertType;
import etsf20.basesystem.web.pages.TemplatePage;
import io.javalin.http.HttpStatus;
import org.apache.hc.core5.net.URIBuilder;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Common operations used by request handlers
 */
public class Controllers {
    private static URI appendUri(String uri, String appendQuery) throws ValidationException {
        try {
            URI oldUri = new URI(uri);
            return new URI(oldUri.getScheme(), oldUri.getAuthority(), oldUri.getPath(),
                    oldUri.getQuery() == null ? appendQuery : oldUri.getQuery() + "&" + appendQuery, oldUri.getFragment());
        } catch(URISyntaxException ex) {
            throw new ValidationException("Invalid URL in returnPath", ex.getMessage(), ex);
        }
    }

    /**
     * Read UUID from a query paraemter
     * @param ctx        context
     * @param pathParam  query parameter to read from
     * @return UUID
     * @throws ValidationException if query parameter does not contain a valid UUID
     */
    public static UUID readUUID(Context ctx, String pathParam) throws ValidationException {
        String pathUuid = ctx.pathParam(pathParam);

        UUID journalUuid;
        try {
            journalUuid = UUID.fromString(pathUuid);
        } catch(IllegalArgumentException ex) {
            throw new ValidationException("Could not parse UUID", ex.getMessage(), ex);
        }

        return journalUuid;
    }

    /**
     * Add state reference if missing
     * @param ctx context
     * @return true if state reference was missing, expects the caller to return immediately
     */
    public static boolean addStateReferenceIfMissing(Context ctx) throws ValidationException {
        String stateRef = ctx.queryParam("_s");
        if(stateRef == null) {
            URI s = null;
            try {
                s = new URIBuilder(ctx.fullUrl()).addParameter("_s", UUID.randomUUID().toString()).build();
            } catch (URISyntaxException e) {
                throw new ValidationException("Could not parse URL", e.getMessage(), e);
            }

            String url = s.getPath() + "?" + s.getQuery();
            ctx.status(HttpStatus.TEMPORARY_REDIRECT);
            ctx.redirect(url);
            return true;
        } else {
            try {
                UUID.fromString(stateRef);
            } catch(IllegalArgumentException ex) {
                throw new ValidationException("Could not parse UUID", ex.getMessage(), ex);
            }
            return false;
        }
    }

    /**
     * Set message on next page load by using a redirect or render an empty page with the message if no redirect found
     * @param ctx     context
     * @param message alert message to display
     * @param type    alert type
     * @throws ValidationException if return path could not be parsed
     */
    public static void returnPathMessageRedirect(Context ctx, String message, AlertType type) throws ValidationException {
        returnPathMessageRedirect(ctx, message, type, null);
    }

    /**
     * Set message on next page load by using a redirect or render an empty page with the message if no redirect specified
     * @param ctx     context
     * @param message alert message to display
     * @param type    alert type
     * @param defaultRedirect default redirect if none could be found in the query string
     * @throws ValidationException if return path could not be parsed
     */
    public static void returnPathMessageRedirect(Context ctx, String message, AlertType type, String defaultRedirect) throws ValidationException {
        String returnPath = ctx.queryParam("returnPath");
        if(returnPath != null) {
            ctx.redirect(appendUri(returnPath, "message=" + URLEncoder.encode(message, StandardCharsets.UTF_8) + "&message-type=" + type.toString()).toString());
        } else if(defaultRedirect != null){
            ctx.redirect(appendUri(defaultRedirect, "message=" + URLEncoder.encode(message, StandardCharsets.UTF_8) + "&message-type=" + type.toString()).toString());
        } else {
            TemplatePage page = TemplatePage.from(ctx, "pages/utility/empty.jte");
            page.setAlert(new Alert(message, type, false));
            page.render();
        }
    }
}
