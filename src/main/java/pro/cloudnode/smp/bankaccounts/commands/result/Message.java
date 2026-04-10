package pro.cloudnode.smp.bankaccounts.commands.result;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class Message extends CommandResult {
    public final @Nullable Audience audience;
    public final @NotNull Component message;

    public Message(@Nullable Audience audience, @NotNull Component message) {
        super();
        this.audience = audience;
        this.message = message;
    }

    public Message(@Nullable Audience audience, @NotNull String message, final @NotNull TagResolver @NotNull ... placeholders) {
        this(audience, MiniMessage.miniMessage().deserialize(message, placeholders));
    }

    public void send() {
        if (audience != null)
            audience.sendMessage(message);
    }
}
