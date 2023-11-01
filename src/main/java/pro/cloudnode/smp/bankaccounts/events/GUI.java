package pro.cloudnode.smp.bankaccounts.events;

import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pro.cloudnode.smp.bankaccounts.Account;
import pro.cloudnode.smp.bankaccounts.BankAccounts;
import pro.cloudnode.smp.bankaccounts.POS;
import pro.cloudnode.smp.bankaccounts.Transaction;

import java.util.*;
import java.util.stream.Collectors;

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
                final @NotNull ItemStack @NotNull [] items = getGuiItems(inventory);
                final @NotNull ItemStack deleteItem = findItem(items, BankAccounts.Key.POS_OWNER_GUI).get();
                final @NotNull Optional<@NotNull ItemStack> more = findItem(items, BankAccounts.Key.POS_OWNER_GUI_MORE);
                final @NotNull Optional<@NotNull ItemStack> less = findItem(items, BankAccounts.Key.POS_OWNER_GUI_LESS);
                final @NotNull PersistentDataContainer container = deleteItem.getItemMeta().getPersistentDataContainer();
                final @NotNull NamespacedKey @NotNull [] ownerKeys = new NamespacedKey[]{BankAccounts.Key.POS_OWNER_GUI, BankAccounts.Key.POS_OWNER_GUI_MORE, BankAccounts.Key.POS_OWNER_GUI_LESS};
                final @NotNull Optional<@NotNull String> id = Arrays.stream(ownerKeys).map(key -> container.get(key, PersistentDataType.STRING)).filter(Objects::nonNull).findFirst();
                final @NotNull Optional<@NotNUll POS> pos = id.flatMap(POS::get);

                if (pos.isEmpty()) {
                    inventory.close();
                    return;
                }
                if (event.getCurrentItem() != null && event.getCurrentItem().equals(deleteItem)) {
                    pos.get().delete();
                    event.getWhoClicked().sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("messages.pos-removed"))));
                    inventory.close();
                } else if (event.getCurrentItem() != null && more.isPresent() && event.getCurrentItem().equals(more.get())) {
                    // shift the items 1 up and get more from the metadata
                    final @NotNull List<@NotNull MetadataValue> value = event.getWhoClicked().getMetadata("pos-owner-gui-more");
                    if (value.isEmpty()) return;
                    if (value.get(0).value() == null) return;
                    final @NotNull ItemStack @NotNull [] moreItems = (ItemStack[]) value.get(0).value();
                    assert moreItems != null;
                    // save the last row
                    final @NotNull ItemStack @NotNull [] lastRow = Arrays.copyOfRange(inventory.getContents(), 0, 9);
                    event.getWhoClicked().removeMetadata("pos-owner-gui-more", BankAccounts.getInstance());
                    event.getWhoClicked().setMetadata("pos-owner-gui-less", new FixedMetadataValue(BankAccounts.getInstance(), lastRow));
                    // move all rows from 1 to 5 up by 1
                    for (int i = 0; i < 36; i++) {
                        inventory.setItem(i, inventory.getItem(i + 9));
                    }
                    // set the last row to the new items
                    for (int i = 0; i < 9; i++) {
                        inventory.setItem(36 + i, moreItems[i]);
                    }

                    // replace the more arrow with a less arrow
                    inventory.setItem(inventory.getSize() - 2, getButton(Button.LESS, pos.get(), pos.get().seller, true));
                } else if (event.getCurrentItem() != null && less.isPresent() && event.getCurrentItem().equals(less.get())) {
                    // shift the items 1 down and get more from the metadata
                    final @NotNull List<@NotNull MetadataValue> value = event.getWhoClicked().getMetadata("pos-owner-gui-less");
                    if (value.isEmpty()) return;
                    if (value.get(0).value() == null) return;
                    final @NotNull ItemStack @NotNull [] lessItems = (ItemStack[]) value.get(0).value();
                    assert lessItems != null;
                    // save the first row
                    final @NotNull ItemStack @NotNull [] firstRow = Arrays.copyOfRange(inventory.getContents(), 36, 45);
                    event.getWhoClicked().removeMetadata("pos-owner-gui-less", BankAccounts.getInstance());
                    event.getWhoClicked().setMetadata("pos-owner-gui-more", new FixedMetadataValue(BankAccounts.getInstance(), firstRow));
                    // move all rows from 1 to 5 down by 1
                    for (int i = 35; i >= 0; i--) {
                        inventory.setItem(i + 9, inventory.getItem(i));
                    }
                    // set the first row to the new items
                    for (int i = 0; i < 9; i++) {
                        inventory.setItem(i, lessItems[i]);
                    }

                    // replace the less arrow with a more arrow
                    inventory.setItem(inventory.getSize() - 2, getButton(Button.MORE, pos.get(), pos.get().seller, true));

                }
            }
            case "pos-buyer" -> {
                event.setCancelled(true);
                final @NotNull ItemStack[] items = getGuiItems(inventory);
                final @NotNull ItemStack confirm = findItem(items, BankAccounts.Key.POS_BUYER_GUI_CONFIRM).get();
                final @NotNull ItemStack info = findItem(items, BankAccounts.Key.POS_BUYER_GUI).get();
                final @NotNull ItemStack cancel = findItem(items, BankAccounts.Key.POS_BUYER_GUI_CANCEL).get();
                final @NotNull Optional<@NotNull ItemStack> more = findItem(items, BankAccounts.Key.POS_BUYER_GUI_MORE);
                final @NotNull Optional<@NotNull ItemStack> less = findItem(items, BankAccounts.Key.POS_BUYER_GUI_LESS);

                final @NotNull String[] checksums = info.getItemMeta().getPersistentDataContainer().get(BankAccounts.Key.POS_BUYER_GUI, PersistentDataType.STRING).split(",");

                final @NotNull Optional<POS> pos = POS.get(Objects.requireNonNull(cancel.getItemMeta().getPersistentDataContainer().get(BankAccounts.Key.POS_BUYER_GUI_CANCEL, PersistentDataType.STRING)));
                if (pos.isEmpty()) {
                    inventory.close();
                    return;
                }
                final @NotNull Optional<Account> buyer = Account.get(Objects.requireNonNull(confirm.getItemMeta().getPersistentDataContainer().get(BankAccounts.Key.POS_BUYER_GUI_CONFIRM, PersistentDataType.STRING)));
                if (buyer.isEmpty()) {
                    inventory.close();
                    return;
                }


                Block block = pos.get().getBlock();
                if (!(block.getState() instanceof final @NotNull Chest chest)) {
                    inventory.close();
                    event.getWhoClicked().sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("messages.errors.pos-empty"))));
                    final @Nullable Player seller = pos.get().seller.owner.getPlayer();
                    if (seller != null) seller.sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("messages.errors.pos-empty"))));
                    pos.get().delete();
                    return;
                }

                final @NotNull ItemStack @NotNull [] chestItems = Arrays.stream(chest.getInventory().getStorageContents()).filter(Objects::nonNull).toArray(ItemStack[]::new);

                final @Nullable ItemStack item = event.getCurrentItem();
                if (item == null) return;

                if (item.equals(confirm)) {
                    if (!POS.verifyChecksum(chestItems, checksums)) {
                        inventory.close();
                        event.getWhoClicked().sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("messages.errors.pos-items-changed"))));
                        final @Nullable Player seller = pos.get().seller.owner.getPlayer();
                        if (seller != null) seller.sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("messages.errors.pos-items-changed"))));
                        pos.get().delete();
                        return;
                    }
                    if (pos.get().seller.frozen) {
                        inventory.close();
                        event.getWhoClicked().sendMessage(Account.placeholders(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("messages.errors.frozen")), pos.get().seller));
                        final @Nullable Player seller = pos.get().seller.owner.getPlayer();
                        if (seller != null) {
                            seller.sendMessage(Account.placeholders(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("messages.errors.frozen")), pos.get().seller));
                            seller.sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("messages.pos-removed"))));
                        }
                        return;
                    }
                    if (buyer.get().frozen) {
                        inventory.close();
                        event.getWhoClicked().sendMessage(Account.placeholders(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("messages.errors.frozen")), buyer.get()));
                        return;
                    }
                    if (!buyer.get().hasFunds(pos.get().price)) {
                        inventory.close();
                        event.getWhoClicked().sendMessage(Account.placeholders(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("messages.errors.insufficient-funds")), buyer.get()));
                        return;
                    }
                    if (event.getWhoClicked().getInventory().getSize() - event.getWhoClicked().getInventory().getStorageContents().length < items.length) {
                        inventory.close();
                        event.getWhoClicked().sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("messages.errors.target-inventory-full")),
                                Placeholder.unparsed("player", event.getWhoClicked().getName())
                        ));
                        return;
                    }
                    inventory.close();
                    Transaction transaction = buyer.get().transfer(pos.get().seller, pos.get().price, pos.get().description, "pos");
                    chest.getInventory().clear();
                    event.getWhoClicked().getInventory().addItem(chestItems);
                    pos.get().delete();
                    final @NotNull String itemsFormatted = chestItems.length == 1 ? "1 item" : chestItems.length + " items";
                    event.getWhoClicked().sendMessage(Transaction.placeholders(transaction, Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("messages.pos-purchase"))
                            .replace("<items>", String.valueOf(chestItems.length))
                            .replace("<items-formatted>", itemsFormatted)
                    ));
                    final @Nullable Player seller = pos.get().seller.owner.getPlayer();
                    if (seller != null)
                        seller.sendMessage(Transaction.placeholders(transaction, Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("messages.pos-purchase-seller"))
                                .replace("<items>", String.valueOf(chestItems.length))
                                .replace("<items-formatted>", itemsFormatted)
                        ));
                }
                else if (item.equals(cancel)) {
                    inventory.close();
                } else if (more.isPresent() && item.equals(more.get())) {
                    // shift the items 1 up and get more from the metadata
                    final @NotNull List<@NotNull MetadataValue> value = event.getWhoClicked().getMetadata("pos-buyer-gui-more");
                    if (value.isEmpty()) return;
                    if (value.get(0).value() == null) return;
                    final @NotNull ItemStack @NotNull [] moreItems = (ItemStack[]) value.get(0).value();
                    assert moreItems != null;
                    // save the first row
                    final @NotNull ItemStack @NotNull [] firstRow = Arrays.copyOfRange(inventory.getContents(), 0, 9);
                    event.getWhoClicked().removeMetadata("pos-buyer-gui-more", BankAccounts.getInstance());
                    event.getWhoClicked().setMetadata("pos-buyer-gui-less", new FixedMetadataValue(BankAccounts.getInstance(), firstRow));
                    // move all rows from 1 to 5 up by 1
                    for (int i = 0; i < 36; i++) {
                        inventory.setItem(i, inventory.getItem(i + 9));
                    }
                    // set the last row to the new items
                    for (int i = 0; i < 9; i++) {
                        inventory.setItem(36 + i, moreItems[i]);
                    }

                    // replace the more arrow with a less arrow
                    inventory.setItem(inventory.getSize() - 1, getButton(Button.LESS, pos.get(), buyer.get()));

                } else if (less.isPresent() && item.equals(less.get())) {
                    // shift the items 1 down and get more from the metadata
                    final @NotNull List<final @NotNull MetadataValue> value = event.getWhoClicked().getMetadata("pos-buyer-gui-less");
                    if (value.isEmpty()) return;
                    if (value.get(0).value() == null) return;
                    final @NotNull ItemStack @NotNull [] lessItems = (ItemStack[]) value.get(0).value();
                    assert lessItems != null;
                    // save the last row
                    final @NotNull ItemStack @NotNull [] lastRow = Arrays.copyOfRange(inventory.getContents(), 36, 45);
                    event.getWhoClicked().removeMetadata("pos-buyer-gui-less", BankAccounts.getInstance());
                    event.getWhoClicked().setMetadata("pos-buyer-gui-more", new FixedMetadataValue(BankAccounts.getInstance(), lastRow));
                    // move all rows from 1 to 5 down by 1
                    for (int i = 35; i >= 0; i--) {
                        inventory.setItem(i + 9, inventory.getItem(i));
                    }
                    // set the first row to the new items
                    for (int i = 0; i < 9; i++) {
                        inventory.setItem(i, lessItems[i]);
                    }

                    inventory.setItem(inventory.getSize() - 1, getButton(Button.MORE, pos.get(), buyer.get()));
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClick(final @NotNull InventoryDragEvent event) {
        if (hasGuiItem(event.getInventory()))
            event.setCancelled(true);
    }

    public final static @NotNull HashMap<@NotNull String, @NotNull NamespacedKey[]> keys = new HashMap<>() {{
        put("pos-owner", new NamespacedKey[]{
                BankAccounts.Key.POS_OWNER_GUI,
                BankAccounts.Key.POS_OWNER_GUI_MORE,
                BankAccounts.Key.POS_OWNER_GUI_LESS,
        });
        put("pos-buyer", new NamespacedKey[]{
                BankAccounts.Key.POS_BUYER_GUI_CONFIRM,
                BankAccounts.Key.POS_BUYER_GUI,
                BankAccounts.Key.POS_BUYER_GUI_CANCEL,
                BankAccounts.Key.POS_BUYER_GUI_MORE,
                BankAccounts.Key.POS_BUYER_GUI_LESS
        });
    }};

    /**
     * Finds an item with a given key in an array of items
     * @param items The items to search
     * @param key The key to search for
     * @return The item with the key
     */
    public @NotNull Optional<@NotNull ItemStack> findItem(final @NotNull ItemStack @NotNull [] items, final @NotNull NamespacedKey key) {
        return Arrays.stream(items).filter(item -> item.getItemMeta().getPersistentDataContainer().has(key)).findFirst();
    }

    public final boolean isGuiItem(final @NotNull ItemStack item) {
        return item.hasItemMeta() && keys.entrySet().stream().anyMatch(entry -> Arrays.stream(entry.getValue()).anyMatch(key -> item.getItemMeta().getPersistentDataContainer().has(key)));
    }

    public final boolean isGuiItem(final @NotNull ItemStack item, final @NotNull NamespacedKey key) {
        return item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(key);
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

    public enum Button {
        CONFIRM,
        INFO,
        DECLINE,
        MORE,
        LESS,
        OVERVIEW,
        DELETE
    }

    /**
     * Get a button for the GUI
     * @param button The button to get
     * @param pos The pos
     * @param account The account
     * @param isOwner Whether the player is the owner of the pos
     * @return The button
     */
    public static @NotNull ItemStack getButton(final @NotNull Button button, final @NotNull POS pos, final @NotNull Account account, final boolean isOwner) {
        final @NotNull ItemStack item = new ItemStack(Objects.requireNonNull(Material.getMaterial(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("pos." + button.name().toLowerCase() + ".material")))), 1);
        if (BankAccounts.getInstance().getConfig().getBoolean("pos." + button.name().toLowerCase() + ".glint")) {
            item.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            item.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 1);
        }
        final @NotNull ItemMeta meta = item.getItemMeta();
        meta.displayName(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("pos." + button.name().toLowerCase() + ".name"))).decoration(TextDecoration.ITALIC, false));
        if (account == null) {
            meta.lore(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getStringList("pos." + button.name().toLowerCase() + ".lore")).stream().map(line -> MiniMessage.miniMessage().deserialize(line, Placeholder.unparsed("description", pos.description == null ? "no description" : pos.description),
                    Placeholder.unparsed("price", pos.price.toPlainString()),
                    Placeholder.unparsed("price-formatted", BankAccounts.formatCurrency(pos.price)),
                    Placeholder.unparsed("price-short", BankAccounts.formatCurrencyShort(pos.price)))).collect(Collectors.toList()));
        } else {
            meta.lore(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getStringList("pos." + button.name().toLowerCase() + ".lore")).stream().map(line -> MiniMessage.miniMessage().deserialize(line, Placeholder.unparsed("description", pos.description == null ? "no description" : pos.description),
                    Placeholder.unparsed("price", pos.price.toPlainString()),
                    Placeholder.unparsed("price-formatted", BankAccounts.formatCurrency(pos.price)),
                    Placeholder.unparsed("price-short", BankAccounts.formatCurrencyShort(pos.price)),
                    Placeholder.unparsed("account", account.owner.getName()),
                    Placeholder.unparsed("account-id", account.id),
                    Placeholder.unparsed("account-type", account.type.name()),
                    Placeholder.unparsed("balance-formatted", BankAccounts.formatCurrency(account.balance)),
                    Placeholder.unparsed("balance", String.valueOf(account.balance))
            )).collect(Collectors.toList()));
        }
        final @NotNull PersistentDataContainer container = meta.getPersistentDataContainer();
        if (isOwner) {
            switch (button) {
                case MORE -> container.set(BankAccounts.Key.POS_OWNER_GUI_MORE, PersistentDataType.STRING, pos.id());
                case LESS -> container.set(BankAccounts.Key.POS_OWNER_GUI_LESS, PersistentDataType.STRING, pos.id());
                case DELETE, CONFIRM -> container.set(BankAccounts.Key.POS_OWNER_GUI, PersistentDataType.STRING, pos.id());
            }
        } else {
            switch (button) {
                case CONFIRM -> {
                    if (account != null) container.set(BankAccounts.Key.POS_BUYER_GUI_CONFIRM, PersistentDataType.STRING, account.id);
                }
//            case INFO -> @TODO: implement INFO checksums
                case DECLINE -> container.set(BankAccounts.Key.POS_BUYER_GUI_CANCEL, PersistentDataType.STRING, pos.id());
                case MORE -> container.set(BankAccounts.Key.POS_BUYER_GUI_MORE, PersistentDataType.STRING, pos.id());
                case LESS -> container.set(BankAccounts.Key.POS_BUYER_GUI_LESS, PersistentDataType.STRING, pos.id());
            }
        }

        item.setItemMeta(meta);
        return item;
    }

    /**
     * Get a button for the GUI
     * @param button The button to get
     * @param pos The pos
     * @param isOwner Whether the player is the owner of the pos
     * @return The button
     */
    public static @NotNull ItemStack getButton(final @NotNull Button button, final @NotNull POS pos, boolean isOwner) {
        return getButton(button, pos, null, isOwner);
    }

    /**
     * Get a button for the buyer GUI
     * @param button The button to get
     * @param pos The pos
     * @return The button
     */
    public static @NotNull ItemStack getButton(final @NotNull Button button, final @NotNull POS pos) {
        return getButton(button, pos, false);
    }

    /**
     * Get a button for the GUI
     * @param button The button to get
     * @param pos The pos
     * @param account The account
     * @return The button
     */
    public static @NotNull ItemStack getButton(final @NotNull Button button, final @NotNull POS pos, final Account account) {
        return getButton(button, pos, account, false);
    }
}
