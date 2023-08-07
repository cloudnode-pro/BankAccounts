package pro.cloudnode.smp.bankaccounts.commands;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.UnsafeValues;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pro.cloudnode.smp.bankaccounts.BankAccounts;
import pro.cloudnode.smp.bankaccounts.BankConfig;
import pro.cloudnode.smp.bankaccounts.ChestShop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ItemInfoCommand extends pro.cloudnode.smp.bankaccounts.Command {
    @Override
    public boolean onCommand(final @NotNull CommandSender sender, final @NotNull Command command, final @NotNull String label, final @NotNull String @NotNull [] args) {
        if (!sender.hasPermission("bank.iteminfo"))
            return sendMessage(sender, BankConfig.MESSAGES_ERRORS_NO_PERMISSION);
        @NotNull ItemStack item;
        @NotNull Optional<@NotNull String> snbt = Optional.empty();
        boolean gui = false;
        final @NotNull String @NotNull [] argsCopy;
        if (args.length > 0) {
            gui = Arrays.asList(args).contains("--gui");
            argsCopy = Arrays.stream(args).filter(s -> !s.equals("--gui")).toArray(String[]::new);
        }
        else argsCopy = args;
        if (argsCopy.length > 0) {
            final boolean hasNbt = argsCopy[0].contains("{");
            final @NotNull String materialName = hasNbt ? argsCopy[0].split("\\{")[0] : argsCopy[0];
            final @Nullable Material material = Material.matchMaterial(materialName.toUpperCase());
            if (material == null)
                return sendMessage(sender, BankConfig.MESSAGES_ERRORS_INVALID_ITEM, Placeholder.unparsed("item", materialName));
            if (hasNbt) {
                snbt = Optional.of(argsCopy[0].substring(materialName.length()));
                item = Bukkit.getUnsafe().modifyItemStack(new ItemStack(material), snbt.get());
            }
            else item = new ItemStack(material);
        }
        else if (sender instanceof final @NotNull Player player) {
            item = player.getInventory().getItemInMainHand();
            if (item.getType().isAir()) return sendMessage(sender, BankConfig.MESSAGES_ERRORS_MUST_HOLD_ITEM);
        }
        else return sendUsage(sender, label, "<item>");

        if (item.getType().isAir()) return sendMessage(sender, BankConfig.MESSAGES_ERRORS_INVALID_ITEM, Placeholder.unparsed("item", item.getType().getKey().getKey()));

        final @NotNull ItemMeta meta = item.getItemMeta();
        for (final @NotNull NamespacedKey key : meta.getPersistentDataContainer().getKeys())
            meta.getPersistentDataContainer().remove(key);
        item.setItemMeta(meta);

        if (gui && sender instanceof final @NotNull Player player) {
            ChestShop.openItemPreview(player, item);
            return true;
        }
        else return sendMessage(sender, Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString(BankConfig.MESSAGES_ITEM_HASH.getKey()))
                .replace("<hash>", ChestShop.hashItem(item))
                .replace("<item>", item.getType().getKey().getKey() + ":1" + snbt.map(s -> ":" + s).orElse(""))
                .replace("<cmd>", "/" + label + " " + (argsCopy.length > 0 ? item.getType().getKey().getKey() + snbt.orElse("") : "") + " --gui"),
                Placeholder.component("item_name", item.displayName())
        );
    }

    @Override
    public @NotNull List<String> onTabComplete(final @NotNull CommandSender sender, final @NotNull Command command, final @NotNull String alias, final @NotNull String @NotNull [] args) {
        if (args.length == 1) {
            final @NotNull List<@NotNull String> completions = new ArrayList<>();
            final @NotNull String arg = args[0].toLowerCase();
            for (final @NotNull Material material : Material.values()) {
                final @NotNull String name = material.getKey().getKey();
                if (name.startsWith(arg)) completions.add(name);
            }
            return completions;
        }
        return new ArrayList<>();
    }
}
