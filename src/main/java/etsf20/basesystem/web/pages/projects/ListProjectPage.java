package etsf20.basesystem.web.pages.projects;

import etsf20.basesystem.domain.models.Project;
import etsf20.basesystem.domain.models.User;
import etsf20.basesystem.web.pages.Page;
import io.javalin.http.Context;

import java.util.List;

public class ListProjectPage extends Page {
    public final List<Project> projects;
    public final List<Project> myProjects;

    public ListProjectPage(Context cfx, List<Project> projects, List<Project> myProjects) {
        super(cfx);
        this.projects = projects;
        this.myProjects = myProjects;
    }

    public List<Project> getProjects() {
        return projects;
    }

    public List<Project> getProjectsForCurrentUser() {
    	return myProjects;
    }
    
    @Override
    public void render() {
        this.render("pages/projects/list.jte");
    }
}