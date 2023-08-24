package localhost.challenge.service;

import jakarta.persistence.OptimisticLockException;
import localhost.challenge.domain.Transaction;
import localhost.challenge.domain.exception.TransactionBalanceException;
import localhost.challenge.service.port.CreateTransaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

  private final CreateTransaction createTransactionPort;

  @Retryable(
      retryFor = OptimisticLockException.class,
      maxAttempts = 2,
      backoff = @Backoff(delay = 200),
      recover = "recoverTransaction")
  public void createTransaction(Transaction transaction) {
    createTransactionPort.performTransaction(transaction);
  }

  @Recover
  void recoverTransaction(Throwable exception, Transaction transaction) {
    log.error("transaction failed to process {}", transaction);
    if (exception instanceof TransactionBalanceException ex) {
      throw ex;
    } else {
      // here signal somehow unexpected error happens

      // rethrow for exception handler
      throw new TransactionBalanceException("Transaction failed", exception);
    }
  }
}
