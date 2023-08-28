package localhost.challenge.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import jakarta.persistence.OptimisticLockException;
import javax.money.MonetaryAmount;
import localhost.challenge.config.retry.RetryConfig;
import localhost.challenge.domain.Transaction;
import localhost.challenge.domain.exception.TransactionBalanceException;
import localhost.challenge.service.port.CreateTransaction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
@ContextConfiguration(classes = {TransactionService.class, RetryConfig.class})
class TransactionServiceTest {

  @MockBean private CreateTransaction createTransactionMock;

  @Autowired private TransactionService service;

  @Test
  void testRetry2TimesOnOptimisticLockException() {
    doThrow(OptimisticLockException.class)
        .when(createTransactionMock)
        .performTransaction(any(Transaction.class));
    var transaction = getTransaction();

    assertThrows(TransactionBalanceException.class, () -> service.createTransaction(transaction));

    verify(createTransactionMock, times(2)).performTransaction(eq(transaction));
  }

  @Test
  void testDoNotRetryOnOtherException() {
    doThrow(NullPointerException.class)
        .when(createTransactionMock)
        .performTransaction(any(Transaction.class));
    var transaction = getTransaction();

    assertThrows(TransactionBalanceException.class, () -> service.createTransaction(transaction));

    verify(createTransactionMock, times(1)).performTransaction(eq(transaction));
  }

  private Transaction getTransaction() {
    return new Transaction("testId", "testFrom", "testTo", mock(MonetaryAmount.class));
  }
}
