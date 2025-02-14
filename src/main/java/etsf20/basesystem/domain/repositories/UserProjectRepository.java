package etsf20.basesystem.domain.repositories;

import etsf20.basesystem.persistance.Database;
import etsf20.basesystem.persistance.DatabaseException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


public class UserProjectRepository extends BaseRepository {

    public UserProjectRepository(Database db) {
        super(db);
    }

    /**
     * Add a user to a project
     *
     * @param username    The ID of the user
     * @param projectUuid The UUID of the project
     * @return true if insertion was successful
     */
    public boolean addUserToProject(String username, UUID projectUuid) {
        String sql = "INSERT INTO user_projects (username, project_uuid) VALUES (?, ?)";
        try (var stmt = db.prepare(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, projectUuid.toString());
            return stmt.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new DatabaseException("Failed to add user to project", ex);
        }
    }

    /**
     * Remove a user from a project.
     *
     * @param username     The ID of the user
     * @param projectUuid The UUID of the project
     * @return true if deletion was successful
     */
    public boolean removeUserFromProject(String username, UUID projectUuid) {
        String sql = "DELETE FROM user_projects WHERE username = ? AND project_uuid = ?";
        try (var stmt = db.prepare(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, projectUuid.toString());
            return stmt.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new DatabaseException("Failed to remove user from project", ex);
        }
    }

    /**
     * Get all users assigned to a project.
     *
     * @param projectUuid The UUID of the project
     * @return A list of user IDs and roles
     */
    public List<Map<String, Object>> getUsersForProject(UUID projectUuid) {
        String sql = "SELECT username FROM user_projects WHERE project_uuid = ?";
        try {
            return db.list(sql, rs -> {
                Map<String, Object> result = new HashMap<>();
                result.put("username", rs.getString("username"));
                return result;
            }, projectUuid.toString());

        } catch (DatabaseException ex) {
            throw new DatabaseException("Failed to fetch users for project", ex);
        }
    }

    /**
     * Maps a result set row to a user-project mapping.
     */
    private Map<String, Object> mapUserProject(ResultSet rs) throws SQLException {
        Map<String, Object> result = new HashMap<>();
        result.put("username", rs.getString("username"));
        result.put("project_uuid", UUID.fromString(rs.getString("project_uuid")));
        return result;
    }
}





