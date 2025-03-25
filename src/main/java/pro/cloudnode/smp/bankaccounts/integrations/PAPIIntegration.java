package pro.cloudnode.smp.bankaccounts.integrations;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import pro.cloudnode.smp.bankaccounts.Account;
import pro.cloudnode.smp.bankaccounts.BankAccounts;

import java.math.BigDecimal;
import java.util.Arrays;

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
    public String onRequest(final @NotNull OfflinePlayer player, final @NotNull String params) {
        final @NotNull String[] args = params.split("_");

        if (args.length < 1) return null;
        return switch (args[0]) {
            case "balance" -> {
                if (args.length == 1)
                    yield String.valueOf(
                            Arrays.stream(Account.get(player))
                                    .map(account -> account.balance)
                                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    );
                yield switch (args[1]) {
                    case "formatted" -> {
                        if (args.length == 3)
                            yield Account.get(Account.Tag.from(args[2])).map(value -> BankAccounts.formatCurrency(value.balance)).orElse(null);
                        if (args.length == 2)
                            yield BankAccounts.formatCurrency(
                                    Arrays.stream(Account.get(player))
                                            .map(account -> account.balance)
                                            .reduce(BigDecimal.ZERO, BigDecimal::add)
                            );
                        yield null;
                    }
                    default -> Account.get(Account.Tag.from(args[1])).map(value -> String.valueOf(value.balance)).orElse(null);
                };
            }
            case "owner" -> args.length < 2 ? null : Account.get(Account.Tag.from(args[1])).map(value -> value.owner.getName()).orElse(null);
            case "type" -> args.length < 2 ? null : Account.get(Account.Tag.from(args[1])).map(value -> value.type.getName()).orElse(null);
            case "name" -> args.length < 2 ? null : Account.get(Account.Tag.from(args[1])).map(value -> value.name).orElse(null);
            default -> null;
        };
    }
}
