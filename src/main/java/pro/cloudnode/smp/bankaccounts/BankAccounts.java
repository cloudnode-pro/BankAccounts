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
