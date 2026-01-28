package nz.co.anz.tokenization;
import static org.assertj.core.api.Assertions.assertThat;

import nz.co.anz.tokenization.utils.AccountMasker;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Unit tests for Account Masker.
 */
@DisplayName("Unit tests for Account Masker.")
class AccountMaskerUnitTest
{
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   ", "\t", "\n"})
    void shouldReturnMaskedForNullOrBlankInput(String input) {
        assertThat(AccountMasker.maskAccountNumber(input))
            .isEqualTo("****");
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "1",
        "12",
        "123",
        "1-2",
        "--1"
    })
    void shouldReturnMaskedWhenLengthLessThanFour(String input) {
        assertThat(AccountMasker.maskAccountNumber(input))
            .isEqualTo("****");
    }

    @ParameterizedTest
    @CsvSource({
        "1234567890123456, ************3456",
        "1234-5678-9012-3456, ************3456",
        "0000-0000-0000-9999, ************9999"
    })
    void shouldMaskAccountNumberCorrectly(String input, String expected) {
        assertThat(AccountMasker.maskAccountNumber(input))
            .isEqualTo(expected);
    }

    @Test
    void shouldMaskAccountNumberWithSpaces() {
        String result = AccountMasker.maskAccountNumber("1234 5678 9012 3456");

        // spaces are preserved because only hyphens are removed
        assertThat(result)
            .endsWith("3456")
            .matches("\\*+3456");

        assertThat(result.length()).isEqualTo(19);
    }

    @Test
    void shouldOnlyRevealLastFourDigits() {
        String result = AccountMasker.maskAccountNumber("9999-8888-7777-1234");

        assertThat(result).endsWith("1234");

        assertThat(result.substring(0, result.length() - 4))
            .matches("\\*+");
    }
}
