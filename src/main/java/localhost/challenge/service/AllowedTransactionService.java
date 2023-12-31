package localhost.challenge.service;

import java.math.BigDecimal;
import javax.money.MonetaryAmount;
import localhost.challenge.service.port.IsAllowedTransaction;
import org.springframework.stereotype.Service;

/** Do not allow zero and negative transactions, check for transaction limits */
@Service
public class AllowedTransactionService implements IsAllowedTransaction {

  private static final BigDecimal MAX_ACCEPTED = new BigDecimal(1_000_000);

  @Override
  public boolean isAllowedTransaction(MonetaryAmount amount) {
    if (isRejectedFraction(amount)) {
      return false;
    }

    final var actualBalance = amount.getNumber().numberValue(BigDecimal.class);
    // do not allow negative balance
    if (actualBalance.compareTo(MAX_ACCEPTED) > 0) {
      // do not allow more than one million as initial balance value
      return false;
    } else {
      return actualBalance.compareTo(BigDecimal.ZERO) >= 0;
    }
  }

  private boolean isRejectedFraction(MonetaryAmount amount) {
    int fractionDigits = amount.getCurrency().getDefaultFractionDigits();
    var multiplayer = BigDecimal.valueOf(Math.pow(10, fractionDigits));

    var exactValue = amount.getNumber().numberValue(BigDecimal.class);

    var fraction = exactValue.multiply(multiplayer).remainder(BigDecimal.ONE);
    // do not allow fractions greater then .99, i.e. 0.101 EUR is forbidden
    return fraction.compareTo(BigDecimal.ZERO) > 0;
  }
}
