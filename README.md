[![Banner](https://i.ibb.co/B338K1f/banner.png)](https://modrinth.com/plugin/Dc8RS2En/)

# Bank Accounts
[![CodeQL](https://github.com/cloudnode-pro/BankAccounts/actions/workflows/codeql.yml/badge.svg)](https://github.com/cloudnode-pro/BankAccounts/actions/workflows/codeql.yml)
[![Modrinth](https://img.shields.io/badge/Modrinth-%2326292f?logo=modrinth)](https://modrinth.com/plugin/Dc8RS2En/)
[![Version](https://img.shields.io/modrinth/v/Dc8RS2En)](https://modrinth.com/plugin/Dc8RS2En/)
[![Game Versions](https://img.shields.io/modrinth/game-versions/Dc8RS2En)](https://modrinth.com/plugin/Dc8RS2En/)
[![Downloads](https://img.shields.io/modrinth/dt/Dc8RS2En)](https://modrinth.com/plugin/Dc8RS2En/)

A multi-account economy plugin.

## Commands
| Command                                               | Description                              | Aliases                      |
|-------------------------------------------------------|------------------------------------------|------------------------------|
| `/bank`                                               | Show plugin version                      |                              |
| `/bank balance [account]`                             | List accounts or show balance of account | `bal`, `account`, `accounts` |
| `/bank balance --player <player>`                     | List accounts of another player          | `bal`, `account`, `accounts` |
| `/bank transfer <from> <to> <amount> [description]`   | Move money to another account            | `send`, `pay`                |
| `/bank transactions <account> [page=1\|--all]`        | List transactions                        | `history`                    |
| `/bank create <PERSONAL\|BUSINESS>`                   | Create a new account                     | `new`                        |
| `/bank create <PERSONAL\|BUSINESS> --player <player>` | Create an account for another player     | `new`                        |
| `/bank instrument <account>`                          | Create instrument (bank card)            | `card`                       |
| `/bank delete <account>`                              | Delete an account                        |                              |
| `/bank setbalance <account> <balance\|Infinity>`      | Set the balance of an account            | `setbal`                     |
| `/bank setname <account> [name]`                      | Set an account's name                    | `rename`                     |
| `/bank reload`                                        | Reload configuration                     |                              |
| `/pos <account> <price> [description]`                | Create a new POS                         |                              |

## Permissions
| Permission                      | Command        | Description                                                                               | Recommended group |
|---------------------------------|----------------|-------------------------------------------------------------------------------------------|-------------------|
| `bank.command`                  | `bank`         | Required to use any bank command                                                          | `default`         |
| `bank.balance.self`             | `balance`      | List your accounts and check own balance                                                  | `default`         |
| `bank.transfer.self`            | `transfer`     | Transfer money between your accounts                                                      | `default`         |
| `bank.transfer.other`           | `transfer`     | Send money to another player's account                                                    | `default`         |
| `bank.history`                  | `transactions` | List transactions for an account you own                                                  | `default`         |
| `bank.account.create`           | `create`       | Create a new account                                                                      | `default`         |
| `bank.instrument.create`        | `instrument`   | Create an instrument (bank card)                                                          | `default`         |
| `bank.set.name`                 | `setname`      | Set the name of an account                                                                | `default`         |
| `bank.delete`                   | `delete`       | Delete an account                                                                         | `default`         |
| `bank.pos.create`               | `pos`          | Create a new POS                                                                          | `default`         |
| `bank.reload`                   | `reload`       | Reload the plugin                                                                         | `admin`           |
| `bank.account.create.other`     | `create`       | Create a new account for another player                                                   | `admin`           |
| `bank.account.create.bypass`    | `create`       | Bypass the maximum account limit                                                          | `admin`           |
| `bank.instrument.create.other`  | `instrument`   | Create an instrument for account you don't own. Also allows giving cards to other players | `admin`           |
| `bank.instrument.create.bypass` | `instrument`   | Bypass the configuration option `instruments.require-item`                                | `admin`           |
| `bank.set.balance`              | `setbalance`   | Set the balance of an account                                                             | `admin`           |
| `bank.set.name.other`           | `setname`      | Set the name of an account owned by another player                                        | `admin`           |
| `bank.set.name.personal`        | `setname`      | Allow renaming personal accounts (not recommended)                                        | `admin`           |
| `bank.delete.other`             | `delete`       | Delete an account owned by another player                                                 | `admin`           |
| `bank.delete.personal`          | `delete`       | Bypass `prevent-close-last-personal`                                                      | `admin`           |
| `bank.pos.create.other`         | `pos`          | Create a new POS for another player's account                                             | `admin`           |
| `bank.history.other`            | `transactions` | List transactions of accounts owned by another player                                     | `admin`           |
