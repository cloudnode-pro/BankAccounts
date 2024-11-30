package pro.cloudnode.smp.bankaccounts.commands;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.DoubleChestInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pro.cloudnode.smp.bankaccounts.Account;
import pro.cloudnode.smp.bankaccounts.BankAccounts;
import pro.cloudnode.smp.bankaccounts.Command;
import pro.cloudnode.smp.bankaccounts.POS;
import pro.cloudnode.smp.bankaccounts.Permissions;
import pro.cloudnode.smp.bankaccounts.commands.result.CommandResult;
import pro.cloudnode.smp.bankaccounts.commands.result.Message;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;
import java.util.Set;

/**
 * Create a POS at the location the player is looking at.
 * <p>
 * Permission: {@code bank.pos.create}
 * <p>
 * {@code /pos <account> <price> [description]}
 */
public final class POSCommand extends Command {
    @Override
    public @NotNull CommandResult execute(final @NotNull CommandSender sender, final @NotNull String label, final @NotNull String @NotNull [] args) {
        if (!(sender instanceof final @NotNull Player player))
            return new Message(sender, BankAccounts.getInstance().config().messagesErrorsPlayerOnly());
        if (!player.hasPermission(Permissions.POS_CREATE))
            return new Message(sender, BankAccounts.getInstance().config().messagesErrorsNoPermission());

        if (args.length < 2)
            return sendUsage(sender, label, (args.length > 0 ? args[0] : "<account>") + " <price> [description]");

        final @NotNull Optional<@NotNull Account> account = Account.get(Account.Tag.from(args[0]));
        if (account.isEmpty()) return new Message(sender, BankAccounts.getInstance().config().messagesErrorsAccountNotFound());

        if (account.get().type == Account.Type.PERSONAL && !BankAccounts.getInstance().config().posAllowPersonal() && !player.hasPermission(Permissions.POS_CREATE_PERSONAL))
            return new Message(sender, BankAccounts.getInstance().config().messagesErrorsPosCreateBusinessOnly());

        if (account.get().frozen)
            return new Message(sender, BankAccounts.getInstance().config().messagesErrorsFrozen(account.get()));

        if (!player.hasPermission(Permissions.POS_CREATE_OTHER) && !account.get().owner.equals(player))
            return new Message(sender, BankAccounts.getInstance().config().messagesErrorsNotAccountOwner());

        final @NotNull BigDecimal price;
        try {
            price = new BigDecimal(args[1]).setScale(2, RoundingMode.HALF_UP);
        }
        catch (final @NotNull NumberFormatException e) {
            return new Message(sender, BankAccounts.getInstance().config().messagesErrorsInvalidNumber(args[1]));
        }

        if (price.compareTo(BigDecimal.ZERO) <= 0)
            return new Message(sender, BankAccounts.getInstance().config().messagesErrorsInvalidNumber(args[1]));


        final @Nullable Block target = player.getTargetBlockExact(5);
        if (target == null) return new Message(sender, BankAccounts.getInstance().config().messagesErrorsBlockTooFar());

        final @NotNull Optional<@NotNull BlockState> block = BankAccounts.runOnMain(target::getState, 5);
        if (block.isEmpty()) return new Message(sender, BankAccounts.getInstance().config().messagesErrorsAsyncFailed());
        if (!(block.get() instanceof final @NotNull Chest chest))
            return new Message(sender, BankAccounts.getInstance().config().messagesErrorsPosNotChest());
        if (chest.getInventory() instanceof DoubleChestInventory)
            return new Message(sender, BankAccounts.getInstance().config().messagesErrorsPosDoubleChest());
        if (chest.getInventory().isEmpty()) return new Message(sender, BankAccounts.getInstance().config().messagesErrorsPosEmpty());
        if (POS.get(chest).isPresent()) return new Message(sender, BankAccounts.getInstance().config().messagesErrorsPosAlreadyExists());

        @Nullable String description = args.length > 2 ? String.join(" ", Arrays.copyOfRange(args, 2, args.length)) : null;
        if (description != null && description.length() > 64) description = description.substring(0, 63) + "…";

        final @NotNull Set<@NotNull String> disallowedChars = getDisallowedCharacters(description);
        if (!disallowedChars.isEmpty())
            return new Message(sender, BankAccounts.getInstance().config().messagesErrorsDisallowedCharacters(disallowedChars));

        final @NotNull POS pos = new POS(target.getLocation(), price, description, account.get(), new Date());
        pos.save();
        return new Message(sender, BankAccounts.getInstance().config().messagesPosCreated(pos));
    }

    @Override
    public @NotNull ArrayList<@NotNull String> tab(final @NotNull CommandSender sender, final @NotNull String @NotNull [] args) {
        final @NotNull ArrayList<@NotNull String> suggestions = new ArrayList<>();
        if (sender.hasPermission(Permissions.POS_CREATE) && sender instanceof final @NotNull Player player && args.length == 1) {
            if (args[0].startsWith("@")) {
                if (sender.hasPermission(Permissions.POS_CREATE_OTHER))
                    suggestions.addAll(sender.getServer().getOnlinePlayers().stream().map(p -> "@" + p.getName()).toList());
                else suggestions.add("@" + player.getName());
            }
            else {
                final @NotNull Account @NotNull [] accounts;
                if (sender.hasPermission(Permissions.POS_CREATE_OTHER))
                    accounts = Account.get();
                else accounts = Account.get(BankAccounts.getOfflinePlayer(sender));
                suggestions.addAll(Arrays.stream(accounts).filter(account -> !account.frozen && (account.type != Account.Type.PERSONAL || BankAccounts.getInstance().config().posAllowPersonal() || sender.hasPermission(Permissions.POS_CREATE_PERSONAL))).map(a -> a.id).toList());
            }
        }
        return suggestions;
    }
}
