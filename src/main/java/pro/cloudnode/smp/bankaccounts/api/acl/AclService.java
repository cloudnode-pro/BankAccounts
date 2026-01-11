/*
 * BankAccounts is a Minecraft economy plugin that enables players to hold multiple bank accounts.
 * Copyright © 2023–2026 Cloudnode OÜ.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <https://www.gnu.org/licenses/>.
 */

package pro.cloudnode.smp.bankaccounts.api.acl;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pro.cloudnode.smp.bankaccounts.api.Repository;
import pro.cloudnode.smp.bankaccounts.api.Service;
import pro.cloudnode.smp.bankaccounts.api.TypedIdentifier;
import pro.cloudnode.smp.bankaccounts.api.account.AccountId;

import javax.sql.DataSource;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents the ACL service.
 */
public final class AclService extends Service {
    private final @NotNull AccountAclRepository account;

    /**
     * Constructs a new ACL service.
     *
     * @param parentLogger the parent logger, or null if none
     * @param dataSource   the database source
     */
    @ApiStatus.Internal
    public AclService(final @Nullable Logger parentLogger, final @NotNull DataSource dataSource) {
        super(parentLogger);
        this.account = new AccountAclRepository(dataSource);
    }

    /**
     * Retrieves the ACL account entry for the specified subject and account.
     *
     * @param subject the subject identifier
     * @param account the account identifier
     * @return the ACL entry if present, otherwise empty
     * @throws ServiceException if the subject type is not allowed for accounts
     */
    @NotNull
    public Optional<AccountAclEntry> get(final @NotNull TypedIdentifier subject, final @NotNull AccountId account)
            throws ServiceException {
        if (subject.type() != TypedIdentifier.Type.HOLDER) {
            throw new InternalException(String.format("Invalid subject type %s for accounts", subject.type().name()));
        }
        try {
            return this.account.get(subject, account.id());
        } catch (final Repository.RepositoryException e) {
            logger.log(
                    Level.SEVERE,
                    String.format("Failed to get ACL for: %s, %s", subject.serialize(), account.serialize()),
                    e
            );
            throw new InternalException("Failed to check for ID collision");
        }
    }

    /**
     * Retrieves the ACL entry for the specified subject and resource.
     *
     * @param subject  the subject identifier
     * @param resource the resource identifier
     * @return the ACL entry if present, otherwise empty
     * @throws ServiceException if the subject type is invalid for the resource or the resource type is unsupported
     */
    @NotNull
    public Optional<? extends AccessControlListEntry<?>> get(
            final @NotNull TypedIdentifier subject,
            final @NotNull TypedIdentifier resource
    ) throws ServiceException {
        return switch (resource.type()) {
            case ACCOUNT -> get(subject, new AccountId(resource.id()));
            default -> throw new InternalException(String.format("%s is not a resource ID", resource.serialize()));
        };
    }
}
