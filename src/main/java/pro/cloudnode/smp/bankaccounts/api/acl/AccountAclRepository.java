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

package pro.cloudnode.smp.bankaccounts.api.acl;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import pro.cloudnode.smp.bankaccounts.api.Repository;
import pro.cloudnode.smp.bankaccounts.api.TypedIdentifier;
import pro.cloudnode.smp.bankaccounts.api.account.AccountId;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

/**
 * Represents the account access control list repository.
 */
@ApiStatus.Internal
public final class AccountAclRepository extends Repository<AccountAclEntry> {
    /**
     * Constructs an account ACL repository with the specified data source.
     *
     * @param dataSource the database source
     */
    @ApiStatus.Internal
    public AccountAclRepository(final @NotNull DataSource dataSource) {
        super(dataSource);
    }

    /**
     * Retrieves the ACL entry for the specified subject and account.
     *
     * @param subject the subject identifier
     * @param account the account ID
     * @return the ACL entry if found, otherwise empty
     * @throws RepositoryException if an error occurs during query execution
     */
    @ApiStatus.Internal
    @NotNull
    public Optional<AccountAclEntry> get(final @NotNull TypedIdentifier subject, final @NotNull String account) {
        return queryOne(
                "SELECT * FROM bank_account_acl_entries WHERE subject_type = ? AND subject_id = ? AND account = ?",
                stmt -> {
                    stmt.setString(1, subject.type().name());
                    stmt.setString(2, subject.id());
                    stmt.setString(3, account);
                }
        );
    }

    /**
     * Retrieves all ACL entries for a given account.
     *
     * @param account the account ID
     * @return the list of ACL entries
     * @throws RepositoryException if an error occurs during query execution
     */
    @ApiStatus.Internal
    @NotNull
    public List<AccountAclEntry> getAllForAccount(final @NotNull String account) {
        return queryMany(
                "SELECT * FROM bank_account_acl_entries WHERE account = ?",
                stmt -> stmt.setString(1, account)
        );
    }

    /**
     * Retrieves all ACL entries for a given subject.
     *
     * @param subject the subject identifier
     * @return the list of ACL entries
     * @throws RepositoryException if an error occurs during query execution
     */
    @ApiStatus.Internal
    @NotNull
    public List<AccountAclEntry> getAllForSubject(final @NotNull TypedIdentifier subject) {
        return queryMany(
                "SELECT * FROM bank_account_acl_entries WHERE subject_type = ? AND subject_id = ?", stmt -> {
                    stmt.setString(1, subject.type().name());
                    stmt.setString(2, subject.id());
                }
        );
    }

    /**
     * Inserts a new ACL entry into the database.
     *
     * @param entry the ACL entry to insert
     * @throws RepositoryException if an error occurs during query execution
     */
    @ApiStatus.Internal
    public void insert(final @NotNull AccountAclEntry entry) {
        queryUpdate(
                """
                        INSERT INTO bank_account_acl_entries (
                            subject_type,
                            subject_id,
                            relation,
                            account,
                            created
                        ) VALUES (?, ?, ?, ?, ?)
                        """, stmt -> {
                    stmt.setString(1, entry.subject().type().name());
                    stmt.setString(2, entry.subject().id());
                    stmt.setString(3, entry.relation().serialize());
                    stmt.setString(4, entry.account().id());
                    stmt.setTimestamp(5, Timestamp.from(entry.created()));
                }
        );
    }

    /**
     * Updates an existing ACL entry in the database.
     *
     * @param entry the ACL entry to update
     * @throws IllegalStateException          if no rows were modified
     * @throws Repository.RepositoryException if an error occurs during query execution
     */
    @ApiStatus.Internal
    public void update(final @NotNull AccountAclEntry entry) {
        final boolean updated = queryUpdate(
                """
                        UPDATE bank_account_acl_entries
                        SET relation = ?
                        WHERE subject_type = ? AND subject_id = ? AND account = ?
                        """, stmt -> {
                    stmt.setString(1, entry.relation().serialize());
                    stmt.setString(2, entry.subject().type().name());
                    stmt.setString(3, entry.subject().id());
                    stmt.setString(4, entry.account().id());
                }
        ) > 0;

        if (!updated) {
            throw new IllegalStateException(String.format(
                    "No rows were modified for subject %s on account %s",
                    entry.subject().id(),
                    entry.account()
            ));
        }
    }

    /**
     * Removes an ACL entry from the database.
     *
     * @param subject   the subject identifier
     * @param accountId the account ID
     * @throws RepositoryException if an error occurs during query execution
     */
    @ApiStatus.Internal
    public void remove(final @NotNull TypedIdentifier subject, final @NotNull String accountId) {
        queryUpdate(
                "DELETE FROM bank_account_acl_entries WHERE subject_type = ? AND subject_id = ? AND account = ?",
                stmt -> {
                    stmt.setString(1, subject.type().name());
                    stmt.setString(2, subject.id());
                    stmt.setString(3, accountId);
                }
        );
    }

    @ApiStatus.Internal
    @Override
    @NotNull
    protected AccountAclEntry map(final @NotNull ResultSet resultSet) throws SQLException {
        return new AccountAclEntry(
                new TypedIdentifier(
                        TypedIdentifier.Type.valueOf(resultSet.getString("subject_type")),
                        resultSet.getString("subject_id")
                ),
                AccountAclEntry.Role.valueOf(resultSet.getString("relation").toUpperCase()),
                new AccountId(resultSet.getString("account")),
                resultSet.getTimestamp("created").toInstant()
        );
    }
}
