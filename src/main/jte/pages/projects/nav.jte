@import gg.jte.Content
@param etsf20.basesystem.web.pages.Page page
@param Content extraLinks = @``

<ul class="nav nav-underline pb-3">
    @template.components.navitem(title="List", href="/projects/", page=page, exact = true)
    @template.components.navitem(title="Create", href="/projects/create", query = "returnPath=/projects/", page=page)
    ${extraLinks}
</ul>