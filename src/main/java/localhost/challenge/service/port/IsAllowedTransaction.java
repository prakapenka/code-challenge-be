package localhost.challenge.service.port;

import javax.money.MonetaryAmount;

public interface IsAllowedTransaction {

  boolean isAllowedTransaction(MonetaryAmount amount);
}
