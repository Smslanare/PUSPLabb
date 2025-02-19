package etsf20.basesystem.domain.repositories;
import etsf20.basesystem.domain.models.User;
import etsf20.basesystem.domain.models.UserRole;
import etsf20.basesystem.domain.models.Project;
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
		// If user is already in project, return false
		String sql1 = "SELECT * FROM user_projects WHERE username = ? AND project_uuid = ?";
		try (var stmt = db.prepare(sql1)) {
			stmt.setString(1, username);
			stmt.setString(2, projectUuid.toString());
			if (stmt.executeQuery().next()) {
				return false;
			}
		} catch (SQLException ex) {
			throw new DatabaseException("Failed to check if user is already in project", ex);
		}

		String sql2 = "INSERT INTO user_projects (username, project_uuid) VALUES (?, ?)";
		try (var stmt = db.prepare(sql2)) {
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
	 * @param username    The ID of the user
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
	public List<User> getUsersForProject(UUID projectUuid) {
	    String sql = "SELECT u.username, u.displayName, u.role " +
	                 "FROM user_projects up " +
	                 "JOIN users u ON up.username = u.username " +
	                 "WHERE up.project_uuid = ? " +
	                 "ORDER BY u.displayName ASC";
	    try {
	        return db.list(sql, rs -> {
	            String username = rs.getString("username");
	            String displayName = rs.getString("displayName");
	            String roleStr = rs.getString("role");

	            UserRole role = UserRole.valueOf(roleStr.toUpperCase()); 

	            return new User(username, displayName, role);
	        }, projectUuid.toString());
	    } catch (DatabaseException ex) {
	        throw new DatabaseException("Failed to fetch users for project", ex);
	    }
	}



	public List<Project> getProjectsForUser(String username) {
		String sql = "SELECT p.project_uuid, p.projectName, p.description FROM user_projects up JOIN projects p ON up.project_uuid = p.project_uuid WHERE up.username = ?";
        try {
            return db.list(sql, this::mapProject, username);
        } catch (DatabaseException ex) {
            throw new DatabaseException("Failed to fetch projects for user", ex);
        }
    }

    private Project mapProject(ResultSet rs) throws SQLException {
        return new Project(
                UUID.fromString(rs.getString("project_uuid")),
                rs.getString("projectName"),
                rs.getString("description")
        );
	}

	public List<String> getUsernameForProject(UUID uuid) {
		String sql = "SELECT u.username, u.displayName \r\n"
				+ "FROM user_projects up\r\n"
				+ "JOIN users u ON up.username = u.username\r\n"
				+ "WHERE up.project_uuid = ?\r\n"
				+ "ORDER BY u.displayName ASC;\r\n"
				+ "";
		try {
			return db.list(sql, rs -> rs.getString("username"), uuid.toString());
		} catch (DatabaseException ex) {
			throw new DatabaseException("Failed to fetch users for project", ex);
		}
	}
	
}