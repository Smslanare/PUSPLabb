@import etsf20.basesystem.domain.models.UserRole
@import gg.jte.Content
@param etsf20.basesystem.web.pages.Page page
@param Content extraLinks = @``

<ul class="nav nav-underline pb-3">
    @template.components.navitem(title="List", href="/projects/", page=page, exact = true)
    @if(page.session().isLoggedIn() && page.session().userRole() == UserRole.ADMIN)
        @template.components.navitem(title="Create", href="/projects/create", query = "returnPath=/projects/", page=page)
        ${extraLinks}
    @endif
</ul>