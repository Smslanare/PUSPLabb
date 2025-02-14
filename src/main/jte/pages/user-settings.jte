@param etsf20.basesystem.web.pages.users.SettingPage page

@template.layout.page(
title = "Journal - Edit",
page = page,
content = @`
    <h3>User settings (${page.getUser().getDisplayName()})</h3>
    <h5 class="mt-4">Password settings</h5>
    <div class="border bg-light p-3">
        <form method="post" action="${page.fullPath()}">
            <div class="mb-3">
                <label for="old-password" class="form-label">Old password</label>
                <input type="password" class="form-control ${page.getFieldBootstrapCssClass("old-password")}" id="old-password" name="old-password" value="">
                @if(page.fieldHasError("old-password"))
                    <div class="invalid-feedback">
                        ${page.getErrorFeedback("old-password")}
                    </div>
                @endif
            </div>
            <div class="mb-3">
                <label for="new-password" class="form-label">New password</label>
                <input type="password" class="form-control ${page.getFieldBootstrapCssClass("new-password")}" id="new-password" name="new-password" value="">
                @if(page.fieldHasError("new-password"))
                    <div class="invalid-feedback">
                        ${page.getErrorFeedback("new-password")}
                    </div>
                @endif
            </div>
            <div class="mb-3">
                <label for="new-repeat-password" class="form-label">Repeat new password</label>
                <input type="password" class="form-control ${page.getFieldBootstrapCssClass("new-repeat-password")}" id="new-repeat-password" name="new-repeat-password"  value="">
                @if(page.fieldHasError("new-repeat-password"))
                    <div class="invalid-feedback">
                        ${page.getErrorFeedback("new-repeat-password")}
                    </div>
                @endif
            </div>
            <button type="submit" class="btn btn-primary">Update password</button>
        </form>
    </div>
`)