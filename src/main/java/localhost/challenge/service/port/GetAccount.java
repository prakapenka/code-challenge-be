package localhost.challenge.service.port;

import java.util.Optional;
import localhost.challenge.domain.Account;

public interface GetAccount {
  Optional<Account> getAccount(String id);
}
