package nz.co.anz.tokenization.data;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for token mappings. */
public interface TokenRepository extends JpaRepository<TokenEntity, String> {
  Optional<TokenEntity> findByAccountNumber(final String accountNumber);
}
