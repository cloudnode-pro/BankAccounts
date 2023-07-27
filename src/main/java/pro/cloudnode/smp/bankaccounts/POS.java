package pro.cloudnode.smp.bankaccounts;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.inventory.DoubleChestInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

public final class POS {
    /**
     * POS X coordinate
     */
    public final int x;
    /**
     * POS Y coordinate
     */
    public final int y;
    /**
     * POS Z coordinate
     */
    public final int z;
    /**
     * POS world
     */
    public final @NotNull World world;
    /**
     * Price of POS contents
     */
    public final @NotNull BigDecimal price;
    /**
     * Description that appears on the bank statement
     */
    public final @Nullable String description;
    /**
     * Account that receives the money from the sale
     */
    public final @NotNull Account seller;
    /**
     * Date the POS was created
     */
    public final @NotNull Date created;


    /**
     * Create new POS instance
     *
     * @param x           POS X coordinate
     * @param y           POS Y coordinate
     * @param z           POS Z coordinate
     * @param world       POS world
     * @param price       Price of POS contents
     * @param description Description that appears on the bank statement
     * @param seller      Account that receives the money from the sale
     * @param created     Date the POS was created
     */
    public POS(final int x, final int y, final int z, final @NotNull World world, final @NotNull BigDecimal price, final String description, final @NotNull Account seller, final @NotNull Date created) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.world = world;
        this.price = price;
        this.description = description;
        this.seller = seller;
        this.created = created;
    }

    /**
     * Create new POS instance
     *
     * @param location    Location of the POS
     * @param price       Price of POS contents
     * @param description Description that appears on the bank statement
     * @param seller      Account that receives the money from the sale
     * @param created     Date the POS was created
     */
    public POS(final @NotNull Location location, final @NotNull BigDecimal price, final String description, final @NotNull Account seller, final @NotNull Date created) {
        this.x = location.getBlockX();
        this.y = location.getBlockY();
        this.z = location.getBlockZ();
        this.world = location.getWorld();
        this.price = price;
        this.description = description;
        this.seller = seller;
        this.created = created;
    }

    /**
     * Create new POS from SQL result
     *
     * @param rs Result set
     */
    public POS(final @NotNull ResultSet rs) throws SQLException, IllegalStateException {
        this.x = rs.getInt("x");
        this.y = rs.getInt("y");
        this.z = rs.getInt("z");
        final World world = BankAccounts.getInstance().getServer().getWorld(rs.getString("world"));
        if (world == null) throw new IllegalStateException("World not found: " + rs.getString("world"));
        this.world = world;
        this.price = rs.getBigDecimal("price");
        this.description = rs.getString("description");
        this.seller = Account.get(rs.getString("seller")).orElse(new Account.ClosedAccount());
        this.created = rs.getDate("created");
    }

    /**
     * Get location of the POS
     */
    public @NotNull Location getLocation() {
        return new Location(world, x, y, z);
    }

    /**
     * Get POS block
     */
    public @NotNull Block getBlock() {
        return world.getBlockAt(x, y, z);
    }

    /**
     * Save POS to database
     */
    public void save() {
        try (final Connection conn = BankAccounts.getInstance().getDb().getConnection();
             final PreparedStatement stmt = conn.prepareStatement("INSERT INTO `pos` (`x`, `y`, `z`, `world`, `price`, `description`, `seller`, `created`) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
            stmt.setInt(1, x);
            stmt.setInt(2, y);
            stmt.setInt(3, z);
            stmt.setString(4, world.getName());
            stmt.setBigDecimal(5, price);
            if (description == null) stmt.setNull(6, Types.VARCHAR);
            else stmt.setString(6, description);
            stmt.setString(7, seller.id);
            stmt.setTimestamp(8, new Timestamp(created.getTime()));

            stmt.executeUpdate();
        } catch (final SQLException e) {
            BankAccounts.getInstance().getLogger().log(Level.SEVERE, "Could not save POS in " + world.getName() + " at X: " + x + " Y: " + y + " Z: " + z, e);
        }
    }

    /**
     * Delete POS
     */
    public void delete() {
        try (final Connection conn = BankAccounts.getInstance().getDb().getConnection();
             final PreparedStatement stmt = conn.prepareStatement("DELETE FROM `pos` WHERE `x` = ? AND `y` = ? AND `z` = ? AND `world` = ? LIMIT 1")) {
            stmt.setInt(1, x);
            stmt.setInt(2, y);
            stmt.setInt(3, z);
            stmt.setString(4, world.getName());
            stmt.executeUpdate();
        } catch (final SQLException e) {
            BankAccounts.getInstance().getLogger().log(Level.SEVERE, "Could not delete POS in " + world.getName() + " at X: " + x + " Y: " + y + " Z: " + z, e);
        }
    }

    /**
     * Get POS
     *
     * @param x     POS X coordinate
     * @param y     POS Y coordinate
     * @param z     POS Z coordinate
     * @param world POS world
     */
    public static Optional<POS> get(final int x, final int y, final int z, final @NotNull World world) {
        try (final Connection conn = BankAccounts.getInstance().getDb().getConnection();
             final PreparedStatement stmt = conn.prepareStatement("SELECT * FROM `pos` WHERE `x` = ? AND `y` = ? AND `z` = ? AND `world` = ? LIMIT 1")) {
            stmt.setInt(1, x);
            stmt.setInt(2, y);
            stmt.setInt(3, z);
            stmt.setString(4, world.getName());
            final ResultSet rs = stmt.executeQuery();
            return rs.next() ? Optional.of(new POS(rs)) : Optional.empty();
        } catch (final SQLException e) {
            BankAccounts.getInstance().getLogger().log(Level.SEVERE, "Could not get POS in " + world.getName() + " at X: " + x + " Y: " + y + " Z: " + z, e);
            return Optional.empty();
        }
    }

    /**
     * Get POS
     *
     * @param location Location of the POS
     */
    public static Optional<POS> get(final @NotNull Location location) {
        return get(location.getBlockX(), location.getBlockY(), location.getBlockZ(), location.getWorld());
    }

    /**
     * Get POS
     *
     * @param block Block of the POS
     */
    public static Optional<POS> get(final @NotNull Block block) {
        return get(block.getLocation());
    }

    /**
     * Get POS
     * <p>
     * Takes into account double chests.
     *
     * @param chest Chest of the POS
     */
    public static Optional<POS> get(final @NotNull Chest chest) {
        if (chest.getInventory() instanceof final @NotNull DoubleChestInventory inventory) {
            final @Nullable Location left = inventory.getLeftSide().getLocation();
            final @Nullable Location right = inventory.getRightSide().getLocation();
            if (left != null) {
                final @NotNull Optional<POS> pos = get(left);
                if (pos.isPresent()) return pos;
            }
            if (right != null) {
                final @NotNull Optional<POS> pos = get(right);
                if (pos.isPresent()) return pos;
            }
            return Optional.empty();
        }
        else return get(chest.getLocation());
    }
}
