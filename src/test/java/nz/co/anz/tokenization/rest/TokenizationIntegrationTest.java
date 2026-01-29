package nz.co.anz.tokenization.rest;

import static org.assertj.core.api.Assertions.assertThat;

import nz.co.anz.tokenization.data.TokenEntity;
import nz.co.anz.tokenization.data.TokenRepository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ProblemDetail;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

/**
 * This is the integration test for /tokenize endpoint.
 */
@Tag("integration")
@DisplayName("Tokenization Integration Tests")
@TestPropertySource(locations = {"/integration-test.properties"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "classpath:sql/clear-test.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class TokenizationIntegrationTest {
    // Regex for exactly 32 alphanumeric characters
    private static final String TOKEN_REGEX = "^[a-zA-Z0-9]{32}$";

    @Autowired
    private WebTestClient webClient;

    @Autowired
    private TokenRepository tokenRepository;

    @Test
    @DisplayName("Tokenize fails when request body is missing")
    void testTokenizeFailedWhenRequestBodyMissing() {
        // GIVEN,WHEN
        final EntityExchangeResult<ProblemDetail> result =
            webClient.post()
                .uri("/tokenize")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(ProblemDetail.class)
                .returnResult();

        // THEN
        assertThat(result.getResponseBody().getDetail()).isEqualTo("Request body is required");
        assertThat(tokenRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("Tokenize fails when more than 50 account numbers are provided")
    void tokenizeFailsWhenTooManyAccountNumbers() {
        // GIVEN
        final List<String> accountNumbers = IntStream.range(0, 51)
            .mapToObj(i -> "1234 5678 9012 3456")
            .toList();

        // WHEN
        final EntityExchangeResult<String> result = webClient.post()
            .uri("/tokenize")
            .bodyValue(accountNumbers)
            .exchange()
            .expectStatus().isBadRequest()
            .expectBody(String.class)
            .returnResult();

        // THEN
        assertThat(result.getResponseBody()).contains("Maximum 50 account numbers per request");
        assertThat(tokenRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("Tokenize fails when account number format is invalid")
    void tokenizeFailsWhenAccountFormatInvalid() {
        // GIVEN
        final List<String> invalidAccountNumbers = List.of("invalid-account");

        // WHEN
        final EntityExchangeResult<String> result = webClient.post()
            .uri("/tokenize")
            .bodyValue(invalidAccountNumbers)
            .exchange()
            .expectStatus().isBadRequest()
            .expectBody(String.class)
            .returnResult();

        // THEN
        assertThat(result.getResponseBody()).contains("Wrong account number format");
        // AND: no data persisted
        assertThat(tokenRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("Tokenize successfully creates token for valid account number")
    @Transactional(readOnly = true)
    void tokenizeCreatesTokenSuccessfully() {

        // GIVEN
        final List<String> request = List.of("1234 5678 9012 3456");

        // WHEN
        final EntityExchangeResult<List<String>>  tokensInResponse = webClient.post()
            .uri("/tokenize")
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk()
            .expectBody(new ParameterizedTypeReference<List<String>>() {})
            .returnResult();

        // THEN
        final List<String> tokens = tokensInResponse.getResponseBody();
        assertThat(tokens).hasSize(1)
            .allSatisfy(token -> assertThat(token)
                .as("Token %s does not match the expected 32-char alphanumeric format", token)
                .matches(TOKEN_REGEX));
        // Verifies that correct account to token mapping has been persisted.
        final Optional<TokenEntity> entity = tokenRepository.findByAccountNumber("1234 5678 9012 3456");
        assertThat(entity).isPresent();
        assertThat(entity.get().getToken()).isEqualTo(tokens.getFirst());
    }

    @Test
    @DisplayName("Tokenize returns existing token when account number already tokenized")
    @Sql({ "classpath:sql/existing-token-mapping.sql"})
    void tokenizeReturnsExistingToken()
    {
        // GIVEN
        final List<String> request = List.of("1234 5678 9012 3456");
        final String existingToken = "uS8vN3dph7ttuKMHbuk4Hsbbln1aAvLY";

        // WHEN
        final EntityExchangeResult<List<String>> tokensInResponse = webClient.post()
            .uri("/tokenize")
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk()
            .expectBody(new ParameterizedTypeReference<List<String>>() {})
            .returnResult();

        // THEN
        assertThat(tokensInResponse.getResponseBody()).containsExactly(existingToken);
        // AND: Verify the database record has NOT been touched/modified
        // We fetch the record and compare it against our expected values
        TokenEntity existingDbRecord = tokenRepository.findById(existingToken).orElseThrow();

        assertThat(existingDbRecord)
            .as("The database record should remain unchanged")
            .extracting(TokenEntity::getAccountNumber)
            .isEqualTo("1234 5678 9012 3456");

        // Also ensure no new records were created
        assertThat(tokenRepository.count())
            .as("Database size should still be 1")
            .isEqualTo(1);
    }
}
