package localhost.challenge.service.port;

import java.math.BigDecimal;

public interface IsBalanceLimit {

  default boolean isRejected(BigDecimal amount) {
    return amount.compareTo(getBalanceLimit()) > 0;
  }

  BigDecimal getBalanceLimit();
}
