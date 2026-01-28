
package nz.co.anz.tokenization.rest;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import nz.co.anz.tokenization.services.TokenizationService;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller exposing tokenization endpoints.
 */
@Validated
@RestController
@RequestMapping
public class TokenizationController {

    private final TokenizationService tokenizationService;

    public TokenizationController(TokenizationService tokenizationService) {
        this.tokenizationService = tokenizationService;
    }

    /**
     * Improvement: this endpoint should be authenticated/authorised, and also rate limited.
     * @param accountNumbers
     * @return
     */
    @PostMapping("/tokenize")
    public List<String> tokenize(@RequestBody @NotEmpty @Size(max = 50, message = "Maximum 50 account numbers per request")
        final List<@Pattern(regexp = "^(\\d{4}[-\\s]?){3}\\d{4}$", message = "Wrong account number format") String> accountNumbers) {
        return tokenizationService.tokenize(accountNumbers);
    }

    /**
     * Improvement: this endpoint should be authenticated/authorised, and also rate limited.
     */
    @PostMapping("/detokenize")
    public List<String> detokenize(@RequestBody @NotEmpty @Size(max = 50, message = "Maximum 50 tokens per request")
        final List<@Pattern(regexp = "^[A-Za-z0-9]{32}$", message = "Wrong token format.") String> tokens) {
        return tokenizationService.detokenize(tokens);
    }
}
