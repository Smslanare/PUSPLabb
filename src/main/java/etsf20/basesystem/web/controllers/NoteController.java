package etsf20.basesystem.web.controllers;

import io.javalin.http.Context;
import io.javalin.http.HandlerType;
import io.javalin.http.NotFoundResponse;
import etsf20.basesystem.domain.models.Note;
import etsf20.basesystem.domain.models.UserRole;
import etsf20.basesystem.domain.repositories.Repositories;
import etsf20.basesystem.web.ValidationException;
import etsf20.basesystem.web.pages.AlertType;
import etsf20.basesystem.web.pages.Session;
import etsf20.basesystem.web.pages.notes.CreateEditNotePage;
import etsf20.basesystem.web.pages.notes.ListNotesPage;
import etsf20.basesystem.web.pages.notes.ViewNotePage;
import etsf20.basesystem.web.pages.FormattedString;
import etsf20.basesystem.web.pages.QuestionPage;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static io.javalin.apibuilder.ApiBuilder.*;

/**
 * Request handlers for notes
 */
public class NoteController {

    /**
     * GET /journal/
     */
    public static void list(Context ctx) {
        Repositories repos = Repositories.from(ctx);
        List<Note> notes = repos.notes().list(Session.from(ctx).username(), -1, 0, false);
        ListNotesPage listNotesPage = new ListNotesPage(ctx, notes);
        listNotesPage.render();
    }

    /**
     * GET,POST /journal/create
     */
    public static void create(Context ctx) throws ValidationException {
        CreateEditNotePage createPage = new CreateEditNotePage(ctx, "", "", "");
        if(ctx.method() == HandlerType.POST) {
            createPage.readForm();
            // user submitted
            if(createPage.isFormValid()) {
                Repositories repos = Repositories.from(ctx);
                repos.notes().create(new Note(createPage.getTitle(), createPage.getBody(), Session.from(ctx).username()));
                repos.commit();

                Controllers.returnPathMessageRedirect(ctx, "Journal note successfully created", AlertType.SUCCESS);
                return;
            }
        }

        createPage.render();
    }

    /**
     * GET /journal/{journal-uuid}/
     * @throws ValidationException if uuid has invalid format
     */
    public static void view(Context ctx) throws ValidationException {

        Repositories repos = Repositories.from(ctx);
        Optional<Note> note = repos.notes().get(Controllers.readUUID(ctx, "note-uuid"));
        if(note.isEmpty()) {
            throw new NotFoundResponse();
        }

        var viewPage = new ViewNotePage(ctx, note.get());
        viewPage.render();
    }

    /**
     * GET,POST /notes/{note-uuid}/edit
     * @throws ValidationException if uuid has invalid format or form has been incorrectly posted
     */
    public static void edit(Context ctx) throws ValidationException {
        Repositories repos = Repositories.from(ctx);
        UUID noteUuid = Controllers.readUUID(ctx, "note-uuid");

        Optional<Note> note = repos.notes().get(noteUuid);
        if(note.isEmpty()) {
            throw new NotFoundResponse();
        } else {
            Note entry = note.get();
            CreateEditNotePage editPage = new CreateEditNotePage(
                    ctx,
                    entry.getTitle(),
                    entry.getBody(),
                    entry.getUuid().toString()
            );

            editPage.readForm();

            if (ctx.method() == HandlerType.POST) {
                // user submitted
                if (editPage.isFormValid()) {
                    entry.setTitle(editPage.getTitle());
                    entry.setBody(editPage.getBody());
                    repos.notes().update(entry);
                    repos.commit();

                    Controllers.returnPathMessageRedirect(ctx,
                            "Journal note successfully updated",
                            AlertType.SUCCESS,
                            "/notes/");
                    return;
                }
            }

            editPage.render();
        }
    }

    /**
     * GET,POST /{note-uuid}/delete
     * @throws ValidationException if uuid has invalid format
     */
    public static void delete(Context ctx) throws ValidationException {
        Repositories repos = Repositories.from(ctx);
        UUID noteUuid = Controllers.readUUID(ctx, "note-uuid");

        Optional<Note> note = repos.notes().get(noteUuid);
        if(note.isEmpty()) {
            throw new NotFoundResponse();
        } else {
            if(!note.get().getUserName().equals(Session.from(ctx).username())) {
                // Another user that is not the author requested to delete a note - only if this user is ADMIN is this allowed
                if(Session.from(ctx).userRole() != UserRole.ADMIN) {
                    throw new NotFoundResponse();
                }
            }

            if(ctx.method() != HandlerType.POST) {
                QuestionPage.builder()
                        .title("Do you wish to remove journal note?")
                        .content(new FormattedString()
                                .text("Note titled `")
                                .italic(note.get().getTitle())
                                .text("`"))
                        .option("Yes", QuestionPage.ButtonStyle.DANGER)
                        .option("No", QuestionPage.ButtonStyle.SUCCESS)
                        .create(ctx)
                        .render();
            }  else {
                if(QuestionPage.getChoice(ctx).equals("Yes")) {
                    if(repos.notes().delete(Session.from(ctx).username(), noteUuid)) {
                        // success
                        repos.commit();
                        Controllers.returnPathMessageRedirect(ctx, "Note successfully deleted", AlertType.SUCCESS);
                        return;
                    }

                    throw new NotFoundResponse();
                }

                Controllers.returnPathMessageRedirect(ctx, "Delete operation canceled", AlertType.INFORMATION, "/notes/");
            }
        }
    }

    /**
     * Configure handlers for note pages, called from {@code Main.configure}
     */
    public static void configure() {
        get("/", NoteController::list, UserRole.loggedIn());
        get("/create", NoteController::create, UserRole.loggedIn());
        post("/create", NoteController::create, UserRole.loggedIn());

        get("/{note-uuid}/", NoteController::view, UserRole.loggedIn());

        get("/{note-uuid}/edit", NoteController::edit, UserRole.loggedIn());
        post("/{note-uuid}/edit", NoteController::edit, UserRole.loggedIn());

        get("/{note-uuid}/delete", NoteController::delete, UserRole.loggedIn());
        post("/{note-uuid}/delete", NoteController::delete, UserRole.loggedIn());
    }
}
