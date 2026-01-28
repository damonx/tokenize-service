
package nz.co.anz.tokenization.exception;

/**
 * Exception thrown when a token cannot be resolved.
 */
public class TokenNotFoundException extends RuntimeException {
    public TokenNotFoundException(final String token) {
        super("Token not found: " + token);
    }
}
