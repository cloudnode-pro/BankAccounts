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

package pro.cloudnode.smp.bankaccounts.api.holder;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import pro.cloudnode.smp.bankaccounts.api.TypedIdentifier;

/**
 * Represents an account holder identifier.
 */
public final class HolderId extends TypedIdentifier {
    /**
     * Constructs an account holder identifier.
     *
     * @param id an identifier of the holder entity
     */
    HolderId(final @NotNull TypedIdentifier id) {
        super(Type.HOLDER, id);
    }

    /**
     * Returns the identifier of the holder entity.
     *
     * @return the identifier representing the underlying holder entity
     */
    @NotNull
    public TypedIdentifier entity() {
        return TypedIdentifier.deserialize(id());
    }

    /**
     * Creates an account holder identifier of a player entity.
     *
     * @param player the player
     * @return the account holder identifier
     */
    @NotNull
    public static HolderId player(final @NotNull OfflinePlayer player) {
        return new HolderId(new TypedIdentifier(Type.PLAYER, player.getUniqueId().toString()));
    }
}
