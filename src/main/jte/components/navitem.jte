@import etsf20.basesystem.web.pages.Page

@param Page page
@param String title
@param String href
@param String query = ""
@param boolean exact = false
!{
    String path = page.path();
    boolean active;
    if(href.equals("/")) {
        active = page.path().equals(href);
    } else {
        if(exact) {
            active = page.path().equals(href);
        } else {
            active = page.path().startsWith(href);
        }
    }

    if(!query.isEmpty()) {
        query = "?" + query;
    }
}
<li class="nav-item">
    @if(active)
        <a href="${href}${query}" class="nav-link active" aria-current="page">${title}</a>
    @else
        <a href="${href}${query}" class="nav-link">${title}</a>
    @endif
</li>