package localhost.challenge.service.port;

import javax.money.CurrencyUnit;

public interface IsAllowedCurrency {

  default boolean isRejected(CurrencyUnit currencyUnit) {
    return !isAllowed(currencyUnit);
  }

  boolean isAllowed(CurrencyUnit currencyUnit);
}
