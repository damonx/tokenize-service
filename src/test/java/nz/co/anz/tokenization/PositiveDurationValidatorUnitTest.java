package nz.co.anz.tokenization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import nz.co.anz.tokenization.validators.PositiveDurationValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;

import java.time.Duration;
import java.util.stream.Stream;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Unit test for {@link PositiveDurationValidator}.
 */
class PositiveDurationValidatorUnitTest
{
    private PositiveDurationValidator positiveDurationValidator;

    @Mock
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp()
    {
        positiveDurationValidator = new PositiveDurationValidator();
    }

    @Test
    @DisplayName("Valid valid ttl should return true.")
    void validCacheDurationConfiguration()
    {
        // GIVEN
        // WHEN
        boolean isValid = positiveDurationValidator.isValid(Duration.parse("PT5M"), context);
        // THEN
        assertThat(isValid).isTrue();
    }

    private static Stream<Arguments> nonPositiveDurationStrings()
    {
        return Stream.of(
            arguments(named("ZERO duration.", "PT0M")),
            arguments(named("Prefix negative duration.", "-PT5M")),
            arguments(named("Negative number duration.", "PT-5M"))
        );
    }

    @ParameterizedTest(name = "{index} -> {0}")
    @MethodSource("nonPositiveDurationStrings")
    @DisplayName("Test invalid cache duration configurations, especially zero and negative duration.")
    void invalidCacheDurationConfigurations(final String duration)
    {
        // GIVEN
        // WHEN
        boolean isValid = positiveDurationValidator.isValid(Duration.parse(duration), context);
        // THEN
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Test null cache duration.")
    void nullCacheDurationConfiguration()
    {
        // GIVEN
        // WHEN
        boolean isValid = positiveDurationValidator.isValid(null, context);
        // THEN
        assertThat(isValid).isFalse();
    }
}
