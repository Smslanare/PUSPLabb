package etsf20.basesystem.web.pages.projects;

import etsf20.basesystem.domain.models.User;
import io.javalin.http.Context;
import etsf20.basesystem.web.pages.forms.FormPage;
import etsf20.basesystem.web.pages.forms.FormField;
import etsf20.basesystem.web.pages.forms.StringFormField;
import etsf20.basesystem.web.pages.forms.Validation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CreateEditProjectPage extends FormPage {

    private String projectName;
    private String description;
    private final String uuid;
    private List<User> users;
    private String selectedUser = null;

    public CreateEditProjectPage(Context ctx, String projectName, String description, String uuid, List<User> users) {
        super(ctx);
        this.projectName = projectName;
        this.description = description;
        this.uuid = uuid;
        this.users = users;
    }

    public String getUuid() {
        return uuid;
    }

    @Override
    protected Map<String, List<String>> step() {
        FormField<String> projectNameField = new StringFormField("projectName")
                .requiredNonEmpty("required")
                .check(v -> v.length() < 200, "too long");

        FormField<String> descriptionField = new StringFormField("description")
                .check(v -> v.length() <= 64000, "too long");

        FormField<String> userField = new StringFormField("user");

        readField(projectNameField);
        readField(descriptionField);
        readField(userField);

        this.projectName = projectNameField.get();
        this.description = descriptionField.get();
        this.selectedUser = userField.get();

        System.out.println("Selected user: " + selectedUser);

        return Validation.collectErrors(projectNameField, descriptionField);
    }

    public String getProjectName() {
        return this.projectName;
    }

    public String getDescription() {
        return this.description;
    }

    public String getSelectedUser() {
        return this.selectedUser;
    }


    public List<User> getUsers() {
        return users != null ? users : new ArrayList<User>() {
        };
    }

    public boolean isSelectedUser(String username) {
        return false;
    }

    public void render() {
        this.render("pages/projects/upsert.jte");
    }
}