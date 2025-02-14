package etsf20.basesystem.domain.models;

import etsf20.basesystem.security.Argon2PasswordHash;

import java.io.Serializable;

public class User implements Serializable {
    private final String username;
    private String displayName;
    private UserRole userRole;

    // Do not save the hash if class is serialized
    private transient String passwordHash;

    public User(String username, String displayName, UserRole userRole) {
        this.userRole = userRole;
        this.displayName = displayName;
        this.username = username;
    }

    public User(String username, String displayName, String passwordHash, UserRole userRole) {
        this.username = username;
        this.displayName = displayName;
        this.passwordHash = passwordHash;
        this.userRole = userRole;
    }

    public static User createWithPassword(String username, String displayName, String password, UserRole userRole) {
        return new User(username, displayName, Argon2PasswordHash.create(password), userRole);
    }

    public String getUsername() {
        return username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    /**
     * Verify password
     * @param testPassword password to test
     * @return true if equal
     */
    public boolean verifyPassword(String testPassword) {
        return Argon2PasswordHash.verify(passwordHash, testPassword);
    }

    /**
     * Hashes the password and updates savedPasswordHash
     * @param password plaintext password
     */
    public void setPassword(String password) {
        this.passwordHash = Argon2PasswordHash.create(password);
    }

    public UserRole getRole() {
        return userRole;
    }

    public void setRole(UserRole userRole) {
        this.userRole = userRole;
    }
}
