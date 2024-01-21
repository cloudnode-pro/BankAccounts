package pro.cloudnode.smp.bankaccounts;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public abstract class Command implements CommandExecutor, TabCompleter {
    /**
     * Send message to sender.
     *
     * @param sender  Command sender.
     * @param message Message to send.
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
     * Send command usage to sender.
     *
     * @param sender    Command sender.
     * @param label     Command label.
     * @param arguments Command arguments.
     * @return Always true.
     */
    protected static boolean sendUsage(final @NotNull CommandSender sender, final @NotNull String label, final @NotNull String arguments) {
        return sendMessage(sender, BankAccounts.getInstance().config().messagesCommandUsage(label, arguments));
    }

    /**
     * Execute command
     *
     * @param sender Command sender
     * @param label  Command label
     * @param args   Command arguments
     */
    protected abstract boolean execute(final @NotNull CommandSender sender, final @NotNull String label, final @NotNull String @NotNull [] args);

    /**
     * Tab complete
     *
     * @param sender Command sender
     * @param args   Command arguments
     */
    protected abstract @Nullable List<@NotNull String> tab(final @NotNull CommandSender sender, final @NotNull String @NotNull [] args);

    @Override
    public final boolean onCommand(final @NotNull CommandSender sender, final @NotNull org.bukkit.command.Command command, final @NotNull String label, final @NotNull String @NotNull [] args) {
        BankAccounts.getInstance().getServer().getScheduler().runTaskAsynchronously(BankAccounts.getInstance(), () -> {
            final boolean ignored = execute(sender, label, args);
        });
        return true;
    }

    @Override
    public final @Nullable List<@NotNull String> onTabComplete(final @NotNull CommandSender sender, final @NotNull org.bukkit.command.Command command, final @NotNull String label, final @NotNull String @NotNull [] args) {
        final @Nullable List<@NotNull String> suggestions = tab(sender, args);
        return Optional.ofNullable(suggestions).map(s -> s.stream().filter(suggestion -> suggestion.toLowerCase().startsWith(args[args.length - 1].toLowerCase())).toList()).orElse(null);
    }
}
