package pro.cloudnode.smp.bankaccounts.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pro.cloudnode.smp.bankaccounts.Account;
import pro.cloudnode.smp.bankaccounts.BankAccounts;
import pro.cloudnode.smp.bankaccounts.BankConfig;
import pro.cloudnode.smp.bankaccounts.Transaction;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class BankCommand extends pro.cloudnode.smp.bankaccounts.Command {
    @Override
    public boolean onCommand(final @NotNull CommandSender sender, final @NotNull Command command, final @NotNull String label, final @NotNull String @NotNull [] args) {
        if (!sender.hasPermission("bank.command")) return sendMessage(sender, BankConfig.MESSAGES_ERRORS_NO_PERMISSION);
        return run(sender, label, args);
    }

    @Override
    public @NotNull ArrayList<@NotNull String> onTabComplete(final @NotNull CommandSender sender, final @NotNull Command command, final @NotNull String label, final @NotNull String @NotNull [] args) {
        final @NotNull ArrayList<@NotNull String> suggestions = new ArrayList<>();
        if (!sender.hasPermission("bank.command")) return suggestions;
        if (args.length == 1) {
            suggestions.add("help");
            if (sender.hasPermission("bank.balance.self"))
                suggestions.addAll(Arrays.asList("balance", "bal", "account", "accounts"));
            if (sender.hasPermission("bank.reload")) suggestions.add("reload");
            if (sender.hasPermission("bank.account.create")) suggestions.addAll(Arrays.asList("create", "new"));
            if (sender.hasPermission("bank.set.balance")) suggestions.addAll(Arrays.asList("setbal", "setbalance"));
            if (sender.hasPermission("bank.set.name")) suggestions.addAll(Arrays.asList("setname", "rename"));
            if (sender.hasPermission("bank.delete")) suggestions.add("delete");
            if (sender.hasPermission("bank.transfer.self") || sender.hasPermission("bank.transfer.other"))
                suggestions.addAll(Arrays.asList("transfer", "send", "pay"));
            if (sender.hasPermission("bank.history")) suggestions.addAll(Arrays.asList("transactions", "history"));
            if (sender.hasPermission("bank.instrument.create")) suggestions.addAll(Arrays.asList("instrument", "card"));
            if (sender.hasPermission("bank.whois")) suggestions.addAll(Arrays.asList("whois", "who", "info"));
        }
        else {
            switch (args[0]) {
                case "balance", "bal", "account", "accounts" -> {
                    if (!sender.hasPermission("bank.balance.self") && !sender.hasPermission("bank.balance.other"))
                        return suggestions;
                    if (args.length == 2) {
                        if (sender.hasPermission("bank.balance.other")) suggestions.add("--player");
                        if (sender instanceof OfflinePlayer player) {
                            final @NotNull Account @NotNull [] accounts = Account.get(player);
                            for (Account account : accounts) suggestions.add(account.id);
                        }
                        else {
                            final @NotNull Account @NotNull [] accounts = Account.get();
                            for (final @NotNull Account account : accounts) suggestions.add(account.id);
                        }
                    }
                    else if (args.length == 3 && args[1].equals("--player") && sender.hasPermission("bank.balance.other"))
                        suggestions.addAll(Arrays.stream(Account.get()).map(account -> account.owner.getName())
                                .filter(Objects::nonNull).collect(Collectors.toSet()));
                }
                case "create", "new" -> {
                    if (!sender.hasPermission("bank.account.create")) return suggestions;
                    if (args.length == 2) {
                        suggestions.addAll(Arrays.asList("PERSONAL", "BUSINESS"));
                    }
                    else if (args.length == 3 && sender.hasPermission("bank.account.create.other"))
                        suggestions.add("--player");
                    else if (args.length == 4 && args[2].equals("--player") && sender.hasPermission("bank.account.create.other"))
                        suggestions.addAll(Arrays.stream(Account.get()).map(account -> account.owner.getName())
                                .filter(Objects::nonNull).collect(Collectors.toSet()));
                }
                case "setbal", "setbalance" -> {
                    if (!sender.hasPermission("bank.set.balance")) return suggestions;
                    if (args.length == 2) {
                        final @NotNull Account @NotNull [] accounts = Account.get();
                        for (final @NotNull Account account : accounts) suggestions.add(account.id);
                    }
                    if (args.length == 3) suggestions.add("Infinity");
                }
                case "setname", "rename" -> {
                    if (!sender.hasPermission("bank.set.name")) return suggestions;
                    if (args.length == 2) {
                        final @NotNull Account @NotNull [] accounts = sender.hasPermission("bank.set.name.other") ? Account.get() : Account.get(BankAccounts.getOfflinePlayer(sender));
                        for (final @NotNull Account account : accounts) suggestions.add(account.id);
                    }
                }
                case "delete" -> {
                    if (!sender.hasPermission("bank.delete")) return suggestions;
                    if (args.length == 2) suggestions.addAll(Arrays
                            .stream(sender.hasPermission("bank.delete.other") ? Account.get() : Account.get(BankAccounts.getOfflinePlayer(sender)))
                            .filter(account -> sender.hasPermission("bank.delete.person") || account.type != Account.Type.PERSONAL)
                            .map(account -> account.id).collect(Collectors.toSet()));
                }
                case "transfer", "send", "pay" -> {
                    if (!sender.hasPermission("bank.transfer.self") && !sender.hasPermission("bank.transfer.other"))
                        return suggestions;
                    if (args.length == 2) {
                        final @NotNull Account @NotNull [] accounts = Account.get(BankAccounts.getOfflinePlayer(sender));
                        for (final @NotNull Account account : accounts) suggestions.add(account.id);
                    }
                    else if (args.length == 3) suggestions.addAll(Arrays
                            .stream(sender.hasPermission("bank.transfer.other") ? Account.get() : Account.get(BankAccounts.getOfflinePlayer(sender)))
                            .filter(account -> !account.id.equals(args[1])).map(account -> account.id)
                            .collect(Collectors.toSet()));
                }
                case "transactions", "history" -> {
                    if (!sender.hasPermission("bank.history")) return suggestions;
                    if (args.length == 2) {
                        final @NotNull Account @NotNull [] accounts = sender.hasPermission("bank.history.other") ? Account.get() : Account.get(BankAccounts.getOfflinePlayer(sender));
                        for (final @NotNull Account account : accounts) suggestions.add(account.id);
                    }
                    else if (args.length == 3) suggestions.add("--all");
                }
                case "instrument", "card" -> {
                    if (!sender.hasPermission("bank.instrument.create")) return suggestions;
                    if (args.length == 2) {
                        final @NotNull Account @NotNull [] accounts = sender.hasPermission("bank.instrument.create.other") ? Account.get() : Account.get(BankAccounts.getOfflinePlayer(sender));
                        for (final @NotNull Account account : accounts) suggestions.add(account.id);
                    }
                    else if (args.length == 3 && sender.hasPermission("bank.instrument.create.other"))
                        suggestions.addAll(BankAccounts.getInstance().getServer().getOnlinePlayers().stream()
                                .map(Player::getName).toList());
                }
                case "whois", "who", "info" -> {
                    if (!sender.hasPermission("bank.whois")) return suggestions;
                    if (args.length == 2)
                        suggestions.addAll(Arrays.stream(Account.get()).map(account -> account.id).toList());
                }
            }
        }
        return suggestions;
    }

    /**
     * Decide which command to run
     */
    public static boolean run(final @NotNull CommandSender sender, final @NotNull String label, final @NotNull String @NotNull [] args) {
        if (args.length == 0) return overview(sender);
        final @NotNull String @NotNull [] argsSubset = Arrays.copyOfRange(args, 1, args.length);
        return switch (args[0]) {
            case "help" -> help(sender);
            case "bal", "balance", "account", "accounts" ->
                    balance(sender, Arrays.copyOfRange(args, 1, args.length), label);
            case "reload" -> reload(sender);
            case "create", "new" -> create(sender, argsSubset, label);
            case "setbal", "setbalance" -> setBalance(sender, argsSubset, label);
            case "setname", "rename" -> setName(sender, argsSubset, label);
            case "delete" -> delete(sender, argsSubset, label);
            case "transfer", "send", "pay" -> transfer(sender, argsSubset, label);
            case "transactions", "history" -> transactions(sender, argsSubset, label);
            case "instrument", "card" -> instrument(sender, argsSubset, label);
            case "whois", "who", "info" -> whois(sender, argsSubset, label);
            default -> sendMessage(sender, BankConfig.MESSAGES_ERRORS_UNKNOWN_COMMAND);
        };
    }

    /**
     * Plugin overview
     */
    public static boolean overview(final @NotNull CommandSender sender) {
        final @NotNull BankAccounts plugin = BankAccounts.getInstance();
        return sendMessage(sender, "<green><name></green> <white>v<version> by</white> <gray><author></gray>", Placeholder.unparsed("name", plugin
                .getPluginMeta().getName()), Placeholder.unparsed("version", plugin.getPluginMeta()
                .getVersion()), Placeholder.unparsed("author", String.join(", ", plugin.getPluginMeta().getAuthors())));
    }

    /**
     * Plugin help
     */
    public static boolean help(final @NotNull CommandSender sender) {
        sendMessage(sender, "<dark_gray>---</dark_gray>");
        sendMessage(sender, "<green>Available commands:");
        sendMessage(sender, "");
        if (sender.hasPermission("bank.balance.self"))
            sendMessage(sender, "<click:suggest_command:/bank balance ><green>/bank balance <gray>[account]</gray></green> <white>- Check your accounts</click>");
        if (sender.hasPermission("bank.balance.other"))
            sendMessage(sender, "<click:suggest_command:/bank balance --player ><green>/bank balance <gray>--player [player]</gray></green> <white>- List another player's accounts</click>");
        if (sender.hasPermission("bank.transfer.self") || sender.hasPermission("bank.transfer.other"))
            sendMessage(sender, "<click:suggest_command:/bank transfer ><green>/bank transfer <gray><from> <to> <amount> [description]</gray></green> <white>- Transfer money to another account</click>");
        if (sender.hasPermission("bank.history"))
            sendMessage(sender, "<click:suggest_command:/bank transactions ><green>/bank transactions <gray><account> [page=1]</gray></green> <white>- List transactions</click>");
        if (sender.hasPermission("bank.account.create"))
            sendMessage(sender, "<click:suggest_command:/bank create ><green>/bank create <gray><PERSONAL|BUSINESS></gray></green> <white>- Create a new account</click>");
        if (sender.hasPermission("bank.account.create.other"))
            sendMessage(sender, "<click:suggest_command:/bank create --player ><green>/bank create <gray><PERSONAL|BUSINESS> --player [player]</gray></green> <white>- Create an account for another player</click>");
        if (sender.hasPermission("bank.delete"))
            sendMessage(sender, "<click:suggest_command:/bank delete ><green>/bank delete <gray><account></gray></green> <white>- Delete an account</click>");
        if (sender.hasPermission("bank.instrument.create"))
            sendMessage(sender, "<click:suggest_command:/bank instrument ><green>/bank instrument <gray><account>" + (sender.hasPermission("bank.instrument.create.other") ? " [player]" : "") + "</gray></green> <white>- Create a new instrument</click>");
        if (sender.hasPermission("bank.whois"))
            sendMessage(sender, "<click:suggest_command:/bank whois ><green>/bank whois <gray><account></gray></green> <white>- Get information about an account</click>");
        if (sender.hasPermission("bank.pos.create"))
            sendMessage(sender, "<click:suggest_command:/pos ><green>/pos <gray><account> <price> [description]</gray></green> <white>- Create a new point of sale</click>");
        if (sender.hasPermission("bank.set.balance"))
            sendMessage(sender, "<click:suggest_command:/bank setbalance ><green>/bank setbalance <gray><account> <balance|Infinity></gray></green> <white>- Set an account's balance</click>");
        if (sender.hasPermission("bank.set.name"))
            sendMessage(sender, "<click:suggest_command:/bank setname ><green>/bank setname <gray><account> [name]</gray></green> <white>- Set an account's name</click>");
        if (sender.hasPermission("bank.reload"))
            sendMessage(sender, "<click:suggest_command:/bank reload><green>/bank reload</green> <white>- Reload plugin configuration</click>");
        return sendMessage(sender, "<dark_gray>---</dark_gray>");
    }

    /**
     * Get balance
     * <p>Usage:</p>
     * <ul>
     *  <li>{@code balance} List all accounts. If only one account exists, its balance is displayed. Permission: {@code bank.balance.self}</li>
     *  <li>{@code balance <account>} Display the balance of the specified account.
     *      <p>Permissions:</p>
     *      <ul>
     *          <li>{@code bank.balance.self} Allows seeing balances of own accounts</li>
     *          <li>{@code bank.balance.other} Allows seeing balances of any account</li>
     *      </ul>
     *  </li>
     *  <li>{@code balance --player <player>} List all accounts owned by the specified player. Permission: `bank.balance.other`</li>
     * </ul>
     */
    public static boolean balance(final @NotNull CommandSender sender, final @NotNull String @NotNull [] args, final @NotNull String label) throws NullPointerException {
        if (args.length == 0) {
            if (!sender.hasPermission("bank.balance.self"))
                return sendMessage(sender, BankConfig.MESSAGES_ERRORS_NO_PERMISSION);
            return listAccounts(sender, BankAccounts.getOfflinePlayer(sender));
        }
        else if (args[0].equals("--player")) {
            if (!sender.hasPermission("bank.balance.other"))
                return sendMessage(sender, BankConfig.MESSAGES_ERRORS_NO_PERMISSION);
            if (args.length == 1) return sendUsage(sender, label, "balance --player <player>");
            return listAccounts(sender, BankAccounts.getInstance().getServer().getOfflinePlayer(args[1]));
        }
        else {
            if (!sender.hasPermission("bank.balance.self"))
                return sendMessage(sender, BankConfig.MESSAGES_ERRORS_NO_PERMISSION);
            final @NotNull Optional<@NotNull Account> account = Account.get(args[0]);
            if (account.isEmpty()) return sendMessage(sender, BankConfig.MESSAGES_ERRORS_ACCOUNT_NOT_FOUND);
            else if (!sender.hasPermission("bank.balance.other") && !account.get().owner.getUniqueId()
                    .equals((BankAccounts.getOfflinePlayer(sender)).getUniqueId()))
                return sendMessage(sender, BankConfig.MESSAGES_ERRORS_NOT_ACCOUNT_OWNER);
            else return sendMessage(sender, Account.placeholders(Objects.requireNonNull(BankAccounts.getInstance()
                        .getConfig().getString("messages.balance")), account.get()));
        }
    }

    private static boolean listAccounts(final @NotNull CommandSender sender, final @NotNull OfflinePlayer player) {
        final @NotNull Account @NotNull [] accounts = Account.get(player);
        if (accounts.length == 0) return sendMessage(sender, BankConfig.MESSAGES_ERRORS_NO_ACCOUNTS);
        else if (accounts.length == 1)
            return sendMessage(sender, Account.placeholders(Objects.requireNonNull(BankAccounts.getInstance()
                    .getConfig().getString("messages.balance")), accounts[0]));
        else {
            sendMessage(sender, BankConfig.MESSAGES_LIST_ACCOUNTS_HEADER);
            for (final @NotNull Account account : accounts)
                sendMessage(sender, Account.placeholders(Objects.requireNonNull(BankAccounts.getInstance().getConfig()
                        .getString("messages.list-accounts.entry")), account));
        }
        return true;
    }

    /**
     * Reload plugin configuration
     */
    public static boolean reload(final @NotNull CommandSender sender) {
        if (!sender.hasPermission("bank.reload")) return sendMessage(sender, BankConfig.MESSAGES_ERRORS_NO_PERMISSION);
        BankAccounts.reload();
        return sendMessage(sender, BankConfig.MESSAGES_RELOAD);
    }

    /**
     * Create account
     * create [type] [--player <player>]
     */
    public static boolean create(final @NotNull CommandSender sender, final @NotNull String @NotNull [] args, final @NotNull String label) {
        if (!sender.hasPermission("bank.account.create"))
            return sendMessage(sender, BankConfig.MESSAGES_ERRORS_NO_PERMISSION);
        @NotNull OfflinePlayer target = BankAccounts.getOfflinePlayer(sender);
        if (Arrays.asList(args).contains("--player")) {
            if (!sender.hasPermission("bank.account.create.other"))
                return sendMessage(sender, BankConfig.MESSAGES_ERRORS_NO_PERMISSION);
            else {
                // find value of --player
                int index = Arrays.asList(args).indexOf("--player");
                // index out of bounds
                if (index == -1 || index >= args.length - 1)
                    return sendUsage(sender, label, "create <PERSONAL|BUSINESS> --player <player>");
                target = BankAccounts.getInstance().getServer().getOfflinePlayer(args[index + 1]);
            }
        }
        if (args.length == 0)
            return sendUsage(sender, label, "create <PERSONAL|BUSINESS> " + (sender.hasPermission("bank.account.create.other") ? "[--player <player>]" : ""));
        // check if target is the same as sender
        if (target.getUniqueId().equals(BankAccounts.getOfflinePlayer(sender)
                .getUniqueId()) && !sender.hasPermission("bank.account.create"))
            return sendMessage(sender, BankConfig.MESSAGES_ERRORS_NO_PERMISSION);
        final @NotNull Optional<Account.Type> optionalType = Account.Type.fromString(args[0]);
        if (optionalType.isEmpty())
            return sendUsage(sender, label, "create <PERSONAL|BUSINESS> " + (sender.hasPermission("bank.account.create.other") ? "[--player <player>]" : ""));
        if (!sender.hasPermission("bank.account.create.bypass")) {
            final @NotNull Account @NotNull [] accounts = Account.get(target, optionalType.get());
            int limit = BankAccounts.getInstance().getConfig()
                    .getInt("account-limits." + Account.Type.getType(optionalType.get()));
            if (limit != -1 && accounts.length >= limit)
                return sendMessage(sender, BankConfig.MESSAGES_ERRORS_MAX_ACCOUNTS, Placeholder.unparsed("type", optionalType.get().name), Placeholder.unparsed("limit", String.valueOf(BankAccounts
                        .getInstance().getConfig()
                        .getInt("account-limits." + Account.Type.getType(optionalType.get())))));
        }

        final @NotNull Account account = new Account(target, optionalType.get(), null, BigDecimal.ZERO, false);
        account.insert();

        return sendMessage(sender, Account.placeholders(Objects.requireNonNull(BankAccounts.getInstance().getConfig()
                .getString("messages.account-created")), account));
    }

    /**
     * Set account balance
     * setbal <account> <bal>
     */
    public static boolean setBalance(final @NotNull CommandSender sender, final @NotNull String @NotNull [] args, final @NotNull String label) {
        if (!sender.hasPermission("bank.set.balance"))
            return sendMessage(sender, BankConfig.MESSAGES_ERRORS_NO_PERMISSION);
        if (args.length < 2)
            return sendUsage(sender, label, "setbalance " + (args.length > 0 ? args[0] : "<account>") + " <balance|Infinity>");
        final @NotNull Optional<@NotNull Account> account = Account.get(args[0]);
        if (account.isEmpty()) return sendMessage(sender, BankConfig.MESSAGES_ERRORS_ACCOUNT_NOT_FOUND);
        else {
            final BigDecimal balance;
            try {
                balance = args[1].equalsIgnoreCase("Infinity") ? null : BigDecimal.valueOf(Double.parseDouble(args[1]));
            }
            catch (NumberFormatException e) {
                return sendMessage(sender, BankConfig.MESSAGES_ERRORS_INVALID_NUMBER, Placeholder.unparsed("number", args[1]));
            }
            account.get().balance = balance;
            account.get().update();
            return sendMessage(sender, Account.placeholders(Objects.requireNonNull(BankAccounts.getInstance()
                    .getConfig().getString("messages.balance-set")), account.get()));
        }
    }

    /**
     * Set account name
     */
    public static boolean setName(final @NotNull CommandSender sender, final @NotNull String @NotNull [] args, final @NotNull String label) {
        if (!sender.hasPermission("bank.set.name"))
            return sendMessage(sender, BankConfig.MESSAGES_ERRORS_NO_PERMISSION);
        if (args.length < 2)
            return sendUsage(sender, label, "setname " + (args.length > 0 ? args[0] : "<account>") + " [name]");
        final @NotNull Optional<@NotNull Account> account = Account.get(args[0]);
        if (account.isEmpty()) return sendMessage(sender, BankConfig.MESSAGES_ERRORS_ACCOUNT_NOT_FOUND);
        else {
            if (!sender.hasPermission("bank.set.name.other") && !account.get().owner.getUniqueId()
                    .equals(BankAccounts.getOfflinePlayer(sender).getUniqueId()))
                return sendMessage(sender, BankConfig.MESSAGES_ERRORS_NOT_ACCOUNT_OWNER);
            if (!sender.hasPermission("bank.set.name.personal") && account.get().type == Account.Type.PERSONAL)
                return sendMessage(sender, BankConfig.MESSAGES_ERRORS_RENAME_PERSONAL);
            String name = String.join(" ", Arrays.copyOfRange(args, 1, args.length)).trim();
            name = name.length() > 32 ? name.substring(0, 32) : name;
            name = name.length() == 0 ? null : name;

            if (name != null && (name.contains("<") || name.contains(">")))
                return sendMessage(sender, BankConfig.MESSAGES_ERRORS_DISALLOWED_CHARACTERS, Placeholder.unparsed("characters", "<>"));

            account.get().name = name;
            account.get().update();
            return sendMessage(sender, Account.placeholders(Objects.requireNonNull(BankAccounts.getInstance()
                    .getConfig().getString("messages.name-set")), account.get()));
        }
    }

    /**
     * Delete account
     */
    public static boolean delete(final @NotNull CommandSender sender, final @NotNull String @NotNull [] args, final @NotNull String label) {
        if (!sender.hasPermission("bank.delete")) return sendMessage(sender, BankConfig.MESSAGES_ERRORS_NO_PERMISSION);
        if (args.length < 1) return sendUsage(sender, label, "delete <account>");
        final @NotNull Optional<@NotNull Account> account = Account.get(args[0]);
        if (account.isEmpty()) return sendMessage(sender, BankConfig.MESSAGES_ERRORS_ACCOUNT_NOT_FOUND);
        if (!sender.hasPermission("bank.delete.other") && !account.get().owner.getUniqueId()
                .equals(BankAccounts.getOfflinePlayer(sender).getUniqueId()))
            return sendMessage(sender, BankConfig.MESSAGES_ERRORS_NOT_ACCOUNT_OWNER);
        final @NotNull Optional<@NotNull BigDecimal> balance = Optional.ofNullable(account.get().balance);
        if (balance.isPresent() && balance.get().compareTo(BigDecimal.ZERO) != 0)
            return sendMessage(sender, BankConfig.MESSAGES_ERRORS_CLOSING_BALANCE);
        if (account.get().frozen) return sendMessage(sender, Account.placeholders(Objects.requireNonNull(BankAccounts.getInstance()
                .getConfig().getString(BankConfig.MESSAGES_ERRORS_FROZEN.getKey())), account.get()));
        if (BankAccounts.getInstance().getConfig()
                .getBoolean("prevent-close-last-personal") && account.get().type == Account.Type.PERSONAL && !sender.hasPermission("bank.delete.personal") && Account.get(account.get().owner, Account.Type.PERSONAL).length == 1)
            return sendMessage(sender, BankConfig.MESSAGES_ERRORS_CLOSING_PERSONAL);
        account.get().delete();
        return sendMessage(sender, Account.placeholders(Objects.requireNonNull(BankAccounts.getInstance().getConfig()
                .getString("messages.account-deleted")), account.get()));
    }

    /**
     * Make a transfer to another account
     * <p>
     * {@code transfer [--confirm] <from> <to> <amount> [description]}
     */
    public static boolean transfer(final @NotNull CommandSender sender, final @NotNull String @NotNull [] args, final @NotNull String label) {
        if (!sender.hasPermission("bank.transfer.self") && !sender.hasPermission("bank.transfer.other"))
            return sendMessage(sender, BankConfig.MESSAGES_ERRORS_NO_PERMISSION);
        boolean confirm = args.length > 0 && args[0].equals("--confirm");
        @NotNull String[] argsCopy = args;
        if (confirm) argsCopy = Arrays.copyOfRange(argsCopy, 1, argsCopy.length);
        if (args.length < 3)
            return sendUsage(sender, label, "transfer " + (argsCopy.length > 0 ? argsCopy[0] : "<from>") + " " + (argsCopy.length > 1 ? argsCopy[1] : "<to>") + " <amount> [description]");
        final @NotNull Optional<@NotNull Account> from = Account.get(args[0]);
        // account does not exist
        if (from.isEmpty()) return sendMessage(sender, BankConfig.MESSAGES_ERRORS_ACCOUNT_NOT_FOUND);
        // sender does not own account
        if (!from.get().owner.getUniqueId().equals(BankAccounts.getOfflinePlayer(sender).getUniqueId()))
            return sendMessage(sender, BankConfig.MESSAGES_ERRORS_NOT_ACCOUNT_OWNER);
        // account is frozen
        if (from.get().frozen)
            return sendMessage(sender, Account.placeholders(Objects.requireNonNull(BankAccounts.getInstance()
                    .getConfig().getString(BankConfig.MESSAGES_ERRORS_FROZEN.getKey())), from.get()));
        // recipient does not exist
        final @NotNull Optional<@NotNull Account> to = Account.get(args[1]);
        if (to.isEmpty()) return sendMessage(sender, BankConfig.MESSAGES_ERRORS_ACCOUNT_NOT_FOUND);
        // to is same as from
        if (from.get().id.equals(to.get().id)) return sendMessage(sender, BankConfig.MESSAGES_ERRORS_SAME_FROM_TO);
        // to is frozen
        if (to.get().frozen)
            return sendMessage(sender, Account.placeholders(Objects.requireNonNull(BankAccounts.getInstance()
                    .getConfig().getString(BankConfig.MESSAGES_ERRORS_FROZEN.getKey())), to.get()));
        // to is foreign
        if (!sender.hasPermission("bank.transfer.other") && !to.get().owner.getUniqueId()
                .equals(BankAccounts.getOfflinePlayer(sender).getUniqueId())) {
            return sendMessage(sender, BankConfig.MESSAGES_ERRORS_TRANSFER_SELF_ONLY);
        }
        // to is not foreign
        if (!sender.hasPermission("bank.transfer.self") && to.get().owner.getUniqueId()
                .equals(BankAccounts.getOfflinePlayer(sender).getUniqueId()))
            return sendMessage(sender, BankConfig.MESSAGES_ERRORS_TRANSFER_OTHER_ONLY);

        final @NotNull BigDecimal amount;
        try {
            amount = BigDecimal.valueOf(Double.parseDouble(args[2])).setScale(2, RoundingMode.HALF_UP);
        }
        catch (NumberFormatException e) {
            return sendMessage(sender, BankConfig.MESSAGES_ERRORS_INVALID_NUMBER, Placeholder.unparsed("number", args[2]));
        }
        // amount is 0 or less
        if (amount.compareTo(BigDecimal.ZERO) <= 0)
            return sendMessage(sender, BankConfig.MESSAGES_ERRORS_NEGATIVE_TRANSFER);
        // account has insufficient funds
        if (!from.get().hasFunds(amount))
            return sendMessage(sender, Account.placeholders(Objects.requireNonNull(BankAccounts.getInstance()
                    .getConfig().getString(BankConfig.MESSAGES_ERRORS_INSUFFICIENT_FUNDS.getKey())), from.get()));

        String description = args.length > 3 ? String.join(" ", Arrays.copyOfRange(args, 3, args.length)).trim() : null;
        if (description != null && description.length() > 64) description = description.substring(0, 64);

        if (description != null && (description.contains("<") || description.contains(">")))
            return sendMessage(sender, BankConfig.MESSAGES_ERRORS_DISALLOWED_CHARACTERS, Placeholder.unparsed("characters", "<>"));

        if (!confirm && BankAccounts.getInstance().getConfig().getBoolean("transfer-confirmation.enabled")) {
            final @NotNull BigDecimal minAmount = BigDecimal.valueOf(BankAccounts.getInstance().getConfig()
                    .getDouble(BankConfig.TRANSFER_CONFIRMATION_MIN_AMOUNT.getKey()));
            boolean bypassOwnAccounts = BankAccounts.getInstance().getConfig()
                    .getBoolean(BankConfig.TRANSFER_CONFIRMATION_BYPASS_OWN_ACCOUNTS.getKey());
            if (amount.compareTo(minAmount) >= 0 && (!bypassOwnAccounts || !from.get().owner.getUniqueId()
                    .equals(to.get().owner.getUniqueId()))) {
                return sendMessage(sender, transferConfirmation(from.get(), to.get(), amount, description));
            }
        }

        final @NotNull Transaction transfer = from.get().transfer(to.get(), amount, description, null);
        sendMessage(sender, Transaction.placeholders(transfer, Objects.requireNonNull(BankAccounts.getInstance()
                .getConfig().getString(BankConfig.MESSAGES_TRANSFER_SENT.getKey()))));
        final @NotNull Optional<@NotNull Player> player = Optional.ofNullable(to.get().owner.getPlayer());
        if (player.isPresent() && player.get().isOnline() && !player.get().getUniqueId()
                .equals(BankAccounts.getOfflinePlayer(sender).getUniqueId()))
            sendMessage(player.get(), Transaction.placeholders(transfer, Objects.requireNonNull(BankAccounts
                    .getInstance().getConfig().getString(BankConfig.MESSAGES_TRANSFER_RECEIVED.getKey()))));

        return true;
    }

    /**
     * List transactions
     * <p>
     * {@code transactions <account> [page|--all]}
     */
    public static boolean transactions(final @NotNull CommandSender sender, final @NotNull String @NotNull [] args, final @NotNull String label) {
        if (!sender.hasPermission("bank.history")) return sendMessage(sender, BankConfig.MESSAGES_ERRORS_NO_PERMISSION);
        if (args.length < 1) return sendUsage(sender, label, "transactions <account> [page=1|--all]");
        final @NotNull Optional<@NotNull Account> account = Account.get(args[0]);
        if (account.isEmpty()) return sendMessage(sender, BankConfig.MESSAGES_ERRORS_ACCOUNT_NOT_FOUND);
        if (!sender.hasPermission("bank.history.other") && !account.get().owner.getUniqueId()
                .equals(BankAccounts.getOfflinePlayer(sender).getUniqueId()))
            return sendMessage(sender, BankConfig.MESSAGES_ERRORS_NOT_ACCOUNT_OWNER);
        final int page;
        @NotNull Optional<@NotNull Integer> limit = Optional.of(BankAccounts.getInstance().getConfig()
                .getInt(BankConfig.HISTORY_PER_PAGE.getKey()));
        if (args.length > 1) {
            if (args[1].equals("--all")) {
                page = 1;
                limit = Optional.empty();
            }
            else {
                try {
                    page = Integer.parseInt(args[1]);
                    if (page <= 0) throw new NumberFormatException();
                }
                catch (final @NotNull NumberFormatException e) {
                    return sendMessage(sender, BankConfig.MESSAGES_ERRORS_INVALID_NUMBER, Placeholder.unparsed("number", args[1]));
                }
            }
        }
        else page = 1;
        final @NotNull Transaction @NotNull [] transactions = limit
                .map(integer -> Transaction.get(account.get(), integer, page))
                .orElseGet(() -> Transaction.get(account.get()));
        if (transactions.length == 0) sendMessage(sender, BankConfig.MESSAGES_HISTORY_NO_TRANSACTIONS);
        else {
            final int count = Transaction.count(account.get());
            final int maxPage = (int) Math.ceil((double) count / limit.orElse(count));
            transactionsHeaderFooter(sender, account.get(), page, maxPage, Objects.requireNonNull(BankAccounts
                    .getInstance().getConfig().getString(BankConfig.MESSAGES_HISTORY_HEADER.getKey())));
            for (final @NotNull Transaction transaction : transactions)
                sendMessage(sender, Transaction.historyPlaceholders(transaction, account.get(), Objects.requireNonNull(BankAccounts
                        .getInstance().getConfig().getString(BankConfig.MESSAGES_HISTORY_ENTRY.getKey()))));
            transactionsHeaderFooter(sender, account.get(), page, maxPage, Objects.requireNonNull(BankAccounts
                    .getInstance().getConfig().getString(BankConfig.MESSAGES_HISTORY_FOOTER.getKey())));
        }
        return true;
    }

    /**
     * Create instrument
     */
    public static boolean instrument(final @NotNull CommandSender sender, final @NotNull String @NotNull [] args, final @NotNull String label) {
        if (!sender.hasPermission("bank.instrument.create"))
            return sendMessage(sender, BankConfig.MESSAGES_ERRORS_NO_PERMISSION);
        if (!(sender instanceof Player)) {
            if (!sender.hasPermission("bank.instrument.create.other"))
                return sendMessage(sender, BankConfig.MESSAGES_ERRORS_PLAYER_ONLY);
            else if (args.length < 2)
                return sendUsage(sender, label, "instrument " + (args.length > 0 ? args[0] : "<account>") + " <player>");
        }
        else if (args.length < 1)
            return sendUsage(sender, label, "instrument <account>" + (sender.hasPermission("bank.instrument.create.other") ? " <player>" : ""));
        final Player target = !(sender instanceof final @NotNull Player player) || (sender.hasPermission("bank.instrument.create.other") && args.length >= 2) ? BankAccounts
                .getInstance().getServer().getPlayer(args[1]) : player;
        if (target == null || !target.isOnline())
            return sendMessage(sender, BankConfig.MESSAGES_ERRORS_PLAYER_NOT_FOUND);
        final @NotNull Optional<@NotNull Account> account = Account.get(args[0]);
        if (account.isEmpty()) return sendMessage(sender, BankConfig.MESSAGES_ERRORS_ACCOUNT_NOT_FOUND);
        if (!sender.hasPermission("bank.instrument.create.other") && !account.get().owner.getUniqueId()
                .equals(BankAccounts.getOfflinePlayer(sender).getUniqueId()))
            return sendMessage(sender, BankConfig.MESSAGES_ERRORS_NOT_ACCOUNT_OWNER);

        if (target.getInventory().firstEmpty() == -1)
            return sendMessage(sender, BankConfig.MESSAGES_ERRORS_TARGET_INVENTORY_FULL, Placeholder.unparsed("player", target.getName()));

        if (BankAccounts.getInstance().getConfig()
                .getBoolean(BankConfig.INSTRUMENTS_REQUIRE_ITEM.getKey()) && sender instanceof final @NotNull Player player) {
            final @NotNull Material material = Objects.requireNonNull(Material.getMaterial(Objects.requireNonNull(BankAccounts
                    .getInstance().getConfig().getString(BankConfig.INSTRUMENTS_MATERIAL.getKey()))));
            final ItemStack item = Arrays.stream(player.getInventory().getStorageContents())
                    .filter(itemStack -> itemStack != null && itemStack.getType() == material && !itemStack.hasItemMeta())
                    .findFirst().orElse(null);

            if (!sender.hasPermission("bank.instrument.create.bypass")) {
                if (item == null)
                    return sendMessage(sender, BankConfig.MESSAGES_ERRORS_INSTRUMENT_REQUIRES_ITEM, Placeholder.unparsed("material", Objects
                            .requireNonNull(BankAccounts.getInstance().getConfig()
                                    .getString(BankConfig.INSTRUMENTS_MATERIAL.getKey())).toLowerCase()));
                else {
                    final @NotNull ItemStack clone = item.clone();
                    clone.setAmount(1);
                    player.getInventory().removeItem(clone);
                }
            }
        }

        target.getInventory().addItem(account.get().createInstrument());
        return sendMessage(sender, BankConfig.MESSAGES_INSTRUMENT_CREATED);
    }

    /**
     * Account whois
     */
    public static boolean whois(final @NotNull CommandSender sender, final @NotNull String @NotNull [] args, final @NotNull String label) {
        if (!sender.hasPermission("bank.whois")) return sendMessage(sender, BankConfig.MESSAGES_ERRORS_NO_PERMISSION);
        if (args.length < 1) return sendUsage(sender, label, "whois <account>");
        final @NotNull Optional<@NotNull Account> account = Account.get(args[0]);
        return account
                .map(value -> sendMessage(sender, Account.placeholders(Objects.requireNonNull(BankAccounts.getInstance()
                        .getConfig()
                        .getString(BankConfig.MESSAGES_WHOIS.getKey())), value)))
                .orElseGet(() -> sendMessage(sender, BankConfig.MESSAGES_ERRORS_ACCOUNT_NOT_FOUND));
    }

    /**
     * Send transaction history header or footer
     *
     * @param sender  Command sender
     * @param account Account
     * @param page    Current page
     * @param maxPage Maximum page
     * @param message Message to replace placeholders in
     */
    public static void transactionsHeaderFooter(@NotNull CommandSender sender, @NotNull Account account, int page, int maxPage, @NotNull String message) {
        message = message.replace("<page>", String.valueOf(page)).replace("<max-page>", String.valueOf(maxPage))
                .replace("<cmd-prev>", "/bank transactions " + account.id + " " + (page - 1))
                .replace("<cmd-next>", "/bank transactions " + account.id + " " + (page + 1));
        sender.sendMessage(Account.placeholders(message, account));
    }

    /**
     * Transfer confirmation message
     * <ul>
     *    <li>{@code <amount>} Transfer amount without formatting, example: 123456.78</li>
     *    <li>{@code <amount-formatted>} Transfer amount with formatting, example: 123,456.78</li>
     *    <li>{@code <amount-short>} Transfer amount with formatting, example: 123k</li>
     *    <li>{@code <description>} Transfer description</li>
     *    <li>{@code <confirm-command>} Command to run to confirm transfer</li>
     * </ul>
     *
     * @param from        Account sending from
     * @param to          Account sending to
     * @param amount      Amount of transfer
     * @param description Description of transfer
     */
    public static Component transferConfirmation(@NotNull Account from, @NotNull Account to, @NotNull BigDecimal amount, @Nullable String description) {
        String message = Objects
                .requireNonNull(BankAccounts.getInstance().getConfig().getString("messages.confirm-transfer"))
                .replace("<amount>", amount.toPlainString())
                .replace("<amount-formatted>", BankAccounts.formatCurrency(amount))
                .replace("<amount-short>", BankAccounts.formatCurrencyShort(amount))
                .replace("<description>", description == null ? "<gray><i>no description</i></gray>" : description)
                .replace("<confirm-command>", "/bank transfer --confirm " + from.id + " " + to.id + " " + amount.toPlainString() + (description == null ? "" : " " + description));
        return Account.placeholders(message, new HashMap<>() {{
            put("from", from);
            put("to", to);
        }});
    }
}
