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
    @ParameterizedTest(name = "{index} -> when input is: {0}")
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   ", "\t", "\n"})
    @DisplayName("Test null or blank input should have 4 asterisks mask returned.")
    void shouldReturnMaskedForNullOrBlankInput(final String input) {
        assertThat(AccountMasker.maskAccountNumber(input))
            .isEqualTo("****");
    }

    @ParameterizedTest(name = "{index} -> when input is: {0}")
    @ValueSource(strings = {
        "1",
        "12",
        "123",
        "1-2",
        "--1"
    })
    @DisplayName("Test length less than 4 input should have 4 asterisks mask returned.")
    void shouldReturnMaskedWhenLengthLessThanFour(final String input) {
        assertThat(AccountMasker.maskAccountNumber(input))
            .isEqualTo("****");
    }

    @ParameterizedTest(name = "{index} -> when input is: {0}")
    @CsvSource({
        "1234567890123456, ************3456",
        "1234-5678-9012-3456, ************3456",
        "0000-0000-0000-9999, ************9999"
    })
    @DisplayName("Correctly mask account numbers.")
    void shouldMaskAccountNumberCorrectly(final String input, final String expected) {
        assertThat(AccountMasker.maskAccountNumber(input))
            .isEqualTo(expected);
    }

    @DisplayName("Test input with spaces should be masked correctly.")
    @Test
    void shouldMaskAccountNumberWithSpaces() {
        String result = AccountMasker.maskAccountNumber("1234 5678 9012 3456");

        // spaces are preserved because only hyphens are removed
        assertThat(result)
            .endsWith("3456")
            .matches("\\*+3456");

        assertThat(result.length()).isEqualTo(19);
    }
}
