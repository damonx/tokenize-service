package nz.co.anz.tokenization.data;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository for token mappings.
 */
public interface TokenRepository extends JpaRepository<TokenEntity, String> {
    Optional<TokenEntity> findByAccountNumber(final String accountNumber);
}
