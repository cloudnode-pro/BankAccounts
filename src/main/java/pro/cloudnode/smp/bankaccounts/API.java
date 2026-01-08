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
