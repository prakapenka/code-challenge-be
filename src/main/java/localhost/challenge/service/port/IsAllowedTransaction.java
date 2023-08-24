package localhost.challenge.service.port;

import java.math.BigDecimal;
import javax.money.CurrencyUnit;
import javax.money.MonetaryAmount;
import org.javamoney.moneta.Money;

public interface IsAllowedTransaction {

  default boolean isAllowedTransaction(BigDecimal amount, CurrencyUnit currencyUnit) {
    int fractionDigits = currencyUnit.getDefaultFractionDigits();
    var multiplayer = BigDecimal.valueOf(Math.pow(10, fractionDigits));
    var fraction = amount.multiply(multiplayer).remainder(BigDecimal.ONE);
    if (fraction.compareTo(BigDecimal.ZERO) > 0) {
      // do not allow fractions greater then .99, i.e. 0.101 EUR is forbidden
      return false;
    }
    return isAllowedTransaction(Money.of(amount, currencyUnit));
  }

  boolean isAllowedTransaction(MonetaryAmount amount);
}
