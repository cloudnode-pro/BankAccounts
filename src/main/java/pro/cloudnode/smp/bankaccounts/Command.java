package pro.cloudnode.smp.bankaccounts;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class Command implements CommandExecutor, TabCompleter {
    /**
     * Send message to sender.
     *
     * @param sender       Command sender.
     * @param message      Message to send.
     * @return Always true.
     */
    public static boolean sendMessage(final @NotNull CommandSender sender, final @NotNull Component message) {
        sender.sendMessage(message);
        return true;
    }
    /**
     * Send message to sender.
     *
     * @param sender       Command sender.
     * @param message      Message to send.
     * @param placeholders Placeholders to replace.
     * @return Always true.
     */
    public static boolean sendMessage(final @NotNull CommandSender sender, final @NotNull String message, final @NotNull TagResolver @NotNull ... placeholders) {
        sendMessage(sender, MiniMessage.miniMessage().deserialize(message, placeholders));
        return true;
    }

    /**
     * Send config message to sender.
     *
     * @param sender       Command sender.
     * @param path         Path to message in config.
     * @param placeholders Placeholders to replace.
     * @return Always true.
     */
    public static boolean sendMessage(final @NotNull CommandSender sender, final @NotNull BankConfig path, final @NotNull TagResolver @NotNull ... placeholders) {
        return sendMessage(sender, Objects.requireNonNull(BankAccounts.getInstance().getConfig().getString(path.getKey())), placeholders);
    }

    /**
     * Send command usage to sender.
     *
     * @param sender    Command sender.
     * @param label     Command label.
     * @param arguments Command arguments.
     * @return Always true.
     */
    protected static boolean sendUsage(final @NotNull CommandSender sender, final @NotNull String label, final @NotNull String arguments) {
        return sendMessage(sender, BankConfig.MESSAGES_COMMAND_USAGE, Placeholder.unparsed("command", label), Placeholder.unparsed("arguments", arguments));
    }
}
