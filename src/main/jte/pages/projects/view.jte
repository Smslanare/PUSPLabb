@import java.time.ZoneId
@import java.time.format.DateTimeFormatter
@import java.time.format.FormatStyle
@import java.util.Locale
@param etsf20.basesystem.web.pages.projects.ViewProjectPage page

@template.layout.page(
title = page.project().getProjectName(),
page = page,
content = @`
    @template.pages.projects.nav(page = page, extraLinks = @`
        @template.components.navitem(title="View", href="/projects/" + page.project().getUuid().toString() + "/", exact = true, page=page)
        @template.components.navitem(title="Edit", href="/projects/" + page.project().getUuid().toString() + "/edit?returnPath=" + page.path(), exact = true, page=page)
    `)
    !{
    DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL)
                                                   .withLocale(Locale.getDefault())
                                                   .withZone(ZoneId.systemDefault());
    }
    <h1>${page.project().getProjectName()}</h1>
    <p class="text-muted"><cite title="Source Title">${page.project().getProjectName()}</cite></p>
    <p>${page.project().getDescription()}</p>

    <a href="/projects/${page.project().getUuid().toString()}/delete?returnPath=/projects/">Delete</a>
`)