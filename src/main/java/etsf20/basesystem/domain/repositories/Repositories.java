package etsf20.basesystem.domain.repositories;

import io.javalin.http.Context;
import etsf20.basesystem.persistance.Database;

import java.sql.Connection;

public class Repositories {
    private final Database db;
    private NoteRepository journals;
    private UserRepository users;
    private ProjectRepository projects;
    private UserProjectRepository userProjects;

    public Repositories(Database db) {
        this.db = db;
    }

    /**
     * Create a repositories instance from current request
     * @param ctx context
     */
    public static Repositories from(Context ctx) {
        return new Repositories(Database.get(ctx));
    }

    /**
     * Create a repositories instance from a database connection
     * @param conn jdbc connection
     */
    public static Repositories from(Connection conn) {
        return new Repositories(new Database(conn));
    }

    /**
     * Create a repositories instance from a wrapped database connection
     * @param db wrapped database instance
     */
    public static Repositories from(Database db) {
        return new Repositories(db);
    }

    /**
     * User repository
     */
    public UserRepository users() {
        if(users == null) {
            this.users = new UserRepository(db);
        }
        return users;
    }

    /**
     * Note repository
     */
    public NoteRepository notes() {
        if(journals == null) {
            this.journals = new NoteRepository(db);
        }
        return journals;
    }

    public ProjectRepository projects() {
        if(projects == null) {
            this.projects = new ProjectRepository(db);
        }
        return projects;
    }

    public UserProjectRepository userProjects() {
        if(userProjects == null) {
            this.userProjects = new UserProjectRepository(db);
        }
        return userProjects;
    }

    /**
     * Commit current changes
     */
    public void commit() {
        this.db.commit();
    }

    /**
     * Rollback current changes
     */
    public void rollback() {
        this.db.rollback();
    }

    /**
     * Get underlying wrapped database connection
     */
    public Database getDatabase() {
        return db;
    }
}
