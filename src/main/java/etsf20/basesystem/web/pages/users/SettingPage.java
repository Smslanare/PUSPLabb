package etsf20.basesystem.web.pages.users;

import etsf20.basesystem.web.controllers.UserController;
import io.javalin.http.Context;
import etsf20.basesystem.domain.models.User;
import etsf20.basesystem.security.Argon2PasswordHash;
import etsf20.basesystem.web.pages.forms.FormPage;
import etsf20.basesystem.web.pages.forms.StringFormField;
import etsf20.basesystem.web.pages.forms.Validation;

import java.util.List;
import java.util.Map;

/**
 * Settings page
 * @see UserController
 */
public class SettingPage extends FormPage {

    private final User user;

    public SettingPage(Context ctx, User user) {
        super(ctx);
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    @Override
    protected Map<String, List<String>> step() {
        StringFormField oldPassword = new StringFormField( "old-password")
                .requiredNonEmpty("required")
                .check(v -> Argon2PasswordHash.verify(user.getPasswordHash(), v), "incorrect password");

        StringFormField newPassword = new StringFormField( "new-password")
                .requiredNonEmpty("required")
                .checkLength(10, 200, "must be between 10 and 200 characters");

        StringFormField newRepeatPassword = new StringFormField( "new-repeat-password")
                .requiredNonEmpty("required")
                .check(v -> newPassword.get().equals(v), "repeated password must match");

        readField(oldPassword);
        readField(newPassword);
        readField(newRepeatPassword);

        Map<String, List<String>> errors = Validation.collectErrors(oldPassword, newPassword, newRepeatPassword);
        if(errors.isEmpty()) {
            user.setPassword(newPassword.get());
        }
        return errors;
    }

    @Override
    public void render() {
        this.render("pages/user-settings.jte");
    }
}
