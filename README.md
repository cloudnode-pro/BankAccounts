# Bank Accounts
A multi-account economy plugin.

## Permissions
| Permission                   | Command        | Description                                           | Recommended group |
|------------------------------|----------------|-------------------------------------------------------|-------------------|
| `bank.command`               | `bank`         | Required to use any bank command                      | `default`         |
| `bank.balance.self`          | `balance`      | List your accounts and check own balance              | `default`         |
| `bank.transfer.self`         | `transfer`     | Transfer money between your accounts                  | `default`         |
| `bank.transfer.other`        | `transfer`     | Send money to another player's account                | `default`         |
| `bank.history`               | `transactions` | List transactions for an account you own              | `default`         |
| `bank.account.create`        | `create`       | Create a new account                                  | `default`         |
| `bank.set.name`              | `setname`      | Set the name of an account                            | `default`         |
| `bank.delete`                | `delete`       | Delete an account                                     | `default`         |
| `bank.reload`                | `reload`       | Reload the plugin                                     | `admin`           |
| `bank.account.create.other`  | `create`       | Create a new account for another player               | `admin`           |
| `bank.account.create.bypass` | `create`       | Bypass the maximum account limit                      | `admin`           |
| `bank.set.balance`           | `setbalance`   | Set the balance of an account                         | `admin`           |
| `bank.set.name.other`        | `setname`      | Set the name of an account owned by another player    | `admin`           |
| `bank.set.name.personal`     | `setname`      | Allow renaming personal accounts (not recommended)    | `admin`           |
| `bank.delete.other`          | `delete`       | Delete an account owned by another player             | `admin`           |
| `bank.delete.personal`       | `delete`       | Bypass `prevent-close-personal`                       | `admin`           |
| `bank.history.other`         | `transactions` | List transactions of accounts owned by another player | `admin`           |