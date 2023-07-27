package pro.cloudnode.smp.bankaccounts.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pro.cloudnode.smp.bankaccounts.Account;
import pro.cloudnode.smp.bankaccounts.BankAccounts;
import pro.cloudnode.smp.bankaccounts.POS;

import java.math.BigDecimal;
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
public final class POSCommand implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(final @NotNull CommandSender sender, final @NotNull Command command, final @NotNull String label, final String[] args) {
        if (sender instanceof final @NotNull Player player) {
            if (!player.hasPermission("bank.pos.create")) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("messages.errors.no-permission"))));
                return true;
            }

            if (args.length < 2) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>(!) Usage: <white>/<command> " + (args.length > 0 ? args[0] : "<account>") + " <price> [description]",
                        Placeholder.unparsed("command", label)
                ));
                return true;
            }

            final @NotNull Optional<Account> account = Account.get(args[0]);
            if (account.isEmpty()) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("messages.errors.account-not-found"))));
                return true;
            }

            if (account.get().frozen) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("messages.errors.frozen"))));
                return true;
            }

            if (!player.hasPermission("bank.pos.create.other") && !account.get().owner.equals(player)) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("messages.errors.not-account-owner"))));
                return true;
            }

            final @NotNull BigDecimal price;
            try {
                price = BigDecimal.valueOf(Double.parseDouble(args[1]));
            } catch (NumberFormatException e) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("messages.errors.invalid-number")),
                        Placeholder.unparsed("number", args[1])
                ));
                return true;
            }

            final @Nullable Block target = player.getTargetBlockExact(5);
            if (target == null) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("messages.errors.block-too-far"))));
                return true;
            }

            if (target.getType() != Material.CHEST) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("messages.errors.pos-not-chest"))));
                return true;
            }

            final @NotNull Chest chest = (Chest) target.getState();
            if (chest.getInventory().isEmpty()) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("messages.errors.pos-empty"))));
                return true;
            }
            if (POS.get(chest).isPresent()) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("messages.errors.pos-already-exists"))));
                return true;
            }

            final @Nullable String description = args.length > 2 ? String.join(" ", Arrays.copyOfRange(args, 2, args.length)) : null;

            final POS pos = new POS(target.getLocation(), price, description, account.get(), new Date());
            pos.save();
            player.sendMessage(replacePlaceholders(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("messages.pos-created")), pos));
        } else
            sender.sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString("messages.errors.player-only"))));
        return true;
    }

    public ArrayList<String> onTabComplete(final @NotNull CommandSender sender, final @NotNull Command command, final @NotNull String label, final String[] args) {
        return new ArrayList<>();
    }

    /**
     * Replace POS placeholders
     *
     * @param message Message to replace placeholders in
     * @param pos     POS to get placeholders from
     */
    public static Component replacePlaceholders(final String message, final POS pos) {
        return BankCommand.accountPlaceholders(message
                        .replace("<price>", pos.price.toPlainString())
                        .replace("<price-formatted>", BankAccounts.formatCurrency(pos.price))
                        .replace("<price-short>", BankAccounts.formatCurrencyShort(pos.price))
                        .replace("<description>", pos.description == null ? "<gray><i>no description</i></gray>" : pos.description)
                , pos.seller);
    }
}
