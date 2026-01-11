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

import org.jetbrains.annotations.NotNull;
import pro.cloudnode.smp.bankaccounts.api.TypedIdentifier;

/**
 * Represents an account identifier.
 */
public final class AccountId extends TypedIdentifier {
    /**
     * Constructs an account identifier.
     *
     * @param id the account ID
     */
    public AccountId(@NotNull String id) {
        super(Type.ACCOUNT, id);
    }
}
