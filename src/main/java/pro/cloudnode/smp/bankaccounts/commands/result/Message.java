package pro.cloudnode.smp.bankaccounts.commands.result;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;

public final class Message extends CommandResult {
    public final @NotNull Audience audience;
    public final @NotNull Component message;

    public Message(@NotNull Audience audience, @NotNull Component message) {
        super();
        this.audience = audience;
        this.message = message;
    }

    public Message(@NotNull Audience audience, @NotNull String message, final @NotNull TagResolver @NotNull ... placeholders) {
        this(audience, MiniMessage.miniMessage().deserialize(message, placeholders));
    }

    public void send() {
        audience.sendMessage(message);
    }
}
