package etsf20.basesystem.persistance;

import java.sql.*;
import java.util.*;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import io.javalin.config.Key;
import io.javalin.http.Context;
import org.slf4j.LoggerFactory;
import etsf20.basesystem.Config;

/**
 * Database abstraction class.
 * <p> A set of reusable methods to simplify JDBC/SQL usage
 *
 * @author Marcus Klang, Marcus.Klang@cs.lth.se
 */
@SuppressWarnings("unused")
public class Database implements AutoCloseable {
    public static final Key<HikariDataSource> PoolKey = new Key<>("db.pool");

    private final Connection conn;

    public Connection connection() {
        return conn;
    }

    /**
     * Database instance from a connection
     * @param conn active JDBC connection
     */
    public Database(Connection conn) {
        this.conn = conn;
    }


    public static HikariDataSource createPool(Config systemConfig) {
        HikariConfig config = new HikariConfig();
        config.setMaximumPoolSize(16);
        config.setJdbcUrl(systemConfig.getJdbcUrl());
        config.setUsername(systemConfig.getDbUsername());
        config.setPassword(systemConfig.getDbPassword());
        config.setAutoCommit(false); // we will be using transactions
        config.setTransactionIsolation("TRANSACTION_READ_COMMITTED"); // minimum isolation is to only read committed

        return new HikariDataSource(config);
    }

    /**
     * Get database connection from a request context
     * <p><b>Remarks:</b> Reuses an ongoing connection if one is available</p>
     * @param ctx request context
     * @return new instance of database
     * @throws DatabaseException when a new connection cannot be created
     */
    public static Database get(Context ctx) {
        try {
            Connection conn = ctx.attribute("db");
            if(conn == null) {
                // Could not find an active connection - create new one (get one from the pool or block until available)
                conn = ctx.appData(PoolKey).getConnection();
                ctx.attribute("db", conn);
            }
            return new Database(conn);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to get a connection to the database.", e);
        }
    }
    
    /**
     * Commit current transaction
     * @throws DatabaseException runtime exception for any database failure
     */
    public void commit() {
        try {
            conn.commit();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to commit.", e);
        }
    }

    /**
     * Rollback current transaction
     * @throws DatabaseException runtime exception for any database failure
     */
    public void rollback() {
        try {
            conn.rollback();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to rollback.", e);
        }
    }

    /**
     * When a request has finished dispose any connections properly by rolling back if anything has not been commited
     * @param ctx Request Context
     */
    public static void dispose(Context ctx) {
        Connection conn = ctx.attribute("db");
        if(conn != null) {

            // Rollback if something has not been commited - happens if a request handler
            // crashes but has made changes to the database.
            try {
                conn.rollback();
            } catch (SQLException e) {
                // Ignore exception as we can't do anything about it anyway - printout if something bad happends
                LoggerFactory.getLogger(Database.class).error("Failed to rollback during dispose", e);
            }

            // Try to close the connection
            try {
                conn.close();
            } catch (SQLException e) {
                // Ignore exception as we can't do anything about it anyway - printout if something bad happends
                LoggerFactory.getLogger(Database.class).error("Failed to close connection during dispose", e);
            }

            // Mark that no connection is active on this request
            ctx.attribute("db", null);
        }
    }

    /**
     * Utility method to provide a parameter setter for variable list of parameters
     * @param params params
     * @return parameter setter lambda
     */
    private static ParameterSetter objectParameterSetter(Object...params) {
        return (stmt) -> {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i+1, params[i]);
            }
        };
    }

    /**
     * Get the first if any result from a query 
     * @param <T> mapped output type
     * @param sql sql query
     * @param mapper result mapper 
     * @param params optional params for query
     * @return optional first result
     * @throws DatabaseException if a database access error occurs, this method is called on a closed connection.
     */
    public <T> Optional<T> findFirst(String sql, ResultFunction<T> mapper, Object...params) {
        List<T> result = list(sql, mapper, objectParameterSetter(params),1);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    /**
     * Require 1 results from a query 
     * @param <T> mapped output type
     * @param sql sql query
     * @param mapper result mapper 
     * @param params optional params for query
     * @throws DatabaseException if exactly one row is not returned, database access error occurs,
     *                           this method is called on a closed connection.
     * @return optional first result
     */
    public <T> T findSingle(String sql, ResultFunction<T> mapper, Object...params) {
        List<T> result = list(sql, mapper, objectParameterSetter(params),2);
        if(result.size() != 1) {
            throw new DatabaseException("Did not recieve a single result.");
        }

        return result.get(0);
    }

    /**
     * Check if a row exists (minimum 1 row)
     * @param sql    sql query
     * @param params optional sql parameters
     * @return true if at least one row exists
     * @throws DatabaseException if a database access error occurs, this method is called on a closed connection.
     */
    public boolean exists(String sql, Object...params) {
        List<Boolean> result = list(sql, (res) -> Boolean.TRUE, params, 1);
        return !result.isEmpty();
    }

    /**
     * Query data from the database
     * @param <T> mapped output type
     * @param sql sql query
     * @param mapper result mapper 
     * @param params optional params for query
     * @return list of results
     * @throws DatabaseException if a database access error occurs, this method is called on a closed connection.
     */
    public <T> List<T> list(String sql, ResultFunction<T> mapper, Object...params) {
        return list(sql, mapper, objectParameterSetter(params),0 );
    }

    /**
     * Query data from the database
     * @param <T> mapped output type
     * @param sql sql query
     * @param mapper result mapper 
     * @param parameterSetter lambda function that sets parameters
     * @param limit maximum number of results to fetch, 0 if unbounded/get all
     * @return list of results
     * @throws DatabaseException if a database access error occurs, this method is called on a closed connection.
     */
    public <T> List<T> list(String sql, ResultFunction<T> mapper, ParameterSetter parameterSetter, int limit) {
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            parameterSetter.accept(statement);

            ArrayList<T> data = new ArrayList<>();
            ResultSet result = statement.executeQuery();
            while(result.next() && (limit == 0 || data.size() < limit)) {
                data.add(mapper.apply(result));
            }

            result.close();
            return data;
        } catch (SQLException e) {
            throw new DatabaseException("failed to run query: " + sql, e);
        }
    }

    private void executeUpdate(String sql, ParameterSetter parameterSetters) {
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            parameterSetters.accept(statement);
            statement.executeUpdate();
        }
        catch(SQLIntegrityConstraintViolationException e) {
            throw new DatabaseValidationException(e);
        }
        catch(SQLException e) {
            throw new DatabaseException(e);
        }
    }

    /**
     * Simple insert into database
     * <b>Remarks:</b> Will use INSERT INTO table DEFAULT VALUES if map is empty.
     * @param table  table to insert into
     * @param values the values to insert (named columns to value)
     * @throws DatabaseException if a database access error occurs, this method is called on a closed connection.
     */
    public void insert(String table, Map<String,Object> values) {
        Objects.requireNonNull(table);
        Objects.requireNonNull(values);
        if(table.isEmpty()) {
            throw new IllegalArgumentException("table");
        }

        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO ").append(table);

        if(values.isEmpty()) {
            sb.append(" DEFAULT VALUES");
            executeUpdate(sb.toString(), (stmt) -> {});
        } else {
            List<Map.Entry<String,Object>> entries = new ArrayList<>(values.entrySet());
            entries.sort(Map.Entry.comparingByKey());

            sb.append("(");
            sb.append(entries.get(0).getKey());
            for(int i = 1 ; i < entries.size(); i++) {
                sb.append(",").append(entries.get(i).getKey());
            }
            sb.append(") VALUES (");

            sb.append("?");
            sb.append(",?".repeat(entries.size() - 1));
            sb.append(")");

            executeUpdate(sb.toString(), (PreparedStatement stmt) -> {
                for(int i = 0; i < entries.size(); i++) {
                    stmt.setObject(i+1, entries.get(i).getValue());
                }
            });
        }
    }

    /**
     * Simple insert into database
     * <b>Remarks:</b> Will use INSERT INTO table DEFAULT VALUES if map is empty.
     * @param table  table to insert into
     * @param values the values to insert (named columns to value)
     * @throws DatabaseException if a database access error occurs, this method is called on a closed connection.
     */
    public <T> T insertWithGenerated(String table, Map<String,Object> values, ResultFunction<T> generated) {
        Objects.requireNonNull(table);
        Objects.requireNonNull(values);
        if(table.isEmpty()) {
            throw new IllegalArgumentException("table");
        }

        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO ").append(table);

        List<Map.Entry<String,Object>> entries = Collections.emptyList();

        if(values.isEmpty()) {
            sb.append(" DEFAULT VALUES");
        } else {
            entries = new ArrayList<>(values.entrySet());
            entries.sort(Map.Entry.comparingByKey());

            sb.append("(");
            sb.append(entries.get(0).getKey());
            for(int i = 1 ; i < entries.size(); i++) {
                sb.append(",").append(entries.get(i).getKey());
            }
            sb.append(") VALUES (");

            sb.append("?");
            sb.append(",?".repeat(entries.size() - 1));
            sb.append(")");
        }

        try (PreparedStatement statement = conn.prepareStatement(sb.toString(), Statement.RETURN_GENERATED_KEYS)) {
            for(int i = 0; i < entries.size(); i++) {
                statement.setObject(i+1, entries.get(i).getValue());
            }

            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if(!generatedKeys.next()) {
                    throw new DatabaseException("expected generated keys - none given");
                }

                return generated.apply(generatedKeys);
            }
        }
        catch(SQLIntegrityConstraintViolationException e) {
            throw new DatabaseValidationException(e);
        }
        catch(SQLException e) {
            throw new DatabaseException(e);
        }
    }

    /**
     * Updates row in a table
     *
     * <p><b>Remarks:</b> table and column are inserted verbatim which means they are vulnerable to SQL inject attacks.
     * Hardcode these values, if that is not possible you must take necessary steps to validate input.
     * @param table table to update
     * @param key the row to select
     * @param updates changes to table
     * @return true if update was successful or no update required (updates is empty)
     * @throws DatabaseException if a database access error occurs, this method is called on a closed connection.
     */
    public boolean update(String table, Map<String,Object> key, Map<String,Object> updates) {
        Objects.requireNonNull(table);
        Objects.requireNonNull(key);
        Objects.requireNonNull(updates);

        if(key.isEmpty()) {
            throw new DatabaseException("missing a key for updating a row - required");
        }

        if(updates.isEmpty()) {
            // ignores one special case: that the row does not exist
            return true;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("UPDATE ").append(table).append(" SET ");

        ArrayList<Object> params = new ArrayList<>();

        // changes
        {
            var iter = updates.entrySet().iterator();

            var elem = iter.next();
            sb.append(elem.getKey()).append(" = ").append("?");
            params.add(elem.getValue());

            while (iter.hasNext()) {
                sb.append(",");
                elem = iter.next();
                sb.append(elem.getKey()).append(" = ").append("?");
                params.add(elem.getValue());
            }
        }

        sb.append(" WHERE ");

        // key
        {
            var iter = key.entrySet().iterator();
            var elem = iter.next();

            sb.append(elem.getKey()).append(" = ").append("?");
            params.add(elem.getValue());

            while (iter.hasNext()) {
                sb.append(" AND ");
                elem = iter.next();
                sb.append(elem.getKey()).append(" = ").append("?");
                params.add(elem.getValue());
            }
        }

        try (PreparedStatement updateStmt = conn.prepareStatement(sb.toString())) {
            for (int i = 0; i < params.size(); i++) {
                updateStmt.setObject(i + 1, params.get(i));
            }
            int changes = updateStmt.executeUpdate();

            return changes > 0;
        }
        catch (SQLException ex) {
            throw new DatabaseException("failed to update " + table, ex);
        }
    }

    /**
     * Deletes rows from a table
     * @param table table to delete from
     * @param key key to use to select rows
     * @return true if rows where deleted or false if no rows where deleted
     * @throws DatabaseException if a database access error occurs, this method is called on a closed connection.
     */
    public boolean delete(String table, Map<String,Object> key) {
        Objects.requireNonNull(table);
        Objects.requireNonNull(key);

        if(key.isEmpty()) {
            throw new DatabaseException("missing a key for updating a row - required");
        }

        StringBuilder sb = new StringBuilder();
        sb.append("DELETE FROM ").append(table).append(" WHERE ");

        ArrayList<Object> params = new ArrayList<>();

        // key
        var iter = key.entrySet().iterator();
        var elem = iter.next();

        sb.append(elem.getKey()).append(" = ").append("?");
        params.add(elem.getValue());

        while (iter.hasNext()) {
            sb.append(" AND ");
            elem = iter.next();
            sb.append(elem.getKey()).append(" = ").append("?");
            params.add(elem.getValue());
        }

        try (PreparedStatement updateStmt = conn.prepareStatement(sb.toString())) {
            for (int i = 0; i < params.size(); i++) {
                updateStmt.setObject(i + 1, params.get(i));
            }
            int changes = updateStmt.executeUpdate();

            return changes > 0;
        }
        catch (SQLException ex) {
            throw new DatabaseException("failed to delete from " + table, ex);
        }
    }

    /**
     * Prepare a custom query
     *
     * @param sql sql
     * @return prepared statement
     * @throws SQLException if a database access error occurs or this method is called on a closed connection
     */
    public PreparedStatement prepare(String sql) throws SQLException {
        return conn.prepareStatement(sql);
    }

    /**
     * Change current connection transaction isolation level to serializable
     *
     * @throws DatabaseException if a database access error occurs, this method is called on a closed connection.
     */
    public void setSerializedIsolation() {
        try {
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    /**
     * Manually close the connection. Only used for special cases.
     * @see Database#dispose(Context)
     * @throws SQLException if a database access error occurs
     */
    @Override
    public void close() throws SQLException {
        this.conn.close();
    }
}