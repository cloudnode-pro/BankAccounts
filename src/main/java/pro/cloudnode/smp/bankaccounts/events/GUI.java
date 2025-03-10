package pro.cloudnode.smp.bankaccounts.events;

import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pro.cloudnode.smp.bankaccounts.Account;
import pro.cloudnode.smp.bankaccounts.BankAccounts;
import pro.cloudnode.smp.bankaccounts.POS;
import pro.cloudnode.smp.bankaccounts.Transaction;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;

public class GUI implements Listener {
    @EventHandler
    public void onInventoryClick(final @NotNull InventoryClickEvent event) {
        final @NotNull ClickType clickType = event.getClick();
        final @NotNull Inventory top = event.getView().getTopInventory();
        if (
                (
                        clickType == ClickType.DOUBLE_CLICK
                        || clickType == ClickType.SHIFT_LEFT
                        || clickType == ClickType.SHIFT_RIGHT
                )
                && hasGuiItem(top)
        )
            event.setCancelled(true);


        final @NotNull Inventory inventory = Optional.ofNullable(event.getClickedInventory()).orElse(event.getInventory());
        switch (getGui(inventory).orElse(".null")) {
            case "pos-owner" -> {
                event.setCancelled(true);
                final @NotNull ItemStack item = getGuiItems(inventory)[0];
                final @NotNull PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
                final @NotNull Optional<POS> pos = POS.get(Objects.requireNonNull(container.get(BankAccounts.Key.POS_OWNER_GUI, PersistentDataType.STRING)));
                if (pos.isEmpty()) {
                    inventory.close();
                    return;
                }
                if (event.getCurrentItem() != null && event.getCurrentItem().equals(item)) {
                    pos.get().delete();
                    event.getWhoClicked().sendMessage(BankAccounts.getInstance().config().messagesPosRemoved());
                    inventory.close();
                }
            }
            case "pos-buyer" -> {
                event.setCancelled(true);
                final @NotNull ItemStack[] items = getGuiItems(inventory);
                final @NotNull ItemStack confirm = items[0];
                final @NotNull ItemStack info = items[1];
                final @NotNull ItemStack cancel = items[2];

                final @NotNull String[] checksums = info.getItemMeta().getPersistentDataContainer().get(BankAccounts.Key.POS_BUYER_GUI, PersistentDataType.STRING).split(",");

                final @NotNull Optional<POS> pos = POS.get(Objects.requireNonNull(cancel.getItemMeta().getPersistentDataContainer().get(BankAccounts.Key.POS_BUYER_GUI_CANCEL, PersistentDataType.STRING)));
                if (pos.isEmpty()) {
                    inventory.close();
                    return;
                }
                final @NotNull Optional<@NotNull Account> buyer = Account.get(Account.Tag.id(Objects.requireNonNull(confirm.getItemMeta().getPersistentDataContainer().get(BankAccounts.Key.POS_BUYER_GUI_CONFIRM, PersistentDataType.STRING))));
                if (buyer.isEmpty()) {
                    inventory.close();
                    return;
                }


                Block block = pos.get().getBlock();
                if (!(block.getState() instanceof final @NotNull Chest chest)) {
                    inventory.close();
                    event.getWhoClicked().sendMessage(BankAccounts.getInstance().config().messagesErrorsPosEmpty());
                    final @Nullable Player seller = pos.get().seller.owner.getPlayer();
                    if (seller != null) seller.sendMessage(BankAccounts.getInstance().config().messagesErrorsPosEmpty());
                    pos.get().delete();
                    return;
                }

                final @NotNull ItemStack @NotNull [] chestItems = Arrays.stream(chest.getInventory().getStorageContents()).filter(Objects::nonNull).toArray(ItemStack[]::new);

                final @Nullable ItemStack item = event.getCurrentItem();
                if (item == null) return;

                if (item.equals(confirm)) {
                    if (!POS.verifyChecksum(chestItems, checksums)) {
                        inventory.close();
                        event.getWhoClicked().sendMessage(BankAccounts.getInstance().config().messagesErrorsPosItemsChanged());
                        final @Nullable Player seller = pos.get().seller.owner.getPlayer();
                        if (seller != null) seller.sendMessage(BankAccounts.getInstance().config().messagesErrorsPosItemsChanged());
                        pos.get().delete();
                        return;
                    }
                    if (pos.get().seller.frozen) {
                        inventory.close();
                        event.getWhoClicked().sendMessage(BankAccounts.getInstance().config().messagesErrorsFrozen(pos.get().seller));
                        final @Nullable Player seller = pos.get().seller.owner.getPlayer();
                        if (seller != null) {
                            seller.sendMessage(BankAccounts.getInstance().config().messagesErrorsFrozen(pos.get().seller));
                            seller.sendMessage(BankAccounts.getInstance().config().messagesPosRemoved());
                        }
                        pos.get().delete();
                        return;
                    }
                    if (buyer.get().frozen) {
                        inventory.close();
                        event.getWhoClicked().sendMessage(BankAccounts.getInstance().config().messagesErrorsFrozen(buyer.get()));
                        return;
                    }
                    if (!buyer.get().hasFunds(pos.get().price)) {
                        inventory.close();
                        event.getWhoClicked().sendMessage(BankAccounts.getInstance().config().messagesErrorsInsufficientFunds(buyer.get()));
                        return;
                    }
                    if (event.getWhoClicked().getInventory().getSize() - event.getWhoClicked().getInventory().getStorageContents().length < items.length) {
                        inventory.close();
                        event.getWhoClicked().sendMessage(BankAccounts.getInstance().config().messagesErrorsTargetInventoryFull(event.getWhoClicked()));
                        return;
                    }
                    inventory.close();
                    Transaction transaction = buyer.get().transfer(pos.get().seller, pos.get().price, pos.get().description, "pos");
                    chest.getInventory().clear();
                    event.getWhoClicked().getInventory().addItem(chestItems);
                    pos.get().delete();
                    event.getWhoClicked().sendMessage(BankAccounts.getInstance().config().messagesPosPurchase(transaction, chestItems));
                    final @Nullable Player seller = pos.get().seller.owner.getPlayer();
                    if (seller != null)
                        seller.sendMessage(BankAccounts.getInstance().config().messagesPosPurchaseSeller(transaction, items));
                }
                else if (item.equals(cancel)) {
                    inventory.close();
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClick(final @NotNull InventoryDragEvent event) {
        if (hasGuiItem(event.getInventory()))
            event.setCancelled(true);
    }

    /**
     * Detect changes in items of POS chest while a POS GUI is opened and prevent the item change.
     * POS items can still be modified if no POS GUI is opened for that POS.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void posItemsChangeWhileOpened(final @NotNull InventoryMoveItemEvent event) {
        final @NotNull Inventory source = event.getSource();
        final @NotNull Inventory destination = event.getDestination();
        for (final @NotNull POS pos : POS.activePosChestGuis.values()) {
            final @Nullable Chest chest = pos.getChest();
            if (chest == null) continue;
            final @NotNull Inventory chestInventory = chest.getInventory();
            if (source.equals(chestInventory) || destination.equals(chestInventory)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    /**
     * POS GUI closed
     */
    @EventHandler
    public void posGuiClosed(final @NotNull InventoryCloseEvent event) {
        if (!POS.activePosChestGuis.containsKey(event.getInventory())) return;
        POS.activePosChestGuis.remove(event.getInventory());
    }

    public final static @NotNull HashMap<@NotNull String, @NotNull NamespacedKey[]> keys = new HashMap<>() {{
        put("pos-owner", new NamespacedKey[]{BankAccounts.Key.POS_OWNER_GUI});
        put("pos-buyer", new NamespacedKey[]{BankAccounts.Key.POS_BUYER_GUI_CONFIRM, BankAccounts.Key.POS_BUYER_GUI, BankAccounts.Key.POS_BUYER_GUI_CANCEL});
    }};

    public final boolean isGuiItem(final @NotNull ItemStack item) {
        return item.hasItemMeta() && keys.entrySet().stream().anyMatch(entry -> Arrays.stream(entry.getValue()).anyMatch(key -> item.getItemMeta().getPersistentDataContainer().has(key)));
    }

    public final boolean isGuiItem(final @NotNull ItemStack item, final @NotNull NamespacedKey[] key) {
        return item.hasItemMeta() && Arrays.stream(key).anyMatch(item.getItemMeta().getPersistentDataContainer()::has);
    }

    public final @NotNull ItemStack[] getGuiItems(final @NotNull Inventory inventory) {
        return Arrays.stream(inventory.getContents()).filter(Objects::nonNull).filter(this::isGuiItem).toArray(ItemStack[]::new);
    }

    public final boolean hasGuiItem(final @NotNull Inventory inventory) {
        return Arrays.stream(inventory.getContents()).filter(Objects::nonNull).anyMatch(this::isGuiItem);
    }

    public final @NotNull Optional<@NotNull String> getGui(final @NotNull Inventory inventory) {
        return Arrays.stream(inventory.getContents()).filter(Objects::nonNull).filter(this::isGuiItem).map(item -> keys.entrySet().stream().filter(entry -> isGuiItem(item, entry.getValue())).findFirst().orElse(null)).filter(Objects::nonNull).map(HashMap.Entry::getKey).findFirst();
    }
}
