package pro.cloudnode.smp.bankaccounts;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pro.cloudnode.smp.bankaccounts.commands.BaltopCommand;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAccessor;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.TimeZone;

public final class BankConfig {
    public @NotNull FileConfiguration config;

    public BankConfig(@NotNull final FileConfiguration config) {
        this.config = config;
    }

    //db.db
    public @NotNull String dbDb() {
        return Objects.requireNonNull(config.getString("db.db"));
    }

    // db.sqlite.file
    public @NotNull String dbSqliteFile() {
        return Objects.requireNonNull(config.getString("db.sqlite.file"));
    }

    // db.mariadb.jdbc
    public @NotNull String dbMariadbJdbc() {
        return Objects.requireNonNull(config.getString("db.mariadb.jdbc"));
    }

    // db.mariadb.user
    public @NotNull String dbMariadbUser() {
        return Objects.requireNonNull(config.getString("db.mariadb.user"));
    }

    // db.mariadb.password
    public @NotNull String dbMariadbPassword() {
        return Objects.requireNonNull(config.getString("db.mariadb.password"));
    }

    // db.cachePrepStmts
    public boolean dbCachePrepStmts() {
        return config.getBoolean("db.cachePrepStmts");
    }

    // db.prepStmtCacheSize
    public int dbPrepStmtCacheSize() {
        return config.getInt("db.prepStmtCacheSize");
    }

    // db.prepStmtCacheSqlLimit
    public int dbPrepStmtCacheSqlLimit() {
        return config.getInt("db.prepStmtCacheSqlLimit");
    }

    // db.useServerPrepStmts
    public boolean dbUseServerPrepStmts() {
        return config.getBoolean("db.useServerPrepStmts");
    }

    // db.useLocalSessionState
    public boolean dbUseLocalSessionState() {
        return config.getBoolean("db.useLocalSessionState");
    }

    // db.rewriteBatchedStatements
    public boolean dbRewriteBatchedStatements() {
        return config.getBoolean("db.rewriteBatchedStatements");
    }

    // db.cacheResultSetMetadata
    public boolean dbCacheResultSetMetadata() {
        return config.getBoolean("db.cacheResultSetMetadata");
    }

    // db.cacheServerConfiguration
    public boolean dbCacheServerConfiguration() {
        return config.getBoolean("db.cacheServerConfiguration");
    }

    // db.elideSetAutoCommits
    public boolean dbElideSetAutoCommits() {
        return config.getBoolean("db.elideSetAutoCommits");
    }

    // db.maintainTimeStats
    public boolean dbMaintainTimeStats() {
        return config.getBoolean("db.maintainTimeStats");
    }

    // currency.symbol
    public @NotNull String currencySymbol() {
        return Objects.requireNonNull(config.getString("currency.symbol"));
    }

    // currency.format
    public @NotNull DecimalFormat currencyFormat() {
        return new DecimalFormat(Objects.requireNonNull(config.getString("currency.format")));
    }

    // starting-balance
    public @NotNull Optional<@NotNull Double> startingBalance() {
        if (Objects.requireNonNull(config.getString("starting-balance")).equalsIgnoreCase("null"))
            return Optional.empty();
        else return Optional.of(config.getDouble("starting-balance"));
    }

    // prevent-close-last-personal
    public boolean preventCloseLastPersonal() {
        return config.getBoolean("prevent-close-last-personal");
    }

    // server-account.enabled
    public boolean serverAccountEnabled() {
        return config.getBoolean("server-account.enabled");
    }

    // server-account.name
    public @NotNull String serverAccountName() {
        return Objects.requireNonNull(config.getString("server-account.name"));
    }

    // server-account.type
    public @NotNull Account.Type serverAccountType() {
        return Account.Type.getType(config.getInt("server-account.type"));
    }

    // server-account.starting-balance
    public @NotNull Optional<@NotNull Double> serverAccountStartingBalance() {
        if (Objects.requireNonNull(config.getString("server-account.starting-balance")).equalsIgnoreCase("infinity"))
            return Optional.empty();
        else return Optional.of(config.getDouble("server-account.starting-balance"));
    }

    // account-limits.
    public int accountLimits(final @NotNull Account.Type type) {
        return config.getInt("account-limits." + Account.Type.getType(type));
    }

    // baltop.per-page
    public int baltopPerPage() {
        return config.getInt("baltop.per-page");
    }

    // transfer-confirmation.enabled
    public boolean transferConfirmationEnabled() {
        return config.getBoolean("transfer-confirmation.enabled");
    }

    // transfer-confirmation.min-amount
    public double transferConfirmationMinAmount() {
        return config.getDouble("transfer-confirmation.min-amount");
    }

    // transfer-confirmation.bypass-own-accounts
    public boolean transferConfirmationBypassOwnAccounts() {
        return config.getBoolean("transfer-confirmation.bypass-own-accounts");
    }

    // history.per-page
    public int historyPerPage() {
        return config.getInt("history.per-page");
    }

    // instruments.material
    public @NotNull Material instrumentsMaterial() {
        return Objects.requireNonNull(Material.getMaterial(Objects.requireNonNull(config.getString("instruments.material"))));
    }

    // instruments.require-item
    public boolean instrumentsRequireItem() {
        return config.getBoolean("instruments.require-item");
    }

    // instruments.name
    public @NotNull Component instrumentsName(final @NotNull Account account, final @NotNull TemporalAccessor created) {
        return MiniMessage.miniMessage().deserialize(
                Objects.requireNonNull(config.getString("instruments.name")),
                Placeholder.unparsed("account", account.name()),
                Placeholder.unparsed("account-id", account.id),
                Placeholder.unparsed("account-type", account.type.getName()),
                Placeholder.component("account-owner", account.ownerName()),
                Formatter.date("date", created)
        ).decoration(TextDecoration.ITALIC, false);
    }

    // instruments.lore
    public @NotNull List<@NotNull Component> instrumentsLore(final @NotNull Account account, final @NotNull TemporalAccessor created) {
        return Objects.requireNonNull(config.getStringList("instruments.lore")).stream().map(string -> MiniMessage.miniMessage().deserialize(
                string,
                Placeholder.unparsed("account", account.name()),
                Placeholder.unparsed("account-id", account.id),
                Placeholder.unparsed("account-type", account.type.getName()),
                Placeholder.component("account-owner", account.ownerName()),
                Formatter.date("date", created)
        ).decoration(TextDecoration.ITALIC, false)).toList();
    }

    // instruments.glint.enabled
    public boolean instrumentsGlintEnabled() {
        return config.getBoolean("instruments.glint.enabled");
    }

    // instruments.glint.enchantment
    public @NotNull Enchantment instrumentsGlintEnchantment() {
        return Objects.requireNonNull(Registry.ENCHANTMENT.get(NamespacedKey.minecraft(Objects.requireNonNull(config.getString("instruments.glint.enchantment")))));
    }

    // pos.allow-personal
    public boolean posAllowPersonal() {
        return config.getBoolean("pos.allow-personal");
    }

    // pos.title
    public @NotNull Component posTitle(final @NotNull POS pos) {
        return MiniMessage.miniMessage().deserialize(
                Objects.requireNonNull(config.getString("pos.title")),
                Placeholder.unparsed("description", pos.description == null ? "no description" : pos.description),
                Placeholder.unparsed("price", pos.price.toPlainString()),
                Placeholder.unparsed("price-formatted", BankAccounts.formatCurrency(pos.price)),
                Placeholder.unparsed("price-short", BankAccounts.formatCurrencyShort(pos.price))
        );
    }

    // pos.info.material
    public @NotNull Material posInfoMaterial() {
        return Objects.requireNonNull(Material.getMaterial(Objects.requireNonNull(config.getString("pos.info.material"))));
    }

    // pos.info.glint
    public boolean posInfoGlint() {
        return config.getBoolean("pos.info.glint");
    }

    // pos.info.name-owner
    public @NotNull Component posInfoNameOwner(final @NotNull POS pos) {
        return MiniMessage.miniMessage().deserialize(
                Objects.requireNonNull(config.getString("pos.info.name-owner")),
                Placeholder.unparsed("description", pos.description == null ? "no description" : pos.description),
                Placeholder.unparsed("price", pos.price.toPlainString()),
                Placeholder.unparsed("price-formatted", BankAccounts.formatCurrency(pos.price)),
                Placeholder.unparsed("price-short", BankAccounts.formatCurrencyShort(pos.price))
        ).decoration(TextDecoration.ITALIC, false);
    }

    // pos.info.name-buyer
    public @NotNull Component posInfoNameBuyer(final @NotNull POS pos) {
        return MiniMessage.miniMessage().deserialize(
                Objects.requireNonNull(config.getString("pos.info.name-buyer")),
                Placeholder.unparsed("description", pos.description == null ? "no description" : pos.description),
                Placeholder.unparsed("price", pos.price.toPlainString()),
                Placeholder.unparsed("price-formatted", BankAccounts.formatCurrency(pos.price)),
                Placeholder.unparsed("price-short", BankAccounts.formatCurrencyShort(pos.price)),
                Placeholder.unparsed("account", pos.seller.name()),
                Placeholder.unparsed("account-id", pos.seller.id),
                Placeholder.unparsed("account-type", pos.seller.type.getName()),
                Placeholder.component("account-owner", pos.seller.ownerName()),
                Placeholder.unparsed("balance", pos.seller.balance == null ? "∞" : pos.seller.balance.toPlainString()),
                Placeholder.unparsed("balance-formatted", BankAccounts.formatCurrency(pos.seller.balance)),
                Placeholder.unparsed("balance-short", BankAccounts.formatCurrencyShort(pos.seller.balance))
        ).decoration(TextDecoration.ITALIC, false);
    }

    // pos.info.lore-owner
    public @NotNull List<@NotNull Component> posInfoLoreOwner(final @NotNull POS pos) {
        return Objects.requireNonNull(config.getStringList("pos.info.lore-owner")).stream().map(string -> MiniMessage.miniMessage().deserialize(
                string,
                Placeholder.unparsed("description", pos.description == null ? "no description" : pos.description),
                Placeholder.unparsed("price", pos.price.toPlainString()),
                Placeholder.unparsed("price-formatted", BankAccounts.formatCurrency(pos.price)),
                Placeholder.unparsed("price-short", BankAccounts.formatCurrencyShort(pos.price))
        ).decoration(TextDecoration.ITALIC, false)).toList();
    }

    // pos.info.lore-buyer
    public @NotNull List<@NotNull Component> posInfoLoreBuyer(final @NotNull POS pos) {
        return Objects.requireNonNull(config.getStringList("pos.info.lore-buyer")).stream().map(string -> MiniMessage.miniMessage().deserialize(
                string,
                Placeholder.unparsed("description", pos.description == null ? "no description" : pos.description),
                Placeholder.unparsed("price", pos.price.toPlainString()),
                Placeholder.unparsed("price-formatted", BankAccounts.formatCurrency(pos.price)),
                Placeholder.unparsed("price-short", BankAccounts.formatCurrencyShort(pos.price)),
                Placeholder.unparsed("account", pos.seller.name()),
                Placeholder.unparsed("account-id", pos.seller.id),
                Placeholder.unparsed("account-type", pos.seller.type.getName()),
                Placeholder.component("account-owner", pos.seller.ownerName()),
                Placeholder.unparsed("balance", pos.seller.balance == null ? "∞" : pos.seller.balance.toPlainString()),
                Placeholder.unparsed("balance-formatted", BankAccounts.formatCurrency(pos.seller.balance)),
                Placeholder.unparsed("balance-short", BankAccounts.formatCurrencyShort(pos.seller.balance))
        ).decoration(TextDecoration.ITALIC, false)).toList();
    }

    // pos.delete.material
    public @NotNull Material posDeleteMaterial() {
        return Objects.requireNonNull(Material.getMaterial(Objects.requireNonNull(config.getString("pos.delete.material"))));
    }

    // pos.delete.glint
    public boolean posDeleteGlint() {
        return config.getBoolean("pos.delete.glint");
    }

    // pos.delete.name
    public @NotNull Component posDeleteName() {
        return MiniMessage.miniMessage().deserialize(Objects.requireNonNull(config.getString("pos.delete.name"))).decoration(TextDecoration.ITALIC, false);
    }

    // pos.delete.lore
    public @NotNull List<@NotNull Component> posDeleteLore() {
        return Objects.requireNonNull(config.getStringList("pos.delete.lore")).stream().map(string -> MiniMessage.miniMessage().deserialize(string).decoration(TextDecoration.ITALIC, false)).toList();
    }

    // pos.confirm.material
    public @NotNull Material posConfirmMaterial() {
        return Objects.requireNonNull(Material.getMaterial(Objects.requireNonNull(config.getString("pos.confirm.material"))));
    }

    // pos.confirm.glint
    public boolean posConfirmGlint() {
        return config.getBoolean("pos.confirm.glint");
    }

    // pos.confirm.name
    public @NotNull Component posConfirmName(final @NotNull POS pos, final @NotNull Account buyer) {
        return MiniMessage.miniMessage().deserialize(
                Objects.requireNonNull(config.getString("pos.confirm.name")),
                Placeholder.unparsed("description", pos.description == null ? "no description" : pos.description),
                Placeholder.unparsed("price", pos.price.toPlainString()),
                Placeholder.unparsed("price-formatted", BankAccounts.formatCurrency(pos.price)),
                Placeholder.unparsed("price-short", BankAccounts.formatCurrencyShort(pos.price)),
                Placeholder.unparsed("account", buyer.name()),
                Placeholder.unparsed("account-id", buyer.id),
                Placeholder.unparsed("account-type", buyer.type.getName()),
                Placeholder.component("account-owner", buyer.ownerName()),
                Placeholder.unparsed("balance", buyer.balance == null ? "∞" : buyer.balance.toPlainString()),
                Placeholder.unparsed("balance-formatted", BankAccounts.formatCurrency(buyer.balance)),
                Placeholder.unparsed("balance-short", BankAccounts.formatCurrencyShort(buyer.balance))
        ).decoration(TextDecoration.ITALIC, false);
    }

    // pos.confirm.lore
    public @NotNull List<@NotNull Component> posConfirmLore(final @NotNull POS pos, final @NotNull Account buyer) {
        return Objects.requireNonNull(config.getStringList("pos.confirm.lore")).stream().map(string -> MiniMessage.miniMessage().deserialize(
            string,
            Placeholder.unparsed("description", pos.description == null ? "no description" : pos.description),
            Placeholder.unparsed("price", pos.price.toPlainString()),
            Placeholder.unparsed("price-formatted", BankAccounts.formatCurrency(pos.price)),
            Placeholder.unparsed("price-short", BankAccounts.formatCurrencyShort(pos.price)),
            Placeholder.unparsed("account", buyer.name()),
            Placeholder.unparsed("account-id", buyer.id),
            Placeholder.unparsed("account-type", buyer.type.getName()),
            Placeholder.component("account-owner", buyer.ownerName()),
            Placeholder.unparsed("balance", buyer.balance == null ? "∞" : buyer.balance.toPlainString()),
            Placeholder.unparsed("balance-formatted", BankAccounts.formatCurrency(buyer.balance)),
            Placeholder.unparsed("balance-short", BankAccounts.formatCurrencyShort(buyer.balance))
        ).decoration(TextDecoration.ITALIC, false)).toList();
    }

    // pos.decline.material
    public @NotNull Material posDeclineMaterial() {
        return Objects.requireNonNull(Material.getMaterial(Objects.requireNonNull(config.getString("pos.decline.material"))));
    }

    // pos.decline.glint
    public boolean posDeclineGlint() {
        return config.getBoolean("pos.decline.glint");
    }

    // pos.decline.name
    public @NotNull Component posDeclineName() {
        return MiniMessage.miniMessage().deserialize(Objects.requireNonNull(config.getString("pos.decline.name"))).decoration(TextDecoration.ITALIC, false);
    }

    // pos.decline.lore
    public @NotNull List<@NotNull Component> posDeclineLore() {
        return Objects.requireNonNull(config.getStringList("pos.decline.lore")).stream().map(string -> MiniMessage.miniMessage().deserialize(string).decoration(TextDecoration.ITALIC, false)).toList();
    }

    // interest.*.rate
    public double interestRate(final @NotNull Account.Type type) {
        return config.getDouble("interest." + Account.Type.getType(type) + ".rate");
    }

    // interest.*.interval
    public int interestInterval(final @NotNull Account.Type type) {
        return config.getInt("interest." + Account.Type.getType(type) + ".interval");
    }

    // interest.*.description
    public @NotNull String interestDescription(final @NotNull Account.Type type, final double rate, final @NotNull Account account) {
        return Objects.requireNonNull(config.getString("interest." + Account.Type.getType(type) + ".description"))
                .replace("<rate>", String.valueOf(rate))
                .replace("<rate-formatted>", new DecimalFormat("#.##").format(rate) + "%")
                .replace("<balance>", account.balance == null ? "∞" : account.balance.toPlainString())
                .replace("<balance-formatted>", BankAccounts.formatCurrency(account.balance))
                .replace("<balance-short>", BankAccounts.formatCurrencyShort(account.balance));
    }

    // messages.command-usage
    public @NotNull Component messagesCommandUsage(final @NotNull String command, final @NotNull String arguments) {
        return MiniMessage.miniMessage().deserialize(
                Objects.requireNonNull(config.getString("messages.command-usage"))
                        .replace("<command>", command)
                        .replace("<arguments>", arguments)
        );
    }

    // messages.types.
    public @NotNull String messagesTypes(final @NotNull Account.Type type) {
        return Objects.requireNonNull(config.getString("messages.types." + Account.Type.getType(type)));
    }

    // messages.errors.no-accounts
    public @NotNull Component messagesErrorsNoAccounts() {
        return MiniMessage.miniMessage().deserialize(Objects.requireNonNull(config.getString("messages.errors.no-accounts")));
    }

    // messages.errors.no-permission
    public @NotNull Component messagesErrorsNoPermission() {
        return MiniMessage.miniMessage().deserialize(Objects.requireNonNull(config.getString("messages.errors.no-permission")));
    }

    // messages.errors.account-not-found
    public @NotNull Component messagesErrorsAccountNotFound() {
        return MiniMessage.miniMessage().deserialize(Objects.requireNonNull(config.getString("messages.errors.account-not-found")));
    }

    // messages.errors.unknown-command
    public @NotNull Component messagesErrorsUnknownCommand() {
        return MiniMessage.miniMessage().deserialize(Objects.requireNonNull(config.getString("messages.errors.unknown-command")));
    }

    // messages.errors.max-accounts
    public @NotNull Component messagesErrorsMaxAccounts(final @NotNull Account.Type type, final int limit) {
        return MiniMessage.miniMessage().deserialize(
                Objects.requireNonNull(config.getString("messages.errors.max-accounts"))
                        .replace("<type>", type.getName())
                        .replace("<limit>", String.valueOf(limit))
        );
    }

    // messages.errors.rename-personal
    public @NotNull Component messagesErrorsRenamePersonal() {
        return MiniMessage.miniMessage().deserialize(Objects.requireNonNull(config.getString("messages.errors.rename-personal")));
    }

    // messages.errors.not-account-owner
    public @NotNull Component messagesErrorsNotAccountOwner() {
        return MiniMessage.miniMessage().deserialize(Objects.requireNonNull(config.getString("messages.errors.not-account-owner")));
    }

    // messages.errors.frozen
    public @NotNull Component messagesErrorsFrozen(final @NotNull Account account) {
        return MiniMessage.miniMessage().deserialize(
                Objects.requireNonNull(config.getString("messages.errors.frozen"))
                        .replace("<account>", account.name())
                        .replace("<account-id>", account.id)
                        .replace("<account-type>", account.type.getName())
                        .replace("<account-owner>", account.ownerNameUnparsed())
                        .replace("<balance>", account.balance == null ? "∞" : account.balance.toPlainString())
                        .replace("<balance-formatted>", BankAccounts.formatCurrency(account.balance))
                        .replace("<balance-short>", BankAccounts.formatCurrencyShort(account.balance))
        );
    }

    // messages.errors.same-from-to
    public @NotNull Component messagesErrorsSameFromTo() {
        return MiniMessage.miniMessage().deserialize(Objects.requireNonNull(config.getString("messages.errors.same-from-to")));
    }

    // messages.errors.transfer-self-only
    public @NotNull Component messagesErrorsTransferSelfOnly() {
        return MiniMessage.miniMessage().deserialize(Objects.requireNonNull(config.getString("messages.errors.transfer-self-only")));
    }

    // messages.errors.transfer-other-only
    public @NotNull Component messagesErrorsTransferOtherOnly() {
        return MiniMessage.miniMessage().deserialize(Objects.requireNonNull(config.getString("messages.errors.transfer-other-only")));
    }

    // messages.errors.invalid-number
    public @NotNull Component messagesErrorsInvalidNumber(final @NotNull String number) {
        return MiniMessage.miniMessage().deserialize(
                Objects.requireNonNull(config.getString("messages.errors.invalid-number"))
                        .replace("<number>", number)
        );
    }

    // messages.errors.negative-transfer
    public @NotNull Component messagesErrorsNegativeTransfer() {
        return MiniMessage.miniMessage().deserialize(Objects.requireNonNull(config.getString("messages.errors.negative-transfer")));
    }

    // messages.errors.insufficient-funds
    public @NotNull Component messagesErrorsInsufficientFunds(final @NotNull Account account) {
        return MiniMessage.miniMessage().deserialize(
                Objects.requireNonNull(config.getString("messages.errors.insufficient-funds"))
                        .replace("<account>", account.name())
                        .replace("<account-id>", account.id)
                        .replace("<account-type>", account.type.getName())
                        .replace("<account-owner>", account.ownerNameUnparsed())
                        .replace("<balance>", account.balance == null ? "∞" : account.balance.toPlainString())
                        .replace("<balance-formatted>", BankAccounts.formatCurrency(account.balance))
                        .replace("<balance-short>", BankAccounts.formatCurrencyShort(account.balance))
        );
    }

    // messages.errors.closing-balance
    public @NotNull Component messagesErrorsClosingBalance(final @NotNull Account account) {
        return MiniMessage.miniMessage().deserialize(
                Objects.requireNonNull(config.getString("messages.errors.closing-balance"))
                        .replace("<account>", account.name())
                        .replace("<account-id>", account.id)
                        .replace("<account-type>", account.type.getName())
                        .replace("<account-owner>", account.ownerNameUnparsed())
                        .replace("<balance>", account.balance == null ? "∞" : account.balance.toPlainString())
                        .replace("<balance-formatted>", BankAccounts.formatCurrency(account.balance))
                        .replace("<balance-short>", BankAccounts.formatCurrencyShort(account.balance))
        );
    }

    // messages.errors.closing-personal
    public @NotNull Component messagesErrorsClosingPersonal() {
        return MiniMessage.miniMessage().deserialize(Objects.requireNonNull(config.getString("messages.errors.closing-personal")));
    }

    // messages.errors.player-only
    public @NotNull Component messagesErrorsPlayerOnly() {
        return MiniMessage.miniMessage().deserialize(Objects.requireNonNull(config.getString("messages.errors.player-only")));
    }

    // messages.errors.player-not-found
    public @NotNull Component messagesErrorsPlayerNotFound() {
        return MiniMessage.miniMessage().deserialize(Objects.requireNonNull(config.getString("messages.errors.player-not-found")));
    }

    // messages.errors.instrument-requires-item
    public @NotNull Component messagesErrorsInstrumentRequiresItem(final @NotNull Material material) {
        return MiniMessage.miniMessage().deserialize(
                Objects.requireNonNull(config.getString("messages.errors.instrument-requires-item"))
                        .replace("<material-key>", material.translationKey())
                        .replace("<material>", material.name())
        );
    }

    // messages.errors.target-inventory-full
    public @NotNull Component messagesErrorsTargetInventoryFull(final @NotNull HumanEntity player) {
        return MiniMessage.miniMessage().deserialize(
                Objects.requireNonNull(config.getString("messages.errors.target-inventory-full"))
                        .replace("<player>", player.getName())
        );
    }

    // messages.errors.block-too-far
    public @NotNull Component messagesErrorsBlockTooFar() {
        return MiniMessage.miniMessage().deserialize(Objects.requireNonNull(config.getString("messages.errors.block-too-far")));
    }

    // messages.errors.pos-already-exists
    public @NotNull Component messagesErrorsPosAlreadyExists() {
        return MiniMessage.miniMessage().deserialize(Objects.requireNonNull(config.getString("messages.errors.pos-already-exists")));
    }

    // messages.errors.pos-not-chest
    public @NotNull Component messagesErrorsPosNotChest() {
        return MiniMessage.miniMessage().deserialize(Objects.requireNonNull(config.getString("messages.errors.pos-not-chest")));
    }

    // messages.errors.pos-double-chest
    public @NotNull Component messagesErrorsPosDoubleChest() {
        return MiniMessage.miniMessage().deserialize(Objects.requireNonNull(config.getString("messages.errors.pos-double-chest")));
    }

    // messages.errors.pos-empty
    public @NotNull Component messagesErrorsPosEmpty() {
        return MiniMessage.miniMessage().deserialize(Objects.requireNonNull(config.getString("messages.errors.pos-empty")));
    }

    // messages.errors.pos-invalid-card
    public @NotNull Component messagesErrorsPosInvalidCard() {
        return MiniMessage.miniMessage().deserialize(Objects.requireNonNull(config.getString("messages.errors.pos-invalid-card")));
    }

    // messages.errors.pos-no-permission
    public @NotNull Component messagesErrorsPosNoPermission() {
        return MiniMessage.miniMessage().deserialize(Objects.requireNonNull(config.getString("messages.errors.pos-no-permission")));
    }

    // messages.errors.no-card
    public @NotNull Component messagesErrorsNoCard() {
        return MiniMessage.miniMessage().deserialize(Objects.requireNonNull(config.getString("messages.errors.no-card")));
    }

    // messages.errors.pos-items-changed
    public @NotNull Component messagesErrorsPosItemsChanged() {
        return MiniMessage.miniMessage().deserialize(Objects.requireNonNull(config.getString("messages.errors.pos-items-changed")));
    }

    // messages.errors.pos-create-business-only
    public @NotNull Component messagesErrorsPosCreateBusinessOnly() {
        return MiniMessage.miniMessage().deserialize(Objects.requireNonNull(config.getString("messages.errors.pos-create-business-only")));
    }

    // messages.errors.disallowed-characters
    public @NotNull Component messagesErrorsDisallowedCharacters(final @NotNull String characters) {
        return MiniMessage.miniMessage().deserialize(
                Objects.requireNonNull(config.getString("messages.errors.disallowed-characters"))
                        .replace("<characters>", characters)
        );
    }

    // messages.errors.already-frozen
    public @NotNull Component messagesErrorsAlreadyFrozen(final @NotNull Account account) {
        return MiniMessage.miniMessage().deserialize(
                Objects.requireNonNull(config.getString("messages.errors.already-frozen"))
                        .replace("<account>", account.name())
                        .replace("<account-id>", account.id)
                        .replace("<account-type>", account.type.getName())
                        .replace("<account-owner>", account.ownerNameUnparsed())
                        .replace("<balance>", account.balance == null ? "∞" : account.balance.toPlainString())
                        .replace("<balance-formatted>", BankAccounts.formatCurrency(account.balance))
                        .replace("<balance-short>", BankAccounts.formatCurrencyShort(account.balance))
        );
    }

    // messages.errors.not-frozen
    public @NotNull Component messagesErrorsNotFrozen(final @NotNull Account account) {
        return MiniMessage.miniMessage().deserialize(
                Objects.requireNonNull(config.getString("messages.errors.not-frozen"))
                        .replace("<account>", account.name())
                        .replace("<account-id>", account.id)
                        .replace("<account-type>", account.type.getName())
                        .replace("<account-owner>", account.ownerNameUnparsed())
                        .replace("<balance>", account.balance == null ? "∞" : account.balance.toPlainString())
                        .replace("<balance-formatted>", BankAccounts.formatCurrency(account.balance))
                        .replace("<balance-short>", BankAccounts.formatCurrencyShort(account.balance))
        );
    }

    // messages.errors.negative-invoice
    public @NotNull Component messagesErrorsNegativeInvoice() {
        return MiniMessage.miniMessage().deserialize(Objects.requireNonNull(config.getString("messages.errors.negative-invoice")));
    }

    // messages.balance
    public @NotNull Component messagesBalance(final @NotNull Account account) {
        return MiniMessage.miniMessage().deserialize(
                Objects.requireNonNull(config.getString("messages.balance"))
                        .replace("<account>", account.name())
                        .replace("<account-id>", account.id)
                        .replace("<account-type>", account.type.getName())
                        .replace("<account-owner>", account.ownerNameUnparsed())
                        .replace("<balance>", account.balance == null ? "∞" : account.balance.toPlainString())
                        .replace("<balance-formatted>", BankAccounts.formatCurrency(account.balance))
                        .replace("<balance-short>", BankAccounts.formatCurrencyShort(account.balance))
        );
    }

    // messages.list-accounts.header
    public @NotNull Component messagesListAccountsHeader() {
        return MiniMessage.miniMessage().deserialize(Objects.requireNonNull(config.getString("messages.list-accounts.header")));
    }

    // messages.list-accounts.entry
    public @NotNull Component messagesListAccountsEntry(final @NotNull Account account) {
        return MiniMessage.miniMessage().deserialize(
                Objects.requireNonNull(config.getString("messages.list-accounts.entry"))
                        .replace("<account>", account.name())
                        .replace("<account-id>", account.id)
                        .replace("<account-type>", account.type.getName())
                        .replace("<account-owner>", account.ownerNameUnparsed())
                        .replace("<balance>", account.balance == null ? "∞" : account.balance.toPlainString())
                        .replace("<balance-formatted>", BankAccounts.formatCurrency(account.balance))
                        .replace("<balance-short>", BankAccounts.formatCurrencyShort(account.balance))
        );
    }

    // messages.reload
    public @NotNull Component messagesReload() {
        return MiniMessage.miniMessage().deserialize(Objects.requireNonNull(config.getString("messages.reload")));
    }

    // messages.account-created
    public @NotNull Component messagesAccountCreated(final @NotNull Account account) {
        return MiniMessage.miniMessage().deserialize(
                Objects.requireNonNull(config.getString("messages.account-created"))
                        .replace("<account>", account.name())
                        .replace("<account-id>", account.id)
                        .replace("<account-type>", account.type.getName())
                        .replace("<account-owner>", account.ownerNameUnparsed())
                        .replace("<balance>", account.balance == null ? "∞" : account.balance.toPlainString())
                        .replace("<balance-formatted>", BankAccounts.formatCurrency(account.balance))
                        .replace("<balance-short>", BankAccounts.formatCurrencyShort(account.balance))
        );
    }

    // messages.balance-set
    public @NotNull Component messagesBalanceSet(final @NotNull Account account) {
        return MiniMessage.miniMessage().deserialize(
                Objects.requireNonNull(config.getString("messages.balance-set"))
                        .replace("<account>", account.name())
                        .replace("<account-id>", account.id)
                        .replace("<account-type>", account.type.getName())
                        .replace("<account-owner>", account.ownerNameUnparsed())
                        .replace("<balance>", account.balance == null ? "∞" : account.balance.toPlainString())
                        .replace("<balance-formatted>", BankAccounts.formatCurrency(account.balance))
                        .replace("<balance-short>", BankAccounts.formatCurrencyShort(account.balance))
        );
    }

    // messages.name-set
    public @NotNull Component messagesNameSet(final @NotNull Account account) {
        return MiniMessage.miniMessage().deserialize(
                Objects.requireNonNull(config.getString("messages.name-set"))
                        .replace("<account>", account.name())
                        .replace("<account-id>", account.id)
                        .replace("<account-type>", account.type.getName())
                        .replace("<account-owner>", account.ownerNameUnparsed())
                        .replace("<balance>", account.balance == null ? "∞" : account.balance.toPlainString())
                        .replace("<balance-formatted>", BankAccounts.formatCurrency(account.balance))
                        .replace("<balance-short>", BankAccounts.formatCurrencyShort(account.balance))
        );
    }

    // messages.account-frozen
    public @NotNull Component messagesAccountFrozen(final @NotNull Account account) {
        return MiniMessage.miniMessage().deserialize(
                Objects.requireNonNull(config.getString("messages.account-frozen"))
                        .replace("<account>", account.name())
                        .replace("<account-id>", account.id)
                        .replace("<account-type>", account.type.getName())
                        .replace("<account-owner>", account.ownerNameUnparsed())
                        .replace("<balance>", account.balance == null ? "∞" : account.balance.toPlainString())
                        .replace("<balance-formatted>", BankAccounts.formatCurrency(account.balance))
                        .replace("<balance-short>", BankAccounts.formatCurrencyShort(account.balance))
        );
    }

    // messages.account-unfrozen
    public @NotNull Component messagesAccountUnfrozen(final @NotNull Account account) {
        return MiniMessage.miniMessage().deserialize(
                Objects.requireNonNull(config.getString("messages.account-unfrozen"))
                        .replace("<account>", account.name())
                        .replace("<account-id>", account.id)
                        .replace("<account-type>", account.type.getName())
                        .replace("<account-owner>", account.ownerNameUnparsed())
                        .replace("<balance>", account.balance == null ? "∞" : account.balance.toPlainString())
                        .replace("<balance-formatted>", BankAccounts.formatCurrency(account.balance))
                        .replace("<balance-short>", BankAccounts.formatCurrencyShort(account.balance))
        );
    }

    // messages.account-deleted
    public @NotNull Component messagesAccountDeleted(final @NotNull Account account) {
        return MiniMessage.miniMessage().deserialize(
                Objects.requireNonNull(config.getString("messages.account-deleted"))
                        .replace("<account>", account.name())
                        .replace("<account-id>", account.id)
                        .replace("<account-type>", account.type.getName())
                        .replace("<account-owner>", account.ownerNameUnparsed())
                        .replace("<balance>", account.balance == null ? "∞" : account.balance.toPlainString())
                        .replace("<balance-formatted>", BankAccounts.formatCurrency(account.balance))
                        .replace("<balance-short>", BankAccounts.formatCurrencyShort(account.balance))
        );
    }

    // messages.confirm-transfer
    public @NotNull Component messagesConfirmTransfer(final @NotNull Account from, final @NotNull Account to, final @NotNull BigDecimal amount, final @Nullable String description) {
        return MiniMessage.miniMessage().deserialize(
                Objects.requireNonNull(config.getString("messages.confirm-transfer"))
                        .replace("<from-account>", from.name())
                        .replace("<from-account-id>", from.id)
                        .replace("<from-account-type>", from.type.getName())
                        .replace("<from-account-owner>", from.ownerNameUnparsed())
                        .replace("<from-balance>", from.balance == null ? "∞" : from.balance.toPlainString())
                        .replace("<from-balance-formatted>", BankAccounts.formatCurrency(from.balance))
                        .replace("<from-balance-short>", BankAccounts.formatCurrencyShort(from.balance))
                        .replace("<to-account>", from.name())
                        .replace("<to-account-id>", from.id)
                        .replace("<to-account-type>", from.type.getName())
                        .replace("<to-account-owner>", from.ownerNameUnparsed())
                        .replace("<to-balance>", from.balance == null ? "∞" : from.balance.toPlainString())
                        .replace("<to-balance-formatted>", BankAccounts.formatCurrency(from.balance))
                        .replace("<to-balance-short>", BankAccounts.formatCurrencyShort(from.balance))
                        .replace("<amount>", amount.toPlainString())
                        .replace("<amount-formatted>", BankAccounts.formatCurrency(amount))
                        .replace("<amount-short>", BankAccounts.formatCurrencyShort(amount))
                        .replace("<description>", description == null ? "<gray><i>no description</i>" : description)
                        .replace("<confirm-command>", "/bank transfer --confirm " + from.id + " " + to.id + " " + amount.toPlainString() + (description == null ? "" : " " + description))
        );
    }

    // messages.transfer-sent
    public @NotNull Component messagesTransferSent(final @NotNull Transaction transaction) {
        return MiniMessage.miniMessage().deserialize(
                Objects.requireNonNull(config.getString("messages.transfer-sent"))
                        .replace("<from-account>", transaction.from.name())
                        .replace("<from-account-id>", transaction.from.id)
                        .replace("<from-account-type>", transaction.from.type.getName())
                        .replace("<from-account-owner>", transaction.from.ownerNameUnparsed())
                        .replace("<from-balance>", transaction.from.balance == null ? "∞" : transaction.from.balance.toPlainString())
                        .replace("<from-balance-formatted>", BankAccounts.formatCurrency(transaction.from.balance))
                        .replace("<from-balance-short>", BankAccounts.formatCurrencyShort(transaction.from.balance))
                        .replace("<to-account>", transaction.from.name())
                        .replace("<to-account-id>", transaction.from.id)
                        .replace("<to-account-type>", transaction.from.type.getName())
                        .replace("<to-account-owner>", transaction.from.ownerNameUnparsed())
                        .replace("<to-balance>", transaction.from.balance == null ? "∞" : transaction.from.balance.toPlainString())
                        .replace("<to-balance-formatted>", BankAccounts.formatCurrency(transaction.from.balance))
                        .replace("<to-balance-short>", BankAccounts.formatCurrencyShort(transaction.from.balance))
                        .replace("<amount>", transaction.amount.toPlainString())
                        .replace("<amount-formatted>", BankAccounts.formatCurrency(transaction.amount))
                        .replace("<amount-short>", BankAccounts.formatCurrencyShort(transaction.amount))
                        .replace("<description>", transaction.description == null ? "<gray><i>no description</i>" : transaction.description)
                        .replace("<transaction-id>", String.valueOf(transaction.getId()))
                        .replace("<instrument>", transaction.instrument == null ? "direct transfer" : transaction.instrument)
        );
    }

    // messages.transfer-received
    public @NotNull Component messagesTransferReceived(final @NotNull Transaction transaction) {
        return MiniMessage.miniMessage().deserialize(
                Objects.requireNonNull(config.getString("messages.transfer-received"))
                        .replace("<from-account>", transaction.from.name())
                        .replace("<from-account-id>", transaction.from.id)
                        .replace("<from-account-type>", transaction.from.type.getName())
                        .replace("<from-account-owner>", transaction.from.ownerNameUnparsed())
                        .replace("<from-balance>", transaction.from.balance == null ? "∞" : transaction.from.balance.toPlainString())
                        .replace("<from-balance-formatted>", BankAccounts.formatCurrency(transaction.from.balance))
                        .replace("<from-balance-short>", BankAccounts.formatCurrencyShort(transaction.from.balance))
                        .replace("<to-account>", transaction.from.name())
                        .replace("<to-account-id>", transaction.from.id)
                        .replace("<to-account-type>", transaction.from.type.getName())
                        .replace("<to-account-owner>", transaction.from.ownerNameUnparsed())
                        .replace("<to-balance>", transaction.from.balance == null ? "∞" : transaction.from.balance.toPlainString())
                        .replace("<to-balance-formatted>", BankAccounts.formatCurrency(transaction.from.balance))
                        .replace("<to-balance-short>", BankAccounts.formatCurrencyShort(transaction.from.balance))
                        .replace("<amount>", transaction.amount.toPlainString())
                        .replace("<amount-formatted>", BankAccounts.formatCurrency(transaction.amount))
                        .replace("<amount-short>", BankAccounts.formatCurrencyShort(transaction.amount))
                        .replace("<description>", transaction.description == null ? "<gray><i>no description</i>" : transaction.description)
                        .replace("<transaction-id>", String.valueOf(transaction.getId()))
                        .replace("<instrument>", transaction.instrument == null ? "direct transfer" : transaction.instrument)
        );
    }

    // messages.history.header
    public @NotNull Component messagesHistoryHeader(final @NotNull Account account, final int page, final int maxPage) {
        return MiniMessage.miniMessage().deserialize(
                Objects.requireNonNull(config.getString("messages.history.header"))
                        .replace("<account>", account.name())
                        .replace("<account-id>", account.id)
                        .replace("<account-type>", account.type.getName())
                        .replace("<account-owner>", account.ownerNameUnparsed())
                        .replace("<balance>", account.balance == null ? "∞" : account.balance.toPlainString())
                        .replace("<balance-formatted>", BankAccounts.formatCurrency(account.balance))
                        .replace("<balance-short>", BankAccounts.formatCurrencyShort(account.balance))
                        .replace("<page>", String.valueOf(page))
                        .replace("<max-page>", String.valueOf(maxPage))
                        .replace("<cmd-prev>", "/bank transactions " + account.id + " " + (page - 1))
                        .replace("<cmd-next>", "/bank transactions " + account.id + " " + (page + 1))
        );
    }

    // messages.history.entry
    public @NotNull Component messagesHistoryEntry(final @NotNull Transaction transaction, final @NotNull Account account) {
        final boolean isSender = transaction.from.id.equals(account.id);
        final @NotNull Account other = isSender ? transaction.to : transaction.from;
        final @NotNull BigDecimal amount = isSender ? transaction.amount.negate() : transaction.amount;
        final SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return MiniMessage.miniMessage().deserialize(
                Objects.requireNonNull(config.getString("messages.history.entry"))
                        .replace("<account>", account.name())
                        .replace("<account-id>", account.id)
                        .replace("<account-type>", account.type.getName())
                        .replace("<account-owner>", account.ownerNameUnparsed())
                        .replace("<balance>", account.balance == null ? "∞" : account.balance.toPlainString())
                        .replace("<balance-formatted>", BankAccounts.formatCurrency(account.balance))
                        .replace("<balance-short>", BankAccounts.formatCurrencyShort(account.balance))
                        .replace("<other-account>", other.name())
                        .replace("<other-account-id>", other.id)
                        .replace("<other-account-type>", other.type.getName())
                        .replace("<other-account-owner>", other.ownerNameUnparsed())
                        .replace("<other-balance>", other.balance == null ? "∞" : other.balance.toPlainString())
                        .replace("<other-balance-formatted>", BankAccounts.formatCurrency(other.balance))
                        .replace("<other-balance-short>", BankAccounts.formatCurrencyShort(other.balance))
                        .replace("<amount>", amount.toPlainString())
                        .replace("<amount-formatted>", BankAccounts.formatCurrency(amount))
                        .replace("<amount-short>", BankAccounts.formatCurrencyShort(amount))
                        .replace("<description>", transaction.description == null ? "<gray><i>no description</i></gray>" : transaction.description)
                        .replace("<transaction-id>", String.valueOf(transaction.getId()))
                        .replace("<instrument>", transaction.instrument == null ? "direct transfer" : transaction.instrument)
                        .replace("<full_date>", sdf.format(transaction.time) + " UTC")
                        .replace("<full-date>", sdf.format(transaction.time) + " UTC"),
                Formatter.date("date", transaction.time.toInstant().atZone(ZoneOffset.UTC).toLocalDateTime())
        );
    }

    // messages.history.footer
    public @NotNull Component messagesHistoryFooter(final @NotNull Account account, final int page, final int maxPage) {
        return MiniMessage.miniMessage().deserialize(
                Objects.requireNonNull(config.getString("messages.history.footer"))
                        .replace("<account>", account.name())
                        .replace("<account-id>", account.id)
                        .replace("<account-type>", account.type.getName())
                        .replace("<account-owner>", account.ownerNameUnparsed())
                        .replace("<balance>", account.balance == null ? "∞" : account.balance.toPlainString())
                        .replace("<balance-formatted>", BankAccounts.formatCurrency(account.balance))
                        .replace("<balance-short>", BankAccounts.formatCurrencyShort(account.balance))
                        .replace("<page>", String.valueOf(page))
                        .replace("<max-page>", String.valueOf(maxPage))
                        .replace("<cmd-prev>", "/bank transactions " + account.id + " " + (page - 1))
                        .replace("<cmd-next>", "/bank transactions " + account.id + " " + (page + 1))
        );
    }

    // messages.history.no-transactions
    public @NotNull Component messagesHistoryNoTransactions() {
        return MiniMessage.miniMessage().deserialize(Objects.requireNonNull(config.getString("messages.history.no-transactions")));
    }

    // messages.instrument-created
    public @NotNull Component messagesInstrumentCreated() {
        return MiniMessage.miniMessage().deserialize(Objects.requireNonNull(config.getString("messages.instrument-created")));
    }

    // messages.pos-removed
    public @NotNull Component messagesPosCreated(final @NotNull POS pos) {
        return MiniMessage.miniMessage().deserialize(
                Objects.requireNonNull(config.getString("messages.pos-created"))
                        .replace("<account>", pos.seller.name())
                        .replace("<account-id>", pos.seller.id)
                        .replace("<account-type>", pos.seller.type.getName())
                        .replace("<account-owner>", pos.seller.ownerNameUnparsed())
                        .replace("<balance>", pos.seller.balance == null ? "∞" : pos.seller.balance.toPlainString())
                        .replace("<balance-formatted>", BankAccounts.formatCurrency(pos.seller.balance))
                        .replace("<balance-short>", BankAccounts.formatCurrencyShort(pos.seller.balance))
                        .replace("<price>", pos.price.toPlainString())
                        .replace("<price-formatted>", BankAccounts.formatCurrency(pos.price))
                        .replace("<price-short>", BankAccounts.formatCurrencyShort(pos.price))
                        .replace("<description>", pos.description == null ? "<gray><i>no description</i></gray>" : pos.description)
                        .replace("<x>", String.valueOf(pos.x))
                        .replace("<y>", String.valueOf(pos.y))
                        .replace("<z>", String.valueOf(pos.z))
                        .replace("<pos>", "X: " + pos.x + " Y: " + pos.y + " Z: " + pos.z + " in " + pos.world.getName())
                        .replace("<world>", pos.world.getName())
        );
    }

    // messages.pos-removed
    public @NotNull Component messagesPosRemoved() {
        return MiniMessage.miniMessage().deserialize(Objects.requireNonNull(config.getString("messages.pos-removed")));
    }

    // messages.pos-purchase
    public @NotNull Component messagesPosPurchase(final @NotNull Transaction transaction, final @NotNull ItemStack @NotNull [] items) {
        return MiniMessage.miniMessage().deserialize(
                Objects.requireNonNull(config.getString("messages.pos-purchase"))
                        .replace("<from-account>", transaction.from.name())
                        .replace("<from-account-id>", transaction.from.id)
                        .replace("<from-account-type>", transaction.from.type.getName())
                        .replace("<from-account-owner>", transaction.from.ownerNameUnparsed())
                        .replace("<from-balance>", transaction.from.balance == null ? "∞" : transaction.from.balance.toPlainString())
                        .replace("<from-balance-formatted>", BankAccounts.formatCurrency(transaction.from.balance))
                        .replace("<from-balance-short>", BankAccounts.formatCurrencyShort(transaction.from.balance))
                        .replace("<to-account>", transaction.from.name())
                        .replace("<to-account-id>", transaction.from.id)
                        .replace("<to-account-type>", transaction.from.type.getName())
                        .replace("<to-account-owner>", transaction.from.ownerNameUnparsed())
                        .replace("<to-balance>", transaction.from.balance == null ? "∞" : transaction.from.balance.toPlainString())
                        .replace("<to-balance-formatted>", BankAccounts.formatCurrency(transaction.from.balance))
                        .replace("<to-balance-short>", BankAccounts.formatCurrencyShort(transaction.from.balance))
                        .replace("<amount>", transaction.amount.toPlainString())
                        .replace("<amount-formatted>", BankAccounts.formatCurrency(transaction.amount))
                        .replace("<amount-short>", BankAccounts.formatCurrencyShort(transaction.amount))
                        .replace("<description>", transaction.description == null ? "<gray><i>no description</i>" : transaction.description)
                        .replace("<transaction-id>", String.valueOf(transaction.getId()))
                        .replace("<instrument>", transaction.instrument == null ? "direct transfer" : transaction.instrument)
                        .replace("<items>", String.valueOf(items.length))
                        .replace("<items-formatted>", items.length == 1 ? "1 item" : items.length + " items")
        );
    }

    // messages.pos-purchase-seller
    public @NotNull Component messagesPosPurchaseSeller(final @NotNull Transaction transaction, final @NotNull ItemStack @NotNull [] items) {
        return MiniMessage.miniMessage().deserialize(
                Objects.requireNonNull(config.getString("messages.pos-purchase-seller"))
                        .replace("<from-account>", transaction.from.name())
                        .replace("<from-account-id>", transaction.from.id)
                        .replace("<from-account-type>", transaction.from.type.getName())
                        .replace("<from-account-owner>", transaction.from.ownerNameUnparsed())
                        .replace("<from-balance>", transaction.from.balance == null ? "∞" : transaction.from.balance.toPlainString())
                        .replace("<from-balance-formatted>", BankAccounts.formatCurrency(transaction.from.balance))
                        .replace("<from-balance-short>", BankAccounts.formatCurrencyShort(transaction.from.balance))
                        .replace("<to-account>", transaction.from.name())
                        .replace("<to-account-id>", transaction.from.id)
                        .replace("<to-account-type>", transaction.from.type.getName())
                        .replace("<to-account-owner>", transaction.from.ownerNameUnparsed())
                        .replace("<to-balance>", transaction.from.balance == null ? "∞" : transaction.from.balance.toPlainString())
                        .replace("<to-balance-formatted>", BankAccounts.formatCurrency(transaction.from.balance))
                        .replace("<to-balance-short>", BankAccounts.formatCurrencyShort(transaction.from.balance))
                        .replace("<amount>", transaction.amount.toPlainString())
                        .replace("<amount-formatted>", BankAccounts.formatCurrency(transaction.amount))
                        .replace("<amount-short>", BankAccounts.formatCurrencyShort(transaction.amount))
                        .replace("<description>", transaction.description == null ? "<gray><i>no description</i>" : transaction.description)
                        .replace("<transaction-id>", String.valueOf(transaction.getId()))
                        .replace("<instrument>", transaction.instrument == null ? "direct transfer" : transaction.instrument)
                        .replace("<items>", String.valueOf(items.length))
                        .replace("<items-formatted>", items.length == 1 ? "1 item" : items.length + " items")
        );
    }

    // messages.whois
    public @NotNull Component messagesWhois(final @NotNull Account account) {
        return MiniMessage.miniMessage().deserialize(
                Objects.requireNonNull(config.getString("messages.whois"))
                        .replace("<account>", account.name())
                        .replace("<account-id>", account.id)
                        .replace("<account-type>", account.type.getName())
                        .replace("<account-owner>", account.ownerNameUnparsed())
                        .replace("<balance>", account.balance == null ? "∞" : account.balance.toPlainString())
                        .replace("<balance-formatted>", BankAccounts.formatCurrency(account.balance))
                        .replace("<balance-short>", BankAccounts.formatCurrencyShort(account.balance))
        );
    }

    // messages.baltop.header
    public @NotNull Component messagesBaltopHeader(final @NotNull String category, final int page, final @NotNull String cmdPrev, final @NotNull String cmdNext) {
        return MiniMessage.miniMessage().deserialize(
                Objects.requireNonNull(config.getString("messages.baltop.header"))
                        .replace("<category>", category)
                        .replace("<page>", String.valueOf(page))
                        .replace("<cmd-prev>", cmdPrev)
                        .replace("<cmd-next>", cmdNext)
        );
    }

    // messages.baltop.entry
    public @NotNull Component messagesBaltopEntry(final @NotNull Account account, final int position) {
        return MiniMessage.miniMessage().deserialize(
                Objects.requireNonNull(config.getString("messages.baltop.entry"))
                .replace("<account>", account.name())
                .replace("<account-id>", account.id)
                .replace("<account-type>", account.type.getName())
                .replace("<account-owner>", account.ownerNameUnparsed())
                .replace("<balance>", account.balance == null ? "∞" : account.balance.toPlainString())
                .replace("<balance-formatted>", BankAccounts.formatCurrency(account.balance))
                .replace("<balance-short>", BankAccounts.formatCurrencyShort(account.balance))
                .replace("<position>", String.valueOf(position))
        );
    }

    // messages.baltop.entry-player
    public @NotNull Component messagesBaltopEntryPlayer(final @NotNull BaltopCommand.BaltopPlayer entry, final int position) {
        return MiniMessage.miniMessage().deserialize(
                Objects.requireNonNull(config.getString("messages.baltop.entry-player"))
                        .replace("<position>", String.valueOf(position))
                        .replace("<uuid>", entry.uuid.toString())
                        .replace("<username>", entry.uuid.toString().equals(BankAccounts.getConsoleOfflinePlayer().getUniqueId().toString())
                                ? "the Server"
                                : Optional.ofNullable(BankAccounts.getInstance().getServer().getOfflinePlayer(entry.uuid).getName())
                                    .orElse("Unknown Player"))
                        .replace("<balance>", entry.balance.toPlainString())
                        .replace("<balance-formatted>", BankAccounts.formatCurrency(entry.balance))
                        .replace("<balance-short>", BankAccounts.formatCurrencyShort(entry.balance))
        );
    }

    // messages.update-available
    public @NotNull Component messagesUpdateAvailable(final @NotNull String version) {
        return MiniMessage.miniMessage().deserialize(
                Objects.requireNonNull(config.getString("messages.update-available"))
                        .replace("<version>", version)
        );
    }
}
