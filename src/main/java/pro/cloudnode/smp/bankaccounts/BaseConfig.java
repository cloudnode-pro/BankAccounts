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
