package etsf20.basesystem.web.pages;

import io.javalin.http.Context;

/**
 * Basic page that renders a template without any special data
 */
public final class TemplatePage extends Page {
    private final String viewTemplate;

    private TemplatePage(Context ctx, String viewTemplate) {
        super(ctx);
        this.viewTemplate = viewTemplate;
    }

    public static TemplatePage from(Context ctx, String viewTemplate) {
        return new TemplatePage(ctx, viewTemplate);
    }

    @Override
    public void render() {
        this.render(viewTemplate);
    }
}
