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

    /**
     *  Adds the following placeholders:
     *  <ul>
     *      <li>%bankaccounts_balance_&lt;accountID&gt;% - returns balance of account with specified ID</li>
     *      <li>%bankaccounts_balance_formatted_&lt;accountID&gt;% - returns formatted balance of account with specified ID</li>
     *      <li>%bankaccounts_owner_&lt;accountID&gt;% - returns name of the owner of account with specified ID</li>
     *      <li>%bankaccounts_type_&lt;accountID&gt;% - returns type of account with specified ID</li>
     *      <li>%bankaccounts_name_&lt;accountID&gt;% - returns name of account with specified ID</li>
     *  </ul>
    */
    @Override
    public String onRequest(OfflinePlayer player, String params) {
        final @NotNull String[] args = params.split("_");

        return switch (args[0]) {
            case "balance" -> switch (args[1]) {
                case "formatted" -> Account.get(args[2]).map(value -> BankAccounts.formatCurrency(value.balance)).orElse(null);
                default -> Account.get(args[1]).map(value -> String.valueOf(value.balance)).orElse(null);
            };
            case "owner" -> Account.get(args[1]).map(value -> value.owner.getName()).orElse(null);
            case "type" -> Account.get(args[1]).map(value -> value.type.getName()).orElse(null);
            case "name" -> Account.get(args[1]).map(value -> value.name).orElse(null);
            default -> null;
        };
    }
}
