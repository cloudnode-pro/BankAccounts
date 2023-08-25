package pro.cloudnode.smp.bankaccounts;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pro.cloudnode.smp.bankaccounts.commands.BankCommand;
import pro.cloudnode.smp.bankaccounts.commands.POSCommand;
import pro.cloudnode.smp.bankaccounts.events.BlockBreak;
import pro.cloudnode.smp.bankaccounts.events.GUI;
import pro.cloudnode.smp.bankaccounts.events.Join;
import pro.cloudnode.smp.bankaccounts.events.PlayerInteract;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

public final class BankAccounts extends JavaPlugin {

    public final @NotNull HikariConfig config = new HikariConfig();
    private HikariDataSource dbSource;

    public @NotNull HikariDataSource getDb() {
        return dbSource;
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reload();

        // Register commands
        final @NotNull HashMap<@NotNull String, @NotNull CommandExecutor> commands = new HashMap<>() {{
            put("bank", new BankCommand());
            put("pos", new POSCommand());
        }};
        for (Map.Entry<@NotNull String, @NotNull CommandExecutor> entry : commands.entrySet()) {
            final PluginCommand command = getCommand(entry.getKey());
            if (command == null) {
                getLogger().log(Level.SEVERE, "Could not register command: " + entry.getKey());
                getServer().getPluginManager().disablePlugin(this);
                return;
            }
            command.setExecutor(entry.getValue());
        }

        // Register events
        final @NotNull Listener[] events = new Listener[]{
                new Join(),
                new BlockBreak(),
                new PlayerInteract(),
                new GUI()
        };
        for (final @NotNull Listener event : events) getServer().getPluginManager().registerEvents(event, this);
    }

    @Override
    public void onDisable() {
        dbSource.close();
    }

    /**
     * Setup database source
     */
    private void setupDbSource() {
        switch (Objects.requireNonNull(getConfig().getString("db.db"))) {
            case "sqlite" -> {
                config.setDriverClassName(org.sqlite.JDBC.class.getName());
                config.setJdbcUrl("jdbc:sqlite:" + getDataFolder().getAbsolutePath() + "/" + Objects.requireNonNull(getConfig().getString("db.sqlite.file")));
            }
            case "mariadb" -> {
                config.setDriverClassName(org.mariadb.jdbc.Driver.class.getName());
                config.setJdbcUrl(Objects.requireNonNull(getConfig().getString("db.mariadb.jdbc")));
                config.setUsername(Objects.requireNonNull(getConfig().getString("db.mariadb.user")));
                config.setPassword(Objects.requireNonNull(getConfig().getString("db.mariadb.password")));
            }
            default -> {
                getLogger().log(Level.SEVERE, "Invalid database type.");
                getServer().getPluginManager().disablePlugin(this);
                return;
            }
        }
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
     * Reload plugin
     */
    public static void reload() {
        getInstance().reloadConfig();
        getInstance().setupDbSource();
        getInstance().initDbWrapper();
        createServerAccount();
        getInstance().getServer().getScheduler().runTaskAsynchronously(getInstance(), () -> {
            checkForUpdates().ifPresent(latestVersion -> {
                getInstance().getLogger().warning("An update is available: " + latestVersion);
                getInstance().getLogger().warning("Please update to the latest version to benefit from bug fixes, security patches, new features and support.");
                getInstance().getLogger().warning("Update details: https://modrinth.com/plugin/bankaccounts/version/" + latestVersion);
            });
        });
    }

    /**
     * Create tables
     */
    private void initDb() throws @NotNull SQLException, @NotNull IOException {
        final @NotNull HashMap<@NotNull String, @NotNull String> initFiles = new HashMap<>() {{
            put("mariadb", "db-init/mysql.sql");
            put("sqlite", "db-init/sql.sql");
        }};
        final @NotNull String db = Objects.requireNonNull(getConfig().getString("db.db"));
        if (!initFiles.containsKey(db)) {
            getLogger().log(Level.SEVERE, "Invalid database type.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        final @NotNull String initFile = initFiles.get(db);
        @NotNull String setup;
        try (final InputStream in = getClassLoader().getResourceAsStream(initFile)) {
            setup = new String(Objects.requireNonNull(in).readAllBytes());
        }
        catch (@NotNull IOException e) {
            getLogger().log(Level.SEVERE, "Could not read db setup file.", e);
            throw e;
        }
        final @NotNull String[] queries = setup.split(";");
        for (@NotNull String query : queries) {
            query = query.stripTrailing().stripIndent().replaceAll("^\\s+(?:--.+)*", "");
            if (query.isBlank()) continue;
            try (final @NotNull Connection conn = getDb().getConnection();
                 final @NotNull PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.execute();
            }
        }
        getLogger().info("Database setup complete.");
    }

    private void initDbWrapper() {
        try {
            initDb();
        }
        catch (@NotNull SQLException | @NotNull IOException e) {
            getLogger().log(Level.SEVERE, "Could not initialize database.", e);
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    /**
     * Get instance of the plugin
     */
    public static @NotNull BankAccounts getInstance() {
        return getPlugin(BankAccounts.class);
    }

    /**
     * Get currency symbol
     */
    public static @NotNull String getCurrencySymbol() {
        final @Nullable String symbol = getInstance().getConfig().getString("currency.symbol");
        return symbol != null ? symbol : "$";
    }

    /**
     * Format currency
     *
     * @param amount Amount
     */
    public static String formatCurrency(final @Nullable BigDecimal amount) {
        if (amount == null) return getCurrencySymbol() + "∞";
        final @Nullable String format = getInstance().getConfig().getString("currency.format");
        return (amount.compareTo(BigDecimal.ZERO) < 0 ? "<red>-" : "") + getCurrencySymbol() + new DecimalFormat(format != null ? format : "#,##0.00").format(amount.abs().setScale(2, RoundingMode.HALF_UP)) + (amount.compareTo(BigDecimal.ZERO) < 0 ? "</red>" : "");
    }

    /**
     * Short currency format
     * <p>
     * Example: {@code $1.23}, {@code $1.2K}, {@code $132K}, {@code $1.2M}, {@code $100M}, etc. including B and T
     *
     * @param amount Amount
     */
    public static String formatCurrencyShort(final @Nullable BigDecimal amount) {
        final @NotNull String currencySymbol = getCurrencySymbol();
        if (amount == null) return "∞";
        final @NotNull BigDecimal absAmount = amount.abs().setScale(2, RoundingMode.HALF_UP);
        final @NotNull String prefix = (amount.compareTo(BigDecimal.ZERO) < 0 ? "-" : "") + currencySymbol;
        if (absAmount.compareTo(BigDecimal.valueOf(1000)) < 0)
            return prefix + absAmount.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();

        final @NotNull Map<BigDecimal, String> bounds = Map.of(
                BigDecimal.valueOf(1000), "K",
                BigDecimal.valueOf(1_000_000), "M",
                BigDecimal.valueOf(1_000_000_000), "B",
                BigDecimal.valueOf(1_000_000_000_000L), "T"
        );
        @Nullable Map.Entry<BigDecimal, String> entry = bounds.entrySet().stream().sorted(Map.Entry.comparingByKey()).filter(e -> absAmount.divide(e.getKey(), RoundingMode.HALF_UP).compareTo(BigDecimal.valueOf(1000)) < 0).findFirst().orElse(null);
        if (entry == null) entry = bounds.entrySet().stream().max(Map.Entry.comparingByKey()).orElse(null);
        if (entry == null) return "FAIL";
        final @NotNull BigDecimal bound = entry.getKey();
        final @NotNull String suffix = bounds.get(bound);
        final @NotNull BigDecimal divided = absAmount.divide(bound, RoundingMode.HALF_UP);
        int scale = divided.compareTo(BigDecimal.valueOf(10)) < 0 ? 2 : divided.compareTo(BigDecimal.valueOf(100)) < 0 ? 1 : 0;
        return (amount.compareTo(BigDecimal.ZERO) < 0 ? "<red>" : "") + prefix + divided.setScale(scale, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString() + suffix + (amount.compareTo(BigDecimal.ZERO) < 0 ? "</red>" : "");
    }

    /**
     * Namespaced key
     *
     * @param key Key
     */
    public static @NotNull NamespacedKey namespacedKey(final @NotNull String key) {
        return new NamespacedKey(getInstance(), key);
    }

    /**
     * Create server account, if enabled in config
     */
    private static void createServerAccount() {
        if (getInstance().getConfig().getBoolean("server-account.enabled")) {
            final @NotNull Account[] accounts = Account.get(getConsoleOfflinePlayer());
            if (accounts.length > 0) return;
            final @Nullable String name = getInstance().getConfig().getString("server-account.name");
            final @NotNull Account.Type type = Account.Type.getType(getInstance().getConfig().getInt("server-account.type"));
            final @Nullable BigDecimal balance = Objects.requireNonNull(getInstance().getConfig().getString("server-account.starting-balance")).equals("Infinity") ? null : BigDecimal.valueOf(getInstance().getConfig().getDouble("server-account.starting-balance"));
            new Account(getConsoleOfflinePlayer(), type, name, balance, false).insert();
        }
    }

    /**
     * Get console offline player
     * <p>
     * Not real player; UUID: 00000000-0000-0000-0000-000000000000
     */
    public static @NotNull OfflinePlayer getConsoleOfflinePlayer() {
        return BankAccounts.getInstance().getServer().getOfflinePlayer(UUID.fromString("00000000-0000-0000-0000-000000000000"));
    }

    /**
     * Get offline player or console as offline player (UUID 0)
     *
     * @param sender Command sender
     */
    public static @NotNull OfflinePlayer getOfflinePlayer(@NotNull CommandSender sender) {
        return sender instanceof OfflinePlayer ? (OfflinePlayer) sender : getConsoleOfflinePlayer();
    }

    /**
     * Check for plugin updates using Modrinth API
     *
     * @return The latest version if an update is available, otherwise empty
     */
    public static Optional<String> checkForUpdates() {
        final @NotNull BankAccounts plugin = BankAccounts.getInstance();
        final @NotNull String mcVersion = plugin.getServer().getMinecraftVersion();
        final @NotNull String pluginName = plugin.getPluginMeta().getName();
        final @NotNull String pluginVersion = plugin.getPluginMeta().getVersion();
        try {
            final @NotNull HttpClient client = HttpClient.newHttpClient();
            final @NotNull HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.modrinth.com/v2/project/Dc8RS2En/version?featured=true&game_versions=[%22" + mcVersion + "%22]"))
                    .header("User-Agent",
                            pluginName + "/" + pluginVersion
                    )
                    .GET()
                    .build();
            final @NotNull HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() < 400 && res.statusCode() >= 200 && res.body() != null) {
                final @NotNull JsonObject json = JsonParser.parseString(res.body()).getAsJsonArray().get(0).getAsJsonObject();
                if (json.has("version_number")) {
                    final @NotNull String latestVersion = json.get("version_number").getAsString();
                    if (!latestVersion.equals(pluginVersion))
                        return Optional.of(latestVersion);
                }
            }
        }
        catch (final @NotNull Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to check for updates", e);
        }
        return Optional.empty();
    }

    public static final class Key {
        public final static @NotNull NamespacedKey INSTRUMENT_ACCOUNT = namespacedKey("instrument-account");
        public final static @NotNull NamespacedKey POS_OWNER_GUI = namespacedKey("pos-owner-gui");
        public final static @NotNull NamespacedKey POS_BUYER_GUI = namespacedKey("pos-buyer-gui");
        public final static @NotNull NamespacedKey POS_BUYER_GUI_CONFIRM = namespacedKey("pos-buyer-gui-confirm");
        public final static @NotNull NamespacedKey POS_BUYER_GUI_CANCEL = namespacedKey("pos-buyer-gui-cancel");
    }
}
