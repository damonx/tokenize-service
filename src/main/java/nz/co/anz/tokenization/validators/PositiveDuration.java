package nz.co.anz.tokenization.validators;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * Ensures that the annotated value represents a strictly positive time span.
 * <p>
 * Accepted formats follow ISO-8601 (e.g. {@code PT5M}, {@code PT30S}).
 * Zero or negative values (e.g. {@code PT0S}, {@code -PT1M}) are rejected.
 *
 * <pre>
 * {@code
 * public class TokenCacheProperties {
 *
 *     @PositiveDuration
 *     private Duration ttl;
 * }
 * }
 * </pre>
 */
@Constraint(validatedBy = PositiveDurationValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface PositiveDuration {
    /**
     * Message to be displayed when validation fails.
     *
     * @return the message
     */
    String message() default "Cache TTL duration must be positive";

    /**
     * Groups for the constraint.
     *
     * @return the groups
     */
    Class<?>[] groups() default {};

    /**
     * Payload for the constraint.
     *
     * @return the payload
     */
    Class<? extends Payload>[] payload() default {};
}
