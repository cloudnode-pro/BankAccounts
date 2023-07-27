package pro.cloudnode.smp.bankaccounts.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;
import pro.cloudnode.smp.bankaccounts.Account;
import pro.cloudnode.smp.bankaccounts.BankAccounts;

import java.math.BigDecimal;

public final class Join implements Listener {
    @EventHandler
    public void onPlayerJoin(final @NotNull PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        if (!"null".equals(BankAccounts.getInstance().getConfig().getString("starting-balance"))) {
            final @NotNull Account[] accounts = Account.get(player, Account.Type.PERSONAL);
            if (accounts.length == 0) {
                final double startingBalance = BankAccounts.getInstance().getConfig().getDouble("starting-balance");
                new Account(player, Account.Type.PERSONAL, null, BigDecimal.valueOf(startingBalance), false).save();
            }
        }
    }
}
