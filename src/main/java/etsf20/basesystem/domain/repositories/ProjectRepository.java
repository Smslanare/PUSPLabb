package etsf20.basesystem.domain.repositories;

import etsf20.basesystem.domain.models.Note;
import etsf20.basesystem.domain.models.Project;
import etsf20.basesystem.persistance.Database;
import etsf20.basesystem.persistance.DatabaseException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.*;

public class ProjectRepository extends BaseRepository {

	public ProjectRepository(Database db) {
		super(db);
	}

	public UUID create(Project project) {
		return db.insertWithGenerated("projects",
				Map.of("projectName", project.getProjectName(), "description", project.getDescription()),
				rs -> UUID.fromString(rs.getString("project_uuid")));
	}

	private Project mapProject(ResultSet rs) throws SQLException {
		return new Project(UUID.fromString(rs.getString("project_uuid")), rs.getString("projectName"),
				rs.getString("description"));
	}

	/**
	 * List all projects
	 *
	 * @return list of projects
	 */
	public List<Project> list() {
		return list(-1, 0, true);
	}

	/**
	 * List all projects
	 *
	 * @param limit     the maximum number of entries to return, -1 for no limit
	 * @param offset    the starting position
	 * @param ascending sorting by ascending project name or descending
	 * @return list of projects
	 */
	public List<Project> list(int limit, int offset, boolean ascending) {
		String sql = "SELECT " + "project_uuid, " + "projectName, " + "description " + "FROM projects";

		if (ascending) {
			sql += " ORDER BY projectName ASC";
		} else {
			sql += " ORDER BY projectName DESC";
		}

		ArrayList<Object> params = new ArrayList<>();

		if (limit != -1) {
			sql += " LIMIT ?";
			params.add(limit);
		}

		if (offset != 0) {
			sql += " OFFSET ?";
			params.add(offset);
		}

		return db.list(sql, this::mapProject, params.toArray());
	}

	/**
	 * Get a project
	 *
	 * @param uuid project uuid
	 * @return project if it could be found
	 */
	public Optional<Project> get(UUID uuid) {
		String sql = "SELECT project_uuid, projectName, description " + "FROM projects " + "WHERE project_uuid = ?";

		return db.findFirst(sql, this::mapProject, uuid.toString());
	}

	public boolean delete(UUID uuid) {
		try (var stmt = db.prepare("DELETE FROM projects " + "WHERE project_uuid = ?")) {
			stmt.setString(1, uuid.toString());
			return stmt.executeUpdate() > 0;
		} catch (SQLException ex) {
			throw new DatabaseException("failed to delete project", ex);
		}
	}

	public boolean update(Project project) {
		return update(project, Instant.now());
	}

	/**
	 * Update project
	 *
	 * @param project updated project
	 * @param now     current server time for the updated timestamp
	 * @return true if project was found and updated
	 */
	public boolean update(Project project, Instant now) {
		Map<String, Object> changes = new HashMap<>();
		changes.put("projectName", project.getProjectName());
		changes.put("description", project.getDescription());

		return db.update("projects", Map.of("project_uuid", project.getUuid()), changes);
	}

}