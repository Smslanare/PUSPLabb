@param etsf20.basesystem.web.pages.admin.NewPasswordPage page

@template.layout.page(
title = "User - new password",
page = page,
content = @`
    @template.pages.admin.users.nav(page = page)
    <h3>${page.getMessage()}</h3>
    <p>New password:<br/><pre>${page.getNewPassword()}</pre></p>
`)