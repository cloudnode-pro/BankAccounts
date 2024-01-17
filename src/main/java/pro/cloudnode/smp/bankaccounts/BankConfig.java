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
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.time.temporal.TemporalAccessor;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
    public final @NotNull Material instrumentsMaterial() {
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
    public @NotNull String interestDescription(final @NotNull Account.Type type) {
        return Objects.requireNonNull(config.getString("interest." + Account.Type.getType(type) + ".description"));
    }

    // messages.command-usage
    public @NotNull String messagesCommandUsage() {
        return Objects.requireNonNull(config.getString("messages.command-usage"));
    }

    // messages.types.
    public @NotNull String messagesTypes(final @NotNull Account.Type type) {
        return Objects.requireNonNull(config.getString("messages.types." + Account.Type.getType(type)));
    }

    // messages.errors.no-accounts
    public @NotNull String messagesErrorsNoAccounts() {
        return Objects.requireNonNull(config.getString("messages.errors.no-accounts"));
    }

    // messages.errors.no-permission
    public @NotNull String messagesErrorsNoPermission() {
        return Objects.requireNonNull(config.getString("messages.errors.no-permission"));
    }

    // messages.errors.account-not-found
    public @NotNull String messagesErrorsAccountNotFound() {
        return Objects.requireNonNull(config.getString("messages.errors.account-not-found"));
    }

    // messages.errors.unknown-command
    public @NotNull String messagesErrorsUnknownCommand() {
        return Objects.requireNonNull(config.getString("messages.errors.unknown-command"));
    }

    // messages.errors.max-accounts
    public @NotNull String messagesErrorsMaxAccounts() {
        return Objects.requireNonNull(config.getString("messages.errors.max-accounts"));
    }

    // messages.errors.rename-personal
    public @NotNull String messagesErrorsRenamePersonal() {
        return Objects.requireNonNull(config.getString("messages.errors.rename-personal"));
    }

    // messages.errors.not-account-owner
    public @NotNull String messagesErrorsNotAccountOwner() {
        return Objects.requireNonNull(config.getString("messages.errors.not-account-owner"));
    }

    // messages.errors.frozen
    public @NotNull String messagesErrorsFrozen() {
        return Objects.requireNonNull(config.getString("messages.errors.frozen"));
    }

    // messages.errors.same-from-to
    public @NotNull String messagesErrorsSameFromTo() {
        return Objects.requireNonNull(config.getString("messages.errors.same-from-to"));
    }

    // messages.errors.transfer-self-only
    public @NotNull String messagesErrorsTransferSelfOnly() {
        return Objects.requireNonNull(config.getString("messages.errors.transfer-self-only"));
    }

    // messages.errors.transfer-other-only
    public @NotNull String messagesErrorsTransferOtherOnly() {
        return Objects.requireNonNull(config.getString("messages.errors.transfer-other-only"));
    }

    // messages.errors.invalid-number
    public @NotNull String messagesErrorsInvalidNumber() {
        return Objects.requireNonNull(config.getString("messages.errors.invalid-number"));
    }

    // messages.errors.negative-transfer
    public @NotNull String messagesErrorsNegativeTransfer() {
        return Objects.requireNonNull(config.getString("messages.errors.negative-transfer"));
    }

    // messages.errors.insufficient-funds
    public @NotNull String messagesErrorsInsufficientFunds() {
        return Objects.requireNonNull(config.getString("messages.errors.insufficient-funds"));
    }

    // messages.errors.closing-balance
    public @NotNull String messagesErrorsClosingBalance() {
        return Objects.requireNonNull(config.getString("messages.errors.closing-balance"));
    }

    // messages.errors.closing-personal
    public @NotNull String messagesErrorsClosingPersonal() {
        return Objects.requireNonNull(config.getString("messages.errors.closing-personal"));
    }

    // messages.errors.player-only
    public @NotNull String messagesErrorsPlayerOnly() {
        return Objects.requireNonNull(config.getString("messages.errors.player-only"));
    }

    // messages.errors.player-not-found
    public @NotNull String messagesErrorsPlayerNotFound() {
        return Objects.requireNonNull(config.getString("messages.errors.player-not-found"));
    }

    // messages.errors.instrument-requires-item
    public @NotNull String messagesErrorsInstrumentRequiresItem() {
        return Objects.requireNonNull(config.getString("messages.errors.instrument-requires-item"));
    }

    // messages.errors.target-inventory-full
    public @NotNull String messagesErrorsTargetInventoryFull() {
        return Objects.requireNonNull(config.getString("messages.errors.target-inventory-full"));
    }

    // messages.errors.block-too-far
    public @NotNull String messagesErrorsBlockTooFar() {
        return Objects.requireNonNull(config.getString("messages.errors.block-too-far"));
    }

    // messages.errors.pos-already-exists
    public @NotNull String messagesErrorsPosAlreadyExists() {
        return Objects.requireNonNull(config.getString("messages.errors.pos-already-exists"));
    }

    // messages.errors.pos-not-chest
    public @NotNull String messagesErrorsPosNotChest() {
        return Objects.requireNonNull(config.getString("messages.errors.pos-not-chest"));
    }

    // messages.errors.pos-double-chest
    public @NotNull String messagesErrorsPosDoubleChest() {
        return Objects.requireNonNull(config.getString("messages.errors.pos-double-chest"));
    }

    // messages.errors.pos-empty
    public @NotNull String messagesErrorsPosEmpty() {
        return Objects.requireNonNull(config.getString("messages.errors.pos-empty"));
    }

    // messages.errors.pos-invalid-card
    public @NotNull String messagesErrorsPosInvalidCard() {
        return Objects.requireNonNull(config.getString("messages.errors.pos-invalid-card"));
    }

    // messages.errors.pos-no-permission
    public @NotNull String messagesErrorsPosNoPermission() {
        return Objects.requireNonNull(config.getString("messages.errors.pos-no-permission"));
    }

    // messages.errors.no-card
    public @NotNull String messagesErrorsNoCard() {
        return Objects.requireNonNull(config.getString("messages.errors.no-card"));
    }

    // messages.errors.pos-items-changed
    public @NotNull String messagesErrorsPosItemsChanged() {
        return Objects.requireNonNull(config.getString("messages.errors.pos-items-changed"));
    }

    // messages.errors.pos-create-business-only
    public @NotNull String messagesErrorsPosCreateBusinessOnly() {
        return Objects.requireNonNull(config.getString("messages.errors.pos-create-business-only"));
    }

    // messages.errors.disallowed-characters
    public @NotNull String messagesErrorsDisallowedCharacters() {
        return Objects.requireNonNull(config.getString("messages.errors.disallowed-characters"));
    }

    // messages.errors.already-frozen
    public @NotNull String messagesErrorsAlreadyFrozen() {
        return Objects.requireNonNull(config.getString("messages.errors.already-frozen"));
    }

    // messages.errors.not-frozen
    public @NotNull String messagesErrorsNotFrozen() {
        return Objects.requireNonNull(config.getString("messages.errors.not-frozen"));
    }

    // messages.balance
    public @NotNull String messagesBalance() {
        return Objects.requireNonNull(config.getString("messages.balance"));
    }

    // messages.list-accounts.header
    public @NotNull String messagesListAccountsHeader() {
        return Objects.requireNonNull(config.getString("messages.list-accounts.header"));
    }

    // messages.list-accounts.entry
    public @NotNull String messagesListAccountsEntry() {
        return Objects.requireNonNull(config.getString("messages.list-accounts.entry"));
    }

    // messages.reload
    public @NotNull String messagesReload() {
        return Objects.requireNonNull(config.getString("messages.reload"));
    }

    // messages.account-created
    public @NotNull String messagesAccountCreated() {
        return Objects.requireNonNull(config.getString("messages.account-created"));
    }

    // messages.balance-set
    public @NotNull String messagesBalanceSet() {
        return Objects.requireNonNull(config.getString("messages.balance-set"));
    }

    // messages.name-set
    public @NotNull String messagesNameSet() {
        return Objects.requireNonNull(config.getString("messages.name-set"));
    }

    // messages.account-frozen
    public @NotNull String messagesAccountFrozen() {
        return Objects.requireNonNull(config.getString("messages.account-frozen"));
    }

    // messages.account-unfrozen
    public @NotNull String messagesAccountUnfrozen() {
        return Objects.requireNonNull(config.getString("messages.account-unfrozen"));
    }

    // messages.account-deleted
    public @NotNull String messagesAccountDeleted() {
        return Objects.requireNonNull(config.getString("messages.account-deleted"));
    }

    // messages.confirm-transfer
    public @NotNull String messagesConfirmTransfer() {
        return Objects.requireNonNull(config.getString("messages.confirm-transfer"));
    }

    // messages.transfer-sent
    public @NotNull String messagesTransferSent() {
        return Objects.requireNonNull(config.getString("messages.transfer-sent"));
    }

    // messages.transfer-received
    public @NotNull String messagesTransferReceived() {
        return Objects.requireNonNull(config.getString("messages.transfer-received"));
    }

    // messages.history.header
    public @NotNull String messagesHistoryHeader() {
        return Objects.requireNonNull(config.getString("messages.history.header"));
    }

    // messages.history.entry
    public @NotNull String messagesHistoryEntry() {
        return Objects.requireNonNull(config.getString("messages.history.entry"));
    }

    // messages.history.footer
    public @NotNull String messagesHistoryFooter() {
        return Objects.requireNonNull(config.getString("messages.history.footer"));
    }

    // messages.history.no-transactions
    public @NotNull String messagesHistoryNoTransactions() {
        return Objects.requireNonNull(config.getString("messages.history.no-transactions"));
    }

    // messages.instrument-created
    public @NotNull String messagesInstrumentCreated() {
        return Objects.requireNonNull(config.getString("messages.instrument-created"));
    }

    // messages.pos-removed
    public @NotNull String messagesPosCreated() {
        return Objects.requireNonNull(config.getString("messages.pos-created"));
    }

    // messages.pos-removed
    public @NotNull String messagesPosRemoved() {
        return Objects.requireNonNull(config.getString("messages.pos-removed"));
    }

    // messages.pos-purchase
    public @NotNull String messagesPosPurchase() {
        return Objects.requireNonNull(config.getString("messages.pos-purchase"));
    }

    // messages.pos-purchase.seller
    public @NotNull String messagesPosPurchaseSeller() {
        return Objects.requireNonNull(config.getString("messages.pos-purchase.seller"));
    }

    // messages.whois
    public @NotNull String messagesWhois() {
        return Objects.requireNonNull(config.getString("messages.whois"));
    }

    // messages.baltop.header
    public @NotNull String messagesBaltopHeader() {
        return Objects.requireNonNull(config.getString("messages.baltop.header"));
    }

    // messages.baltop.entry
    public @NotNull String messagesBaltopEntry() {
        return Objects.requireNonNull(config.getString("messages.baltop.entry"));
    }

    // messages.baltop.entry.player
    public @NotNull String messagesBaltopEntryPlayer() {
        return Objects.requireNonNull(config.getString("messages.baltop.entry-player"));
    }

    // messages.update-available
    public @NotNull String messagesUpdateAvailable() {
        return Objects.requireNonNull(config.getString("messages.update-available"));
    }
}
