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

package pro.cloudnode.smp.bankaccounts;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Objects;

abstract class BaseConfig {
    protected final @NotNull YamlConfiguration config;
    private final @NotNull JavaPlugin plugin;
    private final @NotNull String path;

    protected BaseConfig(final @NotNull JavaPlugin plugin, final @NotNull String path) {
        this.plugin = plugin;
        this.path = path;
        this.config = new YamlConfiguration();
    }

    public final void reload() throws IOException, InvalidConfigurationException {
        load();
    }

    protected final void load() throws IOException, InvalidConfigurationException {
        plugin.saveResource(path, false);
        config.load(file());
        config.addDefaults(YamlConfiguration.loadConfiguration(resource()));
    }

    private @NotNull File file() {
        return new File(plugin.getDataFolder(), path);
    }

    private @NotNull Reader resource() {
        return new InputStreamReader(Objects.requireNonNull(plugin.getResource(path)));
    }
}
