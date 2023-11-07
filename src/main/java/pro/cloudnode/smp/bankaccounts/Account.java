package pro.cloudnode.smp.bankaccounts;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentWrapper;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Bank account
 */
public class Account {
    /**
     * Unique account ID
     */
    public final @NotNull String id;
    /**
     * Account owner
     */
    public final @NotNull OfflinePlayer owner;
    /**
     * Account type
     */
    public final @NotNull Type type;
    /**
     * Account display name
     * <p>
     * Mostly intended for business accounts, i.e. showing the company name
     */
    public @Nullable String name;
    /**
     * Account balance
     */
    public @Nullable BigDecimal balance;
    /**
     * Whether the account is frozen
     * <p>
     * Frozen accounts cannot be used for transactions
     */
    public boolean frozen;

    /**
     * Create a new account instance
     * @param id Unique account ID
     * @param owner Account owner
     * @param type Account type
     * @param name Account display name
     * @param balance Account balance
     * @param frozen Whether the account is frozen
     */
    public Account(final @NotNull String id, final @NotNull OfflinePlayer owner, final @NotNull Type type, final @Nullable String name, final @Nullable BigDecimal balance, final boolean frozen) {
        this.id = id;
        this.owner = owner;
        this.type = type;
        this.name = name;
        this.balance = balance;
        this.frozen = frozen;
    }

    /**
     * Create a new account
     * @param owner Account owner
     * @param type Account type
     * @param name Account display name
     * @param balance Account balance
     * @param frozen Whether the account is frozen
     */
    public Account(final @NotNull OfflinePlayer owner, final @NotNull Type type, final @Nullable String name, final @Nullable BigDecimal balance, final boolean frozen) {
        this(StringGenerator.generate(16), owner, type, name, balance, frozen);
    }

    /**
     * Create bank account instance from database result set
     * @param rs Database result set
     */
    public Account(final @NotNull ResultSet rs) throws @NotNull SQLException {
        this(
                rs.getString("id"),
                BankAccounts.getInstance().getServer().getOfflinePlayer(UUID.fromString(rs.getString("owner"))),
                Type.getType(rs.getInt("type")),
                rs.getString("name"),
                rs.getBigDecimal("balance"),
                rs.getBoolean("frozen")
        );
    }

    /**
     * Update account balance
     * @param diff Balance difference (positive or negative)
     */
    public final void updateBalance(final @NotNull BigDecimal diff) {
        if (balance == null) return;
        this.balance = balance.add(diff);
        this.update();
    }

    /**
     * Create a new transaction/transfer
     * @param to Recipient account
     * @param amount Transaction amount Must be greater than zero.
     * @param description Transaction description
     * @param instrument Payment instrument used to facilitate the transaction
     * @throws IllegalStateException If the sender or recipient account is frozen or the sender has insufficient funds
     * @throws IllegalArgumentException If the amount is less than or equal to zero
     */
    public final @NotNull Transaction transfer(final @NotNull Account to, final @NotNull BigDecimal amount, final @Nullable String description, final @Nullable String instrument) {
        if (frozen) throw new IllegalStateException("Your account is frozen");
        if (to.frozen) throw new IllegalStateException("Recipient account is frozen");
        if (amount.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("Amount must be greater than zero");
        if (!hasFunds(amount)) throw new IllegalStateException("Insufficient funds");

        final @NotNull Transaction transaction = new Transaction(this, to, amount, description, instrument);
        transaction.save();
        this.updateBalance(amount.negate());
        to.updateBalance(amount);
        return transaction;
    }

    /**
     * Check if account has sufficient funds
     * @param amount Amount to check
     */
    public final boolean hasFunds(final @NotNull BigDecimal amount) {
        return balance == null || balance.compareTo(amount) >= 0;
    }

    /**
     * Create payment instrument
     */
    public final @NotNull ItemStack createInstrument() {
        final @NotNull Material material = Objects.requireNonNull(Material.getMaterial(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString(BankConfig.INSTRUMENTS_MATERIAL.getKey()))));
        final @NotNull ItemStack instrument = new ItemStack(material);

        final @NotNull String name = Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString(BankConfig.INSTRUMENTS_NAME.getKey()));
        final @NotNull List<String> lore = Objects.requireNonNull(BankAccounts.getInstance().getConfig().getStringList(BankConfig.INSTRUMENTS_LORE.getKey()));
        final boolean glint = BankAccounts.getInstance().getConfig().getBoolean(BankConfig.INSTRUMENTS_GLINT_ENABLED.getKey());

        final @NotNull ItemMeta meta = instrument.getItemMeta();
        meta.displayName(this.instrumentPlaceholders(name));
        meta.lore(lore.stream().map(this::instrumentPlaceholders).toList());

        if (glint) {
            final @NotNull NamespacedKey key = NamespacedKey.minecraft(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString(BankConfig.INSTRUMENTS_GLINT_ENCHANTMENT.getKey())));
            final @NotNull Enchantment enchantment = Objects.requireNonNull(EnchantmentWrapper.getByKey(key));
            meta.addEnchant(enchantment, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        final @NotNull NamespacedKey id = BankAccounts.Key.INSTRUMENT_ACCOUNT;
        meta.getPersistentDataContainer().set(id, PersistentDataType.STRING, this.id);

        instrument.setItemMeta(meta);

        return instrument;
    }

    /**
     * Check if an item is an instrument
     *
     * @param item Item to check
     */
    public static boolean isInstrument(final @NotNull ItemStack item) {
        return item.getItemMeta().getPersistentDataContainer().has(BankAccounts.Key.INSTRUMENT_ACCOUNT, PersistentDataType.STRING);
    }

    /**
     * Set placeholders for instrument
     * @param string String to set placeholders in
     */
    public final @NotNull Component instrumentPlaceholders (final @NotNull String string) {
        return MiniMessage.miniMessage().deserialize(string,
                Placeholder.unparsed("account", this.name == null ? (this.type == Type.PERSONAL && this.owner.getName() != null ? this.owner.getName() : this.id) : this.name),
                Placeholder.parsed("account-id", this.id),
                Placeholder.parsed("account-type", this.type.getName()),
                Placeholder.parsed("account-owner", this.owner.getUniqueId().equals(BankAccounts.getConsoleOfflinePlayer().getUniqueId()) ? "<i>the server</i>" : this.owner.getName() == null ? "<i>unknown player</i>" : this.owner.getName()),
                Formatter.date("date", LocalDateTime.now(ZoneOffset.UTC))
        ).decoration(TextDecoration.ITALIC, false);
    }

    /**
     * Get account by ID
     * @param id Account ID
     */
    public static @NotNull Optional<@NotNull Account> get(final @NotNull String id) {
        try (final @NotNull Connection conn = BankAccounts.getInstance().getDb().getConnection();
             final @NotNull PreparedStatement stmt = conn.prepareStatement("SELECT * FROM `bank_accounts` WHERE `id` = ? LIMIT 1")) {
            stmt.setString(1, id);
            final @NotNull ResultSet rs = stmt.executeQuery();
            return rs.next() ? Optional.of(new Account(rs)) : Optional.empty();
        }
        catch (final @NotNull Exception e) {
            BankAccounts.getInstance().getLogger().log(Level.SEVERE, "Could not get account: " + id, e);
            return Optional.empty();
        }
    }

    /**
     * Get account from instrument
     * @param instrument Instrument item
     */
    public static @NotNull Optional<@NotNull Account> get(final @NotNull ItemStack instrument) {
        if (!isInstrument(instrument)) return Optional.empty();
        final @NotNull NamespacedKey id = BankAccounts.Key.INSTRUMENT_ACCOUNT;
        final @NotNull ItemMeta meta = instrument.getItemMeta();
        final @NotNull String accountId = Objects.requireNonNull(meta.getPersistentDataContainer().get(id, PersistentDataType.STRING));
        return get(accountId);
    }

    /**
     * Get accounts by owner
     * @param owner Account owner
     * @param type Account type
     */
    public static @NotNull Account[] get(final @NotNull OfflinePlayer owner, final @Nullable Type type) {
        final @NotNull List<@NotNull Account> accounts = new ArrayList<>();
        try (final @NotNull Connection conn = BankAccounts.getInstance().getDb().getConnection();
             final @NotNull PreparedStatement stmt = conn.prepareStatement(type == null ? "SELECT * FROM `bank_accounts` WHERE `owner` = ?" : "SELECT * FROM `bank_accounts` WHERE `owner` = ? AND `type` = ?")) {
            stmt.setString(1, owner.getUniqueId().toString());
            if (type != null) stmt.setInt(2, Type.getType(type));
            final @NotNull ResultSet rs = stmt.executeQuery();

            while (rs.next()) accounts.add(new Account(rs));
            return accounts.toArray(new Account[0]);
        }
        catch (final @NotNull Exception e) {
            BankAccounts.getInstance().getLogger().log(Level.SEVERE, "Could not get accounts for: " + owner.getUniqueId().toString() + " (" + owner.getName() + "), type = " + (type == null ? "all" : type.name()), e);
            return new Account[0];
        }
    }

    /**
     * Get accounts by owner
     * @param owner Account owner
     */
    public static @NotNull Account[] get(final @NotNull OfflinePlayer owner) {
        return get(owner, null);
    }

    /**
     * Get all accounts
     */
    public static @NotNull Account[] get() {
        final @NotNull List<@NotNull Account> accounts = new ArrayList<>();
        try (final @NotNull Connection conn = BankAccounts.getInstance().getDb().getConnection();
             final @NotNull PreparedStatement stmt = conn.prepareStatement("SELECT * FROM `bank_accounts`")) {
            final @NotNull ResultSet rs = stmt.executeQuery();

            while (rs.next()) accounts.add(new Account(rs));
            return accounts.toArray(new Account[0]);
        } catch (final @NotNull Exception e) {
            BankAccounts.getInstance().getLogger().log(Level.SEVERE, "Could not get accounts", e);
            return new Account[0];
        }
    }

    /**
     * Insert into database
     */
    public void insert() {
        try (final @NotNull Connection conn = BankAccounts.getInstance().getDb().getConnection();
             final @NotNull PreparedStatement stmt = conn.prepareStatement("INSERT INTO `bank_accounts` (`id`, `owner`, `type`, `name`, `balance`, `frozen`) VALUES (?, ?, ?, ?, ?, ?)")) {
            stmt.setString(1, id);
            stmt.setString(2, owner.getUniqueId().toString());
            stmt.setInt(3, Type.getType(type));
            if (name == null) stmt.setNull(4, java.sql.Types.VARCHAR);
            else stmt.setString(4, name);
            if (balance == null) stmt.setNull(5, java.sql.Types.DECIMAL);
            else stmt.setBigDecimal(5, balance);
            stmt.setBoolean(6, frozen);

            stmt.executeUpdate();
        } catch (final @NotNull Exception e) {
            BankAccounts.getInstance().getLogger().log(Level.SEVERE, "Could not save account: " + id, e);
        }
    }

    /**
     * Update in database
     */
    public void update() {
        try (final @NotNull Connection conn = BankAccounts.getInstance().getDb().getConnection();
             final @NotNull PreparedStatement stmt = conn.prepareStatement("UPDATE `bank_accounts` SET `name` = ?, `balance` = ?, `frozen` = ? WHERE `id` = ?")) {
            if (name == null) stmt.setNull(1, java.sql.Types.VARCHAR);
            else stmt.setString(1, name);
            if (balance == null) stmt.setNull(2, java.sql.Types.DECIMAL);
            else stmt.setBigDecimal(2, balance);
            stmt.setBoolean(3, frozen);
            stmt.setString(4, id);

            stmt.executeUpdate();
        } catch (final @NotNull Exception e) {
            BankAccounts.getInstance().getLogger().log(Level.SEVERE, "Could not save account: " + id, e);
        }
    }

    /**
     * Delete account from database
     */
    public void delete() {
        try (final @NotNull Connection conn = BankAccounts.getInstance().getDb().getConnection();
             final @NotNull PreparedStatement stmt = conn.prepareStatement("DELETE FROM `bank_accounts` WHERE `id` = ?")) {
            stmt.setString(1, id);
            stmt.executeUpdate();
        } catch (final @NotNull Exception e) {
            BankAccounts.getInstance().getLogger().log(Level.SEVERE, "Could not delete account: " + id, e);
        }
    }

    /**
     * Account placeholders
     * @param string String to deserialize with MiniMessage and apply placeholders to
     * @param accounts Accounts to apply placeholders to
     */
    public static String placeholdersString(@NotNull String string, HashMap<String, @NotNull Account> accounts) {
        for (Map.Entry<String, Account> entry : accounts.entrySet()) {
            String name = entry.getKey();
            Account account = entry.getValue();
            String prefix = name.isEmpty() ? "" : name + "-";
            string = string.replace("<" + prefix + "account>", account.name == null ? (account.type == Account.Type.PERSONAL && account.owner.getName() != null ? account.owner.getName() : account.id) : account.name)
                    .replace("<" + prefix + "account-id>", account.id)
                    .replace("<" + prefix + "account-type>", account.type.getName())
                    .replace("<" + prefix + "account-owner>", account.owner.getUniqueId().equals(BankAccounts.getConsoleOfflinePlayer().getUniqueId()) ? "<i>the server</i>" : account.owner.getName() == null ? "<i>unknown player</i>" : account.owner.getName())
                    .replace("<" + prefix + "balance>", account.balance == null ? "∞" : account.balance.toPlainString())
                    .replace("<" + prefix + "balance-formatted>", BankAccounts.formatCurrency(account.balance))
                    .replace("<" + prefix + "balance-short>", BankAccounts.formatCurrencyShort(account.balance));
        }
        return string;
    }

    /**
     * Account placeholders
     * @param string String to deserialize with MiniMessage and apply placeholders to
     * @param accounts Accounts to apply placeholders to
     */
    public static Component placeholders(@NotNull String string, HashMap<String, Account> accounts) {
        return MiniMessage.miniMessage().deserialize(placeholdersString(string, accounts));
    }

    /**
     * Account placeholders
     * @param string String to deserialize with MiniMessage and apply placeholders to
     * @param account Account to apply placeholders to
     */
    public static Component placeholders(@NotNull String string, Account account) {
        return placeholders(string, new HashMap<>() {{
            put("", account);
        }});
    }

    /**
     * Account placeholders
     * @param string String to deserialize with MiniMessage and apply placeholders to
     * @param account Account to apply placeholders to
     */
    public static String placeholdersString(@NotNull String string, Account account) {
        return placeholdersString(string, new HashMap<>() {{
            put("", account);
        }});
    }

    /**
     * Bank account type
     */
    public enum Type {
        /**
         * Personal, individual, private account
         */
        PERSONAL,
        /**
         * Account owned by a company or other corporate entity
         */
        BUSINESS;

        /**
         * Get type name (as set in config)
         */
        public @NotNull String getName() {
            return Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("messages.types." + getType(this)));
        }

        /**
         * Convert account type to integer
         * @param type Account type
         * @return Account type as integer
         */
        public static int getType(final @NotNull Type type) {
            return type.ordinal();
        }

        /**
         * Convert integer to account type
         * @param type Account type as integer
         * @return Account type
         */
        public static @NotNull Type getType(final int type) {
            return Type.values()[type];
        }

        public static @NotNull Optional<@NotNull Type> fromString(final @NotNull String name) {
            for (final @NotNull Type type : Type.values())
                if (type.name().equalsIgnoreCase(name)) return Optional.of(type);
            return Optional.empty();
        }
    }

    /**
     * A dummy class representing a missing account (e.g. deleted).
     */
    public static final class ClosedAccount extends Account {
        public ClosedAccount() {
            super("closed account", BankAccounts.getConsoleOfflinePlayer(), Type.PERSONAL, null, BigDecimal.ZERO, true);
        }

        @Override
        public void insert() {}

        @Override
        public void update() {}

        @Override
        public void delete() {}
    }
}
