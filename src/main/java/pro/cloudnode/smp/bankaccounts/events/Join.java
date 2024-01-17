package pro.cloudnode.smp.bankaccounts.events;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;
import pro.cloudnode.smp.bankaccounts.Account;
import pro.cloudnode.smp.bankaccounts.BankAccounts;
import pro.cloudnode.smp.bankaccounts.Permissions;

import java.math.BigDecimal;
import java.util.Optional;

public final class Join implements Listener {
    @EventHandler
    public void onPlayerJoin(final @NotNull PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final @NotNull Optional<@NotNull Double> startingBalance = BankAccounts.getInstance().config()
                .startingBalance();
        startingBalance.ifPresent(aDouble -> Bukkit.getScheduler()
                .runTaskAsynchronously(BankAccounts.getInstance(), () -> {
                    final @NotNull Account[] accounts = Account.get(player, Account.Type.PERSONAL);
                    if (accounts.length == 0) {
                        new Account(player, Account.Type.PERSONAL, null, BigDecimal.valueOf(aDouble), false).insert();
                    }
                }));
        if (player.hasPermission(Permissions.NOTIFY_UPDATE)) {
            BankAccounts.getInstance().getServer().getScheduler().runTaskLater(BankAccounts.getInstance(), () -> {
                BankAccounts.checkForUpdates().ifPresent(latestVersion -> {
                    player.sendMessage(BankAccounts.getInstance().config().messagesUpdateAvailable(latestVersion));
                });
            }, 20L);
        }
    }
}
