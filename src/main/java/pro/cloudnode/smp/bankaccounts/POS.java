package pro.cloudnode.smp.bankaccounts;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.IntStream;
import java.util.zip.CRC32;

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
     * @param location    Location of the POS
     * @param price       Price of POS contents
     * @param description Description that appears on the bank statement
     * @param seller      Account that receives the money from the sale
     * @param created     Date the POS was created
     */
    public POS(final @NotNull Location location, final @NotNull BigDecimal price, final @Nullable String description, final @NotNull Account seller, final @NotNull Date created) {
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
    public POS(final @NotNull ResultSet rs) throws @NotNull SQLException, @NotNull IllegalStateException {
        this.x = rs.getInt("x");
        this.y = rs.getInt("y");
        this.z = rs.getInt("z");
        final @NotNull Optional<@NotNull World> world = Optional.ofNullable(BankAccounts.getInstance().getServer().getWorld(UUID.fromString(rs.getString("world"))));
        if (world.isEmpty()) throw new IllegalStateException("World not found: " + rs.getString("world"));
        this.world = world.get();
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
        return getLocation().getBlock();
    }

    /**
     * Create POS id
     */
    public @NotNull String id() {
        return world.getUID() + ":" + x + ":" + y + ":" + z;
    }

    /**
     * Save POS to database
     */
    public void save() {
        try (final @NotNull Connection conn = BankAccounts.getInstance().getDb().getConnection();
             final @NotNull PreparedStatement stmt = conn.prepareStatement("INSERT INTO `pos` (`x`, `y`, `z`, `world`, `price`, `description`, `seller`, `created`) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
            stmt.setInt(1, x);
            stmt.setInt(2, y);
            stmt.setInt(3, z);
            stmt.setString(4, world.getUID().toString());
            stmt.setBigDecimal(5, price);
            if (description == null) stmt.setNull(6, Types.VARCHAR);
            else stmt.setString(6, description);
            stmt.setString(7, seller.id);
            stmt.setTimestamp(8, new Timestamp(created.getTime()));

            stmt.executeUpdate();
        } catch (final @NotNull SQLException e) {
            BankAccounts.getInstance().getLogger().log(Level.SEVERE, "Could not save POS in " + world.getName() + " at X: " + x + " Y: " + y + " Z: " + z, e);
        }
    }

    /**
     * Delete POS
     */
    public void delete() {
        try (final @NotNull Connection conn = BankAccounts.getInstance().getDb().getConnection();
             final @NotNull PreparedStatement stmt = conn.prepareStatement("DELETE FROM `pos` WHERE `x` = ? AND `y` = ? AND `z` = ? AND `world` = ?")) {
            stmt.setInt(1, x);
            stmt.setInt(2, y);
            stmt.setInt(3, z);
            stmt.setString(4, world.getUID().toString());
            stmt.executeUpdate();
        } catch (final @NotNull SQLException e) {
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
    public static @NotNull Optional<@NotNull POS> get(final int x, final int y, final int z, final @NotNull World world) {
        try (final @NotNull Connection conn = BankAccounts.getInstance().getDb().getConnection();
             final @NotNull PreparedStatement stmt = conn.prepareStatement("SELECT * FROM `pos` WHERE `x` = ? AND `y` = ? AND `z` = ? AND `world` = ? LIMIT 1")) {
            stmt.setInt(1, x);
            stmt.setInt(2, y);
            stmt.setInt(3, z);
            stmt.setString(4, world.getUID().toString());
            final @NotNull ResultSet rs = stmt.executeQuery();
            return rs.next() ? Optional.of(new POS(rs)) : Optional.empty();
        } catch (final @NotNull SQLException e) {
            BankAccounts.getInstance().getLogger().log(Level.SEVERE, "Could not get POS in " + world.getName() + " at X: " + x + " Y: " + y + " Z: " + z, e);
            return Optional.empty();
        }
    }

    /**
     * Get POS
     *
     * @param location Location of the POS
     */
    public static @NotNull Optional<@NotNull POS> get(final @NotNull Location location) {
        return get(location.getBlockX(), location.getBlockY(), location.getBlockZ(), location.getWorld());
    }

    /**
     * Get POS
     *
     * @param block Block of the POS
     */
    public static @NotNull Optional<@NotNull POS> get(final @NotNull Block block) {
        return get(block.getLocation());
    }

    /**
     * Get POS
     * <p>
     * Takes into account double chests.
     *
     * @param chest Chest of the POS
     */
    public static @NotNull Optional<@NotNull POS> get(final @NotNull Chest chest) {
        if (chest.getInventory() instanceof final @NotNull DoubleChestInventory inventory) {
            final @NotNull Optional<@NotNull Location> left = Optional.ofNullable(inventory.getLeftSide().getLocation());
            final @NotNull Optional<@NotNull Location> right = Optional.ofNullable(inventory.getRightSide().getLocation());
            if (left.isPresent()) {
                final @NotNull Optional<@NotNull POS> pos = get(left.get());
                if (pos.isPresent()) return pos;
            }
            if (right.isPresent()) {
                final @NotNull Optional<@NotNull POS> pos = get(right.get());
                if (pos.isPresent()) return pos;
            }
            return Optional.empty();
        } else return get(chest.getLocation());
    }

    /**
     * Get POS
     *
     * @param id POS id
     */
    public static @NotNull Optional<@NotNull POS> get(final @NotNull String id) {
        final @NotNull String @NotNull [] split = id.split(":");
        if (split.length != 4) return Optional.empty();
        final @NotNull Optional<@NotNull World> world = Optional.ofNullable(BankAccounts.getInstance().getServer().getWorld(UUID.fromString(split[0])));
        if (world.isEmpty()) return Optional.empty();
        try {
            final int x = Integer.parseInt(split[1]);
            final int y = Integer.parseInt(split[2]);
            final int z = Integer.parseInt(split[3]);
            return get(x, y, z, world.get());
        } catch (final @NotNull NumberFormatException e) {
            return Optional.empty();
        }
    }

    /**
     * Open POS owner GUI
     * <p>
     * A gui that shows a preview of the items inside the POS. An extra row is added to the GUI, this row contains a
     * paper item which shows the POS price and description. A barrier item is also added to the GUI, this item is used
     * as a button to delete the POS.
     *
     * @param player Player to open the GUI for
     * @param chest  The POS chest
     * @param pos    The POS
     */
    public static void openOwnerGui(final @NotNull Player player, final @NotNull Chest chest, final @NotNull POS pos) {
        final @NotNull ItemStack @NotNull [] items = Arrays.stream(chest.getInventory().getStorageContents()).filter(Objects::nonNull).toArray(ItemStack[]::new);
        final int extraRows = 1;
        final int size = extraRows * 9 + items.length + 9 - items.length % 9;
        final @NotNull Inventory gui = Bukkit.createInventory(null, size, BankAccounts.getInstance().config().posTitle(pos));
        gui.addItem(items);

        final @NotNull ItemStack overview = new ItemStack(BankAccounts.getInstance().config().posInfoMaterial(), 1);
        if (BankAccounts.getInstance().config().posInfoGlint()) {
            overview.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            overview.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 1);
        }
        final @NotNull ItemMeta overviewMeta = overview.getItemMeta();
        overviewMeta.displayName(BankAccounts.getInstance().config().posInfoNameOwner(pos));
        overviewMeta.lore(BankAccounts.getInstance().config().posInfoLoreOwner(pos));
        overview.setItemMeta(overviewMeta);
        gui.setItem(size - 5, overview);

        final @NotNull ItemStack delete = new ItemStack(BankAccounts.getInstance().config().posDeleteMaterial(), 1);
        if (BankAccounts.getInstance().config().posDeleteGlint()) {
            delete.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            delete.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 1);
        }
        final @NotNull ItemMeta deleteMeta = delete.getItemMeta();
        deleteMeta.displayName(BankAccounts.getInstance().config().posDeleteName());
        deleteMeta.lore(BankAccounts.getInstance().config().posDeleteLore());
        final @NotNull PersistentDataContainer deleteContainer = deleteMeta.getPersistentDataContainer();
        deleteContainer.set(BankAccounts.Key.POS_OWNER_GUI, PersistentDataType.STRING, pos.id());
        delete.setItemMeta(deleteMeta);
        gui.setItem(size - 1, delete);

        player.openInventory(gui);
    }

    /**
     * Buyer POS GUI
     * <p>
     * A gui that shows a preview of the items inside the POS. An extra row is added to the GUI, this row contains a
     * paper item with shows the POS price and description. There are also two buttons, on to confirm purchase and
     * one to cancel the purchase.
     *
     * @param player Player to open the GUI for
     * @param chest  The POS chest
     * @param pos    The POS
     */
    public static void openBuyGui(final @NotNull Player player, final @NotNull Chest chest, final @NotNull POS pos, final @NotNull Account account) {
        final @NotNull ItemStack @NotNull [] items = Arrays.stream(chest.getInventory().getStorageContents()).filter(Objects::nonNull).toArray(ItemStack[]::new);
        final int extraRows = 1;
        final int size = extraRows * 9 + items.length + 9 - items.length % 9;
        final @NotNull Inventory gui = Bukkit.createInventory(null, size, BankAccounts.getInstance().config().posTitle(pos));
        gui.addItem(items);

        final @NotNull ItemStack overview = new ItemStack(BankAccounts.getInstance().config().posInfoMaterial(), 1);
        if (BankAccounts.getInstance().config().posInfoGlint()) {
            overview.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            overview.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 1);
        }
        final @NotNull ItemMeta overviewMeta = overview.getItemMeta();
        overviewMeta.displayName(BankAccounts.getInstance().config().posInfoNameBuyer(pos));
        overviewMeta.lore(BankAccounts.getInstance().config().posInfoLoreBuyer(pos));
        final @NotNull PersistentDataContainer overviewContainer = overviewMeta.getPersistentDataContainer();
        overviewContainer.set(BankAccounts.Key.POS_BUYER_GUI, PersistentDataType.STRING, String.join(",", POS.checksum(items)));
        overview.setItemMeta(overviewMeta);
        gui.setItem(size - 5, overview);

        final @NotNull ItemStack confirm = new ItemStack(BankAccounts.getInstance().config().posConfirmMaterial(), 1);
        if (BankAccounts.getInstance().config().posConfirmGlint()) {
            confirm.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            confirm.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 1);
        }
        final @NotNull ItemMeta confirmMeta = confirm.getItemMeta();
        confirmMeta.displayName(BankAccounts.getInstance().config().posConfirmName(pos, account));
        confirmMeta.lore(BankAccounts.getInstance().config().posConfirmLore(pos, account));
        final @NotNull PersistentDataContainer confirmContainer = confirmMeta.getPersistentDataContainer();
        confirmContainer.set(BankAccounts.Key.POS_BUYER_GUI_CONFIRM, PersistentDataType.STRING, account.id);
        confirm.setItemMeta(confirmMeta);
        gui.setItem(size - 7, confirm);

        final @NotNull ItemStack cancel = new ItemStack(BankAccounts.getInstance().config().posDeclineMaterial(), 1);
        if (BankAccounts.getInstance().config().posDeclineGlint()) {
            cancel.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            cancel.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 1);
        }
        final @NotNull ItemMeta cancelMeta = cancel.getItemMeta();
        cancelMeta.displayName(BankAccounts.getInstance().config().posDeclineName());
        cancelMeta.lore(BankAccounts.getInstance().config().posDeclineLore());
        final @NotNull PersistentDataContainer cancelContainer = cancelMeta.getPersistentDataContainer();
        cancelContainer.set(BankAccounts.Key.POS_BUYER_GUI_CANCEL, PersistentDataType.STRING, pos.id());
        cancel.setItemMeta(cancelMeta);
        gui.setItem(size - 3, cancel);

        player.openInventory(gui);
    }

    /**
     * Item to checksum
     *
     * @param item The item to create a checksum for
     */
    public static @NotNull String checksum(final @NotNull ItemStack item) {
        final byte[] bytes = item.serializeAsBytes();
        CRC32 crc32 = new CRC32();
        crc32.update(bytes);
        return Long.toHexString(crc32.getValue());
    }

    /**
     * Items to checksums
     *
     * @param items The items to create checksums for
     */
    public static @NotNull String @NotNull [] checksum(final @NotNull ItemStack @NotNull [] items) {
        return Arrays.stream(items).sorted(Comparator.comparing(ItemStack::translationKey)).sorted(Comparator.comparing(ItemStack::getAmount)).map(POS::checksum).toArray(String[]::new);
    }

    /**
     * Verify checksum
     *
     * @param item     The item to verify
     * @param checksum The checksum to verify against
     */
    public static boolean verifyChecksum(final @NotNull ItemStack item, final @NotNull String checksum) {
        return checksum(item).equals(checksum);
    }

    /**
     * Verify checksums
     *
     * @param items     The items to verify
     * @param checksums The checksums to verify against
     */
    public static boolean verifyChecksum(final @NotNull ItemStack @NotNull [] items, final @NotNull String @NotNull [] checksums) {
        if (items.length != checksums.length)
            throw new IllegalArgumentException("The number of items and checksums must be the same.");

        return IntStream.range(0, items.length).allMatch(i -> verifyChecksum(items[i], checksums[i]));
    }
}
