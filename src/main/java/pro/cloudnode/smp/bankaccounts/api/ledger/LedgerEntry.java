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

package pro.cloudnode.smp.bankaccounts.api.ledger;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

/**
 * Represents a ledger entry.
 */
public final class LedgerEntry {
    private final @NotNull String id;
    private final @NotNull String account;
    private final @NotNull BigDecimal amount;
    private final @NotNull BigDecimal balance;
    private final @NotNull Instant created;
    private final @NotNull LedgerEntry.Initiator initiator;
    private final @NotNull String channel;
    private final @Nullable String description;
    private final @Nullable String relatedAccount;
    private final @Nullable String previousId;

    LedgerEntry(
            final @NotNull String id,
            final @NotNull String account,
            final @NotNull BigDecimal amount,
            final @NotNull BigDecimal balance,
            final @NotNull Instant created,
            final @NotNull LedgerEntry.Initiator initiator,
            final @NotNull String channel,
            final @Nullable String description,
            final @Nullable String relatedAccount,
            final @Nullable String previousId
    ) {
        this.id = id;
        this.account = account;
        this.amount = amount;
        this.balance = balance;
        this.created = created;
        this.initiator = initiator;
        this.channel = channel;
        this.description = description;
        this.relatedAccount = relatedAccount;
        this.previousId = previousId;
    }

    /**
     * Returns the unique ledger entry identifier.
     *
     * @return the ledger entry ID
     */
    @NotNull
    public String id() {
        return id;
    }

    /**
     * Returns the ID of the account to which this ledger entry applies.
     *
     * @return the account ID
     */
    @NotNull
    public String account() {
        return account;
    }

    /**
     * Returns the amount of this ledger entry.
     *
     * @return the ledger entry amount
     */
    @NotNull
    public BigDecimal amount() {
        return amount;
    }


    /**
     * Returns the account balance of the account after applying this ledger entry.
     *
     * @return the account balance
     */
    @NotNull
    public BigDecimal balance() {
        return balance;
    }

    /**
     * Returns the timestamp when this ledger entry was created.
     *
     * @return the ledger entry creation timestamp
     */
    @NotNull
    public Instant created() {
        return created;
    }

    /**
     * Returns the initiator of the transaction of this ledger entry.
     *
     * @return the ledger entry initiator
     */
    @NotNull
    public LedgerEntry.Initiator initiator() {
        return initiator;
    }


    /**
     * Returns the identifier of the initiator channel.
     *
     * @return the initiator channel ID
     */
    @NotNull
    public String channel() {
        return channel;
    }

    /**
     * Returns the description of this ledger entry.
     *
     * @return the ledger entry description if set, empty otherwise
     */
    @NotNull
    public Optional<String> description() {
        return Optional.ofNullable(description);
    }

    /**
     * Returns the related account identifier of this ledger entry.
     *
     * @return the related account ID if the account exists, empty otherwise
     */
    @NotNull
    public Optional<String> relatedAccount() {
        return Optional.ofNullable(relatedAccount);
    }

    /**
     * Returns the ID of the previous ledger entry for this account.
     *
     * @return the previous ledger entry ID, or empty if this is the first entry
     */
    @NotNull
    public Optional<String> previousId() {
        return Optional.ofNullable(previousId);
    }

    /**
     * Indicates the initiator of a transaction.
     */
    public enum Initiator {
        /**
         * Direct transfer initiated by an account holder.
         */
        USER,

        /**
         * Transaction initiated by a third-party integration.
         */
        INTEGRATION
    }
}
