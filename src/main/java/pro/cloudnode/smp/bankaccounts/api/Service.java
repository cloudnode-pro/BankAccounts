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

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Logger;

/**
 * Represents an API service.
 */
@ApiStatus.Internal
public abstract class Service {
    /**
     * Logger for the service.
     */
    @ApiStatus.Internal
    protected final @NotNull Logger logger;

    /**
     * Constructs a new service with an optional parent logger.
     *
     * @param parentLogger the parent logger, or null if none
     */
    @ApiStatus.Internal
    protected Service(final @Nullable Logger parentLogger) {
        this.logger = Logger.getLogger(parentLogger != null
                ? String.format("%s/%s", parentLogger.getName(), getClass().getSimpleName())
                : getClass().getSimpleName());
        if (parentLogger != null) {
            this.logger.setParent(parentLogger);
        }
    }

    /**
     * Indicates a service-level failure.
     */
    public static abstract class ServiceException extends Exception {
        /**
         * Constructs a new exception with the specified message.
         *
         * @param message the exception message
         */
        @ApiStatus.Internal
        public ServiceException(final @NotNull String message) {
            super(message);
        }
    }

    /**
     * Indicates a generic internal error.
     */
    public static final class InternalException extends ServiceException {
        /**
         * Constructs a new exception with the specified message.
         *
         * @param message the exception message
         */
        @ApiStatus.Internal
        public InternalException(final @NotNull String message) {
            super(message);
        }
    }
}
