[<img src="https://wsrv.nl/?url=https://raw.githubusercontent.com/cloudnode-pro/BankAccounts/master/icon.svg&w=128&output=webp" alt="BankAccounts logo" align="left">](https://modrinth.com/plugin/Dc8RS2En)

# Bank Accounts
[![Available on Modrinth](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/compact/available/modrinth_vector.svg)](https://modrinth.com/plugin/Dc8RS2En) [![Available for Paper](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/compact/supported/paper_vector.svg)](https://papermc.io/software/paper) [![Available on GitHub](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/compact/available/github_vector.svg)](https://github.com/cloudnode-pro/BankAccounts/)

[![Java CI with Maven](https://github.com/cloudnode-pro/BankAccounts/actions/workflows/maven.yml/badge.svg)](https://github.com/cloudnode-pro/BankAccounts/actions/workflows/maven.yml) [![CodeQL](https://github.com/cloudnode-pro/BankAccounts/actions/workflows/codeql.yml/badge.svg)](https://github.com/cloudnode-pro/BankAccounts/actions/workflows/codeql.yml) [![Version](https://img.shields.io/modrinth/v/Dc8RS2En)](https://modrinth.com/plugin/Dc8RS2En/version/latest) [![Game Versions](https://img.shields.io/modrinth/game-versions/Dc8RS2En)](https://modrinth.com/plugin/Dc8RS2En/) [![Downloads](https://img.shields.io/modrinth/dt/Dc8RS2En)](https://modrinth.com/plugin/Dc8RS2En/)

A Minecraft economy plugin that enables players to hold multiple bank accounts.

[**`Download`**](https://modrinth.com/plugin/Dc8RS2En/version/latest)

## Features

See also: [Commands](https://github.com/cloudnode-pro/BankAccounts/wiki/Commands) · [Permissions](https://github.com/cloudnode-pro/BankAccounts/wiki/Permissions)

### Multiple Accounts/Balances
Players can have multiple independent bank accounts. You can have both personal and business accounts.

### Transaction History
See the history of your account’s transactions using the `/bank history` command.

### Payment Requests (Invoices)
Request money from players and track the payment status.

### Vault Support

If [Vault](https://github.com/MilkBowl/Vault/releases/latest) is installed on your server,
you can [enable its integration in the configuration](https://github.com/cloudnode-pro/BankAccounts/blob/dad253525b6bc3ee9647cd01c75e2c425a921f58/src/main/resources/config.yml#L38-L44).
This allows BankAccounts to function as a Vault economy provider,
enabling compatibility with third-party plugins that support Vault.

### POS and Bank Cards

You can create a [Point of Sale](https://github.com/cloudnode-pro/BankAccounts/wiki/POS), which is a type of single-use chest shop. Players can pay using a bank card (`/bank card`).

### PlaceholderAPI Support

BankAccounts provides several *PlaceholderAPI* placeholders that you can use. See
the [Placeholders Wiki](https://github.com/cloudnode-pro/BankAccounts/wiki/Placeholders) for an exhaustive list.

### Extensive Configuration

All functionality is fully configurable. See [default config](https://github.com/cloudnode-pro/BankAccounts/blob/master/src/main/resources/config.yml).

## Support

| Matrix | [bankaccounts:cloudnode.pro](https://matrix.to/#/#bankaccounts:cloudnode.pro) |
|--------|-------------------------------------------------------------------------------|

Join our dedicated BankAccounts community space on Matrix to engage in discussions, receive support, and stay updated on crucial plugin announcements.

If you want to report a problem or request a feature, please [submit a new issue](https://github.com/cloudnode-pro/BankAccounts/issues/new?labels=bug).

## Release Cycle

BankAccounts follows a weekly **time-based release schedule**, with new features or changes typically released every **Tuesday**.

When we merge critical bug fixes, we may publish out-of-band releases on any day of the week.

## Report Issues
Please ensure that you are using the [latest version](https://modrinth.com/plugin/Dc8RS2En/version/latest) of BankAccounts. The newest bug fixes are only available in the most recent version, and support is provided exclusively for this version.

If you encounter any problems with the plugin, please first check the [list of known issues](https://github.com/cloudnode-pro/BankAccounts/issues?q=is%3Aopen+is%3Aissue+label%3Abug) on our GitHub repository. If you don’t find a similar fault listed there, we encourage you to [submit a new issue](https://github.com/cloudnode-pro/BankAccounts/issues/new?labels=bug). Resolving bugs is the highest priority for this project.

To help us resolve your issue as quickly as possible, please provide as much relevant information as possible, including error logs, screenshots, and detailed steps to reproduce the problem.

## Feature Requests

To suggest a new feature, please [create a new issue](https://github.com/cloudnode-pro/BankAccounts/issues/new), providing a detailed description of your idea.

## Contributing

New contributors are most welcome to the project.

If you're interested in contributing, follow these steps:

1.  [Fork the repository](https://github.com/cloudnode-pro/BankAccounts/fork)
2.  Create a new branch for your contributions.
3.  Make your changes and ensure they align with the project’s goals.
4.  Commit your changes with clear and descriptive messages.
5.  Push your changes to your fork.
6.  Submit a pull request.

## Thanks To

[<img src="https://github.com/JetBrains/logos/raw/master/web/jetbrains/jetbrains-simple.svg" alt="JetBrains logo" height="96" align="left">](https://www.jetbrains.com)

[JetBrains](https://www.jetbrains.com/), creators of the IntelliJ IDEA, supports BankAccounts with one of their [Open Source Licenses](https://www.jetbrains.com/opensource/). IntelliJ IDEA is the recommended IDE for working with BankAccounts.
