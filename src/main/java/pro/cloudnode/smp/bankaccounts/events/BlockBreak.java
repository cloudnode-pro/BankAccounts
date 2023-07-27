package pro.cloudnode.smp.bankaccounts.events;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import pro.cloudnode.smp.bankaccounts.POS;

import java.util.Objects;
import java.util.Optional;

public final class BlockBreak implements Listener {
    @EventHandler
    public void onBlockBreak(final @NotNull BlockBreakEvent event) {
        final @NotNull Block block = event.getBlock();
        if (block.getState() instanceof final @NotNull Chest chest) {
            final @NotNull Inventory inventory = chest.getInventory();
            if (!inventory.isEmpty()) {
                final @NotNull Optional<POS> pos = POS.get(chest);
                if (pos.isPresent()) {
                    pos.get().delete();
                    event.getPlayer().sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(pro.cloudnode.smp.bankaccounts.BankAccounts.getInstance().getConfig().getString("messages.pos-removed"))));
                }
            }
        }
    }
}
