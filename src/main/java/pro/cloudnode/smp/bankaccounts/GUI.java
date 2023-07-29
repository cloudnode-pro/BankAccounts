package pro.cloudnode.smp.bankaccounts;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Optional;

public abstract class GUI {
    /**
     * Number of rows in the GUI
     */
    public final int rows;

    /**
     * Items of the GUI
     */
    public final @NotNull ArrayList<Item> items;

    /**
     * Title of the GUI
     */
    public final @Nullable Component title;

    /**
     * Inventory
     */
    private final @NotNull Inventory inventory;

    /**
     * Create GUI
     * @param rows Number of rows in the GUI
     * @param items Items of the GUI
     * @param title Title of the GUI
     */
    public GUI(int rows, @NotNull ArrayList<Item> items, @Nullable Component title) {
        this.rows = rows;
        this.items = items;
        this.title = title;
        this.inventory = title != null ? Bukkit.createInventory(null, rows, title) : Bukkit.createInventory(null, rows);
        putItems();
    }

    /**
     * Put items in the inventory
     */
    private void putItems() {
        for (Item item : items)
            inventory.setItem(item.slot, item.itemStack);
    }

    /**
     * Open GUI
     * @param player Player to open GUI
     */
    public void open(@NotNull Player player) {
        player.openInventory(inventory);
    }

    /**
     * Get item in slot
     * @param slot Slot
     */
    private Optional<Item> getItem(int slot) {
        return items.stream().filter(item -> item.slot == slot).findFirst();
    }

    /**
     * Item is clicked
     */
    @EventHandler
    public void onInventoryClick(final @NotNull InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        final @NotNull Optional<Item> item = getItem(event.getSlot());
        if (item.isPresent()) {
            if (!item.get().stealable) event.setCancelled(true);
            item.get().onClick(event.getWhoClicked(), item.get(), this);
        }
    }

    /**
     * GUI item
     */
    public static final class Item {
        /**
         * Item slot
         */
        public final int slot;

        /**
         * Item stack
         */
        public final @NotNull ItemStack itemStack;

        /**
         * Is item stealable
         */
        public final boolean stealable;

        /**
         * Item action on click
         */
        public final @Nullable GUIAction action;

        /**
         * Item constructor
         *
         * @param slot      Item slot
         * @param itemStack Item stack
         * @param stealable Is item stealable
         * @param action    Item action on click
         */
        public Item(int slot, @NotNull ItemStack itemStack, boolean stealable, @Nullable GUIAction action) {
            this.slot = slot;
            this.itemStack = itemStack;
            this.stealable = stealable;
            this.action = action;
        }

        /**
         * Item constructor
         *
         * @param slot      Item slot
         * @param itemStack Item stack
         * @param stealable Is item stealable
         */
        public Item(int slot, @NotNull ItemStack itemStack, boolean stealable) {
            this(slot, itemStack, stealable, null);
        }

        /**
         * Item constructor
         *
         * @param slot      Item slot
         * @param itemStack Item stack
         */
        public Item(int slot, @NotNull ItemStack itemStack) {
            this(slot, itemStack, false, null);
        }

        /**
         * Item is clicked
         */
        public void onClick(final @NotNull HumanEntity player, final @NotNull Item item, final @NotNull GUI gui) {
            if (action != null) action.run(player, item, gui);
        }
    }

    @FunctionalInterface
    private interface GUIAction {
        void run(final @NotNull HumanEntity player, final @NotNull Item item, final @NotNull GUI gui);
    }
}
