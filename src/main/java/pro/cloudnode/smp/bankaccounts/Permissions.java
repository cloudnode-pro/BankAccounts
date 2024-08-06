package pro.cloudnode.smp.bankaccounts;

import org.jetbrains.annotations.NotNull;

public final class Permissions {
    public static final @NotNull String COMMAND = "bank.command";
    public static final @NotNull String BALANCE_SELF = "bank.balance.self";
    public static final @NotNull String TRANSFER_SELF = "bank.transfer.self";
    public static final @NotNull String TRANSFER_OTHER = "bank.transfer.other";
    public static final @NotNull String HISTORY = "bank.history";
    public static final @NotNull String ACCOUNT_CREATE = "bank.account.create";
    public static final @NotNull String INSTRUMENT_CREATE = "bank.instrument.create";
    public static final @NotNull String WHOIS = "bank.whois";
    public static final @NotNull String SET_NAME = "bank.set.name";
    public static final @NotNull String FREEZE = "bank.freeze";
    public static final @NotNull String DELETE = "bank.delete";
    public static final @NotNull String BALTOP = "bank.baltop";
    public static final @NotNull String POS_CREATE = "bank.pos.create";
    public static final @NotNull String POS_USE = "bank.pos.use";
    public static final @NotNull String INVOICE_CREATE = "bank.invoice.create";
    public static final @NotNull String INVOICE_CREATE_OTHER = "bank.invoice.create.other";
    public static final @NotNull String INVOICE_VIEW = "bank.invoice.view";
    public static final @NotNull String INVOICE_VIEW_OTHER = "bank.invoice.view.other";
    public static final @NotNull String INVOICE_NOTIFY = "bank.invoice.notify";
    public static final @NotNull String INVOICE_PAY_OTHER = "bank.invoice.pay.other";
    public static final @NotNull String INVOICE_PAY_ACCOUNT_OTHER = "bank.invoice.pay.account-other";
    public static final @NotNull String INVOICE_SEND = "bank.invoice.send";
    public static final @NotNull String INVOICE_SEND_OTHER = "bank.invoice.send.other";
    public static final @NotNull String RELOAD = "bank.reload";
    public static final @NotNull String BALANCE_OTHER = "bank.balance.other";
    public static final @NotNull String TRANSFER_FROM_OTHER = "bank.transfer.from-other";
    public static final @NotNull String HISTORY_OTHER = "bank.history.other";
    public static final @NotNull String ACCOUNT_CREATE_OTHER = "bank.account.create.other";
    public static final @NotNull String ACCOUNT_CREATE_BYPASS = "bank.account.create.bypass";
    public static final @NotNull String ACCOUNT_CREATE_VAULT = "bank.account.create.vault";
    public static final @NotNull String INSTRUMENT_CREATE_OTHER = "bank.instrument.create.other";
    public static final @NotNull String INSTRUMENT_CREATE_BYPASS = "bank.instrument.create.bypass";
    public static final @NotNull String SET_BALANCE = "bank.set.balance";
    public static final @NotNull String SET_NAME_OTHER = "bank.set.name.other";
    public static final @NotNull String SET_NAME_VAULT = "bank.set.name.vault";
    public static final @NotNull String FREEZE_OTHER = "bank.freeze.other";
    public static final @NotNull String DELETE_OTHER = "bank.delete.other";
    public static final @NotNull String DELETE_VAULT = "bank.delete.vault";
    public static final @NotNull String POS_CREATE_OTHER = "bank.pos.create.other";
    public static final @NotNull String POS_CREATE_PERSONAL = "bank.pos.create.personal";
    public static final @NotNull String POS_USE_OTHER = "bank.pos.use.other";
    public static final @NotNull String NOTIFY_UPDATE = "bank.notify-update";
}
