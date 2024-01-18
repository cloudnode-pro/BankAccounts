package pro.cloudnode.smp.bankaccounts.commands;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pro.cloudnode.smp.bankaccounts.Account;
import pro.cloudnode.smp.bankaccounts.BankAccounts;
import pro.cloudnode.smp.bankaccounts.Command;
import pro.cloudnode.smp.bankaccounts.Invoice;
import pro.cloudnode.smp.bankaccounts.Permissions;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public final class InvoiceCommand extends Command {
    @Override
    public boolean execute(final @NotNull CommandSender sender, final @NotNull String label, final @NotNull String @NotNull [] args) {
        if (!sender.hasPermission(Permissions.COMMAND))
            return sendMessage(sender, BankAccounts.getInstance().config().messagesErrorsNoPermission());
        if (args.length < 1)
            return sendMessage(sender, BankAccounts.getInstance().config().messagesErrorsUnknownCommand());
        return switch (args[0]) {
            case "create" -> create(sender, Arrays.copyOfRange(args, 1, args.length), label);
            default -> sendMessage(sender, BankAccounts.getInstance().config().messagesErrorsUnknownCommand());
        };
    }

    @Override
    public @Nullable List<@NotNull String> tab(final @NotNull CommandSender sender, final @NotNull String @NotNull [] args) {
        return null;
    }

    /**
     * Create invoice
     * <p>{@code /invoice create <account> <amount> [description] [--player <player>]}</p>
     */
    public static boolean create(final @NotNull CommandSender sender, @NotNull String @NotNull [] args, final @NotNull String label) {
        if (!sender.hasPermission(Permissions.INVOICE_CREATE))
            return sendMessage(sender, BankAccounts.getInstance().config().messagesErrorsNoPermission());

        final @NotNull Optional<@NotNull String> playerArg;
        final int playerIndex = Arrays.asList(args).indexOf("--player");
        playerArg = playerIndex == -1 || playerIndex + 1 >= args.length ? Optional.empty() : Optional.of(args[playerIndex + 1]);
        final @NotNull String @NotNull [] argsCopy;
        if (playerIndex != -1) {
            final @NotNull String @NotNull [] partA = Arrays.copyOfRange(args, 0, playerIndex);
            final @NotNull String @NotNull [] partB = Arrays.copyOfRange(args, playerArg.isPresent() ? playerIndex + 2 : playerIndex + 1, args.length);
            argsCopy = Stream.of(partA, partB).flatMap(Stream::of).toArray(String[]::new);
        }
        else argsCopy = args.clone();

        final @Nullable OfflinePlayer target = playerArg.map(u -> BankAccounts.getInstance().getServer().getOfflinePlayer(u)).orElse(null);

        final @NotNull String usage = "create <account> <amount> [description] [--player <player>]";
        if (argsCopy.length == 0) return sendUsage(sender, label, usage);
        if (argsCopy.length == 1) return sendUsage(sender, label, usage.replace("<account>", argsCopy[0]));

        final @NotNull BigDecimal amount;
        try {
            amount = BigDecimal.valueOf(Double.parseDouble(argsCopy[1])).setScale(2, RoundingMode.HALF_UP);
        }
        catch (NumberFormatException e) {
            return sendMessage(sender, BankAccounts.getInstance().config().messagesErrorsInvalidNumber(argsCopy[1]));
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0)
            return sendMessage(sender, BankAccounts.getInstance().config().messagesErrorsNegativeInvoice());

        final @NotNull Optional<@NotNull Account> account = Account.get(argsCopy[0]);
        if (account.isEmpty())
            return sendMessage(sender, BankAccounts.getInstance().config().messagesErrorsAccountNotFound());
        if (!sender.hasPermission(Permissions.INVOICE_CREATE_OTHER) && !account.get().owner.getUniqueId().equals(BankAccounts.getOfflinePlayer(sender).getUniqueId()))
            return sendMessage(sender, BankAccounts.getInstance().config().messagesErrorsNotAccountOwner());
        if (account.get().frozen)
            return sendMessage(sender, BankAccounts.getInstance().config().messagesErrorsFrozen(account.get()));

        final @Nullable String description = argsCopy.length < 3 ? null : String.join(" ", Arrays.copyOfRange(argsCopy, 2, argsCopy.length));

        final @NotNull Invoice invoice = new Invoice(account.get(), amount, description, target);
        invoice.insert();
        // TODO: send messages

        return true;
    }
}
