package localhost.challenge.service.port;

import localhost.challenge.domain.Account;
import localhost.challenge.domain.exception.AccountCreationException;

public interface CreateAccount {
  Account createAccount(Account account) throws AccountCreationException;
}
