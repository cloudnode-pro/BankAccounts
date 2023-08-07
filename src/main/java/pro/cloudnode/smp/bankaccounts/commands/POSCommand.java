package pro.cloudnode.smp.bankaccounts.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pro.cloudnode.smp.bankaccounts.Account;
import pro.cloudnode.smp.bankaccounts.BankAccounts;
import pro.cloudnode.smp.bankaccounts.BankConfig;
import pro.cloudnode.smp.bankaccounts.POS;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

/**
 * Create a POS at the location the player is looking at.
 * <p>
 * Permission: {@code bank.pos.create}
 * <p>
 * {@code /pos <account> <price> [description]}
 */
public final class POSCommand extends pro.cloudnode.smp.bankaccounts.Command {
    @Override
    public boolean onCommand(final @NotNull CommandSender sender, final @NotNull Command command, final @NotNull String label, final @NotNull String @NotNull [] args) {
        if (!(sender instanceof final @NotNull Player player))
            return sendMessage(sender, BankConfig.MESSAGES_ERRORS_PLAYER_ONLY);
        if (!player.hasPermission("bank.pos.create"))
            return sendMessage(sender, BankConfig.MESSAGES_ERRORS_NO_PERMISSION);

        if (args.length < 2)
            return sendUsage(sender, label, (args.length > 0 ? args[0] : "<account>") + " <price> [description]");

        final @NotNull Optional<@NotNull Account> account = Account.get(args[0]);
        if (account.isEmpty()) return sendMessage(sender, BankConfig.MESSAGES_ERRORS_ACCOUNT_NOT_FOUND);

        if (account.get().type == Account.Type.PERSONAL && !BankAccounts.getInstance().getConfig()
                .getBoolean("pos.allow-personal") && !player.hasPermission("bank.pos.create.personal"))
            return sendMessage(sender, BankConfig.MESSAGES_ERRORS_POS_CREATE_BUSINESS_ONLY);

        if (account.get().frozen)
            return sendMessage(sender, Account.placeholders(Objects.requireNonNull(BankAccounts.getInstance()
                    .getConfig().getString(BankConfig.MESSAGES_ERRORS_FROZEN.getKey())), account.get()));

        if (!player.hasPermission("bank.pos.create.other") && !account.get().owner.equals(player))
            return sendMessage(sender, BankConfig.MESSAGES_ERRORS_NOT_ACCOUNT_OWNER);

        final @NotNull BigDecimal price;
        try {
            price = BigDecimal.valueOf(Double.parseDouble(args[1])).setScale(2, RoundingMode.HALF_UP);
        }
        catch (final @NotNull NumberFormatException e) {
            return sendMessage(sender, BankConfig.MESSAGES_ERRORS_INVALID_NUMBER, Placeholder.unparsed("number", args[1]));
        }

        if (price.compareTo(BigDecimal.ZERO) <= 0)
            return sendMessage(sender, BankConfig.MESSAGES_ERRORS_INVALID_NUMBER, Placeholder.unparsed("number", args[1]));


        final @Nullable Block target = player.getTargetBlockExact(5);
        if (target == null) return sendMessage(sender, BankConfig.MESSAGES_ERRORS_BLOCK_TOO_FAR);

        if (target.getType() != Material.CHEST) return sendMessage(sender, BankConfig.MESSAGES_ERRORS_POS_NOT_CHEST);

        final @NotNull Chest chest = (Chest) target.getState();
        if (chest.getInventory().isEmpty()) return sendMessage(sender, BankConfig.MESSAGES_ERRORS_POS_EMPTY);
        if (POS.get(chest).isPresent()) return sendMessage(sender, BankConfig.MESSAGES_ERRORS_POS_ALREADY_EXISTS);

        @Nullable String description = args.length > 2 ? String.join(" ", Arrays.copyOfRange(args, 2, args.length)) : null;
        if (description != null && description.length() > 64) description = description.substring(0, 64);

        if (description != null && (description.contains("<") || description.contains(">")))
            return sendMessage(sender, BankConfig.MESSAGES_ERRORS_DISALLOWED_CHARACTERS, Placeholder.unparsed("characters", "<>"));

        final @NotNull POS pos = new POS(target.getLocation(), price, description, account.get(), new Date());
        pos.save();
        return sendMessage(sender, replacePlaceholders(Objects.requireNonNull(BankAccounts.getInstance().getConfig()
                .getString("messages.pos-created")), pos));
    }

    public @NotNull ArrayList<@NotNull String> onTabComplete(final @NotNull CommandSender sender, final @NotNull Command command, final @NotNull String label, final @NotNull String @NotNull [] args) {
        final @NotNull ArrayList<@NotNull String> suggestions = new ArrayList<>();
        if (sender.hasPermission("bank.pos.create") && sender instanceof Player && args.length == 1) {
            final @NotNull Account[] accounts = sender.hasPermission("bank.pos.create.other") ? Account.get() : Account.get(BankAccounts.getOfflinePlayer(sender));
            for (final @NotNull Account account : accounts) {
                if (account.frozen || (account.type == Account.Type.PERSONAL && !BankAccounts.getInstance().getConfig()
                        .getBoolean("pos.allow-personal") && !sender.hasPermission("bank.pos.create.personal")))
                    continue;
                suggestions.add(account.id);
            }
        }
        return suggestions;
    }

    /**
     * Replace POS placeholders
     *
     * @param message Message to replace placeholders in
     * @param pos     POS to get placeholders from
     */
    public static @NotNull Component replacePlaceholders(final @NotNull String message, final @NotNull POS pos) {
        return Account.placeholders(message.replace("<price>", pos.price.toPlainString())
                .replace("<price-formatted>", BankAccounts.formatCurrency(pos.price))
                .replace("<price-short>", BankAccounts.formatCurrencyShort(pos.price))
                .replace("<description>", pos.description == null ? "<gray><i>no description</i></gray>" : pos.description), pos.seller);
    }
}
