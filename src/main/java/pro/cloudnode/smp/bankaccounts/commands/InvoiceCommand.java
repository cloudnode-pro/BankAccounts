package pro.cloudnode.smp.bankaccounts.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pro.cloudnode.smp.bankaccounts.Account;
import pro.cloudnode.smp.bankaccounts.BankAccounts;
import pro.cloudnode.smp.bankaccounts.BankConfig;
import pro.cloudnode.smp.bankaccounts.Command;
import pro.cloudnode.smp.bankaccounts.Invoice;
import pro.cloudnode.smp.bankaccounts.Permissions;
import pro.cloudnode.smp.bankaccounts.commands.result.CommandResult;
import pro.cloudnode.smp.bankaccounts.commands.result.Message;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class InvoiceCommand extends Command {
    @Override
    public @NotNull CommandResult execute(final @NotNull CommandSender sender, final @NotNull String label, final @NotNull String @NotNull [] args) {
        if (!sender.hasPermission(Permissions.COMMAND))
            return new Message(sender, BankAccounts.getInstance().config().messagesErrorsNoPermission());
        if (args.length < 1)
            return help(sender, label);
        return switch (args[0]) {
            case "help" -> help(sender, label);
            case "create", "new" -> create(sender, Arrays.copyOfRange(args, 1, args.length), label);
            case "view", "details", "check", "show" -> details(sender, Arrays.copyOfRange(args, 1, args.length), label);
            case "pay" -> pay(sender, Arrays.copyOfRange(args, 1, args.length), label);
            case "send", "remind" -> send(sender, Arrays.copyOfRange(args, 1, args.length), label);
            case "list" -> list(sender, Arrays.copyOfRange(args, 1, args.length), label);
            default -> new Message(sender, BankAccounts.getInstance().config().messagesErrorsUnknownCommand(label));
        };
    }

    @Override
    public @Nullable List<@NotNull String> tab(final @NotNull CommandSender sender, final @NotNull String @NotNull [] args) {
        final @NotNull List<@NotNull String> list = new ArrayList<>();
        if (args.length <= 1) {
            list.add("help");
            if (sender.hasPermission(Permissions.INVOICE_CREATE)) list.addAll(Arrays.asList("create", "new"));
            if (sender.hasPermission(Permissions.INVOICE_VIEW)) list.addAll(Arrays.asList("view", "details", "check", "show"));
            if (sender.hasPermission(Permissions.TRANSFER_SELF) || sender.hasPermission(Permissions.TRANSFER_OTHER)) list.add("pay");
            if (sender.hasPermission(Permissions.INVOICE_SEND)) list.addAll(Arrays.asList("send", "remind"));
            if (sender.hasPermission(Permissions.INVOICE_VIEW)) list.add("list");
        }
        else switch (args[0]) {
            case "create", "new" -> {
                if ("--player".equals(args[args.length - 2])) return null;
                else if (!args[args.length - 1].isEmpty() && "--player".startsWith(args[args.length - 1])) list.add("--player");
                else if (args.length == 2 && sender.hasPermission(Permissions.INVOICE_CREATE)) {
                    if (sender.hasPermission(Permissions.INVOICE_CREATE_OTHER) || !(sender instanceof final @NotNull OfflinePlayer player)) {
                        if (args[1].startsWith("@")) list.addAll(sender.getServer().getOnlinePlayers().stream().map(p -> "@" + p.getName()).collect(Collectors.toSet()));
                        else list.addAll(Arrays.stream(Account.get()).map(a -> a.id).toList());
                    }
                    else {
                        if (args[1].startsWith("@")) list.add("@" + player.getName());
                        else list.addAll(Arrays.stream(Account.get(BankAccounts.getOfflinePlayer(sender))).map(a -> a.id).toList());
                    }
                }
            }
            case "view", "details", "check", "show" -> {
                if (args.length == 2) {
                    if (sender.hasPermission(Permissions.INVOICE_VIEW) && sender.hasPermission(Permissions.INVOICE_VIEW_OTHER))
                        list.addAll(Arrays.stream(Invoice.get()).map(a -> a.id).toList());
                    else if (sender.hasPermission(Permissions.INVOICE_VIEW)) {
                        final @NotNull Account @NotNull [] accounts = Account.get(BankAccounts.getOfflinePlayer(sender));
                        list.addAll(Arrays.stream(Invoice.get(BankAccounts.getOfflinePlayer(sender), accounts)).map(a -> a.id).toList());
                    }
                }
            }
            case "pay" -> {
                if (!sender.hasPermission(Permissions.TRANSFER_SELF) && !sender.hasPermission(Permissions.TRANSFER_OTHER)) break;
                if (args.length == 2) {
                    if (sender.hasPermission(Permissions.INVOICE_PAY_OTHER)) list.addAll(Arrays.stream(Invoice.get()).map(a -> a.id).toList());
                    else list.addAll(Arrays.stream(Invoice.get(BankAccounts.getOfflinePlayer(sender))).map(a -> a.id).toList());
                }
                else if (args.length == 3) {
                    if (sender.hasPermission(Permissions.INVOICE_PAY_ACCOUNT_OTHER) || !(sender instanceof final @NotNull OfflinePlayer player)) {
                        if (args[2].startsWith("@")) list.addAll(sender.getServer().getOnlinePlayers().stream().map(p -> "@" + p.getName()).collect(Collectors.toSet()));
                        else list.addAll(Arrays.stream(Account.get()).map(a -> a.id).toList());
                    }
                    else {
                        if (args[2].startsWith("@")) list.add("@" + player.getName());
                        else list.addAll(Arrays.stream(Account.get(BankAccounts.getOfflinePlayer(sender))).map(a -> a.id).toList());
                    }
                }
            }
            case "send", "remind" -> {
                if (!sender.hasPermission(Permissions.INVOICE_SEND)) break;
                if (args.length == 2) {
                    if (sender.hasPermission(Permissions.INVOICE_SEND_OTHER)) list.addAll(Arrays.stream(Invoice.get()).map(a -> a.id).toList());
                    else list.addAll(Arrays.stream(Invoice.get(BankAccounts.getOfflinePlayer(sender), Account.get(BankAccounts.getOfflinePlayer(sender)))).filter(i -> i.transaction == null).map(a -> a.id).toList());
                }
                if (args.length == 3) {
                    final @NotNull Optional<@NotNull Invoice> invoice = Invoice.get(args[1]);
                    if (invoice.isPresent() && (sender.hasPermission(Permissions.INVOICE_SEND_OTHER) || invoice.get().seller.owner.equals(BankAccounts.getOfflinePlayer(sender)))) {
                        invoice.get().buyer().flatMap(buyer -> Optional.ofNullable(buyer.getPlayer()))
                                .ifPresent(player -> list.add(player.getName()));
                    }
                }
            }
            case "list" -> {
                if (!sender.hasPermission(Permissions.INVOICE_VIEW)) break;
                if (sender.hasPermission(Permissions.INVOICE_VIEW_OTHER) && "--player".equals(args[args.length - 1])) return null;
                else if (sender.hasPermission(Permissions.INVOICE_VIEW_OTHER) && !args[args.length - 1].isEmpty() && "--player".startsWith(args[args.length - 1])) list.add("--player");
                else if (args.length == 2) list.addAll(Arrays.asList("all", "sent", "received"));
            }
        }
        return list;
    }

    /**
     * Show available commands
     * <p>{@code /invoice help}</p>
     */
    public static @NotNull CommandResult help(final @NotNull CommandSender sender, final @NotNull String label) {
        BankAccounts.getInstance().config().messagesHelpInvoiceHeader().ifPresent(sender::sendMessage);
        if (sender.hasPermission(Permissions.INVOICE_CREATE)) {
            BankAccounts.getInstance().config().messagesHelpInvoiceCommands(BankConfig.HelpCommandsInvoice.CREATE, label + " create", "<account> <amount> [description]").ifPresent(sender::sendMessage);
            BankAccounts.getInstance().config().messagesHelpInvoiceCommands(BankConfig.HelpCommandsInvoice.CREATE_PLAYER, label + " create", "<player> <amount> [description] --player <player>").ifPresent(sender::sendMessage);
        }
        if (sender.hasPermission(Permissions.INVOICE_VIEW))
            BankAccounts.getInstance().config().messagesHelpInvoiceCommands(BankConfig.HelpCommandsInvoice.VIEW, label + " view", "<invoice>").ifPresent(sender::sendMessage);
        if (sender.hasPermission(Permissions.TRANSFER_SELF) || sender.hasPermission(Permissions.TRANSFER_OTHER))
            BankAccounts.getInstance().config().messagesHelpInvoiceCommands(BankConfig.HelpCommandsInvoice.PAY, label + " pay", "<invoice> <account>").ifPresent(sender::sendMessage);
        if (sender.hasPermission(Permissions.INVOICE_SEND))
            BankAccounts.getInstance().config().messagesHelpInvoiceCommands(BankConfig.HelpCommandsInvoice.SEND, label + " send", "<invoice> <player>").ifPresent(sender::sendMessage);
        if (sender.hasPermission(Permissions.INVOICE_VIEW)) {
            BankAccounts.getInstance().config().messagesHelpInvoiceCommands(BankConfig.HelpCommandsInvoice.LIST, label + " list", "[all|sent|received] [page]").ifPresent(sender::sendMessage);
            if (sender.hasPermission(Permissions.INVOICE_VIEW_OTHER))
                BankAccounts.getInstance().config().messagesHelpInvoiceCommands(BankConfig.HelpCommandsInvoice.LIST_OTHER, label + " list", "[all|sent|received] [page] --player <player>").ifPresent(sender::sendMessage);
        }
        BankAccounts.getInstance().config().messagesHelpInvoiceFooter().ifPresent(sender::sendMessage);
        return CommandResult.DO_NOTHING;
    }

    /**
     * Create invoice
     * <p>{@code /invoice create <account> <amount> [description] [--player <player>]}</p>
     */
    public static @NotNull CommandResult create(final @NotNull CommandSender sender, @NotNull String @NotNull [] args, final @NotNull String label) {
        if (!sender.hasPermission(Permissions.INVOICE_CREATE))
            return new Message(sender, BankAccounts.getInstance().config().messagesErrorsNoPermission());

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

        if (target != null && !target.isOnline() && !target.hasPlayedBefore())
            return new Message(sender, BankAccounts.getInstance().config().messagesErrorsPlayerNeverJoined());

        final @NotNull String usage = "create <account> <amount> [description] [--player <player>]";
        if (argsCopy.length == 0) return sendUsage(sender, label, usage);
        if (argsCopy.length == 1) return sendUsage(sender, label, usage.replace("<account>", argsCopy[0]));

        final @NotNull BigDecimal amount;
        try {
            amount = new BigDecimal(argsCopy[1]).setScale(2, RoundingMode.HALF_UP);
        }
        catch (final @NotNull NumberFormatException e) {
            return new Message(sender, BankAccounts.getInstance().config().messagesErrorsInvalidNumber(argsCopy[1]));
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0)
            return new Message(sender, BankAccounts.getInstance().config().messagesErrorsNegativeInvoice());

        final @NotNull Optional<@NotNull Account> account = Account.get(Account.Tag.from(argsCopy[0]));
        if (account.isEmpty())
            return new Message(sender, BankAccounts.getInstance().config().messagesErrorsAccountNotFound());
        if (!sender.hasPermission(Permissions.INVOICE_CREATE_OTHER) && !account.get().owner.getUniqueId().equals(BankAccounts.getOfflinePlayer(sender).getUniqueId()))
            return new Message(sender, BankAccounts.getInstance().config().messagesErrorsNotAccountOwner());
        if (account.get().frozen)
            return new Message(sender, BankAccounts.getInstance().config().messagesErrorsFrozen(account.get()));

        @Nullable String description = argsCopy.length < 3 ? null : String.join(" ", Arrays.copyOfRange(argsCopy, 2, argsCopy.length));
        if (description != null && description.length() > 64) description = description.substring(0, 63) + "…";

        final @NotNull Set<@NotNull String> disallowedChars = getDisallowedCharacters(description);
        if (!disallowedChars.isEmpty())
            return new Message(sender, BankAccounts.getInstance().config().messagesErrorsDisallowedCharacters(disallowedChars));

        final @NotNull Invoice invoice = new Invoice(account.get(), amount, description, target);
        invoice.insert();

        final @NotNull Optional<@NotNull Player> onlineRecipient = invoice.buyer().isPresent() ? Optional.ofNullable(invoice.buyer().get().getPlayer()) : Optional.empty();
        onlineRecipient.ifPresent(player -> player.sendMessage(BankAccounts.getInstance().config().messagesInvoiceReceived(invoice)));
        return new Message(sender, BankAccounts.getInstance().config().messagesInvoiceCreated(invoice));
    }

    /**
     * View invoice details
     * <p>{@code /invoice details <invoice>}</p>
     */
    public static @NotNull CommandResult details(final @NotNull CommandSender sender, @NotNull String @NotNull [] args, final @NotNull String label) {
        if (!sender.hasPermission(Permissions.INVOICE_VIEW))
            return new Message(sender, BankAccounts.getInstance().config().messagesErrorsNoPermission());

        if (args.length < 1)
            return sendUsage(sender, label, "details <invoice>");

        final @NotNull Optional<@NotNull Invoice> invoice = Invoice.get(args[0]);
        if (invoice.isEmpty()) return new Message(sender, BankAccounts.getInstance().config().messagesErrorsInvoiceNotFound());
        if (!sender.hasPermission(Permissions.INVOICE_VIEW_OTHER)
                && !invoice.get().buyer().map(b -> b.getUniqueId().equals(BankAccounts.getOfflinePlayer(sender).getUniqueId())).orElse(true)
                && !invoice.get().seller.owner.getUniqueId().equals(BankAccounts.getOfflinePlayer(sender).getUniqueId())
        ) return new Message(sender, BankAccounts.getInstance().config().messagesErrorsInvoiceNotFound());

        return new Message(sender, BankAccounts.getInstance().config().messagesInvoiceDetails(invoice.get()));
    }

    /**
     * Pay invoice
     * <p>{@code /invoice pay <invoice> <account>}</p>
     */
    public static @NotNull CommandResult pay(final @NotNull CommandSender sender, @NotNull String @NotNull [] args, final @NotNull String label) {
        if (!sender.hasPermission(Permissions.TRANSFER_SELF) && !sender.hasPermission(Permissions.TRANSFER_OTHER))
            return new Message(sender, BankAccounts.getInstance().config().messagesErrorsNoPermission());

        final @NotNull String usage = "pay <invoice> <account>";
        if (args.length < 1) return sendUsage(sender, label, usage);
        if (args.length < 2) return sendUsage(sender, label, usage.replace("<invoice>", args[0]));

        final @NotNull Optional<@NotNull Invoice> invoice = Invoice.get(args[0]);
        if (invoice.isEmpty()) return new Message(sender, BankAccounts.getInstance().config().messagesErrorsInvoiceNotFound());
        if (!sender.hasPermission(Permissions.INVOICE_PAY_OTHER) && invoice.get().buyer().map(b -> !b.getUniqueId().equals(BankAccounts.getOfflinePlayer(sender).getUniqueId())).orElse(false))
            return new Message(sender, BankAccounts.getInstance().config().messagesErrorsInvoiceNotFound());
        if (invoice.get().transaction != null)
            return new Message(sender, BankAccounts.getInstance().config().messagesErrorsInvoiceAlreadyPaid());

        final @NotNull Optional<@NotNull Account> account = Account.get(Account.Tag.from(args[1]));
        if (account.isEmpty())
            return new Message(sender, BankAccounts.getInstance().config().messagesErrorsAccountNotFound());
        if (!sender.hasPermission(Permissions.INVOICE_PAY_ACCOUNT_OTHER) && !account.get().owner.getUniqueId().equals(BankAccounts.getOfflinePlayer(sender).getUniqueId()))
            return new Message(sender, BankAccounts.getInstance().config().messagesErrorsNotAccountOwner());
        if (invoice.get().seller.id.equals(account.get().id))
            return new Message(sender, BankAccounts.getInstance().config().messagesErrorsInvoicePaySelf());
        if (!sender.hasPermission(Permissions.TRANSFER_SELF) && account.get().owner.getUniqueId().equals(invoice.get().seller.owner.getUniqueId()))
            return new Message(sender, BankAccounts.getInstance().config().messagesErrorsTransferOtherOnly());
        if (!sender.hasPermission(Permissions.TRANSFER_OTHER) && !account.get().owner.getUniqueId().equals(invoice.get().seller.owner.getUniqueId()))
            return new Message(sender, BankAccounts.getInstance().config().messagesErrorsTransferOtherOnly());
        if (account.get().frozen)
            return new Message(sender, BankAccounts.getInstance().config().messagesErrorsFrozen(account.get()));
        if (!account.get().hasFunds(invoice.get().amount))
            return new Message(sender, BankAccounts.getInstance().config().messagesErrorsInsufficientFunds(account.get()));

        invoice.get().pay(account.get());

        Optional.ofNullable(invoice.get().seller.owner.getPlayer()).ifPresent(player -> player.sendMessage(BankAccounts.getInstance().config().messagesInvoicePaidSeller(invoice.get())));
        return new Message(sender, BankAccounts.getInstance().config().messagesInvoicePaidBuyer(invoice.get()));
    }

    /**
     * Send invoice to a player
     * <p>{@code /invoice send <invoice> <player>}</p>
     */
    public static @NotNull CommandResult send(final @NotNull CommandSender sender, @NotNull String @NotNull [] args, final @NotNull String label) {
        if (!sender.hasPermission(Permissions.INVOICE_SEND))
            return new Message(sender, BankAccounts.getInstance().config().messagesErrorsNoPermission());

        final @NotNull String usage = "send <invoice> <player>";
        if (args.length < 1) return sendUsage(sender, label, usage);
        if (args.length < 2) return sendUsage(sender, label, usage.replace("<invoice>", args[0]));

        final @NotNull Optional<@NotNull Player> player = Optional.ofNullable(BankAccounts.getInstance().getServer().getPlayer(args[1]));
        if (player.isEmpty()) return new Message(sender, BankAccounts.getInstance().config().messagesErrorsPlayerNotFound());

        final @NotNull Optional<@NotNull Invoice> invoice = Invoice.get(args[0]);
        if (invoice.isEmpty()) return new Message(sender, BankAccounts.getInstance().config().messagesErrorsInvoiceNotFound());
        if (!sender.hasPermission(Permissions.INVOICE_SEND_OTHER) && !invoice.get().seller.owner.getUniqueId().equals(BankAccounts.getOfflinePlayer(sender).getUniqueId()))
            return new Message(sender, BankAccounts.getInstance().config().messagesErrorsInvoiceNotFound());
        if (invoice.get().buyer().isPresent() && !invoice.get().buyer().get().getUniqueId().equals(player.get().getUniqueId()) && !player.get().hasPermission(Permissions.INVOICE_VIEW_OTHER))
            return new Message(sender, BankAccounts.getInstance().config().messagesErrorsInvoiceCannotSend());
        if (invoice.get().transaction != null)
            return new Message(sender, BankAccounts.getInstance().config().messagesErrorsInvoiceAlreadyPaid());

        player.get().sendMessage(BankAccounts.getInstance().config().messagesInvoiceReceived(invoice.get()));
        return new Message(sender, BankAccounts.getInstance().config().messagesInvoiceSent(invoice.get()));
    }

    /**
     * List invoices
     * <p>{@code /invoice list [all|sent|received] [page] [--player <player>]}</p>
     */
    public static @NotNull CommandResult list(final @NotNull CommandSender sender, @NotNull String @NotNull [] args, final @NotNull String label) {
        if (!sender.hasPermission(Permissions.INVOICE_VIEW))
            return new Message(sender, BankAccounts.getInstance().config().messagesErrorsNoPermission());

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

        final @NotNull OfflinePlayer target = playerArg.map(u -> BankAccounts.getInstance().getServer().getOfflinePlayer(u)).orElse(BankAccounts.getOfflinePlayer(sender));
        if (!target.isOnline() && !target.hasPlayedBefore() && !target.getUniqueId().equals(BankAccounts.getConsoleOfflinePlayer().getUniqueId()))
            return new Message(sender, BankAccounts.getInstance().config().messagesErrorsPlayerNeverJoined());

        final @NotNull String type = argsCopy.length < 1 ? "all" : argsCopy[0];
        final int page;
        if (argsCopy.length < 2) page = 1;
        else {
            try {
                page = Integer.parseInt(argsCopy[1]);
            }
            catch (final @NotNull NumberFormatException e) {
                return new Message(sender, BankAccounts.getInstance().config().messagesErrorsInvalidNumber(argsCopy[1]));
            }
        }

        final int limit = BankAccounts.getInstance().config().invoicePerPage();
        final int offset = limit * (page - 1);

        final @NotNull Invoice @NotNull [] invoices = switch (type.toLowerCase()) {
            case "received" -> Invoice.get(target, limit, offset);
            case "sent" -> {
                final @NotNull Account @NotNull [] sellerAccounts = Account.get(BankAccounts.getOfflinePlayer(sender));
                yield Invoice.get(sellerAccounts, limit, offset);
            }
            default -> {
                final @NotNull Account @NotNull [] sellerAccounts = Account.get(BankAccounts.getOfflinePlayer(sender));
                yield Invoice.get(target, sellerAccounts, limit, offset);
            }
        };

        final @NotNull String cmdPrev = "/" + label + " list " + type + " " + (page - 1) + playerArg.map(u -> " --player " + u).orElse("");
        final @NotNull String cmdNext = "/" + label + " list " + type + " " + (page + 1) + playerArg.map(u -> " --player " + u).orElse("");
        
        sender.sendMessage(BankAccounts.getInstance().config().messagesInvoiceListHeader(page, cmdPrev, cmdNext));
        for (final @NotNull Invoice invoice : invoices) sender.sendMessage(BankAccounts.getInstance().config().messagesInvoiceListEntry(invoice));
        return new Message(sender, BankAccounts.getInstance().config().messagesInvoiceListFooter(page, cmdPrev, cmdNext));
    }
}
