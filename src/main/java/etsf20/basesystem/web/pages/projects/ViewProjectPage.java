package etsf20.basesystem.web.pages.projects;

import etsf20.basesystem.domain.models.Project;
import etsf20.basesystem.web.pages.Page;

import io.javalin.http.Context;

public class ViewProjectPage extends Page {
    private final Project project;

    public ViewProjectPage(Context ctx, Project project) {
        super(ctx);
        this.project = project;
    }

    public Project project() {
        return this.project;
    }

    public void render() {
        this.render("pages/projects/view.jte");
    }
}
