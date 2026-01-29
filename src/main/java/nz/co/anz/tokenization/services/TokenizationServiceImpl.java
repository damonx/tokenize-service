
package nz.co.anz.tokenization.services;

import nz.co.anz.tokenization.data.TokenEntity;
import nz.co.anz.tokenization.data.TokenRepository;
import nz.co.anz.tokenization.exception.GlobalExceptionHandler;
import nz.co.anz.tokenization.utils.AccountMasker;
import nz.co.anz.tokenization.utils.TokenGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Default implementation of {@link TokenizationService}.
 */
@Service
public class TokenizationServiceImpl implements TokenizationService
{
    private static Logger logger = LogManager.getLogger(GlobalExceptionHandler.class);
    private final TokenRepository repository;
    private final TokenFinder tokenFinder;

    /**
     * Constructor.
     *
     * @param repository the instance of {@link TokenRepository}.
     */
    public TokenizationServiceImpl(final TokenRepository repository, final TokenFinder tokenFinder)
    {
        this.repository = repository;
        this.tokenFinder = tokenFinder;
    }

    @Override
    public List<String> tokenize(final List<String> accountNumbers)
    {
        return accountNumbers.stream()
            .map(this::tokenizeAccountNumber)
            .toList();
    }

    /**
     * Tokenizes the provided account number by either retrieving an existing token
     * from the database or generating a new one.
     * <p>
     * If the account number has been tokenized previously, the existing token is returned.
     * Otherwise, a new 32-character alphanumeric token is generated, persisted to the
     * repository associated with the account number, and then returned.
     * </p>
     *
     * @param accountNumber The raw account number string to be tokenized.
     * @return A unique 32-character alphanumeric token representing the account number.
     */
    private String tokenizeAccountNumber(final String accountNumber)
    {
        logger.info("Tokenizing account number: {}", AccountMasker.maskAccountNumber(accountNumber));
        return repository.findByAccountNumber(accountNumber)
            .map(TokenEntity::getToken)
            .orElseGet(() -> {
                final String token = TokenGenerator.generate(32);
                repository.save(new TokenEntity(token, accountNumber));
                return token;
            });
    }

    @Override
    public List<String> detokenize(final List<String> tokens)
    {
        return tokens.stream()
            .map(tokenFinder::resolve)
            .toList();
    }
}
