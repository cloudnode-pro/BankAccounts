package pro.cloudnode.smp.bankaccounts.events;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
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

import java.util.Objects;
import java.util.Optional;

public final class PlayerInteract implements Listener {
    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        final @NotNull Player player = event.getPlayer();
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            final @NotNull Optional<Block> block = Optional.ofNullable(event.getClickedBlock());
            @NotNull Optional<@NotNull POS> pos = Optional.empty();

            if (block.isEmpty()) return;

            // handle double chests by checking if either side is a POS
            if (block.get().getState() instanceof Chest doubleChest && doubleChest.getInventory() instanceof DoubleChestInventory) {
                final @NotNull Chest leftChest = (Chest) Objects.requireNonNull(((DoubleChestInventory) doubleChest.getInventory()).getLeftSide().getHolder());
                final @NotNull Chest rightChest = (Chest) Objects.requireNonNull(((DoubleChestInventory) doubleChest.getInventory()).getRightSide().getHolder());

                if (POS.get(leftChest).isPresent()) {
                    pos = POS.get(leftChest);
                } else if (POS.get(rightChest).isPresent()) {
                    pos = POS.get(rightChest);
                }
            } else if (block.get().getState() instanceof final @NotNull Chest chest && !chest.getInventory().isEmpty()) {
                // handle single chests
                pos = POS.get(block.get());
            } else {
                // return if the block is not a chest
                return;
            }

            if (pos.isEmpty()) return;
            event.setCancelled(true);

            Chest chest = (Chest) block.get().getState(); // the handling of double chests is done inside POS#get

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
            final @NotNull Optional<@NotNull Account> account = Account.get(heldItem);
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
