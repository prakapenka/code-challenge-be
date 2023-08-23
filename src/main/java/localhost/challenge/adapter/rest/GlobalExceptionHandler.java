package localhost.challenge.adapter.rest;

import jakarta.validation.ConstraintViolationException;
import localhost.challenge.domain.exception.AccountCreationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  @ExceptionHandler({AccountCreationException.class, ConstraintViolationException.class})
  public final ResponseEntity<Void> handleAccountCreationException(Exception ex) {
    return ResponseEntity.badRequest().build();
  }
}
