package etsf20.basesystem.web.controllers;

import io.javalin.http.Context;
import io.javalin.http.HandlerType;
import etsf20.basesystem.domain.models.User;
import etsf20.basesystem.domain.models.UserRole;
import etsf20.basesystem.domain.repositories.Repositories;
import etsf20.basesystem.web.ValidationException;
import etsf20.basesystem.web.pages.AlertType;
import etsf20.basesystem.web.pages.Session;
import etsf20.basesystem.web.pages.users.SettingPage;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.post;

/**
 * Request handlers for user settings
 */
public class UserController {

    /**
     * GET,POST / - Update password page
     */
    public static void update(Context ctx) throws ValidationException {
        Session session = Session.from(ctx);

        Repositories repos = Repositories.from(ctx);
        User user = repos.users().get(session.username()).orElseThrow();

        SettingPage userSettingPage = new SettingPage(ctx, user);
        userSettingPage.readForm();
        if(ctx.method() == HandlerType.POST) {
            if(userSettingPage.isFormValid()) {
                // user is updated by the page
                repos.users().update(user);
                repos.commit();
                Controllers.returnPathMessageRedirect(ctx, "Password successfully updated", AlertType.SUCCESS, "/user/settings");
                return;
            }
        }

        userSettingPage.render();
    }

    /**
     * Configure handlers for user pages, called from {@code Main.configure}
     */
    public static void configure() {
        get("/settings", UserController::update, UserRole.loggedIn());
        post("/settings", UserController::update, UserRole.loggedIn());
    }
}
