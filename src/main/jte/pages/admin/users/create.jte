@import etsf20.basesystem.web.pages.admin.CreateUserPage
@import etsf20.basesystem.domain.models.UserRole
@param CreateUserPage page

@template.layout.page(
title = "User - Create",
page = page,
content = @`
    @template.pages.admin.users.nav(page = page)
    @if(page.state().step() == 0)
        <form method="post" action="${page.statefulPath()}">
            <div class="mb-3">
                <label for="username" class="form-label">Username</label>
                <input type="text" class="form-control ${page.getFieldBootstrapCssClass("username")}" id="username" name="username"  aria-describedby="usernameHelp" value="${page.state().username()}">
                @if(page.fieldHasError("username"))
                    <div class="invalid-feedback">
                        ${page.getErrorFeedback("username")}
                    </div>
                @endif
            </div>
            <div class="mb-3">
                <label for="displayName" class="form-label">Display Name</label>
                <input type="text" class="form-control ${page.getFieldBootstrapCssClass("displayName")}" id="displayName" name="displayName"  aria-describedby="displayNameHelp" value="${page.state().displayName()}">
                @if(page.fieldHasError("displayName"))
                    <div class="invalid-feedback">
                        ${page.getErrorFeedback("displayName")}
                    </div>
                @endif
            </div>
            <div class="mb-3">
                <label for="displayName" class="form-label">User Role</label>
                <select class="form-select" id="userRole" name="userRole" aria-label="Select user">
                    <option selected="${page.state().userRole() == UserRole.USER}" value="USER">User</option>
                    <option selected="${page.state().userRole() == UserRole.ADMIN}" value="ADMIN">Administrator</option>>
                </select>
            </div>
            <button type="submit" class="btn btn-primary">Next step</button>
            <a href="${page.returnPathOrDefault("/admin/users/")}" class="btn btn-secondary">Cancel</a>
        </form>
    @endif
    @if(page.state().step() == 1)
        <form method="post" action="${page.statefulPath()}">
        <div class="mb-12">
            <h3>Confirm that the following is correct:</h3>
            <p><b>Username: </b>${page.state().username()}</p>
            <p><b>Display name: </b>${page.state().displayName()}</p>
            <p><b>Role: </b>${page.state().userRole()}</p>

            <button type="submit" class="btn btn-primary" value="create" name="action">Create User</button>
            <button type="submit" class="btn btn-secondary" value="go-back" name="action">Go back</button>
        </div>
        </form>
    @endif
`)