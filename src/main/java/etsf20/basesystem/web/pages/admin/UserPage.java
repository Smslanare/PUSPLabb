package etsf20.basesystem.web.pages.admin;

import io.javalin.http.Context;
import etsf20.basesystem.domain.models.User;
import etsf20.basesystem.web.pages.Page;

import java.util.List;

public class UserPage extends Page {

    private final List<User> users;

    public UserPage(Context ctx, List<User> users) {
        super(ctx);
        this.users = users;
    }

    public List<User> getUsers() {
        return this.users;
    }

    public void render() {
        this.render("pages/admin/users/list.jte");
    }
}
