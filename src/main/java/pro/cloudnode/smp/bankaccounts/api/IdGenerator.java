package pro.cloudnode.smp.bankaccounts.api;

import org.jetbrains.annotations.NotNull;

import java.security.SecureRandom;

/**
 * Represents an ID generator.
 */
public final class IdGenerator {
    /**
     * Base58 ID generator.
     * <p>
     * Uses 1–9, A–Z except I and O, and a–z except l.
     */
    public static final @NotNull IdGenerator BASE58 = new IdGenerator(
            "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz");

    private static final SecureRandom RANDOM = new SecureRandom();
    private final @NotNull CharSequence charset;
    private final int base;

    /**
     * Constructs a new ID generator.
     *
     * @param charset the character set to use for generating IDs
     */
    public IdGenerator(final @NotNull CharSequence charset) {
        this.charset = charset;
        this.base = charset.length();
    }

    /**
     * Generates a uniformly distributed, cryptographically secure, pseudo-random string.
     *
     * @param length the length of the generated string
     * @return the generated string
     * @throws IllegalArgumentException if {@code length} is not positive
     */
    @NotNull
    public String random(final int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Length must be positive");
        }

        final StringBuilder result = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            result.append(charset.charAt(RANDOM.nextInt(base)));
        }

        return result.toString();
    }
}
