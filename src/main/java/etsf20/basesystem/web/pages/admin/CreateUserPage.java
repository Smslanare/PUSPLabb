package etsf20.basesystem.web.pages.admin;

import io.javalin.http.Context;
import etsf20.basesystem.domain.models.UserRole;
import etsf20.basesystem.web.pages.forms.FormPage;
import etsf20.basesystem.web.pages.forms.StringFormField;
import etsf20.basesystem.web.pages.forms.Validation;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CreateUserPage extends FormPage {

    /**
     * Represents the state for this page, stored between page loads
     * @param step        current step
     * @param username    entered username
     * @param displayName entered name to display
     * @param userRole    entered user role
     */
    public record State(
            int step,
            String username,
            String displayName,
            UserRole userRole) implements Serializable {

        public State() {
            this(0, "", "", UserRole.USER);
        }
    }

    public CreateUserPage(Context ctx) {
        super(ctx);
    }

    public State state() {
        return state(State.class, State::new);
    }

    public boolean shouldCreateNewUser() {
        return state().step() == 2;
    }

    /**
     * Validate form and set internal state
     *
     * @see FormPage#readForm()
     * @return errors as a map that maps field name to list of errors
     */
    @Override
    protected Map<String, List<String>> step() {
        State state = this.state();

        if(state.step == 0) {
            // Read form and validate
            StringFormField username = new StringFormField("username")
                    .requiredNonEmpty("required")
                    .checkLength(5,60, "must be between 5 and 60 characters")
                    .check(v -> v.matches("[a-zA-Z][a-zA-Z0-9_\\-]{4,59}"), "must start with a letter and valid characters are letters, numbers, dash, or underscore");

            StringFormField displayName = new StringFormField("displayName")
                    .requiredNonEmpty("required")
                    .checkLength(1,120, "must be between 1 and 120 characters long");

            readField(username);
            readField(displayName);
            UserRole role = UserRole.valueOf(readField("userRole"));

            Map<String, List<String>> errors = Validation.collectErrors(username, displayName);
            setState(new State(errors.isEmpty() ? 1 : 0, username.getTrimmed(), displayName.getTrimmed(), role));
            return errors;
        }

        if(state.step == 1) {
            // We have to register the fields so that if they are required they are known
            registerField("username");
            registerField("displayName");
            registerField("userRole");

            // Check if information is correct
            String action = ctx.formParam("action");
            if(action != null && action.equals("go-back")) {
                setState(new State(0, state.username, state.displayName, state.userRole));
            }
            if(action != null && action.equals("create")) {
                setState(new State(2, state.username, state.displayName, state.userRole));
            }
        }

        return Collections.emptyMap();
    }

    public void render() {
        this.render("pages/admin/users/create.jte");
    }
}
