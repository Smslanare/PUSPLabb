@param etsf20.basesystem.web.pages.notes.CreateEditNotePage page

@template.layout.page(
title = page.getUuid().isEmpty() ? "Create Note" : "Edit Note",
page = page,
content = @`
    @template.pages.notes.nav(page = page, extraLinks = @`
        @if(!page.getUuid().isEmpty())
        @template.components.navitem(title="View", href="/notes/" + page.getUuid() + "/", exact = true, page=page)
        @template.components.navitem(title="Edit", href="/notes/" + page.getUuid() + "/edit", query = "returnPath="+page.getUuid(), page=page)
        @endif
`)
    <form method="post" action="${page.fullPath()}">
        <div class="mb-3">
            <label for="title" class="form-label">Title</label>
            <input type="text" class="form-control ${page.getFieldBootstrapCssClass("title")}" id="title" name="title"  placeholder="note title" value="${page.getTitle()}">
            @if(page.fieldHasError("title"))
            <div class="invalid-feedback">
                ${page.getErrorFeedback("title")}
            </div>
            @endif
        </div>
        <div class="mb-3">
            <label for="body" class="form-label">Body</label>
            <textarea class="form-control ${page.getFieldBootstrapCssClass("body")}" id="body" name="body" rows="4" placeholder="content of note">${page.getBody()}</textarea>
            @if(page.fieldHasError("body"))
                <div class="invalid-feedback">
                    ${page.getErrorFeedback("body")}
                </div>
            @endif
        </div>
        <button type="submit" class="btn btn-primary">Save</button>
        <a href="${page.returnPathOrDefault("/notes/")}" class="btn btn-secondary">Cancel</a>
    </form>
`)