package pro.cloudnode.smp.bankaccounts;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
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
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pro.cloudnode.smp.bankaccounts.events.GUI;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.stream.Collectors;
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
     * Create POS id
     */
    public @NotNull String id() {
        return world.getName() + ":" + x + ":" + y + ":" + z;
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
             final PreparedStatement stmt = conn.prepareStatement("DELETE FROM `pos` WHERE `x` = ? AND `y` = ? AND `z` = ? AND `world` = ?")) {
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
        } else return get(chest.getLocation());
    }

    /**
     * Get POS
     *
     * @param id POS id
     */
    public static Optional<POS> get(final @NotNull String id) {
        final String[] split = id.split(":");
        if (split.length != 4) return Optional.empty();
        final World world = Bukkit.getWorld(split[0]);
        if (world == null) return Optional.empty();
        try {
            final int x = Integer.parseInt(split[1]);
            final int y = Integer.parseInt(split[2]);
            final int z = Integer.parseInt(split[3]);
            return get(x, y, z, world);
        } catch (final NumberFormatException e) {
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
        final @NotNull ItemStack[] items = Arrays.stream(chest.getInventory().getStorageContents()).filter(Objects::nonNull).toArray(ItemStack[]::new);
        int extraRows = 1;
        final int size = Math.min(extraRows * 9 + items.length + 9 - items.length % 9, 54);
        final @NotNull Inventory gui = Bukkit.createInventory(null, size, MiniMessage.miniMessage().deserialize(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("pos.title")),
                Placeholder.unparsed("description", pos.description == null ? "no description" : pos.description),
                Placeholder.unparsed("price", pos.price.toPlainString()),
                Placeholder.unparsed("price-formatted", BankAccounts.formatCurrency(pos.price)),
                Placeholder.unparsed("price-short", BankAccounts.formatCurrencyShort(pos.price))
        ));

        for (int i = 0; i < items.length - 9; i++) {
            gui.setItem(i, items[i]);
        }

        final @NotNull ItemStack info = new ItemStack(Objects.requireNonNull(Material.getMaterial(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("pos.info.material")))), 1);
        if (BankAccounts.getInstance().getConfig().getBoolean("pos.info.glint")) {
            info.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            info.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 1);
        }
        final @NotNull ItemMeta infoMeta = info.getItemMeta();
        infoMeta.displayName(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("pos.info.name-owner")),
                Placeholder.unparsed("description", pos.description == null ? "no description" : pos.description),
                Placeholder.unparsed("price", pos.price.toPlainString()),
                Placeholder.unparsed("price-formatted", BankAccounts.formatCurrency(pos.price)),
                Placeholder.unparsed("price-short", BankAccounts.formatCurrencyShort(pos.price))
        ).decoration(TextDecoration.ITALIC, false));
        infoMeta.lore(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getStringList("pos.info.lore-owner")).stream()
                .map(line -> MiniMessage.miniMessage().deserialize(line,
                        Placeholder.unparsed("description", pos.description == null ? "no description" : pos.description),
                        Placeholder.unparsed("price", pos.price.toPlainString()),
                        Placeholder.unparsed("price-formatted", BankAccounts.formatCurrency(pos.price)),
                        Placeholder.unparsed("price-short", BankAccounts.formatCurrencyShort(pos.price))
                ).decoration(TextDecoration.ITALIC, false)).collect(Collectors.toList()));
        info.setItemMeta(infoMeta);
        gui.setItem(size - 5, info);

        gui.setItem(size - 1, GUI.getButton(GUI.Button.DELETE, pos, true));

        // pagination
        if (items.length >= 54) {
            gui.setItem(size - 2, GUI.getButton(GUI.Button.MORE, pos, true));

            // @todo: there needs to be a better way of saving this
            // save last 9 items in metadata
            player.setMetadata("pos-owner-gui-more", new FixedMetadataValue(BankAccounts.getInstance(), Arrays.copyOfRange(items, items.length - 9, items.length)));
        }

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
        final @NotNull ItemStack[] items = Arrays.stream(chest.getInventory().getStorageContents()).filter(Objects::nonNull).toArray(ItemStack[]::new);
        int extraRows = 1;
        final int size = Math.min(extraRows * 9 + items.length + 9 - items.length % 9, 54);
        final @NotNull Inventory gui = Bukkit.createInventory(null, size, MiniMessage.miniMessage().deserialize(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("pos.title")),
                Placeholder.unparsed("description", pos.description == null ? "no description" : pos.description),
                Placeholder.unparsed("price", pos.price.toPlainString()),
                Placeholder.unparsed("price-formatted", BankAccounts.formatCurrency(pos.price)),
                Placeholder.unparsed("price-short", BankAccounts.formatCurrencyShort(pos.price))
        ));

        for (int i = 0; i < items.length - 9; i++) {
            gui.setItem(i, items[i]);
        }

        final @NotNull ItemStack info = new ItemStack(Objects.requireNonNull(Material.getMaterial(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("pos.info.material")))), 1);
        if (BankAccounts.getInstance().getConfig().getBoolean("pos.info.glint")) {
            info.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            info.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 1);
        }
        final @NotNull ItemMeta infoMeta = info.getItemMeta();
        infoMeta.displayName(MiniMessage.miniMessage().deserialize(Account.placeholdersString(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("pos.info.name-buyer")), pos.seller),
                Placeholder.unparsed("description", pos.description == null ? "no description" : pos.description),
                Placeholder.unparsed("price", pos.price.toPlainString()),
                Placeholder.unparsed("price-formatted", BankAccounts.formatCurrency(pos.price)),
                Placeholder.unparsed("price-short", BankAccounts.formatCurrencyShort(pos.price))
        ).decoration(TextDecoration.ITALIC, false));
        infoMeta.lore(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getStringList("pos.info.lore-buyer")).stream()
                .map(line -> MiniMessage.miniMessage().deserialize(Account.placeholdersString(line, pos.seller),
                        Placeholder.unparsed("description", pos.description == null ? "no description" : pos.description),
                        Placeholder.unparsed("price", pos.price.toPlainString()),
                        Placeholder.unparsed("price-formatted", BankAccounts.formatCurrency(pos.price)),
                        Placeholder.unparsed("price-short", BankAccounts.formatCurrencyShort(pos.price))
                ).decoration(TextDecoration.ITALIC, false)).collect(Collectors.toList()));
        final @NotNull PersistentDataContainer overviewContainer = infoMeta.getPersistentDataContainer();
        overviewContainer.set(BankAccounts.Key.POS_BUYER_GUI, PersistentDataType.STRING, String.join(",", POS.checksum(items)));
        info.setItemMeta(infoMeta);

        gui.setItem(size - 5, info);
        gui.setItem(size - 7, GUI.getButton(GUI.Button.CONFIRM, pos, account));
        gui.setItem(size - 3, GUI.getButton(GUI.Button.DECLINE, pos, account));

        // pagination
        if (items.length >= 54) {
            gui.setItem(size - 1, GUI.getButton(GUI.Button.MORE, pos, account));

            // @todo: there needs to be a better way of saving this
            // save last 9 items in metadata
            player.setMetadata("pos-buyer-gui-more", new FixedMetadataValue(BankAccounts.getInstance(), Arrays.copyOfRange(items, items.length - 9, items.length)));
        }

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
    public static @NotNull String[] checksum(final @NotNull ItemStack[] items) {
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
    public static boolean verifyChecksum(final @NotNull ItemStack[] items, final @NotNull String[] checksums) {
        return Arrays.equals(checksum(items), checksums);
    }
}
