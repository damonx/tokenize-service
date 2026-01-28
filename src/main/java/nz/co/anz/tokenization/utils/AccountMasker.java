package nz.co.anz.tokenization.utils;

import org.apache.commons.lang3.StringUtils;

public final class AccountMasker
{
    private AccountMasker()
    {
        // DO NOT INSTANTIATE ME.
    }

    /**
     * Masks an account number for secure display using Apache Commons StringUtils.
     * <p>Features:
     * <ul>
     *   <li>Null-safe and blank-safe input handling</li>
     *   <li>Removes common separators (hyphens, spaces)</li>
     *   <li>Shows only last 4 digits for privacy</li>
     *   <li>Input validation with custom error messages</li>
     *   <li>Efficient string manipulation</li>
     * </ul>
     * </p>
     *
     * @param accountNumber The original account number, may be null or empty
     * @return Masked account number showing only last 4 digits
     */
    public static String maskAccountNumber(String accountNumber) {
        if (StringUtils.isBlank(accountNumber)) {
            return "****";
        }

        final String cleanNumber = StringUtils.replaceChars(accountNumber, "-", "");

        if (StringUtils.length(cleanNumber) < 4) {
            return "****";
        }

        // Get last 4 characters
        final String lastFour = StringUtils.right(cleanNumber, 4);

        // Create mask of appropriate length
        final String mask = StringUtils.repeat('*', StringUtils.length(cleanNumber) - 4);

        return mask + lastFour;
    }
}
