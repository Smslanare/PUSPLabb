package etsf20.basesystem.persistance;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Functional interface to map a row from a result set into a concrete model instance
 * @param <T> model instance type
 */
@FunctionalInterface
public interface ResultFunction<T> {
    /**
     * Applies this function to the given argument.
     * <p><b>Remarks:</b> implementation should only get data from result set,
     * state is managed by caller, e.g `next()` should never be called.
     *
     * @param rs the function argument
     * @return the function result
     */
    T apply(ResultSet rs) throws SQLException;
}
