package etsf20.basesystem.web.pages;

import io.javalin.http.Context;

/**
 * Error page
 */
public class ErrorPage extends Page {

    private final String title;
    private final String description;

    public ErrorPage(Context ctx, String title, String description) {
        super(ctx);
        this.title = title;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public void render() {
        this.render("error.jte");
    }
}
