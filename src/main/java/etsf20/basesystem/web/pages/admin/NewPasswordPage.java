package etsf20.basesystem.web.pages.admin;

import io.javalin.http.Context;
import etsf20.basesystem.web.pages.Page;

public class NewPasswordPage extends Page {

    private final String message;
    private final String newPassword;

    public NewPasswordPage(Context ctx, String message, String newPassword) {
        super(ctx);
        this.message = message;
        this.newPassword = newPassword;
    }

    public String getMessage() {
        return message;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void render() {
        this.render("pages/admin/users/new-password.jte");
    }
}
