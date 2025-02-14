package etsf20.basesystem.domain;

import com.zaxxer.hikari.HikariDataSource;
import etsf20.basesystem.Config;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import etsf20.basesystem.Main;
import etsf20.basesystem.domain.models.Project;
import etsf20.basesystem.domain.models.User;
import etsf20.basesystem.domain.models.UserProject;
import etsf20.basesystem.domain.models.UserRole;
import etsf20.basesystem.domain.repositories.Repositories;
import etsf20.basesystem.persistance.Database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class TestUserProject {
    private Repositories repos;
    private HikariDataSource pool;

    @BeforeEach
    public void setUp() throws SQLException {
        Config testConfig = Config.testConfigurationSingleConnection();
        pool = Database.createPool(testConfig);

        try (Connection conn = pool.getConnection()) {
            Main.createSchemaIfNotExists(conn);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        Database testing = new Database(pool.getConnection());

        this.repos = Repositories.from(testing);
    }

    @AfterEach
    public void tearDown() throws SQLException {
        this.repos.getDatabase().close();
        pool.close();
    }

    private UserProject createExampleUserProject() {
        return new UserProject("testUser", UUID.randomUUID().toString());
    }

    private void createUser(String username) {
        User user = new User(username, "Test User", "hashedPassword", UserRole.USER);
        repos.users().create(user);
    }

    private UUID createProject() {
        Project project = new Project(UUID.randomUUID(), "Test Project", "Test Description");
        return repos.projects().create(project);
    }

    @Test
    public void testAddUserToProject() {
        UserProject userProject = createExampleUserProject();
        createUser(userProject.getUsername());
        UUID projectId = createProject();
        boolean added = repos.userProjects().addUserToProject(userProject.getUsername(), projectId);
        assertTrue(added, "user not added to project");
    }

    @Test
    public void testRemoveUserFromProject() {
        UserProject userProject = createExampleUserProject();
        createUser(userProject.getUsername());
        UUID projectId = createProject();
        repos.userProjects().addUserToProject(userProject.getUsername(), projectId);

        boolean removed = repos.userProjects().removeUserFromProject(userProject.getUsername(), projectId);
        assertTrue(removed, "user not removed from project");
    }

    @Test
    public void testGetUsersForProject() {
        UserProject userProject = createExampleUserProject();
        createUser(userProject.getUsername());
        UUID projectId = createProject();
        repos.userProjects().addUserToProject(userProject.getUsername(), projectId);

        List<String> users = repos.userProjects().getUsersForProject(projectId);
        assertFalse(users.isEmpty(), "no users found for project");
        assertEquals(userProject.getUsername(), users.get(0), "username does not match");
    }
}