package pro.cloudnode.smp.bankaccounts;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;
import java.util.logging.Level;

public final class ModrinthUpdate {
    public final @NotNull String version;
    public final @NotNull String name;

    public ModrinthUpdate(final @NotNull String version, final @NotNull String name) {
        this.version = version;
        this.name = name;
    }

    public final @NotNull String url() {
        return "https://modrinth.com/plugin/bankaccounts/version/" + this.version;
    }

    /**
     * Query the Modrinth versions API for a compatible update.
     *
     * @return The latest version if an update is available, otherwise empty.
     */
    public static @NotNull Optional<@NotNull ModrinthUpdate> checkForUpdates() {
        final @NotNull BankAccounts plugin = BankAccounts.getInstance();
        final @NotNull String mcVersion = plugin.getServer().getMinecraftVersion();
        final @NotNull String pluginName = plugin.getPluginMeta().getName();
        final @NotNull String pluginVersion = plugin.getPluginMeta().getVersion();
        try {
            final @NotNull HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.modrinth.com/v2/project/Dc8RS2En/version?version_type=release&game_versions=[%22" + mcVersion + "%22]"))
                    .header("User-Agent", pluginName + "/" + pluginVersion).GET().build();
            final @NotNull HttpResponse<@NotNull String> res = HttpClient.newHttpClient()
                    .send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() >= 400 || res.statusCode() < 200 || res.body() == null || JsonParser
                    .parseString(res.body()).getAsJsonArray().isEmpty()) return Optional.empty();
            final @NotNull JsonObject json = JsonParser.parseString(res.body()).getAsJsonArray().get(0)
                    .getAsJsonObject();
            final @NotNull String version = json.get("version_number").getAsString();
            final @NotNull String name = json.get("name").getAsString();
            final @NotNull ModrinthUpdate latest = new ModrinthUpdate(version, name);
            if (!latest.version.equals(pluginVersion)) return Optional.of(latest);
            return Optional.empty();
        }
        catch (final @NotNull Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to check for updates", e);
            return Optional.empty();
        }
    }
}
