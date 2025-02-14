package etsf20.basesystem.web.controllers;

import io.javalin.http.Context;
import etsf20.basesystem.domain.models.UserRole;
import etsf20.basesystem.web.controllers.admin.AdminUserController;
import etsf20.basesystem.web.pages.TemplatePage;

import static io.javalin.apibuilder.ApiBuilder.*;

/**
 * Request handlers for administration pages
 */
public class AdminController {

    /**
     * GET /admin/
     */
    public static void index(Context ctx) {
        TemplatePage.from(ctx, "pages/admin/index.jte").render();
    }

    /**
     * Configure handlers for administration pages, called from {@code Main.configure}
     */
    public static void configure() {
        get("/", AdminController::index, UserRole.ADMIN);
        path("/users/", AdminUserController::configure);
    }
}
