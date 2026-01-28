package nz.co.anz.tokenization.services;

import nz.co.anz.tokenization.exception.TokenNotFoundException;

import java.util.List;

/**
 * Service interface defining tokenization and detokenization operations
 * for sensitive account identifiers.
 */
public interface TokenizationService
{
    /**
     * Tokenizes a collection of account numbers.
     * <p>
     * Each input account number will be replaced with a generated token.
     * If an account number has already been tokenized previously, the existing
     * token may be returned to ensure idempotency.
     * </p>
     *
     * @param accountNumbers a list of account numbers to be tokenized;
     *                 must not be {@code null} or empty
     * @return a list of tokens corresponding one-to-one with the input
     *         account numbers, in the same order
     * @throws IllegalArgumentException if the input list is null or empty
     */
    List<String> tokenize(List<String> accountNumbers);

    /**
     * Detokenizes a collection of tokens back to their original account numbers.
     * <p>
     * Each token is resolved to its original account number using the
     * token-to-account mapping maintained by the service.
     * </p>
     *
     * @param tokens a list of tokens to be detokenized;
     *               must not be {@code null} or empty
     * @return a list of original account numbers corresponding one-to-one
     *         with the input tokens, in the same order
     * @throws TokenNotFoundException if any token cannot be resolved
     *                                to an account number
     * @throws IllegalArgumentException if the input list is null or empty
     */
    List<String> detokenize(List<String> tokens);
}
