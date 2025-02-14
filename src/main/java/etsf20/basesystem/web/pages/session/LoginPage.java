package etsf20.basesystem.web.pages.session;

import io.javalin.http.Context;
import etsf20.basesystem.web.pages.forms.FormPage;
import etsf20.basesystem.web.pages.forms.StringFormField;
import etsf20.basesystem.web.pages.forms.Validation;

import java.util.List;
import java.util.Map;

public class LoginPage extends FormPage {
    private String username = "";
    private String password = "";
    private String error = "";
    private String returnPath;

    /**
     * Construct login page
     * @param ctx context
     * @param returnPath return path
     */
    public LoginPage(Context ctx, String returnPath) {
        super(ctx);
        this.username = "";
        this.password = "";
        this.returnPath = returnPath;
    }

    /**
     * Construct login page from a POST
     * @param ctx context
     */
    public LoginPage(Context ctx) {
        super(ctx);
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getError() {
        return error;
    }

    @Override
    protected Map<String, List<String>> step() {
        StringFormField username = new StringFormField("username")
                .requiredNonEmpty("username is empty")
                .check(v -> v.length() >= 5, "username too short")
                .check(v -> v.length() < 60, "username too long")
                .check(v -> !v.isEmpty() && ((v.charAt(0) >= 'a' && v.charAt(0) <= 'z') || (v.charAt(0) >= 'A' && v.charAt(0) <= 'Z')), "invalid username");

        StringFormField password = new StringFormField("password")
                .requiredNonEmpty("password is empty", true);

        readField(username);
        readField(password);

        this.returnPath = ctx.formParam("return-path");
        if(this.returnPath == null) {
            this.returnPath = "";
        }
        this.username = username.getTrimmed();
        this.password = password.get();

        return Validation.collectErrors(username, password);
    }

    public String returnPath() {
        return this.returnPath;
    }

    public String username() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    @Override
    public void render() {
        this.render("login.jte");
    }
}
