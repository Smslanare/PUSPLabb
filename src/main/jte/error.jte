@param etsf20.basesystem.web.pages.ErrorPage page

@if(page.session().isLoggedIn())
    @template.layout.page(
    title = page.getTitle(),
    page = page,
    content = @`
        <h1 class="text-body-emphasis">${page.getTitle()}</h1>
        <p class="fs-5 col-md-12">${page.getDescription()}</p>
    `)
@else
    @template.layout.barepage(
    title = page.getTitle(),
    page = page,
    content = @`
        <h1 class="text-body-emphasis">${page.getTitle()}</h1>
        <p class="fs-5 col-md-12">${page.getDescription()}</p>
    `)
@endif