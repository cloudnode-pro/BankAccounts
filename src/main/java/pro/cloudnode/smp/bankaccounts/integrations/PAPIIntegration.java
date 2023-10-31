package pro.cloudnode.smp.bankaccounts.integrations;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import pro.cloudnode.smp.bankaccounts.Account;
import pro.cloudnode.smp.bankaccounts.BankAccounts;

public final class PAPIIntegration extends PlaceholderExpansion {
    @Override
    public @NotNull String getIdentifier() {
        return "bankaccounts";
    }

    @Override
    public @NotNull String getAuthor() {
        return "cloudnode";
    }

    @Override
    public @NotNull String getVersion() {
        return BankAccounts.getInstance().getPluginMeta().getVersion();
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if (params.startsWith("balance_")) { // %bankaccounts_balance_<accountID>%
            final @NotNull String accountID = params.substring("balance_".length());
            return Account.get(accountID).map(value -> String.valueOf(value.balance)).orElse(null);
        } else {
            return null;
        }
    }
}
