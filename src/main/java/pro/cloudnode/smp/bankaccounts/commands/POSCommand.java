package pro.cloudnode.smp.bankaccounts.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.DoubleChestInventory;
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
            return sendMessage(sender, BankAccounts.getInstance().config().messagesErrorsPlayerOnly());
        if (!player.hasPermission("bank.pos.create"))
            return sendMessage(sender, BankAccounts.getInstance().config().messagesErrorsNoPermission());

        if (args.length < 2)
            return sendUsage(sender, label, (args.length > 0 ? args[0] : "<account>") + " <price> [description]");

        final @NotNull Optional<@NotNull Account> account = Account.get(args[0]);
        if (account.isEmpty()) return sendMessage(sender, BankAccounts.getInstance().config().messagesErrorsAccountNotFound());

        if (account.get().type == Account.Type.PERSONAL && !BankAccounts.getInstance().config().posAllowPersonal() && !player.hasPermission("bank.pos.create.personal"))
            return sendMessage(sender, BankAccounts.getInstance().config().messagesErrorsPosCreateBusinessOnly());

        if (account.get().frozen)
            return sendMessage(sender, Account.placeholders(BankAccounts.getInstance().config().messagesErrorsFrozen(), account.get()));

        if (!player.hasPermission("bank.pos.create.other") && !account.get().owner.equals(player))
            return sendMessage(sender, BankAccounts.getInstance().config().messagesErrorsNotAccountOwner());

        final @NotNull BigDecimal price;
        try {
            price = BigDecimal.valueOf(Double.parseDouble(args[1])).setScale(2, RoundingMode.HALF_UP);
        }
        catch (final @NotNull NumberFormatException e) {
            return sendMessage(sender, BankAccounts.getInstance().config().messagesErrorsInvalidNumber(), Placeholder.unparsed("number", args[1]));
        }

        if (price.compareTo(BigDecimal.ZERO) <= 0)
            return sendMessage(sender, BankAccounts.getInstance().config().messagesErrorsInvalidNumber(), Placeholder.unparsed("number", args[1]));


        final @Nullable Block target = player.getTargetBlockExact(5);
        if (target == null) return sendMessage(sender, BankAccounts.getInstance().config().messagesErrorsBlockTooFar());

        if (!(target.getState() instanceof final @NotNull Chest chest))
            return sendMessage(sender, BankAccounts.getInstance().config().messagesErrorsPosNotChest());
        if (chest.getInventory() instanceof DoubleChestInventory)
            return sendMessage(sender, BankAccounts.getInstance().config().messagesErrorsPosDoubleChest());
        if (chest.getInventory().isEmpty()) return sendMessage(sender, BankAccounts.getInstance().config().messagesErrorsPosEmpty());
        if (POS.get(chest).isPresent()) return sendMessage(sender, BankAccounts.getInstance().config().messagesErrorsPosAlreadyExists());

        @Nullable String description = args.length > 2 ? String.join(" ", Arrays.copyOfRange(args, 2, args.length)) : null;
        if (description != null && description.length() > 64) description = description.substring(0, 64);

        if (description != null && (description.contains("<") || description.contains(">")))
            return sendMessage(sender, BankAccounts.getInstance().config().messagesErrorsDisallowedCharacters(), Placeholder.unparsed("characters", "<>"));

        final @NotNull POS pos = new POS(target.getLocation(), price, description, account.get(), new Date());
        pos.save();
        return sendMessage(sender, replacePlaceholders(BankAccounts.getInstance().config().messagesPosCreated(), pos));
    }

    public @NotNull ArrayList<@NotNull String> onTabComplete(final @NotNull CommandSender sender, final @NotNull Command command, final @NotNull String label, final @NotNull String @NotNull [] args) {
        final @NotNull ArrayList<@NotNull String> suggestions = new ArrayList<>();
        if (sender.hasPermission("bank.pos.create") && sender instanceof Player && args.length == 1) {
            final @NotNull Account[] accounts = sender.hasPermission("bank.pos.create.other") ? Account.get() : Account.get(BankAccounts.getOfflinePlayer(sender));
            for (final @NotNull Account account : accounts) {
                if (account.frozen || (account.type == Account.Type.PERSONAL && !BankAccounts.getInstance().config().posAllowPersonal() && !sender.hasPermission("bank.pos.create.personal")))
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
