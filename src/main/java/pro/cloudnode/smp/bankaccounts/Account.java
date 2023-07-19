package pro.cloudnode.smp.bankaccounts;

import org.bukkit.OfflinePlayer;

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
    public final OfflinePlayer owner;
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
    public BigDecimal balance;
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
    public Account(String id, OfflinePlayer owner, Type type, String name, BigDecimal balance, boolean frozen) {
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
     */
    public Account(OfflinePlayer owner, Type type, String name) {
        this(StringGenerator.generate(16), owner, type, name, BigDecimal.ZERO, false);
    }

    /**
     * Create bank account instance from database result set
     * @param rs Database result set
     */
    public Account(ResultSet rs) throws SQLException {
        this(
                rs.getString("id"),
                BankAccounts.getInstance().getServer().getOfflinePlayer(UUID.fromString(rs.getString("owner"))),
                getType(rs.getInt("type")),
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
        this.balance = balance.add(diff);
        this.save();
    }

    /**
     * Get account by ID
     * @param id Account ID
     */
    public static Optional<Account> getByID(String id) {
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
            if (type != null) stmt.setInt(2, getType(type));
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
     * Insert or update account into database
     */
    public void save() {
        try (Connection conn = BankAccounts.getInstance().getDb().getConnection();
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO `bank_accounts` (`id`, `owner`, `type`, `name`, `balance`, `frozen`) VALUES (?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE `name` = ?, `balance` = ?, `frozen` = ?")) {
            // insert
            stmt.setString(1, id);
            stmt.setString(2, owner.getUniqueId().toString());
            stmt.setInt(3, getType(type));
            stmt.setString(4, name);
            stmt.setBigDecimal(5, balance);
            stmt.setBoolean(6, frozen);
            // update
            stmt.setString(7, name);
            stmt.setBigDecimal(8, balance);
            stmt.setBoolean(9, frozen);

            stmt.execute();
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
        PERSONAL,
        /**
         * Account owned by a company or other corporate entity
         */
        BUSINESS
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
}
