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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.Optional;

/**
 * Represents a bank account.
 */
public final class Account {
    private final @NotNull String id;
    private final @NotNull Type type;
    private final @NotNull Instant created;
    private boolean allowNegative;
    private @NotNull Status status;
    private @Nullable String name;

    Account(
            final @NotNull String id,
            final @NotNull Type type,
            final boolean allowNegative,
            final @NotNull Status status,
            final @Nullable String name,
            final @NotNull Instant created
    ) {
        this.id = id;
        this.type = type;
        this.allowNegative = allowNegative;
        this.status = status;
        this.name = name;
        this.created = created;
    }

    /**
     * Returns the unique account identifier.
     *
     * @return the account ID
     */
    @NotNull
    public String id() {
        return id;
    }

    /**
     * Returns the type of the account.
     *
     * @return the account type
     */
    @NotNull
    public Type type() {
        return type;
    }

    /**
     * Returns whether the account balance of this account is allowed to be negative.
     *
     * @return true if the account balance can be negative, false otherwise
     */
    public boolean allowNegative() {
        return allowNegative;
    }

    /**
     * Sets whether the balance of this account is allowed to be negative.
     *
     * @param allowNegative true to allow negative balance, false to disallow
     */
    public void allowNegative(final boolean allowNegative) {
        this.allowNegative = allowNegative;
    }

    /**
     * Returns the status of the account.
     *
     * @return the account status
     */
    @NotNull
    public Status status() {
        return status;
    }

    /**
     * Sets the status of the account.
     *
     * @param status the new account status
     */
    public void status(final @NotNull Status status) {
        this.status = status;
    }

    /**
     * Returns the name given to the account by the holder.
     * <p>
     * The name of {@link Type#PERSONAL} accounts should be visible to the account holder only. For
     * {@link Type#INTEGRATION} accounts, the name of the holder should be used instead. The names of
     * {@link Type#BUSINESS} accounts should be public.
     *
     * @return the account name if set, empty otherwise
     */
    @NotNull
    public Optional<String> name() {
        return Optional.ofNullable(name);
    }

    /**
     * Sets the name of the account.
     *
     * @param name the new account name, or null to clear the name
     */
    public void name(final @Nullable String name) {
        this.name = name;
    }

    /**
     * Returns the timestamp when the account was created.
     *
     * @return the account creation timestamp
     */
    @NotNull
    public Instant created() {
        return created;
    }

    /**
     * Returns whether this account is frozen.
     * <p>
     * When frozen, the account cannot be debited or credited.
     *
     * @return true if the account is frozen, false otherwise
     */
    public boolean frozen() {
        return status == Status.FROZEN_VOLUNTARY || status == Status.FROZEN;
    }

    /**
     * Represents an account type.
     */
    public enum Type {
        /**
         * Personal account.
         */
        PERSONAL,

        /**
         * Business account.
         */
        BUSINESS,

        /**
         * Account for third-party integrations which have no concept of accounts, one per holder.
         */
        INTEGRATION
    }

    /**
     * Represents account status.
     */
    public enum Status {
        /**
         * Account is active.
         */
        ACTIVE,

        /**
         * Account is frozen voluntarily and can be unfrozen by the holder.
         */
        FROZEN_VOLUNTARY,

        /**
         * Account is frozen and can only be unfrozen by the bank.
         */
        FROZEN
    }
}
