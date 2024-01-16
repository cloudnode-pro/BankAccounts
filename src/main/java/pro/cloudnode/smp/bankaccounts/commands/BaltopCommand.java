package pro.cloudnode.smp.bankaccounts.commands;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pro.cloudnode.smp.bankaccounts.Account;
import pro.cloudnode.smp.bankaccounts.BankAccounts;
import pro.cloudnode.smp.bankaccounts.Command;
import pro.cloudnode.smp.bankaccounts.Permissions;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

public final class BaltopCommand extends Command {
    @Override
    public boolean execute(final @NotNull CommandSender sender, final @NotNull String label, final @NotNull String @NotNull [] args) {
        return run(sender, label, args, new String[0]);
    }

    @Override
    public @NotNull List<@NotNull String> tab(final @NotNull CommandSender sender, final @NotNull String @NotNull [] args) {
        final @NotNull ArrayList<@NotNull String> suggestions = new ArrayList<>();
        if (!sender.hasPermission(Permissions.BALTOP)) return suggestions;
        if (args.length == 1) suggestions.addAll(Arrays.asList("personal", "business", "player"));
        else if (args.length == 2) suggestions.add("1");
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
        final @NotNull String cmdNext = "/baltop" + (type.map(s -> " " + s).orElse("")) + " " + Math.min(1000000000, page + 1);

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
            final @NotNull BaltopPlayer @NotNull [] players = BaltopPlayer.get(perPage, page);
            for (int i = 0; i < players.length; i++) {
                final @NotNull BaltopPlayer entry = players[i];
                final @NotNull OfflinePlayer player = BankAccounts.getInstance().getServer().getOfflinePlayer(entry.uuid);
                sendMessage(sender, BankAccounts.getInstance().config().messagesBaltopEntryPlayer()
                        .replace("<position>", String.valueOf((page - 1) * perPage + i + 1))
                        .replace("<uuid>", entry.uuid.toString())
                        .replace("<username>", entry.uuid.toString().equals(BankAccounts.getConsoleOfflinePlayer().getUniqueId().toString()) ? "the Server" : Optional.ofNullable(player.getName()).orElse("Unknown Player"))
                        .replace("<balance>", entry.balance.toPlainString())
                        .replace("<balance-formatted>", BankAccounts.formatCurrency(entry.balance))
                        .replace("<balance-short>", BankAccounts.formatCurrencyShort(entry.balance))
                );
            }
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

    public static final class BaltopPlayer {
        public final @NotNull UUID uuid;
        public final @NotNull BigDecimal balance;

        public BaltopPlayer(final @NotNull ResultSet rs) throws @NotNull SQLException {
            this.uuid = UUID.fromString(rs.getString("owner"));
            this.balance = rs.getBigDecimal("balance");
        }

        public static @NotNull BaltopPlayer @NotNull [] get(final int perPage, final int page) {
            final @NotNull List<@NotNull BaltopPlayer> entries = new ArrayList<>();
            try (final @NotNull Connection conn = BankAccounts.getInstance().getDb().getConnection();
                 final @NotNull PreparedStatement stmt = conn.prepareStatement("SELECT `owner`, SUM(`balance`) AS `balance` FROM `bank_accounts` WHERE `balance` IS NOT NULL AND `balance` > 0 GROUP BY `owner` LIMIT ? OFFSET ?;")) {
                stmt.setInt(1, perPage);
                stmt.setInt(2, (page - 1) * perPage);
                final @NotNull ResultSet rs = stmt.executeQuery();
                while (rs.next()) entries.add(new BaltopPlayer(rs));
                return entries.toArray(new BaltopPlayer[0]);
            }
            catch (final @NotNull SQLException e) {
                BankAccounts.getInstance().getLogger().log(Level.SEVERE, "Could not get top balance players", e);
                return new BaltopPlayer[0];
            }
        }
    }
}
