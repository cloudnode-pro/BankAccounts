package pro.cloudnode.smp.bankaccounts.events;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;
import pro.cloudnode.smp.bankaccounts.ChestShop;

import java.util.Optional;

public final class ChestShopInteract implements Listener {
    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        final @NotNull Player player = event.getPlayer();
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.LEFT_CLICK_BLOCK) return;
        final @NotNull Optional<@NotNull Block> block = Optional.ofNullable(event.getClickedBlock());
        if (block.isEmpty() || !(block.get().getState() instanceof final @NotNull Sign sign)) return;
        final @NotNull SignSide signSide = sign.getSide(sign.getInteractableSideFor(player));
        final @NotNull String @NotNull [] lines = signSide.lines().stream().map(PlainTextComponentSerializer.plainText()::serialize).toArray(String[]::new);
        if (lines.length != 4 || (!lines[0].equals("[BUY]") && !lines[0].equals("[SELL]"))) return;
        if (!(sign.getBlockData() instanceof final @NotNull org.bukkit.block.data.type.WallSign signData)) return;
        if (!(sign.getBlock().getRelative(signData.getFacing().getOppositeFace()).getState() instanceof final @NotNull Chest chest))
            return;
        final @NotNull Optional<@NotNull ChestShop> chestShop = ChestShop.get(sign);
        if (chestShop.isEmpty()) return;
        event.setCancelled(true);
    }
}
