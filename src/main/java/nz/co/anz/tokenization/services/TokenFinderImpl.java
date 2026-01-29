package nz.co.anz.tokenization.services;

import nz.co.anz.tokenization.data.TokenRepository;
import nz.co.anz.tokenization.exception.TokenNotFoundException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Component
public class TokenFinderImpl implements TokenFinder {

    private final TokenRepository repository;

    /**
     * Constructor.
     * @param repository the token repository.
     */
    public TokenFinderImpl(final TokenRepository repository)
    {
        this.repository = repository;
    }

    @Cacheable(cacheNames = "tokenToAccount", key = "#token")
    @Override
    public String resolve(final String token) {
        return repository.findById(token)
            .orElseThrow(() -> new TokenNotFoundException(token))
            .getAccountNumber();
    }
}
