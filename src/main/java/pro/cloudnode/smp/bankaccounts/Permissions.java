package pro.cloudnode.smp.bankaccounts;

import org.jetbrains.annotations.NotNull;

public final class Permissions {
    public static @NotNull String COMMAND = "bank.command";
    public static @NotNull String BALANCE_SELF = "bank.balance.self";
    public static @NotNull String TRANSFER_SELF = "bank.transfer.self";
    public static @NotNull String TRANSFER_OTHER = "bank.transfer.other";
    public static @NotNull String HISTORY = "bank.history";
    public static @NotNull String ACCOUNT_CREATE = "bank.account.create";
    public static @NotNull String INSTRUMENT_CREATE = "bank.instrument.create";
    public static @NotNull String WHOIS = "bank.whois";
    public static @NotNull String SET_NAME = "bank.set.name";
    public static @NotNull String FREEZE = "bank.freeze";
    public static @NotNull String DELETE = "bank.delete";
    public static @NotNull String BALTOP = "bank.baltop";
    public static @NotNull String POS_CREATE = "bank.pos.create";
    public static @NotNull String POS_USE = "bank.pos.use";
    public static @NotNull String RELOAD = "bank.reload";
    public static @NotNull String BALANCE_OTHER = "bank.balance.other";
    public static @NotNull String HISTORY_OTHER = "bank.history.other";
    public static @NotNull String ACCOUNT_CREATE_OTHER = "bank.account.create.other";
    public static @NotNull String ACCOUNT_CREATE_BYPASS = "bank.account.create.bypass";
    public static @NotNull String INSTRUMENT_CREATE_OTHER = "bank.instrument.create.other";
    public static @NotNull String INSTRUMENT_CREATE_BYPASS = "bank.instrument.create.bypass";
    public static @NotNull String SET_BALANCE = "bank.set.balance";
    public static @NotNull String SET_NAME_OTHER = "bank.set.name.other";
    public static @NotNull String SET_NAME_PERSONAL = "bank.set.name.personal";
    public static @NotNull String FREEZE_OTHER = "bank.freeze.other";
    public static @NotNull String DELETE_OTHER = "bank.delete.other";
    public static @NotNull String DELETE_PERSONAL = "bank.delete.personal";
    public static @NotNull String POS_CREATE_OTHER = "bank.pos.create.other";
    public static @NotNull String POS_CREATE_PERSONAL = "bank.pos.create.personal";
    public static @NotNull String POS_USE_OTHER = "bank.pos.use.other";
    public static @NotNull String NOTIFY_UPDATE = "bank.notify-update";
}
