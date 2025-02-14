package etsf20.basesystem.domain.repositories;

import etsf20.basesystem.domain.models.Note;
import etsf20.basesystem.persistance.Database;
import etsf20.basesystem.persistance.DatabaseException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.*;


public class NoteRepository extends BaseRepository {

    public NoteRepository(Database db) {
        super(db);
    }

    /**
     * Create new note
     *
     * @param note note
     * @return UUID for created journal note
     */
    public UUID create(Note note) {
        return db.insertWithGenerated("notes",
                Map.of("username", note.getUserName(),
                       "title", note.getTitle(),
                       "body", note.getBody()
                ), rs -> UUID.fromString(rs.getString("note_uuid"))
        );
    }

    /**
     * Map a note row to a Note object
     * @param rs result set
     * @return populated note
     * @throws SQLException when any operation on the result set object fails
     */
    private Note mapNote(ResultSet rs) throws SQLException {
        return new Note(
                UUID.fromString(rs.getString("note_uuid")),
                rs.getTimestamp("timestamp").toInstant(),
                rs.getString("username"),
                rs.getString("displayName"),
                rs.getString("title"),
                rs.getString("body")
        );
    }

    /**
     * List all notes
     *
     * @return list of journal entries
     */
    public List<Note> list(String username) {
        return list(username, -1, 0, true);
    }

    /**
     * List all notes
     *
     * @param limit the maximum number of entries to return, -1 for no limit
     * @param offset the starting position
     * @param ascending sorting by ascending timestamp or descending
     * @return list of journal entries
     */
    public List<Note> list(String username, int limit, int offset, boolean ascending) {
        String sql = "SELECT " +
                     "note_uuid, " +
                     "timestamp, " +
                     "users.username, " +
                     "displayName, " +
                     "title, " +
                     "body " +
                     "FROM notes "+
                     "JOIN users USING (username) " +
                     "WHERE notes.username = ?";

        ArrayList<Object> params = new ArrayList<>();
        params.add(username);

        if(ascending) {
            sql += " ORDER BY timestamp ASC";
        } else {
            sql += " ORDER BY timestamp DESC";
        }


        if(limit != -1) {
            sql += " LIMIT ?";
            params.add(limit);
        }

        if(offset != 0) {
            sql += " OFFSET ?";
            params.add(offset);
        }

        return db.list(sql, this::mapNote, params.toArray());
    }

    /**
     * Get a note
     *
     * @param uuid note uuid
     * @return note if it could be found
     */
    public Optional<Note> get(UUID uuid) {
        String sql = "SELECT note_uuid, timestamp, users.username, displayName, title, body " +
                "FROM notes " +
                "JOIN users USING (username) " +
                "WHERE note_uuid = ?";

        return db.findFirst(sql, this::mapNote, uuid.toString());
    }

    /**
     * Update note
     *
     * @param note updated note
     * @return true if note was found and updated
     */
    public boolean update(Note note) {
        return update(note, Instant.now());
    }


    /**
     * Update note
     *
     * @param note updated note
     * @param now current server time for the updated timestamp
     * @return true if note was found and updated
     */
    public boolean update(Note note, Instant now) {
        Map<String,Object> changes = new HashMap<>();
        changes.put("title", note.getTitle());
        changes.put("body", note.getBody());
        changes.put("timestamp", now);

        return db.update("notes", Map.of("note_uuid", note.getUuid()), changes);
    }

    /**
     * Remove note
     *
     * @param user the user that attempts to remove note (to check that user has permission)
     * @param uuid note uuid
     * @return true if note was found and deleted, false if not found or user does not have permission
     */
    public boolean delete(String user, UUID uuid) {
        try(var stmt = db.prepare("DELETE FROM notes "+
                       "WHERE note_uuid = ? " +
                       "AND (username = ? " +
                            "OR EXISTS (SELECT username FROM users WHERE users.username = ? AND role = 'ADMIN')" +
                       ")")) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, user);
            stmt.setString(3, user);
            return stmt.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new DatabaseException("failed to delete note", ex);
        }
    }
}
