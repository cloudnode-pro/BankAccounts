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
import java.util.ArrayList;
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
            case "create", "new" -> create(sender, Arrays.copyOfRange(args, 1, args.length), label);
            case "view", "details", "check", "show" -> details(sender, Arrays.copyOfRange(args, 1, args.length), label);
            case "pay" -> pay(sender, Arrays.copyOfRange(args, 1, args.length), label);
            default -> sendMessage(sender, BankAccounts.getInstance().config().messagesErrorsUnknownCommand());
        };
    }

    @Override
    public @Nullable List<@NotNull String> tab(final @NotNull CommandSender sender, final @NotNull String @NotNull [] args) {
        final @NotNull List<@NotNull String> list = new ArrayList<>();
        if (args.length <= 1) {
            if (sender.hasPermission(Permissions.INVOICE_CREATE)) list.addAll(Arrays.asList("create", "new"));
            if (sender.hasPermission(Permissions.INVOICE_VIEW)) list.addAll(Arrays.asList("view", "details", "check", "show"));
            if (sender.hasPermission(Permissions.TRANSFER_SELF) || sender.hasPermission(Permissions.TRANSFER_OTHER)) list.add("pay");
        }
        else switch (args[0]) {
            case "create", "new" -> {
                if ("--player".equals(args[args.length - 1])) return null;
                else if (!args[args.length - 1].isEmpty() && "--player".startsWith(args[args.length - 1])) list.add("--player");
                else if (args.length == 2) {
                    if (sender.hasPermission(Permissions.INVOICE_CREATE) && sender.hasPermission(Permissions.INVOICE_CREATE_OTHER))
                        list.addAll(Arrays.stream(Account.get()).map(a -> a.id).toList());
                    else if (sender.hasPermission(Permissions.INVOICE_CREATE))
                        list.addAll(Arrays.stream(Account.get(BankAccounts.getOfflinePlayer(sender))).map(a -> a.id).toList());
                }
            }
            case "view", "details", "check", "show" -> {
                if (args.length == 2) {
                    if (sender.hasPermission(Permissions.INVOICE_VIEW) && sender.hasPermission(Permissions.INVOICE_VIEW_OTHER))
                        list.addAll(Arrays.stream(Invoice.get()).map(a -> a.id).toList());
                    else if (sender.hasPermission(Permissions.INVOICE_VIEW))
                        list.addAll(Arrays.stream(Invoice.get(BankAccounts.getOfflinePlayer(sender))).map(a -> a.id).toList());
                }
            }
            case "pay" -> {
                if (!sender.hasPermission(Permissions.TRANSFER_SELF) && !sender.hasPermission(Permissions.TRANSFER_OTHER)) break;
                if (args.length == 2) {
                    if (sender.hasPermission(Permissions.INVOICE_PAY_OTHER)) list.addAll(Arrays.stream(Invoice.get()).map(a -> a.id).toList());
                    else list.addAll(Arrays.stream(Invoice.get(BankAccounts.getOfflinePlayer(sender))).map(a -> a.id).toList());
                }
                else if (args.length == 3) {
                    if (sender.hasPermission(Permissions.INVOICE_PAY_ACCOUNT_OTHER)) list.addAll(Arrays.stream(Account.get()).map(a -> a.id).toList());
                    else list.addAll(Arrays.stream(Account.get(BankAccounts.getOfflinePlayer(sender))).map(a -> a.id).toList());
                }
            }
        }
        return list;
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

    /**
     * View invoice details
     * <p>{@code /invoice details <invoice>}</p>
     */
    public static boolean details(final @NotNull CommandSender sender, @NotNull String @NotNull [] args, final @NotNull String label) {
        if (!sender.hasPermission(Permissions.INVOICE_VIEW))
            return sendMessage(sender, BankAccounts.getInstance().config().messagesErrorsNoPermission());

        if (args.length < 1)
            return sendUsage(sender, label, "details <invoice>");

        final @NotNull Optional<@NotNull Invoice> invoice = Invoice.get(args[0]);
        if (invoice.isEmpty()) return sendMessage(sender, BankAccounts.getInstance().config().messagesErrorsInvoiceNotFound());
        if (!sender.hasPermission(Permissions.INVOICE_VIEW_OTHER) && invoice.get().buyer().map(b -> b.getUniqueId().equals(BankAccounts.getOfflinePlayer(sender).getUniqueId())).orElse(true))
            return sendMessage(sender, BankAccounts.getInstance().config().messagesErrorsInvoiceNotFound());

        return sendMessage(sender, BankAccounts.getInstance().config().messagesInvoiceDetails(invoice.get()));
    }

    /**
     * Pay invoice
     * <p>{@code /invoice pay <invoice> <account>}</p>
     */
    public static boolean pay(final @NotNull CommandSender sender, @NotNull String @NotNull [] args, final @NotNull String label) {
        if (!sender.hasPermission(Permissions.TRANSFER_SELF) && !sender.hasPermission(Permissions.TRANSFER_OTHER))
            return sendMessage(sender, BankAccounts.getInstance().config().messagesErrorsNoPermission());

        final @NotNull String usage = "pay <invoice> <account>";
        if (args.length < 1) return sendUsage(sender, label, usage);
        if (args.length < 2) return sendUsage(sender, label, usage.replace("<invoice>", args[0]));

        final @NotNull Optional<@NotNull Invoice> invoice = Invoice.get(args[0]);
        if (invoice.isEmpty()) return sendMessage(sender, BankAccounts.getInstance().config().messagesErrorsInvoiceNotFound());
        if (!sender.hasPermission(Permissions.INVOICE_PAY_OTHER) && invoice.get().buyer().map(b -> !b.getUniqueId().equals(BankAccounts.getOfflinePlayer(sender).getUniqueId())).orElse(false))
            return sendMessage(sender, BankAccounts.getInstance().config().messagesErrorsInvoiceNotFound());
        if (invoice.get().transaction != null)
            return sendMessage(sender, BankAccounts.getInstance().config().messagesErrorsInvoiceAlreadyPaid());

        final @NotNull Optional<@NotNull Account> account = Account.get(args[1]);
        if (account.isEmpty())
            return sendMessage(sender, BankAccounts.getInstance().config().messagesErrorsAccountNotFound());
        if (!sender.hasPermission(Permissions.INVOICE_PAY_ACCOUNT_OTHER) && !account.get().owner.getUniqueId().equals(BankAccounts.getOfflinePlayer(sender).getUniqueId()))
            return sendMessage(sender, BankAccounts.getInstance().config().messagesErrorsNotAccountOwner());
        if (invoice.get().seller.id.equals(account.get().id))
            return sendMessage(sender, BankAccounts.getInstance().config().messagesErrorsInvoicePaySelf());
        if (account.get().frozen)
            return sendMessage(sender, BankAccounts.getInstance().config().messagesErrorsFrozen(account.get()));
        if (!account.get().hasFunds(invoice.get().amount))
            return sendMessage(sender, BankAccounts.getInstance().config().messagesErrorsInsufficientFunds(account.get()));

        invoice.get().pay(account.get());

        // TODO: invoice paid messages

        return true;
    }
}
