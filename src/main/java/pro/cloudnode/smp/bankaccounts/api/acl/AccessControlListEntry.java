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

import org.jetbrains.annotations.NotNull;
import pro.cloudnode.smp.bankaccounts.Serializable;
import pro.cloudnode.smp.bankaccounts.api.TypedIdentifier;

import java.time.Instant;

/**
 * Represents an access control list entry.
 *
 * @param <T> the relation type
 */
public abstract class AccessControlListEntry<T extends AccessControlListEntry.Relation> {
    private final @NotNull TypedIdentifier subject;
    private @NotNull T relation;
    private final @NotNull TypedIdentifier resource;
    private final @NotNull Instant created;

    AccessControlListEntry(
            final @NotNull TypedIdentifier subject,
            final @NotNull T relation,
            final @NotNull TypedIdentifier resource,
            final @NotNull Instant created
    ) {
        this.subject = subject;
        this.relation = relation;
        this.resource = resource;
        this.created = created;
    }

    /**
     * Returns the identifier of the subject.
     *
     * @return the subject ID
     */
    @NotNull
    public TypedIdentifier subject() {
        return subject;
    }

    /**
     * Returns the relation of the subject to the resource.
     *
     * @return the relation
     */
    @NotNull
    public T relation() {
        return relation;
    }

    /**
     * Sets the relation of the subject to the resource.
     *
     * @param relation the new relation
     */
    public void relation(final @NotNull T relation) {
        this.relation = relation;
    }

    /**
     * Returns the identifier of the resource.
     *
     * @return the resource ID
     */
    @NotNull
    public TypedIdentifier resource() {
        return resource;
    }

    /**
     * Returns the timestamp when the ACL entry was created.
     *
     * @return the creation timestamp
     */
    @NotNull
    public Instant created() {
        return created;
    }

    /**
     * Represents the relation of a subject to a resource.
     */
    public interface Relation extends Serializable {
    }
}
