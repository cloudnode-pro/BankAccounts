package pro.cloudnode.smp.bankaccounts.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pro.cloudnode.smp.bankaccounts.Account;
import pro.cloudnode.smp.bankaccounts.BankAccounts;
import pro.cloudnode.smp.bankaccounts.Transaction;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TimeZone;
import java.util.UUID;

public class BankCommand implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!sender.hasPermission("bank.command")) sender.sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("messages.errors.no-permission"))));
        else run(sender, label, args);
        return true;
    }

    @Override
    public ArrayList<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        ArrayList<String> suggestions = new ArrayList<>();
        if (!sender.hasPermission("bank.command")) return suggestions;
        if (args.length == 1) {
            suggestions.add("help");
            if (sender.hasPermission("bank.balance.self")) suggestions.addAll(Arrays.asList("balance", "bal", "account", "accounts"));
            if (sender.hasPermission("bank.reload")) suggestions.add("reload");
            if (sender.hasPermission("bank.account.create")) suggestions.addAll(Arrays.asList("create", "new"));
            if (sender.hasPermission("bank.set.balance")) suggestions.addAll(Arrays.asList("setbal", "setbalance"));
            if (sender.hasPermission("bank.set.name")) suggestions.addAll(Arrays.asList("setname", "rename"));
            if (sender.hasPermission("bank.delete")) suggestions.add("delete");
            if (sender.hasPermission("bank.transfer.self") || sender.hasPermission("bank.transfer.other")) suggestions.addAll(Arrays.asList("transfer", "send", "pay"));
            if (sender.hasPermission("bank.history")) suggestions.addAll(Arrays.asList("transactions", "history"));
        }
        else {
            switch (args[0]) {
                case "balance", "bal", "account", "accounts" -> {
                    if (!sender.hasPermission("bank.balance.self") && !sender.hasPermission("bank.balance.other")) return suggestions;
                    if (args.length == 2) {
                        if (sender.hasPermission("bank.balance.other")) suggestions.add("--player");
                        if (sender instanceof OfflinePlayer player) {
                            Account[] accounts = Account.get(player);
                            for (Account account : accounts) suggestions.add(account.id);
                        }
                        else {
                            Account[] accounts = Account.get();
                            for (Account account : accounts) suggestions.add(account.id);
                        }
                    }
                    else if (args.length == 3 && args[1].equals("--player") && sender.hasPermission("bank.balance.other")) {
                        Account[] accounts = Account.get();
                        for (Account account : accounts) if (account.owner.getName() != null) suggestions.add(account.owner.getName());
                    }
                }
                case "create", "new" -> {
                    if (!sender.hasPermission("bank.account.create")) return suggestions;
                    if (args.length == 2) {
                        suggestions.addAll(Arrays.asList("PERSONAL", "BUSINESS"));
                    }
                    else if (args.length == 3 && sender.hasPermission("bank.account.create.other"))
                        suggestions.add("--player");
                    else if (args.length == 4 && args[2].equals("--player") && sender.hasPermission("bank.account.create.other")) {
                        Account[] accounts = Account.get();
                        for (Account account : accounts)
                            if (account.owner.getName() != null) suggestions.add(account.owner.getName());
                    }
                }
                case "setbal", "setbalance" -> {
                    if (!sender.hasPermission("bank.set.balance")) return suggestions;
                    if (args.length == 2) {
                        Account[] accounts = Account.get();
                        for (Account account : accounts) suggestions.add(account.id);
                    }
                    if (args.length == 3) suggestions.add("Infinity");
                }
                case "setname", "rename" -> {
                    if (!sender.hasPermission("bank.set.name")) return suggestions;
                    if (args.length == 2) {
                        Account[] accounts = sender.hasPermission("bank.set.name.other") ? Account.get() : Account.get(BankAccounts.getOfflinePlayer(sender));
                        for (Account account : accounts) suggestions.add(account.id);
                    }
                }
                case "delete" -> {
                    if (!sender.hasPermission("bank.delete")) return suggestions;
                    if (args.length == 2) {
                        Account[] accounts = sender.hasPermission("bank.delete.other") ? Account.get() : Account.get(BankAccounts.getOfflinePlayer(sender));
                        if (!sender.hasPermission("bank.delete.person")) accounts = Arrays.stream(accounts).filter(account -> account.type != Account.Type.PERSONAL).toArray(Account[]::new);
                        for (Account account : accounts) suggestions.add(account.id);
                    }
                }
                case "transfer", "send", "pay" -> {
                    if (!sender.hasPermission("bank.transfer.self") && !sender.hasPermission("bank.transfer.other")) return suggestions;
                    if (args.length == 2) {
                        Account[] accounts = Account.get(BankAccounts.getOfflinePlayer(sender));
                        for (Account account : accounts) suggestions.add(account.id);
                    }
                    else if (args.length == 3) {
                        Account[] accounts = sender.hasPermission("bank.transfer.other") ? Account.get() : Account.get(BankAccounts.getOfflinePlayer(sender));
                        accounts = Arrays.stream(accounts).filter(account -> !account.id.equals(args[1])).toArray(Account[]::new);
                        for (Account account : accounts) suggestions.add(account.id);
                    }
                }
                case "transactions", "history" -> {
                    if (!sender.hasPermission("bank.history")) return suggestions;
                    if (args.length == 2) {
                        Account[] accounts = sender.hasPermission("bank.history.other") ? Account.get() : Account.get(BankAccounts.getOfflinePlayer(sender));
                        for (Account account : accounts) suggestions.add(account.id);
                    }
                }
            }
        }
        return suggestions;
    }

    /**
     * Decide which command to run
     */
    public static void run(@NotNull CommandSender sender, @NotNull String label, String[] args) {
        if (args.length == 0) overview(sender);
        else switch (args[0]) {
            case "help" -> help(sender, label);
            case "bal", "balance", "account", "accounts" -> balance(sender, Arrays.copyOfRange(args, 1, args.length), label);
            case "reload" -> reload(sender);
            case "create", "new" -> create(sender, Arrays.copyOfRange(args, 1, args.length), label);
            case "setbal", "setbalance" -> setBalance(sender, Arrays.copyOfRange(args, 1, args.length), label);
            case "setname", "rename" -> setName(sender, Arrays.copyOfRange(args, 1, args.length), label);
            case "delete" -> delete(sender, Arrays.copyOfRange(args, 1, args.length), label);
            case "transfer", "send", "pay" -> transfer(sender, Arrays.copyOfRange(args, 1, args.length), label);
            case "transactions", "history" -> transactions(sender, Arrays.copyOfRange(args, 1, args.length), label);
            default -> sender.sendMessage(MiniMessage.miniMessage().deserialize(BankAccounts.getInstance().getConfig().getString("messages.errors.unknown-command")));
        }
    }

    /**
     * Plugin overview
     */
    public static void overview(@NotNull CommandSender sender) {
        BankAccounts plugin = BankAccounts.getInstance();
        sender.sendMessage(MiniMessage.miniMessage().deserialize("<green><name></green> <white>v<version> by</white> <gray><author></gray>",
                Placeholder.unparsed("name", plugin.getPluginMeta().getName()),
                Placeholder.unparsed("version", plugin.getPluginMeta().getVersion()),
                Placeholder.unparsed("author", String.join(", ", plugin.getPluginMeta().getAuthors()))));
    }

    /**
     * Plugin help
     */
    public static void help(@NotNull CommandSender sender, @NotNull String label) {
        sender.sendMessage(MiniMessage.miniMessage().deserialize("<dark_gray>---</dark_gray>"));
        sender.sendMessage(MiniMessage.miniMessage().deserialize("<green>Available commands:"));
        sender.sendMessage(MiniMessage.miniMessage().deserialize(""));
        if (sender.hasPermission("bank.balance.self")) sender.sendMessage(MiniMessage.miniMessage().deserialize("<click:suggest_command:/bank balance ><green>/bank balance <gray>[account]</gray></green> <white>- Check your accounts</click>"));
        if (sender.hasPermission("bank.balance.other")) sender.sendMessage(MiniMessage.miniMessage().deserialize("<click:suggest_command:/bank balance --player ><green>/bank balance <gray>--player [player]</gray></green> <white>- List another player's accounts</click>"));
        if (sender.hasPermission("bank.transfer.self") || sender.hasPermission("bank.transfer.other")) sender.sendMessage(MiniMessage.miniMessage().deserialize("<click:suggest_command:/bank transfer ><green>/bank transfer <gray><from> <to> <amount> [description]</gray></green> <white>- Transfer money to another account</click>"));
        if (sender.hasPermission("bank.history")) sender.sendMessage(MiniMessage.miniMessage().deserialize("<click:suggest_command:/bank transactions ><green>/bank transactions <gray>[account] [page=1]</gray></green> <white>- List transactions</click>"));
        if (sender.hasPermission("bank.account.create")) sender.sendMessage(MiniMessage.miniMessage().deserialize("<click:suggest_command:/bank create ><green>/bank create <gray>[PERSONAL|BUSINESS]</gray></green> <white>- Create a new account</click>"));
        if (sender.hasPermission("bank.account.create.other")) sender.sendMessage(MiniMessage.miniMessage().deserialize("<click:suggest_command:/bank create --player ><green>/bank create <gray>[PERSONAL|BUSINESS] --player [player]</gray></green> <white>- Create an account for another player</click>"));
        if (sender.hasPermission("bank.delete")) sender.sendMessage(MiniMessage.miniMessage().deserialize("<click:suggest_command:/bank delete ><green>/bank delete <gray>[account]</gray></green> <white>- Delete an account</click>"));
        if (sender.hasPermission("bank.set.balance")) sender.sendMessage(MiniMessage.miniMessage().deserialize("<click:suggest_command:/bank setbalance ><green>/bank setbalance <gray>[account] [balance]</gray></green> <white>- Set an account's balance</click>"));
        if (sender.hasPermission("bank.set.name")) sender.sendMessage(MiniMessage.miniMessage().deserialize("<click:suggest_command:/bank setname ><green>/bank setname <gray>[account] [name]</gray></green> <white>- Set an account's name</click>"));
        if (sender.hasPermission("bank.reload")) sender.sendMessage(MiniMessage.miniMessage().deserialize("<click:suggest_command:/bank reload><green>/bank reload</green> <white>- Reload plugin configuration</click>"));
        sender.sendMessage(MiniMessage.miniMessage().deserialize("<dark_gray>---</dark_gray>"));
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
    public static void balance(@NotNull CommandSender sender, String[] args, String label) throws NullPointerException {
        if (args.length == 0) {
            if (!sender.hasPermission("bank.balance.self")) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("messages.errors.no-permission"))));
                return;
            }
            @NotNull OfflinePlayer player = BankAccounts.getOfflinePlayer(sender);
            listAccounts(sender, player);
        }
        else switch (args[0]) {
            case "--player" -> {
                if (!sender.hasPermission("bank.balance.other")) {
                    sender.sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("messages.errors.no-permission"))));
                    return;
                }
                if (args.length == 1) {
                    sender.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>(!) Usage: <white>/<command> balance --player <player>",
                            Placeholder.unparsed("command", label)
                    ));
                    return;
                }
                @NotNull OfflinePlayer player = BankAccounts.getInstance().getServer().getOfflinePlayer(args[1]);
                listAccounts(sender, player);
            }
            default -> {
                if (!sender.hasPermission("bank.balance.self")) {
                    sender.sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("messages.errors.no-permission"))));
                    return;
                }
                Optional<Account> account = Account.get(args[0]);
                if (account.isEmpty()) sender.sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("messages.errors.account-not-found"))));
                else if (!sender.hasPermission("bank.balance.other") && !account.get().owner.getUniqueId().equals((BankAccounts.getOfflinePlayer(sender)).getUniqueId())) {
                    sender.sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("messages.errors.not-account-owner"))));
                    return;
                }
                else sender.sendMessage(accountPlaceholders(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("messages.balance")), account.get()));
            }
        }
    }

    private static void listAccounts(@NotNull CommandSender sender, @NotNull OfflinePlayer player) {
        Account[] accounts = Account.get(player);
        if (accounts.length == 0) sender.sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("messages.errors.no-accounts"))));
        else if (accounts.length == 1) sender.sendMessage(accountPlaceholders(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("messages.balance")), accounts[0]));
        else {
            sender.sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("messages.list-accounts.header"))));
            for (Account account : accounts) sender.sendMessage(accountPlaceholders(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("messages.list-accounts.entry")), account));
        }
    }

    /**
     * Reload plugin configuration
     */
    public static void reload(@NotNull CommandSender sender) {
        if (!sender.hasPermission("bank.reload")) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("messages.errors.no-permission"))));
            return;
        }
        BankAccounts.getInstance().reloadConfig();
        sender.sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("messages.reload"))));
    }

    /**
     * Create account
     * create [type] [--player <player>]
     */
    public static void create(@NotNull CommandSender sender, String[] args, String label) {
        @NotNull OfflinePlayer target = BankAccounts.getOfflinePlayer(sender);
        if (Arrays.asList(args).contains("--player")) {
            if (!sender.hasPermission("bank.account.create.other")) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("messages.errors.no-permission"))));
                return;
            }
            else {
                // find value of --player
                int index = Arrays.asList(args).indexOf("--player");
                // index out of bounds
                if (index == -1 || index >= args.length - 1) {
                    sender.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>(!) Usage: <white>/<command> create [PERSONAL|BUSINESS] --player <player>",
                            Placeholder.unparsed("command", label)
                    ));
                    return;
                }
                target = BankAccounts.getInstance().getServer().getOfflinePlayer(args[index + 1]);
            }
        }
        // check if target is the same as sender
        if (target.getUniqueId().equals(BankAccounts.getOfflinePlayer(sender).getUniqueId()) && !sender.hasPermission("bank.account.create")) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("messages.errors.no-permission"))));
            return;
        }
        @NotNull Account.Type type = args.length == 0 ? Account.Type.PERSONAL : Account.Type.fromString(args[0]).orElse(Account.Type.PERSONAL);
        if (!sender.hasPermission("bank.account.create.bypass")) {
            Account[] accounts = Account.get(target, type);
            int limit = BankAccounts.getInstance().getConfig().getInt("account-limits." + Account.Type.getType(type));
            if (limit != -1 && accounts.length >= limit) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("messages.errors.max-accounts")),
                        Placeholder.unparsed("type", type.name),
                        Placeholder.unparsed("limit", String.valueOf(BankAccounts.getInstance().getConfig().getInt("account-limits." + Account.Type.getType(type))))
                ));
                return;
            }
        }

        Account account = new Account(target, type, null, BigDecimal.ZERO, false);
        account.save();

        sender.sendMessage(accountPlaceholders(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("messages.account-created")), account));
    }

    /**
     * Set account balance
     * setbal <account> <bal>
     */
    public static void setBalance(@NotNull CommandSender sender, String[] args, String label) {
        if (!sender.hasPermission("bank.set.balance")) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("messages.errors.no-permission"))));
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>(!) Usage: <white>/<command> setbalance " + (args.length > 0 ? args[0] : "<account>") + " <bal>",
                    Placeholder.unparsed("command", label)
            ));
            return;
        }
        Optional<Account> account = Account.get(args[0]);
        if (account.isEmpty()) sender.sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("messages.errors.account-not-found"))));
        else {
            // acceptable values: -1.99, 0, 12345.6, Infinity (= null)
            BigDecimal balance;
            try {
                balance = args[1].equalsIgnoreCase("Infinity") ? null : BigDecimal.valueOf(Double.parseDouble(args[1]));
            }
            catch (NumberFormatException e) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("messages.errors.invalid-number")),
                        Placeholder.unparsed("number", args[1])
                ));
                return;
            }
            account.get().balance = balance;
            account.get().save();
            sender.sendMessage(accountPlaceholders(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("messages.balance-set")), account.get()));
        }
    }

    /**
     * Set account name
     */
    public static void setName(@NotNull CommandSender sender, String[] args, String label) {
        if (!sender.hasPermission("bank.set.name")) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("messages.errors.no-permission"))));
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>(!) Usage: <white>/<command> setname " + (args.length > 0 ? args[0] : "<account>") + " <name>",
                    Placeholder.unparsed("command", label)
            ));
            return;
        }
        Optional<Account> account = Account.get(args[0]);
        if (account.isEmpty())
            sender.sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("messages.errors.account-not-found"))));
        else {
            if (!sender.hasPermission("bank.set.name.other") && !account.get().owner.getUniqueId().equals(BankAccounts.getOfflinePlayer(sender).getUniqueId())) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("messages.errors.not-account-owner"))));
                return;
            }
            if (!sender.hasPermission("bank.set.name.personal") && account.get().type == Account.Type.PERSONAL) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("messages.errors.rename-personal"))));
                return;
            }
            String name = String.join(" ", Arrays.copyOfRange(args, 1, args.length)).trim();
            name = name.length() > 32 ? name.substring(0, 32) : name;
            name = name.length() == 0 ? null : name;
            account.get().name = name;
            account.get().save();
            sender.sendMessage(accountPlaceholders(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("messages.name-set")), account.get()));
        }
    }

    /**
     * Delete account
     */
    public static void delete(@NotNull CommandSender sender, String[] args, String label) {
        if (!sender.hasPermission("bank.delete")) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("messages.errors.no-permission"))));
            return;
        }
        if (args.length < 1) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>(!) Usage: <white>/<command> delete <account>",
                    Placeholder.unparsed("command", label)
            ));
            return;
        }
        Optional<Account> account = Account.get(args[0]);
        if (account.isEmpty())
            sender.sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("messages.errors.account-not-found"))));
        else {
            if (!sender.hasPermission("bank.delete.other") && !account.get().owner.getUniqueId().equals(BankAccounts.getOfflinePlayer(sender).getUniqueId())) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("messages.errors.not-account-owner"))));
                return;
            }
            if (account.get().balance != null && account.get().balance.compareTo(BigDecimal.ZERO) != 0) {
                sender.sendMessage(accountPlaceholders(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("messages.errors.closing-balance")), account.get()));
                return;
            }
            if (account.get().frozen) {
                sender.sendMessage(accountPlaceholders(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("messages.errors.frozen")), account.get()));
                return;
            }
            if (BankAccounts.getInstance().getConfig().getBoolean("prevent-close-personal") && account.get().type == Account.Type.PERSONAL && !sender.hasPermission("bank.delete.personal")) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("messages.errors.closing-personal"))));
                return;
            }
            account.get().delete();
            sender.sendMessage(accountPlaceholders(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("messages.account-deleted")), account.get()));
        }
    }

    /**
     * Make a transfer to another account
     * <p>
     * {@code transfer [--confirm] <from> <to> <amount> [description]}
     */
    public static void transfer(@NotNull CommandSender sender, String[] args, String label) {
        if (!sender.hasPermission("bank.transfer.self") && !sender.hasPermission("bank.transfer.other")) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("messages.errors.no-permission"))));
            return;
        }
        boolean confirm = args.length > 0 && args[0].equals("--confirm");
        if (confirm) args = Arrays.copyOfRange(args, 1, args.length);
        if (args.length < 3) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>(!) Usage: <white>/<command> transfer " + (args.length > 0 ? args[0] : "<from>") + " " + (args.length > 1 ? args[1] : "<to>") + " <amount> [description]",
                    Placeholder.unparsed("command", label)
            ));
            return;
        }
        Optional<Account> from = Account.get(args[0]);
        // account does not exist
        if (from.isEmpty()) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("messages.errors.account-not-found"))));
            return;
        }
        // sender does not own account
        if (!from.get().owner.getUniqueId().equals(BankAccounts.getOfflinePlayer(sender).getUniqueId())) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("messages.errors.not-account-owner"))));
            return;
        }
        // account is frozen
        if (from.get().frozen) {
            sender.sendMessage(accountPlaceholders(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("messages.errors.frozen")), from.get()));
            return;
        }
        // recipient does not exist
        Optional<Account> to = Account.get(args[1]);
        if (to.isEmpty()) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("messages.errors.account-not-found"))));
            return;
        }
        // to is same as from
        if (from.get().id.equals(to.get().id)) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("messages.errors.same-from-to"))));
            return;
        }
        // to is frozen
        if (to.get().frozen) {
            sender.sendMessage(accountPlaceholders(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("messages.errors.frozen")), to.get()));
            return;
        }
        // to is foreign
        if (!sender.hasPermission("bank.transfer.other") && !to.get().owner.getUniqueId().equals(BankAccounts.getOfflinePlayer(sender).getUniqueId())) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("messages.errors.transfer-self-only"))));
            return;
        }
        // to is not foreign
        if (!sender.hasPermission("bank.transfer.self") && to.get().owner.getUniqueId().equals(BankAccounts.getOfflinePlayer(sender).getUniqueId())) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("messages.errors.transfer-other-only"))));
            return;
        }

        @NotNull BigDecimal amount;
        try {
            amount = BigDecimal.valueOf(Double.parseDouble(args[2])).setScale(2, RoundingMode.HALF_UP);
        }
        catch (NumberFormatException e) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("messages.errors.invalid-number")),
                    Placeholder.unparsed("number", args[2])
            ));
            return;
        }
        // amount is 0 or less
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("messages.errors.negative-transfer"))));
            return;
        }
        // account has insufficient funds
        if (!from.get().hasFunds(amount)) {
            sender.sendMessage(accountPlaceholders(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("messages.errors.insufficient-funds")), from.get()));
            return;
        }

        String description = args.length > 3 ? String.join(" ", Arrays.copyOfRange(args, 3, args.length)).trim() : null;
        if (description != null && description.length() > 64) description = description.substring(0, 64);

        if (!confirm && BankAccounts.getInstance().getConfig().getBoolean("transfer-confirmation.enabled")) {
            // show confirmation if amount is above this
            BigDecimal minAmount = BigDecimal.valueOf(BankAccounts.getInstance().getConfig().getDouble("transfer-confirmation.min-amount"));
            // show confirmation if accounts have different owners or if bypassOwnAccounts is false
            boolean bypassOwnAccounts = BankAccounts.getInstance().getConfig().getBoolean("transfer-confirmation.bypass-own-accounts");
            if (amount.compareTo(minAmount) < 0 || (bypassOwnAccounts && from.get().owner.getUniqueId().equals(to.get().owner.getUniqueId()))) {
                // no confirmation needed
            }
            else {
                sender.sendMessage(transferConfirmation(from.get(), to.get(), amount, description));
                return;
            }
        }

        Transaction transfer = from.get().transfer(to.get(), amount, description, null);
        sender.sendMessage(transferSuccess(transfer, from.get(), to.get(), amount, description, Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("messages.transfer-sent"))));
        // if owner of receiving account is online and is not the sender, send them a message
        Player player = to.get().owner.getPlayer();
        if (player != null && player.isOnline() && !player.getUniqueId().equals(BankAccounts.getOfflinePlayer(sender).getUniqueId()))
            player.sendMessage(transferSuccess(transfer, from.get(), to.get(), amount, description, Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("messages.transfer-received"))));
    }

    /**
     * List transactions
     * <p>
     * {@code transactions <account> [page|--all]}
     */
    public static void transactions(@NotNull CommandSender sender, String[] args, String label) {
        if (!sender.hasPermission("bank.history")) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("messages.errors.no-permission"))));
            return;
        }
        if (args.length < 1) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>(!) Usage: <white>/<command> transactions <account> [page|--all]",
                    Placeholder.unparsed("command", label)
            ));
            return;
        }
        Optional<Account> account = Account.get(args[0]);
        if (account.isEmpty()) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("messages.errors.account-not-found"))));
            return;
        }
        if (!sender.hasPermission("bank.history.other") && !account.get().owner.getUniqueId().equals(BankAccounts.getOfflinePlayer(sender).getUniqueId())) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("messages.errors.not-account-owner"))));
            return;
        }
        @NotNull Optional<Integer> page;
        if (args.length > 1) {
            if (args[1].equals("--all")) page = Optional.empty();
            else {
                try {
                    page = Optional.of(Integer.parseInt(args[1]));
                    if (page.get() <= 0) throw new NumberFormatException();
                }
                catch (NumberFormatException e) {
                    sender.sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("messages.errors.invalid-number")),
                            Placeholder.unparsed("number", args[1])
                    ));
                    return;
                }
            }
        }
        else page = Optional.of(1);

        int limit = BankAccounts.getInstance().getConfig().getInt("history.per-page");
        int maxPage = (int) Math.ceil((double) Transaction.count(account.get()) / limit);
        if (page.isPresent() && page.get() > maxPage) page = Optional.of(maxPage);
        Transaction[] transactions = page.map(integer -> Transaction.get(account.get(), limit, integer)).orElseGet(() -> Transaction.get(account.get()));
        if (transactions.length == 0) sender.sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("messages.history.no-transactions"))));
        else {
            transactionsHeaderFooter(sender, account.get(), page.orElse(1), maxPage, Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("messages.history.header")));
            for (Transaction transaction : transactions) sender.sendMessage(transactionPlaceholders(sender, transaction, account.get(), Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("messages.history.entry"))));
            transactionsHeaderFooter(sender, account.get(), page.orElse(1), maxPage, Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("messages.history.footer")));
        }
    }

    /**
     * Send transaction history header or footer
     * @param sender Command sender
     * @param account Account
     * @param page Current page
     * @param maxPage Maximum page
     * @param message Message to replace placeholders in
     */
    public static void transactionsHeaderFooter(@NotNull CommandSender sender, @NotNull Account account, int page, int maxPage, @NotNull String message) {
        message = message
                .replace("<page>", String.valueOf(page))
                .replace("<max-page>", String.valueOf(maxPage))
                .replace("<cmd-prev>", "/bank transactions " + account.id + " " + (page - 1))
                .replace("<cmd-next>", "/bank transactions " + account.id + " " + (page + 1));
        sender.sendMessage(accountPlaceholders(message, account));
    }

    /**
     * Transaction placeholders
     * @param sender Command sender
     * @param transaction Transaction
     * @param account Account
     * @param message Message to replace placeholders in
     */
    public static Component transactionPlaceholders(@NotNull CommandSender sender, @NotNull Transaction transaction, @NotNull Account account, @NotNull String message) {
        final SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        message = message
                .replace("<amount>", transaction.amount.toPlainString())
                .replace("<amount-formatted>", BankAccounts.formatCurrency(transaction.amount))
                .replace("<amount-short>", BankAccounts.formatCurrencyShort(transaction.amount))
                .replace("<description>", transaction.description == null ? "<gray><i>no description</i></gray>" : transaction.description)
                .replace("<transaction-id>", String.valueOf(transaction.getId()))
                .replace("<full_date>", sdf.format(transaction.time) + " UTC");
        Account other = transaction.getOther(account);
        message = accountPlaceholdersString(message, new HashMap<>() {{
            put("", account);
            put("other", other);
        }});
        return MiniMessage.miniMessage().deserialize(message, Formatter.date("date", transaction.time.toInstant().atZone(ZoneOffset.UTC).toLocalDateTime()));
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
     * @param from Account sending from
     * @param to Account sending to
     * @param amount Amount of transfer
     * @param description Description of transfer
     */
    public static Component transferConfirmation(@NotNull Account from, @NotNull Account to, @NotNull BigDecimal amount, @Nullable String description) {
        String message = Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("messages.confirm-transfer"))
                .replace("<amount>", amount.toPlainString())
                .replace("<amount-formatted>", BankAccounts.formatCurrency(amount))
                .replace("<amount-short>", BankAccounts.formatCurrencyShort(amount))
                .replace("<description>", description == null ? "<gray><i>no description</i></gray>" : description)
                .replace("<confirm-command>", "/bank transfer --confirm " + from.id + " " + to.id + " " + amount.toPlainString() + (description == null ? "" : " " + description));
        return accountPlaceholders(message, new HashMap<>() {{
            put("from", from);
            put("to", to);
        }});
    }

    /**
     * Transfer success message
     * <ul>
     *     <li>{@code <transaction-id>} Transaction ID</li>
     *     <li>{@code <amount>} Transfer amount without formatting, example: 123456.78</li>
     *     <li>{@code <amount-formatted>} Transfer amount with formatting, example: 123,456.78</li>
     *     <li>{@code <amount-short>} Transfer amount with formatting, example: 123k</li>
     *     <li>{@code <description>} Transfer description</li>
     * </ul>
     * @param transaction Transaction
     * @param from Account sending from
     * @param to Account sending to
     * @param amount Amount of transfer
     * @param description Description of transfer
     * @param message Message to replace placeholders in
     */
    public static Component transferSuccess(@NotNull Transaction transaction, @NotNull Account from, @NotNull Account to, @NotNull BigDecimal amount, @Nullable String description, @NotNull String message) {
        return accountPlaceholders(message
                        .replace("<transaction-id>", String.valueOf(transaction.getId()))
                        .replace("<amount>", amount.toPlainString())
                        .replace("<amount-formatted>", BankAccounts.formatCurrency(amount))
                        .replace("<amount-short>", BankAccounts.formatCurrencyShort(amount))
                        .replace("<description>", description == null ? "<gray><i>no description</i></gray>" : description),
                new HashMap<>() {{
                    put("from", from);
                    put("to", to);
                }});
    }

    /**
     * Account placeholders
     * @param string String to deserialize with MiniMessage and apply placeholders to
     * @param account Account to apply placeholders to
     */
    public static Component accountPlaceholders(@NotNull String string, Account account) {
        return accountPlaceholders(string, new HashMap<>() {{
            put("", account);
        }});
    }

    /**
     * Account placeholders
     * @param string String to deserialize with MiniMessage and apply placeholders to
     * @param accounts Accounts to apply placeholders to
     */
    public static String accountPlaceholdersString(@NotNull String string, HashMap<String, @NotNull Account> accounts) {
        for (Map.Entry<String, Account> entry : accounts.entrySet()) {
            String name = entry.getKey();
            Account account = entry.getValue();
            String prefix = name.isEmpty() ? "" : name + "-";
            string = string.replace("<" + prefix + "account>", account.name == null ? (account.type == Account.Type.PERSONAL && account.owner.getName() != null ? account.owner.getName() : account.id) : account.name)
                    .replace("<" + prefix + "account-id>", account.id)
                    .replace("<" + prefix + "account-type>", account.type.name)
                    .replace("<" + prefix + "account-owner>", account.owner.getUniqueId().equals(BankAccounts.getConsoleOfflinePlayer().getUniqueId()) ? "<i>the server</i>" : account.owner.getName() == null ? "<i>unknown player</i>" : account.owner.getName())
                    .replace("<" + prefix + "balance>", account.balance == null ? "∞" : account.balance.toPlainString())
                    .replace("<" + prefix + "balance-formatted>", BankAccounts.formatCurrency(account.balance))
                    .replace("<" + prefix + "balance-short>", BankAccounts.formatCurrencyShort(account.balance));
        }
        return string;
    }

    /**
     * Account placeholders
     * @param string String to deserialize with MiniMessage and apply placeholders to
     * @param accounts Accounts to apply placeholders to
     */
    public static Component accountPlaceholders(@NotNull String string, HashMap<String, Account> accounts) {
        return MiniMessage.miniMessage().deserialize(accountPlaceholdersString(string, accounts));
    }
}
