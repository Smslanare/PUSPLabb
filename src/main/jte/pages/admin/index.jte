@param etsf20.basesystem.web.pages.Page page

@template.layout.page(
title = "Administration",
page = page,
content = @`
    <div class="card">
        <h5 class="card-header">Users</h5>
        <div class="card-body">
            <p class="card-text">List, Create, Edit and Delete users.</p>
            <a href="./users/" class="btn btn-primary stretched-link">Manage users</a>
        </div>
    </div>
`)