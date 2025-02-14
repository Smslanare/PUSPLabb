@param etsf20.basesystem.web.pages.projects.CreateEditProjectPage page

@template.layout.page(
title = page.getUuid().isEmpty() ? "Create Project" : "Edit Project",
page = page,
content = @`
    @template.pages.projects.nav(page = page, extraLinks = @`
        @if(!page.getUuid().isEmpty())
            @template.components.navitem(title="View", href="/projects/" + page.getUuid() + "/", exact = true, page=page)
        @endif
    `)
    <form method="post" action="${page.fullPath()}">
        <div class="mb-3">
            <label for="projectName" class="form-label">Project Name</label>
            <input type="text" class="form-control ${page.getFieldBootstrapCssClass("title")}" id="projectName" name="projectName" placeholder="Project name" value="${page.getProjectName()}">
            @if(page.fieldHasError("projectName"))
                <div class="invalid-feedback">
                    ${page.getErrorFeedback("projectName")}
                </div>
            @endif
        </div>
        <div class="mb-3">
            <label for="description" class="form-label">Description</label>
            <textarea class="form-control ${page.getFieldBootstrapCssClass("description")}" id="description" name="description" rows="4" placeholder="Project description">${page.getDescription()}</textarea>
            @if(page.fieldHasError("description"))
                <div class="invalid-feedback">
                    ${page.getErrorFeedback("description")}
                </div>
            @endif
        </div>
        <div class="mb-3">
            <label for="user" class="form-label">User</label>
            <select class="form-select ${page.getFieldBootstrapCssClass("user")}" id="user" name="user">
                <option value="">Select user</option>
                @for(etsf20.basesystem.domain.models.User user : page.getUsers())
                    <option value="${user.getUsername()}">${user.getUsername()} | ${user.getUsername()}</option>
                @endfor
            </select>
            @if(page.fieldHasError("user"))
                <div class="invalid-feedback">
                    ${page.getErrorFeedback("user")}
                </div>
            @endif
        </div>

        <button type="submit" class="btn btn-primary">Save</button>
        <a href="${page.returnPathOrDefault("/projects/")}" class="btn btn-secondary">Cancel</a>
    </form>
`)