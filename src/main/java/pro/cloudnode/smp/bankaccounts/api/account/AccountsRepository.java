/*
 * A Minecraft economy plugin that enables players to hold multiple bank accounts.
 * Copyright © 2023–2026 Cloudnode OÜ.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <https://www.gnu.org/licenses/>.
 */

package pro.cloudnode.smp.bankaccounts.api.account;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import pro.cloudnode.smp.bankaccounts.api.Repository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Optional;

/**
 * Represents the accounts repository.
 */
@ApiStatus.Internal
public final class AccountsRepository extends Repository<Account> {
    /**
     * Constructs an accounts repository with the specified data source.
     *
     * @param dataSource the database source
     */
    @ApiStatus.Internal
    public AccountsRepository(final @NotNull DataSource dataSource) {
        super(dataSource);
    }

    /**
     * Retrieves the account with the specified ID from the database.
     *
     * @param id the ID of the account to retrieve
     * @return the account if found, otherwise empty
     * @throws RepositoryException if an error occurs during query execution
     */
    @ApiStatus.Internal
    @NotNull
    public Optional<Account> getById(final @NotNull String id) throws RepositoryException {
        return queryOne("SELECT * FROM bank_accounts WHERE id = ?", stmt -> stmt.setString(1, id));
    }

    /**
     * Checks whether an account with the specified ID exists in the database.
     *
     * @param id the ID of the account to check
     * @return true if the account exists, false otherwise
     * @throws RepositoryException if an error occurs during query execution
     */
    @ApiStatus.Internal
    public boolean exists(final @NotNull String id) throws RepositoryException {
        return queryInt("SELECT 1 FROM bank_accounts WHERE id = ?", stmt -> stmt.setString(1, id)).orElse(0) > 0;
    }

    /**
     * Inserts a new account into the database.
     *
     * @param account the account to insert
     * @throws RepositoryException if an error occurs during query execution
     * @see #update
     */
    @ApiStatus.Internal
    public void insert(final @NotNull Account account) throws RepositoryException {
        queryUpdate(
                """
                        INSERT INTO bank_accounts (
                            id,
                            type,
                            allow_negative,
                            status,
                            name,
                            created
                        ) VALUES (?, ?, ?, ?, ?, ?)
                        """, stmt -> {
                    stmt.setString(1, account.id());
                    stmt.setInt(2, account.type().ordinal());
                    stmt.setBoolean(3, account.allowNegative());
                    stmt.setInt(4, account.status().ordinal());
                    stmt.setString(5, account.name().orElse(null));
                    stmt.setTimestamp(6, Timestamp.from(account.created()));
                }
        );
    }

    /**
     * Updates an existing account in the database.
     *
     * @param account the account to update
     * @throws IllegalStateException          if no rows were modified
     * @throws Repository.RepositoryException if an error occurs during query execution
     */
    @ApiStatus.Internal
    public void update(final @NotNull Account account) throws RepositoryException {
        final boolean updated = queryUpdate(
                "UPDATE bank_accounts SET allow_negative = ?, status = ?, name = ? WHERE id = ?", stmt -> {
                    stmt.setBoolean(1, account.allowNegative());
                    stmt.setInt(2, account.status().ordinal());
                    stmt.setString(3, account.name().orElse(null));
                    stmt.setString(4, account.id());
                }
        ) > 0;

        if (!updated) {
            throw new IllegalStateException(String.format("No rows were modified for account %s", account.id()));
        }
    }

    @ApiStatus.Internal
    @Override
    @NotNull
    protected Account map(final @NotNull ResultSet resultSet) throws SQLException {
        return new Account(
                resultSet.getString("id"),
                Account.Type.values()[resultSet.getInt("type")],
                resultSet.getBoolean("allow_negative"),
                Account.Status.values()[resultSet.getInt("status")],
                resultSet.getString("name"),
                resultSet.getTimestamp("created").toInstant()
        );
    }
}
