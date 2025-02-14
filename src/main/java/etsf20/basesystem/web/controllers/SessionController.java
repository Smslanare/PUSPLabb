package etsf20.basesystem.web.controllers;

import io.javalin.http.Context;
import io.javalin.http.HandlerType;
import io.javalin.http.HttpStatus;
import etsf20.basesystem.domain.models.UserRole;
import etsf20.basesystem.domain.models.User;
import etsf20.basesystem.domain.repositories.Repositories;
import etsf20.basesystem.domain.repositories.UserRepository;
import etsf20.basesystem.web.pages.session.LoginPage;
import etsf20.basesystem.web.pages.Session;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

import static io.javalin.apibuilder.ApiBuilder.*;

/**
 * Request handlers for session management such as Login, Logout
 */
public class SessionController {

    /**
     * GET,POST /session/login
     */
    public static void login(Context ctx) {
        Repositories repos = Repositories.from(ctx);
        UserRepository userRepo = repos.users();
        LoginPage loginPage = new LoginPage(ctx, "");

        if(ctx.method() == HandlerType.POST) {
            loginPage.readForm();
            if(loginPage.isFormValid()) {
                Optional<User> dbUser = userRepo.get(loginPage.username());
                if(dbUser.isEmpty()) {
                    loginPage.setError("invalid username or password");
                    loginPage.render();
                    return;
                }

                User user = dbUser.get();
                if(user.verifyPassword(loginPage.getPassword())) {
                    //Login successful
                    Session session = Session.from(ctx);
                    session.login(loginPage.username(), user.getDisplayName(), user.getRole());
                    if(!loginPage.returnPath().isEmpty()) {
                        ctx.redirect(URLDecoder.decode(loginPage.returnPath(), StandardCharsets.UTF_8));
                    }  else {
                        System.out.println("Success: " + ctx.cookieMap());
                        ctx.redirect("/");
                    }

                    return;
                }

                // This signals to the browser that the attempt was unsuccessful
                ctx.status(HttpStatus.UNAUTHORIZED);
                loginPage.setError("invalid username or password");
                loginPage.render();
                return;
            }
        }

        loginPage.render();
    }

    /**
     * GET /session/logout - logout
     */
    public static void logout(Context ctx) {
        Session.from(ctx).logout();
        ctx.redirect("/");
    }

    /**
     * GET /session/debug
     */
    public static void debug(Context ctx) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> entry : ctx.sessionAttributeMap().entrySet()) {
            sb.append(entry.getKey()).append("=").append(entry.getValue().toString()).append("\n");
        }

        ctx.result(sb.toString());
    }

    /**
     * Configure handlers for session pages, called from {@code Main.configure}
     */
    public static void configure() {
        // Login handlers
        get("/login", SessionController::login, UserRole.values());
        post("/login", SessionController::login, UserRole.values());

        // Logout handlers
        get("/logout", SessionController::logout, UserRole.loggedIn());

        // Debug handler
        get("/debug", SessionController::debug, UserRole.values());
    }
}
