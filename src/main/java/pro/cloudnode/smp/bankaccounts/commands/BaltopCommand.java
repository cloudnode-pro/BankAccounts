package pro.cloudnode.smp.bankaccounts.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pro.cloudnode.smp.bankaccounts.Account;
import pro.cloudnode.smp.bankaccounts.BankAccounts;
import pro.cloudnode.smp.bankaccounts.Permissions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public final class BaltopCommand extends pro.cloudnode.smp.bankaccounts.Command {
    @Override
    public boolean onCommand(final @NotNull CommandSender sender, final @NotNull Command command, final @NotNull String label, final @NotNull String @NotNull [] args) {
        return run(sender, label, args, new String[0]);
    }

    @Override
    public @NotNull List<@NotNull String> onTabComplete(final @NotNull CommandSender sender, final @NotNull Command command, final @NotNull String label, final @NotNull String @NotNull [] args) {
        final @NotNull ArrayList<@NotNull String> suggestions = new ArrayList<>();
        if (!sender.hasPermission(Permissions.BALTOP)) return suggestions;
        if (args.length == 2) suggestions.addAll(Arrays.asList("personal", "business", "player"));
        else if (args.length == 3) suggestions.add("1");
        return suggestions;
    }

    public static boolean run(final @NotNull CommandSender sender, final @NotNull String label, final @NotNull String @NotNull [] args, final @NotNull String @NotNull [] labelArgs) {
        final @NotNull Optional<@NotNull String> type = args.length > 0 && (args[0].equalsIgnoreCase("personal") || args[0].equalsIgnoreCase("business") || args[0].equalsIgnoreCase("player")) ? Optional.of(args[0]) : Optional.empty();
        int page = (int) Math.min(1e9, (args.length == 1 && type.isEmpty() && isInteger(args[0])) ? Integer.parseInt(args[0]) : ((args.length > 1 && isInteger(args[1])) ? Integer.parseInt(args[1]) : 1));
        if (page < 1) page = 1;
        final int perPage = BankAccounts.getInstance().config().baltopPerPage();

        final @NotNull String labelArgsString = labelArgs.length > 0 ? " " + String.join(" ", labelArgs) : "";
        final @NotNull String argsString = (args.length > 0 ? " " + String.join(" ", args) : "");
        final @NotNull String cmdPrev = "/baltop" + (type.map(s -> " " + s).orElse("")) + " " + Math.max(1, page - 1);
        final @NotNull String cmdNext = "/baltop" + (type.map(s -> " " + s).orElse("")) + " " + Math.min(1e9, page + 1);

        final @Nullable Account.Type accountType = type.flatMap(Account.Type::fromString).orElse(null);
        if (accountType != null || type.isEmpty()) {
            final @NotNull String category = accountType != null ? BankAccounts.getInstance().config().messagesTypes(accountType) : "All";
            final @NotNull Account @NotNull [] accounts = Account.getTopBalance(perPage, page, accountType);
            sendMessage(sender, BankAccounts.getInstance().config().messagesBaltopHeader()
                    .replace("<category>", category)
                    .replace("<page>", String.valueOf(page))
                    .replace("<cmd-prev>", cmdPrev)
                    .replace("<cmd-next>", cmdNext));
            for (int i = 0; i < accounts.length; i++) {
                final @NotNull Account account = accounts[i];
                sendMessage(sender, Account.placeholders(BankAccounts.getInstance().config().messagesBaltopEntry()
                        .replace("<position>", String.valueOf((page - 1) * perPage + i + 1)), account));
            }
        }
        else {
            sendMessage(sender, BankAccounts.getInstance().config().messagesBaltopHeader()
                    .replace("<category>", "Players")
                    .replace("<page>", String.valueOf(page))
                    .replace("<cmd-prev>", cmdPrev)
                    .replace("<cmd-next>", cmdNext));
            sendMessage(sender, "not implemented");
        }
        return true;
    }

    public static boolean isInteger(final @NotNull String string) {
        try {
            Integer.parseInt(string);
            return true;
        }
        catch (final NumberFormatException e) {
            return false;
        }
    }
}
