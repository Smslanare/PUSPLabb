package etsf20.basesystem.web.pages.projects;

import io.javalin.http.Context;
import etsf20.basesystem.web.pages.forms.FormPage;
import etsf20.basesystem.web.pages.forms.FormField;
import etsf20.basesystem.web.pages.forms.StringFormField;
import etsf20.basesystem.web.pages.forms.Validation;

import java.util.List;
import java.util.Map;

public class CreateEditProjectPage extends FormPage {

    private String projectName;
    private String description;
    private final String uuid;

    public CreateEditProjectPage(Context ctx, String projectName, String description, String uuid) {
        super(ctx);
        this.projectName = projectName;
        this.description = description;
        this.uuid = uuid;
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

        readField(projectNameField);
        readField(descriptionField);

        this.projectName = projectNameField.get();
        this.description = descriptionField.get();

        return Validation.collectErrors(projectNameField, descriptionField);
    }

    public String getProjectName() {
        return this.projectName;
    }

    public String getDescription() {
        return this.description;
    }

    public void render() {
        this.render("pages/projects/upsert.jte");
    }
}