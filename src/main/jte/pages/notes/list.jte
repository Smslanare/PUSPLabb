@import java.time.ZoneId
@import java.time.format.DateTimeFormatter
@import java.time.format.FormatStyle
@import java.util.Locale
@param etsf20.basesystem.web.pages.notes.ListNotesPage page

@template.layout.page(
title = "Journal",
page = page,
content = @`
    @template.pages.notes.nav(page = page)
    !{
    DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL)
                                                   .withLocale(Locale.getDefault())
                                                   .withZone(ZoneId.systemDefault());
    }
    @for(var note : page.getNotes())
        <a class="nav-link pb-3" href="/notes/${note.getUuid().toString()}/">
            <h3>${note.getTitle()}</h3>
            <p class="text-muted"><em>${note.getDisplayName()}</em> @ <small>${formatter.format(note.getTimestamp())}</small></p>
        </a>
    @endfor
`)