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
