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

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Represents a typed identifier of a resource.
 *
 * @param type the type of the resource
 * @param id   the identifier of the resource
 */
public record TypedIdentifier(@NotNull Type type, @NotNull String id) {
    /**
     * Parses a typed identifier from a string in the format {@code <type>:<id>}.
     *
     * @param identifier the identifier string to parse
     * @return the corresponding typed identifier
     * @throws IllegalArgumentException if the identifier format is invalid or the type is unknown
     */
    @NotNull
    public static TypedIdentifier fromString(final @NotNull String identifier) {
        final int colonIndex = identifier.indexOf(':');
        if (colonIndex == -1) {
            throw new IllegalArgumentException(String.format("Invalid identifier: %s", identifier));
        }
        return new TypedIdentifier(
                Type.fromName(identifier.substring(0, colonIndex)),
                identifier.substring(colonIndex + 1)
        );
    }

    /**
     * Creates a typed identifier for a player.
     *
     * @param player the player to create the identifier for
     * @return the typed identifier for the player
     */
    @NotNull
    public static TypedIdentifier player(final @NotNull OfflinePlayer player) {
        return new TypedIdentifier(Type.PLAYER, player.getUniqueId().toString());
    }

    /**
     * Returns a string representation of this typed identifier in the format {@code <type>:<id>}.
     *
     * @return the identifier string representation
     */
    @Override
    @NotNull
    public String toString() {
        return type.getName() + ':' + id;
    }

    /**
     * Represents the type of resource.
     */
    public enum Type {
        /**
         * {@link pro.cloudnode.smp.bankaccounts.api.account.Account} type.
         */
        ACCOUNT("account"),

        /**
         * {@link org.bukkit.OfflinePlayer} type.
         */
        PLAYER("player");

        private static final @NotNull Map<String, Type> typeMap = Arrays.stream(Type.values())
                .collect(Collectors.toUnmodifiableMap(Type::getName, Function.identity()));
        private final @NotNull String name;

        Type(final @NotNull String name) {
            this.name = name;
        }

        /**
         * Returns the type corresponding to the given name.
         *
         * @param name the name to get the type for
         * @return the corresponding type
         * @throws IllegalArgumentException if the name is unknown
         */
        public static @NotNull Type fromName(final @NotNull String name) {
            final Type type = typeMap.get(name);
            if (type == null) {
                throw new IllegalArgumentException(String.format("Unknown type: %s", name));
            }
            return type;
        }

        /**
         * Returns the name of this type.
         *
         * @return the type name
         */
        public final @NotNull String getName() {
            return name;
        }
    }
}
