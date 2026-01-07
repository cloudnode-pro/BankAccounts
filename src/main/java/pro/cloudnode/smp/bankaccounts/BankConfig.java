package pro.cloudnode.smp.bankaccounts;

import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

final class BankConfig extends BaseConfig {
    public BankConfig(final @NotNull JavaPlugin plugin) {
        super(plugin, "config.yaml");
    }

    public int idLengthAccount() {
        return config.getInt("id-length.account");
    }

    public int idLengthTransaction() {
        return config.getInt("id-length.transaction");
    }
}
