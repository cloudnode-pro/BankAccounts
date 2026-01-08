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

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the BankAccounts plugin.
 */
public final class BankAccounts extends JavaPlugin {
    private BankConfig config;
    private HikariDataSource dataSource;
    private API api;

    /**
     * Constructs BankAccounts.
     *
     * @hidden
     */
    @ApiStatus.Internal
    public BankAccounts() {
    }

    /**
     * Returns the public API for BankAccounts.
     *
     * @return the BankAccounts API
     */
    public static @NotNull API api() {
        final var plugin = getPlugin(BankAccounts.class);
        if (plugin.api == null) {
            throw new IllegalStateException("BankAccounts is not enabled");
        }
        return plugin.api;
    }

    /**
     * @hidden
     */
    @ApiStatus.Internal
    @Override
    public void onEnable() {
        reload();
    }

    /**
     * @hidden
     */
    @ApiStatus.Internal
    @Override
    public void onDisable() {
        dataSource.close();
        dataSource = null;
        config = null;
        api = null;
    }

    private void reload() {
        this.onDisable();

        config = new BankConfig(this);

        try {
            config.load();
        } catch (final Throwable e) {
            throw new RuntimeException("Failed to reload config", e);
        }

        final HikariConfig hikari = new HikariConfig();

        hikari.setJdbcUrl(String.format("jdbc:h2:%s/bank.mv.db", getDataFolder().getAbsolutePath()));

        dataSource = new HikariDataSource(hikari);
        new DatabaseInit(dataSource).init();
        api = new API(getLogger(), dataSource, config.idLengthAccount(), config.idLengthTransaction());
    }
}
