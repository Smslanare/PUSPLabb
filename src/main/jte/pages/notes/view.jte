@import java.time.ZoneId
@import java.time.format.DateTimeFormatter
@import java.time.format.FormatStyle
@import java.util.Locale
@param etsf20.basesystem.web.pages.notes.ViewNotePage page

@template.layout.page(
title = "",
page = page,
content = @`
    @template.pages.notes.nav(page = page, extraLinks = @`
        @template.components.navitem(title="View", href="/notes/" + page.note().getUuid().toString() + "/", exact = true, page=page)
        @template.components.navitem(title="Edit", href="/notes/" + page.note().getUuid().toString() + "/edit?returnPath=" + page.path(), exact = true, page=page)
    `)
    !{
    DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL)
                                                   .withLocale(Locale.getDefault())
                                                   .withZone(ZoneId.systemDefault());
    }
    <h1>${page.note().getTitle()}</h1>
        <p class="text-muted"><cite title="Source Title">${page.note().getDisplayName()}</cite> @ ${formatter.format(page.note().getTimestamp())}</p>
        <p>${page.note().getBody()}</p>

        <a href="/notes/${page.note().getUuid().toString()}/delete?returnPath=/notes/">Delete</a>
`)