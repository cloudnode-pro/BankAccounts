package pro.cloudnode.smp.bankaccounts.events;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;
import pro.cloudnode.smp.bankaccounts.Account;
import pro.cloudnode.smp.bankaccounts.BankAccounts;
import pro.cloudnode.smp.bankaccounts.BankConfig;
import pro.cloudnode.smp.bankaccounts.Command;

import java.math.BigDecimal;
import java.util.Objects;

public final class Join implements Listener {
    @EventHandler
    public void onPlayerJoin(final @NotNull PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        if (!"null".equals(BankAccounts.getInstance().getConfig().getString(BankConfig.STARTING_BALANCE.getKey()))) {
            Bukkit.getScheduler().runTaskAsynchronously(BankAccounts.getInstance(), () -> {
                final @NotNull Account[] accounts = Account.get(player, Account.Type.PERSONAL);
                if (accounts.length == 0) {
                    final double startingBalance = BankAccounts.getInstance().getConfig().getDouble(BankConfig.STARTING_BALANCE.getKey());
                    new Account(player, Account.Type.PERSONAL, null, BigDecimal.valueOf(startingBalance), false).insert();
                }
            });
        }
        if (player.hasPermission("bank.notify-update")) {
            BankAccounts.getInstance().getServer().getScheduler().runTaskLater(BankAccounts.getInstance(), () -> {
                BankAccounts.checkForUpdates().ifPresent(latestVersion -> {
                    Command.sendMessage(player, Objects.requireNonNull(BankAccounts.getInstance().getConfig()
                                    .getString(BankConfig.MESSAGES_UPDATE_AVAILABLE.getKey()))
                            .replace("<version>", latestVersion));
                });
            }, 20L);
        }
    }
}
