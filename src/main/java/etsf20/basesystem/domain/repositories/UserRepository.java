package etsf20.basesystem.domain.repositories;

import etsf20.basesystem.domain.models.UserRole;
import etsf20.basesystem.domain.models.User;
import etsf20.basesystem.persistance.Database;

import java.util.*;

public class UserRepository extends BaseRepository {

    public UserRepository(Database db) {
        super(db);
    }

    /**
     * Get user by username
     * @param username username to search for
     * @return optional user
     */
    public Optional<User> get(String username) {
        return db.findFirst("SELECT username, displayName, hashedPassword, role FROM users WHERE username = ?", rs -> {
            String dbUsername = rs.getString(1);
            String displayName = rs.getString(2);
            String hashedPassword = rs.getString(3);
            UserRole userRole = UserRole.valueOf(rs.getString(4));
            return new User(dbUsername, displayName, hashedPassword, userRole);
        }, username);
    }

    /**
     * Create new user
     */
    public void create(User user) {
        db.insert("users", Map.of(
                "username", user.getUsername(),
                "displayName", user.getDisplayName(),
                "hashedPassword", user.getPasswordHash(),
                "role", user.getRole().toString()
        ));
    }

    /**
     * List users in the database
     */
    public List<User> list() {
        return list(null);
    }

    /**
     * List users in the database
     *
     * @param userRoleFilter filter by role or no role if null
     * @return list of users
     */
    public List<User> list(UserRole userRoleFilter) {
        ArrayList<String> params = new ArrayList<>();

        String sql = "SELECT username, displayname, role FROM users";
        if(userRoleFilter != null) {
            sql += " WHERE role = ?";
            params.add(userRoleFilter.toString());
        }
        sql += " ORDER BY username";

        return db.list(sql, rs -> {
            String username = rs.getString(1);
            String displayName = rs.getString(2);
            UserRole userRole = UserRole.valueOf(rs.getString(3));
            return new User(username, displayName, userRole);
        }, params.toArray());
    }

    /**
     * Update user
     * @return true if user was found and updated
     */
    public boolean update(User user) {
        return db.update("users",
                Map.of("username", user.getUsername()),
                Map.of("displayname", user.getDisplayName(),
                       "hashedPassword", user.getPasswordHash(),
                       "role", user.getRole().toString()));
    }

    /**
     * Delete user
     */
    public boolean delete(String username) {
        return db.delete("users", Map.of("username", username));
    }
}
