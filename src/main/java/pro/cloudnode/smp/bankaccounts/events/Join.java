package pro.cloudnode.smp.bankaccounts.events;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;
import pro.cloudnode.smp.bankaccounts.Account;
import pro.cloudnode.smp.bankaccounts.BankAccounts;
import pro.cloudnode.smp.bankaccounts.Invoice;
import pro.cloudnode.smp.bankaccounts.Permissions;

import java.math.BigDecimal;
import java.util.Optional;

public final class Join implements Listener {
    @EventHandler
    public void onPlayerJoin(final @NotNull PlayerJoinEvent event) {
        final @NotNull Player player = event.getPlayer();
        final @NotNull Optional<@NotNull BigDecimal> startingBalance = BankAccounts.getInstance().config().startingBalance();
        BankAccounts.getInstance().getServer().getScheduler().runTaskAsynchronously(BankAccounts.getInstance(), () -> {
            if (Account.getVaultAccount(player).isEmpty()) {
                if (startingBalance.isPresent()) {
                    // if the player already has a personal account, they will not be given starting balance
                    final @NotNull BigDecimal balance = startingBalance.get().compareTo(BigDecimal.ZERO) <= 0 || Account.get(player, Account.Type.PERSONAL).length > 0 ? BigDecimal.ZERO : startingBalance.get();
                    new Account(player, Account.Type.VAULT, null, balance, false).insert();
                }
                else if (BankAccounts.getInstance().config().integrationsVaultEnabled()) {
                    // Vault account is required if the Vault integration is enabled, regardless of starting balance
                    new Account(player, Account.Type.VAULT, null, BigDecimal.ZERO, false).insert();
                }
            }
        });
        if (player.hasPermission(Permissions.NOTIFY_UPDATE)) {
            BankAccounts.getInstance().getServer().getScheduler().runTaskLaterAsynchronously(BankAccounts.getInstance(), () -> BankAccounts.checkForUpdates().ifPresent(latestVersion ->
                player.sendMessage(BankAccounts.getInstance().config().messagesUpdateAvailable(latestVersion))
            ), 20L);
        }
        if (player.hasPermission(Permissions.INVOICE_NOTIFY) && BankAccounts.getInstance().config().invoiceNotifyJoin() && Invoice.countUnpaid(player) > 0) {
            BankAccounts.getInstance().getServer().getScheduler().runTaskLater(BankAccounts.getInstance(), () -> {
                final @NotNull Optional<@NotNull Component> message = BankAccounts.getInstance().config().messagesInvoiceNotify(Invoice.countUnpaid(player));
                message.ifPresent(player::sendMessage);
            }, 20L);
        }
    }
}
