package etsf20.basesystem.domain.repositories;

import etsf20.basesystem.persistance.Database;

/**
 * Base class for all repositories
 * @author Marcus Klang, Marcus.Klang@cs.lth.se
 */
public abstract class BaseRepository {
    protected Database db;

    public BaseRepository(Database db) {
        this.db = db;
    }
}
