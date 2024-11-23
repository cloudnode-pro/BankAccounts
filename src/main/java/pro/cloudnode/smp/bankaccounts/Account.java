package pro.cloudnode.smp.bankaccounts;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.ApiStatus;
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
import java.util.Arrays;
import java.util.Date;
import java.util.List;
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
    public @NotNull OfflinePlayer owner;
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

    public final @NotNull String name() {
        return this.name == null ? (this.type == Type.VAULT && this.owner.getName() != null ? this.owner.getName() : this.id) : this.name;
    }

    public final @NotNull Component ownerName() {
        return this.owner.getUniqueId().equals(BankAccounts.getConsoleOfflinePlayer().getUniqueId()) ? MiniMessage.miniMessage().deserialize("<i>the server</i>") : this.owner.getName() == null ? MiniMessage.miniMessage().deserialize("<i>unknown player</i>") : Component.text(this.owner.getName());
    }

    public final @NotNull String ownerNameUnparsed() {
        return this.owner.getUniqueId().equals(BankAccounts.getConsoleOfflinePlayer().getUniqueId()) ? "<i>the server</i>" : this.owner.getName() == null ? "<i>unknown player</i>" : this.owner.getName();
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
        final @NotNull Material material = BankAccounts.getInstance().config().instrumentsMaterial();
        final @NotNull ItemStack instrument = new ItemStack(material);

        final @NotNull ItemMeta meta = instrument.getItemMeta();
        meta.displayName(BankAccounts.getInstance().config().instrumentsName(this, LocalDateTime.now(ZoneOffset.UTC)));
        meta.lore(BankAccounts.getInstance().config().instrumentsLore(this, LocalDateTime.now(ZoneOffset.UTC)));

        if (BankAccounts.getInstance().config().instrumentsGlintEnabled()) {
            final @NotNull Enchantment enchantment = BankAccounts.getInstance().config().instrumentsGlintEnchantment();
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
     * Get account by ID
     * @param id Account ID
     * @deprecated Use {@link #get(Account.Tag)}
     */
    @Deprecated
    public static @NotNull Optional<@NotNull Account> get(final @NotNull String id) {
        return getByID(id);
    }

    /**
     * Get account by ID
     * @param id Account ID
     */
    @ApiStatus.Internal
    private static @NotNull Optional<@NotNull Account> getByID(final @NotNull String id) {
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
     * Get account by tag
     */
    public static @NotNull Optional<@NotNull Account> get(final @NotNull Account.Tag tag) {
        return switch (tag.type) {
            case ID -> get(tag.value);
            case USERNAME -> {
                final @NotNull OfflinePlayer player = BankAccounts.getInstance().getServer().getOfflinePlayer(tag.value);
                yield getVaultAccount(player);
            }
        };
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
            BankAccounts.getInstance().getLogger().log(Level.SEVERE, "Could not get accounts for: " + owner.getUniqueId() + " (" + owner.getName() + "), type = " + (type == null ? "all" : type.name()), e);
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
     * Get the Vault account of a player
     */
    public static @NotNull Optional<@NotNull Account> getVaultAccount(final @NotNull OfflinePlayer player) {
        final @NotNull Account @NotNull [] accounts = get(player, Type.VAULT);
        if (accounts.length == 0) return Optional.empty();
        return Optional.of(accounts[0]);
    }

    /**
     * Get accounts sorted by balance
     *
     * @param limit Max number of accounts to return. If not set, all accounts are returned
     * @param page Page number starting from 1. Defaults to 1.
     * @param type If set, only accounts of this type are returned
     */
    public static @NotNull Account @NotNull [] getTopBalance(final @Nullable Integer limit, final @Nullable Integer page, final @Nullable Type type) {
        final @NotNull List<@NotNull Account> accounts = new ArrayList<>();
        final @NotNull String query;
        final int offset = (page != null ? page - 1 : 0) * (limit != null ? limit : 0);
        if (type == null) query = "SELECT * FROM `bank_accounts` WHERE `balance` IS NOT NULL AND `balance` > 0 ORDER BY `balance` DESC" + (limit != null ? " LIMIT ? OFFSET ?" : "");
        else query = "SELECT * FROM `bank_accounts` WHERE `balance` IS NOT NULL AND `balance` > 0 AND `type` = ? ORDER BY `balance` DESC" + (limit != null ? " LIMIT ? OFFSET ?" : "");
        try (final @NotNull Connection conn = BankAccounts.getInstance().getDb().getConnection();
             final @NotNull PreparedStatement stmt = conn.prepareStatement(query)) {
            if (type != null) {
                stmt.setInt(1, Type.getType(type));
                if (limit != null) {
                    stmt.setInt(2, limit);
                    stmt.setInt(3, offset);
                }
            }
            else if (limit != null) {
                stmt.setInt(1, limit);
                stmt.setInt(2, offset);
            }
            final @NotNull ResultSet rs = stmt.executeQuery();
            while (rs.next()) accounts.add(new Account(rs));
            return accounts.toArray(new Account[0]);
        }
        catch (final @NotNull Exception e) {
            BankAccounts.getInstance().getLogger().log(Level.SEVERE, "Could not get top balance accounts", e);
            return new Account[0];
        }
    }

    /*
     * Get the server account
     */
    public static @NotNull Optional<@NotNull Account> getServerAccount() {
        if (!BankAccounts.getInstance().config().serverAccountEnabled()) return Optional.empty();
        final @NotNull Optional<@NotNull Account> account = Arrays.stream(get(BankAccounts.getConsoleOfflinePlayer())).filter(a -> a.type != Type.VAULT).findFirst();
        return account;
    }

    /**
     * Get the server Vault account
     */
    public static @NotNull Optional<@NotNull Account> getServerVaultAccount() {
        if (!BankAccounts.getInstance().config().integrationsVaultEnabled()) return Optional.empty();
        return getVaultAccount(BankAccounts.getConsoleOfflinePlayer());
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
        BUSINESS,
        /**
         * Vault integration account
         */
        VAULT;

        /**
         * Get type name (as set in config)
         */
        public @NotNull String getName() {
            return BankAccounts.getInstance().config().messagesTypes(this);
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

    /**
     * An account tag is a unique pointer to a specific account.
     */
    public record Tag(@NotNull Account.Tag.Type type, @NotNull String value) {
        /**
         * Create a new account ID tag
         * @param id Account ID
         */
        public static @NotNull Tag id(final @NotNull String id) {
            return new Tag(Account.Tag.Type.ID, id);
        }

        /**
         * Create a new Vault account tag by username
         * @param username Username of Vault account holder
         */
        public static @NotNull Tag username(final @NotNull String username) {
            return new Tag(Account.Tag.Type.USERNAME, username);
        }

        /**
         * Create account tag from string
         * @param string Use {@code @} prefix for Vault account owner username.
         */
        public static @NotNull Tag from(final @NotNull String string) {
            if (string.startsWith("@")) return username(string.substring(1));
            return id(string);
        }

        /**
         * Get the account that this tag points to
         */
        public @NotNull Optional<@NotNull Account> get() {
            return Account.get(this);
        }

        /**
         * Account tag type
         */
        public enum Type {
            /**
             * Account by ID
             */
            ID,
            /**
             * Vault account by username
             */
            USERNAME
        }
    }
    
    /**
     * A request to change the owner of a bank account (sent to the new owner)
     */
    public final static class ChangeOwnerRequest {
        /**
         * Account id
         */
        public final @NotNull String account;

        /**
         * New owner UUID
         */
        public final @NotNull UUID newOwner;

        /**
         * Request creation timestamp
         */
        public final @NotNull Date created;

        /**
         * Create a new account ownership transfer request instance
         *
         * @param account Account to transfer ownership of
         * @param newOwner The new account owner
         */
        public ChangeOwnerRequest(final @NotNull Account account, final @NotNull UUID newOwner) {
            this.account = account.id;
            this.newOwner = newOwner;
            this.created = new Date();
        }

        private ChangeOwnerRequest(final @NotNull ResultSet rs) throws SQLException {
            this.account = rs.getString("id");
            this.newOwner = UUID.fromString(rs.getString("new_owner"));
            this.created = new Date(rs.getDate("created").getTime());
        }

        /**
         * Get account
         */
        public @NotNull Optional<@NotNull Account> account() {
            return Account.get(account);
        }

        /**
         * Get new owner
         */
        public @NotNull OfflinePlayer newOwner() {
            return BankAccounts.getInstance().getServer().getOfflinePlayer(newOwner);
        }

        /**
         * Check if request has expired
         */
        public boolean expired() {
            return System.currentTimeMillis() - created.getTime() > BankAccounts.getInstance().config().changeOwnerTimeout() * 6e4;
        }

        /**
         * Confirm/accept the request
         *
         * @return Whether the change was successful
         */
        public boolean confirm() {
            if (expired()) return false;
            final @NotNull Optional<@NotNull Account> account = this.account();
            if (account.isEmpty()) return false;
            if (account.get().frozen) return false;
            account.get().owner = newOwner();
            account.get().update();
            this.delete();
            return true;
        }

        /**
         * Insert into database
         */
        public void insert() {
            try (final @NotNull Connection conn = BankAccounts.getInstance().getDb().getConnection();
                 final @NotNull PreparedStatement stmt = conn.prepareStatement("INSERT INTO `change_owner_requests` (`account`, `new_owner`, `created`) VALUES (?, ?, ?)")) {
                stmt.setString(1, account);
                stmt.setString(2, newOwner.toString());
                stmt.setDate(3, new java.sql.Date(created.getTime()));

                stmt.executeUpdate();
            }
            catch (final @NotNull Exception e) {
                BankAccounts.getInstance().getLogger().log(Level.SEVERE, "Could not save account ownership change request. account: " + account + ", newOwner: " + newOwner, e);
            }
        }

        /**
         * Delete from database
         */
        public void delete() {
            try (final @NotNull Connection conn = BankAccounts.getInstance().getDb().getConnection();
                 final @NotNull PreparedStatement stmt = conn.prepareStatement("DELETE FROM `change_owner_requests` WHERE `account` = ? AND `new_owner` = ?")) {
                stmt.setString(1, account);
                stmt.setString(2, newOwner.toString());
                stmt.executeUpdate();
            }
            catch (final @NotNull Exception e) {
                BankAccounts.getInstance().getLogger().log(Level.SEVERE, "Could not delete account ownership change request. account: " + account + ", newOwner: " + newOwner, e);
            }
        }

        /**
         * Delete all request to transfer a certain account
         *
         * @param account Account ID
         */
        public static void delete(final @NotNull UUID account) {
            try (final @NotNull Connection conn = BankAccounts.getInstance().getDb().getConnection();
                 final @NotNull PreparedStatement stmt = conn.prepareStatement("DELETE FROM `change_owner_requests` WHERE `account` = ?")) {
                stmt.setString(1, account.toString());
                stmt.executeUpdate();
            }
            catch (final @NotNull Exception e) {
                BankAccounts.getInstance().getLogger().log(Level.SEVERE, "Could not delete account ownership change request. account: " + account, e);
            }
        }

        /**
         * Delete expired requests
         */
        private static void deleteExpired() {
            try (final @NotNull Connection conn = BankAccounts.getInstance().getDb().getConnection();
                 final @NotNull PreparedStatement stmt = conn.prepareStatement("DELETE FROM `change_owner_requests` WHERE `created` = ?")) {
                stmt.setDate(1, new java.sql.Date(System.currentTimeMillis() - BankAccounts.getInstance().config().changeOwnerTimeout() * 60_000L));
                stmt.executeUpdate();
            }
            catch (final @NotNull Exception e) {
                BankAccounts.getInstance().getLogger().log(Level.SEVERE, "Could not delete account ownership change request. account: ", e);
            }
        }

        /**
         * Asynchronously delete expired requests
         */
        public final static @NotNull Runnable deleteExpiredLater = () -> BankAccounts.getInstance().getServer().getScheduler().runTaskAsynchronously(BankAccounts.getInstance(), ChangeOwnerRequest::deleteExpired);

        /**
         * Get account ownership change request
         *
         * @param account Account ID
         * @param newOwner New owner
         */
        public static @NotNull Optional<@NotNull ChangeOwnerRequest> get(final @NotNull String account, @NotNull OfflinePlayer newOwner) {
            try (final @NotNull Connection conn = BankAccounts.getInstance().getDb().getConnection();
                 final @NotNull PreparedStatement stmt = conn.prepareStatement("SELECT * FROM `change_owner_requests` WHERE `account` = ? AND `new_owner` = ? LIMIT 1")) {
                stmt.setString(1, account);
                stmt.setString(2, newOwner.getUniqueId().toString());
                final @NotNull ResultSet rs = stmt.executeQuery();
                return rs.next() ? Optional.of(new ChangeOwnerRequest(rs)) : Optional.empty();
            }
            catch (final @NotNull Exception e) {
                BankAccounts.getInstance().getLogger().log(Level.SEVERE, "Could not get account ownership change request. account: " + account + ", newOwner: " + newOwner.getUniqueId(), e);
                return Optional.empty();
            }
        }

        /**
         * Get account ownership change requests
         *
         * @param newOwner New owner
         */
        public static @NotNull Account @NotNull [] get(final @NotNull OfflinePlayer newOwner) {
            try (final @NotNull Connection conn = BankAccounts.getInstance().getDb().getConnection();
                 final @NotNull PreparedStatement stmt = conn.prepareStatement("SELECT * FROM `change_owner_requests` WHERE `new_owner` = ?")) {
                stmt.setString(1, newOwner.getUniqueId().toString());
                final @NotNull ResultSet rs = stmt.executeQuery();

                final @NotNull List<@NotNull Account> accounts = new ArrayList<>();
                while (rs.next()) accounts.add(new Account(rs));
                return accounts.toArray(new Account[0]);
            }
            catch (final @NotNull Exception e) {
                BankAccounts.getInstance().getLogger().log(Level.SEVERE, "Could not get account ownership change requests. newOwner: " + newOwner.getUniqueId(), e);
                return new Account[0];
            }
        }
    }
}
