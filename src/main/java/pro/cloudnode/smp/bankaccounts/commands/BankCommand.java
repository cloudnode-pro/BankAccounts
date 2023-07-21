package pro.cloudnode.smp.bankaccounts.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import pro.cloudnode.smp.bankaccounts.Account;
import pro.cloudnode.smp.bankaccounts.BankAccounts;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;
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
        if (sender.hasPermission("bank.account.create")) sender.sendMessage(MiniMessage.miniMessage().deserialize("<click:suggest_command:/bank create ><green>/bank create <gray>[PERSONAL|BUSINESS]</gray></green> <white>- Create a new account</click>"));
        if (sender.hasPermission("bank.account.create.other")) sender.sendMessage(MiniMessage.miniMessage().deserialize("<click:suggest_command:/bank create --player ><green>/bank create <gray>[PERSONAL|BUSINESS] --player [player]</gray></green> <white>- Create an account for another player</click>"));
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
                else if (!sender.hasPermission("bank.balance.other") && !account.get().owner.getUniqueId().equals(((OfflinePlayer) sender).getUniqueId())) {
                    sender.sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("messages.errors.no-permission"))));
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
                sender.sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("messages.errors.invalid-number"))));
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
                sender.sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("messages.errors.no-permission"))));
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
     * Account placeholders
     * @param string String to deserialize with MiniMessage and apply placeholders to
     */
    public static Component accountPlaceholders(@NotNull String string, Account account) {
        return MiniMessage.miniMessage().deserialize(string
                .replace("<account>", account.name == null ? (account.type == Account.Type.PERSONAL && account.owner.getName() != null ? account.owner.getName() : account.id) : account.name)
                .replace("<account-id>", account.id)
                .replace("<account-type>", account.type.name)
                .replace("<balance>", account.balance == null ? "∞" : account.balance.toPlainString())
                .replace("<balance-formatted>", BankAccounts.formatCurrency(account.balance))
                .replace("<balance-short>", BankAccounts.formatCurrencyShort(account.balance))
        );
    }
}
