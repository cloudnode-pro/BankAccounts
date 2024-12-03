[<img src="https://wsrv.nl/?url=https://raw.githubusercontent.com/cloudnode-pro/BankAccounts/master/icon.svg&w=128&output=webp" alt="BankAccounts logo" align="left">](https://modrinth.com/plugin/Dc8RS2En)

# Bank Accounts

[![Available on Modrinth](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/compact/available/modrinth_vector.svg)](https://modrinth.com/plugin/Dc8RS2En)
[![Available for Paper](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/compact/supported/paper_vector.svg)](https://papermc.io/software/paper)
[![Available on GitHub](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/compact/available/github_vector.svg)](https://github.com/cloudnode-pro/BankAccounts/)

[![Version](https://img.shields.io/modrinth/v/Dc8RS2En)](https://modrinth.com/plugin/Dc8RS2En/version/latest)
[![Game Versions](https://img.shields.io/badge/game_versions-1.20.x-blue)](https://modrinth.com/plugin/Dc8RS2En/)
[![Downloads](https://img.shields.io/modrinth/dt/Dc8RS2En)](https://modrinth.com/plugin/Dc8RS2En/)
[![Licence: GPL-3.0](https://img.shields.io/github/license/cloudnode-pro/BankAccounts)](https://github.com/cloudnode-pro/BankAccounts/blob/master/LICENSE)

A Minecraft economy plugin that enables players to hold multiple bank accounts.

### [<kbd>**Download**</kbd>](https://modrinth.com/plugin/Dc8RS2En/version/latest)

## Features

**See also**:
[Commands](https://github.com/cloudnode-pro/BankAccounts/wiki/Commands) ·
[Permissions](https://github.com/cloudnode-pro/BankAccounts/wiki/Permissions)

### Multiple Accounts & Balances

Players can manage multiple independent bank accounts, each with its own balance.

### Transaction History

All bank transactions are logged, allowing players to review their full balance history.

### Payment Requests (Invoices)

Players can create invoices to request payments from others and track the status of these payments.

### Vault Integration

BankAccounts functions as a Vault economy provider, making it compatible with third-party plugins such as chest shops,
quests, and more.

### Point of Sale (POS) & Bank Cards

Create a [Point of Sale](https://github.com/cloudnode-pro/BankAccounts/wiki/POS), a special type of single-use chest
shop. Players can use bank cards (`/bank card`) to purchase all items available in the POS chest.

### Full Customisation

All features and messages are fully customisable. Check out
the [default config](https://github.com/cloudnode-pro/BankAccounts/blob/master/src/main/resources/config.yml) to explore
available options.

## Support

| Matrix | [bankaccounts:cloudnode.pro](https://matrix.to/#/#bankaccounts:cloudnode.pro) |
|--------|-------------------------------------------------------------------------------|

Join our dedicated BankAccounts community space on Matrix to engage in discussions, receive support, and stay updated on
crucial plugin announcements.

## Commands

The following are all commands provided by BankAccounts. Only the permissions related to using the commands are shown;
to see the full list of permissions (including those not related to commands), refer to the Permissions section instead.

<details> 

## `/bank`

The primary BankAccounts command. Without any arguments, it displays information about the plugin,
such as the currently running version.

Required permission: `bank.command` (for all BankAccounts commands)

   ***

### `/bank help`

Shows all BankAccounts commands that you have permissions to use.

   ***

### `/bank balance`

Lists all of your accounts and their balances. If you only have one account, it will show the balance of that account.

| Permission          | Description | Recommended for |
|---------------------|-------------|-----------------|
| `bank.balance.self` | Required    | Everyone        |

Aliases: `/bank bal`, `/bank account`, `/bank accounts`

   ***

#### `/bank balance <account>`

Shows the balance of the requested account.

| Argument  |          | Type       |
|-----------|----------|------------|
| `account` | Required | Account ID |

| Permission           | Description                                   | Recommended for |
|----------------------|-----------------------------------------------|-----------------|
| `bank.balance.self`  | To see the balances of accounts you own       | Everyone        |
| `bank.balance.other` | To see the balances of accounts you don’t own | Admins          |

   ***

#### `/bank balance --player <username>`

Lists the accounts of the specified player. If they only have one account, the balance of that account will be shown.

| Argument   |          | Type            |
|------------|----------|-----------------|
| `username` | Required | Player Username |

| Permission           | Description |
|----------------------|-------------|
| `bank.balance.other` | Required    |

   ***

### `/bank transfer <from> <to> <amount> [description]`

Send money to another account.

| Argument      |          | Type       | Description                                                 |
|---------------|----------|------------|-------------------------------------------------------------|
| `from`        | Required | Account ID | One of your accounts from which to send the money           |
| `to`          | Required | Account ID | The account that is to receive the money                    |
| `amount`      | Required | Number > 0 | The amount of money to transfer                             |
| `description` | Optional | Text ≤ 64  | Transfer description (visible to both sender and recipient) |

| Permission            | Description                           | Recommended for |
|-----------------------|---------------------------------------|-----------------|
| `bank.transfer.self`  | To transfer to accounts you own       | Everyone        |
| `bank.transfer.other` | To transfer to accounts you don’t own | Everyone        |

Aliases: `/bank send`, `/bank pay`

   ***

### `/bank history <account> [page]`

Shows the transactions history for the requested account.

| Argument  |          | Type        | Description                                        |
|-----------|----------|-------------|----------------------------------------------------|
| `account` | Required | Account ID  |                                                    |
| `page`    | Optional | Integer ≥ 1 | Page number to show or `--all` to show everything. |

| Permission           | Description                                          | Recommended for |
|----------------------|------------------------------------------------------|-----------------|
| `bank.history`       | Required. For seeing your accounts’ transactions     | Everyone        |
| `bank.history.other` | To see transaction history of accounts you don’t own | Admins          |

Aliases: `/bank transactions`

   ***

### `/bank create`

This command is used for creating/opening new bank accounts.

| Account type | Description                                           |
|--------------|-------------------------------------------------------|
| `PERSONAL`   | For personal/private/individual bank accounts         |
| `BUSINESS`   | For businesses, companies, organisations, shops, etc. |

Both account types are functionally identical.

The account type `VAULT` is a special account type used for integration with the Vault plugin.
If Vault is installed, and its integration is enabled in the BankAccounts config,
when other Vault-compatible plugins attempt to access money of a player, they will see the player’s Vault account.

The format `@username` can be used as a valid substitution of a player’s Vault Account ID, e.g.:
`/bank balance @Player123` will show the Vault account balance of Player123.

| Permission            | Description                    | Recommended for |
|-----------------------|--------------------------------|-----------------|
| `bank.account.create` | Required for all sub-commands. | Everyone        |

Aliases: `/bank new`

   ***

#### `/bank create <type>`

Creates a new account owned by you.

| Argument |          | Type         |
|----------|----------|--------------|
| `type`   | Required | Account Type |

| Permission                   | Description                                          | Recommended for |
|------------------------------|------------------------------------------------------|-----------------|
| `bank.account.create.bypass` | Bypass the maximum number of accounts limit          | Admins          |
| `bank.account.create.vault`  | Create Vault integration accounts (not recommended!) | Expert Admins   |

   ***

#### `/bank create <type> --player <username>`

Creates a new account for another player.

| Argument   |          | Type            |
|------------|----------|-----------------|
| `type`     | Required | Account Type    |
| `username` | Required | Player Username |

| Permission                   | Description                                          | Recommended for |
|------------------------------|------------------------------------------------------|-----------------|
| `bank.account.create.other`  | Required                                             | Admins          |
| `bank.account.create.bypass` | Bypass the maximum number of accounts limit          | Admins          |
| `bank.account.create.vault`  | Create Vault integration accounts (not recommended!) | Expert Admins   |

   ***

### `/bank setname <account> [name]`

Set the name of an account.

| Argument  |          | Type       | Description                                   |
|-----------|----------|------------|-----------------------------------------------|
| `account` | Required | Account ID |                                               |
| `name`    | Optional | Text ≤ 32  | If not provided, the account name is cleared. |

| Permission            | Description                                                 | Recommended for |
|-----------------------|-------------------------------------------------------------|-----------------|
| `bank.set.name`       | Required. For renaming your own accounts                    | Everyone        |
| `bank.set.name.other` | For renaming accounts you don’t own                         | Admins          |
| `bank.set.name.vault` | For renaming Vault integration accounts (not recommended!)* | Expert Admins   |

\* When a name is not set, Vault accounts will automatically use the current username of their owner.

Aliases: `/bank rename`

   ***

### `/bank setbalance <account> <balance>`

Sets the account balance to provided value, without recording a transactions.

| Argument  |          | Type       | Description                                                          |
|-----------|----------|------------|----------------------------------------------------------------------|
| `account` | Required | Account ID |                                                                      |
| `balance` | Required | Number     | The new balance value, or `Infinity` to make the balance ∞ infinite. |

| Permission         | Description                                        | Recommended for |
|--------------------|----------------------------------------------------|-----------------|
| `bank.set.balance` | Required. For setting the balance of *any* account | Admins          |

Aliases: `/bank setbal`

   ***

### `/bank whois <account>`

Shows information about the account, such as account name, owner and type, but not balance.

This command allows checking any account and the tab-completion will provide the IDs of all accounts on the server.

| Argument  |          | Type       |
|-----------|----------|------------|
| `account` | Required | Account ID |

| Permission   | Description | Recommended for |
|--------------|-------------|-----------------|
| `bank.whois` | Required    | Everyone        |

Aliases: `/bank who`, `/bank info`

   ***

### `/bank delete <account>`

Permanently delete a bank account.

| Argument  |          | Type       |
|-----------|----------|------------|
| `account` | Required | Account ID |

| Permission          | Description                                          | Recommended for |
|---------------------|------------------------------------------------------|-----------------|
| `bank.delete`       | Required. For deleting your own accounts             | Everyone        |
| `bank.delete.other` | For deleting accounts you don’t own                  | Admins          |
| `bank.delete.vault` | Delete Vault integration accounts (not recommended!) | Expert Admins   |

   ***

### `/bank instrument <account> [player]`

Create a bank card.

| Argument  |          | Type            | Description                                                                                                                                                |
|-----------|----------|-----------------|------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `account` | Required | Account ID      | The account for which to create a card                                                                                                                     |
| `player`  | Optional | Player Username | The player who is given the card. Note: setting this argument requires a permission. This argument is required when running from console or command block. |

| Permission                      | Description                                                                       | Recommended for |
|---------------------------------|-----------------------------------------------------------------------------------|-----------------|
| `bank.instrument.create`        | Required. For creating cards for your own accounts                                | Everyone        |
| `bank.instrument.create.other`  | For creating cards for accounts you don’t own and for using the `player` argument | Admins          |
| `bank.instrument.create.bypass` | For bypassing/ignoring the configuration option `instruments.require-item`        | Admins          |

   ***

### `/bank freeze <account>`

Freezes a bank account. Frozen bank accounts cannot send and receive transactions, create POS, or be deleted.

| Argument  |          | Type       |
|-----------|----------|------------|
| `account` | Required | Account ID |

| Permission          | Description                                    | Recommended for |
|---------------------|------------------------------------------------|-----------------|
| `bank.freeze`       | Required. Note: this also allows unfreezing.   | Admins          |
| `bank.freeze.other` | For freezing/unfreezing accounts you don’t own | Admins          |

Aliases: `/bank disable`, `/bank block`

   ***

### `/bank unfreeze <account>`

Unfreezes a bank account.

| Argument  |          | Type       |
|-----------|----------|------------|
| `account` | Required | Account ID |

| Permission          | Description                                    | Recommended for |
|---------------------|------------------------------------------------|-----------------|
| `bank.freeze`       | Required. Note: this also allows freezing.     | Admins          |
| `bank.freeze.other` | For freezing/unfreezing accounts you don’t own | Admins          |

Aliases: `/bank enable`, `/bank unblock`

   ***

### `/bank reload`

Reload the plugin configuration.

Note: Updating the plugin version or changing the value of `integrations.vault.enabled` requires a server restart.

| Permission    | Description | Recommended for |
|---------------|-------------|-----------------|
| `bank.reload` | Required    | Admins          |

   ***

## `/baltop [type] [page]`

See the top balances leaderboard

| Baltop Type | Description                                      |
|-------------|--------------------------------------------------|
| `personal`  | Show only accounts of type `PERSONAL`            |
| `business`  | Show only accounts of type `BUSINESS`            |
| `player`    | Rank players by the sum of all of their accounts |

| Argument |          | Type        |
|----------|----------|-------------|
| `type`   | Optional | Baltop Type |
| `page`   | Optional | Integer ≥ 1 |

| Permission    | Description | Recommended for |
|---------------|-------------|-----------------|
| `bank.baltop` | Required    | Everyone        |

Aliases: `/bank baltop`

   ***

## `/pos <account> <price> [description]`

This command is used for creating a POS. To create a POS:

1. Place a chest
2. Put the items in the chest
3. Run this command

| Argument      |          | Type       | Description                                                                        |
|---------------|----------|------------|------------------------------------------------------------------------------------|
| `account`     | Required | Account ID | The account to which the money is sent after a successful sale                     |
| `price`       | Required | Number > 0 | The price the buyer must pay to obtain the items                                   |
| `description` | Optional | Text ≤ 64  | A description visible to both parties and also used as the transaction description |

| Permission                 | Description                                  | Recommended for |
|----------------------------|----------------------------------------------|-----------------|
| `bank.pos.create`          | Required                                     | Everyone        |
| `bank.pos.create.personal` | For creating POS using personal account      |                 |
| `bank.pos.create.other`    | For creating POS using account you don’t own | Admins          |

   ***

## `/invoice`

The command used for managing invoices.

   ***

### `/invoice help`

Shows all invoice commands that you have permissions to use.

   ***

### `/invoice create`

Create an invoice

| Permission                  | Description                                                 | Recommended for |
|-----------------------------|-------------------------------------------------------------|-----------------|
| `bank.invoice.create`       | Required. For creating invoices from accounts you own       | Everyone        |
| `bank.invoice.create.other` | Required. For creating invoices from accounts you don’t own | Admins          |

Aliases: `/invoice new`

   ***

#### `/invoice create <account> <amount> [description]`

Create an invoice that anyone can pay.

| Argument      |          | Type       | Description                                                                        |
|---------------|----------|------------|------------------------------------------------------------------------------------|
| `account`     | Required | Account ID | The account to which the money is sent when the invoice is paid                    |
| `amount`      | Required | Number > 0 | The invoice amount to be paid                                                      |
| `description` | Optional | Text ≤ 64  | A description visible to both parties and also used in the transaction description |

   ***

#### `/invoice create <account> <amount> [description] --player <username>`

Create an invoice that only the specified player can pay (from any account they own). When creating an invoice this way,
the recipient player will receive notifications to remind them of unpaid invoices.

| Argument      |          | Type            | Description                                                                        |
|---------------|----------|-----------------|------------------------------------------------------------------------------------|
| `account`     | Required | Account ID      | The account to which the money is sent when the invoice is paid                    |
| `amount`      | Required | Number > 0      | The invoice amount to be paid                                                      |
| `description` | Optional | Text ≤ 64       | A description visible to both parties and also used in the transaction description |
| `username`    | Required | Player Username | The username of the player for whom the invoice is intended                        |

   ***


</details>

## Reporting Issues and Feature Requests

We prioritise resolving problems and bugs to ensure the best experience for our users.
If you encounter a problem or have a feature request, please follow these guidelines:

1. **Search for Existing Issues**: Before opening a new issue,
   check our [issue tracker](https://github.com/cloudnode-pro/BankAccounts/issues) for similar reports.
   You can specifically search for bugs by using this link:
   [Reported Bugs](https://github.com/cloudnode-pro/BankAccounts/issues?q=is:issue+label:bug).
2. **Open a New Issue**: If you don’t find an existing report,
   please [open a new issue](https://github.com/cloudnode-pro/BankAccounts/issues/new/choose).
3. **Provide Details**: When submitting a new issue, include as much relevant information as possible, such as:
    - Server type and version, e.g. Paper 1.21.1
    - Steps to reproduce the problem
    - Errors and warnings logged in the server console

Please note that only the **latest version** of BankAccounts is supported.
If you’re experiencing a problem with an older version, please update to the latest release,
as a fix might already be available in that version.

**See also**: [Security Policy](https://github.com/cloudnode-pro/BankAccounts/blob/master/SECURITY.md)

## New Versions and Releases

BankAccounts follows [semantic versioning](https://semver.org/). Furthermore, new versions are released on **Tuesdays**.
Upcoming releases and other important information is announced on Matrix in the
[News channel](https://matrix.to/#/!qSqOKrcoKdvKBEgVoA:cloudnode.pro?via=cloudnode.pro).

If bug fixes or security-related issues have been resolved, a new version can be released on any day of the week.

Official release builds from the maintainers of BankAccounts are available via
[Modrinth](https://modrinth.com/plugin/Dc8RS2En/versions) and
[GitHub Releases](https://github.com/cloudnode-pro/BankAccounts/releases).

## Licence

BankAccounts is free, libre, open-source software, licensed under
the [GNU General Public License v3.0](https://github.com/cloudnode-pro/BankAccounts/blob/master/LICENSE).

## Contributing

Contributions of any kind are most welcome! Please check out
our [Contributing Guide](https://github.com/cloudnode-pro/BankAccounts/blob/master/CONTRIBUTING.md) for more
information.

## Thanks To

[<img src="https://github.com/JetBrains/logos/raw/master/web/jetbrains/jetbrains-simple.svg" alt="JetBrains logo" height="96" align="left">](https://www.jetbrains.com)

[JetBrains](https://www.jetbrains.com/), creators of the IntelliJ IDEA, supports BankAccounts with one of their Open
Source Licences. IntelliJ IDEA is the recommended IDE for working with BankAccounts.
