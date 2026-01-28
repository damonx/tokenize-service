
package nz.co.anz.tokenization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

import nz.co.anz.tokenization.data.TokenEntity;
import nz.co.anz.tokenization.data.TokenRepository;
import nz.co.anz.tokenization.exception.TokenNotFoundException;
import nz.co.anz.tokenization.services.TokenFinder;
import nz.co.anz.tokenization.services.TokenFinderImpl;
import nz.co.anz.tokenization.services.TokenizationService;
import nz.co.anz.tokenization.services.TokenizationServiceImpl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

/**
 * Unit test for {@link TokenizationService}.
 */
@DisplayName("Unit test for TokenizationService")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TokenizationServiceUnitTest.TestConfig.class)
class TokenizationServiceUnitTest
{
    @Configuration
    static class TestConfig {

        @Bean
        TokenRepository tokenRepository() {
            return mock(TokenRepository.class);
        }

        @Bean
        TokenFinder tokenFinder()
        {
            return new TokenFinderImpl(tokenRepository());
        }

        @Bean
        TokenizationService tokenizationService(final TokenRepository tokenRepository, final TokenFinder tokenFinder) {
            return new TokenizationServiceImpl(tokenRepository, tokenFinder);
        }
    }

    @Autowired
    private TokenizationService tokenizationService;

    @Autowired
    private TokenRepository tokenRepository;

    @Captor
    private ArgumentCaptor<TokenEntity> tokenEntityCaptor;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
        Mockito.reset(tokenRepository);
    }

    @DisplayName("Tokenize should return existing token when one account in request is already tokenized")
    @Test
    void testTokenizeIdempotentBehavior() {
        // GIVEN
        final String accountNumberInRequest = "4111-1111-1111-1111";
        final String existingToken = "EXISTING_TOKEN";

        when(tokenRepository.findByAccountNumber(accountNumberInRequest))
            .thenReturn(Optional.of(new TokenEntity(existingToken, accountNumberInRequest)));

        // WHEN
        final List<String> result = tokenizationService.tokenize(List.of(accountNumberInRequest));

        // THEN
        assertThat(result).containsExactly(existingToken);
        verify(tokenRepository).findByAccountNumber(accountNumberInRequest);
    }

    @DisplayName("Tokenize should return correct tokens for multiple accounts")
    @Test
    void testTokenizeMultipleAccounts() {
        // GIVEN
        final String account1 = "4111-1111-1111-1111";
        final String account2 = "5222-2222-2222-2222";
        final String token1 = "TOKEN_ONE";
        final String token2 = "TOKEN_TWO";

        when(tokenRepository.findByAccountNumber(account1))
            .thenReturn(Optional.of(new TokenEntity(token1, account1)));
        when(tokenRepository.findByAccountNumber(account2))
            .thenReturn(Optional.of(new TokenEntity(token2, account2)));

        // WHEN
        final List<String> result = tokenizationService.tokenize(List.of(account1, account2));

        // THEN
        assertThat(result).containsExactly(token1, token2);
        verify(tokenRepository).findByAccountNumber(account1);
        verify(tokenRepository).findByAccountNumber(account2);
    }

    @DisplayName("Tokenize should generate and persist token when one account in request is new")
    @Test
    void testTokenizeNewAccount() {
        // GIVEN
        final String accountNumberInRequest = "4444-3333-2222-1111";
        when(tokenRepository.findByAccountNumber(accountNumberInRequest))
            .thenReturn(Optional.empty());

        // WHEN
        final List<String> tokens = tokenizationService.tokenize(List.of(accountNumberInRequest));

        // THEN
        assertThat(tokens)
            .hasSize(1)
            .first()
            .asString()
            .hasSize(32)
            .matches("^[A-Za-z0-9]+$");
        verify(tokenRepository).save(Mockito.any(TokenEntity.class));
    }

    @DisplayName("Tokenize should generate and persist tokens when multiple accounts in request are new")
    @Test
    void testTokenizeMultipleNewAccounts() {
        // GIVEN
        final String account1 = "4444-3333-2222-1111";
        final String account2 = "9999-8888-7777-6666";

        when(tokenRepository.findByAccountNumber(anyString())).thenReturn(Optional.empty());

        // WHEN
        final List<String> tokens = tokenizationService.tokenize(List.of(account1, account2));

        // THEN
        assertThat(tokens).hasSize(2);

        // Verify both tokens are 32-char alphanumeric
        tokens.forEach(token ->
            assertThat(token).hasSize(32).matches("^[A-Za-z0-9]+$")
        );

        // Verify tokens are unique
        assertThat(tokens.getFirst()).isNotEqualTo(tokens.get(1));

        // Verify two different entities were saved
        verify(tokenRepository, times(2)).save(tokenEntityCaptor.capture());

        final List<TokenEntity> savedEntities = tokenEntityCaptor.getAllValues();
        assertThat(savedEntities).extracting(TokenEntity::getAccountNumber)
            .containsExactlyInAnyOrder(account1, account2);
    }

    @DisplayName("Detokenize should return original account number for valid token")
    @Test
    void testDetokenizeSuccess() {
        // GIVEN
        final String token = "VALID_TOKEN";
        final String accountNumber = "4444-1111-2222-3333";
        when(tokenRepository.findById(token))
            .thenReturn(Optional.of(new TokenEntity(token, accountNumber)));

        // WHEN
        final List<String> accounts = tokenizationService.detokenize(List.of(token));

        // THEN
        assertThat(accounts).containsExactly(accountNumber);
        verify(tokenRepository).findById(token);
    }

    @DisplayName("Detokenize should return original account numbers for multiple valid tokens")
    @Test
    void testDetokenizeMultipleAccounts() {
        // GIVEN
        final String token1 = "TOKEN_ONE";
        final String account1 = "1111-2222-3333-4444";
        final String token2 = "TOKEN_TWO";
        final String account2 = "5555-6666-7777-8888";

        when(tokenRepository.findById(token1))
            .thenReturn(Optional.of(new TokenEntity(token1, account1)));
        when(tokenRepository.findById(token2))
            .thenReturn(Optional.of(new TokenEntity(token2, account2)));

        // WHEN
        final List<String> accounts = tokenizationService.detokenize(List.of(token1, token2));

        // THEN
        assertThat(accounts).containsExactly(account1, account2);
        verify(tokenRepository).findById(token1);
        verify(tokenRepository).findById(token2);
    }

    @DisplayName("Detokenize should throw TokenNotFoundException when token does not exist")
    @Test
    void testDetokenizeTokenNotFound() {
        // GIVEN
        final String token = "UNKNOWN_TOKEN";
        when(tokenRepository.findById(token)).thenReturn(Optional.empty());

        // WHEN, THEN
        assertThatThrownBy(() -> tokenizationService.detokenize(List.of(token)))
            .isInstanceOf(TokenNotFoundException.class)
            .hasMessage("Token not found: " + token);
        verify(tokenRepository).findById(token);
    }
}
