package etsf20.basesystem.web.controllers;

import etsf20.basesystem.domain.models.User;
import io.javalin.http.Context;
import io.javalin.http.HandlerType;
import io.javalin.http.NotFoundResponse;
import etsf20.basesystem.domain.models.Project;
import etsf20.basesystem.domain.models.UserRole;
import etsf20.basesystem.domain.repositories.Repositories;
import etsf20.basesystem.web.ValidationException;
import etsf20.basesystem.web.pages.AlertType;
import etsf20.basesystem.web.pages.Session;
import etsf20.basesystem.web.pages.projects.CreateEditProjectPage;
import etsf20.basesystem.web.pages.projects.ListProjectPage;
import etsf20.basesystem.web.pages.projects.ViewProjectPage;
import etsf20.basesystem.web.pages.FormattedString;
import etsf20.basesystem.web.pages.QuestionPage;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static io.javalin.apibuilder.ApiBuilder.*;

public class ProjectController {

	static List<User> users;
	
    public static void list(Context ctx) {
        Repositories repos = Repositories.from(ctx);
        List<Project> projects = repos.projects().list();
        List<Project> myProjects = repos.userProjects().getProjectsForUser(Session.from(ctx).username());
        users = repos.users().list();
        ListProjectPage listProjectsPage = new ListProjectPage(ctx, projects, myProjects);
        listProjectsPage.render();
    }

    public static void create(Context ctx) throws ValidationException {
        CreateEditProjectPage createPage = new CreateEditProjectPage(ctx, "", "", "", users);
        if (ctx.method() == HandlerType.POST) {
            createPage.readForm();
            if (createPage.isFormValid()) {
                Repositories repos = Repositories.from(ctx);
                UUID randomUUId = UUID.randomUUID();

                // Create a new project with the new UUID
                Project newProject = new Project(randomUUId, createPage.getProjectName(), createPage.getDescription());
                repos.projects().create(newProject);

                // Commit to ensure the project is saved in the database
                repos.commit();  // Make sure this commit happens here before adding users

            
                // Redirect with success message
                Controllers.returnPathMessageRedirect(ctx, "Project successfully created", AlertType.SUCCESS);
                return;
            }
        }
        createPage.render();
    }

    public static void view(Context ctx) throws ValidationException {
        Repositories repos = Repositories.from(ctx);
        Optional<Project> project = repos.projects().get(Controllers.readUUID(ctx, "project-uuid"));
        if (project.isEmpty()) {
            throw new NotFoundResponse();
        }

        //var viewPage = new ViewProjectPage(ctx, project.get(), repos.userProjects().getUsersForProject(project.get().getUuid()), repos.userProjects().getUsernameForProject(project.get().getUuid()));
        var viewPage = new ViewProjectPage(ctx, project.get(), repos.userProjects().getUsersForProject(project.get().getUuid()));
        viewPage.render();
    }

    public static void edit(Context ctx) throws ValidationException {
        Repositories repos = Repositories.from(ctx);
        UUID projectUuid = Controllers.readUUID(ctx, "project-uuid");

        Optional<Project> project = repos.projects().get(projectUuid);
        if (project.isEmpty()) {
            throw new NotFoundResponse();
        } else {
            Project entry = project.get();
            CreateEditProjectPage editPage = new CreateEditProjectPage(
                    ctx,
                    entry.getProjectName(),
                    entry.getDescription(),
                    entry.getUuid().toString(),
                    repos.users().list()
            );

            editPage.readForm();

            if (ctx.method() == HandlerType.POST) {
                if (editPage.isFormValid()) {
                    entry.setProjectName(editPage.getProjectName());
                    entry.setDescription(editPage.getDescription());
                    if(editPage.getSelectedUser() != null) {
                    	repos.userProjects().addUserToProject(editPage.getSelectedUser(), projectUuid);
                    }
                    repos.projects().update(entry);
                    repos.commit();

                    Controllers.returnPathMessageRedirect(ctx,
                            "Project successfully updated",
                            AlertType.SUCCESS,
                            "/projects/");
                    return;
                }
            }

            editPage.render();
        }
    }

    public static void delete(Context ctx) throws ValidationException {
        Repositories repos = Repositories.from(ctx);
        UUID projectUuid = Controllers.readUUID(ctx, "project-uuid");

        Optional<Project> project = repos.projects().get(projectUuid);
        if (project.isEmpty()) {
            throw new NotFoundResponse();
        } else {
            if (ctx.method() != HandlerType.POST) {
                QuestionPage.builder()
                        .title("Do you wish to remove project?")
                        .content(new FormattedString()
                                .text("Project titled `")
                                .italic(project.get().getProjectName())
                                .text("`"))
                        .option("Yes", QuestionPage.ButtonStyle.DANGER)
                        .option("No", QuestionPage.ButtonStyle.SUCCESS)
                        .create(ctx)
                        .render();
            } else {
                if (QuestionPage.getChoice(ctx).equals("Yes")) {
                    if (repos.projects().delete(projectUuid)) {
                        repos.commit();
                        Controllers.returnPathMessageRedirect(ctx, "Project successfully deleted", AlertType.SUCCESS);
                        return;
                    }

                    throw new NotFoundResponse();
                }

                Controllers.returnPathMessageRedirect(ctx, "Delete operation canceled", AlertType.INFORMATION, "/projects/");
            }
        }
    }

    public static void removeUserFromProject(Context ctx) throws ValidationException {
        Repositories repos = Repositories.from(ctx);
        UUID projectUuid = Controllers.readUUID(ctx, "project-uuid");
        String username = ctx.pathParam("username");

        if (repos.userProjects().removeUserFromProject(username, projectUuid)) {
            repos.commit();
            Controllers.returnPathMessageRedirect(ctx, "User successfully removed from project", AlertType.SUCCESS);
            return;
        }

        throw new NotFoundResponse();
    }
    
    public static void configure() {
        get("/", ProjectController::list, UserRole.loggedIn());
        get("/create", ProjectController::create, UserRole.loggedIn());
        post("/create", ProjectController::create, UserRole.loggedIn());

        get("/{project-uuid}/", ProjectController::	view, UserRole.loggedIn());

        get("/{project-uuid}/edit", ProjectController::edit, UserRole.loggedIn());
        post("/{project-uuid}/edit", ProjectController::edit, UserRole.loggedIn());

        get("/{project-uuid}/delete", ProjectController::delete, UserRole.loggedIn());
        post("/{project-uuid}/delete", ProjectController::delete, UserRole.loggedIn());
        
        get("/{project-uuid}/remove/{username}", ProjectController::removeUserFromProject, UserRole.loggedIn());
        post("/{project-uuid}/remove/{username}", ProjectController::removeUserFromProject, UserRole.loggedIn());
    }
}