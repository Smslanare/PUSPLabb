@import gg.jte.Content
@param etsf20.basesystem.web.pages.Page page
@param Content extraLinks = @``

<div class="container mt-3">
    <div class="d-flex">
        <h5 class="mt-2 me-3">Users</h5>
            <ul class="nav nav-underline pb-3">
                @template.components.navitem(title="List", href="/admin/users/", page=page, exact = true)
                @template.components.navitem(title="Create", href="/admin/users/create", query = "returnPath=/admin/users/", page=page)
                ${extraLinks}
            </ul>
    </div>
</div>