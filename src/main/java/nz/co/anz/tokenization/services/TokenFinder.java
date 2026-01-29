package nz.co.anz.tokenization.services;

import nz.co.anz.tokenization.exception.TokenNotFoundException;

/**
 * Service interface responsible for resolving a token into its original value.
 */
public interface TokenFinder
{
    /**
     * Resolves the given token to its original value.
     *
     * @param token the token to be resolved; must not be {@code null}
     * @return the original value associated with the token
     * @throws TokenNotFoundException if the token cannot be resolved
     * @throws IllegalArgumentException if the token is invalid
     */
    String resolve(String token);
}
