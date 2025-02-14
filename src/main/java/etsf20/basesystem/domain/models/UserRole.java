package etsf20.basesystem.domain.models;

import io.javalin.security.RouteRole;

public enum UserRole implements RouteRole {
    GUEST, //Used to identify users that are not logged in
    USER,
    ADMIN;

    public static UserRole[] loggedIn() {
        return new UserRole[]{USER, ADMIN};
    }
}
