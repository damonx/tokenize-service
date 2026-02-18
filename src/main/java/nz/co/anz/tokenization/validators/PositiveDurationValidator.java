package nz.co.anz.tokenization.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.Duration;

/**
 * Validator implementation for {@link PositiveDuration}.
 *
 * <p>Considers a value valid only if it is non-null and strictly greater than zero. Zero or
 * negative values are rejected.
 */
public class PositiveDurationValidator implements ConstraintValidator<PositiveDuration, Duration> {

  @Override
  public boolean isValid(final Duration value, final ConstraintValidatorContext context) {
    return value != null && value.isPositive();
  }
}
