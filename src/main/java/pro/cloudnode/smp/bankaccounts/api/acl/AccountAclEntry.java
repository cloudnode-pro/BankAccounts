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

import org.jetbrains.annotations.NotNull;
import pro.cloudnode.smp.bankaccounts.api.TypedIdentifier;
import pro.cloudnode.smp.bankaccounts.api.account.AccountId;

import java.time.Instant;

/**
 * Represents an access control list entry for an account.
 */
public final class AccountAclEntry extends AccessControlListEntry<AccountAclEntry.Role> {
    AccountAclEntry(
            final @NotNull TypedIdentifier subject,
            final @NotNull AccountAclEntry.Role role,
            final @NotNull AccountId account,
            final @NotNull Instant created
    ) {
        super(subject, role, account, created);
    }

    /**
     * Constructs a new ACL entry.
     *
     * @param subject the subject identifier
     * @param role    the role of the subject
     * @param account the account ID
     */
    public AccountAclEntry(
            final @NotNull TypedIdentifier subject,
            final @NotNull Role role,
            final @NotNull AccountId account
    ) {
        this(subject, role, account, Instant.now());
    }

    /**
     * Returns the ID of the account to which this ACL entry applies.
     *
     * @return the account ID
     */
    @NotNull
    public AccountId account() {
        return (AccountId) resource();
    }

    /**
     * Represents the relation of a subject to an account.
     */
    public enum Role implements AccessControlListEntry.Relation {
        /**
         * Read-only access to the account.
         */
        VIEWER,

        /**
         * Read-only access to most of the account, but can request payments.
         */
        COLLECTOR,

        /**
         * Access to initiate money transfers and request payments.
         */
        SIGNATORY,

        /**
         * Full access to the account, except ACL management or account closure.
         */
        MANAGER,

        /**
         * Full access to the account.
         */
        OWNER;

        @Override
        @NotNull
        public final String serialize() {
            return name().toLowerCase();
        }

        /**
         * Returns a role from string.
         *
         * @param value the string representation of the role
         * @return the role
         */
        @NotNull
        public static Role deserialize(final @NotNull String value) {
            return valueOf(value.toUpperCase());
        }
    }
}
