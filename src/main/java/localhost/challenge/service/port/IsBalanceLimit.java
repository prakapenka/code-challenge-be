package localhost.challenge.service.port;

import java.math.BigDecimal;
import javax.money.MonetaryAmount;

public interface IsBalanceLimit {

  default boolean isRejected(MonetaryAmount amount) {
    final var value = amount.getNumber().numberValue(BigDecimal.class);
    return value.compareTo(getBalanceLimit()) > 0;
  }

  BigDecimal getBalanceLimit();
}
