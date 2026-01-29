package nz.co.anz.tokenization.utils;

import java.security.SecureRandom;

/**
 * Utility class responsible for generating opaque, random tokens.
 * <p>
 * A {@link SecureRandom} instance is used to ensure that generated tokens
 * are sufficiently unpredictable and resistant to guessing or brute-force
 * attacks, which is especially important in security-adjacent domains such
 * as tokenization of sensitive financial identifiers.
 * </p>
 */
public final class TokenGenerator
{
    /**
     * Character set used for token generation.
     * <p>
     * The character set is explicitly defined to make the entropy model
     * clear, auditable, and easy to reason about.
     * </p>
     */
    private static final String ALPHANUMERIC =
        "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private TokenGenerator() {
        // DO NOT INSTANTIATE ME!.
    }

    public static String generate(int length) {
        final StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ALPHANUMERIC.charAt(SECURE_RANDOM.nextInt(ALPHANUMERIC.length())));
        }
        return sb.toString();
    }
}
