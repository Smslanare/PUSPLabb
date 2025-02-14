@import gg.jte.Content
@param etsf20.basesystem.web.pages.Page page
@param Content extraLinks = @``

<ul class="nav nav-underline pb-3">
    @template.components.navitem(title="List", href="/notes/", page=page, exact = true)
    @template.components.navitem(title="Create", href="/notes/create", query = "returnPath=/notes/", page=page)
    ${extraLinks}
</ul>