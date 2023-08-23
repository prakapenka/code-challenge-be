package localhost.challenge.adapter.rest.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.math.BigDecimal;
import javax.money.CurrencyUnit;
import javax.money.Monetary;
import localhost.challenge.adapter.rest.dto.AccountBalanceDTO;

public class CreateBalanceValidator
    implements ConstraintValidator<ValidCreateBalance, AccountBalanceDTO> {

  private static final CurrencyUnit ONLY_EUR_ALLOWED = Monetary.getCurrency("EUR");
  private static final BigDecimal MAX_ACCEPTED = new BigDecimal(1_000_000);
  private static final BigDecimal HUNDRED = new BigDecimal(100);

  @Override
  public boolean isValid(AccountBalanceDTO balanceDTO, ConstraintValidatorContext context) {
    final var actualCurrencyUnit = balanceDTO.currencyUnit();

    if (!actualCurrencyUnit.equals(ONLY_EUR_ALLOWED)) {
      // only EUR
      return false;
    }

    final var actualBalance = balanceDTO.amount();
    if (actualBalance.compareTo(MAX_ACCEPTED) > 0) {
      // do not allow more than one million as initial balance value
      return false;
    } else if (actualBalance.compareTo(BigDecimal.ZERO) < 0) {
      // do not allow negative balance
      return false;
    }

    BigDecimal fraction = actualBalance.multiply(HUNDRED).remainder(BigDecimal.ONE);
    if (fraction.compareTo(BigDecimal.ZERO) > 0) {
      // do not allow fractions greater then .99, i.e. 0.101 EUR is forbidden
      return false;
    }

    return true;
  }
}
