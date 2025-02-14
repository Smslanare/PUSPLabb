package etsf20.basesystem.web.pages.notes;

import io.javalin.http.Context;
import etsf20.basesystem.domain.models.Note;
import etsf20.basesystem.web.pages.Page;

import java.util.List;

public class ListNotesPage extends Page {
    public final List<Note> notes;

    public ListNotesPage(Context ctx, List<Note> notes) {
        super(ctx);
        this.notes = notes;
    }

    public List<Note> getNotes() {
        return notes;
    }

    @Override
    public void render() {
        this.render("pages/notes/list.jte");
    }
}
