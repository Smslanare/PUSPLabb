package etsf20.basesystem.web.controllers.admin;

import io.javalin.http.Context;
import io.javalin.http.HandlerType;
import io.javalin.http.InternalServerErrorResponse;
import io.javalin.http.NotFoundResponse;
import org.slf4j.LoggerFactory;
import etsf20.basesystem.domain.models.User;
import etsf20.basesystem.domain.repositories.Repositories;
import etsf20.basesystem.domain.models.UserRole;
import etsf20.basesystem.persistance.DatabaseValidationException;
import etsf20.basesystem.web.ValidationException;
import etsf20.basesystem.web.controllers.Controllers;
import etsf20.basesystem.web.pages.Alert;
import etsf20.basesystem.web.pages.AlertType;
import etsf20.basesystem.web.pages.Session;
import etsf20.basesystem.web.pages.admin.CreateUserPage;
import etsf20.basesystem.web.pages.admin.NewPasswordPage;
import etsf20.basesystem.web.pages.admin.UserPage;
import etsf20.basesystem.web.pages.FormattedString;
import etsf20.basesystem.web.pages.QuestionPage;

import java.security.SecureRandom;
import java.util.Optional;

import static io.javalin.apibuilder.ApiBuilder.*;

/**
 * User administration controller
 */
public class AdminUserController {

    /**
     * GET /admin/users/
     */
    public static void index(Context ctx) {
        Repositories repos = Repositories.from(ctx);

        UserPage userPage = new UserPage(ctx, repos.users().list());
        userPage.render();
    }

    /**
     * GET,POST /admin/users/create
     */
    public static void create(Context ctx) throws ValidationException {
        if(Controllers.addStateReferenceIfMissing(ctx)) {
            return;
        }

        CreateUserPage createUserPage = new CreateUserPage(ctx);
        createUserPage.readForm();

        if(createUserPage.shouldCreateNewUser()) {
            CreateUserPage.State state = createUserPage.state();

            String newPassword = generateNewRandomPassword(10);
            User newUser = User.createWithPassword(state.username(), state.displayName(), newPassword, state.userRole());
            var repos = Repositories.from(ctx);
            try {
                repos.users().create(newUser);
            } catch(DatabaseValidationException e) {
                // In this case, the username is the only thing that should cause a validation exception
                LoggerFactory.getLogger(AdminUserController.class).error("Failed to create user", e);

                createUserPage.setAlert(new Alert("User with same name already exists", AlertType.ERROR, true));
                createUserPage.setState(new CreateUserPage.State(0, state.username(), state.displayName(), state.userRole()));
                createUserPage.render();
                return;
            }
            repos.commit();

            createUserPage.session().invalidate(createUserPage.stateReference()); // removes data from server

            var newPasswordPage = new NewPasswordPage(ctx, "User " + newUser.getUsername() + " created.", newPassword);
            newPasswordPage.render();
        } else {
            createUserPage.render();
        }
    }

    /**
     * Generate a new random password
     *
     * @param length number of characters long
     */
    private static String generateNewRandomPassword(int length) {
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVXYZabcdefghijklmnopqrstuvxyz0123456789";
        SecureRandom sr = new SecureRandom();
        StringBuilder newPassword = new StringBuilder();
        for(int i = 0; i < length; i++) {
            newPassword.append(alphabet.charAt(sr.nextInt(alphabet.length())));
        }

        return newPassword.toString();
    }

    /**
     * GET,POST /admin/users/{username}/reset-password
     */
    public static void resetPassword(Context ctx) throws ValidationException {
        Repositories repos = Repositories.from(ctx);
        String username = ctx.pathParam("username");

        // Do not allow you to reset your own password
        if(username.equals(Session.from(ctx).username())) {
            throw new ValidationException("Cannot regenerate password for logged in user", "Trying to update password for user: " + username);
        }

        // Make sure that the user exists
        Optional<User> dbUser = repos.users().get(username);
        if(dbUser.isEmpty()) {
            // Consistency issue - active session with removed user
            throw new InternalServerErrorResponse("logged in user has been removed");
        }

        if(ctx.method() != HandlerType.POST) {
            QuestionPage.builder()
                    .title("Do you wish to reset password?")
                    .content(new FormattedString().bold("User: ").monospaced(username))
                    .option("Yes", QuestionPage.ButtonStyle.DANGER)
                    .option("No", QuestionPage.ButtonStyle.SUCCESS)
                    .create(ctx)
                    .render();
        } else {
            String choice = QuestionPage.getChoice(ctx);

            if(choice.equals("Yes")) {
                User user = dbUser.get();

                String newPassword = generateNewRandomPassword(10);
                user.setPassword(newPassword);

                repos.users().update(user);
                repos.commit();

                var newPasswordPage = new NewPasswordPage(ctx, "Password reset for user: " + username, newPassword);
                newPasswordPage.render();
            } else {
                Controllers.returnPathMessageRedirect(ctx, "Password reset operation cancelled.", AlertType.INFORMATION);
            }
        }
    }

    /**
     * GET,POST /admin/users/{username}/delete
    */
    public static void delete(Context ctx) throws ValidationException {
        Repositories repos = Repositories.from(ctx);
        String username = ctx.pathParam("username");

        if(username.equals(Session.from(ctx).username())) {
            throw new ValidationException("Cannot remove logged in user", "Trying to remove user: " + username);
        }

        Optional<User> user = repos.users().get(username);
        if(user.isEmpty()) {
            throw new NotFoundResponse("User not found");
        }

        if(ctx.method() != HandlerType.POST) {
            QuestionPage.builder()
                    .title("Do you wish to remove user?")
                    .content(new FormattedString().bold("User: ").monospaced(username))
                    .option("No", QuestionPage.ButtonStyle.SUCCESS)
                    .option("Yes", QuestionPage.ButtonStyle.DANGER)
                    .create(ctx)
                    .render();
        }  else {
            String choice = QuestionPage.getChoice(ctx);
            if(choice.equals("Yes")) {
                if(!repos.users().delete(username)) {
                    throw new NotFoundResponse();
                }

                repos.commit();
                Controllers.returnPathMessageRedirect(ctx, "User " + username + " successfully removed.", AlertType.SUCCESS);
                return;
            }

            Controllers.returnPathMessageRedirect(ctx, "Delete operation cancelled.", AlertType.INFORMATION);
        }
    }

    public static void configure() {
        get("/", AdminUserController::index, UserRole.ADMIN);

        get("/create", AdminUserController::create, UserRole.ADMIN);
        post("/create", AdminUserController::create, UserRole.ADMIN);

        get("/{username}/reset-password", AdminUserController::resetPassword, UserRole.ADMIN);
        post("/{username}/reset-password", AdminUserController::resetPassword, UserRole.ADMIN);

        get("/{username}/delete", AdminUserController::delete, UserRole.ADMIN);
        post("/{username}/delete", AdminUserController::delete, UserRole.ADMIN);
    }
}
