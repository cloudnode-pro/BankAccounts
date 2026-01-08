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

/**
 * Represents a double-entry ledger transaction.
 * <p>
 * A transaction consists of one debit and one credit ledger entry. The debit amount must be negative, the credit amount
 * positive, and the two amounts must sum to zero.
 *
 * @param debit  the debit ledger entry
 * @param credit the credit ledger entry
 */
public record Transaction(@NotNull LedgerEntry debit, @NotNull LedgerEntry credit) {
    /**
     * Constructs a transaction, validating its double-entry invariants.
     *
     * @param debit  the debit ledger entry
     * @param credit the credit ledger entry
     * @throws IllegalArgumentException if the debit and credit accounts are the same, the debit amount is non-negative,
     *                                  or the amounts do not sum to zero
     */
    public Transaction {
        if (debit.account().equals(credit.account())) {
            throw new IllegalArgumentException("Debit and credit must be different accounts");
        }

        if (debit.amount().signum() >= 0) {
            throw new IllegalArgumentException("Debit amount must be negative");
        }

        if (debit.amount().add(credit.amount()).signum() != 0) {
            throw new IllegalArgumentException("Debit and credit amounts must sum to zero");
        }
    }
}
