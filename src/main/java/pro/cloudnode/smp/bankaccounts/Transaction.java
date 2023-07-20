package pro.cloudnode.smp.bankaccounts;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Date;
import java.util.Optional;
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
    public final String from;
    /**
     * Recipient account ID
     */
    public final String to;
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
    public Transaction(int id, String from, String to, BigDecimal amount, Date time, @Nullable String description, @Nullable String instrument) {
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
    public Transaction(String from, String to, BigDecimal amount, @Nullable String description, @Nullable String instrument) {
        this(-1, from, to, amount, new Date(), description, instrument);
    }

    /**
     * Create a new transaction from a database result set
     * @param rs Result set
     */
    public Transaction(ResultSet rs) throws SQLException {
        this(rs.getInt("id"), rs.getString("from"), rs.getString("to"), rs.getBigDecimal("amount"), rs.getTimestamp("time"), rs.getString("description"), rs.getString("instrument"));
    }

    /**
     * Get sender account
     */
    public Optional<Account> getFrom() {
        return Account.getByID(from);
    }

    /**
     * Get recipient account
     */
    public Optional<Account> getTo() {
        return Account.getByID(to);
    }

    /**
     * Insert transaction in the database
     */
    public void save() {
        try (Connection conn = BankAccounts.getInstance().getDb().getConnection();
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO `bank_transactions` (`from`, `to`, `amount`, `time`, `description`, `instrument`) VALUES (?, ?, ?, ?, ?, ?)")) {
            stmt.setString(1, from);
            stmt.setString(2, to);
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
     * Get transaction by ID
     * @param id Transaction ID
     */
    public static Optional<Transaction> getByID(int id) {
        try (Connection conn = BankAccounts.getInstance().getDb().getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM `bank_transactions` WHERE `id` = ? LIMIT 1")) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? Optional.of(new Transaction(rs)) : Optional.empty();
        } catch (Exception e) {
            BankAccounts.getInstance().getLogger().log(Level.SEVERE, "Could not get transaction: " + id, e);
            return Optional.empty();
        }
    }
}
