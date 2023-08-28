package localhost.challenge.domain.exception;

public class TransactionBalanceException extends RuntimeException {

  public TransactionBalanceException(String message) {
    super(message, null, true, false);
  }

  public TransactionBalanceException(String message, Throwable e) {
    super(message, e);
  }
}
