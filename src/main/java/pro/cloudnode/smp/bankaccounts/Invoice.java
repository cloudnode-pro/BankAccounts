package pro.cloudnode.smp.bankaccounts;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

/**
 * A payment request
 */
public class Invoice {
    public final @NotNull String id;
    /**
     * The beneficiary
     */
    public final @NotNull Account seller;
    public final @NotNull BigDecimal amount;
    private final @Nullable String description;

    /**
     * A potential buyer/payer. If set, only they can see and pay the invoice. If `null`, the invoice is public.
     */
    private final @Nullable OfflinePlayer buyer;
    public final @NotNull Date created;
    /**
     * Payment transaction. If set, the invoice is paid.
     */
    public @Nullable Transaction transaction;

    public Invoice(final @NotNull String id, final @NotNull Account seller, final @NotNull BigDecimal amount, final @Nullable String description, final @Nullable OfflinePlayer buyer, final @NotNull Date created, final @Nullable Transaction transaction) {
        this.id = id;
        this.seller = seller;
        this.amount = amount;
        this.description = description;
        this.buyer = buyer;
        this.created = created;
        this.transaction = transaction;
    }

    public Invoice(final @NotNull ResultSet rs) throws @NotNull SQLException {
        this(
                rs.getString("id"),
                Account.get(rs.getString("seller")).orElse(new Account.ClosedAccount()),
                rs.getBigDecimal("amount"),
                rs.getString("description"),
                rs.getString("buyer") == null ? null : BankAccounts.getInstance().getServer().getOfflinePlayer(UUID.fromString(rs.getString("buyer"))),
                new Date(rs.getTimestamp("created").getTime()),
                Transaction.get(rs.getInt("transaction")).orElse(null)
        );
    }

    public @NotNull Optional<@NotNull String> description() {
        return Optional.ofNullable(description);
    }

    public @NotNull Optional<@NotNull OfflinePlayer> buyer() {
        return Optional.ofNullable(buyer);
    }

    public void pay(final @NotNull Account buyer) {
        this.transaction = buyer.transfer(this.seller, this.amount, "Invoice #" + this.id, null);
    }

    public void insert() {
        try (final @NotNull Connection conn = BankAccounts.getInstance().getDb().getConnection();
             final @NotNull PreparedStatement stmt = conn.prepareStatement("INSERT INTO `bank_invoices` (`id`, `seller`, `amount`, `description`, `buyer`, `created`, `transaction`) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
            stmt.setString(1, id);
            stmt.setString(2, seller.id);
            stmt.setBigDecimal(3, amount);
            if (description == null) stmt.setNull(4, java.sql.Types.VARCHAR);
            else stmt.setString(4, description);
            if (buyer == null) stmt.setNull(5, java.sql.Types.VARCHAR);
            else stmt.setString(5, buyer.getUniqueId().toString());
            stmt.setTimestamp(6, new java.sql.Timestamp(created.getTime()));
            if (transaction == null) stmt.setNull(7, java.sql.Types.INTEGER);
            else stmt.setInt(7, transaction.getId());

            stmt.executeUpdate();
        } catch (final @NotNull Exception e) {
            BankAccounts.getInstance().getLogger().log(Level.SEVERE, "Could not save invoice: " + id, e);
        }
    }

    public void update() {
        try (final @NotNull Connection conn = BankAccounts.getInstance().getDb().getConnection();
             final @NotNull PreparedStatement stmt = conn.prepareStatement("UPDATE `bank_invoices` SET `transaction` = ? WHERE `id` = ?")) {
            if (transaction == null) stmt.setNull(1, Types.INTEGER);
            else stmt.setInt(1, transaction.getId());
            stmt.setString(2, id);

            stmt.executeUpdate();
        } catch (final @NotNull Exception e) {
            BankAccounts.getInstance().getLogger().log(Level.SEVERE, "Could not update invoice: " + id, e);
        }
    }

    public static @NotNull Optional<@NotNull Invoice> get(final @NotNull String id) {
        try (final @NotNull Connection conn = BankAccounts.getInstance().getDb().getConnection();
             final @NotNull PreparedStatement stmt = conn.prepareStatement("SELECT * FROM `bank_invoices` WHERE `id` = ? LIMIT 1")) {
            stmt.setString(1, id);

            stmt.executeQuery();
            final @NotNull ResultSet rs = stmt.getResultSet();

            if (rs.next()) return Optional.of(new Invoice(rs));
            return Optional.empty();
        }
        catch (final @NotNull Exception e) {
            BankAccounts.getInstance().getLogger().log(Level.SEVERE, "Could not get invoice: " + id, e);
            return Optional.empty();
        }
    }
}
