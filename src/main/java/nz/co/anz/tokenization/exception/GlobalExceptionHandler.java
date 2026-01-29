package nz.co.anz.tokenization.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;

/**
 * Global REST exception handler for the Tokenization service.
 *
 * <p>This class centralises exception handling across all REST controllers and
 * converts application and framework exceptions into consistent
 * {@link org.springframework.http.ProblemDetail} responses.</p>
 *
 * <p>The handler ensures:
 * <ul>
 *   <li>Clear and standardised error responses</li>
 *   <li>Appropriate HTTP status codes</li>
 *   <li>Useful diagnostic information for clients</li>
 *   <li>Centralised logging of exceptional scenarios</li>
 * </ul>
 * </p>
 *
 * <p>This class is applied globally via {@link RestControllerAdvice}.</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static Logger logger = LogManager.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles {@link TokenNotFoundException} thrown when a token cannot be resolved.
     *
     * <p>Returns a {@code 404 Not Found} response with a descriptive error message.</p>
     *
     * @param ex the thrown {@link TokenNotFoundException}
     * @return a {@link ProblemDetail} describing the error
     */
    @ExceptionHandler(TokenNotFoundException.class)
    public ProblemDetail handleTokenNotFound(final TokenNotFoundException ex) {
        logger.warn("Token not found: {}", ex.getMessage());

        final ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND,
            ex.getMessage()
        );
        problemDetail.setTitle("Token Not Found");
        problemDetail.setProperty("Timestamp", Instant.now());
        return problemDetail;
    }

    /**
     * Handles {@link MethodArgumentNotValidException} triggered by request body
     * validation failures (e.g. {@code @Valid} annotated request payloads).
     *
     * <p>Aggregates all validation error messages and returns them as part of a
     * {@code 400 Bad Request} response.</p>
     *
     * @param ex the validation exception
     * @return a {@link ProblemDetail} containing validation errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleMethodArgumentNotValid(final MethodArgumentNotValidException ex) {

        final List<String> errors = ex.getBindingResult()
            .getAllErrors()
            .stream()
            .map(error -> error.getDefaultMessage())
            .toList();

        final ProblemDetail problemDetail = getProblemDetail(errors);

        return problemDetail;
    }

    /**
     * Handles {@link ConstraintViolationException} thrown by method-level
     * validation constraints.
     *
     * <p>Each constraint violation message is collected and returned in a
     * {@code 400 Bad Request} response.</p>
     *
     * @param ex the constraint violation exception
     * @return a {@link ProblemDetail} containing validation errors
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(ConstraintViolationException ex) {

        final List<String> errors = ex.getConstraintViolations()
            .stream()
            .map(ConstraintViolation::getMessage)
            .toList();

        final ProblemDetail problemDetail = getProblemDetail(errors);

        return problemDetail;
    }

    /**
     * Handles cases where the request body is missing or cannot be deserialised.
     *
     * <p>This typically occurs when a request is sent without a body or contains
     * malformed JSON.</p>
     *
     * @param ex the thrown exception
     * @return a {@link ProblemDetail} indicating a missing or unreadable request body
     */
    @ExceptionHandler({HttpMessageNotReadableException.class})
    public ProblemDetail handleMissingRequestBody(final Exception ex) {
        logger.warn("Request body is missing or unreadable", ex);

        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setTitle("Validation Error");
        problemDetail.setDetail("Request body is required");
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }

    /**
     * Fallback handler for any unhandled exceptions.
     *
     * <p>Returns a generic {@code 500 Internal Server Error} response and logs
     * the exception for diagnostic purposes.</p>
     *
     * @param ex the unexpected exception
     * @return a {@link ProblemDetail} representing an internal server error
     */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(final Exception ex) {
        logger.error("Unhandled exception: ", ex);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "An unexpected error occurred"
        );
        problemDetail.setTitle("Internal Server Error");
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    /**
     * Builds a standard {@code 400 Bad Request} {@link ProblemDetail}
     * for validation failures.
     *
     * @param errors the list of validation error messages
     * @return a populated {@link ProblemDetail} instance
     */
    private ProblemDetail getProblemDetail(final List<String> errors)
    {
        final ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setTitle("Validation Error");
        problemDetail.setDetail("Request validation failed");
        problemDetail.setProperty("errors", errors);
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }
}
