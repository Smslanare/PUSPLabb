package etsf20.basesystem.web.pages.projects;

import etsf20.basesystem.domain.models.Project;
import etsf20.basesystem.domain.models.User;
import etsf20.basesystem.web.pages.Page;

import io.javalin.http.Context;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ViewProjectPage extends Page {
    private final Project project;
    private final List<String> userProjects;

    public ViewProjectPage(Context ctx, Project project, List<String> userProjects) {
        super(ctx);
        this.project = project;
        this.userProjects = userProjects;
    }

    public Project project() {
        return this.project;
    }

    public List<String> getUserProjects() {
        return userProjects;
    }

    public void render() {
        this.render("pages/projects/view.jte");
    }
}
