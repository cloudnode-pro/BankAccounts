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
import org.jetbrains.annotations.Nullable;
import pro.cloudnode.smp.bankaccounts.api.IdGenerator;
import pro.cloudnode.smp.bankaccounts.api.Repository;
import pro.cloudnode.smp.bankaccounts.api.Service;
import pro.cloudnode.smp.bankaccounts.api.account.Account;
import pro.cloudnode.smp.bankaccounts.api.account.AccountsService;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents the ledger service.
 */
public final class LedgerService extends Service {
    /**
     * Maximum length of a ledger entry ID.
     */
    public static final int MAX_ID_LENGTH = 32;
    private final @NotNull LedgerRepository repository;
    private final @NotNull AccountsService accounts;
    private final int idLength;

    /**
     * Constructs a new ledger service.
     *
     * @param parentLogger the parent logger, or null if none
     * @param dataSource   the database source
     * @param accounts     the accounts service
     * @param idLength     the length of ledger entry IDs
     */
    @ApiStatus.Internal
    public LedgerService(
            final @Nullable Logger parentLogger,
            final @NotNull DataSource dataSource,
            final @NotNull AccountsService accounts,
            final int idLength
    ) {
        super(parentLogger);
        this.repository = new LedgerRepository(dataSource);
        this.accounts = accounts;

        if (idLength <= 0 || idLength > MAX_ID_LENGTH) {
            throw new IllegalArgumentException(String.format("Ledger entry ID length must be between 1 and %d characters",
                    MAX_ID_LENGTH
            ));
        }
        this.idLength = idLength;
    }

    /**
     * Retrieves the last ledger entry for the specified account.
     *
     * @param account the account ID
     * @return the most recent ledger entry for the account, or empty if none exist
     * @throws InternalException if the ledger could not be queried due to an internal error
     */
    @NotNull
    public Optional<LedgerEntry> getLast(final @NotNull String account) throws InternalException {
        final List<LedgerEntry> latest;
        try {
            latest = repository.getLatest(account, 1);
        } catch (final Repository.RepositoryException e) {
            logger.log(Level.SEVERE, String.format("Failed to get last ledger entry for account %s", account), e);
            throw new InternalException("Failed to get ledger entry");
        }

        if (latest.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(latest.get(0));
    }

    /**
     * Retrieves the running balance of the specified account.
     *
     * @param account the account ID
     * @return the current account balance, or {@code BigDecimal.ZERO} if the account does not exist or has no ledger
     * entries
     * @throws InternalException if the balance could not be queried due to an internal error
     * @see LedgerEntry#balance()
     * @see #getLast(String)
     */
    @NotNull
    public BigDecimal getBalance(final @NotNull String account) throws InternalException {
        return getLast(account).map(LedgerEntry::balance).orElse(BigDecimal.ZERO);
    }

    /**
     * Creates a transfer by appending both a debit and credit entry into the ledger.
     *
     * @param sender      the ID of the account to be debited
     * @param recipient   the ID of the account to be credited
     * @param amount      the positive amount to transfer
     * @param initiator   the initiator of the transaction
     * @param channel     the initiator channel identifier
     * @param description a description for the transaction
     * @return the created transaction
     * @throws InvalidTransferAmountException           if {@code amount} is not positive
     * @throws AccountsService.AccountNotFoundException if either account does not exist.
     * @throws AccountsService.AccountFrozenException   if either account is frozen.
     * @throws InsufficientBalanceException             if the sender account balance is less than {@code amount} and
     *                                                  overdraft is not allowed
     * @throws InternalException                        if the transfer could not be created due to an internal error
     */
    @NotNull
    public Transaction createTransfer(
            final @NotNull String sender,
            final @NotNull String recipient,
            final @NotNull BigDecimal amount,
            final @NotNull LedgerEntry.Initiator initiator,
            final @NotNull String channel,
            final @Nullable String description
    ) throws InvalidTransferAmountException, AccountsService.AccountNotFoundException,
            AccountsService.AccountFrozenException, InsufficientBalanceException, InternalException {

        if (amount.signum() <= 0) {
            throw new InvalidTransferAmountException(amount);
        }

        final String debitId = generateId();
        final String creditId = generateId();

        final int maxAttempts = 5;
        int attempt = 0;

        while (true) {
            ++attempt;

            final Account debitAccount = accounts.getNonFrozenAccountOrThrow(sender);

            final Optional<LedgerEntry> lastDebit = getLast(sender);
            final BigDecimal senderBalance = lastDebit.map(LedgerEntry::balance).orElse(BigDecimal.ZERO);

            if (!debitAccount.allowNegative() && senderBalance.compareTo(amount) < 0) {
                throw new InsufficientBalanceException(sender, senderBalance, amount);
            }

            final Account creditAccount = accounts.getNonFrozenAccountOrThrow(recipient);

            final Optional<LedgerEntry> lastCredit = getLast(recipient);
            final BigDecimal recipientBalance = lastCredit.map(LedgerEntry::balance).orElse(BigDecimal.ZERO);

            final Instant now = Instant.now();

            final LedgerEntry debit = new LedgerEntry(
                    debitId,
                    debitAccount.id(),
                    amount.negate(),
                    senderBalance.subtract(amount),
                    now,
                    initiator,
                    channel,
                    description,
                    recipient,
                    lastDebit.map(LedgerEntry::id).orElse(null)
            );

            final LedgerEntry credit = new LedgerEntry(
                    creditId,
                    creditAccount.id(),
                    amount,
                    recipientBalance.add(amount),
                    now,
                    initiator,
                    channel,
                    description,
                    sender,
                    lastCredit.map(LedgerEntry::id).orElse(null)
            );

            final Transaction transaction = new Transaction(debit, credit);

            try {
                repository.append(transaction);
            } catch (final LedgerRepository.ConcurrencyException e) {
                if (attempt > maxAttempts) {
                    break;
                }
                logger.log(
                        Level.INFO,
                        String.format(
                                "Ledger head advanced concurrently; retrying transfer %d/%d",
                                attempt,
                                maxAttempts
                        ),
                        e
                );
                continue;
            } catch (final Repository.RepositoryException e) {
                logger.log(
                        Level.SEVERE,
                        String.format(
                                "Failed to insert transaction %s -> %s: %s",
                                sender,
                                recipient,
                                amount.toPlainString()
                        ),
                        e
                );
                throw new InternalException("Failed to create transaction");
            }

            return transaction;
        }

        logger.severe(String.format(
                "Forward progress failure: ledger append precondition could not be satisfied after %d attempts",
                attempt
        ));
        throw new InternalException("Failed to create transaction");
    }

    @NotNull
    private String generateId() throws IllegalStateException, InternalException {
        int attempt = 0;
        while (true) {
            String id = IdGenerator.BASE58.random(idLength);
            try {
                if (!repository.exists(id)) {
                    return id;
                }
            } catch (final Repository.RepositoryException e) {
                logger.log(
                        Level.SEVERE,
                        String.format("Failed to check transaction existence during ID generation for ID: %s", id),
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
     * Indicates that the amount specified for a transfer is not positive.
     */
    public static final class InvalidTransferAmountException extends ServiceException {
        /**
         * Amount which is invalid.
         */
        final @NotNull BigDecimal amount;

        private InvalidTransferAmountException(final @NotNull BigDecimal amount) {
            super(String.format("Non-positive amount: %s", amount.toPlainString()));
            this.amount = amount;
        }
    }

    /**
     * Indicates that an account cannot be debited due to insufficient funds.
     */
    public static final class InsufficientBalanceException extends ServiceException {
        /**
         * ID of the account which has insufficient funds.
         */
        public final @NotNull String account;

        /**
         * Balance of the account.
         */
        public final @NotNull BigDecimal balance;

        /**
         * Amount of the transaction.
         */
        public final @NotNull BigDecimal amount;

        private InsufficientBalanceException(
                final @NotNull String account,
                final @NotNull BigDecimal balance,
                final @NotNull BigDecimal amount
        ) {
            super(String.format(
                    "Insufficient funds in account %s: %s < %s",
                    account,
                    balance.toPlainString(),
                    amount.toPlainString()
            ));
            this.account = account;
            this.balance = balance;
            this.amount = amount;
        }
    }
}
