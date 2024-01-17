package pro.cloudnode.smp.bankaccounts;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

/**
 * Bank account transaction
 */
public class Transaction {
    /**
     * Unique transaction ID
     */
    private int id;

    /**
     * Get transaction ID
     */
    public int getId() {
        if (id == -1) throw new IllegalStateException("Transaction has not been saved to the database yet");
        return id;
    }

    /**
     * Sender account ID
     */
    public final Account from;
    /**
     * Recipient account ID
     */
    public final Account to;
    /**
     * Transaction amount.
     * <p>
     * This amount was deducted from the sender's account and added to the recipient's account.
     */
    public final BigDecimal amount;
    /**
     * Transaction date and time
     */
    public final Date time;
    /**
     * Transaction description
     */
    public final @Nullable String description;
    /**
     * Payment instrument used to facilitate the transaction
     */
    public final @Nullable String instrument;

    /**
     * Create a new transaction instance
     * @param id Unique transaction ID
     * @param from Sender account ID
     * @param to Recipient account ID
     * @param amount Transaction amount (deducted from sender, added to recipient)
     * @param time Transaction date and time
     * @param description Transaction description
     * @param instrument Payment instrument used to facilitate the transaction
     */
    public Transaction(int id, Account from, Account to, BigDecimal amount, Date time, @Nullable String description, @Nullable String instrument) {
        this.id = id;
        this.from = from;
        this.to = to;
        this.amount = amount;
        this.time = time;
        this.description = description;
        this.instrument = instrument;
    }

    /**
     * Create a new transaction
     * @param from Sender account ID
     * @param to Recipient account ID
     * @param amount Transaction amount (deducted from sender, added to recipient)
     * @param description Transaction description
     * @param instrument Payment instrument used to facilitate the transaction
     */
    public Transaction(Account from, Account to, BigDecimal amount, @Nullable String description, @Nullable String instrument) {
        this(-1, from, to, amount, new Date(), description, instrument);
    }

    /**
     * Create a new transaction from a database result set
     * @param rs Result set
     */
    public Transaction(ResultSet rs) throws SQLException {
        this(rs.getInt("id"), Account.get(rs.getString("from")).orElse(new Account.ClosedAccount()), Account.get(rs.getString("to")).orElse(new Account.ClosedAccount()), rs.getBigDecimal("amount"), rs.getTimestamp("time"), rs.getString("description"), rs.getString("instrument"));
    }

    /**
     * Insert transaction in the database
     */
    public void save() {
        try (Connection conn = BankAccounts.getInstance().getDb().getConnection();
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO `bank_transactions` (`from`, `to`, `amount`, `time`, `description`, `instrument`) VALUES (?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, from.id);
            stmt.setString(2, to.id);
            stmt.setBigDecimal(3, amount);
            stmt.setTimestamp(4, new Timestamp(time.getTime()));
            if (description == null) stmt.setNull(5, Types.VARCHAR);
            else stmt.setString(5, description);
            if (instrument == null) stmt.setNull(6, Types.VARCHAR);
            else stmt.setString(6, instrument);

            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) this.id = rs.getInt(1);
        } catch (Exception e) {
            BankAccounts.getInstance().getLogger().log(Level.SEVERE, "Could not save transaction: " + this.id, e);
        }
    }

    /**
     * Get ALL transactions of account
     * @param account Account
     */
    public static Transaction[] get(Account account) {
        try (Connection conn = BankAccounts.getInstance().getDb().getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM `bank_transactions` WHERE `from` = ? OR `to` = ? ORDER BY `time` DESC")) {
            stmt.setString(1, account.id);
            stmt.setString(2, account.id);
            ResultSet rs = stmt.executeQuery();
            List<Transaction> transactions = new ArrayList<>();
            while (rs.next()) transactions.add(new Transaction(rs));
            return transactions.toArray(new Transaction[0]);
        } catch (Exception e) {
            BankAccounts.getInstance().getLogger().log(Level.SEVERE, "Could not get transactions of account: " + account.id, e);
            return new Transaction[0];
        }
    }

    /**
     * Get transactions of account
     * @param account Account
     * @param limit Maximum number of transactions to return per page.
     * @param page Page number (starting from 1)
     */
    public static Transaction[] get(Account account, int limit, int page) {
        int offset = (page - 1) * limit;
        try (Connection conn = BankAccounts.getInstance().getDb().getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM `bank_transactions` WHERE `from` = ? OR `to` = ? ORDER BY `time` DESC LIMIT ? OFFSET ?")) {
            stmt.setString(1, account.id);
            stmt.setString(2, account.id);
            stmt.setInt(3, limit);
            stmt.setInt(4, offset);
            ResultSet rs = stmt.executeQuery();
            List<Transaction> transactions = new ArrayList<>();
            while (rs.next()) transactions.add(new Transaction(rs));
            return transactions.toArray(new Transaction[0]);
        } catch (Exception e) {
            BankAccounts.getInstance().getLogger().log(Level.SEVERE, "Could not get transactions of account: " + account.id, e);
            return new Transaction[0];
        }
    }

    /**
     * Count transactions of account
     * @param account Account
     */
    public static int count(Account account) {
        try (Connection conn = BankAccounts.getInstance().getDb().getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM `bank_transactions` WHERE `from` = ? OR `to` = ?")) {
            stmt.setString(1, account.id);
            stmt.setString(2, account.id);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (Exception e) {
            BankAccounts.getInstance().getLogger().log(Level.SEVERE, "Could not count transactions of account: " + account.id, e);
            return 0;
        }
    }
}
