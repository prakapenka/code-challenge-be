package localhost.challenge.service;

import java.util.Objects;
import javax.money.CurrencyUnit;
import javax.money.Monetary;
import localhost.challenge.service.port.IsAllowedCurrency;
import org.springframework.stereotype.Service;

@Service
public class AllowedCurrencyService implements IsAllowedCurrency {

  private static final CurrencyUnit ONLY_EUR_ALLOWED = Monetary.getCurrency("EUR");

  @Override
  public boolean isAllowed(CurrencyUnit currencyUnit) {
    Objects.requireNonNull(currencyUnit, "currency unit is null");
    return Objects.equals(currencyUnit, ONLY_EUR_ALLOWED);
  }
}
