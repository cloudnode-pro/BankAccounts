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

package pro.cloudnode.smp.bankaccounts.internal.permission;

import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pro.cloudnode.smp.bankaccounts.api.TypedIdentifier;
import pro.cloudnode.smp.bankaccounts.internal.auth.Scope;
import pro.cloudnode.smp.bankaccounts.api.holder.Holder;
import pro.cloudnode.smp.bankaccounts.api.holder.HolderId;

import java.util.UUID;

public final class PermissionManager {
    private @Nullable Permission vault = null;

    public void setVault(final @NotNull Permission vault) {
        this.vault = vault;
    }

    /**
     * Checks whether the manager can query offline players.
     *
     * @return true if permissions can be checked for offline players, false otherwise
     */
    public boolean supportsOffline() {
        return vault != null;
    }

    /**
     * Checks whether permissions can be queried for the given player.
     *
     * @param player the player
     * @return true if permissions can be queried for the player, false otherwise
     */
    public boolean canCheck(final @NotNull OfflinePlayer player) {
        return supportsOffline() || player.isOnline();
    }

    /**
     * Checks whether a player has the required scope.
     *
     * @param player the player
     * @param scope  the scope
     * @return true if the player has the scope, false otherwise
     * @throws IllegalStateException if the player is offline and offline permissions are not supported
     * @see #supportsOffline()
     */
    public boolean hasPermission(final @NotNull OfflinePlayer player, final @NotNull Scope scope) {
        final @Nullable Player onlinePlayer = player.getPlayer();
        if (onlinePlayer != null) {
            return onlinePlayer.hasPermission(scope.permission());
        }

        if (!supportsOffline()) {
            throw new IllegalStateException(String.format(
                    "Cannot check permission for offline player %s without Vault",
                    player
            ));
        }

        return vault.playerHas(null, player, scope.permission().getName());
    }

    /**
     * Checks whether the player associated with a typed identifier has the required scope.
     *
     * @param id    the player identifier
     * @param scope the scope
     * @return true if the player has the scope, false otherwise
     * @throws IllegalArgumentException if the identifier is not of type {@link TypedIdentifier.Type#PLAYER}
     * @throws IllegalStateException    if the player is offline and offline permissions are not supported
     * @see #supportsOffline()
     */
    public boolean hasPermission(final @NotNull TypedIdentifier id, final @NotNull Scope scope) {
        if (id.type() != TypedIdentifier.Type.PLAYER) {
            throw new IllegalArgumentException(String.format("Identifier of type %s is not supported", id.type()));
        }

        final OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(id.id()));
        return hasPermission(player, scope);
    }

    /**
     * Checks whether a player associated with a holder identifier has the required scope.
     *
     * @param id    the holder identifier
     * @param scope the scope
     * @return true if the player has the scope, false otherwise
     * @throws IllegalArgumentException if the holder entity is not a player
     * @throws IllegalStateException    if the player is offline and offline permissions are not supported
     * @see #supportsOffline()
     */
    public boolean hasPermission(final @NotNull HolderId id, final @NotNull Scope scope) {
        return hasPermission(id.entity(), scope);
    }

    /**
     * Checks whether an account holder has the required scope.
     *
     * @param holder the account holder
     * @param scope  the scope
     * @return true if the holder has the scope, false otherwise
     * @throws IllegalArgumentException if the holder entity is not a player
     * @throws IllegalStateException    if the player is offline and offline permissions are not supported
     * @see #supportsOffline()
     */
    public boolean hasPermission(final @NotNull Holder holder, final @NotNull Scope scope) {
        return hasPermission(holder.id(), scope);
    }
}
