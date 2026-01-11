/*
 * BankAccounts is a Minecraft economy plugin that enables players to hold multiple bank accounts.
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

package pro.cloudnode.smp.bankaccounts.api.ledger;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import pro.cloudnode.smp.bankaccounts.api.Repository;
import pro.cloudnode.smp.bankaccounts.api.TypedIdentifier;
import pro.cloudnode.smp.bankaccounts.api.account.AccountId;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Represents the ledger repository.
 */
@ApiStatus.Internal
public final class LedgerRepository extends Repository<LedgerEntry> {
    /**
     * Constructs a new ledger repository with the specified data source.
     *
     * @param dataSource the database source
     */
    @ApiStatus.Internal
    public LedgerRepository(final @NotNull DataSource dataSource) {
        super(dataSource);
    }

    /**
     * Retrieves the ledger entry with the specified ID from the database.
     *
     * @param id the ID of the ledger entry to retrieve
     * @return the ledger entry if found, otherwise empty
     * @throws RepositoryException if an error occurs during query execution
     */
    @ApiStatus.Internal
    @NotNull
    public Optional<LedgerEntry> getById(final @NotNull String id) throws RepositoryException {
        return queryOne("SELECT * FROM bank_ledger_entries WHERE id = ?", stmt -> stmt.setString(1, id));
    }

    /**
     * Retrieves the latest ledger entries for a given account from the database.
     *
     * @param account the account ID
     * @param page    the page number, starting from 1
     * @param limit   the maximum number of ledger entries to return per page
     * @return a list of ledger entries ordered from newest to oldest
     * @throws IllegalArgumentException if {@code page} or {@code limit} are less than 1
     * @throws RepositoryException      if an error occurs during query execution
     */
    @ApiStatus.Internal
    public List<LedgerEntry> getLatest(final @NotNull String account, final int page, final int limit)
            throws RepositoryException {

        if (page < 1) {
            throw new IllegalArgumentException("Page must be >= 1");
        }
        if (limit < 1) {
            throw new IllegalArgumentException("Limit must be >= 1");
        }

        return queryMany(
                """
                        SELECT *
                        FROM bank_ledger_entries
                        WHERE account = ?
                        ORDER BY created DESC
                        OFFSET ? ROWS
                        FETCH NEXT ? ROWS ONLY
                        """, stmt -> {
                    stmt.setString(1, account);
                    stmt.setInt(2, (page - 1) * limit);
                    stmt.setInt(3, limit);
                }
        );
    }

    /**
     * Retrieves the latest ledger entries for a given account from the database, starting from the first page.
     *
     * @param account the account ID.
     * @param limit   the maximum number of ledger entries to return per page
     * @return a list of ledger entries ordered from newest to oldest
     * @throws IllegalArgumentException if {@code limit} is less than 1
     * @throws RepositoryException      if an error occurs during query execution
     */
    @ApiStatus.Internal
    public List<LedgerEntry> getLatest(final @NotNull String account, final int limit) throws RepositoryException {
        return getLatest(account, 1, limit);
    }

    /**
     * Checks whether a ledger entry with the specified ID exists in the database.
     *
     * @param id the ledger entry ID to check
     * @return true if the ledger entry exists, false otherwise
     * @throws RepositoryException if an error occurs during query execution
     */
    @ApiStatus.Internal
    public boolean exists(final @NotNull String id) throws RepositoryException {
        return queryInt("SELECT 1 FROM bank_ledger_entries WHERE id = ?", stmt -> stmt.setString(1, id)).orElse(0) > 0;
    }

    /**
     * Appends a transaction into the database.
     *
     * @param transaction the transaction to append
     * @throws ConcurrencyException if the transaction precondition cannot be satisfied due to a concurrent update of
     *                              the ledger
     * @throws RepositoryException  if an error occurs during query execution
     */
    @ApiStatus.Internal
    public void append(final @NotNull Transaction transaction) throws RepositoryException {
        try {
            queryUpdateBatch(
                    """
                            INSERT INTO bank_ledger_entries (
                                id,
                                account,
                                amount,
                                balance,
                                created,
                                initiator,
                                channel,
                                description,
                                related_account,
                                previous_id
                            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                            """,
                    Stream.of(transaction.debit(), transaction.credit()).<Binder>map((ledgerEntry) -> (stmt) -> {
                        stmt.setString(1, ledgerEntry.id());
                        stmt.setString(2, ledgerEntry.account().id());
                        stmt.setBigDecimal(3, ledgerEntry.amount());
                        stmt.setBigDecimal(4, ledgerEntry.balance());
                        stmt.setTimestamp(5, Timestamp.from(ledgerEntry.created()));
                        stmt.setInt(6, ledgerEntry.initiator().ordinal());
                        stmt.setString(7, ledgerEntry.channel());
                        stmt.setString(8, ledgerEntry.description().orElse(null));
                        stmt.setString(9, ledgerEntry.relatedAccount().map(TypedIdentifier::id).orElse(null));
                        stmt.setString(10, ledgerEntry.previousId().orElse(null));
                    }).toList()
            );
        } catch (final RepositoryException e) {
            if (e.getCause() instanceof SQLIntegrityConstraintViolationException) {
                throw new ConcurrencyException(e.getCause());
            }
            throw e;
        }
    }

    @ApiStatus.Internal
    @Override
    @NotNull
    protected LedgerEntry map(final @NotNull ResultSet resultSet) throws SQLException {
        return new LedgerEntry(
                resultSet.getString("id"),
                new AccountId(resultSet.getString("account")),
                resultSet.getBigDecimal("amount"),
                resultSet.getBigDecimal("balance"),
                resultSet.getTimestamp("created").toInstant(),
                LedgerEntry.Initiator.values()[resultSet.getInt("initiator")],
                resultSet.getString("channel"),
                resultSet.getString("description"),
                new AccountId(resultSet.getString("related_account")),
                resultSet.getString("previous_id")
        );
    }

    /**
     * Indicates a ledger concurrency error.
     */
    public static final class ConcurrencyException extends RepositoryException {
        private ConcurrencyException(final @NotNull Throwable cause) {
            super(cause);
        }
    }
}
