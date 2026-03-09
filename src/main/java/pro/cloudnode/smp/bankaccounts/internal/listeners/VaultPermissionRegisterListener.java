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

package pro.cloudnode.smp.bankaccounts.internal.listeners;

import net.milkbowl.vault.permission.Permission;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServiceRegisterEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;
import pro.cloudnode.smp.bankaccounts.internal.permission.PermissionManager;

public record VaultPermissionRegisterListener(@NotNull PermissionManager manager) implements Listener {
    @EventHandler
    public void onRegister(final @NotNull ServiceRegisterEvent event) {
        final RegisteredServiceProvider<?> provider = event.getProvider();

        if (!provider.getService().equals(Permission.class)) {
            return;
        }

        manager.setVault((Permission) provider.getProvider());
    }
}
