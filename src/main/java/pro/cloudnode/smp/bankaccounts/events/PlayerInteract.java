package pro.cloudnode.smp.bankaccounts.events;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import pro.cloudnode.smp.bankaccounts.Account;
import pro.cloudnode.smp.bankaccounts.BankAccounts;
import pro.cloudnode.smp.bankaccounts.BankConfig;
import pro.cloudnode.smp.bankaccounts.POS;

import java.util.Objects;
import java.util.Optional;

public final class PlayerInteract implements Listener {
    @EventHandler
    public void onPlayerInteractEvent(final @NotNull PlayerInteractEvent event) {
        final @NotNull Player player = event.getPlayer();
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            final @NotNull Optional<Block> block = Optional.ofNullable(event.getClickedBlock());
            if (block.isPresent() && block.get().getState() instanceof final @NotNull Chest chest && !chest.getInventory().isEmpty()) {
                final @NotNull Optional<POS> pos = POS.get(block.get());
                if (pos.isEmpty()) return;
                event.setCancelled(true);
                if (player.getUniqueId().equals(pos.get().seller.owner.getUniqueId())) {
                    POS.openOwnerGui(player, chest, pos.get());
                    return;
                }
                if (!player.hasPermission("bank.pos.use")) {
                    player.sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString(BankConfig.MESSAGES_ERRORS_POS_NO_PERMISSION.getKey()))));
                    return;
                }
                final @NotNull ItemStack heldItem = player.getInventory().getItemInMainHand();
                if (heldItem.getType() != Objects.requireNonNull(Material.getMaterial(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString(BankConfig.INSTRUMENTS_MATERIAL.getKey()))))) {
                    player.sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString(BankConfig.MESSAGES_ERRORS_NO_CARD.getKey()))));
                    return;
                }
                final @NotNull Optional<@NotNull Account> account = Account.get(heldItem);
                if (account.isEmpty())
                    player.sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString(BankConfig.MESSAGES_ERRORS_POS_INVALID_CARD.getKey()))));
                else {
                    if (!player.hasPermission("bank.pos.use.other") && !account.get().owner.getUniqueId().equals(player.getUniqueId())) {
                        player.sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString(BankConfig.MESSAGES_ERRORS_NOT_ACCOUNT_OWNER.getKey()))));
                        return;
                    }
                    POS.openBuyGui(player, chest, pos.get(), account.get());
                }
            }
        }
    }
}
