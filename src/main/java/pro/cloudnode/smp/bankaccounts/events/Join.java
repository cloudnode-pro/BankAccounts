package pro.cloudnode.smp.bankaccounts.events;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pro.cloudnode.smp.bankaccounts.Account;
import pro.cloudnode.smp.bankaccounts.BankAccounts;
import pro.cloudnode.smp.bankaccounts.BankConfig;
import pro.cloudnode.smp.bankaccounts.Command;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Join implements Listener {
    @EventHandler
    public void onPlayerJoin(final @NotNull PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        if (!"null".equals(BankAccounts.getInstance().getConfig().getString("starting-balance"))) {
            Bukkit.getScheduler().runTaskAsynchronously(BankAccounts.getInstance(), () -> {
                final @NotNull Account[] accounts = Account.get(player, Account.Type.PERSONAL);
                if (accounts.length == 0) {
                    final double startingBalance = BankAccounts.getInstance().getConfig().getDouble("starting-balance");
                    new Account(player, Account.Type.PERSONAL, null, BigDecimal.valueOf(startingBalance), false).insert();
                }
            });
        }
        if (player.hasPermission("bank.notify-update")) {
            final @NotNull BankAccounts plugin = BankAccounts.getInstance();
            final @NotNull String mcVersion = plugin.getServer().getMinecraftVersion();
            final @NotNull String pluginName = plugin.getPluginMeta().getName();
            final @NotNull String pluginVersion = plugin.getPluginMeta().getVersion();
            try {
                final @NotNull HttpClient client = HttpClient.newHttpClient();
                final @NotNull HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create("https://api.modrinth.com/v2/project/Dc8RS2En/version?featured=true&game_versions=[%22" + mcVersion + "%22]"))
                        .header("User-Agent",
                                pluginName + "/" + pluginVersion
                        )
                        .GET()
                        .build();
                final @NotNull HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
                if (res.statusCode() < 400 && res.statusCode() >= 200 && res.body() != null) {
                    final @NotNull Matcher matcher = Pattern.compile("\"version_number\":\"(.+?)\"").matcher(res.body());
                    if (matcher.find()) {
                        final @Nullable String latestVersion = matcher.group(1);
                        if (latestVersion != null && !latestVersion.equals(pluginVersion))
                            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                                Command.sendMessage(player, Objects.requireNonNull(plugin.getConfig()
                                                .getString(BankConfig.MESSAGES_UPDATE_AVAILABLE.getKey()))
                                        .replace("<version>", latestVersion));
                            }, 20L);
                    }
                }
            }
            catch (final @NotNull Exception e) {
                plugin.getLogger().log(Level.WARNING, "Failed to check for updates", e);
            }
        }
    }
}
