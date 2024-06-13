package pro.cloudnode.smp.bankaccounts;

import net.kyori.adventure.audience.Audience;
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
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public abstract class Command implements CommandExecutor, TabCompleter {
    /**
     * Send message to sender.
     *
     * @param audience Message recipient
     * @param message  Message to send.
     * @return Always true.
     */
    public static boolean sendMessage(final @NotNull Audience audience, final @NotNull Component message) {
        audience.sendMessage(message);
        return true;
    }

    /**
     * Send message to sender.
     *
     * @param audience     Message recipient
     * @param message      Message to send.
     * @param placeholders Placeholders to replace.
     * @return Always true.
     */
    public static boolean sendMessage(final @NotNull Audience audience, final @NotNull String message, final @NotNull TagResolver @NotNull ... placeholders) {
        sendMessage(audience, MiniMessage.miniMessage().deserialize(message, placeholders));
        return true;
    }

    /**
     * Send command usage to sender.
     *
     * @param audience  Message recipient
     * @param label     Command label.
     * @param arguments Command arguments.
     * @return Always true.
     */
    protected static boolean sendUsage(final @NotNull Audience audience, final @NotNull String label, final @NotNull String arguments) {
        return sendMessage(audience, BankAccounts.getInstance().config().messagesCommandUsage(label, arguments));
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

    protected static @NotNull Set<@NotNull String> getDisallowedCharacters(final @Nullable String input) {
        if (input == null) return Set.of();
        final @NotNull Set<@NotNull String> chars = input
                .codePoints()
                .filter(codePoint -> codePoint > 0xFFFF)
                .mapToObj(codePoint -> new String(Character.toChars(codePoint)))
                .collect(Collectors.toSet());
        final @NotNull Matcher matcher = Pattern.compile("[<>\\x00-\\x08\\x0B-\\x1F\\x7F-\\x9F\\u2400-\\u2421\\u200B-\\u200D\\uFEFF\\uD800-\\uDB7F\\uDFFF]").matcher(input);
        while (matcher.find()) chars.add(matcher.group());
        return chars;
    }
}
