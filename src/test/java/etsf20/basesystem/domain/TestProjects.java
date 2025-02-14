package etsf20.basesystem.domain;

import com.zaxxer.hikari.HikariDataSource;
import etsf20.basesystem.Config;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import etsf20.basesystem.Main;
import etsf20.basesystem.domain.models.Project;
import etsf20.basesystem.domain.repositories.Repositories;
import etsf20.basesystem.persistance.Database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class TestProjects {
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

    private Project createExampleProject() {
        return new Project(UUID.randomUUID(), "Test Project", "Test Description");
    }

    private void validateExampleProject(Project project) {
        assertEquals("Test Project", project.getProjectName(), "field projectName does not match 'Test Project'");
        assertEquals("Test Description", project.getDescription(), "field description does not match 'Test Description'");
    }

    @Test
    public void testProjects() {
        UUID uuid = repos.projects().create(createExampleProject());
        assertNotNull(uuid, "uuid is null");

        Optional<Project> dbProject = repos.projects().get(uuid);
        assertFalse(dbProject.isEmpty(), "project could not be found");

        Project firstProject = dbProject.get();
        validateExampleProject(firstProject);

        List<Project> projects = repos.projects().list(-1, 0, true);
        assertFalse(projects.isEmpty(), "could not find projects");

        assertEquals(1, projects.size());
        validateExampleProject(projects.get(0));

        Project firstProjectUpdated = projects.get(0);
        firstProjectUpdated.setProjectName("Updated Project");
        firstProjectUpdated.setDescription("Updated Description");

        repos.projects().update(firstProjectUpdated);

        Optional<Project> updatedProject = repos.projects().get(uuid);
        assertEquals("Updated Project", updatedProject.orElseThrow().getProjectName(), "projectName not updated");
        assertEquals("Updated Description", updatedProject.orElseThrow().getDescription(), "description not updated");

        assertTrue(repos.projects().delete(uuid), "failed to delete project");
        repos.commit();
    }

    @Test
    public void testCreateProject() {
        UUID uuid = repos.projects().create(createExampleProject());
        assertNotNull(uuid, "uuid is null");
    }

    @Test
    public void testRemoveProject() {
        UUID uuid = repos.projects().create(createExampleProject());
        assertNotNull(uuid, "uuid is null");

        assertTrue(repos.projects().delete(uuid), "could not find project");

        UUID nonExistent = UUID.randomUUID();
        assertFalse(repos.projects().delete(nonExistent));
    }

    @Test
    public void testListProjects() {
        List<Project> emptyList = repos.projects().list();
        assertTrue(emptyList.isEmpty(), "not empty");

        UUID uuid = repos.projects().create(createExampleProject());
        assertNotNull(uuid, "uuid is null");

        List<Project> oneItemList = repos.projects().list();
        assertEquals(1, oneItemList.size());
        assertEquals(oneItemList.get(0).getUuid(), uuid, "uuid does not match");

        assertTrue(repos.projects().delete(uuid), "project was deleted");

        emptyList = repos.projects().list();
        assertTrue(emptyList.isEmpty(), "not empty");
    }
}