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
                    player.sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("messages.errors.pos-no-permission"))));
                    return;
                }
                final @NotNull ItemStack heldItem = player.getInventory().getItemInMainHand();
                if (heldItem.getType() == Material.AIR) {
                    player.sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("messages.errors.no-card"))));
                    return;
                }
                final @NotNull Optional<Account> account = Account.get(heldItem);
                if (account.isEmpty())
                    player.sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("messages.errors.pos-invalid-card"))));
                else {
                    if (!player.hasPermission("bank.pos.use.other") && !account.get().owner.getUniqueId().equals(player.getUniqueId())) {
                        player.sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("messages.errors.not-account-owner"))));
                        return;
                    }
                    POS.openBuyGui(player, chest, pos.get(), account.get());
                }
            }
        }
    }
}
