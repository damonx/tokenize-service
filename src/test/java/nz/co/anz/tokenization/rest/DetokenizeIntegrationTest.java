package nz.co.anz.tokenization.rest;

import static org.assertj.core.api.Assertions.assertThat;

import nz.co.anz.tokenization.data.TokenRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ProblemDetail;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

/**
 * This is the integration test for /detokenize endpoint.
 */
@Tag("integration")
@DisplayName("/detokenize Integration Tests")
@TestPropertySource(locations = {"/integration-test.properties"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "classpath:sql/clear-test.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class DetokenizeIntegrationTest {

    @Autowired
    private WebTestClient webClient;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private CacheManager cacheManager;

    @AfterEach
    void clearCache() {
        Optional.ofNullable(cacheManager.getCache("tokenToAccount"))
            .ifPresent(Cache::clear);
    }

    @Test
    @DisplayName("Detokenize fails when request body is missing")
    void detokenizeFailsWhenRequestBodyMissing() {
        // GIVEN, WHEN
        final EntityExchangeResult<ProblemDetail> result = webClient.post()
            .uri("/detokenize")
            .exchange()
            .expectStatus().isBadRequest()
            .expectBody(ProblemDetail.class)
            .returnResult();

        // THEN
        assertThat(result.getResponseBody().getDetail()).isEqualTo("Request body is required");
        assertThat(tokenRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("Detokenize fails when more than 50 tokens are provided")
    void detokenizeFailsWhenTooManyTokens() {
        // GIVEN
        final List<String> tokens = IntStream.range(0, 51)
            .mapToObj(i -> "A".repeat(32))
            .toList();

        // WHEN
        final EntityExchangeResult<String> result = webClient.post()
            .uri("/detokenize")
            .bodyValue(tokens)
            .exchange()
            .expectStatus().isBadRequest()
            .expectBody(String.class)
            .returnResult();

        // THEN
        assertThat(result.getResponseBody()).contains("Maximum 50 tokens per request");
        assertThat(tokenRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("Detokenize fails when token format is invalid")
    void detokenizeFailsWhenTokenFormatInvalid() {
        // GIVEN
        final List<String> invalidTokens = List.of("invalid-token");

        // WHEN
        final EntityExchangeResult<String> result = webClient.post()
            .uri("/detokenize")
            .bodyValue(invalidTokens)
            .exchange()
            .expectStatus().isBadRequest()
            .expectBody(String.class)
            .returnResult();

        // THEN
        assertThat(result.getResponseBody()).contains("Wrong token format.");
        assertThat(tokenRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("Detokenize successfully resolves a single token cache miss and result has been cached.")
    @Sql({ "classpath:sql/existing-token-mapping.sql"})
    void detokenizeResolvesSingleTokenSuccessfully() {
        // GIVEN There is an existing token to account record in database table.
        // Cache is empty.
        final List<String> request = List.of("uS8vN3dph7ttuKMHbuk4Hsbbln1aAvLY");

        // WHEN
        final EntityExchangeResult<List<String>> result = webClient.post()
            .uri("/detokenize")
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk()
            .expectBody(new ParameterizedTypeReference<List<String>>() {})
            .returnResult();

        final List<String> accounts = result.getResponseBody();
        assertThat(accounts).containsExactly("1234 5678 9012 3456");

        // Verify cache populated
        final Cache cache = cacheManager.getCache("tokenToAccount");

        assertThat(cache).isNotNull();
        assertThat(cache.get("uS8vN3dph7ttuKMHbuk4Hsbbln1aAvLY"))
            .extracting(Cache.ValueWrapper::get)
            .isEqualTo("1234 5678 9012 3456");
    }

    @Test
    @DisplayName("Detokenize successfully resolves a single token and cache hit.")
    void detokenizeResolvesSingleTokenHitsCacheSuccessfully() {
        // GIVEN There is an existing token to account record in database table.
        // There is an entry in the cache.
        final List<String> request = List.of("uS8vN3dph7ttuKMHbuk4Hsbbln1aAvLY");
        final Cache cache = cacheManager.getCache("tokenToAccount");
        cache.put("uS8vN3dph7ttuKMHbuk4Hsbbln1aAvLY", "1234 5678 9012 3456");

        // WHEN
        final EntityExchangeResult<List<String>> result = webClient.post()
            .uri("/detokenize")
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk()
            .expectBody(new ParameterizedTypeReference<List<String>>() {})
            .returnResult();

        final List<String> accounts = result.getResponseBody();
        assertThat(accounts).containsExactly("1234 5678 9012 3456");

        // Verify the entry in the cache is intact.
        assertThat(cache).isNotNull();
        assertThat(cache.get("uS8vN3dph7ttuKMHbuk4Hsbbln1aAvLY"))
            .extracting(Cache.ValueWrapper::get)
            .isEqualTo("1234 5678 9012 3456");
    }

    @Test
    @DisplayName("Detokenize fails when token does not exist")
    @Sql({ "classpath:sql/existing-token-mapping.sql"})
    void detokenizeFailsWhenTokenNotFound() {
        // GIVEN
        final List<String> request = List.of("ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ");

        // WHEN
        final EntityExchangeResult<String> result = webClient.post()
            .uri("/detokenize")
            .bodyValue(request)
            .exchange()
            .expectStatus().isNotFound()
            .expectBody(String.class)
            .returnResult();

        // THEN
        assertThat(result.getResponseBody()).contains("Token not found: ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ");
    }

    @Test
    @DisplayName("Detokenize resolves multiple tokens correctly")
    @Sql({ "classpath:sql/existing-two-token-mappings.sql"})
    void detokenizeResolvesMultipleTokens() {
        // GIVEN
        final List<String> request = List.of("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB");

        // WHEN
        final EntityExchangeResult<List<String>> result = webClient.post()
            .uri("/detokenize")
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk()
            .expectBody(new ParameterizedTypeReference<List<String>>() {})
            .returnResult();

        final List<String> accounts = result.getResponseBody();
        assertThat(accounts).containsExactly("1111 2222 3333 4444", "5555 6666 7777 8888");

        // Verify cache populated for both tokens
        final Cache cache = cacheManager.getCache("tokenToAccount");

        assertThat(cache).isNotNull();
        assertThat(cache.get("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"))
            .extracting(Cache.ValueWrapper::get)
            .isEqualTo("1111 2222 3333 4444");
        assertThat(cache.get("BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB"))
            .extracting(Cache.ValueWrapper::get)
            .isEqualTo("5555 6666 7777 8888");
    }
}
