package localhost.challenge.service.port;

import localhost.challenge.domain.Transaction;

public interface CreateTransaction {
  void performTransaction(Transaction transaction);
}
