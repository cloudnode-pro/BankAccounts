package pro.cloudnode.smp.bankaccounts;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.zip.CRC32;

/**
 * Important: the chest shop is the SIGN, not the chest.
 */
public final class ChestShop {
    /**
     * X coordinate of the chest shop.
     */
    public final int x;

    /**
     * Y coordinate of the chest shop.
     */
    public final int y;

    /**
     * Z coordinate of the chest shop.
     */
    public final int z;

    /**
     * World of the chest shop.
     */
    public final @NotNull World world;

    /**
     * Chest shop mode.
     */
    public final @NotNull Mode mode;

    /**
     * Chest shop price.
     */
    public final @NotNull BigDecimal price;

    /**
     * The owner of the chest shop.
     */
    public final @NotNull Account owner;

    /**
     * The hash of the item being sold/bought.
     */
    public final @NotNull String item;

    /**
     * Creates a new chest shop.
     *
     * @param x     X coordinate of the chest shop.
     * @param y     Y coordinate of the chest shop.
     * @param z     Z coordinate of the chest shop.
     * @param world World of the chest shop.
     * @param mode  Chest shop mode.
     * @param price Chest shop price.
     * @param owner The owner of the chest shop.
     * @param item  The hash of the item being sold/bought.
     */
    public ChestShop(final int x, final int y, final int z, final @NotNull World world, final @NotNull Mode mode, final @NotNull BigDecimal price, final @NotNull Account owner, final @NotNull String item) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.world = world;
        this.mode = mode;
        this.price = price;
        this.owner = owner;
        this.item = item;
    }

    private ChestShop(final @NotNull ResultSet rs) throws SQLException {
        this.x = rs.getInt("x");
        this.y = rs.getInt("y");
        this.z = rs.getInt("z");
        final World world = BankAccounts.getInstance().getServer().getWorld(rs.getString("world"));
        if (world == null) throw new IllegalStateException("World not found: " + rs.getString("world"));
        this.world = world;
        this.mode = Mode.getMode(rs.getInt("mode"));
        this.price = new BigDecimal(rs.getString("price"));
        this.owner = Account.get(rs.getString("owner")).orElseGet(Account.ClosedAccount::new);
        this.item = rs.getString("item");
    }

    /**
     * Get location of the chest shop
     */
    public @NotNull Location getLocation() {
        return new Location(world, x, y, z);
    }

    /**
     * Get chest shop sign
     */
    public @NotNull Optional<@NotNull Sign> getSign() {
        return (world.getBlockAt(x, y, z).getState() instanceof final @NotNull Sign sign) ? Optional.of(sign) : Optional.empty();
    }

    /**
     * Get chest shop chest
     */
    public @NotNull Optional<@NotNull Chest> getChest() {
        return getSign().flatMap(sign -> (sign.getBlockData() instanceof final @NotNull org.bukkit.block.data.type.WallSign signData) ? Optional.of(sign.getBlock().getRelative(signData.getFacing().getOppositeFace()).getState()) : Optional.empty()).filter(state -> state instanceof final @NotNull Chest chest).map(state -> (Chest) state);
    }

    /**
     * Get first occurrence of wanted item in the chest
     */
    public @NotNull Optional<@NotNull ItemStack> getFirstItemStack() {
        return getChest().flatMap(chest -> {
            for (final ItemStack itemStack : chest.getInventory().getContents())
                if (itemStack != null && ChestShop.matchesHash(itemStack, item)) return Optional.of(itemStack);
            return Optional.empty();
        });
    }

    /**
     * Get amount of wanted items in the chest
     */
    public int getAvailableAmount() {
        return getChest().map(chest -> {
            int amount = 0;
            for (final ItemStack itemStack : chest.getInventory().getContents())
                if (itemStack != null && ChestShop.matchesHash(itemStack, item)) amount += itemStack.getAmount();
            return amount;
        }).orElse(0);
    }

    /**
     * Create chest shop id
     */
    public @NotNull String id() {
        return world.getName() + ":" + x + ":" + y + ":" + z;
    }

    /**
     * Save to db
     */
    public void save() {
        try (final @NotNull Connection conn = BankAccounts.getInstance().getDb().getConnection(); final @NotNull PreparedStatement stmt = conn.prepareStatement("INSERT INTO `chestshop` (`x`, `y`, `z`, `world`, `mode`, `price`, `owner`, `item`) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
            stmt.setInt(1, x);
            stmt.setInt(2, y);
            stmt.setInt(3, z);
            stmt.setString(4, world.getName());
            stmt.setInt(5, Mode.getMode(mode));
            stmt.setBigDecimal(6, price);
            stmt.setString(7, owner.id);
            stmt.setString(8, item);
            stmt.executeUpdate();
        } catch (final @NotNull SQLException e) {
            BankAccounts.getInstance().getLogger().log(Level.SEVERE, "Could not save chest shop in" + world.getName() + " at X: " + x + " Y: " + y + " Z: " + z, e);
        }
    }

    /**
     * Delete from db
     */
    public void delete() {
        try (final @NotNull Connection conn = BankAccounts.getInstance().getDb().getConnection(); final @NotNull PreparedStatement stmt = conn.prepareStatement("DELETE FROM `chestshop` WHERE `x` = ? AND `y` = ? AND `z` = ? AND `world` = ?")) {
            stmt.setInt(1, x);
            stmt.setInt(2, y);
            stmt.setInt(3, z);
            stmt.setString(4, world.getName());
            stmt.executeUpdate();
        } catch (final @NotNull SQLException e) {
            BankAccounts.getInstance().getLogger().log(Level.SEVERE, "Could not delete chest shop in" + world.getName() + " at X: " + x + " Y: " + y + " Z: " + z, e);
        }
    }

    /**
     * Get from db
     *
     * @param x     X coordinate of the chest shop.
     * @param y     Y coordinate of the chest shop.
     * @param z     Z coordinate of the chest shop.
     * @param world World of the chest shop.
     */
    public static @NotNull Optional<@NotNull ChestShop> get(final int x, final int y, final int z, final @NotNull World world) {
        try (final @NotNull Connection conn = BankAccounts.getInstance().getDb().getConnection(); final @NotNull PreparedStatement stmt = conn.prepareStatement("SELECT * FROM `chestshop` WHERE `x` = ? AND `y` = ? AND `z` = ? AND `world` = ?")) {
            stmt.setInt(1, x);
            stmt.setInt(2, y);
            stmt.setInt(3, z);
            stmt.setString(4, world.getName());
            try (final @NotNull ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return Optional.of(new ChestShop(rs));
            }
        } catch (final @NotNull SQLException e) {
            BankAccounts.getInstance().getLogger().log(Level.SEVERE, "Could not get chest shop in" + world.getName() + " at X: " + x + " Y: " + y + " Z: " + z, e);
        }
        return Optional.empty();
    }

    /**
     * Get all chest shops owned by an account
     *
     * @param owner The owner of the chest shop.
     */
    public static @NotNull List<@NotNull ChestShop> getAll(final @NotNull Account owner) {
        final @NotNull List<@NotNull ChestShop> chestShops = new ArrayList<>();
        try (final @NotNull Connection conn = BankAccounts.getInstance().getDb().getConnection(); final @NotNull PreparedStatement stmt = conn.prepareStatement("SELECT * FROM `chestshop` WHERE `owner` = ?")) {
            stmt.setString(1, owner.id);
            try (final @NotNull ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) chestShops.add(new ChestShop(rs));
            }
        } catch (final @NotNull SQLException e) {
            BankAccounts.getInstance().getLogger().log(Level.SEVERE, "Could not get chest shops owned by " + owner.id, e);
        }
        return chestShops;
    }

    /**
     * Get chest shop
     *
     * @param location Location to get chest shop from.
     */
    public static @NotNull Optional<@NotNull ChestShop> get(final @NotNull Location location) {
        return get(location.getBlockX(), location.getBlockY(), location.getBlockZ(), location.getWorld());
    }

    /**
     * Get chest shop
     *
     * @param block Block to get chest shop from.
     */
    public static @NotNull Optional<@NotNull ChestShop> get(final @NotNull Block block) {
        return get(block.getLocation());
    }

    /**
     * Get chest shop
     *
     * @param block Block to get chest shop from.
     */
    public static @NotNull Optional<@NotNull ChestShop> get(final @NotNull BlockState block) {
        return get(block.getLocation());
    }

    /**
     * Chest shop mode
     */
    public enum Mode {
        BUY, SELL;

        public static Mode getMode(int mode) {
            return Mode.values()[mode];
        }

        public static int getMode(Mode mode) {
            return mode.ordinal();
        }
    }

    /**
     * Hash item. Always has length 6. Uses A-Z, a-z, and 0-9. Padded to 6 characters with `=`. Hashing with CRC32.
     *
     * @param item Item to hash.
     */
    public static @NotNull String hashItem(@NotNull ItemStack item) {
        final @NotNull ItemStack clone = item.clone();
        clone.setAmount(1);
        final @NotNull CRC32 crc32 = new CRC32();
        crc32.update(clone.serializeAsBytes());
        final char[] chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray();
        final @NotNull StringBuilder builder = new StringBuilder();
        long hash = crc32.getValue();
        while (hash != 0) {
            builder.append(chars[(int) (hash % chars.length)]);
            hash /= chars.length;
        }
        while (builder.length() < 6) builder.append('=');
        return builder.toString();
    }

    /**
     * Check if an item matches the hash.
     *
     * @param item Item to check.
     * @param hash Hash to check.
     */
    public static boolean matchesHash(@NotNull ItemStack item, @NotNull String hash) {
        return hash.equals(hashItem(item));
    }

    /**
     * Open item preview GUI
     *
     * @param player Player to open GUI for.
     * @param item Item to preview.
     */
    public static void openItemPreview(final @NotNull Player player, final @NotNull ItemStack item) {
        final @NotNull ItemStack clone = item.clone();
        final @NotNull ItemMeta meta = clone.getItemMeta();
        meta.getPersistentDataContainer().set(BankAccounts.Key.ITEM_PREVIEW_GUI, PersistentDataType.BYTE, (byte) 0);
        clone.setItemMeta(meta);
        final @NotNull Inventory inventory = Bukkit.createInventory(null, 9, MiniMessage.miniMessage().deserialize("Preview — <item>",
                Placeholder.component("item", clone.displayName()))
        );
        inventory.setItem(4, clone);
        player.openInventory(inventory);
    }
}
