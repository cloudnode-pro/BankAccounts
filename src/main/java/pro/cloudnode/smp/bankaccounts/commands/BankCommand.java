package pro.cloudnode.smp.bankaccounts.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import pro.cloudnode.smp.bankaccounts.Account;
import pro.cloudnode.smp.bankaccounts.BankAccounts;

import java.util.ArrayList;
import java.util.Arrays;
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
        }
        else {
            switch (args[0]) {
                case "balance", "bal", "account", "accounts" -> {
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
            case "bal", "balance", "account", "accounts" -> balance(sender, Arrays.copyOfRange(args, 1, args.length));
            case "reload" -> reload(sender);
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
    public static void balance(@NotNull CommandSender sender, String[] args) throws NullPointerException {
        if (args.length == 0) {
            if (!sender.hasPermission("bank.balance.self")) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("messages.errors.no-permission"))));
                return;
            }
            @NotNull OfflinePlayer player = sender instanceof OfflinePlayer ? (OfflinePlayer) sender : BankAccounts.getInstance().getServer().getOfflinePlayer(UUID.fromString("00000000-0000-0000-0000-000000000000"));
            listAccounts(sender, player);
        }
        else switch (args[0]) {
            case "--player" -> {
                if (!sender.hasPermission("bank.balance.other")) {
                    sender.sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("messages.errors.no-permission"))));
                    return;
                }
                if (args.length == 1) {
                    sender.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>(!) Usage: <white>/<command> balance --player <player>"));
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
