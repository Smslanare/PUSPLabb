package etsf20.basesystem.web.pages.notes;

import io.javalin.http.Context;
import etsf20.basesystem.web.pages.forms.FormPage;
import etsf20.basesystem.web.pages.forms.FormField;
import etsf20.basesystem.web.pages.forms.StringFormField;
import etsf20.basesystem.web.pages.forms.Validation;

import java.util.List;
import java.util.Map;

/**
 * Journal Create/Edit page
 */
public class CreateEditNotePage extends FormPage {

    private String title;
    private String body;
    private final String uuid;

    public CreateEditNotePage(Context ctx, String title, String body, String uuid) {
        super(ctx);
        this.body = body;
        this.title = title;
        this.uuid = uuid;
    }

    public String getUuid() {
        return uuid;
    }

    @Override
    protected Map<String, List<String>> step() {
        FormField<String> title = new StringFormField("title")
                .requiredNonEmpty("required")
                .check(v -> v.length() < 200, "too long");

        FormField<String> body = new StringFormField( "body")
                .check(v -> v.length() <= 64000, "too long");

        readField(title);
        readField(body);

        this.title = title.get();
        this.body = body.get();

        return Validation.collectErrors(title, body);
    }

    public String getTitle() {
        return this.title;
    }

    public String getBody() {
        return body;
    }

    public void render() {
        this.render("pages/notes/upsert.jte");
    }
}
