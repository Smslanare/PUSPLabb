package etsf20.basesystem.web.pages.projects;

import etsf20.basesystem.domain.models.Project;
import etsf20.basesystem.web.pages.Page;
import io.javalin.http.Context;

import java.util.List;

public class ListProjectPage extends Page {
    public final List<Project> projects;

    public ListProjectPage(Context cfx, List<Project> projects) {
        super(cfx);
        this.projects = projects;
    }

    public List<Project> getProjects() {
        return projects;
    }

    @Override
    public void render() {
        this.render("pages/projects/list.jte");
    }
}
