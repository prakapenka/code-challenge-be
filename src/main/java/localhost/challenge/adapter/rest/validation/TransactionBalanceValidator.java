package localhost.challenge.adapter.rest.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import javax.money.MonetaryAmount;
import localhost.challenge.service.AllowedCurrencyService;
import localhost.challenge.service.AllowedTransactionService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TransactionBalanceValidator
    implements ConstraintValidator<ValidTransactionBalance, MonetaryAmount> {

  private final AllowedCurrencyService allowedCurrencyService;

  private final AllowedTransactionService allowedTransactionService;

  @Override
  public boolean isValid(MonetaryAmount amount, ConstraintValidatorContext context) {
    final var actualCurrencyUnit = amount.getCurrency();
    if (allowedCurrencyService.isRejected(actualCurrencyUnit)) {
      return false;
    }
    return allowedTransactionService.isAllowedTransaction(amount);
  }
}
