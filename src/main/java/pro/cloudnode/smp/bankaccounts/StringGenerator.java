package pro.cloudnode.smp.bankaccounts;

import org.jetbrains.annotations.NotNull;

import java.security.SecureRandom;
import java.util.StringJoiner;
import java.util.stream.IntStream;

public class StringGenerator {
    /**
     * Generate cryptographically secure random string
     * @param length Length of the string
     * @param characters Characters to use
     */
    public static String generate(int length, @NotNull String characters) {
        return IntStream.range(0, length)
                .mapToObj(i -> Character.toString(randomChar(characters)))
                .collect(() -> new StringJoiner(""), StringJoiner::add, StringJoiner::merge)
                .toString();
    }

    /**
     * Generate cryptographically secure alphanumeric, case-sensitive random string
     * @param length Length of the string
     */
    public static String generate(int length) {
        return generate(length, "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789");
    }

    /**
     * Pick secure random character from a string
     * @param string String to pick from
     * @return Random character
     */
    private static char randomChar(@NotNull String string) {
        return string.charAt(new SecureRandom().nextInt(string.length()));
    }
}
