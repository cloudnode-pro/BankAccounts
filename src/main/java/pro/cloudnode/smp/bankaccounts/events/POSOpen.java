package pro.cloudnode.smp.bankaccounts.events;

import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import pro.cloudnode.smp.bankaccounts.Account;
import pro.cloudnode.smp.bankaccounts.BankAccounts;
import pro.cloudnode.smp.bankaccounts.POS;
import pro.cloudnode.smp.bankaccounts.Permissions;

import java.util.Optional;

public final class POSOpen implements Listener {
    @EventHandler
    public void openPOS(final @NotNull PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        final @NotNull Optional<Block> block = Optional.ofNullable(event.getClickedBlock());
        if (block.isEmpty()) return;
        if (!(block.get().getState() instanceof final @NotNull Chest chest)) return;
        if (chest.getInventory().isEmpty()) return;
        if (chest.getInventory() instanceof DoubleChestInventory) return;

        final @NotNull Optional<POS> pos = POS.get(block.get());
        if (pos.isEmpty()) return;

        event.setUseInteractedBlock(Event.Result.DENY);

        final @NotNull Player player = event.getPlayer();
        if (player.getUniqueId().equals(pos.get().seller.owner.getUniqueId())) {
            POS.openOwnerGui(player, chest, pos.get());
            return;
        }
        if (!player.hasPermission(Permissions.POS_USE)) {
            player.sendMessage(BankAccounts.getInstance().config().messagesErrorsPosNoPermission());
            return;
        }
        final @NotNull ItemStack heldItem = player.getInventory().getItemInMainHand();
        if (heldItem.getType() != BankAccounts.getInstance().config().instrumentsMaterial()) {
            player.sendMessage(BankAccounts.getInstance().config().messagesErrorsNoCard());
            return;
        }
        final @NotNull Optional<@NotNull Account> account = Account.get(heldItem);
        if (account.isEmpty()) player.sendMessage(BankAccounts.getInstance().config().messagesErrorsPosInvalidCard());
        else {
            if (!player.hasPermission(Permissions.POS_USE_OTHER) && !account.get().owner.getUniqueId()
                    .equals(player.getUniqueId())) {
                player.sendMessage(BankAccounts.getInstance().config().messagesErrorsNotAccountOwner());
                return;
            }
            POS.openBuyGui(player, chest, pos.get(), account.get());
        }
    }
}
