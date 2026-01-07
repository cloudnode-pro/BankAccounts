package pro.cloudnode.smp.bankaccounts;

import org.jetbrains.annotations.NotNull;
import pro.cloudnode.smp.bankaccounts.api.account.AccountsService;
import pro.cloudnode.smp.bankaccounts.api.ledger.LedgerService;

import javax.sql.DataSource;
import java.util.logging.Logger;

/**
 * Represents the BankAccounts API.
 */
public final class API {
    /**
     * Service for managing accounts.
     */
    public final @NotNull AccountsService accounts;

    /**
     * Service for managing ledger entries and transactions.
     */
    public final @NotNull LedgerService ledger;

    API(
            final @NotNull Logger parentLogger,
            final @NotNull DataSource dataSource,
            final int idLengthAccount,
            final int idLengthTransaction
    ) {
        this.accounts = new AccountsService(parentLogger, dataSource, idLengthAccount);
        this.ledger = new LedgerService(parentLogger, dataSource, accounts, idLengthTransaction);
    }
}
