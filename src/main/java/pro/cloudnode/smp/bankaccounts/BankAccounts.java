package pro.cloudnode.smp.bankaccounts;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;
import pro.cloudnode.smp.bankaccounts.events.Join;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;

public final class BankAccounts extends JavaPlugin {

    public final HikariConfig config = new HikariConfig();
    private HikariDataSource dbSource;

    public HikariDataSource getDb() {
        return dbSource;
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        setupDbSource();
        try {
            initDb();
        } catch (SQLException | IOException e) {
            getLogger().log(Level.SEVERE, "Could not initialize database.", e);
            getServer().getPluginManager().disablePlugin(this);
        }

        createServerAccount();
        // Register events
        getServer().getPluginManager().registerEvents(new Join(), this);
    }

    @Override
    public void onDisable() {
        dbSource.close();
    }

    /**
     * Setup database source
     */
    private void setupDbSource() {
        config.setDriverClassName(org.mariadb.jdbc.Driver.class.getName());
        if (getConfig().isString("db.jdbc")) config.setJdbcUrl(getConfig().getString("db.jdbc"));
        else getLogger().severe("config: db.jdbc: not set");
        if (getConfig().isString("db.user")) config.setUsername(getConfig().getString("db.user"));
        else getLogger().severe("config: db.user: not set");
        if (getConfig().isString("db.password")) config.setPassword(getConfig().getString("db.password"));
        else getLogger().severe("config: db.password: not set");
        if (getConfig().isBoolean("db.cachePrepStmts"))
            config.addDataSourceProperty("cachePrepStmts", getConfig().getString("db.cachePrepStmts"));
        if (getConfig().isInt("db.prepStmtCacheSize"))
            config.addDataSourceProperty("prepStmtCacheSize", getConfig().getString("db.prepStmtCacheSize"));
        if (getConfig().isInt("db.prepStmtCacheSqlLimit"))
            config.addDataSourceProperty("prepStmtCacheSqlLimit", getConfig().getString("db.prepStmtCacheSqlLimit"));
        if (getConfig().isBoolean("db.useServerPrepStmts"))
            config.addDataSourceProperty("useServerPrepStmts", getConfig().getString("db.useServerPrepStmts"));
        if (getConfig().isBoolean("db.useLocalSessionState"))
            config.addDataSourceProperty("useLocalSessionState", getConfig().getString("db.useLocalSessionState"));
        if (getConfig().isBoolean("db.rewriteBatchedStatements"))
            config.addDataSourceProperty("rewriteBatchedStatements", getConfig().getString("db.rewriteBatchedStatements"));
        if (getConfig().isBoolean("db.cacheResultSetMetadata"))
            config.addDataSourceProperty("cacheResultSetMetadata", getConfig().getString("db.cacheResultSetMetadata"));
        if (getConfig().isBoolean("db.cacheServerConfiguration"))
            config.addDataSourceProperty("cacheServerConfiguration", getConfig().getString("db.cacheServerConfiguration"));
        if (getConfig().isBoolean("db.elideSetAutoCommits"))
            config.addDataSourceProperty("elideSetAutoCommits", getConfig().getString("db.elideSetAutoCommits"));
        if (getConfig().isBoolean("db.maintainTimeStats"))
            config.addDataSourceProperty("maintainTimeStats", getConfig().getString("db.maintainTimeStats"));

        dbSource = new HikariDataSource(config);
    }

    /**
     * Create tables
     */
    private void initDb() throws SQLException, IOException {
        String setup;
        try (InputStream in = getClassLoader().getResourceAsStream("setup.sql")) {
            // Java 9+ way
            setup = new String(in.readAllBytes());
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Could not read db setup file.", e);
            throw e;
        }
        String[] queries = setup.split(";");
        for (String query : queries) {
            if (query.isBlank()) continue;
            try (Connection conn = getDb().getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.execute();
            }
        }
        getLogger().info("Database setup complete.");
    }

    /**
     * Get instance of the plugin
     */
    public static BankAccounts getInstance() {
        return getPlugin(BankAccounts.class);
    }

    /**
     * Get currency symbol
     */
    public static @NotNull String getCurrencySymbol() {
        String symbol = getInstance().getConfig().getString("currency.symbol");
        return symbol != null ? symbol : "$";
    }

    /**
     * Format currency
     *
     * @param amount Amount
     */
    public static String formatCurrency(BigDecimal amount) {
        if (amount == null) return getCurrencySymbol() + "∞";
        String format = getInstance().getConfig().getString("currency.format");
        return getCurrencySymbol() + new DecimalFormat(format != null ? format : "#,##0.00").format(amount);
    }

    /**
     * Short currency format
     * <p>
     * Example: {@code $1.23}, {@code $1.2K}, {@code $132K}, {@code $1.2M}, {@code $100M}, etc. including B and T
     *
     * @param amount Amount
     */
    public static String formatCurrencyShort(BigDecimal amount) {
        String currencySymbol = getCurrencySymbol();
        if (amount == null) return currencySymbol + "∞";
        BigDecimal absAmount = amount.abs();

        if (absAmount.compareTo(BigDecimal.valueOf(1000)) < 0)
            return currencySymbol + amount.setScale(2, RoundingMode.HALF_UP).toString();
        else if (absAmount.compareTo(BigDecimal.valueOf(10_000)) < 0)
            return currencySymbol + amount.divide(BigDecimal.valueOf(1000), 2, RoundingMode.HALF_UP).toString() + "K";
        else if (absAmount.compareTo(BigDecimal.valueOf(100_000)) < 0)
            return currencySymbol + amount.divide(BigDecimal.valueOf(1000), 1, RoundingMode.HALF_UP).toString() + "K";
        else if (absAmount.compareTo(BigDecimal.valueOf(1_000_000)) < 0)
            return currencySymbol + amount.divide(BigDecimal.valueOf(1000), 0, RoundingMode.HALF_UP).toString() + "K";
        else if (absAmount.compareTo(BigDecimal.valueOf(10_000_000)) < 0)
            return currencySymbol + amount.divide(BigDecimal.valueOf(1_000_000), 2, RoundingMode.HALF_UP).toString() + "M";
        else if (absAmount.compareTo(BigDecimal.valueOf(100_000_000)) < 0)
            return currencySymbol + amount.divide(BigDecimal.valueOf(1_000_000), 1, RoundingMode.HALF_UP).toString() + "M";
        else if (absAmount.compareTo(BigDecimal.valueOf(1_000_000_000)) < 0)
            return currencySymbol + amount.divide(BigDecimal.valueOf(1_000_000), 0, RoundingMode.HALF_UP).toString() + "M";
        else if (absAmount.compareTo(BigDecimal.valueOf(10_000_000_000L)) < 0)
            return currencySymbol + amount.divide(BigDecimal.valueOf(1_000_000_000L), 2, RoundingMode.HALF_UP).toString() + "B";
        else if (absAmount.compareTo(BigDecimal.valueOf(100_000_000_000L)) < 0)
            return currencySymbol + amount.divide(BigDecimal.valueOf(1_000_000_000L), 1, RoundingMode.HALF_UP).toString() + "B";
        else if (absAmount.compareTo(BigDecimal.valueOf(1_000_000_000_000L)) < 0)
            return currencySymbol + amount.divide(BigDecimal.valueOf(1_000_000_000L), 0, RoundingMode.HALF_UP).toString() + "B";
        else if (absAmount.compareTo(BigDecimal.valueOf(10_000_000_000_000L)) < 0)
            return currencySymbol + amount.divide(BigDecimal.valueOf(1_000_000_000_000L), 2, RoundingMode.HALF_UP).toString() + "T";
        else
            return currencySymbol + amount.divide(BigDecimal.valueOf(1_000_000_000_000L), 1, RoundingMode.HALF_UP).toString() + "T";
    }

    /**
     * Create server account, if enabled in config
     */
    private static void createServerAccount() {
        if (getInstance().getConfig().getBoolean("server-account.enabled")) {
            Account[] accounts = Account.get(getInstance().getServer().getOfflinePlayer(UUID.fromString("00000000-0000-0000-0000-000000000000")));
            if (accounts.length > 0) return;
            String name = getInstance().getConfig().getString("server-account.name");
            Account.Type type = Account.getType(getInstance().getConfig().getInt("server-account.type"));
            BigDecimal balance = getInstance().getConfig().getString("server-account.starting-balance").equals("Infinity") ? null : BigDecimal.valueOf(getInstance().getConfig().getDouble("server-account.starting-balance"));
            OfflinePlayer server = getInstance().getServer().getOfflinePlayer(UUID.fromString("00000000-0000-0000-0000-000000000000"));
            new Account(server, type, name, balance, false).save();
        }
    }
}
