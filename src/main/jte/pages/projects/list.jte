@import java.time.ZoneId
@import java.time.format.DateTimeFormatter
@import java.time.format.FormatStyle
@import java.util.Locale
@param etsf20.basesystem.web.pages.projects.ListProjectPage page

@template.layout.page(
title = "Projects",
page = page,
content = @`
    @template.pages.projects.nav(page = page)
    !{
    DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL)
                                                   .withLocale(Locale.getDefault())
                                                   .withZone(ZoneId.systemDefault());
    }
    @for(var project : page.getProjects())
        <a class="nav-link pb-3" href="/projects/${project.getUuid().toString()}/">
            <h3>${project.getProjectName()}</h3>
            <p class="text-muted"><em>${project.getDescription()}</em></p>
        </a>
    @endfor
`)