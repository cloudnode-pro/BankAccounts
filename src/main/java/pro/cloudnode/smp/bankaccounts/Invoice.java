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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

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

    public Invoice(final @NotNull Account seller, final @NotNull BigDecimal amount, final @Nullable String description, final @Nullable OfflinePlayer buyer) {
        this(StringGenerator.generate(16), seller, amount, description, buyer, new Date(), null);
    }

    public Invoice(final @NotNull ResultSet rs) throws @NotNull SQLException {
        this(
                rs.getString("id"),
                Account.get(Account.Tag.id(rs.getString("seller"))).orElse(new Account.ClosedAccount()),
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
        transaction = buyer.transfer(seller, amount, "Invoice #" + id, null);
        update();
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
        } catch (final @NotNull SQLException e) {
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
        } catch (final @NotNull SQLException e) {
            BankAccounts.getInstance().getLogger().log(Level.SEVERE, "Could not update invoice: " + id, e);
        }
    }

    public static @NotNull Optional<@NotNull Invoice> get(final @NotNull String id) {
        try (final @NotNull Connection conn = BankAccounts.getInstance().getDb().getConnection();
             final @NotNull PreparedStatement stmt = conn.prepareStatement("SELECT * FROM `bank_invoices` WHERE `id` = ? LIMIT 1")) {
            stmt.setString(1, id);

            final @NotNull ResultSet rs = stmt.executeQuery();

            if (rs.next()) return Optional.of(new Invoice(rs));
            return Optional.empty();
        }
        catch (final @NotNull SQLException e) {
            BankAccounts.getInstance().getLogger().log(Level.SEVERE, "Could not get invoice: " + id, e);
            return Optional.empty();
        }
    }

    public static @NotNull Invoice @NotNull [] get(final @NotNull OfflinePlayer player) {
        try (final @NotNull Connection conn = BankAccounts.getInstance().getDb().getConnection();
             final @NotNull PreparedStatement stmt = conn.prepareStatement("SELECT * FROM `bank_invoices` where `buyer` = ?")) {
            stmt.setString(1, player.getUniqueId().toString());
            final @NotNull ResultSet rs = stmt.executeQuery();

            final @NotNull List<@NotNull Invoice> invoices = new ArrayList<>();
            while (rs.next()) invoices.add(new Invoice(rs));
            return invoices.toArray(new @NotNull Invoice[0]);
        }
        catch (final @NotNull SQLException e) {
            BankAccounts.getInstance().getLogger().log(Level.SEVERE, "Could not get invoices for player: " + player.getUniqueId(), e);
            return new @NotNull Invoice[0];
        }
    }

    public static @NotNull Invoice @NotNull [] get(final @NotNull OfflinePlayer player, final int limit, final int offset) {
        try (final @NotNull Connection conn = BankAccounts.getInstance().getDb().getConnection();
             final @NotNull PreparedStatement stmt = conn.prepareStatement("SELECT * FROM `bank_invoices` where `buyer` = ? ORDER BY `created` DESC LIMIT ? OFFSET ?")) {
            stmt.setString(1, player.getUniqueId().toString());
            stmt.setInt(2, limit);
            stmt.setInt(3, offset);
            final @NotNull ResultSet rs = stmt.executeQuery();

            final @NotNull List<@NotNull Invoice> invoices = new ArrayList<>();
            while (rs.next()) invoices.add(new Invoice(rs));
            return invoices.toArray(new @NotNull Invoice[0]);
        }
        catch (final @NotNull SQLException e) {
            BankAccounts.getInstance().getLogger().log(Level.SEVERE, "Could not get invoices for player: " + player.getUniqueId(), e);
            return new @NotNull Invoice[0];
        }
    }

    public static @NotNull Invoice @NotNull [] get(final @NotNull OfflinePlayer player, final @NotNull Account @NotNull [] seller) {
        final @NotNull String inParams = Arrays.stream(seller).map(s -> "?").collect(Collectors.joining(", "));
        try (final @NotNull Connection conn = BankAccounts.getInstance().getDb().getConnection();
            final @NotNull PreparedStatement stmt = conn.prepareStatement("SELECT * FROM `bank_invoices` where `buyer` = ? OR `seller` IN (" + inParams + ")")) {
            stmt.setString(1, player.getUniqueId().toString());
            for (int i = 0; i < seller.length; ++i) stmt.setString(i + 2, seller[i].id);

            final @NotNull ResultSet rs = stmt.executeQuery();
            final @NotNull List<@NotNull Invoice> invoices = new ArrayList<>();
            while (rs.next()) invoices.add(new Invoice(rs));
            return invoices.toArray(new Invoice[0]);
        }
        catch (final @NotNull SQLException e) {
            BankAccounts.getInstance().getLogger().log(Level.SEVERE, "Could not get invoices for buyer & seller: " + player.getUniqueId(), e);
            return new @NotNull Invoice[0];
        }
    }

    public static @NotNull Invoice @NotNull [] get(final @NotNull OfflinePlayer player, final @NotNull Account @NotNull [] seller, final int limit, final int offset) {
        final @NotNull String inParams = Arrays.stream(seller).map(s -> "?").collect(Collectors.joining(", "));
        try (final @NotNull Connection conn = BankAccounts.getInstance().getDb().getConnection();
             final @NotNull PreparedStatement stmt = conn.prepareStatement("SELECT * FROM `bank_invoices` where `buyer` = ? OR `seller` IN (" + inParams + ") ORDER BY `created` DESC LIMIT ? OFFSET ?")) {
            stmt.setString(1, player.getUniqueId().toString());
            for (int i = 0; i < seller.length; ++i) stmt.setString(i + 2, seller[i].id);
            stmt.setInt(seller.length + 2, limit);
            stmt.setInt(seller.length + 3, offset);

            final @NotNull ResultSet rs = stmt.executeQuery();
            final @NotNull List<@NotNull Invoice> invoices = new ArrayList<>();
            while (rs.next()) invoices.add(new Invoice(rs));
            return invoices.toArray(new Invoice[0]);
        }
        catch (final @NotNull SQLException e) {
            BankAccounts.getInstance().getLogger().log(Level.SEVERE, "Could not get invoices for buyer & seller: " + player.getUniqueId(), e);
            return new @NotNull Invoice[0];
        }
    }

    public static @NotNull Invoice @NotNull [] get(final @NotNull Account @NotNull [] seller, final int limit, final int offset) {
        final @NotNull String inParams = Arrays.stream(seller).map(s -> "?").collect(Collectors.joining(", "));
        try (final @NotNull Connection conn = BankAccounts.getInstance().getDb().getConnection();
            final @NotNull PreparedStatement stmt = conn.prepareStatement("SELECT * FROM `bank_invoices` where `seller` IN (" + inParams + ") ORDER BY `created` DESC LIMIT ? OFFSET ?")) {
            for (int i = 0; i < seller.length; ++i) stmt.setString(i + 1, seller[i].id);
            stmt.setInt(seller.length + 1, limit);
            stmt.setInt(seller.length + 2, offset);

            final @NotNull ResultSet rs = stmt.executeQuery();
            final @NotNull List<@NotNull Invoice> invoices = new ArrayList<>();
            while (rs.next()) invoices.add(new Invoice(rs));
            return invoices.toArray(new Invoice[0]);
        }
        catch (final @NotNull SQLException e) {
            BankAccounts.getInstance().getLogger().log(Level.SEVERE, "Could not get invoices for seller: " + Arrays.toString(seller), e);
            return new @NotNull Invoice[0];
        }
    }

    public static @NotNull Invoice @NotNull [] get() {
        try (final @NotNull Connection conn = BankAccounts.getInstance().getDb().getConnection();
             final @NotNull PreparedStatement stmt = conn.prepareStatement("SELECT * FROM `bank_invoices`")) {
            final @NotNull ResultSet rs = stmt.executeQuery();

            final @NotNull List<@NotNull Invoice> invoices = new ArrayList<>();
            while (rs.next()) invoices.add(new Invoice(rs));
            return invoices.toArray(new @NotNull Invoice[0]);
        }
        catch (final @NotNull SQLException e) {
            BankAccounts.getInstance().getLogger().log(Level.SEVERE, "Could not get invoices", e);
            return new @NotNull Invoice[0];
        }
    }

    public static int countUnpaid(final @NotNull OfflinePlayer player) {
        try (final @NotNull Connection conn = BankAccounts.getInstance().getDb().getConnection();
             final @NotNull PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(`id`) as `count` FROM `bank_invoices` WHERE `buyer` = ? AND `transaction` IS NULL")) {
            stmt.setString(1, player.getUniqueId().toString());

            final @NotNull ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("count");
            return 0;
        }
        catch (final @NotNull SQLException e) {
            BankAccounts.getInstance().getLogger().log(Level.SEVERE, "Could not count unpaid invoices for player: " + player.getUniqueId(), e);
            return 0;
        }
    }
}
