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
