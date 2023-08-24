package localhost.challenge.adapter.rest;

import jakarta.validation.ConstraintViolationException;
import localhost.challenge.adapter.rest.dto.error.ErrorDTO;
import localhost.challenge.domain.exception.AccountCreationException;
import localhost.challenge.domain.exception.TransactionBalanceException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  @ExceptionHandler({AccountCreationException.class, ConstraintViolationException.class})
  public ResponseEntity<Void> handleAccountCreationException(Exception ex) {
    // simply log
    logger.warn(ex);
    return ResponseEntity.badRequest().build();
  }

  @ExceptionHandler(TransactionBalanceException.class)
  public ResponseEntity<ErrorDTO> handleTransactionFailure(TransactionBalanceException ex) {
    var errorInfo = new ErrorDTO(ex.getMessage());
    return ResponseEntity.badRequest().body(errorInfo);
  }
}
