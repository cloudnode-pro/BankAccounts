package pro.cloudnode.smp.bankaccounts;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
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
    public final String id;
    /**
     * Account owner
     */
    public final @NotNull OfflinePlayer owner;
    /**
     * Account type
     */
    public final Type type;
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
    public Account(String id, @NotNull OfflinePlayer owner, Type type, @Nullable String name, @Nullable BigDecimal balance, boolean frozen) {
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
    public Account(@NotNull OfflinePlayer owner, Type type, String name, BigDecimal balance, boolean frozen) {
        this(StringGenerator.generate(16), owner, type, name, balance, frozen);
    }

    /**
     * Create bank account instance from database result set
     * @param rs Database result set
     */
    public Account(ResultSet rs) throws SQLException {
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
    public void updateBalance(BigDecimal diff) {
        if (balance == null) return;
        this.balance = balance.add(diff);
        this.save();
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
    public Transaction transfer(Account to, BigDecimal amount, @Nullable String description, @Nullable String instrument) {
        if (frozen) throw new IllegalStateException("Your account is frozen");
        if (to.frozen) throw new IllegalStateException("Recipient account is frozen");
        if (amount.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("Amount must be greater than zero");
        if (balance != null && balance.compareTo(amount) < 0) throw new IllegalStateException("Insufficient funds");

        Transaction transaction = new Transaction(this.id, to.id, amount, description, instrument);
        transaction.save();
        this.updateBalance(amount.negate());
        to.updateBalance(amount);
        return transaction;
    }

    /**
     * Get account by ID
     * @param id Account ID
     */
    public static Optional<Account> get(String id) {
        try (Connection conn = BankAccounts.getInstance().getDb().getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM `bank_accounts` WHERE `id` = ? LIMIT 1")) {
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? Optional.of(new Account(rs)) : Optional.empty();
        }
        catch (Exception e) {
            BankAccounts.getInstance().getLogger().log(Level.SEVERE, "Could not get account: " + id, e);
            return Optional.empty();
        }
    }

    /**
     * Get accounts by owner
     * @param owner Account owner
     * @param type Account type
     */
    public static Account[] get(OfflinePlayer owner, @Nullable Type type) {
        List<Account> accounts = new ArrayList<>();
        try (Connection conn = BankAccounts.getInstance().getDb().getConnection();
             PreparedStatement stmt = conn.prepareStatement(type == null ? "SELECT * FROM `bank_accounts` WHERE `owner` = ?" : "SELECT * FROM `bank_accounts` WHERE `owner` = ? AND `type` = ?")) {
            stmt.setString(1, owner.getUniqueId().toString());
            if (type != null) stmt.setInt(2, Type.getType(type));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) accounts.add(new Account(rs));
            return accounts.toArray(new Account[0]);
        }
        catch (Exception e) {
            BankAccounts.getInstance().getLogger().log(Level.SEVERE, "Could not get accounts for: " + owner.getUniqueId().toString() + " (" + owner.getName() + "), type = " + (type == null ? "all" : type.name()), e);
            return new Account[0];
        }
    }

    /**
     * Get accounts by owner
     * @param owner Account owner
     */
    public static Account[] get(OfflinePlayer owner) {
        return get(owner, null);
    }

    /**
     * Get all accounts
     */
    public static Account[] get() {
        List<Account> accounts = new ArrayList<>();
        try (Connection conn = BankAccounts.getInstance().getDb().getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM `bank_accounts`")) {
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) accounts.add(new Account(rs));
            return accounts.toArray(new Account[0]);
        } catch (Exception e) {
            BankAccounts.getInstance().getLogger().log(Level.SEVERE, "Could not get accounts", e);
            return new Account[0];
        }
    }

    /**
     * Insert or update account into database
     */
    public void save() {
        try (Connection conn = BankAccounts.getInstance().getDb().getConnection();
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO `bank_accounts` (`id`, `owner`, `type`, `name`, `balance`, `frozen`) VALUES (?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE `name` = ?, `balance` = ?, `frozen` = ?")) {
            // insert
            stmt.setString(1, id);
            stmt.setString(2, owner.getUniqueId().toString());
            stmt.setInt(3, Type.getType(type));
            if (name == null) stmt.setNull(4, java.sql.Types.VARCHAR);
            else stmt.setString(4, name);
            if (balance == null) stmt.setNull(5, java.sql.Types.DECIMAL);
            else stmt.setBigDecimal(5, balance);
            stmt.setBoolean(6, frozen);
            // update
            if (name == null) stmt.setNull(7, java.sql.Types.VARCHAR);
            else stmt.setString(7, name);
            if (balance == null) stmt.setNull(8, java.sql.Types.DECIMAL);
            else stmt.setBigDecimal(8, balance);
            stmt.setBoolean(9, frozen);

            stmt.executeUpdate();
        } catch (Exception e) {
            BankAccounts.getInstance().getLogger().log(Level.SEVERE, "Could not save account: " + id, e);
        }
    }

    /**
     * Bank account type
     */
    public static enum Type {
        /**
         * Personal, individual, private account
         */
        PERSONAL("Personal"),
        /**
         * Account owned by a company or other corporate entity
         */
        BUSINESS("Business");

        /**
         * User-friendly name
         */
        public final @NotNull String name;

        Type(@NotNull String name) {
            this.name = name;
        }

        /**
         * Convert account type to integer
         * @param type Account type
         * @return Account type as integer
         */
        public static int getType(Type type) {
            return type.ordinal();
        }

        /**
         * Convert integer to account type
         * @param type Account type as integer
         * @return Account type
         */
        public static Type getType(int type) {
            return Type.values()[type];
        }

        public static Optional<Type> fromString(String name) {
            for (Type type : Type.values()) {
                if (type.name.equalsIgnoreCase(name)) return Optional.of(type);
            }
            return Optional.empty();
        }
    }
}
