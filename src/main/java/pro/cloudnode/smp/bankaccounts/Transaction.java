package pro.cloudnode.smp.bankaccounts;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.Date;

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
}
