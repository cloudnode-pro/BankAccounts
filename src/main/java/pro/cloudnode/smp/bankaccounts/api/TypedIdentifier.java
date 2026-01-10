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

package pro.cloudnode.smp.bankaccounts.api;

import org.jetbrains.annotations.NotNull;
import pro.cloudnode.smp.bankaccounts.Serializable;

import java.util.Objects;

/**
 * Represents a typed identifier.
 */
public class TypedIdentifier implements Serializable {
    private final @NotNull Type type;
    private final @NotNull String id;

    /**
     * Constructs a typed identifier.
     *
     * @param type the type
     * @param id   the identifier
     */
    public TypedIdentifier(final @NotNull Type type, final @NotNull String id) {
        this.type = type;
        this.id = id;
    }

    /**
     * Parses a typed identifier from a string in the format {@code <type>:<id>}.
     *
     * @param identifier the identifier string to parse
     * @return the corresponding typed identifier
     * @throws IllegalArgumentException if the identifier format is invalid or the type is unknown
     */
    @NotNull
    public static TypedIdentifier deserialize(final @NotNull String identifier) {
        final int colonIndex = identifier.indexOf(':');
        if (colonIndex == -1) {
            throw new IllegalArgumentException(String.format("Invalid identifier: %s", identifier));
        }
        return new TypedIdentifier(
                Type.deserialize(identifier.substring(0, colonIndex)),
                identifier.substring(colonIndex + 1)
        );
    }

    /**
     * Returns a string representation of this typed identifier in the format {@code <type>:<id>}.
     *
     * @return the identifier string representation
     */
    @Override
    @NotNull
    public String serialize() {
        return type.toString() + ':' + id;
    }

    /**
     * Returns the identifier type.
     *
     * @return the type
     */
    public @NotNull Type type() {
        return type;
    }

    /**
     * Returns the identifier value.
     *
     * @return the identifier
     */
    public @NotNull String id() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof final TypedIdentifier ti)) {
            return false;
        }
        return this.type == ti.type && this.id.equals(ti.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type.serialize(), id);
    }

    /**
     * Represents an identifier type.
     */
    public enum Type implements Serializable {
        /**
         * Represents an account holder.
         */
        HOLDER,

        /**
         * Represents an account.
         */
        ACCOUNT;

        @NotNull
        public static Type deserialize(final @NotNull String value) {
            return valueOf(value.toUpperCase());
        }

        @Override
        public @NotNull String serialize() {
            return name().toLowerCase();
        }
    }
}
