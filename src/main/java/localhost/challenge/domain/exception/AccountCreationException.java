package localhost.challenge.domain.exception;

public class AccountCreationException extends RuntimeException {

  public AccountCreationException(String message, Exception parent) {
    super(message, parent);
  }
}
