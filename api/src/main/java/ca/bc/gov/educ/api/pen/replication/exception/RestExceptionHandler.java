package ca.bc.gov.educ.api.pen.replication.exception;

import ca.bc.gov.educ.api.pen.replication.exception.errors.ApiError;
import lombok.val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.persistence.EntityNotFoundException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * The type Rest exception handler.
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

  /**
   * The constant log.
   */
  private static final Logger log = LoggerFactory.getLogger(RestExceptionHandler.class);

  /**
   * Handle http message not readable response entity.
   *
   * @param ex      the ex
   * @param headers the headers
   * @param status  the status
   * @param request the request
   * @return the response entity
   */
  @Override
  protected ResponseEntity<Object> handleHttpMessageNotReadable(final HttpMessageNotReadableException ex, final HttpHeaders headers, final HttpStatus status, final WebRequest request) {
    val error = "Malformed JSON request";
    log.error("{} ", error, ex);
    return this.buildResponseEntity(new ApiError(BAD_REQUEST, error, ex));
  }

  /**
   * Build response entity response entity.
   *
   * @param apiError the api error
   * @return the response entity
   */
  private ResponseEntity<Object> buildResponseEntity(final ApiError apiError) {
    return new ResponseEntity<>(apiError, apiError.getStatus());
  }

  /**
   * Handles EntityNotFoundException. Created to encapsulate errors with more detail than javax.persistence.EntityNotFoundException.
   *
   * @param ex the EntityNotFoundException
   * @return the ApiError object
   */
  @ExceptionHandler(EntityNotFoundException.class)
  protected ResponseEntity<Object> handleEntityNotFound(
    final EntityNotFoundException ex) {
    final ApiError apiError = new ApiError(NOT_FOUND);
    apiError.setMessage(ex.getMessage());
    log.info("{} ", apiError.getMessage(), ex);
    return this.buildResponseEntity(apiError);
  }

  /**
   * Handles IllegalArgumentException
   *
   * @param ex the InvalidParameterException
   * @return the ApiError object
   */
  @ExceptionHandler(IllegalArgumentException.class)
  protected ResponseEntity<Object> handleInvalidParameter(final IllegalArgumentException ex) {
    final ApiError apiError = new ApiError(BAD_REQUEST);
    apiError.setMessage(ex.getMessage());
    log.error("{} ", apiError.getMessage(), ex);
    return this.buildResponseEntity(apiError);
  }

  /**
   * Handles InvalidParameterException
   *
   * @param ex the InvalidParameterException
   * @return the ApiError object
   */
  @ExceptionHandler(InvalidParameterException.class)
  protected ResponseEntity<Object> handleInvalidParameter(final InvalidParameterException ex) {
    final ApiError apiError = new ApiError(BAD_REQUEST);
    apiError.setMessage(ex.getMessage());
    log.error("{} ", apiError.getMessage(), ex);
    return this.buildResponseEntity(apiError);
  }

  /**
   * Handles MethodArgumentNotValidException. Triggered when an object fails @Valid validation.
   *
   * @param ex      the MethodArgumentNotValidException that is thrown when @Valid validation fails
   * @param headers HttpHeaders
   * @param status  HttpStatus
   * @param request WebRequest
   * @return the ApiError object
   */
  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
    final MethodArgumentNotValidException ex,
    final HttpHeaders headers,
    final HttpStatus status,
    final WebRequest request) {
    final ApiError apiError = new ApiError(BAD_REQUEST);
    apiError.setMessage("Validation error");
    apiError.addValidationErrors(ex.getBindingResult().getFieldErrors());
    apiError.addValidationError(ex.getBindingResult().getGlobalErrors());
    log.error("{} ", apiError.getMessage(), ex);
    return this.buildResponseEntity(apiError);
  }


}
