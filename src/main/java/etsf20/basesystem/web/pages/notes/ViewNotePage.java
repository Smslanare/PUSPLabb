package etsf20.basesystem.web.pages.notes;

import io.javalin.http.Context;
import etsf20.basesystem.domain.models.Note;
import etsf20.basesystem.web.pages.Page;

public class ViewNotePage extends Page {
    private final Note note;

    public ViewNotePage(Context ctx, Note note) {
        super(ctx);
        this.note = note;
    }

    public Note note() {
        return this.note;
    }

    public void render() {
        this.render("pages/notes/view.jte");
    }
}
