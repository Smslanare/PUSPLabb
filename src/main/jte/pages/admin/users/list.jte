@param etsf20.basesystem.web.pages.admin.UserPage page

@template.layout.page(
title = "User Configuration",
page = page,
content = @`
    @template.pages.admin.users.nav(page = page)
    !{ String currentUser = page.session().username(); }
    <table class="table">
        <thead>
        <tr>
            <th scope="col">User</th>
            <th scope="col">Name</th>
            <th scope="col">Role</th>
            <th scope="col">Operations</th>
        </tr>
        </thead>
        <tbody>
        @for(var user : page.getUsers())
            <tr>
                <th scope="row">${user.getUsername()}</th>
                <td>${user.getDisplayName()}</td>
                <td>${user.getRole().toString()}</td>
                @if(user.getUsername().equals(currentUser))
                <td></td>
                @else
                <td><a href="/admin/users/${user.getUsername()}/reset-password" class="btn btn-sm btn-primary mx-1">Reset Password</a><a href="/admin/users/${user.getUsername()}/delete?returnPath=/admin/users/" class="btn btn-sm btn-danger mx-1">Delete</a></td>
                @endif
            </tr>
        @endfor
        </tbody>
    </table>
`)