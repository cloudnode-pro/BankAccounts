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

package pro.cloudnode.smp.bankaccounts.api.account;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pro.cloudnode.smp.bankaccounts.api.IdGenerator;
import pro.cloudnode.smp.bankaccounts.api.Repository;
import pro.cloudnode.smp.bankaccounts.api.Service;

import javax.sql.DataSource;
import java.time.Instant;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents the accounts service.
 */
public final class AccountsService extends Service {
    /**
     * Maximum length of an account ID.
     */
    public static final int MAX_ID_LENGTH = 32;
    private final @NotNull AccountsRepository repository;
    private final int idLength;

    /**
     * Constructs a new accounts service.
     *
     * @param parentLogger the parent logger, or null if none
     * @param dataSource   the database source
     * @param idLength     the length of account IDs
     */
    @ApiStatus.Internal
    public AccountsService(
            final @Nullable Logger parentLogger,
            final @NotNull DataSource dataSource,
            final int idLength
    ) {
        super(parentLogger);
        this.repository = new AccountsRepository(dataSource);

        if (idLength <= 0 || idLength > MAX_ID_LENGTH) {
            throw new IllegalArgumentException(String.format(
                    "Account ID length must be between 1 and %d characters",
                    MAX_ID_LENGTH
            ));
        }
        this.idLength = idLength;
    }

    /**
     * Creates and persists an account with the specified ID.
     *
     * @param id   the account ID; must not exceed {@link #MAX_ID_LENGTH} characters and must be unique
     * @param type the account type
     * @return the created account
     * @throws IllegalArgumentException if the ID exceeds the maximum length or is already taken
     * @throws InternalException        if the account cannot be persisted due to an internal error
     */
    @NotNull
    public Account create(final @NotNull String id, final @NotNull Account.Type type) throws InternalException {
        if (id.length() > MAX_ID_LENGTH) {
            throw new IllegalArgumentException(String.format("Account ID cannot exceed %d characters", MAX_ID_LENGTH));
        }

        try {
            if (repository.exists(id)) {
                throw new IllegalArgumentException(String.format("Account with ID %s already exists", id));
            }
        } catch (final Repository.RepositoryException e) {
            logger.log(Level.SEVERE, String.format("Failed to check account existence for ID: %s", id), e);
            throw new InternalException("Failed to check for ID collision");
        }

        final Account account = new Account(id, type, false, Account.Status.ACTIVE, null, Instant.now());
        try {
            repository.insert(account);
            return account;
        } catch (final Repository.RepositoryException e) {
            logger.log(Level.SEVERE, "Failed to insert account", e);
            throw new InternalException("Failed to create account");
        }
    }

    /**
     * Creates and persists an account with a random ID.
     *
     * @param type the account type
     * @return the created account
     * @throws IllegalStateException if a unique ID cannot be generated within 5 attempts
     * @throws InternalException     if the account cannot be persisted due to an internal error
     */
    @NotNull
    public Account create(final @NotNull Account.Type type) throws IllegalStateException, InternalException {
        int attempt = 0;
        while (true) {
            String id = IdGenerator.BASE58.random(idLength);
            try {
                if (!repository.exists(id)) {
                    return create(id, type);
                }
            } catch (final Repository.RepositoryException e) {
                logger.log(
                        Level.SEVERE,
                        String.format("Failed to check account existence during ID generation for ID: %s", id),
                        e
                );
                throw new InternalException("Failed to check for ID collision");
            }

            if (attempt >= 5) {
                throw new IllegalStateException(String.format(
                        "Could not generate unique account ID after %d attempts",
                        attempt
                ));
            }
            ++attempt;
        }
    }

    /**
     * Retrieves the account with the specified ID.
     *
     * @param id the ID of the account to retrieve
     * @return the account if found, empty otherwise
     * @throws InternalException if the account cannot be retrieved due to an internal error
     */
    @NotNull
    public Optional<Account> get(final @NotNull String id) throws InternalException {
        try {
            return repository.getById(id);
        } catch (final Repository.RepositoryException e) {
            logger.log(Level.SEVERE, String.format("Failed to retrieve account with ID: %s", id), e);
            throw new InternalException("Failed to retrieve account");
        }
    }


    /**
     * Retrieves the account with the specified ID if it is not frozen.
     *
     * @param accountId the ID of the account to retrieve
     * @return the account if found and not frozen
     * @throws AccountNotFoundException if no account exists with the given ID
     * @throws AccountFrozenException   if the account is frozen
     * @throws InternalException        if the account cannot be retrieved due to an internal error
     */
    @NotNull
    public Account getNonFrozenAccountOrThrow(String accountId)
            throws AccountNotFoundException, AccountFrozenException, InternalException {
        final Account account = get(accountId).orElseThrow(() -> new AccountNotFoundException(accountId));
        if (account.frozen()) {
            throw new AccountFrozenException(accountId);
        }
        return account;
    }

    /**
     * Indicates that an account does not exist.
     */
    public static final class AccountNotFoundException extends ServiceException {
        /**
         * Constructs a new exception for the specified account ID.
         *
         * @param id the account ID
         */
        @ApiStatus.Internal
        public AccountNotFoundException(final @NotNull String id) {
            super(String.format("Account %s does not exist", id));
        }
    }

    /**
     * Indicates that an account is frozen.
     */
    public static final class AccountFrozenException extends ServiceException {
        /**
         * Constructs a new exception for the specified account ID.
         *
         * @param id the account ID
         */
        @ApiStatus.Internal
        public AccountFrozenException(final @NotNull String id) {
            super(String.format("Account %s is frozen", id));
        }
    }
}
