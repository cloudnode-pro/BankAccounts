package pro.cloudnode.smp.bankaccounts.integrations;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;
import pro.cloudnode.smp.bankaccounts.Account;
import pro.cloudnode.smp.bankaccounts.BankAccounts;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public final class VaultIntegration implements Economy {
    private final @NotNull BankAccounts plugin;
    
    /**
     * Constructor
     */
    public VaultIntegration(final @NotNull BankAccounts plugin) {
        this.plugin = plugin;
    }

    /**
     * Check if Vault economy is enabled.
     */
    @Override
    public boolean isEnabled() {
        return BankAccounts.hasVault();
    }

    /**
     * Economy provider name.
     */
    @Override
    public @NotNull String getName() {
        return this.plugin.getPluginMeta().getName();
    }

    /**
     * Whether this provider supports multi-account.
     */
    @Override
    public boolean hasBankSupport() {
        return false;
    }

    /**
     * Number of digits after the decimal point that this provider supports.
     */
    @Override
    public int fractionalDigits() {
        return 2;
    }

    /**
     * Format currency
     */
    @Override
    public @NotNull String format(final double amount) {
        return BankAccounts.formatCurrency(BigDecimal.valueOf(amount));
    }

    /**
     * Currency name
     */
    @Override
    public @NotNull String currencyNamePlural() {
        return BankAccounts.getCurrencySymbol();
    }

    /**
     * Currency name
     */
    @Override
    public @NotNull String currencyNameSingular() {
        return BankAccounts.getCurrencySymbol();
    }

    /**
     * Check if a player has an account.
     *
     * @deprecated Use {@link #hasAccount(OfflinePlayer)}
     */
    @Deprecated
    @Override
    public boolean hasAccount(final @NotNull String player) {
        return hasAccount(this.plugin.getServer().getOfflinePlayer(player));
    }

    /**
     * Check if a player has an account.
     */
    @Override
    public boolean hasAccount(final @NotNull OfflinePlayer player) {
        return Account.count(player, Account.Type.VAULT) > 0;
    }

    /**
     * Check if a player has an account in world.
     *
     * @deprecated Use {@link #hasAccount(OfflinePlayer, String)}
     */
    @Deprecated
    @Override
    public boolean hasAccount(final @NotNull String player, final @NotNull String world) {
        return hasAccount(this.plugin.getServer().getOfflinePlayer(player), world);
    }

    /**
     * Check if a player has an account in world.
     */
    @Override
    public boolean hasAccount(final @NotNull OfflinePlayer player, final @NotNull String world) {
        return Account.count(player, Account.Type.VAULT) > 0;
    }

    /**
     * Get balance of player.
     *
     * @deprecated Use {@link #getBalance(OfflinePlayer)}
     */
    @Deprecated
    @Override
    public double getBalance(final @NotNull String player) {
        return getBalance(this.plugin.getServer().getOfflinePlayer(player));
    }

    /**
     * Get balance of player.
     */
    @Override
    public double getBalance(final @NotNull OfflinePlayer player) {
        final @NotNull Optional<@NotNull Account> account = Account.getVault(player);
        assert account.isPresent();
        return Optional.ofNullable(account.get().balance).map(BigDecimal::doubleValue).orElse(Double.POSITIVE_INFINITY);
    }

    /**
     * Get balance of player in world.
     *
     * @deprecated Use {@link #getBalance(OfflinePlayer, String)}
     */
    @Deprecated
    @Override
    public double getBalance(final @NotNull String player, final @NotNull String world) {
        return getBalance(this.plugin.getServer().getOfflinePlayer(player), world);
    }

    /**
     * Get balance of player in world.
     *
     * @implNote Multi-world is not supported, this will always return the balance of the player, the same in any world.
     */
    @Override
    public double getBalance(final @NotNull OfflinePlayer player, final @NotNull String world) {
        return getBalance(player);
    }

    /**
     * Check if player's balance is greater than the amount specified.
     *
     * @deprecated Use {@link #has(OfflinePlayer, double)}
     */
    @Deprecated
    @Override
    public boolean has(final @NotNull String player, final double amount) {
        return has(this.plugin.getServer().getOfflinePlayer(player), amount);
    }

    /**
     * Check if player's balance is greater than the amount specified.
     */
    @Override
    public boolean has(final @NotNull OfflinePlayer player, final double amount) {
        final @NotNull Optional<@NotNull Account> account = Account.getVault(player);
        assert account.isPresent();
        return account.get().hasFunds(BigDecimal.valueOf(amount));
    }

    /**
     * Check if player's balance in a world is greater than the amount specified.
     *
     * @deprecated
     */
    @Deprecated
    @Override
    public boolean has(final @NotNull String player, final @NotNull String world, final double amount) {
        return has(this.plugin.getServer().getOfflinePlayer(player), world, amount);
    }

    /**
     * Check if player's balance in a world is greater than the amount specified.
     */
    @Override
    public boolean has(final @NotNull OfflinePlayer player, final @NotNull String world, final double amount) {
        return has(player, amount);
    }

    /**
     * Remove amount of money from player.
     *
     * @param amount A positive amount to withdraw.
     * @deprecated Use {@link #withdrawPlayer(OfflinePlayer, double)}
     */
    @Deprecated
    @Override
    public @NotNull EconomyResponse withdrawPlayer(final @NotNull String player, final double amount) {
        return withdrawPlayer(this.plugin.getServer().getOfflinePlayer(player), amount);
    }

    /**
     * Remove amount of money from player.
     *
     * @param amount A positive amount to withdraw.
     */
    @Override
    public @NotNull EconomyResponse withdrawPlayer(final @NotNull OfflinePlayer player, final double amount) {
        final @NotNull Optional<@NotNull Account> account = Account.getVault(player);
        assert account.isPresent();
        if (!account.get().hasFunds(BigDecimal.valueOf(amount)))
            return new EconomyResponse(amount, Optional.ofNullable(account.get().balance).map(BigDecimal::doubleValue).orElse(Double.POSITIVE_INFINITY), EconomyResponse.ResponseType.FAILURE, "Insufficient funds");
        account.get().transfer(Account.getServerAccount().orElse(new Account.ClosedAccount()), BigDecimal.valueOf(amount), null, null);
        return new EconomyResponse(amount, Optional.ofNullable(account.get().balance).map(BigDecimal::doubleValue).orElse(Double.POSITIVE_INFINITY), EconomyResponse.ResponseType.SUCCESS, null);
    }

    /**
     * Remove amount of money from player in world.
     *
     * @param amount A positive amount to withdraw.
     * @deprecated Use {@link #withdrawPlayer(OfflinePlayer, String, double)}
     */
    @Deprecated
    @Override
    public @NotNull EconomyResponse withdrawPlayer(final @NotNull String player, final @NotNull String world, final double amount) {
        return withdrawPlayer(this.plugin.getServer().getOfflinePlayer(player), world, amount);
    }

    /**
     * Remove amount of money from player in world.
     *
     * @param amount A positive amount to withdraw.
     */
    @Override
    public @NotNull EconomyResponse withdrawPlayer(final @NotNull OfflinePlayer player, final @NotNull String world, final double amount) {
        return withdrawPlayer(player, amount);
    }

    /**
     * Add amount of money to player.
     *
     * @param amount A positive amount to deposit.
     * @deprecated Use {@link #depositPlayer(OfflinePlayer, double)}
     */
    @Deprecated
    @Override
    public @NotNull EconomyResponse depositPlayer(final @NotNull String player, final double amount) {
        return depositPlayer(this.plugin.getServer().getOfflinePlayer(player), amount);
    }

    /**
     * Add amount of money to player.
     *
     * @param amount A positive amount to deposit.
     */
    @Override
    public @NotNull EconomyResponse depositPlayer(final @NotNull OfflinePlayer player, final double amount) {
        final @NotNull Optional<@NotNull Account> account = Account.getVault(player);
        assert account.isPresent();
        final @NotNull Account serverAccount = Account.getServerAccount().orElse(new Account.ClosedAccount());
        if (!serverAccount.hasFunds(BigDecimal.valueOf(amount)))
            return new EconomyResponse(amount, Optional.ofNullable(account.get().balance).map(BigDecimal::doubleValue).orElse(Double.POSITIVE_INFINITY), EconomyResponse.ResponseType.FAILURE, "Insufficient funds in server account");
        try {
            serverAccount.transfer(account.get(), BigDecimal.valueOf(amount), null, null);
        } catch (final @NotNull Exception e) {
            return new EconomyResponse(amount, Optional.ofNullable(account.get().balance).map(BigDecimal::doubleValue).orElse(Double.POSITIVE_INFINITY), EconomyResponse.ResponseType.FAILURE, "Failed to transfer funds from server account to player account");
        }
        return new EconomyResponse(amount, Optional.ofNullable(account.get().balance).map(BigDecimal::doubleValue).orElse(Double.POSITIVE_INFINITY), EconomyResponse.ResponseType.SUCCESS, null);
    }

    /**
     * Add amount of money to player in world.
     *
     * @param amount A positive amount to deposit.
     * @deprecated Use {@link #depositPlayer(OfflinePlayer, String, double)}
     */
    @Deprecated
    @Override
    public @NotNull EconomyResponse depositPlayer(final @NotNull String player, final @NotNull String world, final double amount) {
        return depositPlayer(this.plugin.getServer().getOfflinePlayer(player), world, amount);
    }

    /**
     * Add amount of money to player in world.
     *
     * @param amount A positive amount to deposit.
     */
    @Override
    public @NotNull EconomyResponse depositPlayer(final @NotNull OfflinePlayer player, final @NotNull String world, final double amount) {
        return depositPlayer(player, amount);
    }

    /**
     * @deprecated
     */
    @Override
    public EconomyResponse createBank(String s, String s1) {
        return null;
    }

    @Override
    public EconomyResponse createBank(String s, OfflinePlayer offlinePlayer) {
        return null;
    }

    @Override
    public EconomyResponse deleteBank(String s) {
        return null;
    }

    @Override
    public EconomyResponse bankBalance(String s) {
        return null;
    }

    @Override
    public EconomyResponse bankHas(String s, double v) {
        return null;
    }

    @Override
    public EconomyResponse bankWithdraw(String s, double v) {
        return null;
    }

    @Override
    public EconomyResponse bankDeposit(String s, double v) {
        return null;
    }

    @Override
    public EconomyResponse isBankOwner(String s, String s1) {
        return null;
    }

    @Override
    public EconomyResponse isBankOwner(String s, OfflinePlayer offlinePlayer) {
        return null;
    }

    @Override
    public EconomyResponse isBankMember(String s, String s1) {
        return null;
    }

    @Override
    public EconomyResponse isBankMember(String s, OfflinePlayer offlinePlayer) {
        return null;
    }

    @Override
    public List<String> getBanks() {
        return null;
    }

    @Override
    public boolean createPlayerAccount(String s) {
        return false;
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer offlinePlayer) {
        return false;
    }

    @Override
    public boolean createPlayerAccount(String s, String s1) {
        return false;
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer offlinePlayer, String s) {
        return false;
    }
}
