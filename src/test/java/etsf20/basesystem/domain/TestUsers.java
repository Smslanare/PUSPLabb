package etsf20.basesystem.domain;

import com.zaxxer.hikari.HikariDataSource;
import etsf20.basesystem.Config;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import etsf20.basesystem.Main;
import etsf20.basesystem.domain.models.User;
import etsf20.basesystem.domain.models.UserRole;
import etsf20.basesystem.domain.repositories.Repositories;
import etsf20.basesystem.persistance.Database;
import etsf20.basesystem.security.Argon2PasswordHash;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class TestUsers {
    private Repositories repos;
    private HikariDataSource pool;

    @BeforeEach
    void setUp() throws SQLException {
        Config testConfig = Config.testConfigurationSingleConnection();
        pool = Database.createPool(testConfig);

        try(Connection conn = pool.getConnection()) {
            Main.createSchemaIfNotExists(conn);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        Database testing = new Database(pool.getConnection());

        this.repos = Repositories.from(testing);
    }

    @AfterEach
    void tearDown() throws SQLException {
        this.repos.getDatabase().close();
        pool.close();
    }
    @Test
    public void testVerifyPassword() {
        String hashedPassword = repos.users().get("admin").orElseThrow().getPasswordHash();
        assertTrue(Argon2PasswordHash.verify(hashedPassword, "Admin@1234"), "password is not accepted");
        assertFalse(Argon2PasswordHash.verify(hashedPassword, "admin_123"), "accepts incorrect password");
    }

    /**
     * Integration test
     */
    @Test
    public void testUsers() {
        User newUser = new User("new-user", "New user", UserRole.USER);
        newUser.setPassword("example-password");
        repos.users().create(newUser);

        newUser = repos.users().get("new-user").orElseThrow();
        assertTrue(newUser.verifyPassword("example-password"), "password is not accepted");
        assertFalse(newUser.verifyPassword("example_password"), "accepts incorrect password");

        newUser.setPassword("example-new-password");
        assertTrue(repos.users().update(newUser), "user not updated");

        newUser = repos.users().get("new-user").orElseThrow();
        assertTrue(newUser.verifyPassword("example-new-password"), "password is not accepted");
        assertFalse(newUser.verifyPassword("example_password"), "accepts incorrect password");

        Set<String> collect = repos.users()
                                   .list()
                                   .stream()
                                   .map(User::getUsername)
                                   .collect(Collectors.toSet());

        assertEquals(Set.of("admin", "new-user"), collect, "not matching admin and new-user");

        assertTrue(repos.users().delete("new-user"), "user was not deleted");

        collect = repos.users()
                       .list()
                       .stream()
                       .map(User::getUsername)
                       .collect(Collectors.toSet());

        assertEquals(Set.of("admin"), collect, "not matching admin");
    }

    @Test
    public void testCreateUser() {
        User newUser = new User("normal-user", "New normal user", UserRole.USER);
        newUser.setPassword("normal-password");
        repos.users().create(newUser);

        Optional<User> newDbUser = repos.users().get("normal-user");
        assertFalse(newDbUser.isEmpty(), "could not find created user");

        newUser = newDbUser.get();

        assertEquals("normal-user", newUser.getUsername());
        assertEquals("New normal user", newUser.getDisplayName());
        assertEquals(UserRole.USER, newUser.getRole());

        assertTrue(newUser.verifyPassword("normal-password"));
        assertFalse(newUser.verifyPassword("normal_password"));
    }

    @Test
    public void testCreateAdminUser() {
        User newUser = new User("admin-user", "New admin user", UserRole.ADMIN);
        newUser.setPassword("admin-password");
        repos.users().create(newUser);

        Optional<User> newDbUser = repos.users().get("admin-user");
        assertFalse(newDbUser.isEmpty(), "could not find created user");

        assertEquals("admin-user", newUser.getUsername());
        assertEquals("New admin user", newUser.getDisplayName());
        assertEquals(UserRole.ADMIN, newUser.getRole());

        assertTrue(newUser.verifyPassword("admin-password"));
        assertFalse(newUser.verifyPassword("admin_password"));
    }

    @Test
    public void testListUsers() {
        List<User> list = repos.users().list(UserRole.USER);
        assertTrue(list.isEmpty(), "users are not filtered");

        list = repos.users().list(UserRole.ADMIN);
        assertFalse(list.isEmpty(), "users are not filtered");
        assertEquals(1, list.size());

        User adminUser = list.get(0);
        assertEquals("admin", adminUser.getUsername());
        assertEquals("Administrator", adminUser.getDisplayName());
        assertEquals(UserRole.ADMIN, adminUser.getRole());
    }

    @Test
    public void testRemoveUser() {
        User newUser = new User("normal-user", "New normal user", UserRole.USER);
        newUser.setPassword("normal-password");
        repos.users().create(newUser);

        assertFalse(repos.users().delete("unknown-user"));
        assertEquals(2, repos.users().list().size());

        assertTrue(repos.users().delete("admin"));

        assertFalse(repos.users().list().isEmpty());
        assertTrue(repos.users().delete("normal-user"));

        assertTrue(repos.users().list().isEmpty());
    }
}

