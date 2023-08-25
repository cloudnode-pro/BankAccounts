package pro.cloudnode.smp.bankaccounts;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Package {
    public static final @NotNull NamespacedKey PACKAGE_KEY = new NamespacedKey(BankAccounts.getInstance(), "package");
    public static final @NotNull NamespacedKey CREATED_FOR_KEY = new NamespacedKey(BankAccounts.getInstance(), "package-created-for");
    public final ItemStack[] items;
    public final Date createdAt;
    public final UUID createdFor;
    public final String id;

    /**
     * Creates a new package
     *
     * @param items      The items to be stored in the package
     * @param createdAt  The date the package was created
     * @param createdFor The UUID of the player the package was created for
     */
    public Package(final ItemStack[] items, final Date createdAt, final UUID createdFor) {
        this.items = Arrays.stream(items).map((item) -> {
            if (item == null) return new ItemStack(Material.AIR);
            return item.clone();
        }).toArray(ItemStack[]::new);
        this.createdAt = createdAt;
        this.createdFor = createdFor;
        this.id = StringGenerator.generate(8);
    }

    /**
     * Creates a new package
     *
     * @param items       The items to be stored in the package
     * @param createdFor  The UUID of the player the package was created for
     */
    public Package(final ItemStack[] items, final UUID createdFor) {
        this(items, new Date(), createdFor);
    }

    /**
     * Gets the package as a shulker box item
     *
     * @return The package as a shulker box item
     */
    public ItemStack getItem() {
        ItemStack packageItem = new ItemStack(Material.GRAY_SHULKER_BOX);
        BlockStateMeta meta = (BlockStateMeta) packageItem.getItemMeta();
        ShulkerBox shulkerBox = (ShulkerBox) meta.getBlockState();
        Arrays.stream(getItems()).map((item) -> {
            if (item == null) return new ItemStack(Material.AIR);
            return item.clone();
        }).forEach(shulkerBox.getInventory()::addItem);
        meta.setBlockState(shulkerBox);
        meta.displayName(MiniMessage.miniMessage().deserialize("<green>Package"));
        meta.getPersistentDataContainer().set(PACKAGE_KEY, PersistentDataType.STRING, id);
        meta.getPersistentDataContainer().set(CREATED_FOR_KEY, PersistentDataType.STRING, createdFor.toString());
        packageItem.setItemMeta(meta);
        return packageItem;
    }

    public ItemStack[] getItems() {
        return items;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public UUID getCreatedFor() {
        return createdFor;
    }

    public String getId() {
        return id;
    }

    /**
     * Check if an item is a package
     * @param item the item to check
     * @return true if the item is a package, false otherwise
     */
    public static boolean isPackage(ItemStack item) {
        if (item == null) return false;
        if (item.getType() != Material.GRAY_SHULKER_BOX) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        return meta.getPersistentDataContainer().has(PACKAGE_KEY, PersistentDataType.STRING);
    }

    /**
     * Construct a package from an item
     * @param item the item to get the package from
     * @return the package
     */
    public static Package fromItem(ItemStack item) {
        if (!isPackage(item)) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        String id = meta.getPersistentDataContainer().get(PACKAGE_KEY, PersistentDataType.STRING);
        if (id == null) return null;
        BlockStateMeta blockStateMeta = (BlockStateMeta) meta;
        ShulkerBox shulkerBox = (ShulkerBox) blockStateMeta.getBlockState();
        UUID createdFor = UUID.fromString(Objects.requireNonNull(meta.getPersistentDataContainer().get(CREATED_FOR_KEY, PersistentDataType.STRING)));
        return new Package(shulkerBox.getInventory().getContents(), createdFor);
    }

    /**
     * Action call to open the package
     *
     * @param player The player to open the package for
     * @param _package The package to open
     * @implNote This function does a bunch of checks and gives the player the items inside the package
     */
    public void open(Player player, ItemStack _package) {
        if (player.getInventory().firstEmpty() == -1) {
            player.sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("messages.errors.your-inventory-full"))));
            return;
        }
        if (!player.getUniqueId().equals(createdFor)) {
            player.sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("messages.errors.not-package-owner"))));
            return;
        }
        // check if player's inventory has space for all items
        // @TODO: replace with call to upstream:main function BankAccounts#canFit
        int size = player.getInventory().getSize() % 9 * 9;
        final @NotNull Inventory inv = BankAccounts.getInstance().getServer().createInventory(null, size);
        final @NotNull HashMap<@NotNull Integer, @NotNull ItemStack> didNotFit = inv.addItem(items);
        inv.close();

        if (!didNotFit.isEmpty()) {
            player.sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("messages.errors.not-enough-space"))));
            return;
        }

        // check if player's inventory contains more than 1 of the same package
        // if so, they were probably trying to dupe it
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == Material.GRAY_SHULKER_BOX) {
                ItemMeta meta = item.getItemMeta();
                if (meta != null && meta.getPersistentDataContainer().has(PACKAGE_KEY, PersistentDataType.STRING)) {
                    String id = meta.getPersistentDataContainer().get(PACKAGE_KEY, PersistentDataType.STRING);
                    if (id == null) continue;
                    if (id.equals(this.id)) {
                        count++;
                    }
                }
            }
        }

        // log to console
        if (count > 1) {
            BankAccounts.getInstance().getLogger().warning("Player " + player.getName() + " has possibly tried to dupe package " + id + "!");
            // remove the package from their inventory
            player.getInventory().remove(getItem());
            return;
        }

        // remove the package from their inventory
        player.getInventory().remove(_package);
        player.sendEquipmentChange(player, EquipmentSlot.HAND, new ItemStack(Material.AIR));
        // give the player the package
        player.getInventory().addItem(getItems());
        player.sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("messages.package-opened"))));
    }
}
