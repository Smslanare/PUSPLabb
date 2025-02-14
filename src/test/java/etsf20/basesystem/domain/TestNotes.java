package etsf20.basesystem.domain;

import com.zaxxer.hikari.HikariDataSource;
import etsf20.basesystem.Config;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import etsf20.basesystem.Main;
import etsf20.basesystem.domain.models.Note;
import etsf20.basesystem.domain.repositories.Repositories;
import etsf20.basesystem.persistance.Database;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class TestNotes {
    private Repositories repos;
    private HikariDataSource pool;

    @BeforeEach
    public void setUp() throws SQLException {
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
    public void tearDown() throws SQLException {
        this.repos.getDatabase().close();
        pool.close();
    }

    private Note createExampleNote() {
        return new Note("Test 1", "Body 1", "admin");
    }

    private void validateExampleNote(Note note) {
        assertEquals("Test 1", note.getTitle(), "field title does not match 'Test 1'");
        assertEquals("Body 1", note.getBody(), "field body does not match 'Body 1'");
        assertEquals("admin", note.getUserName(), "field username does not match 'admin'");
        assertEquals("Administrator", note.getDisplayName(), "field displayName does not match 'Administrator'");
        assertTrue(note.getTimestamp().isBefore(Instant.now()), "note created in the future");
        assertTrue(note.getTimestamp().isAfter(Instant.ofEpochMilli(0)), "note has a zero timestamp");
    }

    /**
     * Integration test
     */
    @Test
    public void testNotes() {
        UUID uuid = repos.notes().create(createExampleNote());
        assertNotNull(uuid, "uuid is null");

        Optional<Note> dbNote = repos.notes().get(uuid);
        assertFalse(dbNote.isEmpty(), "not could not be found");

        Note firstNote = dbNote.get();
        validateExampleNote(firstNote);

        assertFalse(repos.notes().delete("user", uuid), "user found");

        List<Note> notes = repos.notes().list("admin", -1, 0, false);
        assertFalse(notes.isEmpty(), "could not find journal");

        assertEquals(1,notes.size());
        validateExampleNote(notes.get(0));

        Note firstNoteUpdated = notes.get(0);
        firstNoteUpdated.setTitle("Test 2");
        firstNoteUpdated.setTitle("Body 2");

        assertTrue(repos.notes().update(firstNoteUpdated, firstNote.getTimestamp().plus(1, ChronoUnit.SECONDS)), "could not update");

        Optional<Note> updatedNote = repos.notes().get(uuid);

        assertTrue(firstNote.getTimestamp().isBefore(updatedNote.orElseThrow().getTimestamp()), "timestamp not updated");

        assertTrue(repos.notes().delete("admin", uuid), "failed to delete");
        repos.commit();
    }

    @Test
    public void testCreateNotes() {
        UUID uuid = repos.notes().create(createExampleNote());
        assertNotNull(uuid, "uuid is null");
    }

    @Test
    public void testUpdateNote() {
        UUID uuid = repos.notes().create(createExampleNote());
        assertNotNull(uuid, "uuid is null");

        Optional<Note> dbNote = repos.notes().get(uuid);
        assertFalse(dbNote.isEmpty(), "not could not be found");

        Note note = dbNote.get();
        note.setTitle("New title");
        assertEquals("New title", note.getTitle());

        repos.notes().update(note);

        dbNote = repos.notes().get(uuid);
        assertFalse(dbNote.isEmpty(), "not could not be found");

        assertEquals("New title", dbNote.get().getTitle());
    }

    @Test
    public void testRemoveNote() {
        UUID uuid = repos.notes().create(createExampleNote());
        assertNotNull(uuid, "uuid is null");

        assertTrue(repos.notes().delete("admin", uuid), "could not find note");

        UUID noneExistent = UUID.randomUUID();
        assertFalse(repos.notes().delete("admin", noneExistent));
    }

    @Test
    public void testListNotes() {
        List<Note> emptyList = repos.notes().list("admin");
        assertTrue(emptyList.isEmpty(), "not empty");

        UUID uuid = repos.notes().create(createExampleNote());
        assertNotNull(uuid, "uuid is null");

        List<Note> oneItemList = repos.notes().list("admin");
        assertEquals(1, oneItemList.size());
        assertEquals(oneItemList.get(0).getUuid(), uuid, "uuid does not match");

        emptyList = repos.notes().list("non-existent");
        assertTrue(emptyList.isEmpty(), "not empty");
    }
}
