@import etsf20.basesystem.domain.models.UserRole
@import etsf20.basesystem.web.pages.Page
@param Page page
<ul class="nav nav-pills col-12 col-lg-auto me-lg-auto mb-2 justify-content-center mb-md-0">
    @template.components.navitem(title="Start", href="/", page=page)
    @if(page.session().isLoggedIn())
        @template.components.navitem(title="Notes", href="/notes/", page=page)
    @endif
    @if(page.session().isLoggedIn() && page.session().userRole() == UserRole.ADMIN)
        @template.components.navitem(title="Administration", href="/admin/", page=page)
    @endif
    @if(page.session().isLoggedIn() && page.session().userRole() == UserRole.ADMIN)
        @template.components.navitem(title="Projects", href="/projects/", page=page)
    @endif
</ul>