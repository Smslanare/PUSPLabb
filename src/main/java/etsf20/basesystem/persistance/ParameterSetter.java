package etsf20.basesystem.persistance;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Functional interface for setting parameters for a prepared statement
 */
@FunctionalInterface
public interface ParameterSetter {
    /**
     * Applies this function to the given argument.
     *
     * @param stmt Statement to set parameters for
     */
    void accept(PreparedStatement stmt) throws SQLException;
}
