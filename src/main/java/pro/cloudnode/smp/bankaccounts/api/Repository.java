package pro.cloudnode.smp.bankaccounts.api;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * Represents a database repository for persistent resources.
 *
 * @param <T> the type of resource managed by this repository
 */
@ApiStatus.Internal
public abstract class Repository<T> {
    private final @NotNull DataSource dataSource;

    /**
     * Constructs a repository with the specified data source.
     *
     * @param dataSource the database source
     */
    @ApiStatus.Internal
    protected Repository(final @NotNull DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Maps the current row of a {@link ResultSet} to a resource.
     *
     * @param resultSet the result set positioned at the row to map
     * @return the mapped resource
     * @throws SQLException if an error occurs during mapping
     */
    @ApiStatus.Internal
    @NotNull
    protected abstract T map(final @NotNull ResultSet resultSet) throws SQLException;


    /**
     * Executes a single update statement.
     *
     * @param sql    the SQL statement
     * @param binder binds parameters to the statement
     * @return the number of affected rows
     * @throws RepositoryException if an error occurs during execution
     */
    @ApiStatus.Internal
    protected final int queryUpdate(final @NotNull String sql, final @NotNull Binder binder)
            throws RepositoryException {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement stmt = conn.prepareStatement(sql)) {
            binder.bind(stmt);

            return stmt.executeUpdate();
        } catch (final SQLException e) {
            throw new RepositoryException(e);
        }
    }

    /**
     * Executes a batch of update statements atomically.
     *
     * @param sql     the SQL statement
     * @param binders a list of binders for each batch entry
     * @return an array containing the update counts for each batch entry
     * @throws RepositoryException if an error occurs during execution
     */
    @ApiStatus.Internal
    protected final int[] queryUpdateBatch(final @NotNull String sql, final @NotNull List<@NotNull Binder> binders)
            throws RepositoryException {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement stmt = conn.prepareStatement(sql)) {
            final boolean auto = conn.getAutoCommit();
            conn.setAutoCommit(false);

            try {
                for (final Binder binder : binders) {
                    binder.bind(stmt);
                    stmt.addBatch();
                }
                return stmt.executeBatch();
            } catch (final SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(auto);
            }
        } catch (final SQLException e) {
            throw new RepositoryException(e);
        }
    }

    /**
     * Queries a single resource.
     *
     * @param sql    the SQL query
     * @param binder binds parameters to the query
     * @return the resource if found, empty otherwise
     * @throws RepositoryException if an error occurs during execution
     */
    @ApiStatus.Internal
    @NotNull
    protected final Optional<T> queryOne(final @NotNull String sql, final @NotNull Binder binder)
            throws RepositoryException {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement stmt = conn.prepareStatement(sql)) {
            binder.bind(stmt);

            try (final ResultSet resultSet = stmt.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(map(resultSet));
                }
                return Optional.empty();
            }
        } catch (final SQLException e) {
            throw new RepositoryException(e);
        }
    }

    /**
     * Queries multiple resources.
     *
     * @param sql    the SQL query
     * @param binder binds parameters to the query
     * @return a list of resources matching the query
     * @throws RepositoryException if an error occurs during execution
     */
    @ApiStatus.Internal
    @NotNull
    protected final List<T> queryMany(final @NotNull String sql, final @NotNull Binder binder)
            throws RepositoryException {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement stmt = conn.prepareStatement(sql)) {
            binder.bind(stmt);

            try (final ResultSet resultSet = stmt.executeQuery()) {
                final List<T> results = new java.util.ArrayList<>();
                while (resultSet.next()) {
                    results.add(map(resultSet));
                }
                return results;
            }
        } catch (final SQLException e) {
            throw new RepositoryException(e);
        }
    }

    /**
     * Queries an integer scalar.
     *
     * @param sql    the SQL query
     * @param binder binds parameters to the query
     * @return the integer result if present, empty otherwise
     * @throws RepositoryException if an error occurs during execution
     */
    @ApiStatus.Internal
    @NotNull
    protected final OptionalInt queryInt(final @NotNull String sql, final @NotNull Binder binder)
            throws RepositoryException {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement stmt = conn.prepareStatement(sql)) {
            binder.bind(stmt);

            try (final ResultSet resultSet = stmt.executeQuery()) {
                if (resultSet.next()) {
                    return OptionalInt.of(resultSet.getInt(1));
                }
                return OptionalInt.empty();
            }
        } catch (final SQLException e) {
            throw new RepositoryException(e);
        }
    }

    /**
     * Binds parameters to a {@link PreparedStatement}.
     */
    @ApiStatus.Internal
    protected interface Binder {
        /**
         * Binds parameters to the statement.
         * @param stmt the statement to bind parameters to
         * @throws SQLException if an error occurs while binding parameters
         */
        @ApiStatus.Internal
        void bind(final @NotNull PreparedStatement stmt) throws SQLException;
    }

    /**
     * Indicates an error during repository operation.
     */
    @ApiStatus.Internal
    public static class RepositoryException extends RuntimeException {
        /**
         * Constructs a new exception with the specified cause.
         *
         * @param cause the cause of the exception
         */
        @ApiStatus.Internal
        public RepositoryException(final @NotNull Throwable cause) {
            super(cause);
        }
    }
}
