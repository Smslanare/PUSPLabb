    @import gg.jte.Content
@import etsf20.basesystem.web.pages.Page
@param String title = ""
@param Page page
@param Content content

<!doctype html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>@if(!title.isEmpty())
            ${title}
        @else
            Journal System
        @endif
    </title>
    <link href="/webjars/bootstrap/5.3.3/css/bootstrap.min.css" rel="stylesheet" />
</head>
<body>
<div class="col-lg-8 mx-auto p-4 py-md-5">
    <header class="d-flex flex-wrap justify-content-center py-3 mb-4 px-3 border bg-light" style="margin-left: -1rem; margin-right: -1rem;">
        <a href="/" class="d-flex align-items-center mb-2 mb-lg-0 link-body-emphasis text-decoration-none me-3">
            <span class="fs-4">Journal System</span>
        </a>
        <ul class="nav nav-pills col-12 col-lg-auto me-lg-auto mb-2 justify-content-center mb-md-0">
        </ul>
        <div class="col-md-3 text-end">
            @if(page.session().isLoggedIn())
                <a class="fw-bold text-decoration-none text-body" style="margin-right:0.5em" href="/user/settings">${page.session().displayName()}</a>
                <a class="btn btn-outline-primary me-2" href="/session/logout">Logout</a>
            @else
                <a class="btn btn-outline-primary me-2" href="/session/login">Login</a>
            @endif
        </div>
    </header>

    <main>
        ${content}
    </main>

    <footer class="pt-5 my-5 text-body-secondary border-top">
        (c) 2025 ETSF20 Authors
    </footer>
</div>
<script src="/webjars/bootstrap/5.3.3/js/bootstrap.bundle.min.js"></script>
</body>
</html>