package pro.cloudnode.smp.bankaccounts;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.InputStream;
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
    }

    @Override
    public void onDisable() {
        dbSource.close();
    }

    /**
     * Setup database source
     */
    private void setupDbSource() {
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
}
